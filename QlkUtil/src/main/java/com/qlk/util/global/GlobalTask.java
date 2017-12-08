package com.qlk.util.global;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 常驻单线程
 * 
 * @author QiLiKing 2015-6-30 下午5:21:46
 */
public class GlobalTask
{
	private static final ExecutorService CACHE_THREAD = Executors.newCachedThreadPool();
	private static ExecutorService SIGNLE_THREAD = Executors.newSingleThreadExecutor();

	/**
	 * Run on a single thread.
	 * 
	 * @param task
	 */
	public static void executeOrderTask(Runnable task)
	{
		SIGNLE_THREAD.execute(task);
	}

	/**
	 * 后台执行
	 */
	public static void executeBackground(Runnable task)
	{
		CACHE_THREAD.execute(task);
	}

	/**
	 * 显示进度框：GlobalDialog.showNetProgress()
	 */
	public static void executeNetTask(Context context, final Runnable netTask)
	{
		executeDialog(netTask, GlobalDialog.showNetProgress((Activity) context));
	}
	public static void executeNetTask(final Runnable netTask)
	{
		executeDialog(netTask,null);
	}

	/**
	 * 显示进度框：GlobalDialog.showProgressBar()
	 */
	public static void executeNormalTask(Context context, final Runnable netTask)
	{
		executeDialog(netTask, GlobalDialog.showBgLoading((Activity) context));
	}

	/**
	 * 显示指定的对话框
	 */
	public static void executeDialog(final Runnable netTask, final Dialog dialog)
	{
		executeBackground(new Runnable()
		{
			@Override
			public void run()
			{
				if (netTask != null)
				{
					netTask.run();
				}
				if (dialog != null)
				{
					GlobalDialog.hideBgLoading();
					dialog.dismiss();
				}
			}
		});
	}

}
