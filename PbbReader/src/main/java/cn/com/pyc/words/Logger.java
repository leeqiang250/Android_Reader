package cn.com.pyc.words;

import android.util.Log;

public class Logger {
	
	public static String tag = "Logger";
	public static boolean isTag = true;
	
	public static void i(String msg) {
		if (isTag) {
			Log.i(tag, msg);
		}
	}
	
	public static void i(String tag, String msg) {
		if (isTag) {
			Log.i(tag, msg);
		}
	}

}
