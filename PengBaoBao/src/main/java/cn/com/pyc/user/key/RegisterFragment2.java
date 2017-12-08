package cn.com.pyc.user.key;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.mobilesdk.util.UIHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.pyc.base.PbbBaseFragment;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.connect.NetworkRequest;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.model.ClientCredentials_Model;
import cn.com.pyc.model.PhoneVerificationModel;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.setting.AboutActivity;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.UserCenterActivity;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.web.WebActivity;
import cn.com.pyc.widget.WidgetTool;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_CLASS_TEXT;

/**
 * 新用户注册页
 *
 * @author Administrator
 */
public class RegisterFragment2 extends PbbBaseFragment implements OnCheckedChangeListener,
        OnClickListener {

    private UserDao db = UserDao.getDB(getActivity());
    private UserInfo userInfo;

    public static final String TYPE_NICK = "nick";
    public static final String TYPE_PASSWORD = "password";
    private EditText phoneOrEmail;//手机号或邮箱
    private EditText edtPsd;//密码
    private ImageView qq_login;//qq登录
    private TextView tv_phone_email;//手机注册或邮箱注册的切换
    private boolean isPhoneRegister = true;//默认是手机号注册
    private EditText verificationCode;//验证码
    private MyCount mc;
    private Button btnVerificationCode;//验证码按钮

    private String securityCode;
    private Button phone;
    private Button email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_key_register2, container, false);
        findViewAndSetListeners(v);
        ((KeyActivity) getActivity()).showKeyboard();
        userInfo = db.getUserInfo();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qq_login:
                qqLogin();
                break;
            // 注册
            case R.id.fkr_btn_register:
                phoneEmailRegister();
                break;
            // 立即登陆
            case R.id.fkr_txt_old_user:
                changeFragment(Pbb_Fields.TAG_KEY_LOGIN);
                break;
            //手机注册或邮箱注册的切换
            case R.id.phone_email_register:
                isPhoneRegister = !isPhoneRegister;
                tv_phone_email.setText(isPhoneRegister ? getString(R.string.email_register) :
                        getString(R.string.phone_register));
                phoneOrEmail.setHint(isPhoneRegister ? "手机号" : "邮箱");
                phoneOrEmail.setInputType(isPhoneRegister ? TYPE_CLASS_NUMBER : TYPE_CLASS_TEXT);
                break;
            //获取验证码
            case R.id.fkr_btn_verification_code:
                getPhoneVerificationCode();
                break;
            //鹏保宝用户服务条款
            case R.id.isAccept:
//                OpenPageUtil.openActivity(getActivity(), AboutActivity.class);
                getActivity().startActivity(new Intent(getActivity(), WebActivity.class).putExtra(
                        GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE, WebActivity.WebPage.Privacy));
                break;
            case R.id.phone:
                isPhoneRegister = true;
                register(isPhoneRegister);
                break;
            case R.id.email:
                isPhoneRegister = false;
                register(isPhoneRegister);
                break;
            default:
                break;
        }
    }

    private void register(boolean isPhoneRegister) {
        if (isPhoneRegister) {
            //手机号
            phone.setTextColor(Color.BLACK);
            phone.setBackground(getResources().getDrawable(R.drawable.xml_click_btn));
            email.setTextColor(Color.WHITE);
            email.setBackground(getResources().getDrawable(R.drawable.xml_phone_email));
        }else {
            //邮箱
            phone.setTextColor(Color.WHITE);
            phone.setBackground(getResources().getDrawable(R.drawable.xml_phone_email));
            email.setTextColor(Color.BLACK);
            email.setBackground(getResources().getDrawable(R.drawable.xml_click_btn));
        }
        phoneOrEmail.setHint(isPhoneRegister ? "手机号" : "邮箱");
        phoneOrEmail.setInputType(isPhoneRegister ? TYPE_CLASS_NUMBER : TYPE_CLASS_TEXT);
    }

    private void phoneEmailRegister() {
        if (!CommonUtil.isNetConnect(getActivity())) {
            UIHelper.showToast(getActivity().getApplicationContext(), getString(cn.com.pyc.pbb
                    .reader.R.string.network_not_available));
            return;
        }
        if (TextUtils.isEmpty(getPhoneEmail()) || TextUtils.isEmpty(getpassword()) || TextUtils
                .isEmpty(getVerificationCode())) {
            UIHelper.showToast(getActivity().getApplicationContext(), getString(R.string
                    .pe_verification_password));
            return;
        }

        if (getpassword().length() < 4 || getpassword().length() > 16) {
            GlobalToast.toastShort(getActivity(), "请输入由4-16位字母、数字或下划线组成的密码。");
            return;
        }
        /*if (outDate((String) SPUtil.get(securityCode,""))) {
            GlobalToast.toastShort(getActivity(), "验证码已过期，请重新获取");
			return;
		}*/

        showBgLoading(getActivity());
        getClientToken(userInfo, getPhoneEmail(), getpassword(), getVerificationCode(),
                isPhoneRegister);
    }

    private void getClientToken(final UserInfo userInfo, final String phoneEmail, final String
            getpassword, final String verificationCode, final boolean isPhoneRegister) {
        NetworkRequest.getClientToken(new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    ClientCredentials_Model mClientCredentials_Model = com.alibaba.fastjson
                            .JSONObject.parseObject(arg0, ClientCredentials_Model.class);
                    String clientToken = mClientCredentials_Model.getAccess_token();
                    pERegister(userInfo, phoneEmail, getpassword, verificationCode,
                            isPhoneRegister, clientToken);
                } catch (Exception e) {
                    e.printStackTrace();
                    load_error();
                }
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onError() {
                load_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                hideBgLoading();
            }
        });
    }

    /**
     * 手机注册或者邮箱注册
     *
     * @Params :
     * @Author :songyumei
     * @Date :2017/8/9
     */
    private void pERegister(final UserInfo userInfo, String phoneEmail, String getpassword,
                            String verificationCode, boolean isPhoneRegister, final String
                                    clientToken) {
        Bundle bundle = new Bundle();
        bundle.putString("UserName", phoneEmail);
        bundle.putString("Password", getpassword);
        bundle.putString("Userfrom", "PbbAndroid");
        bundle.putString(isPhoneRegister ? "Mobilephone" : "Email", phoneEmail);
        bundle.putString("Code", verificationCode);
        bundle.putString("RegType", isPhoneRegister ? "phone" : "email");

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + clientToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        GlobalHttp.post(getPERegisterPath(), bundle, headers, new CommonCallback<String>
                () {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                UIHelper.showToast(getActivity().getApplicationContext(), getString(cn.com.pyc
                        .pbb.reader.R.string.load_server_failed));
                hideBgLoading();
            }

            @Override
            public void onFinished() {
                //hideBgLoading();
            }

            @Override
            public void onSuccess(String arg0) {
                SZLog.d("registe: " + arg0);
                JSONObject object = null;
                try {
                    object = new JSONObject(arg0);
                    String status = (String) object
                            .get("Status");
                    if (status.equals("1")) {

                        String Result = (String) object
                                .get("Result");

                        getUid(clientToken, userInfo,Result);

                        // 记录当前时间
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale
                                .getDefault());
                        String currentTime = sdf.format(new Date());
                        securityCode = "codeSend";
                        SPUtil.save(securityCode, currentTime);

//						startActivity(new Intent(getActivity(),
//								KeySuccessActivity.class));
//						getActivity().finish();
                    } else if (status.equals("0")) {
                        hideBgLoading();
                        GlobalToast.toastShort(getActivity(), (String) object.get("Message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    load_error();
                }
            }
        });
    }

    private void getUid(String clientToken, final UserInfo uinfo, final String result) {
        String uidUrl = Constant.UserSourceHost + "api/v1/useruidkey";
        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("logname", uinfo.getUserName());
        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + clientToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.get(uidUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                System.out.println("UID返回obj:" + arg0);
                try {

                    userInfo.setUserName(result);
                    db.saveUserInfo(userInfo);

                    JSONObject object = new JSONObject(arg0);
                    String Result = (String) object.get("Result");
                    byte[] byteResult = Result.getBytes();
                    if (!byteResult.equals(null)) {
                        uinfo.setUid(byteResult);
                        db.saveUserInfo(uinfo);
                    }

                    hideBgLoading();
                    //注册成功之后直接登录
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    LoginFragment fragmentByTag = (LoginFragment) fragmentManager.findFragmentByTag(Pbb_Fields.TAG_KEY_LOGIN);
                    fragmentByTag.loginEmail(getPhoneEmail(),getpassword());

                    //startActivity(new Intent(getActivity(), KeySuccessActivity.class));
                    //getActivity().finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    load_error();
                }
            }

            @Override
            public void onFinished() {
                //hideBgLoading();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                load_error();
            }

            @Override
            public void onCancelled(CancelledException arg0) {
            }
        });
    }

    private String getPERegisterPath() {
        return Constant.UserSourceHost + "api/v1/userregister";
    }

    /**
     * 验证邮箱格式
     *
     * @Params :
     * @Author :songyumei
     * @Date :2017/8/8
     */
    public boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(" +
                "([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    private void getClientToken(final UserInfo userInfo, final String email) {

        NetworkRequest.getClientToken(new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    ClientCredentials_Model mClientCredentials_Model = com.alibaba.fastjson
                            .JSONObject.parseObject(arg0, ClientCredentials_Model.class);
                    String clientToken = mClientCredentials_Model.getAccess_token();
                    getPEVerificationCode(userInfo, email, clientToken);
                } catch (Exception e) {
                    e.printStackTrace();
                    load_error();
                }
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onError() {
                load_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                hideBgLoading();
            }
        });
    }

    private void load_error() {
        hideBgLoading();
        GlobalToast.toastCenter(getActivity(), getActivity().getResources().getString(R.string
                .pbb_access_server_failed));
    }

    /**
     * 获取验证码
     *
     * @Params :
     * @Author :songyumei
     * @Date :2017/8/9
     */
    private void getPEVerificationCode(UserInfo userInfo, String email, String clientToken) {
        Bundle bundle = new Bundle();
        bundle.putString(isPhoneRegister ? "Phone" : "Email", email);
        bundle.putInt("Codetype", 1);
        String url = isPhoneRegister ? getPhoneVerificationCode2() : getEmailVerificationCodeUrl();

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + clientToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        GlobalHttp.get(url, bundle, headers,
                new CommonCallback<String>() {
                    @Override
                    public void onCancelled(CancelledException arg0) {
                        hideBgLoading();
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        UIHelper.showToast(getActivity().getApplicationContext(),
                                getString(cn.com.pyc.pbb.reader.R.string.send_code_failed));
                        setMyCountTimerCancel();
                        hideBgLoading();
                    }

                    @Override
                    public void onFinished() {
                        hideBgLoading();
                    }

                    @Override
                    public void onSuccess(String arg0) {
                        PhoneVerificationModel o = JSON.parseObject(arg0, PhoneVerificationModel
                                .class);
                        if (TextUtils.equals(o.getStatus(), "1")) {
                            UIHelper.showToast(getActivity().getApplicationContext(),
                                    o.getMessage());
                        } else {
                            UIHelper.showToast(getActivity().getApplicationContext(),
                                    o.getMessage());
//							setVerfyFailCode(o.getCode());
                            setMyCountTimerCancel();
                        }
                    }
                });
    }

    /**
     * 获取验证码,手机验证码,邮箱验证码
     *
     * @Params :
     * @Author :songyumei
     * @Date :2017/8/8
     */
    private void getPhoneVerificationCode() {
        String phonenumber = getPhoneEmail();
        if (TextUtils.isEmpty(getPhoneEmail())) {
            UIHelper.showToast(getActivity().getApplicationContext(), getString(R
                    .string.phone_email_is_empty));
            return;
        }
        if (!CommonUtil.isNetConnect(getActivity())) {
            UIHelper.showToast(getActivity().getApplicationContext(), getString(cn.com.pyc.pbb
                    .reader.R.string.network_not_available));
            return;
        }
        if (isPhoneRegister) {
            if (!StringUtil.isMobileNO(phonenumber)) {
                UIHelper.showToast(getActivity().getApplicationContext(), "请输入正确的手机号");
                return;
            }
        } else {
            //验证邮箱是否正确
            if (!isEmail(phonenumber) || phonenumber.toCharArray().length > 50) {
                UIHelper.showToast(getActivity().getApplicationContext(), "邮箱格式不正确或名称过长");
            }
        }

        btnVerificationCode
                .setBackgroundDrawable(getResources().getDrawable(cn.com.pyc.pbb.reader.R
                        .drawable.verification_bg));
        btnVerificationCode.setEnabled(false);
        btnVerificationCode.setTextColor(getResources().getColor(cn.com.pyc.pbb.reader.R.color
                .white));
        mc = new MyCount(60000, 1000);
        mc.start();
        phoneOrEmail.setEnabled(false);

        showBgLoading(getActivity());
        getClientToken(userInfo, phonenumber);
    }

    /**
     * 手机验证码的URL
     *
     * @Params :
     * @Author :songyumei
     * @Date :2017/8/9
     */
    private String getPhoneVerificationCode2() {
        return Constant.UserSourceHost + "api/v1/userphonecode";
    }

    /**
     * 邮箱验证码的URL
     *
     * @Params :
     * @Author :songyumei
     * @Date :2017/8/9
     */
    private String getEmailVerificationCodeUrl() {//http://api.pyc.com.cn/api/v1/useremailcode
        return Constant.UserSourceHost + "api/v1/useremailcode";
    }

    private String getPhoneEmail() {
        return phoneOrEmail.getText().toString().trim();
    }

    private String getpassword() {
        return edtPsd.getText().toString().trim();
    }

    private String getVerificationCode() {
        return verificationCode.getText().toString().trim();
    }

    /**
     * @Params :
     * @Author :songyumei
     * @Date :2017/8/7
     */
    private void qqLogin() {
        //QQ登录
        Toast.makeText(getActivity(), "qq登录", Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putString(GlobalIntentKeys.BUNDLE_DATA_TYPE, QqFragment.TYPE_FIND_KEY);
        changeFragment(Pbb_Fields.TAG_KEY_QQ, bundle);
    }

    @Override
    protected void findViewAndSetListeners(View v) {
        tv_phone_email = (TextView) v.findViewById(R.id.phone_email_register);
        tv_phone_email.setOnClickListener(this);
        phoneOrEmail = (EditText) v.findViewById(R.id.fkr_edt_phoneoremail);
        verificationCode = (EditText) v.findViewById(R.id.fkr_edt_verification_code);
        btnVerificationCode = (Button) v.findViewById(R.id.fkr_btn_verification_code);
        edtPsd = (EditText) v.findViewById(R.id.fkr_edt_password);
        v.findViewById(R.id.fkr_txt_old_user).setOnClickListener(this);
        v.findViewById(R.id.fkr_btn_register).setOnClickListener(this);
        v.findViewById(R.id.fkr_btn_register).setOnClickListener(this);
        v.findViewById(R.id.fkr_btn_register).setOnClickListener(this);
        v.findViewById(R.id.fkr_btn_verification_code).setOnClickListener(this);
        v.findViewById(R.id.isAccept).setOnClickListener(this);

        phone = (Button) v.findViewById(R.id.phone);
        email = (Button) v.findViewById(R.id.email);
        phone.setOnClickListener(this);
        email.setOnClickListener(this);

        //qq登录
        qq_login = (ImageView) v.findViewById(R.id.qq_login);
        qq_login.setOnClickListener(this);
        String type = getArguments().getString(GlobalIntentKeys.BUNDLE_DATA_TYPE);
        if (type.equals(TYPE_NICK)) {
            v.findViewById(R.id.fkr_lyt_register).setVisibility(View.VISIBLE);
//            ((KeyActivity) getActivity()).setMyTitle("注册");
        } else {
            ((KeyActivity) getActivity()).setMyTitle("隐私空间");
            v.findViewById(R.id.fkr_lyt_register).setVisibility(View.VISIBLE);
        }

        phoneOrEmail.setInputType(isPhoneRegister ? TYPE_CLASS_NUMBER : TYPE_CLASS_TEXT);
    }

    /**
     * @Description: (在fragment hide或者show时调用)
     * @author 李巷阳
     * @date 2016/11/18 17:07
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            String type = getArguments().getString(GlobalIntentKeys.BUNDLE_DATA_TYPE);
            if (type.equals(TYPE_NICK)) {
//                ((KeyActivity) getActivity()).setMyTitle("注册");
            } else {
                ((KeyActivity) getActivity()).setMyTitle("隐私空间");
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        WidgetTool.changVisible(phoneOrEmail, isChecked);
        WidgetTool.changVisible(edtPsd, isChecked);
    }

    /* 定义一个倒计时的内部类 */
    class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            setMyCountTimerCancel();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btnVerificationCode.setText(millisUntilFinished / 1000 + "秒");
        }
    }

    private void setMyCountTimerCancel() {
        btnVerificationCode.setEnabled(true);
        // 设置手机号可以编辑setFocusable
        phoneOrEmail.setEnabled(true);
        if (this.isAdded()) {
            btnVerificationCode
                    .setBackgroundDrawable(getResources().getDrawable(cn.com.pyc.pbb.reader.R
                            .drawable.xml_verification));
        }
        btnVerificationCode.setText("");
        countdowntimer_cancel();
    }

    private void countdowntimer_cancel() {
        if (mc != null) {
            mc.cancel();
            mc = null;
        }
    }

    private boolean outDate(String time) {
        if (TextUtils.isEmpty(time)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String systemTime = sdf.format(new Date()).toString();


        Date begin = null;
        Date end = null;
        try {
            begin = sdf.parse(time);
            end = sdf.parse(systemTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long between = (end.getTime() - begin.getTime()) / 1000;// 除以1000是为了转换成秒

        if (between / 60 >= 5) // 如果超过5分钟
        {
            return true;
        }

        return false;
    }

}
