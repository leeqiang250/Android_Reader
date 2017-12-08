package cn.com.pyc.media;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Files;

public class PycPdf extends MediaFile
{
	public PycPdf(Context context)
	{
		super(context);
	}

	public static final String[] TYPES =
	{ ".pdf" };

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
				String suffix = compare.substring(compare.length() - type.length()).toLowerCase();
				if (suffix.equals(type))
				{
					return true;
				}
			}
		}
		return false;
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

}
