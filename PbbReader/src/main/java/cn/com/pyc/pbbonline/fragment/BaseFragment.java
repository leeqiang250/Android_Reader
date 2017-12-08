package cn.com.pyc.pbbonline.fragment;

import cn.com.pyc.pbbonline.common.Code;

import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.dialog.LoadingBgDialog;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Created by simaben on 2014/5/29.
 */
public abstract class BaseFragment extends Fragment
{
	private LoadingBgDialog loadingBgDlg;

	/**
	 * 显示加载loading框(黑色背景)
	 */
	public LoadingBgDialog showBgLoading(Activity atc, String msg)
	{
		if (loadingBgDlg == null)
			loadingBgDlg = new LoadingBgDialog(atc, msg);
		loadingBgDlg.show();

		return loadingBgDlg;
	}
	/**
	 * 关闭加载的loading框
	 */
	public void hideBgLoading()
	{
		if (loadingBgDlg != null)
		{
			loadingBgDlg.dismiss();
			loadingBgDlg = null;
		}
	}
	/**
	 * 登陆失败，根据cade提示错误信息。
	 * @param code
	 */
	protected void setFailCode(String code)
	{
		switch (code)
		{
			case Code._9110:
				UIHelper.showToast(getActivity().getApplicationContext(), "参数传递错误");
				break;
			case Code._9101:
				UIHelper.showToast(getActivity().getApplicationContext(), "密码错误");
				break;
			case Code._9102:
				UIHelper.showToast(getActivity().getApplicationContext(), "用户未注册");
				break;
			case Code._9107:
				UIHelper.showToast(getActivity().getApplicationContext(), "不能重复绑定");
				break;
			default:
				break;
		}
	}
}
