package cn.com.pyc.media;

import java.util.HashMap;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Images;

public class PycImage extends MediaFile
{
	public PycImage(Context context)
	{
		super(context);
	}

	public static final String[] TYPES =
	{ ".jpg", ".jpeg", ".png" };

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
	public void xCode(Context context, HashMap<String, String> mapPaths)
	{
		super.xCode(context, mapPaths);
		ImageSort.sort(getQlkPaths());
	}

	@Override
	protected void onSearchFinished(boolean autoToast)
	{
		ImageSort.sort(getQlkPaths());
		super.onSearchFinished(autoToast);
	}

	@Override
	protected Uri getMediaUri()
	{
		return Images.Media.EXTERNAL_CONTENT_URI;
	}

	@Override
	protected String[] getSupportTypes()
	{
		return TYPES;
	}

}
