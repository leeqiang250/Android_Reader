package com.sz.mobilesdk.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.database.bean.Downdata;
import com.sz.mobilesdk.database.practice.AlbumDAOImpl;
import com.sz.mobilesdk.database.practice.DowndataDAOImpl;
import com.sz.mobilesdk.models.FolderInfo;
import com.sz.mobilesdk.service.DownloadService;
import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.ParserFileUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;

/**
 * 下载任务管理类
 * <p>
 * 
 * 已废除！
 * 
 * @author hudq
 * 
 */
@Deprecated
public class DownloadTaskManager
{

	// 开始，默认
	public static final int INIT = 0;
	// 等待
	public static final int WAITING = 1;
	// 连接
	public static final int CONNECTING = 2;
	// 暂停
	public static final int PAUSE = 3;
	// 下载中,更新进度中
	public static final int DOWNLOADING = 4;
	// 解析
	public static final int PARSER = 5;
	// 下载异常，ftpPath关闭
	public static final int DOWNLOAD_ERROR = 6;
	// 连接异常，一般是服务端文件异常
	public static final int CONNECT_ERROR = 7;

	private Context mContext;
	private FolderInfo mInfo;
	/** 标示线程是否暂停 */
	public boolean isPause = false;
	private FTPManager mFtpManager = new FTPManager();
	private static ExecutorService mFixedExecutor;
	private static final int POOL_SIZE = 1;

	public DownloadTaskManager(Context context, FolderInfo info)
	{
		this.mContext = context;
		this.mInfo = info;
		if (mFixedExecutor == null)
		{
			mFixedExecutor = Executors.newFixedThreadPool(POOL_SIZE);
		}
	}

	/**
	 * 关闭
	 */
	public static void closeExecutorService()
	{
		if (mFixedExecutor != null && !mFixedExecutor.isShutdown())
		{
			mFixedExecutor.shutdownNow();
			mFixedExecutor = null;
			SZLog.i("shutdown threads pool");
		}
	}

	/**
	 * 下载信息,开始下载
	 * 
	 */
	public synchronized void downloadInfo()
	{
		DownloadThread thread = new DownloadThread(this.mContext, this.mInfo);
		mFixedExecutor.execute(thread);
	}

	/**
	 * 下载线程类
	 * 
	 * @author hudq
	 * 
	 */
	private class DownloadThread extends Thread
	{
		private final int offset = "ftp://".length();
		private final String TAG = DownloadThread.class.getSimpleName();
		private Context context;
		// 标示线程是否在下载中
		private boolean isThreadDownloading = false;
		private FolderInfo o;

		public DownloadThread(Context context, FolderInfo o)
		{
			this.context = context;
			this.o = o;
			Log.v(TAG, "offset = " + offset);
		}

		@Override
		public void run()
		{
			super.run();
			String myProId = o.getMyProId();
			int position = o.getPosition();
			String ftpPath = o.getFtpUrl();
			SZLog.v(TAG, "URL= " + ftpPath);
			// String albumId = AlbumDAOImpl.getInstance().findAlbumId(myProId);
			// if (!TextUtils.isEmpty(albumId)) delAllFile(myProId);
			// if (!CommonUtil.isSdCardCanUsed()) return;

			// 正在连接ftp服务器
			Intent in = new Intent(DownloadService.ACTION_CONNECTING);
			in.putExtra("position", position);
			this.context.sendBroadcast(in);

			// 获取http路径
			// @remove code Constant.LoginConfig.way ==Fields._SCANING;
			boolean isScaning = true;
			// String httpPath = APIUtil.getDownloadProductsUrl(myProId);
			// 通过http路径获取ftp下载路径
			// String ftpPath = APIUtil.getFTPUrlByHttpUrl(httpPath);
			// SZLog.w(TAG, "httpPath = " + httpPath);
			// SZLog.w(TAG, "ftpPath = " + ftpPath);
			// eg: ftpPath =
			// "ftp://video.suizhi.net:8650/3c335fde-caeaaedgb.drm";

			// ftp路径不合法！！！！
			boolean illegalsFtpPath = (StringUtil.isEmptyOrNull(ftpPath)
					|| !ftpPath.startsWith("ftp://") || offset > ftpPath
					.length());
			if (illegalsFtpPath)
			{
				ftpPathError(position, myProId);
				return;
			}
			try
			{
				String filePath_ = FileUtil.getNameFromFilePath(ftpPath);
				//SZLog.w(TAG, "filePathName:" + filePath_);
				String host;
				int port;
				String subFtpPath = ftpPath.substring(offset);
				//SZLog.w(TAG, "subFtpPath = " + subFtpPath);
				if (isScaning)
				{
					String[] mOffsetScan = subFtpPath.split(":");
					String portString = mOffsetScan[1].split("\\/")[0];
					host = mOffsetScan[0];
					port = ConvertToUtil.toInt(portString);
				} else
				{
					host = subFtpPath.split("\\/")[0];
					port = 21;
				}
				// 连接ftp服务器
				FTPClient ftpClient = new FTPClient();
				SZLog.e(TAG, "2.connect ftp server");
				boolean isConnect = mFtpManager.connect(ftpClient, host, port,
						FTPManager.ftpName, FTPManager.ftpPassword);
				if (!isConnect)
				{
					connectError(position, myProId);
					return;
				}
				FTPFile[] files = ftpClient.listFiles(filePath_);
				long fileSize = 0;
				if (files != null && files.length > 0)
				{
					// 文件大小
					fileSize = files[0].getSize();
				}
				if (fileSize == 0)
				{
					connectError(position, myProId);
					return;
				}
				// 下载
				SZInitInterface.checkFilePath();
				StringBuffer strbuffer = new StringBuffer();
				final String localPath = strbuffer
						.append(PathUtil.DEF_DOWNLOAD_DRM_PATH)
						.append(File.separator).append(myProId)
						.append(Fields._DRM).toString();
				// 1.判断数据库中有没有保存下载信息
				// 2.有，继续上次下载；没有创建线程进行下载
				// 3.如果下载成功了，不继续下载
				Downdata mSaveData = DowndataDAOImpl.getInstance()
						.findDowndataById(myProId);
				if (mSaveData == null)
				{
					SZLog.i("first download");
					mSaveData = new Downdata();
					mSaveData.setId(String.valueOf(System.currentTimeMillis()));
					mSaveData.setFileOffsetstr(0 + "M"); // 本地已下载文件大小,格式化后的eg：22M
					mSaveData.setIsDownload("0");
					mSaveData.setCompleteSize(0); // 完成大小
					mSaveData.setLocalpath(localPath);
					mSaveData.setMyProduct_id(myProId);
					mSaveData.setTotalSize(FormatterUtil.formatSize(fileSize)); // 总大小，eg:23.2M
					mSaveData.setFtpPath(ftpPath);
				}
				// 满足上面所有条件，开始下载
				startDownloader(ftpClient, mSaveData, fileSize, filePath_);
			} catch (Exception e)
			{
				ftpPathError(position, myProId);
			}
		}

		/**
		 * ftp路径非法
		 * 
		 * @param position
		 * @param myProId
		 */
		private void ftpPathError(int position, String myProId)
		{
			SZLog.e(TAG, "ftpPath is illegals");
			Intent intent = new Intent(DownloadService.ACTION_ERROR);
			intent.putExtra("position", position);
			intent.putExtra("myProId", myProId);
			this.context.sendBroadcast(intent);
		}

		/**
		 * 连接server失败
		 * 
		 * @param position
		 * @param myProId
		 */
		private void connectError(int position, String myProId)
		{
			SZLog.e(TAG, "connect ftp failed");
			Intent intent = new Intent(DownloadService.ACTION_CONNECT_ERROR);
			intent.putExtra("position", position);
			intent.putExtra("myProId", myProId);
			this.context.sendBroadcast(intent);
		}

		/**
		 * 开始下载了
		 * 
		 * @param ftpClient
		 * @param data
		 * @param fileSize
		 * @param filePath_
		 */
		private void startDownloader(FTPClient ftpClient, Downdata data,
				long fileSize, String filePath_)
		{
			SZLog.e(TAG, "3.connect success,start download task");
			isThreadDownloading = true;
			String myProId = data.getMyProduct_id();
			String ftpPath = data.getFtpPath();
			String localPath = data.getLocalpath();

			RandomAccessFile rAcFile = null;
			InputStream stream = null;
			try
			{
				//long localSize = 0;
				// 判断要下载的文件是否已经存在本地
				//if (FileUtil.checkFilePathExists(localPath))
				//{
				//	localSize = FileUtil.getFileSize(localPath);
				//}
				//SZLog.d(TAG,
				//		"localSize = " + FormatterUtil.formatSize(localSize));
				//SZLog.w("filePathName:" + filePath_); // 得到ftp文件path,eg:abcdef.drm
				String formatTotalSize = FormatterUtil.formatSize(fileSize);
				SZLog.d(TAG, "formatTotalSize = " + formatTotalSize);

				File tmpFile = FileUtil.createFile(localPath); // 创建本地目录文件
				rAcFile = new RandomAccessFile(tmpFile, "rw");
				long offsetSize = data.getCompleteSize();
				SZLog.d(TAG, "offsetSize = " + offsetSize); // 已经完成进度位置
				rAcFile.seek(offsetSize);
				long mCurrentSize = offsetSize;
				ftpClient.setRestartOffset(offsetSize);
				stream = ftpClient.retrieveFileStream(filePath_);

				byte[] buffer = new byte[2048];
				int length = -1;
				SZLog.e(TAG, "4.start write files");
				long time = 0;
				int mTempPercent = 0;
				try
				{
					while (isThreadDownloading
							&& (length = stream.read(buffer)) != -1)
					{
						// 写入文件
						rAcFile.write(buffer, 0, length);
						// 累加文件完成进度
						mCurrentSize += length;
						int mPercentage = (int) (mCurrentSize * 100 / fileSize);
						// 暂停,保存数据库
						if (isPause)
						{
							SZLog.e(TAG, "暂停下载任务");
							isThreadDownloading = false;
							DowndataDAOImpl.getInstance().updateSaveDownData(
									myProId, mPercentage, formatTotalSize,
									mCurrentSize, localPath, ftpPath);
							// 发送最后一次的进度更新,防止界面显示进度误差
							sendProgress(mPercentage, mCurrentSize, fileSize,
									true);
							return;
						}
						// 通知ui更新进度
						if (System.currentTimeMillis() - time > 700)
						{
							SZLog.e(TAG, this.getName() + "progress = "
									+ mPercentage + "%;downloaded："
									+ FormatterUtil.formatSize(mCurrentSize));
							if (mPercentage > mTempPercent)
							{
								sendProgress(mPercentage, mCurrentSize,
										fileSize, false);
							}
							time = System.currentTimeMillis();
							mTempPercent = mPercentage;
						}
					}

					// 正在下载，判断是否下载完成,完成通知更新ui
					if (isThreadDownloading && mCurrentSize >= fileSize)
					{
						SZLog.e(TAG, "5.download complete，mCurrentSize = "
								+ mCurrentSize + ";fileSize = " + fileSize);
						//开始解析
						ParserFileUtil.getInstance().parserDRMFiles(this.context,
								myProId, this.o.getPosition(), this.o);
					}
				} catch (IOException e)
				{
					e.printStackTrace();
					// 下载过程中，读取流异常，保存进度
					DowndataDAOImpl.getInstance().updateSaveDownData(myProId,
							mTempPercent, formatTotalSize, mCurrentSize,
							localPath, ftpPath);
					connectError(this.o.getPosition(), myProId);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
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
		 * 发送进度
		 * 
		 * @param mPercentage
		 * @param mCurrentSize
		 * @param fileSize
		 * @param isLastSaveProgress
		 *            是否是最后一次保存的进度
		 */
		private void sendProgress(int mPercentage, long mCurrentSize,
				long fileSize, boolean isLastSaveProgress)
		{
			Intent intent = new Intent(DownloadService.ACTION_UPDATE);
			intent.putExtra("position", this.o.getPosition());
			intent.putExtra("progress", mPercentage);
			intent.putExtra("currentSize", mCurrentSize);
			intent.putExtra("totalSize", fileSize);
			intent.putExtra("isLastSaveProgress", isLastSaveProgress);
			this.context.sendBroadcast(intent);
		}

		/**
		 * 要下载的专辑已经存在或部分存在，删除
		 * 
		 * @param myProid
		 */
		@Deprecated
		protected void delAllFile(final String myProid)
		{
			String decodePath = PathUtil.DEF_SAVE_FILE_PATH
					+ File.separator + myProid;
			FileUtil.delAllFile(decodePath);
			Thread clearThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					String Album_Id = AlbumDAOImpl.getInstance().findAlbumId(
							myProid);// 获取专辑ID
					if (Album_Id != null)
					{
						AlbumDAOImpl.getInstance().DeleteAlbum(Album_Id);
						String albumContentIdList = AlbumDAOImpl.getInstance()
								.findAlbumContentId(Album_Id);
						if (albumContentIdList != null)
						{
							AlbumDAOImpl.getInstance().DeleteAlbumContent(
									Album_Id);
						}
						String RightContentIdList = AlbumDAOImpl.getInstance()
								.findRightId(Album_Id);
						if (RightContentIdList != null)
						{
							AlbumDAOImpl.getInstance().DeleteRight(Album_Id);
						}
						ArrayList<String> assetIdList = AlbumDAOImpl
								.getInstance().findAssetId(Album_Id);
						if (assetIdList != null && assetIdList.size() > 0)
						{
							AlbumDAOImpl.getInstance().DeleteAsset(Album_Id);
							for (int i = 0; i < assetIdList.size(); i++)
							{
								String assetId = assetIdList.get(i);
								String PermissionId = AlbumDAOImpl
										.getInstance()
										.findPermissionId(assetId);
								if (PermissionId != null)
								{
									AlbumDAOImpl.getInstance()
											.DeletePermission(assetId);
									ArrayList<String> perconstraintIdList = AlbumDAOImpl
											.getInstance().findPerconstraintId(
													PermissionId);
									if (perconstraintIdList != null)
									{
										AlbumDAOImpl.getInstance()
												.DeletePerconstraint(
														PermissionId);
									}
								}
							}
						}
					}
				}
			});
			// Executors.newCachedThreadPool().execute(clearThread);
			clearThread.start();
		}
	}

}
