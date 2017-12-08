package cn.com.pyc.media;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Audio;

public class PycMusic extends MediaFile
{
	public PycMusic(Context context)
	{
		super(context);
	}

	public static final String[] TYPES =
	{ ".mp3", ".wav", ".ape" };

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
		return Audio.Media.EXTERNAL_CONTENT_URI;
	}

	@Override
	protected String[] getSupportTypes()
	{
		return TYPES;
	}

}
