package cn.com.pyc.pbbonline.util;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sz.mobilesdk.database.bean.Album;
import com.sz.mobilesdk.models.FolderInfo;

public class SortNameUtil
{

	/**
	 * FolderInfo list 根据名称排序(未下载的信息)
	 * 
	 * @param items
	 *            List<(FolderInfo)>
	 * @return
	 */
	public static List<FolderInfo> sortFolderInfo(List<FolderInfo> items)
	{
		Collections.sort(items, new Comparator<FolderInfo>()
		{

			@Override
			public int compare(FolderInfo o1, FolderInfo o2)
			{
				Collator c1 = Collator.getInstance(java.util.Locale.CHINA);
				Collator c2 = Collator.getInstance(java.util.Locale.ENGLISH);
				if (isChinese(o1.getProductName()) && isChinese(o2.getProductName()))
				{
					//名字中文开头
					return c1.compare(o1.getProductName(), o2.getProductName());
				}
				else
				{
					return c2.compare(o1.getProductName(), o2.getProductName());
				}
			}
		});
		return items;
	}

	/**
	 * Album list 根据名称排序(已下载的信息)
	 * 
	 * @param items
	 * @return
	 */
	public static List<Album> sortAlbumItems(List<Album> items)
	{
		Collections.sort(items, new Comparator<Album>()
		{

			@Override
			public int compare(Album o1, Album o2)
			{
				Collator c1 = Collator.getInstance(java.util.Locale.CHINA);
				Collator c2 = Collator.getInstance(java.util.Locale.ENGLISH);
				if (isChinese(o1.getName()) && isChinese(o2.getName()))
				{
					//名字中文开头
					return c1.compare(o1.getName(), o2.getName());
				}
				else
				{
					return c2.compare(o1.getName(), o2.getName());
				}
			}
		});
		return items;
	}

	/**
	 * 是否是中文字
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isChinese(String name)
	{
		if (name == null)
			//throw new IllegalArgumentException("params 'name' is null");
			return false;

		if (name.length() > 1)
		{
			//取第一个字
			name = name.substring(0, 1);
		}

		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher matcher = pattern.matcher(name);
		return matcher.matches();
	}

}
