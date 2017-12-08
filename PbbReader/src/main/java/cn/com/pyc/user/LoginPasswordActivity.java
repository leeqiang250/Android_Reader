package cn.com.pyc.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SecurityUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.widget.FlatButton;

import org.xutils.common.Callback;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.LoginBean;
import cn.com.pyc.pbbonline.bean.event.LoginSuccessRefeshRecordEvent;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.widget.HighlightImageView;
import cn.com.pyc.widget.PycEditText;
import cn.com.pyc.widget.PycPsdEditText;
import cn.com.pyc.widget.PycUnderLineTextView;
import de.greenrobot.event.EventBus;

public class LoginPasswordActivity extends PbbBaseActivity implements OnClickListener {
    private static final int CODE_2_VERIFY = 0xb0;
    private TextView title_tv;
    private PycEditText alr_edt_phone;
    private PycPsdEditText alr_edt_psd;
    private FlatButton alr_btn_login;
    private PycUnderLineTextView regist_ll;
    private String phoneNumber;
    private HighlightImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_login);
        ViewHelp.showAppTintStatusBar(this);
        initView();
        initValue();
        findViewAndSetListener();
    }

    private void findViewAndSetListener() {
        alr_btn_login.setOnClickListener(this);
        regist_ll.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void initValue() {
        phoneNumber = getIntent().getStringExtra("phone_number");
        if (!TextUtils.isEmpty(phoneNumber)) {
            alr_edt_phone.setText(phoneNumber);
            alr_edt_psd.requestFocus();
        }
    }

    private void initView() {
        alr_edt_phone = (PycEditText) findViewById(R.id.alr_edt_phone);
        alr_edt_psd = (PycPsdEditText) findViewById(R.id.alr_edit_psd);
        alr_btn_login = (FlatButton) findViewById(R.id.alr_btn_login);
        regist_ll = (PycUnderLineTextView) findViewById(R.id.alr_utv_new_regist);
        title_tv = (TextView) findViewById(R.id.title_tv);
        back = (HighlightImageView) findViewById(R.id.back_img);
        title_tv.setText(getResources().getString(R.string.identity_verify));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.alr_btn_login) {
            logining();
        } else if (id == R.id.alr_utv_new_regist) {
            //验证码登录
            Intent intent = new Intent(this, LoginVerifyCodeActivity.class);
            intent.putExtra("phone_number", phoneNumber);
            startActivityForResult(intent, CODE_2_VERIFY);
            //			Bundle bundle = new Bundle();
            //			bundle.putString("phone_number", phoneNumber);
            //			OpenPageUtil.openActivity(this, LoginVerifyCodeActivity.class, bundle);
            //			finish();
        } else if (id == R.id.back_img) {
            //finish();
            finishSetResultOk();
        }
    }

    @Override
    public void onBackPressed() {
        finishSetResultOk();
    }

    private void finishSetResultOk() {
        setResult(Activity.RESULT_OK, null);
        finish();
    }

    /*
     * 登录
     */
    private void logining() {
        if (!CommonUtil.isNetConnect(this)) {
            UIHelper.showToast(getApplicationContext(), "网络不给力！");
            return;
        }
        final String account_name = alr_edt_phone.getText().toString().trim();
        // 密码
        final String psd_set = alr_edt_psd.getText().toString();
        if (TextUtils.isEmpty(account_name)) {
            UIHelper.showToast(getApplicationContext(), "账号不能为空");
            return;
        }
        if (TextUtils.isEmpty(psd_set)) {
            UIHelper.showToast(getApplicationContext(), "密码不能为空");
            return;
        }
        showBgLoading(this, "正在登录");
        Bundle bundle = new Bundle();
        bundle.putString("username", account_name);
        bundle.putString("password", SecurityUtil.encryptBASE64(psd_set));
        bundle.putString("deviceIdentifier", Constant.TOKEN);
        bundle.putString("registrationid", (String) SPUtil.get(Fields.FIELDS_JPUSH_REGISTERID, ""));
        //add20160526：网页启动app和扫码
        bundle.putString("source", (String) SPUtil.get(Fields.FIELDS_WEB_SOURCE, ""));
        bundle.putString("shareId", (String) SPUtil.get(Fields.FIELDS_ID, ""));
        if (Util_.isWebBrowser()) {
            bundle.putString("weixin", (String) SPUtil.get(Fields.FIELDS_WEB_WEIXIN, ""));
        }
        GlobalHttp.postOn(APIUtil.getLoginPath(), bundle, new Callback.CommonCallback<String>() {
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
                    SPUtil.save(Fields.FIELDS_LOGIN_USER_NAME, account_name);
                    SPUtil.save(Fields.FIELDS_LOGIN_PASSWORD, psd_set);
                    SPUtil.save(Fields.FIELDS_LOGIN_TOKEN, o.getToken());

                    Intent data = new Intent();
                    data.putExtra("opt_flag", true);
                    setResult(Activity.RESULT_OK, data);
                    //登录成功，通知list界面刷新数据
                    EventBus.getDefault().post(new LoginSuccessRefeshRecordEvent(true));
                    finish();
                    UIHelper.showToast(getApplicationContext(), "登录成功");
                } else {
                    LoginVerifyCodeActivity.setFailCodeByLogin(getApplicationContext(), o.getCode
                            ());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (CODE_2_VERIFY == requestCode) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

}
