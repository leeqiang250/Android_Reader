package com.qlk.util.media.scanner;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Scanner
{
	private final ArrayList<String> filterDirs = new ArrayList<String>();
	private final ArrayList<String> filterFiles = new ArrayList<String>();

	/**
	 * 返回该目录下的所有文件夹名称及文件名称
	 * <p>
	 * 排序规则：<br>
	 * 隐藏文件夹-->文件夹-->隐藏文件-->文件
	 * 
	 * @param dir
	 * @return
	 */
	public ArrayList<String> scan(String dir)
	{
		filterDirs.clear();
		filterFiles.clear();

		new File(dir).listFiles(filter);
		MyComparator comparator = new MyComparator();
		Collections.sort(filterDirs, comparator);
		Collections.sort(filterFiles, comparator);

		ArrayList<String> names = new ArrayList<String>();
		names.addAll(filterDirs);
		names.addAll(filterFiles);

		return names;
	}

	FileFilter filter = new FileFilter()
	{

		@Override
		public boolean accept(File file)
		{
			if (file.isDirectory())
			{
				String path = file.getAbsolutePath();
				path = path.substring(path.lastIndexOf(File.separator) + 1);
				filterDirs.add(path);
			}
			else
			{
				filterFiles.add(file.getName());
			}
			return false;
		}
	};

	class MyComparator implements Comparator<String>
	{
		@Override
		public int compare(String lhs, String rhs)
		{
			if (lhs.startsWith(".") && !rhs.startsWith("."))
			{
				return -1;
			}
			else if (!lhs.startsWith(".") && rhs.startsWith("."))
			{
				return 1;
			}
			else
			{
				return String.CASE_INSENSITIVE_ORDER.compare(lhs, rhs);
			}
		}

	}
}
