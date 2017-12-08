package com.qlk.util.global;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qlk.util.R;
import com.qlk.util.global.GlobalDialog.DialogInfo.DialogSize;
import com.sz.view.dialog.LoadingBgDialog;

/**
 * 自定义弹出框
 * 
 * @author QiLiKing 2015-6-30 下午5:13:03
 */
public class GlobalDialog
{
	public static final String DIALOG_TYPE_NORMAL = "normal";
	public static final String DIALOG_TYPE_NET = "net";

	/**
	 * 和系统的ProgressBar样式一样
	 * 
	 * @param context
	 * @return
	 */
	public static Dialog showProgressBar(Context context)
	{
		Dialog dialog = new Dialog(context, R.style.no_bkg);
		dialog.setContentView(new ProgressBar(context));
		dialog.setCancelable(false);
		dialog.show();
		return dialog;
	}

	/**
	 * 自定义联网样式
	 * 
	 * @param activity
	 * @return
	 */
	public static Dialog showNetProgress(Activity activity)
	{
		View v = activity.getLayoutInflater().inflate(R.layout.dialog_conn_net, null);
		ImageView iv = (ImageView) v.findViewById(R.id.dcn_imv_progress);
		RotateAnimation animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setFillAfter(true);
		animation.setDuration(1000);
		animation.setRepeatMode(Animation.RESTART);
		animation.setRepeatCount(Animation.INFINITE);
		LinearInterpolator lir = new LinearInterpolator();
		animation.setInterpolator(lir);
		iv.startAnimation(animation);
		Dialog dialog = new Dialog(activity, R.style.no_bkg);
		dialog.setContentView(v);
		dialog.setCancelable(false);
		setDialogSize(activity, dialog, DialogSize.Mini);
		dialog.show();
		return dialog;
	}

	// 手动控制弹窗大小
	public static void setDialogSize(Activity activity, Dialog dialog, DialogSize dialogSize)
	{
		LayoutParams params = dialog.getWindow().getAttributes();
		switch (dialogSize)
		{
			case Mini:
				params.width = (int) activity.getResources().getDimension(
						R.dimen.FragmentDialgoWidth);
				params.height = LayoutParams.WRAP_CONTENT;
				break;

			case FullScreen:
				params.height = LayoutParams.MATCH_PARENT;
				params.width = LayoutParams.MATCH_PARENT;
				break;

			case Normal:
				params.height = LayoutParams.WRAP_CONTENT;
				params.width = LayoutParams.WRAP_CONTENT;
				break;

			default:
				break;
		}
	}

	/**
	 * 可以自定义提示内容
	 * 
	 * @param activity
	 * @param prompt
	 * @return
	 */
	public static Dialog showProgressDialog(Activity activity, String prompt)
	{
		View v = activity.getLayoutInflater().inflate(R.layout.dialog_progress, null);
		((TextView) v.findViewById(R.id.dp_txt_prompt)).setText(prompt);
		Dialog progressDialog = new Dialog(activity, R.style.no_frame_small);
		progressDialog.setContentView(v);
		progressDialog.setCancelable(false);
		setDialogSize(activity, progressDialog, DialogSize.Mini);
		progressDialog.show();

		return progressDialog;
	}

	/**
	 * 让用户选择（三个按钮）
	 */
	public static Dialog showThreeBtnDialog(Activity activity, final DialogInfo info)
	{
		View v = activity.getLayoutInflater().inflate(R.layout.dialog_three_button, null);
		((TextView) v.findViewById(R.id.dtb_txt_title)).setText(info.title);
		Button btnNegative = (Button) v.findViewById(R.id.dtb_btn_negative);
		btnNegative.setText(info.negativeBtnText);
		Button btnPositive = (Button) v.findViewById(R.id.dtb_btn_positive);
		btnPositive.setText(info.positiveBtnText);
		Button btnNormal = (Button) v.findViewById(R.id.dtb_btn_normal);
		btnNormal.setText(info.normalBtnText);
		TextView txtPrompt = ((TextView) v.findViewById(R.id.dtb_txt_prompt));
		txtPrompt.setText(info.prompt);
		if (info.promptView != null)
		{
			txtPrompt.setVisibility(View.GONE);
			((FrameLayout) v.findViewById(R.id.dtb_lyt_content)).addView(info.promptView);
		}

		final Dialog threeBtnDialog = new Dialog(activity, R.style.no_frame_small);
		threeBtnDialog.setContentView(v);
		threeBtnDialog.setCancelable(info.cancelable);
		setDialogSize(activity, threeBtnDialog, info.dialogSize == null ? DialogSize.Mini
				: info.dialogSize);
		threeBtnDialog.show();

		btnPositive.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				threeBtnDialog.dismiss();
				info.executePositiveTask();
			}
		});
		btnNegative.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				threeBtnDialog.dismiss();
				info.executeNegativeTask();
			}
		});
		btnNormal.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				threeBtnDialog.dismiss();
				info.executeNormalTask();
			}
		});

		return threeBtnDialog;
	}

	/**
	 * 让用户选择（两个按钮）
	 */
	public static Dialog showConfirmDialog(Activity activity, final DialogInfo info)
	{
		View v = activity.getLayoutInflater().inflate(R.layout.dialog_three_button, null);
		((TextView) v.findViewById(R.id.dtb_txt_title)).setText(info.title);
		v.findViewById(R.id.dtb_imv_divider2).setVisibility(View.GONE);
		v.findViewById(R.id.dtb_btn_normal).setVisibility(View.GONE);
		Button btnNegative = (Button) v.findViewById(R.id.dtb_btn_negative);
		btnNegative.setText(info.negativeBtnText);
		Button btnPositive = (Button) v.findViewById(R.id.dtb_btn_positive);
		btnPositive.setText(info.positiveBtnText);
		TextView txtPrompt = ((TextView) v.findViewById(R.id.dtb_txt_prompt));
		txtPrompt.setText(info.prompt);
		if (info.promptView != null)
		{
			txtPrompt.setVisibility(View.GONE);
			((FrameLayout) v.findViewById(R.id.dtb_lyt_content)).addView(info.promptView);
		}

		final Dialog confirmDialog = new Dialog(activity, R.style.no_frame_small);
		confirmDialog.setContentView(v);
		confirmDialog.setCancelable(info.cancelable);
		setDialogSize(activity, confirmDialog, info.dialogSize == null ? DialogSize.Mini
				: info.dialogSize);
		confirmDialog.show();

		btnPositive.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				confirmDialog.dismiss();
				info.executePositiveTask();
			}
		});
		btnNegative.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				confirmDialog.dismiss();
				info.executeNegativeTask();
			}
		});

		return confirmDialog;
	}

	/**
	 * 类似于Toast，不过需要用户点击方可消失
	 */
	public static Dialog showNotifyDialog(Activity activity, final DialogInfo info)
	{
		View v = activity.getLayoutInflater().inflate(R.layout.dialog_three_button, null);
		((TextView) v.findViewById(R.id.dtb_txt_title)).setText(info.title);
		v.findViewById(R.id.dtb_imv_divider1).setVisibility(View.GONE);
		v.findViewById(R.id.dtb_imv_divider2).setVisibility(View.GONE);
		v.findViewById(R.id.dtb_btn_normal).setVisibility(View.GONE);
		v.findViewById(R.id.dtb_btn_negative).setVisibility(View.GONE);
		Button btnPositive = (Button) v.findViewById(R.id.dtb_btn_positive);
		btnPositive.setText(info.positiveBtnText);
		TextView txtPrompt = ((TextView) v.findViewById(R.id.dtb_txt_prompt));
		txtPrompt.setText(info.prompt);
		if (info.promptView != null)
		{
			txtPrompt.setVisibility(View.GONE);
			((FrameLayout) v.findViewById(R.id.dtb_lyt_content)).addView(info.promptView);
		}

		final Dialog notifyDialog = new Dialog(activity, R.style.no_frame_small);
		notifyDialog.setContentView(v);
		notifyDialog.setCancelable(info.cancelable);
		setDialogSize(activity, notifyDialog, info.dialogSize == null ? DialogSize.Mini
				: info.dialogSize);
		notifyDialog.show();

		btnPositive.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				info.executePositiveTask();
				notifyDialog.dismiss();
			}
		});

		return notifyDialog;
	}

	/**
	 * 自定义界面的Dialog
	 */
	public static Dialog showSelfDialog(Activity activity, View v, DialogSize size)
	{
		final Dialog menuDialog = new Dialog(activity, R.style.no_frame_small);
		menuDialog.setContentView(v);
		setDialogSize(activity, menuDialog, size);
		menuDialog.show();

		return menuDialog;
	}

	/**
	 * 都有各自的默认信息，不赋值也可运行
	 * 
	 * @author QiLiKing 2015-6-30 下午5:15:02
	 */
	public static final class DialogInfo
	{
		public enum DialogSize
		{
			Normal, Mini, FullScreen
		}

		public String title = "提示信息";
		public String prompt = "";
		public String negativeBtnText = "取消";
		public String positiveBtnText = "确定";
		public String normalBtnText = "正常";
		public Runnable negativeTask;
		public Runnable positiveTask;
		public Runnable normalTask;
		public DialogSize dialogSize;
		public boolean cancelable = true;
		public View promptView;

		public void executeNegativeTask()
		{
			if (negativeTask != null)
			{
				negativeTask.run();
			}
		}

		public void executePositiveTask()
		{
			if (positiveTask != null)
			{
				positiveTask.run();
			}
		}

		public void executeNormalTask()
		{
			if (normalTask != null)
			{
				normalTask.run();
			}
		}
	}
	private static Dialog loadingBgDlg;
	/**
	 * 显示加载loading框
	 */
	public static Dialog showBgLoading(Activity atc) {
		return showBgLoading(atc, null);
	}
	/**
	 * 显示加载loading框(黑色背景)
	 */
	public static Dialog showBgLoading(Activity atc, String msg) {
		if (loadingBgDlg == null)
			loadingBgDlg = new LoadingBgDialog(atc, msg);

		if (!loadingBgDlg.isShowing())
			loadingBgDlg.show();

		return loadingBgDlg;
	}

	public static void hideBgLoading() {
		if (loadingBgDlg != null) {
			loadingBgDlg.dismiss();
			loadingBgDlg = null;
		}
	}



}
