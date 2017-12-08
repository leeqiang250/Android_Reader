package com.qlk.util.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.qlk.util.global.GlobalDialog;
import com.qlk.util.global.GlobalDialog.DialogInfo;
import com.qlk.util.global.GlobalObserver;
import com.qlk.util.global.GlobalToast;
import com.sz.view.dialog.LoadingBgDialog;
import com.sz.view.dialog.LoadingDialog;

import java.util.Observable;
import java.util.Observer;

/**
 * 默认开启“观察者”模式，不需要可以重写isObserverEnabled()
 *
 * @author QiLiKing 2015-7-29 上午11:35:51
 */
public class BaseActivity extends Activity implements Observer {

    public static final String TAG_WAY = "tag_way";
    public static final String PBB_READER = "pbb_reader"; //Reader
    public static final String PBB_CLIPER = "pbb_cliper"; //pbb

    private LoadingBgDialog loadingBgDlg;
    private Dialog loadingDlg;

    protected enum ExitMode {
        Normal, DoubleClick, Dialog
    }

    /**
     * 全局UI Handler，放在BaseActivity中（而不是BaseApplication），它的子类可以直接使用
     */
    public static final Handler UIHandler = new Handler();
    //	private boolean isFirstLoad = true;
    private long mExitTime;

    protected boolean isObserverEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getWindow() != null)
            getWindow().setBackgroundDrawable(null);        //加快加载速度
        super.onCreate(savedInstanceState);

        BaseApplication.ACTIVITIES.add(this);
        if (isObserverEnabled()) {
            GlobalObserver.getGOb().addObserver(this);
        }
    }

    //	@Override
    //	protected void onResume()
    //	{
    //		super.onResume();
    //		if (isFirstLoad)
    //		{
    //			Runnable task = getDelayLoadTask();
    //			if (task != null)
    //			{
    //				UIHandler.post(task);
    //			}
    //			isFirstLoad = false;
    //		}
    //	}

    /**
     * 设置当Activity退出时的方式：正常退出，双击退出，弹窗提示
     */
    protected ExitMode getExitMode() {
        return ExitMode.Normal;
    }

    /**
     * 当选择ExitMode.Dialog退出时，需要告诉BaseActivity退出框样式
     * <p>
     * 默认是ConfirmDialog
     *
     * @return
     */
    protected Dialog getExitDialog() {
        DialogInfo info = new DialogInfo();
        info.prompt = "要退出本程序吗？";
        info.positiveBtnText = "退出";
        info.positiveTask = new Runnable() {
            @Override
            public void run() {
                ((BaseApplication) getApplication()).safeExit();
            }
        };
        return GlobalDialog.showConfirmDialog(this, info);
    }

    //	/**
    //	 * 这个Task会在onResume中post执行以加快界面显示速度
    //	 * <p>
    //	 * 只有初次创建才会执行
    //	 *
    //	 * @return
    //	 */
    //	protected Runnable getDelayLoadTask()
    //	{
    //		return null;
    //	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.ACTIVITIES.remove(this);
        GlobalObserver.getGOb().deleteObserver(this);
    }

    /**
     * 一般是界面左上角的返回按钮。需要在xml文件中调用onClick="onBackButtonClick"
     *
     * @param v
     */
    public void onBackButtonClick(View v) {
        dismissKeyboard();    //比按物理返回键多一步操作
        onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getExitMode() == ExitMode.DoubleClick) {
                long curTime = System.currentTimeMillis();
                if (curTime - mExitTime > 2000) {
                    GlobalToast.toastShort(this, "再按一次退出程序");
                    mExitTime = curTime;
                } else {
                    ((BaseApplication) getApplication()).safeExit();
                }
            } else if (getExitMode() == ExitMode.Dialog) {
                Dialog dialog = getExitDialog();
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            } else {
                return super.onKeyDown(keyCode, event);
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 写这个是为了不用每次都手动打<br>
     * 这里什么都没有做
     */
    protected void findViewAndSetListeners() {
    }

    /**
     * 写这个是为了不用每次都手动打<br>
     * 这里什么都没有做
     */
    protected void initUI() {
    }

    /**
     * 写这个是为了不用每次都手动打<br>
     * 这里什么都没有做
     */
    protected void refreshUI() {
    }

    /**
     * 可以在子线程中调用此方法
     */
    protected void postRefreshUI() {
        UIHandler.post(new Runnable() {

            @Override
            public void run() {
                refreshUI();
            }
        });
    }

    /**
     * 观察者的回调方法
     */
    @Override
    public void update(Observable observable, Object tag) {

    }

    //	/*-******************************
    //	 * 键盘
    //	 *******************************/
    //
    //	public void showKeyboard()
    //	{
    //		KeyBoardUtil.showKeyboard(this);
    //	}
    //
    //	public void dismissKeyboard()
    //	{
    //		KeyBoardUtil.dismissKeyboard(this);
    //	}

	/*-******************************
     * 键盘
	 *******************************/

    public void showKeyboard() {
        UIHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (getCurrentFocus() != null) {
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(
                            getCurrentFocus(), 0);
                }
            }
        }, 500);
    }

    /**
     * @Description: (我在文本框已经输入文字完毕，请问这键盘怎么才能退出?用下面的方式即可。)
     * @author 李巷阳
     * @date 2016/12/9 11:11
     */
    public void dismissKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * 显示加载loading框
     */
    public void showBgLoading(Activity atc) {
        showBgLoading(atc, null);
    }

    /**
     * 显示加载loading框(黑色背景)
     */
    public LoadingBgDialog showBgLoading(Activity atc, String msg) {
        if (loadingBgDlg == null)
            loadingBgDlg = new LoadingBgDialog(atc, msg);

        if (!loadingBgDlg.isShowing())
            loadingBgDlg.show();

        return loadingBgDlg;
    }

    public void hideBgLoading() {
        if (loadingBgDlg != null) {
            loadingBgDlg.dismiss();
            loadingBgDlg = null;
        }
    }

    /**
     * 显示加载loading框
     */
    public void showLoading(Activity atc) {
        if (loadingDlg == null)
            loadingDlg = new LoadingDialog(atc);

        if (!loadingDlg.isShowing())
            loadingDlg.show();
    }

    public void hideLoading() {
        if (loadingDlg != null) {
            loadingDlg.dismiss();
            loadingDlg = null;
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

}
