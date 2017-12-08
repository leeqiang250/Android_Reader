package com.sz.mobilesdk.receiver;

import com.sz.mobilesdk.manager.DownloadTaskManager2;
import com.sz.mobilesdk.manager.db.DownData2DBManager;
import com.sz.mobilesdk.models.FileData;
import com.sz.mobilesdk.service.DownloadService2;
import com.sz.mobilesdk.util.SZLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * //注册下载的广播 <br/>
 * IntentFilter downloadFilter = new IntentFilter(); <br/>
 * downloadFilter.addAction(DownloadService2.ACTION_UPDATE); <br/>
 * downloadFilter.addAction(DownloadService2.ACTION_CONNECTING); <br/>
 * registerReceiver(downloadReceiver, downloadFilter); <br/>
 * 
 */
public abstract class DownloadReceiver2 extends BroadcastReceiver
{

	public DownloadReceiver2()
	{
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent == null) return;
		if (!intent.hasExtra("FileData"))
			throw new IllegalArgumentException(
					"hasExtra, 'FileData' must be required.");

		FileData o = intent.getParcelableExtra("FileData");
		if (null == intent.getAction()) return;
		SZLog.d("dr2", "position = " + o.getPosition());

		switch (intent.getAction())
		{
			case DownloadService2.ACTION_UPDATE: // 更新进度
			{
				long currentSize = intent.getLongExtra("currentSize", 0);
				int progress = intent.getIntExtra("progress", 0);
				boolean isLast = intent
						.getBooleanExtra("isLastProgress", false);
				int state = isLast ? DownloadTaskManager2.PAUSE
						: DownloadTaskManager2.DOWNLOADING;
				o.setTaskState(state);
				o.setProgress(progress);
				updateProgress(o, progress, currentSize);
			}
				break;
			case DownloadService2.ACTION_DOWNLOAD_ERROR: // 下载出错
			{
				o.setTaskState(DownloadTaskManager2.DOWNLOAD_ERROR);
				pathError(o);
			}
				break;
			case DownloadService2.ACTION_CONNECT_ERROR: // 连接出错
			{
				o.setTaskState(DownloadTaskManager2.CONNECT_ERROR);
				connectError(o);
			}
				break;
			case DownloadService2.ACTION_CONNECTING: // 正在连接
			{
				o.setTaskState(DownloadTaskManager2.CONNECTING);
				connecting(o);
			}
				break;
			case DownloadService2.ACTION_PARSERING: // 解析中
			{
				DownData2DBManager.Builder().deleteByFileId(o.getFiles_id());
				o.setTaskState(DownloadTaskManager2.PARSERING);
				parsering(o);
			}
				break;
			case DownloadService2.ACTION_FINISHED: // 下载完成
			{
				o.setTaskState(DownloadTaskManager2.FINISHED);
				downloadFinished(o);
			}
				break;
			default:
				break;
		}
	}

	/**
	 * 进度更新
	 * 
	 * @param data
	 *            FileData
	 * @param progress
	 *            进度
	 * @param currentSize
	 *            当前下载大小
	 * @param totalSize
	 *            文件总大小
	 */
	protected abstract void updateProgress(FileData data, int progress,
			long currentSize);

	/**
	 * 下载路径非法
	 * 
	 * @param data
	 *            FileData
	 */
	protected abstract void pathError(FileData data);

	/**
	 * 连接失败
	 * 
	 * @param data
	 *            FileData
	 */
	protected abstract void connectError(FileData data);

	/**
	 * 正在连接中
	 * 
	 * @param data
	 *            FileData
	 */
	protected abstract void connecting(FileData data);

	/**
	 * 解析中
	 * 
	 * @param data
	 *            FileData.java
	 */
	protected abstract void parsering(FileData data);

	/**
	 * 解析完成,整个下载流程结束
	 * 
	 * @param data
	 *            FileData
	 */
	protected abstract void downloadFinished(FileData data);

	/**
	 * 正在打包.....
	 * 
	 * @param data
	 *            FileData
	 */
	// protected abstract void packaging(FileData data);

}
