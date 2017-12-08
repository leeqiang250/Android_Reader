package com.qlk.util.tool;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.text.format.Formatter;

public class DataConvert {
	public static byte[] longToByte(long number) {
		long temp = number;
		byte[] b = new byte[8];
		for (int i = 0; i < b.length; i++) {
			b[i] = Long.valueOf(temp & 0xff).byteValue();
			temp = temp >> 8;
		}
		return b;
	}

	public static long byteToLong(byte[] b) {
		if (b == null || b.length != 8) {
			return 0;
		}
		long s = 0;
		long s0 = b[0] & 0xff;
		long s1 = b[1] & 0xff;
		long s2 = b[2] & 0xff;
		long s3 = b[3] & 0xff;
		long s4 = b[4] & 0xff;
		long s5 = b[5] & 0xff;
		long s6 = b[6] & 0xff;
		long s7 = b[7] & 0xff;

		s1 <<= 8;
		s2 <<= 16;
		s3 <<= 24;
		s4 <<= 8 * 4;
		s5 <<= 8 * 5;
		s6 <<= 8 * 6;
		s7 <<= 8 * 7;
		s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
		return s;
	}

	public static byte[] intToBytes(int src) {
		byte[] des = new byte[4];
		des[0] = (byte) (src >> 8 * 0 & 0xff);
		des[1] = (byte) (src >> 8 * 1 & 0xff);
		des[2] = (byte) (src >> 8 * 2 & 0xff);
		des[3] = (byte) (src >> 8 * 3 & 0xff);
		return des;
	}

	public static int bytesToInt(byte[] b) {
		if (b == null || b.length != 4) {
			return 0;
		}

		int s = 0;
		int s0 = b[0] & 0xff;
		int s1 = b[1] & 0xff;
		int s2 = b[2] & 0xff;
		int s3 = b[3] & 0xff;
		s3 <<= 24;
		s2 <<= 16;
		s1 <<= 8;
		s = s0 | s1 | s2 | s3;
		return s;
	}

	public static short bytesToShort(byte[] b) {
		return (short) ((b[0] & 0xff) | ((b[1] & 0xff) << 8));
	}

	/**
	 * @param seconds
	 *            以秒为单位
	 * @return 几分几秒之类的
	 */
	public static String toTime(long seconds) {
		if (seconds <= 0) {
			return "0秒";
		}

		int min = (int) (seconds / 60);
		int sec = (int) (seconds % 60);
		String str = "";
		if (min > 0) {
			str += min + "分";
		}
		if (sec > 0) {
			str += sec + "秒";
		} else {
			str += "钟";
		}
		return str;
	}

	/**
	 * @param millSeconds
	 *            毫秒
	 * @return 类似2015-03-23 13:23:32
	 */
	public static String toDate(long millSeconds) {
		millSeconds = millSeconds == 0 ? System.currentTimeMillis() : millSeconds;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.US);
		return format.format(millSeconds);
	}

	/**
	 * @param context
	 * @param size
	 *            字节数
	 * @return 1.95 T 之类的
	 */
	public static String toSize(Context context, long size) {
		return size < 0 ? "0.00 B" : Formatter.formatFileSize(context, size);
	}

	/**
	 * @param date
	 *            接受类型：yyyy-MM-dd HH:mm:ss或者yyyy/MM/dd HH:mm:ss
	 * @return 毫秒
	 */
	public static long toLongTime(String date) {
		String template = "yyyy-MM-dd HH:mm:ss";
		if (date.contains("/")) {
			template = "yyyy/MM/dd HH:mm:ss";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(template, Locale.US);
		long time = 0;
		try {
			time = sdf.parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			time = 0;
		}
		return time;
	}

	public static long transformTime(String startTime, String endTime) {
		return transformTime(startTime, endTime, null, null);
	}

	/**
	 * @param startTime
	 * @param endTime
	 * @param format
	 *            默认yyyy-MM-dd
	 * @param to
	 *            接受值:"d","h","m","s"，分别是天、时、分、秒
	 * @return
	 */
	public static long transformTime(String startTime, String endTime, String format, String to) {
		if (startTime == null || endTime == null || startTime.equals("") || endTime.equals("")) {
			return 0;
		}

		if (format == null) {
			format = "yyyy-MM-dd";
		}
		if (to == null) {
			to = "d";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		long result = 0;
		final long s = 1000;
		final long m = 60 * s;
		final long h = 60 * m;
		final long d = 24 * h;
		try {
			long diff = sdf.parse(endTime).getTime() - sdf.parse(startTime).getTime();
			long day = diff / d;
			long hour = diff / h;
			long min = diff / m;
			long sec = diff / s;
			result = to.equals("d") ? day : to.equals("h") ? hour : to.equals("m") ? min : sec;
			if (to.equals("d")) {
				result++;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			result = 0;
		}
		return result;
	}

	/**
	 * @param num
	 * @return
	 */
	public static int toLittleNum(long num) {
		return (int) (num / 1000);
	}

	/**
	 * 排除null
	 * 
	 * @param check
	 * @return ""或者本身
	 */
	public static String getSafeStr(String check) {
		if (check == null) {
			check = "";
		}
		return check.trim();
	}

	/**
	 * 排除null
	 * 
	 * @param check
	 * @return new byte[1]或者本身
	 */
	public static byte[] getSafeBytes(byte[] check) {
		if (check == null) {
			check = new byte[1];
		}
		return check;
	}
	
	/**
	 * 排除null
	 */
	public static String getSafeStr(byte[] src) {
		if (src == null) {
			return "";
		}
		try {
			int size = 0;
			for (; size < src.length; size++) {
				if (src[size] == '\0') {
					break;
				}
			}
			return new String(src, 0, size, "UTF-8").trim();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String(src).trim();
	}


	public static byte[] getBytes(byte[] buf, int index, int length) {
		byte[] bufTemp = new byte[length];
		System.arraycopy(buf, index, bufTemp, 0, length);
		return bufTemp;
	}

	/**
	 * @param src
	 * @return 大小>=4
	 */
	public static byte[] toHexByteArray(long src) {
		String str = Long.toHexString(src);
		int size = 8 - str.length();
		for (int k = 0; k < size; k++) {
			str = 0 + str;
		}
		byte[] byteArray = new byte[str.length() / 2];
		int len = byteArray.length;
		int j = 0;
		for (int i = 0; i < len; i++) {
			j = (i << 1);
			byteArray[i] = 0;
			char c = str.charAt(j);
			if ('0' <= c && c <= '9') {
				byteArray[i] |= ((c - '0') << 4);
			} else if ('A' <= c && c <= 'F') {
				byteArray[i] |= ((c - 'A' + 10) << 4);
			} else if ('a' <= c && c <= 'f') {
				byteArray[i] |= ((c - 'a' + 10) << 4);
			} else {
				// TODO: Exception
			}
			j++;
			c = str.charAt(j);
			if ('0' <= c && c <= '9') {
				byteArray[i] |= (c - '0');
			} else if ('A' <= c && c <= 'F') {
				byteArray[i] |= (c - 'A' + 10);
			} else if ('a' <= c && c <= 'f') {
				byteArray[i] |= (c - 'a' + 10);
			} else {

			}
		}
		return byteArray;
	}

}
