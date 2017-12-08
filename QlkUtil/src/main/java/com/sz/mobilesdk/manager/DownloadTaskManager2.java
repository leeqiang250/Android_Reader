package com.sz.mobilesdk.manager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.manager.db.DownData2DBManager;
import com.sz.mobilesdk.models.DownData2;
import com.sz.mobilesdk.models.FileData;
import com.sz.mobilesdk.service.DownloadService2;
import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.ParserFileUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SZLog;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 下载任务管理类2
 * 
 */
@Deprecated
public class DownloadTaskManager2
{
	// 开始，默认状态
	public static final int INIT = 0;
	// 等待
	public static final int WAITING = 1;
	// 连接
	public static final int CONNECTING = 2;
	// 暂停
	public static final int PAUSE = 3;
	// 下载中，更新进度
	public static final int DOWNLOADING = 4;
	// 解析
	public static final int PARSERING = 5;
	// 下载异常，ftpPath错误
	public static final int DOWNLOAD_ERROR = 6;
	// 连接异常，一般是服务端文件异常
	public static final int CONNECT_ERROR = 7;
	// 下载完毕！
	public static final int FINISHED = 8;
	// 正在验证
	public static final int CHECKING = 9;
	// 等待验证
	public static final int WAITING_CHECKING = 10;

	/** 标示线程是否暂停 */
	public boolean isPause = false;
	public boolean isBreak = false;
	private Context mContext;
	private FileData mFileData;
	private FTPManager mFtpManager = new FTPManager();
	private static ExecutorService mFixedExecutor;
	private static final int POOL_SIZE = 1; // 线程池大小

	public DownloadTaskManager2(Context mContext, FileData data)
	{
		this.mContext = mContext;
		this.mFileData = data;
		if (mFixedExecutor == null)
		{
			mFixedExecutor = Executors.newFixedThreadPool(POOL_SIZE);
		}
	}

	/**
	 * 关闭
	 */
	public static void shutdownTask()
	{
		if (mFixedExecutor != null && !mFixedExecutor.isShutdown())
		{
			try
			{
				mFixedExecutor.shutdown();
				mFixedExecutor.awaitTermination(3 * 1000, TimeUnit.SECONDS);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			} finally
			{
				mFixedExecutor.shutdownNow();
				mFixedExecutor = null;
			}
			SZLog.i("shutdownNow download threads pool");
		}
	}

	/**
	 * 下载信息,开始下载
	 * 
	 */
	public synchronized void download()
	{
		DownloadThread2 thread = new DownloadThread2(this.mContext,
				this.mFileData);
		mFixedExecutor.execute(thread);
	}

	/**
	 * 下载线程类2
	 * 
	 */
	private class DownloadThread2 extends Thread
	{
		private static final String TAG = "dt2";
		private final int offset = "ftp://".length();
		private Context context;
		private FileData o2;
		private boolean isThreadDownloading = false; // 标示线程是否在下载中

		public DownloadThread2(Context context, FileData data)
		{
			this.context = context;
			this.o2 = data;
		}

		@Override
		public void run()
		{
			super.run();
			if (isInterrupted())
			{
				connectError();
				return;
			}
			if(isBreak)
				return;
			try
			{
				// 一、正在连接，获取下载路径
				this.context.sendBroadcast(new Intent(
						DownloadService2.ACTION_CONNECTING).putExtra(
						"FileData", o2));

				// 获取下载路径
				String ftpPath = o2.getFtpUrl();
				SZLog.w(TAG, "ftp: " + ftpPath);
				boolean illegalsFtpPath = (ftpPath == null
						|| !ftpPath.startsWith("ftp://") || offset > ftpPath
						.length());
				if (illegalsFtpPath) // ftp路径不合法
				{
					ftpPathError();
					return;
				}
				String[] ftpOffset = ftpPath.substring(offset).split(":");
				String host = ftpOffset[0];
				int port = ConvertToUtil
						.toInt(ftpOffset[1].split("\\/")[0], 21);
				// 二、连接ftp服务器
				SZLog.i("2.connect ftp server");
				FTPClient ftpClient = new FTPClient();
				boolean hasConnectServer = (mFtpManager.connect(ftpClient,
						host, port, FTPManager.ftpName, FTPManager.ftpPassword));
				if (isBreak)
				{
					SZLog.v(TAG, "isBreak!");
					mFtpManager.disconnect(ftpClient);
					isPause = true;
					connectError();
					return;
				}
				if (!hasConnectServer)
				{
					connectError();
					return;
				}
				String filePath_ = FileUtil.getNameFromFilePath(ftpPath);
				FTPFile[] files = ftpClient.listFiles(filePath_);
				long fileSize = 0L;
				if (files != null && files.length > 0)
				{
					FTPFile file = files[0];
					fileSize = file.getSize(); // 文件大小
				}
				if (fileSize == 0L)
				{
					connectError();
					return;
				}
				SZInitInterface.checkFilePath();
				StringBuffer strbuffer = new StringBuffer();
				final String localPath = strbuffer
						.append(PathUtil.DEF_DOWNLOAD_DRM_PATH)
						.append(File.separator).append(o2.getFiles_id())
						.append(Fields._DRM).toString();
				// 1.判断数据库中有没有保存下载信息
				// 2.有，继续上次下载；没有创建线程进行下载
				// 3.如果下载成功了，不继续下载
				DownData2 data2 = DownData2DBManager.Builder().findByFileId(
						o2.getFiles_id());
				if (data2 == null)
				{
					Log.v(TAG, "first download.");
					data2 = new DownData2();
					data2.setCompleteSize(0L);
					data2.setFileId(o2.getFiles_id());
					data2.setFileName(o2.getName());
					data2.setFileSize(fileSize);
					data2.setFtpPath(ftpPath);
					data2.setLocalPath(localPath);
					data2.setMyProId(o2.getSharefolder_id());
				}
				// 三、开始下载。
				startDownload(ftpClient, data2, filePath_);
			} catch (Exception e)
			{
				ftpPathError();
			}
		}

		/**
		 * 开始下载了
		 * 
		 * @param ftpClient
		 * @param data2
		 * @param remoteFileName
		 */
		private void startDownload(FTPClient ftpClient, DownData2 data2,
				String remoteFileName)
		{
			SZLog.i("3.connect success,start download task");
			isThreadDownloading = true;
			String localPath = data2.getLocalPath();
			long fileSize = data2.getFileSize();

			RandomAccessFile rAcFile = null;
			InputStream stream = null;
			try
			{
				// long localSize = 0;
				// 判断要下载的文件是否已经存在本地
				// if (FileUtil.checkFilePathExists(localPath))
				// {
				// localSize = FileUtil.getFileSize(localPath);
				// }
				// SZLog.d(TAG,
				// "localSize = " + FormatterUtil.formatSize(localSize));
				//String totalSize = FormatterUtil.formatSize(fileSize);
				//SZLog.d(TAG, "totalSize = " + totalSize);

				// 创建本地目录文件
				File tmpFile = FileUtil.createFile(localPath);
				rAcFile = new RandomAccessFile(tmpFile, "rw");
				long offsetSize = data2.getCompleteSize(); // 文件完成大小，即是下载的 起始位置
				rAcFile.seek(offsetSize);
				long mCurrentSize = offsetSize;
				ftpClient.setRestartOffset(offsetSize);
				stream = ftpClient.retrieveFileStream(remoteFileName);
				SZLog.d(TAG,
						"offsetSize = " + FormatterUtil.formatSize(offsetSize));

				SZLog.i("4.start write files");
				long time = 0;
				byte[] buffer = new byte[2048];
				int length = -1, mTempPercent = 0;
				try
				{
					while (isThreadDownloading
							&& (length = stream.read(buffer)) != -1)
					{
						rAcFile.write(buffer, 0, length);
						mCurrentSize += length;
						int mPercentage = (int) (mCurrentSize * 100 / fileSize);
						if (isPause) // 暂停,保存数据库
						{
							SZLog.v(TAG, "pause!");
							isThreadDownloading = false;
							data2.setCompleteSize(mCurrentSize);
							data2.setProgress(mPercentage);
							DownData2DBManager.Builder().saveOrUpdate(data2);
							sendProgress(mPercentage, mCurrentSize, true); // 发送最后一次的进度更新,防止界面显示进度误差
							return;
						}
						// 通知ui更新进度
						if (System.currentTimeMillis() - time > 700)
						{
							SZLog.d(this.getName() + "progress = "
									+ mPercentage + "%;downloaded："
									+ FormatterUtil.formatSize(mCurrentSize));
							if (mPercentage > mTempPercent)
							{
								sendProgress(mPercentage, mCurrentSize, false);
							}
							time = System.currentTimeMillis();
							mTempPercent = mPercentage;
						}
					}

					// 正在下载，判断是否下载完成,完成通知更新ui
					if (isThreadDownloading && mCurrentSize >= fileSize)
					{
						SZLog.i("5.download complete，mCurrentSize = "
								+ mCurrentSize + ";fileSize = " + fileSize);

						// 开始解析
						ParserFileUtil.getInstance().parserFile(context, o2);
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					// 下载过程中，读取流异常，保存进度
					data2.setCompleteSize(mCurrentSize);
					data2.setProgress(mTempPercent);
					DownData2DBManager.Builder().saveOrUpdate(data2);
					connectError();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				connectError();
			} finally
			{
				if (stream != null)
				{
					try
					{
						stream.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				if (rAcFile != null)
				{
					try
					{
						rAcFile.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				if (ftpClient != null && ftpClient.isConnected())
				{
					try
					{
						ftpClient.disconnect();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * ftp路径非法
		 */
		private void ftpPathError()
		{
			SZLog.e(TAG, "ftpPath is illegals");
			Intent intent = new Intent(DownloadService2.ACTION_DOWNLOAD_ERROR);
			intent.putExtra("FileData", this.o2);
			this.context.sendBroadcast(intent);
		}

		/**
		 * 连接server失败
		 */
		private void connectError()
		{
			SZLog.e(TAG, "connect ftp failed");
			Intent intent = new Intent(DownloadService2.ACTION_CONNECT_ERROR);
			intent.putExtra("FileData", this.o2);
			this.context.sendBroadcast(intent);
		}

		/**
		 * 发送进度
		 * 
		 * @param mPercentage
		 * @param mCurrentSize
		 * @param isLastProgress
		 *            是否是最后一次保存的进度
		 */
		private void sendProgress(int mPercentage, long mCurrentSize,
				boolean isLastProgress)
		{
			Intent intent = new Intent(DownloadService2.ACTION_UPDATE);
			intent.putExtra("FileData", this.o2);
			intent.putExtra("progress", mPercentage);
			intent.putExtra("currentSize", mCurrentSize);
			intent.putExtra("isLastProgress", isLastProgress);
			this.context.sendBroadcast(intent);
		}
	}

}
