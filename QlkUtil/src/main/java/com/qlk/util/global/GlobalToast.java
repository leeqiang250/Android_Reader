package com.qlk.util.global;

import com.qlk.util.R;
import com.qlk.util.tool.Util.ScreenUtil;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class GlobalToast
{
	private static Toast toast;
	private static Context baseContext;
	private static int yOffset;

	private static final Handler ToastHandler = new Handler(Looper.getMainLooper())
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (toast == null)
			{
				toast = new Toast(baseContext);
				TextView txt = new TextView(baseContext);
				txt.setGravity(Gravity.CENTER);
//				txt.setBackgroundResource(R.drawable.toast);
				txt.setBackgroundResource(R.drawable.xml_click_toast);
				txt.setTextSize(15.0f);
				txt.setTextColor(baseContext.getResources().getColor(R.color.pink));
				toast.setView(txt);
				yOffset = ScreenUtil.getScreenHeight(baseContext) / 7;
			}
			((TextView) toast.getView()).setText((CharSequence) msg.obj);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setGravity(msg.arg2, 0, msg.arg2 == Gravity.CENTER ? 0 : yOffset);
			toast.show();
		}
	};

	/**
	 * 顺序连续发布多条信息而不会被遮挡
	 * 
	 * @param context
	 * @param prompt
	 */
	public static void toastInQueue(Context context, String prompt)
	{
		Toast toast = new Toast(context);
		TextView txt = new TextView(context);
		txt.setBackgroundResource(R.drawable.toast);
		txt.setTextSize(15.0f);
		txt.setTextColor(context.getResources().getColor(R.color.green));
		txt.setText(prompt);
		toast.setView(txt);
		toast.show();
	}

	/**
	 * 自定义toast信息
	 * 
	 * @param gravity
	 *            屏幕中的位置，例如Gravity.top等
	 */
	public static void toast(Context context, String prompt, int gravity, int duration)
	{
		GlobalToast.baseContext = context.getApplicationContext();

		Message msg = Message.obtain();
		msg.obj = prompt;
		msg.arg1 = duration;
		msg.arg2 = gravity;
		ToastHandler.sendMessage(msg);
	}

	/**
	 * 自定义toast信息 duration = Toast.LENGTH_SHORT
	 * 
	 * @param gravity
	 *            屏幕中的位置，例如Gravity.top等
	 */
	public static void toast(Context context, String prompt, int gravity)
	{
		toast(context, prompt, gravity, Toast.LENGTH_SHORT);
	}

	/**
	 * 显示在屏幕中间
	 * 
	 * @param context
	 * @param prompt
	 */
	public static void toastCenter(Context context, String prompt)
	{
		toast(context, prompt, Gravity.CENTER, Toast.LENGTH_SHORT);
	}

	/**
	 * 显示在屏幕下方1/7处，Duration==Toast.LENGTH_SHORT
	 */
	public static void toastShort(Context context, String prompt)
	{
		toast(context, prompt, Gravity.BOTTOM, Toast.LENGTH_SHORT);
	}

	/**
	 * 显示在屏幕下方1/7处，Duration==Toast.LENGTH_LONG
	 */
	public static void toastLong(Context context, String prompt)
	{
		toast(context, prompt, Gravity.BOTTOM, Toast.LENGTH_LONG);
	}

}
