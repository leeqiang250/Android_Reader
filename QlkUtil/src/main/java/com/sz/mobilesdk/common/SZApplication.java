package com.sz.mobilesdk.common;

import android.app.Application;

/**
 * 应用需继承SZApplication，应用的onCreate()中添加super.onCreate(); <br/>
 * <br/>
 * 
 * SZApplication实际等同Application一样。
 * 
 * @author hudq
 * 
 */
public class SZApplication extends Application
{

	// Application对象是全局的，单例
	private static SZApplication instance;

	public static SZApplication getInstance()
	{
		return instance;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
	}

}
