package cn.com.pyc.media;

import java.util.ArrayList;
import java.util.HashMap;

import com.qlk.util.tool.Util.FileUtil;

public class ImageSort
{
	private static final HashMap<String, ArrayList<String>> sorts = new HashMap<String, ArrayList<String>>();
	private static final ArrayList<String> firsts = new ArrayList<String>();

	public static void sort(final ArrayList<String> paths)
	{
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (String path : paths)
		{
			String fold = FileUtil.getFolder(path);
			ArrayList<String> temp = map.get(fold);
			if (temp == null)
			{
				temp = new ArrayList<String>();
				map.put(fold, temp);
			}
			temp.add(0, path);
		}
		ArrayList<String> folders = new ArrayList<String>(map.keySet());
		if (folders.contains("Camera"))
		{
			folders.remove("Camera");
			folders.add(0, "Camera");	// 需求要把Camera放在第一个
		}
		ArrayList<String> tmp = new ArrayList<String>();
		for (String folder : folders)
		{
			tmp.add(map.get(folder).get(0));
		}
		firsts.clear();
		firsts.addAll(tmp);
		sorts.clear();
		sorts.putAll(map);
	}

	public static ArrayList<String> getFirsts()
	{
		return firsts;
	}

	public static HashMap<String, ArrayList<String>> getSort()
	{
		return sorts;
	}
}
