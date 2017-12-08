package cn.com.pyc.pbbonline.util;

import android.content.Context;

import cn.com.pyc.pbb.reader.R;

import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.SZLog;

/**
 * 格式化权限显示工具类
 * 
 * @author hudq
 */
public class ValidDateUtil
{

	private static final String TAG = "ValidDate";

	/**
	 * 获取权限时间
	 * <p>
	 * eg：<br/>
	 * -1 永久有效<br/>
	 * 0 已经过期<br/>
	 * 2016-12-12 23:59:59 到期
	 * 
	 * @param context
	 * @param availableTime
	 * @param endOddTime
	 * @return
	 */
	public static String getValidTime(Context context, long availableTime, long endOddTime)
	{
		SZLog.i("--------------------------------");
		SZLog.d(TAG, "availableTime: " + availableTime);
		String available = FormatterUtil.getLeftAvailableTime(availableTime);
		String deadTime = "-1";
		if ("00天00小时".equals(available))
		{
			// 已经过期。	此处使用的string是SZMobileSDK中string.xml定义的统一格式！
			deadTime = context.getString(R.string.over_time);
		}
		else if ("-1".equals(available))
		{
			// 永久
			deadTime = context.getString(R.string.forever_time);
		}
		else
		{
			String odd_datetime_end = FormatterUtil.getToOddEndTime(endOddTime);
			deadTime = context.getString(R.string.deadline_time, odd_datetime_end);
		}
		SZLog.d(TAG, "time: " + deadTime);
		SZLog.i("--------------------------------");
		return deadTime;
	}

	/**
	 * 获取权限时间
	 * <p>
	 * eg：<br/>
	 * -1 永久有效<br/>
	 * 0 已经过期<br/>
	 * 2016-12-12 23:59:59 到期
	 * 
	 * @param context
	 * @param format_available_time
	 * @param format_odd_endTime
	 * @return
	 */
	public static String getValidTime(Context context, String format_available_time,
			String format_odd_endTime)
	{
		SZLog.i("--------------------------------");
		SZLog.w(TAG, "available_time: " + format_available_time);
		String deadTime = "-1";
		if ("00天00小时".equals(format_available_time))
		{
			// 已经过期
			deadTime = context.getString(R.string.over_time);
		}
		else if ("-1".equals(format_available_time))
		{
			// 永久
			deadTime = context.getString(R.string.forever_time);
		}
		else
		{
			deadTime = context.getString(R.string.deadline_time, format_odd_endTime);
		}
		SZLog.w(TAG, "time: " + deadTime);
		SZLog.i("--------------------------------");
		return deadTime;
	}
}
