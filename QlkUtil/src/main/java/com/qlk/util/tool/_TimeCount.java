package com.qlk.util.tool;

import java.util.HashMap;

/**
 * Calculate time between "start" and "end".
 * 
 * @author QiLiKing 2015-8-3 下午2:55:45
 */
public class _TimeCount
{
	private static HashMap<String, Long> MapTime = new HashMap<String, Long>();

	/**
	 * Start a calculation task.
	 * 
	 * @param tag
	 */
	public static void start(String tag)
	{
		MapTime.put(tag, System.currentTimeMillis());
	}

	/**
	 * Finish the calculation.
	 * 
	 * @param tag
	 */
	public static void end(String tag)
	{
		_SysoXXX.message(tag + "Use Time---" + (System.currentTimeMillis() - MapTime.get(tag))
				+ "ms");
	}
}
