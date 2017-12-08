package com.sz.mobilesdk.util;

import android.graphics.Color;
import android.text.TextUtils;

/**
 * 数据类型转换工具类
 * 
 * @author hudq
 * 
 */
public class ConvertToUtil
{

	private static final String EMPTY_STRING = "";

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String toString(String str)
	{
		if (TextUtils.isEmpty(str))
		{
			return EMPTY_STRING;
		} else
		{
			return str;
		}
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	public static String toString(Object o)
	{
		if ((o == null || o.toString().length() == 0))
		{
			return EMPTY_STRING;
		} else
		{
			return o.toString();
		}
	}

	/**
	 * 转换字符串为int
	 * 
	 * @param str
	 * @return
	 */
	public static int toInt(String str)
	{
		return toInt(str, 0);
	}

	/**
	 * 转换字符串为int
	 * 
	 * @param str
	 * @param def
	 *            默认值
	 * @return
	 */
	public static int toInt(String str, int def)
	{
		if (TextUtils.isEmpty(str))
		{
			return def;
		}
		try
		{
			return Integer.parseInt(str);
		} catch (NumberFormatException e)
		{
			return def;
		}
	}

	/**
	 * 转换字符串为boolean
	 * 
	 * @param str
	 * @return
	 */
	public static boolean toBoolean(String str)
	{
		return toBoolean(str, false);
	}

	/**
	 * 转换字符串为boolean
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static boolean toBoolean(String str, boolean def)
	{
		if (TextUtils.isEmpty(str))
		{
			return def;
		}
		if ("false".equalsIgnoreCase(str) || "0".equals(str))
		{
			return false;
		} else if ("true".equalsIgnoreCase(str) || "1".equals(str))
		{
			return true;
		} else
		{
			return def;
		}
	}

	/**
	 * 转换字符串为float
	 * 
	 * @param str
	 * @return
	 */
	public static float toFloat(String str)
	{
		return toFloat(str, 0F);
	}

	/**
	 * 转换字符串为float
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static float toFloat(String str, float def)
	{
		if (TextUtils.isEmpty(str))
		{
			return def;
		}
		try
		{
			return Float.parseFloat(str);
		} catch (NumberFormatException e)
		{
			return def;
		}
	}

	/**
	 * 转换字符串为double
	 * 
	 * @param str
	 * @return
	 */
	public static double toDouble(String str)
	{
		return toDouble(str, 0);
	}

	/**
	 * 转换字符串为double
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static double toDouble(String str, double def)
	{
		if (TextUtils.isEmpty(str))
		{
			return def;
		}
		try
		{
			return Double.parseDouble(str);
		} catch (NumberFormatException e)
		{
			return def;
		}
	}

	/**
	 * 转换字符串为long
	 * 
	 * @param str
	 * @return
	 */
	public static long toLong(String str)
	{
		return toLong(str, 0L);
	}

	/**
	 * 转换字符串为long
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static long toLong(String str, long def)
	{
		if (TextUtils.isEmpty(str))
		{
			return def;
		}
		try
		{
			return Long.parseLong(str);
		} catch (NumberFormatException e)
		{
			return def;
		}
	}

	/**
	 * 转换字符串为short
	 * 
	 * @param str
	 * @return
	 */
	public static short toShort(String str)
	{
		return toShort(str, (short) 0);
	}

	/**
	 * 转换字符串为short
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static short toShort(String str, short def)
	{
		if (TextUtils.isEmpty(str))
		{
			return def;
		}
		try
		{
			return Short.parseShort(str);
		} catch (NumberFormatException e)
		{
			return def;
		}
	}

	/** 颜色转化 */
	public static int toColor(String str, int def)
	{
		if (TextUtils.isEmpty(str))
		{
			return def;
		}
		try
		{
			return Color.parseColor(str);
		} catch (Exception e)
		{
			return def;
		}
	}

}
