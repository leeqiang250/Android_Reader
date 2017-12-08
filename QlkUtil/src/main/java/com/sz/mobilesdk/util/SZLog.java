package com.sz.mobilesdk.util;

import android.util.Log;

/**
 * 日志打印管理类
 * 
 * @author hudq
 * 
 */
public class SZLog
{

	private static final String TAG = "sz_log";
	private static final boolean showLog = com.qlk.util.BuildConfig.DEBUG;

	public static void i(String... args)
	{
		//if (SZInitInterface.isDebugMode)
		if (showLog)
		{
			for (int i = 0, length = args.length; i < length; i++)
			{
				Log.i(TAG, args[i]);
			}
		}
	}

	public static void i(String tag, String msg)
	{
		//if (SZInitInterface.isDebugMode)
		if (showLog)
		{
			Log.i(tag, msg);
		}
	}

	public static void v(String tag, String msg)
	{
		//if (SZInitInterface.isDebugMode)
		if (showLog)
		{
			Log.v(tag, msg);
		}
	}

	public static void d(String tag, String msg)
	{
		//if (SZInitInterface.isDebugMode)
		if (showLog)
		{
			Log.d(tag, msg);
		}
	}

	public static void d(String msg)
	{
		//if (SZInitInterface.isDebugMode)
		if (showLog)
		{
			Log.d(TAG, msg);
		}
	}

	public static void w(String tag, String msg)
	{
		//if (SZInitInterface.isDebugMode)
		if (showLog)
		{
			Log.w(tag, msg);
		}
	}

	public static void w(String msg)
	{
		//if (SZInitInterface.isDebugMode)
		if (showLog)
		{
			Log.w(TAG, msg);
		}
	}

	public static void e(String tag, String msg)
	{
		//if (SZInitInterface.isDebugMode)
		if (showLog)
		{
			Log.e(tag, msg);
		}
	}


	/*
	 * 打印日志时获取当前的程序文件名、行号、方法名 输出格式为：[FileName | LineNumber | MethodName]
	 */
//	public static String getFileLineMethod()
//	{
//		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
//		StringBuffer toStringBuffer = new StringBuffer("[").append(traceElement.getFileName()).append(" | ").append(traceElement.getLineNumber()).append(" | ")
//				.append(traceElement.getMethodName()).append("]");
//		return toStringBuffer.toString();
//	}
//
//	// 当前文件名
//	public static String _FILE_()
//	{
//		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
//		return traceElement.getFileName();
//	}
//
//	// 当前方法名
//	public static String _FUNC_()
//	{
//		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
//		return traceElement.getMethodName();
//	}
//
//	// 当前行号
//	public static int _LINE_()
//	{
//		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
//		return traceElement.getLineNumber();
//	}

}
