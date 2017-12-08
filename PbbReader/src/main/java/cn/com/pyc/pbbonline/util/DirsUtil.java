package cn.com.pyc.pbbonline.util;

import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.TimeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import cn.com.pyc.pbbonline.bean.event.CopyOnlineDataEvent;
import de.greenrobot.event.EventBus;

/**
 * 获取目录path工具
 * 
 * @author hudq
 */
public class DirsUtil
{
	/**
	 * 获取专辑文件保存的本地路径 <br/>
	 * 每条记录下有多个专辑<br/>
	 * name = username_id; <br/>
	 * sdcard/Android/data/cn.com.pyc.pbb/SZOnline/name/file/folderId/
	 * 
	 * @param userName
	 * @param shareId
	 * @param folderId
	 * @return
	 */
	public static String getSaveFilePath(String userName, String shareId, String folderId)
	{
		String name = userName + Fields._LINE + shareId;
		PathUtil.checkSaveFilePath(name);
		StringBuilder mFileSb = new StringBuilder();
		return mFileSb.append(PathUtil.DEF_SAVE_FILE_PATH)
					.append(File.separator)
					.append(folderId)
					.append(File.separator).toString();
	}

	/**
	 * 获取记录保存文件的本地路径 <br/>
	 * 每一条记录对应一个文件夹<br/>
	 * name = username_id; <br/>
	 * sdcard/Android/data/cn.com.pyc.pbb/SZOnline/name/
	 * 
	 * @param userName
	 * @param shareId
	 * @return
	 */
	public static String getSavePath(String userName, String shareId)
	{
		String name = userName + Fields._LINE + shareId;
		StringBuilder mFileSb = new StringBuilder();
		return mFileSb.append(PathUtil.getSDCard())
				.append(File.separator)
				.append(PathUtil.getSZOffset())
				.append(File.separator)
				.append(name)
				.append(File.separator).toString();
	}

	/**
	 * 检查sd卡上存储的日志文件,达到临界值则清除日志文件。
	 */
	public static void checkSDCardCrashLog(String formatterDate)
	{
		final int maxLogNum = 20;
		// sdcard/sz/crash/
		String path = PathUtil.getSZCrashPath();
		File dir = new File(path);
		if (!dir.exists())
			return;
		if (!dir.isDirectory())
			return;

		File[] files = dir.listFiles();
		if (files == null)
			return;

		int fileCount = files.length;
		SZLog.i("log count: " + fileCount);
		if (fileCount > maxLogNum)
			clearCrashLogs(files, formatterDate);
	}

	// 清除保存的log
	private static void clearCrashLogs(File[] files, String formatterDate)
	{
		try
		{
			for (File file : files)
			{
				if (file != null && file.isFile())
				{
					// name: 2015-11-19-16-52-39-453
					String fileName = FileUtil.getNameFromFileName(file.getName());
					Date date = TimeUtil.getDateFromDateString(fileName, formatterDate);
					if (date != null && new Date().after(date))
					{
						// 删除除今天之前记录的log
						file.delete();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param srcFiles
	 * @param desFiles
	 *            目标文件夹
	 */
	public static void copy(File srcFiles, File desFiles)
	{
		if (!srcFiles.exists())
		{
			SZLog.e("", "源文件路径缺失~");
			EventBus.getDefault().post(new CopyOnlineDataEvent(-1, -1));
			return;
		}

		if (!desFiles.exists()) // 如果文件夹不存在
			desFiles.mkdir(); // 建立新的文件夹
		File[] fl = srcFiles.listFiles();
		try
		{
			for (int i = 0; i < fl.length; i++)
			{
				if (fl[i].isFile())
				{ // 如果是文件类型就复制文件
					long mCurrentSize = 0;
					int length = -1, mTempPercent = 0;

					FileInputStream fis = new FileInputStream(fl[i]);
					FileOutputStream out = new FileOutputStream(new File(desFiles.getPath()
							+ File.separator + fl[i].getName()));

					long totalSize = fis.available();
					byte[] buffer = new byte[2048];
					CopyOnlineDataEvent event = new CopyOnlineDataEvent();
					event.setOrder(i + 1);
					while((length = fis.read(buffer)) != -1)
					{
						out.write(buffer); 		// 复制文件内容
						mCurrentSize += length;	//处理进度数据
						int mPercentage = (int) (mCurrentSize * 100 / totalSize);
						if (mPercentage > mTempPercent)
						{
							//发送进度
							SZLog.d("DirsUtil", "progress(%): " + mPercentage);
							event.setProgress(mPercentage);
							EventBus.getDefault().post(event);
						}
						mTempPercent = mPercentage;
					}
					event = null;
					out.close(); // 关闭输出流
					fis.close(); // 关闭输入流
				}
				if (fl[i].isDirectory())
				{ // 如果是文件夹类型
					File des = new File(desFiles.getPath() + File.separator + fl[i].getName());
					des.mkdir(); // 在目标文件夹中创建相同的文件夹
					copy(fl[i], des); // 递归调用方法本身
				}
			}
			FileUtil.delAllFile(srcFiles.getAbsolutePath());
			EventBus.getDefault().post(new CopyOnlineDataEvent(-1, -1));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
