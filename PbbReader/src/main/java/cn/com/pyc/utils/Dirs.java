package cn.com.pyc.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.qlk.util.media.QlkDirs;
import com.qlk.util.tool.Util.FileUtil;

import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * 这里默认最多2个盘：内置硬盘，外置sd卡
 * <p>
 * 如果有两个以上的盘，则需要重写此代码，目前是抛出null异常或者返回BootExtra相关信息
 * 
 * @author user32
 */
public final class Dirs
{
	// 盘的标示符，不要改动其值。当系统升级时根目录路径可能改变，会影响加解密时文件位置的恢复，故数据库存储时不存绝对路径
	private static final String DISK_DEFAULT = "0";	// 0和1的值不要更改
	private static final String DISK_EXTRA = "1";

	public static final String DIR_PRIVACY = "/.pyc";
	private static final String DIR_CAMERA = "/DCIM/Camera";
	private static final String DIR_SM = "/send";
	private static final String DIR_PAY = "/pay";
	private static final String DIR_LOG = "/log";
	private static final String DIR_AD = "/ad";

	/**
	 * 因为有许多非正常进入鹏保宝的方式，所以为了防止空指针异常，使用CardsPaths之前先调用此方法
	 * 
	 */
	public static ArrayList<String> getCardsPaths()
	{
		return QlkDirs.getCardsPaths();
	}

	/**
	 * 在领取钥匙成功时调用（用户自己领取）
	 * <p>
	 * 输入密码进入主界面时调用（主要是由PC端导入钥匙时此方法起作用，或者用户删除了.pyc文件夹等情况）
	 * <p>
	 * 在搜索明文时，会出现同步情况
	 * 
	 * @param context
	 */
	public synchronized static ArrayList<String> reGetCardsPaths(Context context)
	{
		QlkDirs.reGetCardsPaths(context);
		makeAllDirs(context);
		return QlkDirs.getCardsPaths();
	}

	private static void makeAllDirs(Context context)
	{
		if (context.getPackageName().equals("cn.com.pyc.pbb"))
		{
			return;
		}
		
		File willCrtFile = null;

		// 建.pyc文件夹
		ArrayList<String> cardsPaths = getCardsPaths();
		for (String boot : cardsPaths)
		{
			willCrtFile = new File(boot + DIR_PRIVACY);
			if (!willCrtFile.exists())
			{
				willCrtFile.mkdirs();
			}

			// 建立nomedia文件
			willCrtFile = new File(boot + DIR_PRIVACY + "/.nomedia");
			if (!willCrtFile.exists())
			{
				try
				{
					willCrtFile.createNewFile();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			// 建立提示文件
			willCrtFile = new File(boot + DIR_PRIVACY + "/请谨慎操作此文件夹下所有内容！");
			if (!willCrtFile.exists())
			{
				try
				{
					willCrtFile.createNewFile();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		// 建立log文件夹
		willCrtFile = new File(getPrivacyDir(getDefaultBoot()) + DIR_LOG);
		if (!willCrtFile.exists())
		{
			willCrtFile.mkdirs();
		}

		// 创建相册
		for (String boot : cardsPaths)
		{
			willCrtFile = new File(getCameraDir(boot));
			if (!willCrtFile.exists())
			{
				willCrtFile.mkdirs();
			}
		}

		// 建立广告文件夹
		willCrtFile = new File(getPrivacyDir(getDefaultBoot()) + DIR_AD);
		if (!willCrtFile.exists())
		{
			willCrtFile.mkdirs();
		}

		UserInfo userInfo = UserDao.getDB(context).getUserInfo();
		if (userInfo.isKeyNull())
		{
			return;
		}

		for (String boot : cardsPaths)
		{
			// 创建user文件夹
			willCrtFile = new File(getUserDir(context, boot));
			if (!willCrtFile.exists())
			{
				willCrtFile.mkdirs();
			}

			// 创建发送文件夹
			willCrtFile = new File(getSendDir(context, boot));
			if (!willCrtFile.exists())
			{
				willCrtFile.mkdirs();
			}

			// 创建发送付款文件夹
			willCrtFile = new File(getSendPayDir(context, boot));
			if (!willCrtFile.exists())
			{
				willCrtFile.mkdirs();
			}
		}
	}

	public static boolean isOnInternalDisk(String path)
	{
		return !TextUtils.isEmpty(path) && path.startsWith(getDefaultBoot());
	}

	public static String getDefaultBoot()
	{
		return QlkDirs.getDefaultBoot();
	}

	public static String getLogDir()
	{
		return getPrivacyDir(getDefaultBoot()) + DIR_LOG;
	}

	public static String getAdDir()
	{
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Android/data/cn.com.pyc.pbb/img";
		File file = new File(dir);
		if (!file.exists())
		{
			file.mkdirs();
		}
		else if (file.isFile())
		{
			file.delete();
			file.mkdirs();
		}
		return dir;
	}

	// 由pc端推钥匙时生成的临时文件
	public static String getPycTempPath()
	{
		return getDefaultBoot() + "/pyc.temp";
	}

	// 由pc端启动时生成的临时文件
	public static String getPycSucPath()
	{
		return getDefaultBoot() + "/suc.temp";
	}

	public static String getBootDir(String path)
	{
		if (path.startsWith(getDefaultBoot()))
		{
			return getDefaultBoot();
		}
		else if (path.startsWith(getExtraBoot()))
		{
			return getExtraBoot();
		}
		else
		{
			return getExtraBoot();
		}
	}

	public static String getExtraBoot()
	{
		return QlkDirs.getExtraBoot();
	}

	public static String getPrivacyDir(String path)
	{
		return getBootDir(path) + DIR_PRIVACY;
	}

	// 用相对路径存储，当根目录绝对路径改变（如系统升级）时不会受到影响
	public static String getRelativePath(String absltPath)
	{
		if (absltPath.startsWith(getDefaultBoot()))
		{
			return absltPath.replaceFirst(getDefaultBoot(), DISK_DEFAULT);
		}
		else if (absltPath.startsWith(getExtraBoot()))
		{
			return absltPath.replaceFirst(getExtraBoot(), DISK_EXTRA);
		}
		else
		{
			Log.w("cn.com.pyc", "getRelativePath");
			return absltPath.replaceFirst(getExtraBoot(), DISK_EXTRA); // 如果没有则返回外置（系统升级前的鹏保宝版本低于该版本）
		}
	}

	// 还原数据库中以相对路径存储的文件路径
	public static String getAbsolutePath(String rltvPath)
	{
		if (rltvPath.startsWith(DISK_DEFAULT))
		{
			return rltvPath.replaceFirst(DISK_DEFAULT, getDefaultBoot());
		}
		else if (rltvPath.startsWith(DISK_EXTRA))
		{
			return rltvPath.replaceFirst(DISK_EXTRA, getExtraBoot());
		}
		else
		{
			// 本数据库中是不会出现此种情况的，一旦出现则说明代码有bug
			return getDefaultBoot() + File.separator + "DCIM" + File.separator
					+ FileUtil.getFileName(rltvPath);		// 可能是从系统文件夹里过来的
		}
	}

	/**
	 * 获得隐私空间存储路径
	 * 
	 * @param path
	 *            要存放的目标盘符的任意一个文件的路径
	 * @return
	 */
	public static String getUserDir(Context context, String path)
	{
		final String userName = UserDao.getDB(context).getUserInfo().getUserName();
		String dir = getPrivacyDir(path) + File.separator + userName;
		if (dir.startsWith(getDefaultBoot()))
		{
			dir += "_default";
		}
		return dir;
	}

	/**
	 * 存储外发文件的目录
	 * 
	 * @param boot
	 * @return
	 */
	public static String getSendDir(Context context, String boot)
	{
		return getUserDir(context, boot) + DIR_SM;
	}

	public static String getSendPayDir(Context context, String boot)
	{
		return getSendDir(context, boot) + DIR_PAY;
	}

	/**
	 * 调用系统相机拍照或录像以及由pc端移入的密文解密时的默认路径
	 * 
	 * @param boot
	 * @return
	 */
	public static String getCameraDir(String path)
	{
		return getBootDir(path) + DIR_CAMERA;
	}

	public static boolean isStorageInUse()
	{
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * @param des
	 *            目标空间
	 * @param need
	 *            所需大小
	 * @return true：不能写操作或者没有剩余空间
	 */
	public static boolean isSpaceNotEnough(String dir, long need)
	{
		File file = new File(dir);
		if (!file.exists())
		{
			dir = FileUtil.getParentDir(dir);
			file = new File(dir);
		}
		if (!file.exists())
		{
			file.mkdirs(); // 到底创建不？可能用户就是不需要这个路径了
		}
		else
		{
			if (file.isFile())
			{
				file = file.getParentFile();
			}
		}
		return !file.canWrite() || need >= file.getUsableSpace();
	}

	/**
	 * 根据path后缀找到默认存放路径
	 * 
	 * @param path
	 * @return
	 */
	public static String getDefaultMediaPath(String path)
	{
		return Dirs.getCameraDir(path) + File.separator + FileUtil.getFileName(path);
	}

	public static long getFreeRam(Context context)
	{
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		return mi.availMem;
	}

	public static String getMaxSpaceBoot(Context context)
	{
		reGetCardsPaths(context);
		long space = 0;
		String dir = null;
		ArrayList<String> cardsPaths = getCardsPaths();
		for (String path : cardsPaths)
		{
			long s = new File(path).getUsableSpace();
			if (space < s)
			{
				space = s;
				dir = path;
			}
		}
		return dir;
	}

	public static boolean isOnDisk(String path)
	{
		ArrayList<String> cardsPaths = getCardsPaths();
		for (String boot : cardsPaths)
		{
			if (path.startsWith(boot))
			{
				return true;
			}
		}
		return false;
	}

}
