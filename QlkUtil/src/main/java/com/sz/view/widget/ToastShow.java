package com.sz.view.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qlk.util.R;
import com.sz.mobilesdk.common.Constant;

public class ToastShow
{

	private TextView message;
	private ImageView toastIcon;
	private Toast toastStart = null;
	// 失败
	public static final int IMG_FAIL = 0;
	// 成功
	public static final int IMG_OK = 1;
	// 繁忙
	public static final int IMG_BUSY = 2;

	public static ToastShow getToast()
	{
		return ToastShowInner.instance_;
	}

	private static class ToastShowInner
	{
		private static final ToastShow instance_ = new ToastShow();
	}

	private ToastShow()
	{
	}

	/**
	 * fail状态icon的Toast
	 * 
	 * @param context
	 * @param str
	 */
	public void showFail(Context context, String str)
	{
		show(context, IMG_FAIL, str, Gravity.BOTTOM);
	}

	/**
	 * ok状态icon的Toast
	 * 
	 * @param context
	 * @param str
	 */
	public void showOk(Context context, String str)
	{
		show(context, IMG_OK, str, Gravity.BOTTOM);
	}

	/**
	 * busy状态icon的Toast
	 * 
	 * @param context
	 * @param str
	 */
	public void showBusy(Context context, String str)
	{
		show(context, IMG_BUSY, str, Gravity.BOTTOM);
	}

	/**
	 * 自定义toast显示，位置bottom
	 * 
	 * @param context
	 * @param toastIconTag
	 * @param str
	 */
	public void show(Context context, int toastIconTag, String str)
	{
		show(context, toastIconTag, str, Gravity.BOTTOM);
	}

	/**
	 * 自定义toast显示
	 * 
	 * @param context
	 * @param toastIconTag
	 * @param str
	 * @param gravity
	 */
	public void show(Context context, int toastIconTag, String str, int gravity)
	{
		int yOffset = (Gravity.CENTER == gravity) ? 0 : 220;
		int duration = (Gravity.CENTER == gravity) ? Toast.LENGTH_LONG
				: Toast.LENGTH_SHORT;
		if (toastStart == null)
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			View toastRoot = inflater.inflate(R.layout.sz_toast_show, null);
			message = (TextView) toastRoot.findViewById(R.id.toast_message);
			toastIcon = (ImageView) toastRoot.findViewById(R.id.toast_icon);
			if (Constant.screenWidth != 0)
				toastRoot.setMinimumWidth((int) (Constant.screenWidth * 0.52));
			message.setText(str);
			toastStart = new Toast(context);
			toastStart.setView(toastRoot);
		} else
		{
			message.setText(str);
		}
		toastStart.setGravity(gravity, 0, yOffset);
		toastStart.setDuration(duration);
		switch (toastIconTag)
		{
			case IMG_FAIL:// 失败
				toastIcon.setImageResource(R.drawable.toast_fail);
				break;
			case IMG_OK:// 成功
				toastIcon.setImageResource(R.drawable.toast_success);
				break;
			case IMG_BUSY:// 繁忙警告
				toastIcon.setImageResource(R.drawable.toast_busy);
				break;
			default:// 其它的
				break;
		}
		toastStart.show();
	}

	public void cancel()
	{
		if (toastStart != null)
		{
			toastStart.cancel();
		}
	}
}
