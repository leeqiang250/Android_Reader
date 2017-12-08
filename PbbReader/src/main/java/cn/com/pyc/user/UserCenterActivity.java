package cn.com.pyc.user;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.mobilesdk.util.UIHelper.DialogCallBack;

import org.xutils.common.Callback;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ClearPBBandSZEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.LoginBean;
import cn.com.pyc.pbbonline.bean.event.LoginSuccessRefeshRecordEvent;
import cn.com.pyc.pbbonline.common.Code;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.setting.AboutActivity;
import cn.com.pyc.update.UpdateActivity;
import cn.com.pyc.update.UpdateTool;
import cn.com.pyc.utils.ClearKeyUtil;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.widget.HighlightImageView;
import de.greenrobot.event.EventBus;

/**
 * 用户中心设置
 */
public class UserCenterActivity extends PbbBaseActivity implements OnClickListener {

    private TextView auc_tv_logname;
    //private SharedDBManager dbManager;
    //private AsyncTask<Void, Integer, Boolean> mClearTask;
    //private boolean isClearRunning = false;
    //private TextView exita_ccount_title;

    protected UserInfo userInfo;
    protected UserDao db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        ViewHelp.showAppTintStatusBar(this);
        //dbManager = new SharedDBManager();
        UpdateTool.checkUpdateInfo(this, false);
        EventBus.getDefault().register(this);

        db = UserDao.getDB(this);
        userInfo = db.getUserInfo();

        TextView exita_ccount_title = (TextView) findViewById(R.id.grzx_exita_ccount_title);
        HighlightImageView asa_iv_back = (HighlightImageView) findViewById(R.id.back_img);
        TextView asa_tv_title = (TextView) findViewById(R.id.title_tv);
        RelativeLayout auc_login = (RelativeLayout) findViewById(R.id.auc_user_login);
        auc_tv_logname = (TextView) findViewById(R.id.auc_tv_username);
        ImageView auc_img_login = (ImageView) findViewById(R.id.auc_img_login);
        ImageView auc_arrow_login = (ImageView) findViewById(R.id.auc_arrow_login);
        RelativeLayout auc_my_sell = (RelativeLayout) findViewById(R.id.auc_my_sell);
        RelativeLayout auc_my_share = (RelativeLayout) findViewById(R.id.auc_my_share);
        RelativeLayout auc_clear_cache = (RelativeLayout) findViewById(R.id.auc_clear_cache);
        RelativeLayout auc_feedback = (RelativeLayout) findViewById(R.id.auc_feedback);
        RelativeLayout auc_checknew = (RelativeLayout) findViewById(R.id.auc_check_update);
        RelativeLayout auc_about = (RelativeLayout) findViewById(R.id.auc_about);
        ImageView imvRed = (ImageView) findViewById(R.id.imv_red_bit);
        if (UpdateTool.isApkNew(this)) {
            imvRed.setVisibility(View.VISIBLE);
        } else {
            imvRed.setVisibility(View.GONE);
        }

        //退出按钮在登录的状态下显示
        boolean isLogin = Util_.isLogin();
        asa_tv_title.setText("设置");
        auc_tv_logname.setText(isLogin ? (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "")
                : "登录");
        auc_arrow_login.setVisibility(isLogin ? View.GONE : View.VISIBLE);
        auc_img_login.setImageDrawable(getResources().getDrawable(
                isLogin ? R.drawable.icon_user_logo_login : R.drawable.icon_user_logo_logout));
        exita_ccount_title.setVisibility(!userInfo.isKeyNull() ? View.VISIBLE : View.GONE);
        //listener：
        exita_ccount_title.setOnClickListener(this);
        asa_iv_back.setOnClickListener(this);
        auc_login.setEnabled(!isLogin);
        auc_login.setOnClickListener(this);
        auc_my_sell.setOnClickListener(this);
        auc_my_share.setOnClickListener(this);
        auc_clear_cache.setOnClickListener(this);
        auc_feedback.setOnClickListener(this);
        auc_checknew.setOnClickListener(this);
        auc_about.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_img) {
            backUI();
        } else if (id == R.id.grzx_exita_ccount_title) {

            if (userInfo.isEmailBinded() || userInfo.isPhoneBinded() || userInfo.isQqBinded()) {
                View v1 = LayoutInflater.from(UserCenterActivity.this).inflate(R.layout
                        .dialog_delete, null);
                final Dialog dialog = new Dialog(UserCenterActivity.this, R.style.no_frame_small);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setContentView(v1);
                dialog.show();
                TextView prompt = (TextView) v1.findViewById(R.id.dd_txt_content);
                prompt.setText("退出后不会删除历史数据，请牢记用户名密码方便下次登录。");
                Button check = (Button) v1.findViewById(R.id.dd_btn_sure);
                check.setText("取消");
                Button exit = (Button) v1.findViewById(R.id.dd_btn_cancel);
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
                        exitPbbUser();
                        dialog.cancel();
                    }
                });
            } else {
                View v2 = LayoutInflater.from(UserCenterActivity.this).inflate(R.layout
                                .dialog_delete, null);
                final Dialog dialog = new Dialog(UserCenterActivity.this, R.style.no_frame_small);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setContentView(v2);
                dialog.show();
                TextView prompt = (TextView) v2.findViewById(R.id.dd_txt_content);
                prompt.setText("您没有绑定手机或邮箱，请牢记用户名密码方便下次登录。继续退出？");
                Button check = (Button) v2.findViewById(R.id.dd_btn_sure);
                check.setText("退出");
                Button exit = (Button) v2.findViewById(R.id.dd_btn_cancel);
                exit.setText("取消");
                check.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exitPbbUser();
                        dialog.cancel();
                    }
                });
                exit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.cancel();
                    }
                });
            }

//            exitPbbUser();
//            exitLogin();
        } else if (id == R.id.auc_user_login) {
            startActivity(new Intent(this, LoginVerifyCodeActivity.class));
            finish();
        } else if (id == R.id.auc_my_sell) {
            OpenPageUtil.openBrowserOfSystem(this, "http://www.suizhi.com");
        } else if (id == R.id.auc_my_share) {
            OpenPageUtil.openBrowserOfSystem(this, "http://on.pyc.com.cn");
        } else if (id == R.id.auc_about) {
            OpenPageUtil.openActivity(UserCenterActivity.this, AboutActivity.class);
        } else if (id == R.id.auc_clear_cache) {
            //清空缓存
            clearCache();
        } else if (id == R.id.auc_feedback) {
            startActivity(new Intent(this, WebActivity.class).putExtra(
                    GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.Idea));
        } else if (id == R.id.auc_check_update) {
            checkUpdate();
        }
    }

//    private boolean hasShares() {
//        return dbManager.existData();
//    }


    //接收从PayInfoActivity发送的通知
    public void onEventMainThread(ClearPBBandSZEvent event) {
        if (event.isComplete()) {
            hideBgLoading();
            showToast(getString(R.string.clear_success));
        }
    }

    private void clearCache() {
        UIHelper.showCommonDialog(this, "清空缓存", "清空缓存会删除所有已下载和导入的文件，确定清空吗？", null,
                new UIHelper.DialogCallBack() {
                    public void onConfirm() {
                        //清除pbb和随知文件
                        showBgLoading(UserCenterActivity.this, getString(R.string
                                .now_clear_item_percent));
                        EventBus.getDefault().post(new ClearPBBandSZEvent(false));

//                        if (!hasShares()) {
//                            return;
//                        }
//                        startClearing();
                    }
                });
    }


//    private void startClearing() {
//        cancelTask();
//        mClearTask = new AsyncTask<Void, Integer, Boolean>() {
//            private LoadingBgDialog dialog;
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                isClearRunning = true;
//                dialog = showBgLoading(UserCenterActivity.this,
//                        getString(R.string.now_clear_item_percent));
//            }
//
//            @Override
//            protected Boolean doInBackground(Void... params) {
//                if (isCancelled())
//                    return false;
//                List<Shared> shareds = getDeviceSharedorUserShared();
//                for (int x = 0, size = shareds.size(); x < size; x++) {
//                    if (!isClearRunning)  //添加标志，用户返回键时跳出删除操作
//                        break;
//
//                    publishProgress(x, size);
//                    String shareId = shareds.get(x).getShareId();
//                    Util_.deleteCommonDataDB(SZInitInterface.getUserName(""), shareId);
//                }
//                return true;
//            }
//
//            @Override
//            protected void onProgressUpdate(Integer... values) {
//                if (dialog != null) {
//                    int ints = values[0] * 100 / values[1];
//                    String msg = getString(R.string.now_clear_item_percent);
//                    if (ints != 0)
//                        msg = msg + "(" + ints + "%)";
//                    dialog.setContentText(msg);
//                }
//            }
//
//            @Override
//            protected void onPostExecute(Boolean result) {
//                super.onPostExecute(result);
//                hideBgLoading();
//                if (result)
//                    showToast(getString(R.string.clear_success));
//            }
//
//            @Override
//            protected void onCancelled(Boolean result) {
//                super.onCancelled(result);
//                hideBgLoading();
//                isClearRunning = false;
//            }
//        };
//        mClearTask.execute();
//    }
//
//    private void cancelTask() {
//        if (mClearTask != null && mClearTask.getStatus() == AsyncTask.Status.RUNNING) {
//            mClearTask.cancel(true);
//            mClearTask = null;
//        }
//        isClearRunning = false;
//    }

//    protected List<Shared> getDeviceSharedorUserShared() {
//        List<Shared> records = new ArrayList<Shared>();
//        if (dbManager == null) {
//            return records;
//        }
//
//        if (Util_.isLogin()) {
//            String account = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
//            records = dbManager.findByAccount(account);
//        } else {
//            records = dbManager.findByDevice();
//        }
//        return records;
//    }

    private void checkUpdate() {
        if (!CommonUtil.isNetConnect(this)) {
            GlobalToast.toastShort(this, getString(R.string.network_not_available));
            return;
        }
        if (UpdateTool.isApkNew(this)) {
            //UIHelper.showCommonDialog(this, "发现新版本", "是否现在升级？", "升级", new DialogCallBack() {
            UIHelper.showCommonDialog(this, "发现新版本", "有新版本更新了，为了更好的体验，请耐心升级到最新版！", "升级", new
                    DialogCallBack() {
                        @Override
                        public void onConfirm() {
                            startActivity(new Intent(UserCenterActivity.this, UpdateActivity
                                    .class));
                        }
                    });
        } else {
            UIHelper.showToast(getApplicationContext(), "已是最新版本");
        }
    }

    //退出鹏宝宝用户
    private void exitPbbUser() {
        //移除保存的token和name
        ClearKeyUtil.removeKey();
        //结束webView网页
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_BROWSER_FINISH));
        //注销后，也需要通知发现界面更新状态（注销情况后）
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_DISCOVER));
        //通知主界面刷新
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_HOME_TAB_3));

        userInfo.setUserName(null);
        userInfo.setUid(null);
        db.saveUserInfo(userInfo);
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_SETTING));
        finish();
    }

    //退出原来online用户
    private void exitLogin() {
        if (!CommonUtil.isNetConnect(UserCenterActivity.this)) {
            showToast(getString(R.string.network_not_available));
            return;
        }
        UIHelper.showCommonDialog(this, "注销", "您确定注销当前登录？", null, new UIHelper.DialogCallBack() {

            @Override
            public void onConfirm() {
                loginOut();
            }
        });
    }

    protected void loginOut() {
        showBgLoading(UserCenterActivity.this, "正在注销");
        String username = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
        String token = (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, "");
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        bundle.putString("token", token);
        bundle.putString("deviceIdentifier", Constant.TOKEN);
        bundle.putString("registrationid", (String) SPUtil.get(Fields.FIELDS_JPUSH_REGISTERID, ""));
        GlobalHttp.post(APIUtil.getExitLoginPath(), bundle, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                UIHelper.showToast(getApplicationContext(), getString(R.string.load_server_failed));
            }

            @Override
            public void onFinished() {
                hideBgLoading();
            }

            @Override
            public void onSuccess(String arg0) {
                LoginBean o = JSON.parseObject(arg0, LoginBean.class);
                if (o.isSuccess()) {
                    SPUtil.remove(Fields.FIELDS_LOGIN_USER_NAME);
                    SPUtil.remove(Fields.FIELDS_LOGIN_PASSWORD);
                    SPUtil.remove(Fields.FIELDS_LOGIN_TOKEN);
                    auc_tv_logname.setText("登录");
                    EventBus.getDefault().post(new LoginSuccessRefeshRecordEvent(false));
                    //退出成功，通知sharelist界面刷新数据
                    backUI();
                    UIHelper.showToast(getApplicationContext(), "退出成功!");
                } else {
                    setFailCode(o.getCode());
                }
            }
        });
    }

    protected void setFailCode(String code) {
        switch (code) {
            case Code._9109: {
                SPUtil.remove(Fields.FIELDS_LOGIN_USER_NAME);
                SPUtil.remove(Fields.FIELDS_LOGIN_PASSWORD);
                SPUtil.remove(Fields.FIELDS_LOGIN_TOKEN);
                EventBus.getDefault().post(new LoginSuccessRefeshRecordEvent(false));
                backUI();
                UIHelper.showToast(getApplicationContext(), "当前登录已失效或未登录！");
            }
            break;
            case Code._9110:
                UIHelper.showToast(getApplicationContext(), "参数错误");
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        cancelTask();
        backUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void backUI() {
        finish();
        //overridePendingTransition(0, R.anim.slide_out_from_right);
    }
}
