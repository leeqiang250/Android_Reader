package cn.com.pyc.base;

import android.app.Activity;
import android.app.Dialog;

import com.qlk.util.base.BaseFragment;
import com.sz.view.dialog.LoadingBgDialog;
import com.sz.view.dialog.LoadingDialog;

public class PbbBaseFragment extends BaseFragment
{
	private LoadingBgDialog loadingBgDlg;
	private Dialog loadingDlg;
	
	/**
//	 * 显示加载loading框
//	 */
	public void showBgLoading(Activity atc)
	{
		showBgLoading(atc, null);
	}

	/**
	 * 显示加载loading框(黑色背景)
	 */
	public LoadingBgDialog showBgLoading(Activity atc, String msg)
	{
		if (loadingBgDlg == null)
			loadingBgDlg = new LoadingBgDialog(atc, msg);

		if (!loadingBgDlg.isShowing())
			loadingBgDlg.show();
		
		return loadingBgDlg;
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

		if (!loadingDlg.isShowing())
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
