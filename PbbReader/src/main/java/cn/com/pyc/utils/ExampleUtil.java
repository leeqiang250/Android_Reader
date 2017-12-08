package cn.com.pyc.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.pyc.global.PbbSP;
import cn.com.pyc.pbb.reader.R;

import com.qlk.util.tool.Util.ScreenUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

public class ExampleUtil
{
	public static final String PREFS_NAME = "JPUSH_EXAMPLE";
	public static final String PREFS_DAYS = "JPUSH_EXAMPLE_DAYS";
	public static final String PREFS_START_TIME = "PREFS_START_TIME";
	public static final String PREFS_END_TIME = "PREFS_END_TIME";
	public static final String KEY_APP_KEY = "JPUSH_APPKEY";

	public static boolean isEmpty(String s)
	{
		if (null == s)
			return true;
		if (s.length() == 0)
			return true;
		if (s.trim().length() == 0)
			return true;
		return false;
	}

	// 校验Tag Alias 只能是数�?英文字母和中�?
	public static boolean isValidTagAndAlias(String s)
	{
		Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
		Matcher m = p.matcher(s);
		return m.matches();
	}

	// 取得AppKey
	public static String getAppKey(Context context)
	{
		Bundle metaData = null;
		String appKey = null;
		try
		{
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			if (null != ai)
				metaData = ai.metaData;
			if (null != metaData)
			{
				appKey = metaData.getString(KEY_APP_KEY);
				if ((null == appKey) || appKey.length() != 24)
				{
					appKey = null;
				}
			}
		}
		catch (NameNotFoundException e)
		{

		}
		return appKey;
	}

	// 取得版本�?
	public static String GetVersion(Context context)
	{
		try
		{
			PackageInfo manager = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return manager.versionName;
		}
		catch (NameNotFoundException e)
		{
			return "Unknown";
		}
	}

	public static void showToast(final String toast, final Context context)
	{
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
				Looper.loop();
			}
		}).start();
	}

	public static boolean isConnected(Context context)
	{
		ConnectivityManager conn = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conn.getActiveNetworkInfo();
		return (info != null && info.isConnected());
	}

	public static String getImei(Context context, String imei)
	{
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imei = telephonyManager.getDeviceId();
		}
		catch (Exception e)
		{
			Log.e(ExampleUtil.class.getSimpleName(), e.getMessage());
		}
		return imei;
	}

	/**
	 * 显示覆盖蒙板
	 * 
	 * @param context
	 */
	@Deprecated
	public static void showGuide(final Activity context)
	{
		if (!((Boolean) PbbSP.getGSP(context).getValue(PbbSP.SP_GUIDE_CODE_CLICK, false)))
		{
			final ImageView view = new ImageView(context);
			view.setBackgroundResource(R.drawable.reader_main_guide);
			final Dialog dialog = new Dialog(context, R.style.no_bkg_pyc);
			dialog.setContentView(view);
			LayoutParams lay = dialog.getWindow().getAttributes();
			lay.width = ScreenUtil.getScreenWidth(context);
			lay.height = ScreenUtil.getScreenHeight(context);
			dialog.show();
			view.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
					PbbSP.getGSP(context).putValue(PbbSP.SP_GUIDE_CODE_CLICK, true);
					view.setBackgroundDrawable(null);
				}
			});
		}
	}

	/**
	 * 显示覆盖蒙板,点击图标消失
	 * 
	 * @param context
	 */
	@Deprecated
	public static void showGuide2(final Activity context)
	{
		if (!((Boolean) PbbSP.getGSP(context).getValue(PbbSP.SP_GUIDE_CODE_CLICK, false)))
		{
			final View guidePopView = context.getLayoutInflater().inflate(R.layout.xml_pop_guide,
					null);

			final Dialog dialog = new Dialog(context, R.style.no_bkg_pyc);

			dialog.setContentView(guidePopView);
			android.view.WindowManager.LayoutParams lay = dialog.getWindow().getAttributes();
			lay.width = ScreenUtil.getScreenWidth(context);
			lay.height = ScreenUtil.getScreenHeight(context);
			dialog.show();
			final ImageView imvtip1 = (ImageView) guidePopView.findViewById(R.id.imv_tip1);
			final ImageView imvtip2 = (ImageView) guidePopView.findViewById(R.id.imv_tip2);

			imvtip1.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View arg0)
				{
					if (imvtip2.getVisibility() == 0)
					{
						imvtip1.setVisibility(View.GONE);
					}
					else
					{
						imvtip1.setVisibility(View.GONE);
						dialog.dismiss();
						PbbSP.getGSP(context).putValue(PbbSP.SP_GUIDE_CODE_CLICK, true);
					}
				}
			});

			imvtip2.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View arg0)
				{
					if (imvtip1.getVisibility() == 0)
					{
						imvtip2.setVisibility(View.GONE);
					}
					else
					{
						imvtip2.setVisibility(View.GONE);
						dialog.dismiss();
						PbbSP.getGSP(context).putValue(PbbSP.SP_GUIDE_CODE_CLICK, true);
					}
				}
			});
		}
	}
}
