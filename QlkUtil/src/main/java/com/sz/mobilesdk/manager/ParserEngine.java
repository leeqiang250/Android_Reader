package com.sz.mobilesdk.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sz.mobilesdk.models.xml.OEX_Rights;
import com.sz.mobilesdk.models.xml.XML2JSON_Album;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.PullXMLReader;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.Util;

/**
 * 解析文件
 */
public class ParserEngine
{
	private static final String TAG = "parser";

	/**
	 * drm文件构成 drm分为有头文件和资源文件两部分构成，中间通过32byte的md5做分割 <br/>
	 * 前8byte通过一个long类型变量，记录了整个头文件的长度
	 * <p>
	 * 每个头文件又分为3部分: <br/>
	 * 40个byte记录文件名; 8个byte记录一个long类型变量，表示文件的起点 ; 8个byte记录一个long类型变量，表示文件的终点 <br/>
	 * 前8byte的long类型变量 /(40+8+8) = 头文件的个数，每个头文件都对应着一个资源文件，所以也就得到了资源文件的个数 <br/>
	 * <br/>
	 * 
	 * 解析文件，得到资源文件
	 * 
	 * @param drmPath
	 *            被解析文件路径
	 * @param decodePath
	 *            解析后保存文件路径
	 */
	public static List<CommonFile> parserDRMFile(String drmPath,
			String decodePath)
	{

		List<CommonFile> files = new ArrayList<CommonFile>();

		File file = new File(drmPath);

		if (!FileUtil.checkFilePathExists(drmPath))
		{
			Log.i(TAG, "文件不存在");
			return null;
		}
		FileOutputStream fos = null;
		FileInputStream fis = null;
		byte[] eightBytes = new byte[8];
		byte[] fileMd5Bytes = new byte[32];
		byte[] headFileNameBytes = new byte[40];
		int bufferLength = 1024 * 1024;
		try
		{
			// 头文件解析
			fis = new FileInputStream(file);

			fis.read(eightBytes);
			long len1 = Util.bytes2Long(eightBytes);
			int count = (int) (len1 / (headFileNameBytes.length
					+ eightBytes.length + eightBytes.length));

			long[] filestarts = new long[count];
			long[] fileends = new long[count];
			for (int i = 0; i < count; i++)
			{
				CommonFile f = new CommonFile();
				fis.read(headFileNameBytes);
				String filename = new String(headFileNameBytes).trim();
				f.filetype = getFileType(filename);
				f.filename = filename;
				f.filepath = decodePath + File.separator + filename;
				fis.read(eightBytes);
				filestarts[i] = Util.bytes2Long(eightBytes);
				fis.read(eightBytes);
				fileends[i] = Util.bytes2Long(eightBytes);
				files.add(f);

				SZLog.e(TAG, "filename: " + f.filename);
			}

			fis.read(fileMd5Bytes);

			// 资源文件解析
			for (int i = 0; i < count; i++)
			{
				File f = new File(decodePath, files.get(i).filename);
				if (!f.exists())
				{
					SZLog.i("文件绝对路径：" + f.getAbsolutePath());
					f.createNewFile();
				}
				fos = new FileOutputStream(f);
				int len = -1;
				byte[] buffer = null;
				if (fileends[i] > bufferLength)
				{
					int sum = (int) ((fileends[i] - fileMd5Bytes.length) / bufferLength);
					buffer = new byte[bufferLength];
					while ((len = fis.read(buffer)) != -1)
					{
						fos.write(buffer, 0, len);
						// fos.flush();
						sum--;
						if (sum == 0)
						{
							buffer = new byte[(int) ((fileends[i] - fileMd5Bytes.length) % bufferLength)];
							fis.read(buffer);
							fos.write(buffer);
							// fos.flush();
							fis.read(fileMd5Bytes);
							break;
						}
					}

				} else
				{
					buffer = new byte[(int) (fileends[i] - fileMd5Bytes.length)];
					fis.read(buffer);
					fos.write(buffer);
					// fos.flush();
					fis.read(fileMd5Bytes);
				}

				if (fos != null)
				{
					try
					{
						fos.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		} finally
		{

			if (fis != null)
			{
				try
				{
					fis.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (fos != null)
			{
				try
				{
					fos.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return files;
	}

	public static OEX_Rights parserRight(File name) throws Exception
	{
		SZLog.i("parserRight path: " + name.getAbsolutePath());
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(name);
			return PullXMLReader.readXML(fis);
		} catch (FileNotFoundException e)
		{
			throw new FileNotFoundException("file not found");
		} finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 解析albumInfo.xml实际内容为JSON格式的字符
	 * 
	 * @param albumInfoFile
	 *            albumInfo文件
	 * 
	 * @param list
	 *            CommonFile文件
	 * @return
	 * @throws Exception
	 */
	public static XML2JSON_Album parserJSON(File albumInfoFile,
			List<CommonFile> list) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		XML2JSON_Album albumInfo = new XML2JSON_Album();
		// 获得权限文件获得albumInfo.xml文件
		FileInputStream reader = null;
		try
		{
			SZLog.i("parserAlbumInfo path: " + albumInfoFile.getAbsolutePath());
			reader = new FileInputStream(albumInfoFile);
			byte[] bytes = new byte[1024];
			while ((reader.read(bytes)) != -1)
			{
				sb.append(new String(bytes));
			}

			// 将字符串转成JSON对象
			JSONObject rootObj = new JSONObject(sb.toString());
			JSONObject contentNames = rootObj.getJSONObject("contentNames");

			ArrayList<String> al = new ArrayList<String>();
			for (int x = 0; x < list.size(); x++)
			{
				if (x > 1)
				{
					String filename = list.get(x).filename.split("\\.")[0];
					String contentname = contentNames.getString(filename);
					al.add("\"" + filename + "\"");
					al.add("\"" + contentname + "\"");
				}
			}
			// 对contentNames只能用字符串方式解析，因为key值不是固定的
			albumInfo.setContentList(al);
			albumInfo.setInfoList(parserJSONToArrayList(rootObj
					.getString("infos")));
			albumInfo.setInfoObj(rootObj.getJSONObject("infos"));

			// 将字符串转成JSON对象
			// JSONObject rootObj = new JSONObject(sb.toString());
			// 对contentNames只能用字符串方式解析，因为key值不是固定的
			// albumInfo.setContentList(DRMUtil.parserJSONToArrayList(rootObj.getString("contentNames")));
			// albumInfo.setInfoList(DRMUtil.parserJSONToArrayList(rootObj.getString("infos")));
			// albumInfo.setInfoObj(rootObj.getJSONObject("infos"));

		} catch (FileNotFoundException e)
		{
			throw new FileNotFoundException("file not found");
		} catch (IOException e)
		{
			throw new IOException("");
		} catch (JSONException e)
		{
			throw new JSONException("json data is illegal");
		} finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return albumInfo;
	}

	/**
	 * 解析contentNames 将contentNames对应的字符串中的所有双引号之内的内容添加到一个list中
	 * 
	 * @param json
	 */
	public static ArrayList<String> parserJSONToArrayList(String json)
	{

		json.replaceAll("\\{", "");
		json.replaceAll("\\}", "");
		json.replaceAll("\"", "");

		ArrayList<String> contents = new ArrayList<String>();
		Pattern p = Pattern.compile("\"(.*?)\"");
		Matcher m = p.matcher(json);
		while (m.find())
		{
			contents.add(m.group().trim());
		}

		return contents;
	}

	/**
	 * 获取文件类型
	 * 
	 * @param fileName
	 * @return
	 */
	public static FILETYPE getFileType(String fileName)
	{
		String type = FileUtil.getExtFromFileName(fileName);
		if ("pdf".equalsIgnoreCase(type)) // equals
		{
			return FILETYPE.PDF;
		} else if ("xml".equalsIgnoreCase(type))
		{
			if (fileName.contains("albumInfo"))
			{
				return FILETYPE.ALBUMINFO;
			} else
			{
				return FILETYPE.RIGHT;
			}
		} else if ("mp3".equalsIgnoreCase(type))
		{
			return FILETYPE.MP3;
		} else if ("mp4".equalsIgnoreCase(type))
		{
			return FILETYPE.MP4;
		} else if ("drm".equalsIgnoreCase(type)) { return FILETYPE.DRM; }

		return FILETYPE.UNDEFINITION;
	}

	public static class CommonFile
	{
		public String filename;
		public String filepath;
		public FILETYPE filetype;
		public long filestart;
		// public long fileSize;
	}

	public enum FILETYPE
	{
		DRM, PDF, MP3, MP4, RIGHT, ALBUMINFO, UNDEFINITION;// *.drm文件，权限文件，各种资源文件，albumInfo文件
	}

}
