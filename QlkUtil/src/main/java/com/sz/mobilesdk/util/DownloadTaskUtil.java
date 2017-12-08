package com.sz.mobilesdk.util;

import android.content.Context;
import android.content.Intent;

import com.sz.mobilesdk.models.FileData;
import com.sz.mobilesdk.models.FolderInfo;
import com.sz.mobilesdk.receiver.DownloadReceiver;
import com.sz.mobilesdk.service.DownloadService;
import com.sz.mobilesdk.service.DownloadService2;

/**
 * 下载工具管理
 * 
 * @author hudq
 * 
 */
public class DownloadTaskUtil
{

	/**
	 * 开启下载单个文件服务
	 * 
	 * @param ctx
	 * @param o
	 */
	public static void startDownloadFile(Context ctx, FileData o)
	{
		Intent intent = new Intent(ctx, DownloadService2.class);
		intent.putExtra("FileData", o);
		ctx.startService(intent);
	}

	/**
	 * 停止指定的下载单个文件的任务
	 * 
	 * @param ctx
	 * @param fileId
	 */
	public static void stopDownloadFile(Context ctx, String fileId)
	{
		Intent intent = new Intent(ctx, DownloadService2.class);
		intent.setAction(DownloadService2.ACTION_STOP);
		intent.putExtra("taskId", fileId);
		ctx.startService(intent);
	}

	/**
	 * 停止所有下载单个文件任务
	 * 
	 * @param ctx
	 */
	public static void stopDownloadFile(Context ctx)
	{
		Intent intent = new Intent(ctx, DownloadService2.class);
		intent.setAction(DownloadService2.ACTION_ALL_STOP);
		ctx.startService(intent);
	}

	// ////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////

	/**
	 * 开始下载任务
	 * 
	 * @param ctx
	 * @param o
	 *            下载产品模型
	 * @param position
	 *            下载文件位置索引
	 */
	@Deprecated
	public static void startDownloadTask(Context ctx, FolderInfo o, int position)
	{
		DownloadReceiver.sTaskIdSet.add(o.getMyProId());
		o.setPosition(position);

		Intent intent = new Intent(ctx, DownloadService.class);
		intent.putExtra("DownloadInfo", o);
		ctx.startService(intent);
	}

	/**
	 * 停止当前下载任务
	 * 
	 * @param ctx
	 * @param myProId
	 *            文件id
	 */
	@Deprecated
	public static void stopDownloadTask(Context ctx, String myProId)
	{
		DownloadReceiver.sTaskIdSet.remove(myProId);

		Intent intent = new Intent(ctx, DownloadService.class);
		intent.setAction(DownloadService.ACTION_STOP);
		intent.putExtra("myProId", myProId);
		ctx.startService(intent);
	}

	/**
	 * 停止所有下载任务
	 * 
	 * @param context
	 */
	@Deprecated
	public static void stopAllDownloadTask(Context ctx)
	{
		Intent intent = new Intent(ctx, DownloadService.class);
		intent.setAction(DownloadService.ACTION_ALL_STOP);
		ctx.startService(intent);
	}

}
