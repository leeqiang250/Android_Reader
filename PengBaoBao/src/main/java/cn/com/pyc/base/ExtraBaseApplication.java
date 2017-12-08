package cn.com.pyc.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.qlk.util.tool.Util;

import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.db.sm.SmDao;
import cn.com.pyc.pbb.R;
import cn.com.pyc.user.InsertPsdActivity;
import cn.com.pyc.user.UserInfoActivity;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.wxapi.WXTool;

public class ExtraBaseApplication extends PbbBaseApplication {

    @Override
    public void onCreate() {
        SmDao.getInstance(this, true).query(); //create init.
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base); MultiDex.install(this);
    }
    @Override
    public void safeExit() {
        Util.FileUtil.deleteFile(Dirs.getPycSucPath());
        super.safeExit();
    }

    /*-*****************************************
     * 退出
     *******************************************/
    // 退出时要检查钥匙的情况来显示不同的退出页面
    public void exitWithPrompt(Activity activity) {
        // 非正常方式进入则直接退出
        if (WXTool.isFromWxMake || WXTool.isFromWxRead) {
            safeExit();
            return;
        }

        UserInfo userInfo = UserDao.getDB(this).getUserInfo();
        if (activity instanceof InsertPsdActivity || userInfo.isEmailBinded()
                || userInfo.isQqBinded() || userInfo.isPhoneBinded()) {
            // 输入密码界面不弹出checkDialog
            showExitDialog(activity);
        } else {
            showCheckDialog(activity);
        }
    }

//	private void showCheckDialog(final Activity activity)
//	{
//		DialogInfo info = new DialogInfo();
//		info.prompt = "为避免文件丢失，请尽快完成身份验证!";
//		info.positiveBtnText = "验证身份";
//		info.negativeBtnText = "退出";
//		info.positiveTask = new Runnable()
//		{
//
//			@Override
//			public void run()
//			{
//				activity.startActivity(new Intent(activity, UserInfoActivity.class));		// 注意，必须是activity.startActivity()，否则需要singTask栈
//			}
//		};
//		info.negativeTask = new Runnable()
//		{
//
//			@Override
//			public void run()
//			{
//				safeExit();
//			}
//		};
//		GlobalDialog.showConfirmDialog(activity, info);
//	}
//
//	private void showExitDialog(Activity activity)
//	{
//		DialogInfo info = new DialogInfo();
//		info.prompt = "有小宝在请放心！再会！";
//		info.positiveBtnText = "取消";
//		info.negativeBtnText = "退出";
//		info.negativeTask = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				safeExit();
//			}
//		};
//		GlobalDialog.showConfirmDialog(activity, info);
//	}
//
//	private void showNullKeyExitDialog(final Activity activity)
//	{
//		DialogInfo info = new DialogInfo();
//		info.prompt = "您还未领取钥匙噢！";
//		info.positiveBtnText = "立即领取";
//		info.negativeBtnText = "退出";
//		info.positiveTask = new Runnable()
//		{
//
//			@Override
//			public void run()
//			{
//				Intent intent = new Intent(activity, KeyActivity.class);
//				intent.putExtra(KeyActivity.TAG_KEY_CURRENT, KeyActivity.TAG_KEY_KEY);
//				activity.startActivity(intent);	// 注意，必须是activity.startActivity()，否则需要singTask栈
//			}
//		};
//		info.negativeTask = new Runnable()
//		{
//
//			@Override
//			public void run()
//			{
//				safeExit();
//			}
//		};
//		GlobalDialog.showConfirmDialog(activity, info);
//	}


    private void showCheckDialog(final Activity activity) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_delete,
                null);
        final Dialog dialog = new Dialog(activity, R.style.no_frame_small);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(v);
        dialog.show();
        TextView prompt = (TextView) v.findViewById(R.id.dd_txt_content);
        prompt.setText("您没有绑定手机或邮箱，请牢记用户名密码方便下次登录。继续退出？");
        Button check = (Button) v.findViewById(R.id.dd_btn_sure);
        check.setText("去绑定");
        Button exit = (Button) v.findViewById(R.id.dd_btn_cancel);
        exit.setText("退出");
        check.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity,
                        UserInfoActivity.class));        // 注意，必须是activity.startActivity()，否则需要singTask栈
                dialog.cancel();
            }
        });
        exit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                safeExit();
                dialog.cancel();
            }
        });
    }

    private void showExitDialog(Activity activity) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_delete,
                null);
        final Dialog dialog = new Dialog(activity, R.style.no_frame_small);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(v);
        dialog.show();
        TextView prompt = (TextView) v.findViewById(R.id.dd_txt_content);
        prompt.setText("退出后不会删除历史数据，请牢记用户名密码方便下次登录。");
        Button check = (Button) v.findViewById(R.id.dd_btn_sure);
        check.setText("取消");
        Button exit = (Button) v.findViewById(R.id.dd_btn_cancel);
        exit.setText("退出");
        check.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        exit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                safeExit();
                dialog.cancel();
            }
        });
    }

   /* private void showNullKeyExitDialog(final Activity activity) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_delete,
                null);
        final Dialog dialog = new Dialog(activity, R.style.no_frame_small);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(v);
        dialog.show();
        TextView prompt = (TextView) v.findViewById(R.id.dd_txt_content);
        prompt.setText("您还未登录噢！");
        Button check = (Button) v.findViewById(R.id.dd_btn_sure);
        check.setText("立即登录");
        Button exit = (Button) v.findViewById(R.id.dd_btn_cancel);
        exit.setText("退出");
        check.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//				Intent intent = new Intent(activity, KeyActivity.class);
//				intent.putExtra(KeyActivity.TAG_KEY_CURRENT,
//						KeyActivity.TAG_KEY_KEY);
//				activity.startActivity(intent);	// 注意，必须是activity.startActivity()，否则需要singTask栈
//				dialog.cancel();

                Intent intent = new Intent(activity, KeyActivity.class);
                intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT,
                        Pbb_Fields.TAG_KEY_LOGIN);
                activity.startActivity(intent);    // 注意，必须是activity.startActivity()，否则需要singTask栈
                dialog.cancel();
            }
        });
        exit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                safeExit();
                dialog.cancel();
            }
        });
    }*/


    // 只要是Activity栈中有隐私空间的Activity，则判定为ture
    public static boolean isCurrentFocusInCipherSpace() {
        for (Activity act : ACTIVITIES) {
            if (act != null && act.getLocalClassName().contains("cipher")) {
                return true;
            }
        }
        return false;
    }
}
