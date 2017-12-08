package com.qlk.util.base;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.sz.view.dialog.LoadingBgDialog;
import com.sz.view.dialog.LoadingDialog;

public class BaseFragmentActivity extends FragmentActivity {

    private Dialog loadingBgDlg;
    private Dialog loadingDialog;


    public void showToast(int resId) {
        showToast(getString(resId));
    }

    public void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 通过类名启动Activity
     *
     * @param clz
     */
    public void openActivity(Class<?> clz) {
        openActivity(clz, null);
    }

    /**
     * 通过类名启动Activity，并且含有Bundle数据
     *
     * @param clz
     * @param bundle 传递的bundle数据
     */
    public void openActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 显示加载loading框
     */
    public void showBgLoading(String msg) {
        if (loadingBgDlg == null) {
            loadingBgDlg = new LoadingBgDialog(this, msg);
            loadingBgDlg.setCancelable(true); // true设置返回取消
            loadingBgDlg.setCanceledOnTouchOutside(false);
        }
        if (!loadingBgDlg.isShowing()) {
            loadingBgDlg.show();
        }
    }

    /**
     * 显示加载loading框,带背景
     */
    public void showBgLoading() {
        showBgLoading(null);
    }

    /**
     * 隐藏加载loading，带背景
     */
    public void hideBgLoading() {
        if (loadingBgDlg != null) {
            loadingBgDlg.dismiss();
            loadingBgDlg = null;
        }
    }

    /**
     * 显示加载进度框，不带背景
     */
    public void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.setCancelable(true); // true设置返回取消
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    /**
     * 隐藏加载进度框，不带背景
     */
    public void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
    
}
