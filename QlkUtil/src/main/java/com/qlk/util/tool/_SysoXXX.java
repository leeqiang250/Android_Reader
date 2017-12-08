package com.qlk.util.tool;

import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Some methods used for debugging.
 * 
 * @author QiLiKing 2015-8-3 下午2:46:22
 */
public class _SysoXXX
{
	/**
	 * Print all the messages in the intent.
	 * 
	 * @param intent
	 * @param msg
	 */
	public static void intent(Intent intent, Object msg)
	{
		if (intent != null)
		{
			message(msg);
			message("action:" + intent.getAction());
			Uri uri = intent.getData();
			if (uri != null)
			{
				message("data:" + uri.toString());
			}
			uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
			if (uri != null)
			{
				message("EXTRA_STREAM:" + uri.toString());
			}
			Bundle bundle = intent.getExtras();
			bundle(bundle, "");
		}
	}

	/**
	 * Print all the messages in the bundle.
	 * 
	 * @param intent
	 * @param msg
	 */
	public static void bundle(Bundle bundle, Object msg)
	{
		if (bundle != null)
		{
			Set<String> keys = bundle.keySet();
			for (String key : keys)
			{
				_SysoXXX.message("msg:::::" + key + "---" + bundle.get(key));
			}
		}
	}

	/**
	 * Print every element of the bytes in each line.
	 * 
	 * @param intent
	 * @param msg
	 */
	public static void array(byte[] b, String msg)
	{
		message(msg);
		final int size = b.length;
		for (int i = 0; i < size; i++)
		{
			message(i + "---" + Byte.toString(b[i]));
		}
	}

	/**
	 * Print every element of the bytes in each line.
	 * 
	 * @param intent
	 * @param msg
	 */
	public static void array(long[] b, String msg)
	{
		message(msg);
		final int size = b.length;
		for (int i = 0; i < size; i++)
		{
			message(i + "---" + b[i]);
		}
	}

	/**
	 * Print the parameter.
	 * 
	 * @param msg
	 */
	public static void message(Object msg)
	{
		message(null, null, msg);
	}

	/**
	 * @param tag
	 *            The calling method's name.
	 * @param msg
	 *            The message to print.
	 */
	public static void message(String tag, Object msg)
	{
		message(null, tag, msg);
	}

	/**
	 * @param clazz
	 *            The calling class's name.
	 * @param tag
	 *            The calling method's name.
	 * @param msg
	 *            The message to print.
	 */
	public static void message(Class<? extends Object> clazz, String tag, Object msg)
	{
		String className = clazz != null ? clazz.getName() + " :::::: " : "";
		String tagName = "";
		if (tag != null)
		{
			tagName = tag + " ------ ";
		}

		System.out.println(className + tagName + msg);
	}

	/**
	 * @param clazz
	 *            The calling class's name.
	 * @param msg
	 *            The message to print.
	 */
	public static void message(Class<? extends Object> clazz, Object msg)
	{
		String className = clazz != null ? clazz.getName() + " :::::: " : "";
		System.out.println(className + msg);
	}
}
