package cn.com.pyc.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.qlk.util.base.BaseActivity;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.media.MediaActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.IndexPageHomeActivity;
import cn.com.pyc.sm.PayLimitConditionActivity;
import cn.com.pyc.user.InsertPsdActivity;
import cn.com.pyc.user.ModifyPsdActivity;
import cn.com.pyc.user.PayInfoActivity;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.key.KeyActivity;
import cn.com.pyc.user.key.RegisterFragment2;
import cn.com.pyc.user.key.RollBackKey;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.widget.HighlightImageView;


/**
 * @Date 创建时间：2017/6/26 上午9:56
 * @Author 作者：大熊
 * @Desc 描述：程序主页面
 */
@Deprecated
public class MainActivity extends ExtraBaseActivity implements OnClickListener {

    private static final int DESIGN_TYPE = 3;
    private static final int IDCARD_TYPE = 4;
    private static final int AGREEMENT_TYPE = 5;
    private static final int PERSONAL_PHOTO_TYPE = 6;
    private static final int OTHER_TYPE = 7;
    private static final int MAKEFILE_TYPE = 8;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymain);
//        ViewHelp.showAppTintStatusBar(this);

        GlobalData.searchTotal(this, true);    // 搜索存储卡文件夹获取已经发送,已经接收,隐私空间数量。
        GlobalData.searchPlainsFromSysDB(this); // 查询本地数据库获取各个多媒体文件的数量

        getDBUserInfo();
        //此时已不是第一次进入程序，保存状态
        PbbSP.getGSP(this).putValue(PbbSP.SP_FIRST_FUNCTION, false);

        TextView tvRecharge = (TextView) findViewById(R.id.am_tv_recharge);
        HighlightImageView imbUser = (HighlightImageView) findViewById(R.id.am_imb_user);
        Button btnMakeFile = (Button) findViewById(R.id.am_btn_makefile);
        Button btnReadFile = (Button) findViewById(R.id.am_btn_readfile);

        Button btnDesigns = (Button) findViewById(R.id.btn_designs);
        Button btnIDCard = (Button) findViewById(R.id.btn_id_card);
        Button btnAgreement = (Button) findViewById(R.id.btn_agreement);
        Button btnPrivatePhoto = (Button) findViewById(R.id.btn_personal_photo);
        Button btnOther = (Button) findViewById(R.id.btn_other);

        imbUser.setOnClickListener(this);
        btnMakeFile.setOnClickListener(this);
        btnReadFile.setOnClickListener(this);
        tvRecharge.setOnClickListener(this);

        btnDesigns.setOnClickListener(this);
        btnIDCard.setOnClickListener(this);
        btnAgreement.setOnClickListener(this);
        btnPrivatePhoto.setOnClickListener(this);
        btnOther.setOnClickListener(this);
    }
    protected UserInfo userInfo;
    protected UserDao db;
    public void getDBUserInfo() {
        db = UserDao.getDB(this);
        userInfo = db.getUserInfo();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.am_tv_recharge:
                startActivity(new Intent(this, WebActivity.class).putExtra(GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.Recharge));
                break;
            case R.id.am_imb_user:
                //  checkAndIn(RollBackKey.FromMainUser);
                startActivity(new Intent(MainActivity.this, PayInfoActivity.class));
                /*if (!userInfo.isKeyNull()) {
                }else {
                    startActivity(new Intent(MainActivity2.this, KeyActivity.class).putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN));
                }*/
                break;
            case R.id.am_btn_makefile:
                startActivity(new Intent(this, PycMainActivity.class));
                clickRecord(MAKEFILE_TYPE);
                break;
            case R.id.am_btn_readfile:
                startActivity(new Intent(this, IndexPageHomeActivity.class)
                        .putExtra(BaseActivity.TAG_WAY, BaseActivity.PBB_READER));
                break;
            //设计稿
            case R.id.btn_designs:
                takePhoto();
                clickRecord(DESIGN_TYPE);
                break;
            //身份证银行卡
            case R.id.btn_id_card:
                takePhoto();
                clickRecord(IDCARD_TYPE);
                break;
            //合同票据
            case R.id.btn_agreement:
                takePhoto();
                clickRecord(AGREEMENT_TYPE);
                break;
            //私人照片
            case R.id.btn_personal_photo:

                Intent intent = new Intent(this, MediaActivity.class);
                // Pbb_Fields.TAG_PLAIN_TOTAL 显示本地数量列表.
                intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields.TAG_PLAIN_TOTAL);
                // GlobalIntentKeys.BUNDLE_FLAG_CIPHER 密文环境.
                intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
                intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, true);
                startActivity(intent);
                clickRecord(PERSONAL_PHOTO_TYPE);
                break;
            //其他
            case R.id.btn_other:
                startActivity(new Intent(this, PycMainActivity.class));
                clickRecord(OTHER_TYPE);
                break;
        }
    }

    /**点击记录
    *@Params :
    *@Author :songyumei
    *@Date :2017/8/16
    */
    private void clickRecord(int designType) {
        getClientToken(designType);
    }
    private void getClientToken(final int designType) {
        String clientTokenUrl = Constant.UserTokenHost;

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "client_credentials");

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(
                "Authorization",
                "Basic "
                        + SecurityUtil.encryptBASE64(PhoneInfo.testID
                        + ":" + PhoneInfo.testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.post(clientTokenUrl, bundle, headers,
                new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String arg0) {
                        // 解析Json
                        try {
                            JSONObject object = new JSONObject(arg0);
                            String clientToken = (String) object
                                    .get("access_token");

                            clickRecordCount(designType,clientToken);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinished() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        hideLoading();
                        System.out.println(arg0.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                        // TODO Auto-generated method stub
                    }
                });
    }

    private void clickRecordCount(int designType, String clientToken) {
        Bundle bundle = new Bundle();
        bundle.putString("appid", "28");
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            bundle.putString("appversion", versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        bundle.putString("logtype", "Info");
        if (designType == DESIGN_TYPE) {
            bundle.putString("logtitle", "3");
        }else if (designType == IDCARD_TYPE) {
            bundle.putString("logtitle", "4");
        }else if (designType == AGREEMENT_TYPE) {
            bundle.putString("logtitle", "5");
        }else if (designType == PERSONAL_PHOTO_TYPE) {
            bundle.putString("logtitle", "6");
        }else if (designType == OTHER_TYPE) {
            bundle.putString("logtitle", "7");
        }else if (designType == MAKEFILE_TYPE) {
            bundle.putString("logtitle", "8");
        }
        bundle.putString("devhdid", PhoneInfo.getUUID(this));
        if (!userInfo.isKeyNull()) {
            bundle.putString("logname", userInfo.getUserName());
        }
        String url = Constant.UserSourceHost + "/api/v1/pbblog2";

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + clientToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        GlobalHttp.post(url, bundle,headers,
                new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String arg0) {
                        try {
                            JSONObject object = new JSONObject(arg0);
                            if (TextUtils.equals("1", (CharSequence) object.get("Status"))) {
                                //成功
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinished() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        hideLoading();
                        System.out.println(arg0.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                        // TODO Auto-generated method stub
                    }
                });
    }

    private void load_error() {
        hideBgLoading();
        GlobalToast.toastCenter(this, getResources().getString(R.string.pbb_access_server_failed));
    }
    private void takePhoto() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date())
                + ".jpg";
        photoPath = Dirs.getCameraDir(Dirs.getDefaultBoot()) + "/pbb_" + name;
        Uri uri = Uri.parse("file://" + photoPath);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 如果还有其他类型的request，则再细分
            Intent intent = new Intent(MainActivity.this, PayLimitConditionActivity.class);
            intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, photoPath);
            intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
            startActivity(intent);
        }
    }


    private void checkAndIn(RollBackKey rollBackKey) {
        UserDao.getDB(getApplicationContext());// 初始化数据库
        UserInfo userInfo = UserDao.getDB(this).getUserInfo();// 获取用户信息
        // 没钥匙
        if (userInfo.isKeyNull()) {
            RollBackKey.curRollBackKey = rollBackKey;
            // 没钥匙：点击隐私空间按钮：进入隐私空间注册界面
            if (rollBackKey.equals(RollBackKey.FromMainCipher)) {
                Intent intent = new Intent(this, KeyActivity.class);
                intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_REGISTER);
                intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, RegisterFragment2.TYPE_PASSWORD);
                startActivity(intent);
            }
            // 没钥匙：点击人头：进入登陆按钮
            else {
                Intent intent = new Intent(this, KeyActivity.class);
                intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN);
                startActivity(intent);
            }
        }
        // 有钥匙
        else {
            RollBackKey.curRollBackKey = null;
            // 有钥匙：点击：人头：进入个人中心界面
            if (rollBackKey.equals(RollBackKey.FromMainUser)) {
                Intent intent = new Intent(this, PayInfoActivity.class);
//				intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PROGRESS, Integer.valueOf
// (g_txtNotices.getText().toString().trim()));
                startActivity(intent);
            } else {
                // 有钥匙，没密码：点击隐私空间 ： 进入新密码设置界面
                if (userInfo.isPsdNull()) {
                    Intent intent = new Intent(this, ModifyPsdActivity.class);
                    intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, ModifyPsdActivity
                            .TYPE_FROM_CIPHER);
                    startActivity(intent);
                }
                // 有钥匙，有密码：点击隐私空间 ： 进入隐私空间输入密码界面
                else {
                    Intent intent = new Intent(this, InsertPsdActivity.class);
                    intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields
                            .TYPE_INSERT_CIPHER);
                    startActivity(intent);
                }
            }
        }
    }

   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ((ExtraBaseApplication) getApplication()).exitWithPrompt(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/


    /*private long mExitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long curTime = System.currentTimeMillis();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (curTime - mExitTime < 2000 &&curTime - mExitTime > 0){
                ((BaseApplication) getApplication()).safeExit();
            }else {
                GlobalToast.toastShort(this, "再按一次退出程序");
                mExitTime = curTime;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/
}
