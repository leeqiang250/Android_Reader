package com.sz.mobilesdk.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import android.text.TextUtils;

/**
 * 处理字符串句类
 * 
 */
public class StringUtil
{

	/**
	 * 作者 eg: 周大侠;周大王;
	 * 
	 * @param authors
	 * @return
	 */
	public static String formatAuthors(String authors)
	{
		if (TextUtils.isEmpty(authors)) return "PBBONLINE";
		try
		{
			String[] strs = authors.split(";");
			if (strs.length < 3)
			{
				return authors.replace(";", "");
			}
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < strs.length; i++)
			{
				if (i != strs.length - 1)
				{
					sb.append(strs[i]);
					sb.append("，");
				} else
				{
					sb.append(strs[i]);
				}
			}
			return sb.toString();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return authors;
	}

	/**
	 * 根据固定地址result得到对应的值 <br/>
	 * 
	 * eg1:http://video.sz.net:8080/Cloud/client/content/getProductInfo?username
	 * =s001&id=4aa23 <br/>
	 * <br/>
	 * eg2:http://video.sz.net:8080/Cloud/client/content/getProductInfo?
	 * SystemType=Meeting&username=cpecc&id=54d4aa
	 * 
	 * @param result
	 * @param offsetStr
	 *            取出的字符串，通过此字符串取出对应的value。（忽略大小写）
	 * 
	 * @return String
	 */
	// public static String getStringByResult(String result, String offsetStr)
	// {
	// String valueString = "";
	// if (TextUtils.isEmpty(result)) return valueString;
	// try
	// {
	// int start = result.indexOf(offsetStr);
	// SZLog.d("start[" + offsetStr + "]: " + start);
	// String newResult = result.substring(start);
	// valueString = newResult.substring(offsetStr.length());
	// if (valueString.contains("&"))
	// {
	// String _valueString = valueString.split("&")[0];
	// valueString = _valueString;
	// }
	// } catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// return valueString;
	// }

	public static String getStringByResult(String result, String offsetStr)
	{
		String valueString = "";
		String regex = "(?i)" + offsetStr; // 忽略大小写
		try
		{
			String[] array = result.split(regex);
			valueString = array[1];
			if (valueString.contains("&"))
			{
				String _valueString = valueString.split("&")[0];
				valueString = _valueString;
			}
			SZLog.d(offsetStr, valueString);
		} catch (Exception e)
		{
			e.printStackTrace();
			SZLog.w(offsetStr, valueString);
		}
		return valueString;
	}

	/**
	 * 
	 * 根据给定的url地址（如下），获取对应的主机host和端口号port
	 * 
	 * eg1:http://video.suizhi.net:8657/Cloud/client/content/getProductInfo?
	 * SystemType=Meeting&username=weiqingfeng&id=48eb7526-dc0b
	 * 
	 * <br/>
	 * <br/>
	 * 
	 * eg2: http://video.suizhi.net:8657/Cloud/client/content/getProductInfo?
	 * SystemType=Meeting&username=weiqingfeng&id=3b73c89c-fd2c
	 * 
	 * <br/>
	 * <br/>
	 * 
	 * eg2: http://video.suizhi.net/Cloud/client/content/getProductInfo?
	 * SystemType=Meeting&username=weiqingfeng&id=3b73c89c-fd2c
	 * 
	 * @param resultUrl
	 * @return returnStrs[2] :<br/>
	 *         returnStrs[0] = host主机 <br/>
	 *         returnStrs[1] = port端口 <br/>
	 */
	public static String[] getHostAndPortByResult(String resultUrl)
	{
		String[] returnStrs = new String[2];
		if (TextUtils.isEmpty(resultUrl)) return returnStrs;
		String preOffset = "http://";
		String defaultPort = "80"; // 默认端口
		if (resultUrl.startsWith("https://"))
		{
			preOffset = "https://";
			defaultPort = "443";
		}
		try
		{
			SZLog.i("--------------------------------------");
			String newResult = resultUrl.substring(preOffset.length());
			if (newResult.contains(":"))
			{
				int middleOffset = newResult.indexOf(":");
				SZLog.d("middleOffset:=" + middleOffset);

				String hostStr = newResult.substring(0, middleOffset);
				returnStrs[0] = hostStr;

				int endOffset = newResult.indexOf("/");
				SZLog.d("endOffset/=" + endOffset);
				String portStr = newResult.substring(middleOffset + 1, endOffset);
				returnStrs[1] = portStr;
			} else
			{
				// 无端口号，默认80
				int _middleOffset = newResult.indexOf("/");
				SZLog.d("_middleOffset/=" + _middleOffset);

				String hostStr = newResult.substring(0, _middleOffset);
				returnStrs[0] = hostStr;
				String portStr = defaultPort;
				returnStrs[1] = portStr;
			}
			SZLog.d("host", returnStrs[0]);
			SZLog.d("port", returnStrs[1]);
			SZLog.i("--------------------------------------");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return returnStrs;
	}

	/**
	 * 截取字符串
	 * 
	 * @param search
	 *            待搜索的字符串
	 * @param start
	 *            起始字符串 例如：<title>
	 * @param end
	 *            结束字符串 例如：</title>
	 * @param defaultValue
	 * @return
	 */
	public static String substring(String search, String start, String end, String defaultValue)
	{
		int start_len = start.length();
		int start_pos = isEmpty(start) ? 0 : search.indexOf(start);
		if (start_pos > -1)
		{
			int end_pos = isEmpty(end) ? -1 : search.indexOf(end, start_pos + start_len);
			if (end_pos > -1)
				return search.substring(start_pos + start.length(), end_pos);
			else
				return search.substring(start_pos + start.length());
		}
		return defaultValue;
	}

	/**
	 * 截取字符串
	 * 
	 * @param search
	 *            待搜索的字符串
	 * @param start
	 *            起始字符串 例如：<title>
	 * @param end
	 *            结束字符串 例如：</title>
	 * @return
	 */
	public static String substring(String search, String start, String end)
	{
		return substring(search, start, end, "");
	}

	/**
	 * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty(String input)
	{
		if (input == null || "".equals(input)) return true;

		for (int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n')
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是否是空字符串，或者是 "null"字符串
	 * 
	 * @param mString
	 * @return
	 */
	public static boolean isEmptyOrNull(String mString)
	{
		return TextUtils.isEmpty(mString) || "null".equals(mString);
	}

	/**
	 * 验证手机格式
	 */
	public static boolean isMobileNO(String mobiles)
	{
		/*
		 * ss 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
		String telRegex = "[1][34578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (TextUtils.isEmpty(mobiles))
			return false;
		else
			return mobiles.matches(telRegex);
	}

	/**
	 * 还原11位手机号
	 * 
	 * @param num
	 * @return
	 */
	public static String formatMobileNO(String num)
	{
		if (num != null && !"".equals(num))
		{
			if (num.startsWith("+86"))
			{
				num = num.substring(3);
			} else if (num.startsWith("86"))
			{
				num = num.substring(2);
			}
		} else
		{
			num = "";
		}
		return num;
	}

	/**
	 * 判断给定的字符串是不是数字(可判断为负数：-123)
	 * 
	 * @param strNum
	 * @return
	 */
	public static boolean isNumber(String strNum)
	{
		boolean result = false;
		if (strNum == null || "".equalsIgnoreCase(strNum))
		{
			result = false;
		} else
		{
			Pattern pattern = Pattern.compile("^[\\-]?\\d*$");
			Matcher matcher = pattern.matcher(strNum);
			if (matcher.matches())
			{
				result = true;
			} else
			{
				result = false;
			}
		}
		return result;
	}

	/**
	 * 切割以逗号分隔的字符串，转化成集合返回
	 */
	public static ArrayList<String> getStringAndCommaByList(String mString)
	{
		ArrayList<String> re_list = new ArrayList<String>();
		if (mString != null && !"".equals(mString))
		{
			String[] shareid_array = mString.split("\\.");
			if (shareid_array.length > 0)
			{
				for (int x = 0; x < shareid_array.length; x++)
				{
					re_list.add(shareid_array[x]);
				}
			}
		}
		return re_list;
	}

	/**
	 * 由于推送过来的json不是正规的json,需要我们这边做处理
	 */
	@Deprecated
	public static String getJPushJsonByRegularJson(String JsonOld)
	{
		// 去掉反斜杠
		String Jsonnew = JsonOld.replace("\\", "");
		// 获取第一个:出现的位置
		int index = Jsonnew.indexOf(":");
		// 获取第二个:出现的位置
		index = Jsonnew.indexOf(":", index + 1);
		// 获取初始位置到第二个 ：的位置的字符串
		String result1 = Jsonnew.substring(0, index);
		// 根据第二个点的位置，截取 字符串。得到结果 result
		String result2 = Jsonnew.substring(index);
		// 删除第一个"
		int i = result2.indexOf("\"");
		String result3 = result2.substring(0, i) + result2.substring(i + 1);
		// 删除最后一个"
		int z = result3.lastIndexOf("\"");
		String result4 = result3.substring(0, z) + result3.substring(z + 1);
		String result5 = result1 + result4;
		return result5;
	}

	/**
	 * 获取格式化后的json
	 * 
	 * @param JsonOld
	 * @return
	 */
	public static String getJsonByJPushJsonString(String JsonOld)
	{
		// 去掉反斜杠
		String Jsonnew = JsonOld.replace("\\", "");

		// 去掉"{为 {
		String result1 = Jsonnew.replace("\"{", "{");

		// 去掉"}为 }
		String result2 = result1.replace("}\"", "}");

		SZLog.d("json", "" + result2);

		return result2;
	}

}
