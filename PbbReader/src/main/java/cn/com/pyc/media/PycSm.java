package cn.com.pyc.media;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Files;

import java.util.ArrayList;
import java.util.HashMap;

import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.db.sm.SmDao;
import cn.com.pyc.xcoder.XCoder;

public class PycSm extends MediaFile
{
	public PycSm(Context context)
	{
		super(context);
	}

	public static final String[] TYPES = { ".pbb", ".pyc" };
	
	
	//QQ OpenSDK 会生成一个文件：cn.com.pyc
	@Override
	public boolean isSameType2(String path)
	{
		return isSameType1(path);
	}

	/**
	 * 是否是同一类型的文件
	 * 
	 * @param compare
	 */
	public static boolean isSameType1(String compare)
	{
		String[] types = TYPES;
		for (String type : types)
		{
			if (compare.length() >= type.length())
			{
				String suffix = compare.substring(compare.length() - type.length());
				if (type.equalsIgnoreCase(suffix))
				{
					String split = compare.substring(0, compare.length() - type.length());
					return PycImage.isSameType1(split) || PycPdf.isSameType1(split)
							|| PycMusic.isSameType1(split) || PycVideo.isSameType1(split);
				}
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> delete(String... delPaths)
	{
		SmDao smDao = SmDao.getInstance(mContext, !isFromSendFolder(delPaths[0]));
		HashMap<String, SmInfo> cacheInfos = new HashMap<>();
		for (String path : delPaths)
		{
			SmInfo info = XCoder.analysisSmFile(path).getSmInfo();
			cacheInfos.put(path, info);
		}
		ArrayList<String> del = super.delete(delPaths);
		for (String path : del)
		{
			smDao.delete(cacheInfos.get(path));
		}
		return del;
	}

	public static boolean isFromSendFolder(String path)
	{
		return path != null && path.contains("/.pyc/");
	}

	@Override
	protected Uri getMediaUri()
	{
		return Files.getContentUri("external");
	}

	@Override
	protected String[] getSupportTypes()
	{
		return TYPES;
	}

	@Override
	public boolean isFromUnExpectedDir(String path)
	{
		return false;
	}

}
