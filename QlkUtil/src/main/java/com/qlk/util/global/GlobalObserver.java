package com.qlk.util.global;

import java.util.Observable;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 相对于普通的Observable：额外增加了postNotifyObservers方法，可异步notify；notify之前不用再手动setChanged
 * 
 * @author QiLiKing 2015-6-30 下午5:18:10
 */
public class GlobalObserver extends Observable
{
	private static GlobalObserver gob;

	public static GlobalObserver getGOb()
	{
		if (gob == null)
		{
			gob = new GlobalObserver();
		}
		return gob;
	}

	private GlobalObserver()
	{
	}

	/**
	 * 通过hander通知UI线程
	 * 
	 * @param tag
	 */
	public void postNotifyObservers(Object tag)
	{
		Message msg = Message.obtain();
		msg.obj = tag;
		handler.sendMessage(msg);
	}

	@Override
	public void notifyObservers(Object tag)
	{
		setChanged();
		super.notifyObservers(tag);
	}

	private final Handler handler = new Handler(Looper.getMainLooper())
	{

		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			notifyObservers(msg.obj);
		}

	};

}
