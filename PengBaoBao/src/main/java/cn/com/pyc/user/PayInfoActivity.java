package cn.com.pyc.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.widget.PullRefreshView;
import com.qlk.util.widget.PullRefreshView.OnRefreshListener;
import com.sz.mobilesdk.manager.ImageLoadHelp;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.UIHelper;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Observable;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ClearPBBandSZEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.connect.NetworkRequest;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.model.ClientCredentials_Model;
import cn.com.pyc.model.Userinfo_Model;
import cn.com.pyc.model.Usermoney_Model;
import cn.com.pyc.model.Usertoken_Model;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.manager.ExecutorManager;
import cn.com.pyc.suizhi.bean.event.PicEvent;
import cn.com.pyc.suizhi.help.DRMDBHelper;
import cn.com.pyc.suizhi.help.ProgressHelp;
import cn.com.pyc.suizhi.manager.db.DownDataPatDBManager;
import cn.com.pyc.suizhi.util.SZPathUtil;
import cn.com.pyc.user.key.KeyActivity;
import cn.com.pyc.utils.LoginControll;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.widget.CircleImageView;
import cn.com.pyc.xcoder.XCoder;
import de.greenrobot.event.EventBus;


public class PayInfoActivity extends ExtraBaseActivity implements OnClickListener {

    private static final String PIC_URL = "PIC_URL";
    private PullRefreshView mPullRefreshScrollView;
    private TextView tv_dot;
    private CircleImageView icon;
    private TextView register_tv;

    private UserInfo userInfo;
    private UserDao db;
    private int loadCount = 0;

    private static final int MSG_CLEAR_CACHE = 100;
    private ExecHandler mHandler = new ExecHandler(this);

    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UPDATE_SETTING) {
            onResume(); //更新信息
        }
    }

    //登录头像的图片加载
    public void onEventMainThread(PicEvent event) {
        if (event.getType() == BaseEvent.Type.UI_PIC_ICON) {
            init_UserMoney();//更新余额
            String picUrl = event.getPicUrl();
            SPUtil.save(PIC_URL, picUrl);
            if (userInfo.isKeyNull()) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable
                        .logo_unlogin)); //未登录头像
            } else {
                getImage(picUrl);
            }
        }
    }

    //接收从UserCenterActivity发送的清除通知
    public void onEventMainThread(ClearPBBandSZEvent event) {
        if (!event.isComplete()) {
            ExecutorManager.getInstance().execute(new ClearThread(this));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_info);
        getDBUserInfo();
        init_view();
        init_listener();
    }

    //未登陆状态下显示登陆按钮
    @Override
    protected void onResume() {
        super.onResume();
//        getDBUserInfo();
        //没有钥匙
        if (userInfo.isKeyNull()) {
            register_tv.setText("登录/注册");
            tv_dot.setText("0");
            icon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo_unlogin));
        } else {  // 有钥匙
            init_UserMoney();
            getImage((String) SPUtil.get(PIC_URL, ""));
        }
    }

    private void init_UserMoney() {
        if (!CommonUtil.isNetConnect(this)) {
            refresh();
            return;
        }
        if (loadCount == 0)
            showBgLoading(this);
        loadCount++;
        getUserToken(userInfo);
    }

    private void init_view() {
        EventBus.getDefault().register(this);
        mPullRefreshScrollView = (PullRefreshView) findViewById(R.id.pull_refresh_scrollview);//下拉刷新

        tv_dot = (TextView) findViewById(R.id.tv_dot);//余额
        icon = (CircleImageView) findViewById(R.id.iv);
        register_tv = (TextView) findViewById(R.id.register_tv);
        //登录注册
        findViewById(R.id.tv_login_register).setOnClickListener(this);

        // 如果是企业账户，隐藏相关界面内容
        if (userInfo.getStatus() == 1) {
            findViewById(R.id.rl_user_index_5).setVisibility(View.GONE);
            findViewById(R.id.api_imv_divider5).setVisibility(View.GONE);
        }
    }

    private void init_listener() {
        findViewById(R.id.rl_user_index_2).setOnClickListener(this);
        findViewById(R.id.rl_user_index_5).setOnClickListener(this);
        findViewById(R.id.rl_user_index_7).setOnClickListener(this);
        findViewById(R.id.rl_user_index_10).setOnClickListener(this);
        mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新,获取用户信息等等信息。
                if (!CommonUtil.isNetConnect(PayInfoActivity.this)) {
                    GlobalToast.toastCenter(PayInfoActivity.this, PayInfoActivity.this
                            .getResources().getString(R.string.pbb_net_disconnected));
                    mPullRefreshScrollView.setRefreshComplete();
                    return;
                }
                Pull_down_refresh();
            }
        });
    }

    private void Pull_down_refresh() {
        getClientToken();
    }

    private void getClientToken() {
        NetworkRequest.getClientToken(new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    ClientCredentials_Model mClientCredentials_Model = JSONObject.parseObject
                            (arg0, ClientCredentials_Model.class);
                    String clientToken = mClientCredentials_Model.getAccess_token();
                    getUserInfo(clientToken);
                } catch (Exception e) {
                    e.printStackTrace();
                    show_error();
                }
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onError() {
                show_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                mPullRefreshScrollView.setRefreshComplete();
            }
        });
    }

    private void getUserInfo(String clientToken) {
        NetworkRequest.getUserInfo(userInfo, clientToken, new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    Userinfo_Model userLogin = JSONObject.parseObject(arg0, Userinfo_Model.class);
                    String status = userLogin.getStatus();
                    if (status.equals("1")) {
                        setUInfoData(userInfo, userLogin);
                        db.saveUserInfo(userInfo);
                        getUserToken(userInfo);
                    } else if (status.equals("0")) {
                        show_error();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    show_error();
                }
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onError() {
                show_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                mPullRefreshScrollView.setRefreshComplete();
            }
        });
    }

    private void getUserToken(final UserInfo userInfo) {
        NetworkRequest.getUserToken(userInfo, new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(final String arg0) {
                // 解析Json
                try {
                    Usertoken_Model user = JSONObject.parseObject(arg0, Usertoken_Model.class);
                    getUserMoney(user.getAccess_token(), userInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    show_error();
                    hideBgLoading();
                }
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onError() {
                show_error();
                hideBgLoading();
            }

            @Override
            public void onCancelled(Exception arg0) {
                mPullRefreshScrollView.setRefreshComplete();
                hideBgLoading();
            }
        });
    }

    //TODO
    private void getUserMoney(String access_token, final UserInfo userInfo) {
        NetworkRequest.getUserMoney(access_token, new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                try {
                    Usermoney_Model user_money_model = JSONObject.parseObject(arg0,
                            Usermoney_Model.class);
                    if (user_money_model.getStatus().equals("1")) {
                        userInfo.setMoney(user_money_model.getResult().getMoney());
                        userInfo.setRedbag(user_money_model.getResult().getFreemoney());
                        db.saveUserInfo(userInfo);
                        getDBUserInfo();
                        refresh();
                        mPullRefreshScrollView.setRefreshComplete();
                    } else if (user_money_model.getStatus().equals("0")) {
                        show_error();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    show_error();
                    hideBgLoading();
                }
            }

            @Override
            public void onFinished() {
                mPullRefreshScrollView.setRefreshComplete();
                hideBgLoading();
            }

            @Override
            public void onError() {
                show_error();
                hideBgLoading();
            }

            @Override
            public void onCancelled(Exception arg0) {
                mPullRefreshScrollView.setRefreshComplete();
                hideBgLoading();
            }
        });
    }


    // 更新余额和红包数
    public void refresh() {
        register_tv.setText(userInfo.getNick());
        int money = 0;
        if (userInfo.getMoney() == null || userInfo.getMoney().trim().length() == 0) {
            money = 0;
        } else {
            int i = Integer.parseInt(userInfo.getMoney());
            money = i;
        }

        int redBag = 0;
        if (userInfo.getRedbag() == null || userInfo.getRedbag().trim().length() == 0) {
            redBag = 0;
        } else {
            int i = Integer.parseInt(userInfo.getRedbag());
            redBag = i;
        }
        tv_dot.setText((money + redBag) + "");
        getImage((String) SPUtil.get(PIC_URL, ""));
    }

    @Override
    public void onClick(View v) {
        if (userInfo.isKeyNull()) {
            switch (v.getId()) {
                    // 手动激活记录
                case R.id.rl_user_index_2:
                    // 账户余额
                case R.id.rl_user_index_5:
                    // 我的消息
                case R.id.rl_user_index_7:
                    //登录注册
                case R.id.tv_login_register:
                    //进入登录界面
                    Intent intent = new Intent(this, KeyActivity.class);
                    intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN);
                    startActivity(intent);
                    break;
                //设置
                case R.id.rl_user_index_10:
                    //设置
                    startActivity(new Intent(this, UserCenterActivity.class));
                default:
                    break;
            }
        } else {
            switch (v.getId()) {
                // 手动激活记录
                case R.id.rl_user_index_2:
                    startActivity(new Intent(this, WebActivity.class).putExtra(GlobalIntentKeys
                            .BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.PayRecord));
                    break;
                // 账户余额
                case R.id.rl_user_index_5:
                    startActivity(new Intent(this, WebActivity.class).putExtra(GlobalIntentKeys
                            .BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.AccountBalance));
                    break;
                // 我的消息
                case R.id.rl_user_index_7:
                    startActivity(new Intent(this, WebActivity.class).putExtra(GlobalIntentKeys
                            .BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.MyNoticeRecord));
                    break;
                //设置
                case R.id.rl_user_index_10:
                    //设置
                    startActivity(new Intent(PayInfoActivity.this, UserCenterActivity.class));
                    break;
                //登录注册
                case R.id.tv_login_register:
                    startActivity(new Intent(this, UserInfoActivity.class));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        switch ((ObTag) data) {
            case Key:
                getDBUserInfo();
                refresh();
                break;
            default:
                break;
        }
    }

    /**
     * 把userinfoModel设置到UserInfo对象中。
     *
     * @author 李巷阳
     * @date 2016/12/6 14:58
     */
    private void setUInfoData(UserInfo uinfo, Userinfo_Model userLogin) {
        uinfo.setUserName(userLogin.getResult().getUserName());
        uinfo.setNick(userLogin.getResult().getUserNick());
        uinfo.setPhone(userLogin.getResult().getMobilePhone());
        if (userLogin.getResult().getMobileStatus().equals("1")) {
            uinfo.setPhoneBinded(true);
        } else if (userLogin.getResult().getMobileStatus().equals("0")) {
            uinfo.setPhoneBinded(false);
        }
        uinfo.setEmail(userLogin.getResult().getEmail());
        if (userLogin.getResult().getEmailStatus().equals("1")) {
            uinfo.setEmailBinded(true);
        } else if (userLogin.getResult().getEmailStatus().equals("0")) {
            uinfo.setEmailBinded(false);
        }
        if (!TextUtils.isEmpty(userLogin.getResult().getQQNick())) {
            uinfo.setQqBinded(true);
            uinfo.setQqBinded(1);
            uinfo.setQqNick(userLogin.getResult().getQQNick());
        } else {
            uinfo.setQqBinded(false);
            uinfo.setQqBinded(0);
        }

        if (userLogin.getResult().getIsEnterpriseChild().equals("1")) {
            uinfo.setStatus(1);
        } else if (userLogin.getResult().getIsEnterpriseChild().equals("0")) {
            uinfo.setStatus(0);
        }
        String password = userLogin.getResult().getPassword();
        // 解密服务器返回的密码
        uinfo.setPsd(XCoder.getHttpDecryptText(password));
    }

    private void show_error() {
        GlobalToast.toastShort(PayInfoActivity.this, "对不起,获取用户信息失败。");
        mPullRefreshScrollView.setRefreshComplete();
    }

    public void getDBUserInfo() {
        db = UserDao.getDB(this);
        userInfo = db.getUserInfo();
    }


    //下载头像，保存到sd卡上
    private void getImage(String url) {
        File file = new File(SZPathUtil.getSDCardRootPath() + SZPathUtil
                .getDefaultImageCacheOffset());
        if (!file.exists()) file.mkdirs();
        File head = new File(file.getPath(), userInfo.getUserName() + ".jpg");
        if (head.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(head.getPath());
            icon.setImageBitmap(bmp);
            return;
        }
        RequestParams params = new RequestParams(url);
        params.setUseCookie(false);
        params.setConnectTimeout(30 * 1000);
        params.setAutoResume(false); // 设置断点续传
        params.setAutoRename(true);
        params.setSaveFilePath(head.getPath());
        x.http().get(params, new Callback.CommonCallback<File>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.logo_unlogin));
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(File arg0) {
                if (arg0 == null) {
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.logo_login));
                    return;
                }
                //此path就是对应文件的缓存路径
                Bitmap bmp = BitmapFactory.decodeFile(arg0.getPath());
                icon.setImageBitmap(bmp);
            }
        });
    }

    @Override
    public void onBackPressed() {
        UIHelper.showExitTips(this);
    }


    private static class ClearThread implements Runnable {
        private WeakReference<PayInfoActivity> reference;

        private ClearThread(PayInfoActivity context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void run() {
            if (reference == null) return;
            PayInfoActivity activity = reference.get();
            if (activity == null) return;
            if (LoginControll.checkLogin()) {
                FileUtil.deleteAllFile(SZPathUtil.getFilePrefixPath() + "/");
                FileUtil.deleteAllFile(SZPathUtil.getDRMPrefixPath() + "/");
                DRMDBHelper.deleteTableData();
            }
            ImageLoadHelp.clearCache();
            DownDataPatDBManager.Builder().deleteAll();
            ////ShareMomentEngine.clearSharePosition();
            ProgressHelp.clearProgress();
            //删除pbb
            List<String> paths = GlobalData.Sm.instance(activity).getCopyPaths(false);
            for (String path : paths) {
                GlobalData.Sm.instance(activity).delete(path);
            }
            activity.mHandler.sendEmptyMessageDelayed(MSG_CLEAR_CACHE, 400);
        }
    }

    private static class ExecHandler extends Handler {
        private WeakReference<PayInfoActivity> reference;

        private ExecHandler(PayInfoActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PayInfoActivity activity = reference.get();
            if (activity == null) return;
            //清除完成
            EventBus.getDefault().post(new ClearPBBandSZEvent(true));
            //通知主页面刷新，重新扫描
            EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_HOME_TAB_3));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadCount = 0;
        EventBus.getDefault().unregister(this);
    }
}
