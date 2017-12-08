package com.sz.mobilesdk.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qlk.util.R;
import com.sz.mobilesdk.manager.SystemBarTintManager;

public class UIHelper {

    /**
     * 显示自定义颜色状态栏 ,必须在setContentView之后调用
     *
     * @param activity
     * @param colorId  颜色资源id
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void showTintStatusBar(Activity activity, int colorId) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
            if (activity.getWindow() == null) return;
            // 透明状态栏
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            ViewGroup mRootView = (ViewGroup) activity
                    .findViewById(android.R.id.content);
            mRootView = (ViewGroup) mRootView.getChildAt(0);
            mRootView.setFitsSystemWindows(true);
            mRootView.setClipToPadding(true);

            SystemBarTintManager tintManager = new SystemBarTintManager(
                    activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(false);
            tintManager.setStatusBarTintColor(colorId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示对话框（自定义标题、内容、确定按钮的文本显示）
     *
     * @param context
     * @param titleText   标题，默认文本“提示”
     * @param contetnText 内容
     * @param posBtnText  确定按钮文本，默认“确定”
     * @param callBack
     */
    public static final Dialog showCommonDialog(Context context,
                                                String titleText, String contetnText,
                                                String posBtnText,
                                                final DialogCallBack callBack) {
        final Dialog dialog = new Dialog(context, R.style.SZ_LoadBgDialog);
        View view = View.inflate(context, R.layout.sz_dialog_common, null);
        Button confirm = (Button) view
                .findViewById(R.id.dialog_common_btn_positive);// 确定
        Button cancel = (Button) view
                .findViewById(R.id.dialog_common_btn_negative);// 取消
        TextView content = (TextView) view
                .findViewById(R.id.dialog_common_content);// 显示内容
        TextView title = (TextView) view.findViewById(R.id.dialog_common_title);// 显示标题

        // 设置对话框的宽度
        if (title.getLayoutParams() != null) {
            int width = DeviceUtil.getScreenSize(context).x;
            title.getLayoutParams().width = (int) (width * 0.73);
        }

        // 标题
        if (!TextUtils.isEmpty(titleText)) {
            title.setText(titleText);
        }
        // 内容
        if (!TextUtils.isEmpty(contetnText)) {
            content.setText(contetnText);
        }
        // 确定按钮
        if (!TextUtils.isEmpty(posBtnText)) {
            confirm.setText(posBtnText);
        }

        dialog.setContentView(view);

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onConfirm();
                }
                if (dialog != null) dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        return dialog;
    }

    public static final Dialog showCommonDialog2(Context context,
                                                 String titleText, String contetnText,
                                                 String posBtnText, String negBtnText,
                                                 final DialogCallBackPat callBack) {
        final Dialog dialog = new Dialog(context, R.style.SZ_LoadBgDialog);
        View view = View.inflate(context, R.layout.sz_dialog_common, null);
        Button confirm = (Button) view
                .findViewById(R.id.dialog_common_btn_positive);// 确定
        Button cancel = (Button) view
                .findViewById(R.id.dialog_common_btn_negative);// 取消
        TextView content = (TextView) view
                .findViewById(R.id.dialog_common_content);// 显示内容
        TextView title = (TextView) view.findViewById(R.id.dialog_common_title);// 显示标题

        // 设置对话框的宽度
        if (title.getLayoutParams() != null) {
            int width = DeviceUtil.getScreenSize(context).x;
            title.getLayoutParams().width = (int) (width * 0.73);
        }

        // 标题
        if (!TextUtils.isEmpty(titleText)) {
            title.setText(titleText);
        }
        // 内容
        if (!TextUtils.isEmpty(contetnText)) {
            content.setText(contetnText);
        }
        // 确定按钮
        if (!TextUtils.isEmpty(posBtnText)) {
            confirm.setText(posBtnText);
        }
        //取消按钮
        if (!TextUtils.isEmpty(negBtnText)) {
            cancel.setText(posBtnText);
        }

        dialog.setContentView(view);

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onConfirm();
                }
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onCancel();
                }
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        return dialog;
    }

    /**
     * 一个按钮的dialog
     *
     * @param context
     * @param titleText
     * @param contetnText
     * @param posBtnText
     * @param callBack
     * @return
     */
    public static final Dialog showSingleCommonDialog(Context context,
                                                      String titleText, String contetnText,
                                                      String posBtnText,
                                                      final DialogCallBack callBack) {
        final Dialog dialog = new Dialog(context, R.style.SZ_LoadBgDialog);
        View view = View.inflate(context, R.layout.sz_dialog_common_single,
                null);
        Button confirm = (Button) view
                .findViewById(R.id.dialog_common_btn_positive);// 确定
        TextView content = (TextView) view
                .findViewById(R.id.dialog_common_content);// 显示内容
        TextView title = (TextView) view.findViewById(R.id.dialog_common_title);// 显示标题

        // 设置对话框的宽度
        if (title.getLayoutParams() != null) {
            int width = DeviceUtil.getScreenSize(context).x;
            title.getLayoutParams().width = (int) (width * 0.73);
        }

        // 标题
        if (!TextUtils.isEmpty(titleText)) {
            title.setText(titleText);
        }
        // 内容
        if (!TextUtils.isEmpty(contetnText)) {
            content.setText(contetnText);
        }
        // 确定按钮
        if (!TextUtils.isEmpty(posBtnText)) {
            confirm.setText(posBtnText);
        }

        dialog.setContentView(view);

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onConfirm();
                }
                if (dialog != null) dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        return dialog;
    }

    /**
     * 1.DialogCallBack <br/>
     * <p>
     * 2.DialogCallBackPat <br/>
     */
    public interface BaseDialogCallBack {
    }

    public interface DialogCallBack extends BaseDialogCallBack {
        void onConfirm();
    }

    public interface DialogCallBackPat extends BaseDialogCallBack {
        void onConfirm();

        void onCancel();
    }

    private static long mLastClickBackTime = 0;

    /**
     * 显示退出提示
     */
    public static void showExitTips(Activity acty) {
        long curTime = System.currentTimeMillis();
        if (curTime - mLastClickBackTime < 2000) {
            acty.finish();
        } else {
            mLastClickBackTime = curTime;
            Toast.makeText(acty.getApplicationContext(), "再按一次退出应用!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置UI是否可用
     *
     * @param aty
     * @param enable
     */
    public static void setEnableUI(Activity aty, boolean enable) {
        if (aty.getWindow() != null) {
            aty.getWindow().getDecorView().setEnabled(enable);
        }
    }


}
