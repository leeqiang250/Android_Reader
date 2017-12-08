package com.sz.mobilesdk.service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sz.mobilesdk.manager.DownloadTaskManager2;
import com.sz.mobilesdk.models.FileData;
import com.sz.mobilesdk.util.SZLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService2 extends Service
{

	private static final String TAG = "DownloadService2";
	private Context context;

	// private final int count = Runtime.getRuntime().availableProcessors() * 3;

	// 下载异常
	public static final String ACTION_DOWNLOAD_ERROR = "cn.com.pyc.pbb.Action_Error_2";
	// 连接异常
	public static final String ACTION_CONNECT_ERROR = "cn.com.pyc.pbb.Action_Connect_Error_2";
	// 正在连接
	public static final String ACTION_CONNECTING = "cn.com.pyc.pbb.Action_Connecting_2";
	// 更新ui
	public static final String ACTION_UPDATE = "cn.com.pyc.pbb.Action_Update_2";
	// 停止某一个下载任务
	public static final String ACTION_STOP = "cn.com.pyc.pbb.Action_Stop_2";
	// 停止所有下载(退出应用主页面时)
	public static final String ACTION_ALL_STOP = "cn.com.pyc.pbb.Action_All_Stop_2";
	// 解析中...
	public static final String ACTION_PARSERING = "cn.com.pyc.pbb.Action_Parsering_2";
	// 下载完成了
	public static final String ACTION_FINISHED = "cn.com.pyc.pbb.Action_Finished_2";

	/** 下载任务的集合(key=文件id，value=任务) */
	private static Map<String, DownloadTaskManager2> sTasks;

	@Override
	public void onCreate()
	{
		super.onCreate();
		context = this;
		if (sTasks == null)
		{
			sTasks = new LinkedHashMap<String, DownloadTaskManager2>();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		SZLog.v(TAG, "onDestroy");
		if (sTasks != null)
		{
			sTasks.clear();
			sTasks = null;
		}
		DownloadTaskManager2.shutdownTask();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null)
			return super.onStartCommand(intent, flags, startId);
		FileData data = (FileData) intent.getParcelableExtra("FileData");
		if (data != null)
		{
			startTask(data);
		}
		// action
		actionService(intent);

		return super.onStartCommand(intent, flags, startId);
	}

	private void actionService(Intent intent)
	{
		String action = intent.getAction();
		if (ACTION_STOP.equalsIgnoreCase(action))
		{
			if (!intent.hasExtra("taskId"))
				throw new IllegalArgumentException(
						"hasExtra,'taskId' must be required.");

			// 通过taskId，取出下载任务，暂停下载
			String id = intent.getStringExtra("taskId");
			DownloadTaskManager2 task = sTasks.get(id);
			if (task != null)
			{
				task.isPause = true;
			}
		} else if (ACTION_ALL_STOP.equalsIgnoreCase(action))
		{
			// 停止所有下载
			SZLog.i("taskCount = " + sTasks.size());
			Iterator<String> itor = sTasks.keySet().iterator();
			while (itor.hasNext())
			{
				DownloadTaskManager2 task = sTasks.get(itor.next());
				if (task != null)
				{
					SZLog.d(TAG, "stop all download task");
					task.isPause = true;
					task.isBreak = true;
				}
			}
			stopSelf();
		}
	}

	/**
	 * 启动下载任务
	 * 
	 * @param info
	 */
	private void startTask(FileData o2)
	{
		SZLog.i("1.satrt download : " + o2.getName());
		DownloadTaskManager2 task = new DownloadTaskManager2(context, o2);
		sTasks.put(o2.getFiles_id(), task);

		task.download();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	// /**
	// * 开启下载
	// *
	// * @param ctx
	// * @param o2
	// */
	// public static void startDownloadFile(Context ctx, FileData o2)
	// {
	// Intent intent = new Intent(ctx, DownloadService2.class);
	// intent.putExtra("FileData", o2);
	// ctx.startService(intent);
	// }
	//
	// /**
	// * 停止其中一个下载任务
	// *
	// * @param ctx
	// * @param fileId
	// */
	// public static void stopDwonloadFile(Context ctx, String fileId)
	// {
	// Intent intent = new Intent(ctx, DownloadService2.class);
	// intent.setAction(DownloadService2.ACTION_STOP);
	// intent.putExtra("taskId", fileId);
	// ctx.startService(intent);
	// }
	//
	// /**
	// * 停止所有下载任务
	// *
	// * @param ctx
	// */
	// public static void stopAllDwonloadFile(Context ctx)
	// {
	// Intent intent = new Intent(ctx, DownloadService2.class);
	// intent.setAction(DownloadService2.ACTION_ALL_STOP);
	// ctx.startService(intent);
	// }

}
