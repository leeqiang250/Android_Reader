package cn.com.pyc.base;

import cn.jpush.android.api.JPushInterface;

import com.sz.view.dialog.LoadingBgDialog;
import com.sz.view.dialog.LoadingDialog;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.Dialog;
import android.widget.Toast;

public class BaseActivityGroup extends ActivityGroup
{
	//彭宝宝tabhost
	protected static final int TAB_1 = 0;
	protected static final int TAB_2 = 1;
	protected static final int TAB_3 = 2;

	private Dialog loadingBgDlg;
	private Dialog loadingDlg;

	@Override
	protected void onPause()
	{
		if (getParent() == null)
			JPushInterface.onPause(this);
		super.onPause();

	}

	@Override
	protected void onResume()
	{
		if (getParent() == null)
			JPushInterface.onResume(this);
		super.onResume();
	}

	public void showToast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	public void showToast(int resId)
	{
		Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 显示加载loading框
	 */
	public void showBgLoading(Activity atc)
	{
		showBgLoading(atc, null);
	}

	/**
	 * 显示加载loading框(黑色背景)
	 */
	public void showBgLoading(Activity atc, String msg)
	{
		if (loadingBgDlg == null)
			loadingBgDlg = new LoadingBgDialog(atc, msg);
		loadingBgDlg.show();
	}

	public void hideBgLoading()
	{
		if (loadingBgDlg != null)
		{
			loadingBgDlg.dismiss();
			loadingBgDlg = null;
		}
	}

	/**
	 * 显示加载loading框
	 */
	public void showLoading(Activity atc)
	{
		if (loadingDlg == null)
			loadingDlg = new LoadingDialog(atc);
		loadingDlg.show();
	}

	public void hideLoading()
	{
		if (loadingDlg != null)
		{
			loadingDlg.dismiss();
			loadingDlg = null;
		}
	}
}
