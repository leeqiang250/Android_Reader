package com.sz.mobilesdk.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.WindowManager;

import com.sz.mobilesdk.common.SZApplication;

/**
 * CommonUtil常用工具
 * 
 * @author hudq
 * 
 */
public class CommonUtil
{

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * sd卡是否可用
	 * 
	 * @return
	 */
	public static boolean isSdCardCanUsed()
	{
		try
		{
			return Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 网络是否连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConnect(Context context)
	{
		// 网络状态发生改变了，这时检测网络状态
		// 包括，可用，不可用，wifi，3g，wap，net
		// 拿到网络连接的管理器
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 得到所有的活动的网络信息
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null)
		{
			return info.isConnected() && info.isAvailable();
		} else
		{
			// 没有可用网络
			return false;
		}
	}

	/**
	 * 判断当前网络是否是wifi网络
	 * if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE) { //判断3G网
	 * 
	 * @return boolean
	 */
	public static boolean isWifi(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) { return true; }
		return false;
	}

	/**
	 * 判断当前网络是否是手机网络
	 * 
	 * @return boolean
	 */
	public static boolean isMobileNet(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) { return true; }
		return false;
	}

	/*
	 * NETWORK_TYPE_CDMA 网络类型为CDMA NETWORK_TYPE_EDGE 网络类型为EDGE
	 * NETWORK_TYPE_EVDO_0 网络类型为EVDO0 NETWORK_TYPE_EVDO_A 网络类型为EVDOA
	 * NETWORK_TYPE_GPRS 网络类型为GPRS NETWORK_TYPE_HSDPA 网络类型为HSDPA
	 * NETWORK_TYPE_HSPA 网络类型为HSPA NETWORK_TYPE_HSUPA 网络类型为HSUPA
	 * NETWORK_TYPE_UMTS 网络类型为UMTS
	 * 
	 * 联通3G为UMTS或HSDPA，移动和联通2G为GPRS或EGDE，电信的2G为CDMA，电信的3G为EVDO
	 */
	/**
	 * 手机网络类型,
	 * 
	 * @param context
	 * @return
	 */
	public static String getNetStateType(Context context)
	{
		ConnectivityManager conMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conMan.getActiveNetworkInfo();
		if (info != null)
		{
			if (info.getType() == ConnectivityManager.TYPE_MOBILE)
			{
				switch (info.getSubtype())
				{
					case TelephonyManager.NETWORK_TYPE_GPRS:
					case TelephonyManager.NETWORK_TYPE_EDGE:
					case TelephonyManager.NETWORK_TYPE_CDMA:
					case TelephonyManager.NETWORK_TYPE_1xRTT:
					case TelephonyManager.NETWORK_TYPE_IDEN:
						return "2G";

					case TelephonyManager.NETWORK_TYPE_UMTS:
					case TelephonyManager.NETWORK_TYPE_EVDO_0:
					case TelephonyManager.NETWORK_TYPE_EVDO_A:
					case TelephonyManager.NETWORK_TYPE_HSDPA:
					case TelephonyManager.NETWORK_TYPE_HSUPA:
					case TelephonyManager.NETWORK_TYPE_HSPA:
					case TelephonyManager.NETWORK_TYPE_EVDO_B:
					case TelephonyManager.NETWORK_TYPE_EHRPD:
					case TelephonyManager.NETWORK_TYPE_HSPAP:
						return "3G";

					case TelephonyManager.NETWORK_TYPE_LTE:
						return "4G";
					default:
						return "UNKNOWN";
				}
			} else if (info.getType() == ConnectivityManager.TYPE_WIFI) { return "WIFI"; }
		}
		return "NONE";
	}

	public static boolean is2G(Context context)
	{
		return "2G".equals(getNetStateType(context));
	}

	public static boolean is3G(Context context)
	{
		return "3G".equals(getNetStateType(context));
	}

	public static boolean is4G(Context context)
	{
		return "4G".equals(getNetStateType(context));
	}

	/**
	 * 判断服务是否开启
	 * 
	 * @param mContext
	 * @param classServiceName
	 *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
	 * @return
	 */
	public static boolean isServiceRunning(Context mContext,
			String classServiceName)
	{
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);
		if (!(serviceList.size() > 0)) { return false; }
		for (int i = 0; i < serviceList.size(); i++)
		{
			String mName = serviceList.get(i).service.getClassName();
			if (mName != null && mName.equals(classServiceName))
			{
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	/**
	 * 
	 * 判断packageName是否存在
	 * 
	 * @param ctx
	 * @param packageName
	 * @return
	 */
	public static boolean existPackage(final Context ctx, String packageName)
	{
		if (!TextUtils.isEmpty(packageName))
		{
			for (PackageInfo p : ctx.getPackageManager()
					.getInstalledPackages(0))
			{
				if (packageName.equals(p.packageName)) return true;
			}
		}
		return false;
	}

	/**
	 * 获取本地apk的图标 <br/>
	 * 
	 * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过 appInfo.publicSourceDir =
	 * apkPath;来修正这个问题，详情参见:
	 * http://code.google.com/p/android/issues/detail?id=9151
	 */
	public static Drawable getApkIcon(Context context, String apkPath)
	{
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		if (info != null)
		{
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = apkPath;
			appInfo.publicSourceDir = apkPath;
			try
			{
				return appInfo.loadIcon(pm);
			} catch (OutOfMemoryError e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	/***
	 * App是否前台运行
	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean isAppForward(Context ctx)
	{
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(10);
		if (list == null) return false;

		ComponentName name = list.get(0).topActivity;
		SZLog.v("PackageName", name.getPackageName());
		return TextUtils.equals(ctx.getPackageName(), name.getPackageName());
	}

	/**
	 * App是否前台运行-2
	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean isAppForward2(Context ctx)
	{
		ActivityManager activityManager = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null) return false;

		for (RunningAppProcessInfo appProcess : appProcesses)
		{
			if (appProcess.processName.equals(ctx.getPackageName()))
			{
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND)
				{
					Log.v("Background", appProcess.processName);
					return false;
				} else
				{
					Log.v("Forward", appProcess.processName);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 用于判断当前是否处于锁屏状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isAppLocked(Context context)
	{
		KeyguardManager keyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		boolean isLockedState = keyguardManager.inKeyguardRestrictedInputMode();// 用于判断当前是否处于锁屏状态。
		return isLockedState;
	}

	/**
	 * App是否正在运行
	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean isAppRunning(Context ctx)
	{
		boolean isAppRunning = false;
		final String appPakage = ctx.getPackageName();
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(30);
		if (list == null) return false;
		
		for (RunningTaskInfo info : list)
		{
			if (info.topActivity.getPackageName().equals(appPakage)
					&& info.baseActivity.getPackageName().equals(appPakage))
			{
				isAppRunning = true;
				break;
			}
		}
		return isAppRunning;
	}

	/**
	 * 获取App安装包信息
	 * 
	 * @return
	 */
	public static PackageInfo getPackageInfo()
	{
		PackageInfo info = null;
		try
		{
			Context context = SZApplication.getInstance();
			info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace(System.err);
		}
		if (info == null) info = new PackageInfo();
		return info;
	}

	/**
	 * 获取此应用App的版本名
	 * 
	 * @param context
	 */
	public static String getAppVersionName(Context context)
	{
		return getVersionName(context, context.getPackageName());
	}

	/**
	 * 获取packname应用程序的版本名称
	 * 
	 * @param context
	 *            上下文
	 * @param packname
	 *            包名
	 * @return 版本号，String
	 */
	public static String getVersionName(Context context, String packname)
	{
		try
		{
			PackageManager pm = context.getPackageManager();
			// 0代表是获取版本信息
			PackageInfo info = pm.getPackageInfo(packname, 0);
			return info.versionName;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 获取packname应用程序的版本码
	 * 
	 * @param context
	 *            上下文
	 * @param packname
	 *            包名
	 * @return 版本码，int型
	 */
	public static int getAppVersionCode(Context context, String packname)
	{
		try
		{
			PackageManager pm = context.getPackageManager();
			// 0代表是获取版本信息
			PackageInfo info = pm.getPackageInfo(packname, 0);
			return info.versionCode;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
			return 1;
		}
	}

	/**
	 * 获取本地邮箱账户：权限GET_ACCOUNTS
	 * 
	 * @param context
	 * @return
	 */
	public static String getEmailAccount(Context context)
	{
		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(context).getAccounts();

		for (Account account : accounts)
		{
			if (emailPattern.matcher(account.name).matches())
			{
				String accountName = account.name;
				return accountName;
			}
			return null;
		}
		return null;
	}

	/**
	 * 实现文本复制功能
	 * 
	 * @param context
	 * @param copyStr
	 */
	public static void copyText(Context context, String copyStr)
	{
		android.content.ClipboardManager cm = (android.content.ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("clip_data", copyStr);
		cm.setPrimaryClip(clip);
	}

	/**
	 * 是否全屏
	 * 
	 * @param full
	 * @param ctx
	 */
	public static void fullScreen(boolean full, Activity ctx)
	{
		if (full)
		{
			WindowManager.LayoutParams lp = ctx.getWindow().getAttributes();
			lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			ctx.getWindow().setAttributes(lp);
			ctx.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		} else
		{
			WindowManager.LayoutParams attr = ctx.getWindow().getAttributes();
			// 取消全屏
			attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			ctx.getWindow().setAttributes(attr);
			ctx.getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}

	/**
	 * 获得状态栏的高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getStatusHeight(Context context)
	{
		int statusHeight = 0;
		try
		{
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object object = clazz.newInstance();
			Field field = clazz.getField("status_bar_height");
			int height = Integer.parseInt(field.get(object).toString());
			statusHeight = context.getResources().getDimensionPixelSize(height);
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (InstantiationException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (NumberFormatException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		return statusHeight;
	}


	/**
	 * 判断qq是否可用
	 */
	public static boolean isQQClientAvailable(Context context) {
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn != null && ("com.tencent.mobileqq").equals(pn)) {
					return true;
				}
			}
		}
		return false;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	private static long lastClickTime = 0L;

	/**
	 * 防止重复点击
	 * 
	 * @param millsSeconds
	 * 
	 * @return
	 */

	public static boolean isFastDoubleClick(int millsSeconds)
	{
		long current = System.currentTimeMillis();
		long gap = current - lastClickTime;
		if (millsSeconds < 500)
		{
			millsSeconds = 500;
		}
		if (gap > 0 && gap < millsSeconds) 
		{
			return true;
		}
		lastClickTime = current;
		return false;
	}

}
