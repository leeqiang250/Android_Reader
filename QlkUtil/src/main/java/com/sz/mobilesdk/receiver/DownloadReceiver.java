package com.sz.mobilesdk.receiver;

import java.util.LinkedHashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sz.mobilesdk.database.practice.DowndataDAOImpl;
import com.sz.mobilesdk.models.FolderInfo;
import com.sz.mobilesdk.service.DownloadService;
import com.sz.mobilesdk.util.DownloadTaskUtil;
import com.sz.mobilesdk.util.SZLog;

/**
 * 注册下载的广播 <br/>
 * <p>
 * 
 * 已废除<br/>
 * <p>
 * IntentFilter downloadFilter = new IntentFilter(); <br/>
 * downloadFilter.addAction(DownloadService.ACTION_FINISH); <br/>
 * downloadFilter.addAction(DownloadService.ACTION_UPDATE); <br/>
 * registerReceiver(downloadReceiver, downloadFilter); <br/>
 * 
 */
public abstract class DownloadReceiver extends BroadcastReceiver
{

	/** 存储下载任务的id */
	public static Set<String> sTaskIdSet;

	public DownloadReceiver()
	{
		if (sTaskIdSet == null)
		{
			sTaskIdSet = new LinkedHashSet<String>();
		}
		sTaskIdSet.clear();
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent == null) return;
		// 必传参数position
		int position = intent.getIntExtra("position", 0);
		SZLog.d("position", "position = " + position);
		String action = intent.getAction();
		if (action == null) return;
		switch (action)
		{
			case DownloadService.ACTION_UPDATE:
			{
				long currentSize = intent.getLongExtra("currentSize", 0);
				long totalSize = intent.getLongExtra("totalSize", 0);
				int progress = intent.getIntExtra("progress", 0);
				boolean isLastSaveProgress = intent.getBooleanExtra(
						"isLastSaveProgress", false);
				SZLog.d("isLastSaveProgress = " + isLastSaveProgress);
				// 进度更新操作
				updateProgress(position, progress, currentSize, totalSize,
						isLastSaveProgress);
			}
				break;
			case DownloadService.ACTION_ERROR:
			{
				String myProId = intent.getStringExtra("myProId");
				// ftp路径异常或关闭，下载失败
				pathError(position, myProId);
				sTaskIdSet.remove(myProId);
				// 暂停下载
				DownloadTaskUtil.stopDownloadTask(context, myProId);
				Toast.makeText(context, "下载链接异常或错误", Toast.LENGTH_SHORT).show();
				// :删除本地下载的部分文件，清除数据库进度记录
			}
				break;
			case DownloadService.ACTION_CONNECT_ERROR:
			{
				String myProId = intent.getStringExtra("myProId");
				SZLog.e("", "connect error : " + myProId);
				sTaskIdSet.remove(myProId);
				// 连接异常，失败
				connectError(position, myProId);
				// 暂停任务
				DownloadTaskUtil.stopDownloadTask(context, myProId);
				Toast.makeText(context, "连接服务器失败", Toast.LENGTH_SHORT).show();
			}
				break;
			case DownloadService.ACTION_CONNECTING:
			{
				// 正在连接
				connecting(position);
			}
				break;
			case DownloadService.ACTION_PARSER:
			{
				// 解析
				String myProId = intent.getStringExtra("myProId");
				FolderInfo o = intent.getParcelableExtra("DownloadInfo");
				sTaskIdSet.remove(myProId);
				// 删除下载记录
				DowndataDAOImpl.getInstance().deleteDowndataByMyProId(myProId);

				parser(position, myProId, o);
			}
				break;

			case DownloadService.ACTION_FINISHED:
			{
				// 解析文件完毕,通知ui更新
				downloadFinished(position);
			}
				break;
		}
	}

	/**
	 * 进度更新
	 * 
	 * @param position
	 *            位置索引
	 * @param progress
	 *            进度
	 * @param currentSize
	 *            当前下载大小
	 * @param totalSize
	 *            文件总大小
	 * @param isLastSaveProgress
	 *            是否是最后一次发送进度
	 */
	protected abstract void updateProgress(int position, int progress,
			long currentSize, long totalSize, boolean isLastSaveProgress);

	/**
	 * 下载路径非法
	 * 
	 * @param position
	 * @param myProId
	 */
	protected abstract void pathError(int position, String myProId);

	/**
	 * 连接失败
	 * 
	 * @param position
	 * @param myProId
	 */
	protected abstract void connectError(int position, String myProId);

	/**
	 * 正在连接中
	 * 
	 * @param position
	 */
	protected abstract void connecting(int position);

	/**
	 * 下载完成，开始解析文件了。 <br/>
	 * 
	 * 复写此方法，如果解析文件的方法，则在其中调用。
	 * 
	 * @param position
	 * @param myProId
	 * @param downloadInfo
	 *            DownloadInfo.java
	 */
	protected abstract void parser(int position, String myProId,
			FolderInfo downloadInfo);

	/**
	 * 下载完成,操作通知ui更新
	 * 
	 * @param position
	 */
	protected abstract void downloadFinished(int position);

}
