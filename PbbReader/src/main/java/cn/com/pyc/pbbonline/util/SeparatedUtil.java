package cn.com.pyc.pbbonline.util;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.bean.ContentRight;
import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.MediaUtils;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.bean.SharedClick;
import cn.com.pyc.pbbonline.common.ShareMode;
import cn.com.pyc.pbbonline.db.Shared;
import cn.com.pyc.pbbonline.db.SharedDBManager;
import cn.com.pyc.pbbonline.db.manager.ClickShareDBManger;
import cn.com.pyc.pbbonline.model.SharesReceiveBean;

public class SeparatedUtil
{
	//
	//	/**
	//	 * 获取视频文件DrmFile
	//	 * 
	//	 * @param myProId
	//	 * @param contents
	//	 * @return List<DrmFile>
	//	 */
	//	public List<DrmFile> getDrmFiles(String myProId, List<AlbumContent> contents)
	//	{
	//		List<DrmFile> files = new ArrayList<DrmFile>();
	//		int count = contents.size();
	//		for (int i = 0; i < count; i++)
	//		{
	//			AlbumContent ac = contents.get(i);
	//			if (Fields.MP4.equals(ac.getFileType()))
	//			{
	//				ContentRight mRight = MediaUtils.getInstance().getMediaRight().get(i);
	//				Asset mAsset = MediaUtils.getInstance().getAssetList().get(i);
	//
	//				SZInitInterface.checkFilePath(ac.getMyProId());
	//				String key = (mRight.permitted) ? mAsset.getCek_cipher_value() : "";
	//				String contentId = ac.getContent_id();
	//				String path = new StringBuilder()
	//						.append(PathUtil.DEFAULT_SAVE_FILE_PATH)
	//						.append(File.separator)
	//						.append(myProId)
	//						.append(File.separator)
	//						.append(contentId)
	//						.append(Fields._MP4)
	//						.toString();
	//				path = path.replaceAll("\"", "");
	//				String validityTime = FormatterUtil.getLeftAvailableTime(mRight.availableTime);
	//				String odd_datetime_end = FormatterUtil.getToOddEndTime(mRight.odd_datetime_end);
	//
	//				DrmFile drmFile = new DrmFile(path, null, key);
	//				drmFile.setMyProId(myProId);
	//				drmFile.setContentId(contentId);
	//				drmFile.setOdd_datetime_end(odd_datetime_end);
	//				drmFile.setValidity(validityTime);
	//				drmFile.setTitle(ac.getName());
	//				drmFile.setCheckOpen(mRight.permitted);
	//				drmFile.setAsset_id(ac.getAsset_id());
	//				drmFile.setFormat(ac.getFileType());
	//
	//				files.add(drmFile);
	//			}
	//		}
	//		SZLog.v("", "MP4: " + files.size() + "个");
	//		return files;
	//	}

	/**
	 * 获取可视化包含文件（文件信息和权限信息）
	 * 
	 * @param contents
	 * @param myProId
	 * @param fileType
	 *            PDF,MP3,MP4
	 * @return List<SZFile>
	 */
	public List<SZFile> getSZFiles(List<AlbumContent> contents, String myProId, String fileType)
	{
		List<SZFile> files = new ArrayList<SZFile>();
		//String myProId = album.getMyproduct_id();
		//String albumId = album.getId();
		//String albumPic = album.getPicture();

		SZInitInterface.checkFilePath();
		final int count = contents.size();
		for (int i = 0; i < count; i++)
		{
			AlbumContent ac = contents.get(i);
			if (fileType.equals(ac.getFileType()))
			{
				ContentRight mRight = MediaUtils.getInstance().getMediaRight().get(i);
				Asset mAsset = MediaUtils.getInstance().getAssetList().get(i);

				String key = (mRight.permitted) ? mAsset.getCek_cipher_value() : "";
				String contentId = ac.getContent_id();
				String path = new StringBuilder()
						.append(PathUtil.DEF_SAVE_FILE_PATH)
						.append(File.separator)
						.append(myProId)
						.append(File.separator)
						.append(contentId)
						.append(".")
						.append(ac.getFileType().toLowerCase(Locale.getDefault())).toString();
				path = path.replaceAll("\"", "");
				String validityTime = FormatterUtil.getLeftAvailableTime(mRight.availableTime);
				String odd_datetime_end = FormatterUtil.getToOddEndTime(mRight.odd_datetime_end);

				SZFile file = new SZFile(ac.getName(), path, key);
				//file.setAlbum_id(albumId);
				//file.setAlbum_pic(albumPic);
				file.setMyProId(myProId);
				file.setContentId(contentId);
				file.setAssetId(ac.getAsset_id());
				file.setCheckOpen(mRight.permitted);
				file.setOdd_datetime_end(odd_datetime_end);
				file.setValidity_time(validityTime);
				file.setFormat(ac.getFileType());

				files.add(file);
			}
		}
		SZLog.v("separated", fileType + ": " + files.size() + "个");
		return files;
	}

	/**
	 * 获取所有分享数据，完成后保存到数据库中。耗时操作最好开启线程
	 * 
	 * @param sList
	 * @return
	 */
	public synchronized void resolveShareReceive(List<SharesReceiveBean> sList)
	{
		String account = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
		String defaultAccount = SZInitInterface.getUserName("");
		SharedDBManager dbManager = new SharedDBManager();
		ClickShareDBManger scm = ClickShareDBManger.Builder();
		////List<Shared> shareds = new ArrayList<Shared>();	//调试数据，保存记录

		//查询所有的记录
		List<Shared> allShareds = dbManager.findAll();
		SZLog.i("alls: " + allShareds.size());
		SZLog.i("list: " + sList.size());
		boolean isDeleteUser = false;
		if (allShareds.size() != sList.size())	//存在被回收的分享。（请求数据和保存db的数据不一致）
		{
			//删除：isDelete=false和isRevoke=false的user记录，
			//保证插入的是最新的
			isDeleteUser = dbManager.deleteUserByFlag(false, false);
		}
		if (isDeleteUser && SZInitInterface.isDebugMode)	//调试代码
		{
			for (Shared shared : allShareds)
			{
				if (shared.isRevoke())
				{
					//被回收的记录加入到集合显示
					SZLog.w("revoke: " + shared.getTheme());
					////shareds.add(shared);
				}
			}
		}

		List<Shared> temps = new ArrayList<Shared>();	//临时list储存没有保存的记录
		for (SharesReceiveBean o : sList)
		{
			Shared mSave = dbManager.findByShareId(o.getId());
			if (mSave != null)
			{
				//查询到了数据，记录已保存,且没有删除
				if (!mSave.isDelete())
				{
					////shareds.add(mSave);
				}
			}
			else
			{
				//没有查询到数据，插入保存！
				Shared share = new Shared(o.getId(), o.getTheme(), o.getOwner());
				share.setShareUrl(o.getUrl());
				long time = ConvertToUtil.toLong(o.getReceiveTime());
				if (time == 0)
					time = ConvertToUtil.toLong(o.getCreate_time());
				share.setTime(time);
				share.setShareMode(o.getShare_mode());
				//按设备不需要考虑账户,默认guestpbb;按身份，人数，账户即为登录手机号，二次分享账号可能为""
				boolean device = (ShareMode.SHAREDEVICE.equals(o.getShare_mode()));
				share.setAccountName(device ? defaultAccount : account);
				SharedClick sc = scm.findClickByShareId(o.getId());	//查询对应shareId下的查看状态。
				share.setWhetherNew(sc != null ? !sc.isClick() : true);
				mSave = share;
				share = null;

				temps.add(mSave);
				////dbManager.saveShared(mSave);
				////shareds.add(mSave);
			}
		}
		dbManager.saveSharedAll(temps); //保存时默认已经开启事务
		////return shareds;
	}
}
