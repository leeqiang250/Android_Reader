package com.sz.mobilesdk.service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sz.mobilesdk.manager.DownloadTaskManager;
import com.sz.mobilesdk.models.FolderInfo;
import com.sz.mobilesdk.util.SZLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * sz文件下载任务服务service <br/>
 * 
 * <p>
 * 已废除！<br/>
 * <p>
 * 
 * manifest.xml文件中注册服务 com.sz.mobilesdk.service.DownloadService
 * 
 */
@Deprecated
public class DownloadService extends Service
{

	private String TAG = DownloadService.class.getSimpleName();
	private Context context;

	// private final int count = Runtime.getRuntime().availableProcessors() * 3;

	// 下载异常
	public static final String ACTION_ERROR = "com.sz.mobilesdk.Action_Error";
	// 连接异常
	public static final String ACTION_CONNECT_ERROR = "com.sz.mobilesdk.Action_Connect_Error";
	// 正在连接
	public static final String ACTION_CONNECTING = "com.sz.mobilesdk.Action_Connecting";
	// 更新ui
	public static final String ACTION_UPDATE = "com.sz.mobilesdk.Action_Update";
	// 停止某一个下载任务
	public static final String ACTION_STOP = "com.sz.mobilesdk.Action_Stop";
	// 停止所有下载(退出应用主页面时)
	public static final String ACTION_ALL_STOP = "com.sz.mobilesdk.Action_All_Stop";
	// 解析
	public static final String ACTION_PARSER = "com.sz.mobilesdk.Action_Parser";
	// 下载完毕
	public static final String ACTION_FINISHED = "com.sz.mobilesdk.Action_Finished";

	// 下载任务的集合
	private static Map<String, DownloadTaskManager> sTasks;

	@Override
	public void onCreate()
	{
		super.onCreate();
		context = this;
		if (sTasks == null)
		{
			sTasks = new LinkedHashMap<String, DownloadTaskManager>();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		SZLog.d(TAG, "onDestroy");
		DownloadTaskManager.closeExecutorService();
		if (sTasks != null)
		{
			sTasks.clear();
			sTasks = null;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null) { return super.onStartCommand(intent, flags,
				startId); }
		FolderInfo info = (FolderInfo) intent
				.getParcelableExtra("DownloadInfo");
		if (info != null)
		{
			startTask(info);
		}

		String action = intent.getAction();
		if (action == null)
			return super.onStartCommand(intent, flags, startId);
		if (ACTION_STOP.equalsIgnoreCase(action))
		{
			// 通过myProId，取出下载任务，暂停下载
			String myProId = intent.getStringExtra("myProId");
			DownloadTaskManager task = sTasks.get(myProId);
			if (task != null)
			{
				task.isPause = true;
			}
		} else if (ACTION_ALL_STOP.equalsIgnoreCase(action))
		{
			// 停止所有下载
			SZLog.i("stop task ,size = " + sTasks.size());
			Iterator<String> itor = sTasks.keySet().iterator();
			while (itor.hasNext())
			{
				DownloadTaskManager task = sTasks.get(itor.next());
				if (task != null && !task.isPause)
				{
					SZLog.d(TAG, "stop all task");
					task.isPause = true;
				}
			}
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 启动下载任务
	 * 
	 * @param info
	 */
	private void startTask(FolderInfo info)
	{
		SZLog.e(TAG, "1.satrt download : " + info.getProductName());
		DownloadTaskManager task = new DownloadTaskManager(context, info);
		task.downloadInfo();
		// 将下载任务加到map集合中
		sTasks.put(info.getMyProId(), task);
		// DRMLog.d(TAG, "开启第" + sTasks.size() + "个下载任务");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

}
