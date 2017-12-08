package com.sz.mobilesdk.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.bean.Perconattribute;
import com.sz.mobilesdk.database.bean.Perconstraint;
import com.sz.mobilesdk.database.bean.Permission;
import com.sz.mobilesdk.database.bean.Right;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.database.practice.AlbumDAOImpl;
import com.sz.mobilesdk.database.practice.RightDAOImpl;
import com.sz.mobilesdk.manager.ParserEngine;
import com.sz.mobilesdk.models.FolderInfo;
import com.sz.mobilesdk.models.FileData;
import com.sz.mobilesdk.models.xml.OEX_Agreement.OEX_Asset;
import com.sz.mobilesdk.models.xml.OEX_Agreement.OEX_Permission;
import com.sz.mobilesdk.models.xml.OEX_Rights;
import com.sz.mobilesdk.models.xml.XML2JSON_Album;
import com.sz.mobilesdk.service.DownloadService;
import com.sz.mobilesdk.service.DownloadService2;

/**
 * 解析文件
 * 
 * @author hudq
 * 
 */
public class ParserFileUtil
{

	private static final String TAG = "ParserFile";

	private static ReentrantLock lock;

	private static class ParserFileUtilInner
	{
		private static final ParserFileUtil INSTANCE = new ParserFileUtil();
	}

	private ParserFileUtil()
	{
	}

	public static ParserFileUtil getInstance()
	{
		if (lock == null)
		{
			lock = new ReentrantLock(true);
		}
		return ParserFileUtilInner.INSTANCE;
	}

	private void unlock()
	{
		if (lock != null)
		{
			lock.unlock();
			lock = null;
		}
	}

	private void lock()
	{
		if (lock != null)
		{
			lock.lock();
		}
	}

	private void downloadError(Context context, FileData o2)
	{
		SZLog.e(TAG, "download failed!");
		Intent intent = new Intent(DownloadService2.ACTION_DOWNLOAD_ERROR);
		intent.putExtra("FileData", o2);
		context.sendBroadcast(intent);
	}

	/**
	 * 解析单个文件
	 * 
	 * @param context
	 * @param o
	 */
	public void parserFile(Context context, final FileData o)
	{
		SZLog.e(TAG, "start parser name: " + o.getName());
		context.sendBroadcast(new Intent(DownloadService2.ACTION_PARSERING)
				.putExtra("FileData", o));

		SZInitInterface.checkFilePath();
		// 下载文件路径
		final String drmPath = new StringBuffer()
				.append(PathUtil.DEF_DOWNLOAD_DRM_PATH).append(File.separator)
				.append(o.getFiles_id()).append(Fields._DRM).toString();
		// 解析后文件保存路径
		final String decodePath = new StringBuffer()
				.append(PathUtil.DEF_SAVE_FILE_PATH).append(File.separator)
				.append(o.getSharefolder_id()).toString();

		try
		{
			// 创建下载和解压目录
			FileUtil.createDirectory(drmPath);
			FileUtil.createDirectory(decodePath);

			lock();
			List<ParserEngine.CommonFile> list = ParserEngine.parserDRMFile(
					drmPath, decodePath);
			if (list == null)
			{
				downloadError(context, o);
				return;
			}
			XML2JSON_Album albumInfo = null;
			OEX_Rights rights = null;
			for (ParserEngine.CommonFile c : list)
			{
				if (c.filetype == ParserEngine.FILETYPE.ALBUMINFO)
				{
					albumInfo = ParserEngine.parserJSON(new File(c.filepath),
							list);
				} else if (c.filetype == ParserEngine.FILETYPE.RIGHT)
				{
					rights = ParserEngine.parserRight(new File(c.filepath));
				}
			}
			// 将专辑信息和权限插入表中
			if (albumInfo != null && rights != null)
			{
				if (insertFile(rights, albumInfo, list, o))
				{
					// 插入成功
					SZLog.i("insert success");
					for (ParserEngine.CommonFile c : list)
					{
						if (c.filetype == ParserEngine.FILETYPE.ALBUMINFO
								|| c.filetype == ParserEngine.FILETYPE.RIGHT)
						{
							FileUtil.deleteFileWithPath(c.filepath);
						}
					}
					FileUtil.deleteFileWithPath(drmPath);
				} else
				{
					// 插入失败
					SZLog.i("insert failed or already exist!");
					downloadError(context, o);
				}
			}
			// 解析完毕，发送广播通知更新
			context.sendBroadcast(new Intent(DownloadService2.ACTION_FINISHED)
					.putExtra("FileData", o));
			SZLog.e(TAG, "end parser fileId: " + o.getFiles_id());
		} catch (Exception e)
		{
			e.printStackTrace();
			downloadError(context, o);
		} finally
		{
			unlock();
		}
	}

	/**
	 * 插入单个下载文件所对应的内容
	 * 
	 * @param rights
	 * @param albumInfo
	 * @param mcFiles
	 * @param o
	 * @return
	 */
	public boolean insertFile(OEX_Rights rights, XML2JSON_Album albumInfo,
			List<ParserEngine.CommonFile> mcFiles, FileData o)
	{
		boolean flag = false;
		final List<ParserEngine.CommonFile> mCommonFiles = mcFiles;
		// AlbumContent ac = AlbumContentDAOImpl.getInstance()
		// .findAlbumContentByContentId(o.getFiles_id());
		boolean existData = AlbumContentDAOImpl.getInstance()
				.existAlbumContentByContentId(o.getFiles_id());
		if (existData) return false;
		try
		{
			Right right = new Right();
			long currentTime = System.currentTimeMillis();
			right.setId(String.valueOf(currentTime));
			right.setPro_album_id("0");
			right.setRight_uid(rights.getContextMap().get("uid"));
			right.setRight_version(rights.getContextMap().get("version"));
			DateFormat sDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss", Locale.getDefault());
			String date = sDateFormat.format(new java.util.Date());
			right.setCreate_time(date);
			right.setAccount_id("1");
			right.setUsername(SZInitInterface.getUserName(""));
			// 4、 插入asset内容
			List<OEX_Asset> rightAssets = rights.getAgreement().getAssets();
			LinkedList<Asset> assets = new LinkedList<Asset>();
			for (int i = 0, count = rightAssets.size(); i < count; i++)
			{
				Asset asset = new Asset();
				asset.setId(String.valueOf(currentTime + i));
				asset.setAsset_uid(rightAssets.get(i).getOdd_uid());
				asset.setRight_id(right.getId());
				asset.setCek_cipher_value(rightAssets.get(i).getCipheralue());
				asset.setCek_encrypt_method(rightAssets.get(i)
						.getEnc_algorithm());
				asset.setCek_retrieval_key(rightAssets.get(i)
						.getRetrieval_url());
				asset.setDigest_method(rightAssets.get(i)
						.getDigest_algorithm_key());
				asset.setDigest_value(rightAssets.get(i)
						.getDigest_algorithm_value());
				asset.setCreate_time(date);
				asset.setRight_version(right.getRight_version());
				asset.setUsername(right.getUsername());
				assets.add(asset);
			}
			int n = 0;
			// 3、 插入权限表及约束内容
			List<OEX_Permission> rightPermissions = rights.getAgreement()
					.getPermission();
			for (int i = 0, count = rightPermissions.size(); i < count; i++)
			{
				Permission permission = new Permission();
				permission.setId(String.valueOf(currentTime + i));
				int assetId = Integer.parseInt(rightPermissions.get(i)
						.getAssent_id().substring(5)) - 1;
				String assentId = assets.get(assetId).getId();
				permission.setAsset_id(assentId);
				permission.setCreate_time(date);
				permission.setElement(String.valueOf(rightPermissions.get(i)
						.getType()));
				List<Map<String, String>> attributes = rightPermissions.get(i)
						.getAttributes();
				for (Map<String, String> map : attributes)
				{
					for (Map.Entry<String, String> entry : map.entrySet())
					{
						n++;
						Perconstraint perconstraint = new Perconstraint();
						perconstraint.setId(String.valueOf(currentTime + n));
						perconstraint.setElement(entry.getKey());
						if ("datetime".equals(entry.getKey()))
						{
							Perconattribute startPerconattribute = new Perconattribute();
							Perconattribute endPerconattribute = new Perconattribute();
							String start = rightPermissions.get(i)
									.getStartTime();
							String end = rightPermissions.get(i).getEndTime();
							startPerconattribute.setElement("start");
							startPerconattribute.setValue(start);
							startPerconattribute.setCreate_time(date);
							startPerconattribute
									.setPerconstraint_id(perconstraint.getId());

							endPerconattribute.setElement("end");
							endPerconattribute.setValue(end);
							endPerconattribute.setCreate_time(date);
							endPerconattribute
									.setPerconstraint_id(perconstraint.getId());
							perconstraint
									.addPerconattributes(startPerconattribute);
							perconstraint
									.addPerconattributes(endPerconattribute);
						}
						perconstraint.setPermission_id(permission.getId());
						perconstraint.setValue(entry.getValue());
						perconstraint.setCreate_time(date);
						permission.addPerconstraint(perconstraint);
					}
				}
				assets.get(assetId).addPermission(permission);
			}
			right.setAssets(assets);
			RightDAOImpl.getInstance().cascadedSave(right);

			// jsonObj变成局部变量
			JSONObject albObject = albumInfo.getInfoObj();
			Album album = new Album();
			album.setId(String.valueOf(currentTime));
			album.setName(albObject.getString("albumName"));
			album.setRight_id(albObject.getString("rid"));
			album.setProduct_id(albObject.getString("albumId"));
			album.setModify_time(date);
			album.setCategory(albObject.getString("albumCategory"));
			album.setItem_number(String.valueOf(albumInfo.getContentList()
					.size() / 2));
			album.setPicture(albObject.getString("picture"));
			album.setUsername(SZInitInterface.getUserName(""));
			album.setMyproduct_id(o.getSharefolder_id());
			album.setPublishDate(o.getSharefolder_publish_date());
			album.setAuthor("");
			album.setPicture_ratio("");
			List<String> contents = albumInfo.getContentList();
			for (int i = 0, count = contents.size(); i < count; i += 2)
			{
				AlbumContent albumContent = new AlbumContent();
				for (int j = 0; j < assets.size(); j++)
				{
					String Content_id = contents.get(i).replace("\"", "");
					String odduid = assets.get(j).getAsset_uid();
					if (Content_id.equals(odduid))
					{
						albumContent.setMyProId(album.getMyproduct_id());
						albumContent.setAlbum_id(album.getId());
						albumContent.setModify_time(date);
						albumContent.setName(contents.get(i + 1).replaceAll(
								"\"", ""));
						albumContent.setAsset_id(assets.get(j).getId());
						albumContent.setContent_id(Content_id);
						// 设置文件类型
						for (int k = 0, size = mCommonFiles.size(); k < size; k++)
						{
							ParserEngine.CommonFile file = mCommonFiles.get(k);
							String fileName = file.filename;
							String name = FileUtil
									.getNameFromFileName(fileName);
							if (Content_id.equals(name))
							{
								String extName = String.valueOf(file.filetype);
								SZLog.w(TAG, "extension: " + extName);
								albumContent.setFileType(extName);
							}
						}
						album.addAlbumContent(albumContent);
					}
				}
			}
			SZLog.d("insert2", "album: " + album.toString());
			AlbumDAOImpl.getInstance().cascadedSave(album);
			flag = true;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 解析整个专辑
	 * 
	 * @param context
	 * @param myProid
	 * @param position
	 * @param o
	 *            DownloadInfo.java
	 */
	@Deprecated
	public void parserDRMFiles(Context context, String myProid, int position,
			FolderInfo o)
	{
		Intent intent = new Intent(DownloadService.ACTION_PARSER);
		intent.putExtra("position", o.getPosition());
		intent.putExtra("myProId", myProid);
		intent.putExtra("DownloadInfo", o);
		context.sendBroadcast(intent);

		SZLog.e(TAG, "start parser name: " + o.getProductName());
		SZInitInterface.checkFilePath();
		// 下载文件路径
		final String filePath = new StringBuffer()
				.append(PathUtil.DEF_DOWNLOAD_DRM_PATH).append(File.separator)
				.append(myProid).append(Fields._DRM).toString();
		// 解析后文件保存路径
		final String decodePath = new StringBuffer()
				.append(PathUtil.DEF_SAVE_FILE_PATH).append(File.separator)
				.append(myProid).toString();
		try
		{
			// 创建解压目录
			FileUtil.createDirectory(filePath);
			FileUtil.createDirectory(decodePath);

			lock();
			List<ParserEngine.CommonFile> list = ParserEngine.parserDRMFile(
					filePath, decodePath);
			XML2JSON_Album albumInfo = null;
			OEX_Rights rights = null;
			for (ParserEngine.CommonFile c : list)
			{
				if (c.filetype == ParserEngine.FILETYPE.ALBUMINFO)
				{
					albumInfo = ParserEngine.parserJSON(new File(c.filepath),
							list);
				} else if (c.filetype == ParserEngine.FILETYPE.RIGHT)
				{
					rights = ParserEngine.parserRight(new File(c.filepath));
				}
			}
			// 将专辑信息和权限插入表中
			if (albumInfo != null && rights != null)
			{
				if (insertDRMData(rights, albumInfo, myProid, o, list))
				{
					// 插入成功
					SZLog.i("insert success");
					for (ParserEngine.CommonFile c : list)
					{
						if (c.filetype == ParserEngine.FILETYPE.ALBUMINFO
								|| c.filetype == ParserEngine.FILETYPE.RIGHT)
						{
							FileUtil.deleteFileWithPath(c.filepath);
						}
					}
					FileUtil.deleteFileWithPath(filePath);
				} else
				{
					// 插入失败
					SZLog.i("insert failed");
				}
			}
			// 解析完毕，发送广播通知更新（position参数不可更改名称）
			Intent intent2 = new Intent(DownloadService.ACTION_FINISHED);
			intent2.putExtra("position", position);
			context.sendBroadcast(intent2);
			SZLog.e(TAG, "end parser id: " + myProid);
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			unlock();
		}
	}

	/**
	 * 插入整个专辑
	 * 
	 * @param rights
	 * @param albumInfo
	 * @param myProid
	 * @param o
	 *            {@link FolderInfo}
	 * @param mFiles
	 * @return
	 */
	@Deprecated
	protected boolean insertDRMData(OEX_Rights rights,
			XML2JSON_Album albumInfo, String myProid, FolderInfo o,
			List<ParserEngine.CommonFile> mFiles)
	{
		boolean isInsertDRMData = true;
		final List<ParserEngine.CommonFile> mCommonFiles = mFiles;
		String Album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProid);// 获取专辑ID
		if (Album_Id == null && isInsertDRMData)
		{
			try
			{
				isInsertDRMData = false;
				String mSaveName = SZInitInterface.getUserName("");
				Right right = new Right();
				long currentTime = System.currentTimeMillis();
				right.setId(String.valueOf(currentTime));
				right.setPro_album_id("0");
				right.setRight_uid(rights.getContextMap().get("uid"));
				right.setRight_version(rights.getContextMap().get("version"));
				SimpleDateFormat sDateFormat = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss", Locale.getDefault());
				String date = sDateFormat.format(new java.util.Date());
				right.setCreate_time(date);
				right.setAccount_id("1");
				right.setUsername(mSaveName);
				// 4、 插入asset内容
				List<OEX_Asset> rightAssets = rights.getAgreement().getAssets();
				LinkedList<Asset> assets = new LinkedList<Asset>();
				for (int i = 0, count = rightAssets.size(); i < count; i++)
				{
					Asset asset = new Asset();
					asset.setId(String.valueOf(currentTime + i));
					asset.setAsset_uid(rightAssets.get(i).getOdd_uid());
					asset.setRight_id(right.getId());
					asset.setCek_cipher_value(rightAssets.get(i)
							.getCipheralue());
					asset.setCek_encrypt_method(rightAssets.get(i)
							.getEnc_algorithm());
					asset.setCek_retrieval_key(rightAssets.get(i)
							.getRetrieval_url());
					asset.setDigest_method(rightAssets.get(i)
							.getDigest_algorithm_key());
					asset.setDigest_value(rightAssets.get(i)
							.getDigest_algorithm_value());
					asset.setCreate_time(date);
					asset.setRight_version(right.getRight_version());
					asset.setUsername(right.getUsername());
					assets.add(asset);
				}
				int n = 0;
				// 3、 插入权限表及约束内容
				List<OEX_Permission> rightPermissions = rights.getAgreement()
						.getPermission();
				for (int i = 0, count = rightPermissions.size(); i < count; i++)
				{
					Permission permission = new Permission();
					permission.setId(String.valueOf(currentTime + i));
					int assetId = Integer.parseInt(rightPermissions.get(i)
							.getAssent_id().substring(5)) - 1;
					String assentId = assets.get(assetId).getId();
					permission.setAsset_id(assentId);
					permission.setCreate_time(date);
					permission.setElement(String.valueOf(rightPermissions
							.get(i).getType()));
					List<Map<String, String>> attributes = rightPermissions
							.get(i).getAttributes();
					for (Map<String, String> map : attributes)
					{
						for (Map.Entry<String, String> entry : map.entrySet())
						{
							n++;
							Perconstraint perconstraint = new Perconstraint();
							perconstraint
									.setId(String.valueOf(currentTime + n));
							perconstraint.setElement(entry.getKey());
							if (entry.getKey().equals("datetime"))
							{
								Perconattribute startPerconattribute = new Perconattribute();
								Perconattribute endPerconattribute = new Perconattribute();
								String start = rightPermissions.get(i)
										.getStartTime();
								String end = rightPermissions.get(i)
										.getEndTime();
								startPerconattribute.setElement("start");
								startPerconattribute.setValue(start);
								startPerconattribute.setCreate_time(date);
								startPerconattribute
										.setPerconstraint_id(perconstraint
												.getId());

								endPerconattribute.setElement("end");
								endPerconattribute.setValue(end);
								endPerconattribute.setCreate_time(date);
								endPerconattribute
										.setPerconstraint_id(perconstraint
												.getId());
								perconstraint
										.addPerconattributes(startPerconattribute);
								perconstraint
										.addPerconattributes(endPerconattribute);
							}
							perconstraint.setPermission_id(permission.getId());
							perconstraint.setValue(entry.getValue());
							perconstraint.setCreate_time(date);
							permission.addPerconstraint(perconstraint);
						}
					}
					assets.get(assetId).addPermission(permission);
				}
				right.setAssets(assets);
				// new RightDAOImpl().cascadedSave(right);
				RightDAOImpl.getInstance().cascadedSave(right);

				// jsonObj变成局部变量
				String author = o.getAuthors();
				String picture_ratio = o.getPicture_ratio();
				String publishDate = o.getPublishDate();
				JSONObject albObject = albumInfo.getInfoObj();
				Album album = new Album();
				album.setId(String.valueOf(currentTime));
				album.setName(albObject.getString("albumName"));
				album.setRight_id(albObject.getString("rid"));
				album.setProduct_id(albObject.getString("albumId"));
				album.setModify_time(date);
				album.setCategory(albObject.getString("albumCategory"));
				album.setItem_number(String.valueOf(albumInfo.getContentList()
						.size() / 2));
				album.setUsername(mSaveName);
				album.setPicture(albObject.getString("picture"));
				album.setMyproduct_id(myProid);
				album.setAuthor(author == null ? "PBBOnline" : author);
				album.setPicture_ratio(picture_ratio == null ? "1"
						: picture_ratio);
				album.setPublishDate(publishDate == null ? "" : publishDate);
				List<String> contents = albumInfo.getContentList();
				for (int i = 0, count = contents.size(); i < count; i += 2)
				{
					AlbumContent albumContent = new AlbumContent();
					for (int j = 0; j < assets.size(); j++)
					{
						String Content_id = contents.get(i).replace("\"", "");
						String odduid = assets.get(j).getAsset_uid();
						if (Content_id.equals(odduid))
						{
							albumContent.setMyProId(album.getMyproduct_id());
							albumContent.setAlbum_id(album.getId());
							albumContent.setModify_time(date);
							albumContent.setName(contents.get(i + 1)
									.replaceAll("\"", ""));
							albumContent.setAsset_id(assets.get(j).getId());
							albumContent.setContent_id(Content_id);
							// 设置文件类型
							for (int k = 0; k < mCommonFiles.size(); k++)
							{
								ParserEngine.CommonFile file = mCommonFiles
										.get(k);
								String fileName = file.filename;
								String name = FileUtil
										.getNameFromFileName(fileName);
								if (Content_id.equals(name))
								{
									String extName = String
											.valueOf(file.filetype);
									SZLog.v("extension:", "" + extName);
									albumContent.setFileType(extName);
								}
							}
							album.addAlbumContent(albumContent);
						}
					}
				}
				SZLog.e("insert", "album: " + album.toString());
				AlbumDAOImpl.getInstance().cascadedSave(album);
				isInsertDRMData = true;
			} catch (Exception e)
			{
				isInsertDRMData = false;
				e.printStackTrace();
			}
		}
		return isInsertDRMData;
	}
}
