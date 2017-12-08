package cn.com.pyc.media;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/*-
 * 这个类设计有缺陷：偶尔会出错
 */
@Deprecated
class SortPaths
{
	private int m;
	private long t;
	private String s;
	private String[] paths;
	private long[] times;

	public ArrayList<String> sort(HashMap<String, Long> mapPathTime)
	{
		ArrayList<String> pathTemp = new ArrayList<String>();
		int size = mapPathTime.size();
		if (size <= 0)
		{
			return pathTemp;
		}
		Iterator<String> keys = mapPathTime.keySet().iterator();
		paths = new String[size];
		times = new long[size];
		int i = 0;
		String key;
		Long time;
		while(keys.hasNext() && i < size)
		{
			key = keys.next();
			time = mapPathTime.get(key);
			// 排序时莫名其妙就空指针
			if (key != null && time != null)
			{
				paths[i] = key;
				times[i] = time.longValue();
				i++;
			}
		}
		size = i; // TODO 不知道为什么有时最后一个path为null但用TextUtils.isEmpty(key)检测不出来
		sort_4(0, size - 1);
		for (int j = 0; j < size; j++)
		{
			pathTemp.add(paths[j]);
		}

		return pathTemp;
	}

	// 递归---速度最快
	private void sort_4(int l, int u)
	{
		if (l >= u)
		{
			return;
		}
		m = l;
		for (int i = l + 1; i <= u; i++)
		{
			if (times[i] > times[l])
			{
				swi(++m, i);
			}
		}
		swi(l, m);
		sort_4(l, m - 1);
		sort_4(m + 1, u);
	}

	private void swi(int j, int j2)
	{
		t = times[j];
		times[j] = times[j2];
		times[j2] = t;

		s = paths[j];
		paths[j] = paths[j2];
		paths[j2] = s;
	}
}
