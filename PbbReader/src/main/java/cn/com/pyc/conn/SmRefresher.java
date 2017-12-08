package cn.com.pyc.conn;

import android.text.TextUtils;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.utils.Constant;

/*-
 * 用于批量刷新
 */
public class SmRefresher
{
	private final SparseArray<String> fidToPath = new SparseArray<String>(); // fid对应该文件的路径path

	// 传给服务器的文件的fid，<=10个
	public ArrayList<String> getSendFids(HashMap<String, SmInfo> needRefresInfos)
	{
		Iterator<String> iterator = needRefresInfos.keySet().iterator();
		ArrayList<String> sendFids = new ArrayList<String>();
		String snd = "";
		int count = 0;
		while(iterator.hasNext())
		{
			String path = iterator.next();
			SmInfo info = needRefresInfos.get(path);
			if (info == null)
			{
				continue;
			}
			final int id = info.getFid();
			fidToPath.put(id, path);
			snd += id + ",";
			count++;
			// 服务器一次交互最多接收10个id
			if (count == 10)
			{
				snd = snd.substring(0, snd.lastIndexOf(",")); // 取出最后多出来的“,”
				sendFids.add(snd);
				snd = "";
				count = 0;
			}
		}
		if (!TextUtils.isEmpty(snd))
		{
			// 余数不足10个
			snd = snd.substring(0, snd.lastIndexOf(","));
			sendFids.add(snd);
		}
		return sendFids;
	}

	/**
	 * @param data
	 * @param needRefresInfos
	 *            即getSendIds时传入的mapInfos
	 * @return 更新成功的info，直接修改的needRefresInfos中的值
	 */
	public HashMap<String, SmInfo> analysisReceiveData(byte[] data,
			HashMap<String, SmInfo> needRefresInfos)
	{
		HashMap<String, SmInfo> sucInfos = new HashMap<String, SmInfo>();
		int end = 0;
		for (; end < data.length; end++)
		{
			// c++是以'\0'结束的，所以要找出结束点，否则结束点之后会出现乱码
			if (data[end] == '\0')
			{
				break;
			}
		}
		String[] rcvs = new String(data, 0, end).trim().split(";");
		for (String rcv : rcvs)
		{
			String[] strs = rcv.split(",");
			int id = getInt(strs[0]); // fid
			if (id <= 0)
			{
				// 不合法数据
				continue;
			}
			String path = fidToPath.get(id);
			SmInfo sm = needRefresInfos.get(path);
			sucInfos.put(path, sm);

			// 协议说明离线时只有次数、天数和年数有效，但我分析返回数据后制作类型等也有效，故更新之
			// sm.setOffline(getInt(strs[1])); // 是否离线
			sm.setPayFile(getInt(strs[2])); // 制作类型
			sm.setShowLimit(getInt(strs[3])); // 是否显示条件
			final int openCount = getInt(strs[4]);	// 限制次数
			final int openedCount = getInt(strs[5]);	// 已读次数
			if (sm.isPayFile())
			{
				// 不更新打开次数
				final boolean needApply = openedCount < 0
						|| (sm.isCountLimit() && openedCount == openCount);
				sm.setNeedApply(needApply ? Constant.C_TRUE : Constant.C_FALSE);
			}
			else
			{
				sm.setOpenedCount(openedCount);
			}
			sm.setOpenCount(openCount);
			sm.setStartTime(getStr(strs[6])); // 开始日期
			sm.setEndTime(getStr(strs[7])); // 结束日期
			sm.setSingleOpenTime(getInt(strs[8])); // 阅读时长
			sm.setMakerAllowed(getInt(strs[9])); // 禁止阅读
			sm.setMakeTime(getStr(strs[10])); // 制作时间
			sm.setBindNum(getInt(strs[11])); // 需要激活的数目
			sm.setActiveNum(getInt(strs[12])); // 已经激活的数据
			sm.setDays(getInt(strs[13])); // 总天数
			sm.setRemainDays(getInt(strs[14])); // 剩余天数
			sm.setYears(getInt(strs[15])); // 总年数
			sm.setRemainYears(getInt(strs[16])); // 剩余年数

			// 当初次查看时间为""时，split分割没有它。
			sm.setFirstOpenTime(strs.length > 17 ? getStr(strs[17]) : ""); // 初次查看时间
		}

		return sucInfos;
	}

	// 避免错误：Invalid int: ""
	private int getInt(String num)
	{
		if (TextUtils.isEmpty(num))
		{
			return 0;
		}
		else if(TextUtils.isDigitsOnly(num))
		{
			return Integer.valueOf(num);
		} else
		{
			return 0;
		}
	}

	private String getStr(String str)
	{
		return str.equals("0") ? "" : str;
	}
}
