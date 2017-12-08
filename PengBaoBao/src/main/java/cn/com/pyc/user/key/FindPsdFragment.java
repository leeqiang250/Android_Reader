package cn.com.pyc.user.key;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.PbbBaseFragment;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.widget.PycEditText;

/**
 * 忘记密码-找回密码界面
 *
 * @author Administrator
 */
public class FindPsdFragment extends PbbBaseFragment implements OnClickListener {
    private UserDao db = UserDao.getDB(getActivity());
    private UserInfo userInfo;
    private String userToken;
    private String clientToken;

    private RadioGroup rGroup;

    private LinearLayout llayoutPhonePart;
    private LinearLayout llayoutEmailPart;

    private EditText editPhone;
    private EditText editPhoneCode;
    private EditText editPhoneNewPsd;
    private EditText editPhoneNewPsdAgain;

    private EditText editEmail;
    private EditText editEmailCode;
    private EditText editEmailNewPsd;
    private EditText editEmailNewPsdAgain;

    private Button btnGetPhoneSecurity;
    private Button btnCommitPhoneInfo;
    private Button btnGetEmailSecurity;
    private Button btnCommitEmailInfo;

    private Handler handler;
    private String securityCode;
    private String str;
    public static final String SECURITY_CODE = "security_code";
    private static short Security_Time = 60;
    private static long Exit_Time;
    private final HashMap<String, String> time = new HashMap<String, String>();
    private final HashMap<String, String> masgid = new HashMap<String, String>();
    private Button phone;
    private Button email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_key_find_psd1, null);
        init_data();
        init_view(v);
        init_listener();


        findViewAndSetListeners(v);

		/*
         * final PycEditText edtEmail = ((PycEditText) v
		 * .findViewById(R.id.fkfp_edt_email));
		 * v.findViewById(R.id.fkfp_btn_find).setOnClickListener( new
		 * OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { String email =
		 * edtEmail.getText().toString().trim(); if (!userInfo.isKeyNull()) {
		 * email = ""; // 与服务器约定：如果有钥匙，则不需要邮箱，直接从服务器检索 } findPsdBack(email); }
		 * }); if (!userInfo.isKeyNull()) { TextView explain = (TextView)
		 * v.findViewById(R.id.fkfp_txt_prompt);
		 * explain.setText("检测到已有账号，将自动检索绑定的邮箱，您可以不填写");
		 * explain.setTextSize(12); explain.setTextColor(Color.BLUE); } else {
		 * edtEmail.requestFocus(); ((KeyActivity)
		 * getActivity()).showKeyboard(); }
		 */

        return v;
    }

    private void init_data() {
        userInfo = db.getUserInfo();
        handler = new Handler();
    }

    private void init_view(View v) {
        rGroup = (RadioGroup) v.findViewById(R.id.radioGroup1);
        llayoutPhonePart = (LinearLayout) v.findViewById(R.id.ll_phone_part);
        llayoutEmailPart = (LinearLayout) v.findViewById(R.id.ll_email_part);

        editPhone = (EditText) v.findViewById(R.id.edt_phone);
        editPhoneCode = (EditText) v.findViewById(R.id.edt_phone_security);
        editPhoneNewPsd = (EditText) v.findViewById(R.id.edt_phone_newPsd);
        editPhoneNewPsdAgain = (EditText) v.findViewById(R.id.edt_phone_newPsd_again);

        editEmail = (EditText) v.findViewById(R.id.edt_email);
        editEmailCode = (EditText) v.findViewById(R.id.edt_email_security);
        editEmailNewPsd = (EditText) v.findViewById(R.id.edt_email_newPsd);
        editEmailNewPsdAgain = (EditText) v.findViewById(R.id.edt_email_newPsd_again);

        btnGetPhoneSecurity = (Button) v.findViewById(R.id.btn_get_phone_security);
        btnCommitPhoneInfo = (Button) v.findViewById(R.id.btn_commit_phoneInfo);
        btnGetEmailSecurity = (Button) v.findViewById(R.id.btn_get_email_security);
        btnCommitEmailInfo = (Button) v.findViewById(R.id.btn_commit_emailInfo);

        phone = (Button) v.findViewById(R.id.phone);
        email = (Button) v.findViewById(R.id.email);
        phone.setOnClickListener(this);
        email.setOnClickListener(this);

        //确认按钮的显示监听状态
        btnCommitPhoneOrEmail(editPhone,true);
        btnCommitPhoneOrEmail(editPhoneCode,true);
        btnCommitPhoneOrEmail(editPhoneNewPsd,true);
        btnCommitPhoneOrEmail(editPhoneNewPsdAgain,true);

        btnCommitPhoneOrEmail(editEmail,false);
        btnCommitPhoneOrEmail(editEmailCode,false);
        btnCommitPhoneOrEmail(editEmailNewPsd,false);
        btnCommitPhoneOrEmail(editEmailNewPsdAgain,false);
    }

    private void btnCommitPhoneOrEmail(EditText editPhone, final boolean isPhone) {
        editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                btnPhoneReconfrim(isPhone);
            }
        });
    }

    private void btnPhoneReconfrim(boolean isPhone) {
        if (isPhone) {
            if (isEmpty(isPhone)) {
                btnCommitPhoneInfo.setBackground(getActivity().getResources().getDrawable(R.drawable.xml_click_btn));
                btnCommitPhoneInfo.setTextColor(getActivity().getResources().getColor(R.color.black));
            }else {
                btnCommitPhoneInfo.setBackground(getActivity().getResources().getDrawable(R.drawable.xml_unclick_btn));
                btnCommitPhoneInfo.setTextColor(getActivity().getResources().getColor(R.color.gray));
            }
        }else {
            if (isEmpty(isPhone)) {
                btnCommitEmailInfo.setBackground(getActivity().getResources().getDrawable(R.drawable.xml_click_btn));
                btnCommitEmailInfo.setTextColor(getActivity().getResources().getColor(R.color.black));
            }else {
                btnCommitEmailInfo.setBackground(getActivity().getResources().getDrawable(R.drawable.xml_unclick_btn));
                btnCommitEmailInfo.setTextColor(getActivity().getResources().getColor(R.color.gray));
            }
        }
    }

    private boolean isEmpty(boolean isPhone) {
        if(isPhone) {
            return !TextUtils.isEmpty(editPhone.getText().toString().trim())
                    && !TextUtils.isEmpty(editPhoneCode.getText().toString().trim())
                    &&!TextUtils.isEmpty(editPhoneNewPsd.getText().toString().trim())
                    && !TextUtils.isEmpty(editPhoneNewPsdAgain.getText().toString().trim());
        }else {
            return !TextUtils.isEmpty(editEmail.getText().toString().trim())
                    && !TextUtils.isEmpty(editEmailCode.getText().toString().trim())
                    &&!TextUtils.isEmpty(editEmailNewPsd.getText().toString().trim())
                    && !TextUtils.isEmpty(editEmailNewPsdAgain.getText().toString().trim());
        }
    }

    private void init_listener() {
        btnGetPhoneSecurity.setOnClickListener(this);
        btnCommitPhoneInfo.setOnClickListener(this);
        btnGetEmailSecurity.setOnClickListener(this);
        btnCommitEmailInfo.setOnClickListener(this);
        rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioPhone:
                        llayoutPhonePart.setVisibility(View.VISIBLE);
                        llayoutEmailPart.setVisibility(View.GONE);
                        break;
                    case R.id.radioEmail:
                        llayoutPhonePart.setVisibility(View.GONE);
                        llayoutEmailPart.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        });


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 获取手机验证码
            case R.id.btn_get_phone_security:
                String phone_security = getPhone();
                if (TextUtils.isEmpty(phone_security)) {
                    GlobalToast.toastShort(getActivity(), "请输入手机号");
                    return;
                }
                if (!isMobileNum(phone_security)) {
                    GlobalToast.toastShort(getActivity(), "手机号不合法");
                    return;
                }
                showLoading(getActivity());
                getPhoneClientToken(userInfo, phone_security);
                break;
            // 手机忘记密码找回
            case R.id.btn_commit_phoneInfo:
                String phone_phoneInfo = getPhone();
                String code = getVerification();
                String newPsd = getNewPsd();
                String newPsdAgain = getNewPsdAgain();

                if (TextUtils.isEmpty(phone_phoneInfo) || !isMobileNum(phone_phoneInfo)) {
                    GlobalToast.toastShort(getActivity(), "请输入合法手机号");
                    return;
                }

                if (TextUtils.isEmpty(code)) {
                    GlobalToast.toastShort(getActivity(), "请输入验证码");
                    return;
                }

                if (TextUtils.isEmpty(newPsd)) {
                    GlobalToast.toastShort(getActivity(), "密码不能为空");
                    return;
                }
                if (TextUtils.isEmpty(newPsdAgain)) {
                    GlobalToast.toastShort(getActivity(), "请再次输入新密码");
                    return;
                }
                if (!newPsd.equals(newPsdAgain)) {
                    GlobalToast.toastShort(getActivity(), "两次密码不一致");
                    return;
                }
                if (newPsd.length() < 4 || newPsd.length() > 16) {
                    GlobalToast.toastShort(getActivity(), "请输入由4-16位字母、数字或下划线组成的密码。");
                    return;
                }
                if (outDate(time.get(securityCode))) {
                    GlobalToast.toastShort(getActivity(), "验证码已过期，请重新获取");
                    return;
                }
                showLoading(getActivity());
                commitPhoneInfo(userInfo, phone_phoneInfo, code, newPsd);

                break;
            // 获取邮箱验证码
            case R.id.btn_get_email_security:
                String email_security = getEmail();
                if (TextUtils.isEmpty(email_security)) {
                    GlobalToast.toastShort(getActivity(), "请输入手机号");
                    return;
                }
                showLoading(getActivity());
                getEmailClientToken(userInfo, email_security);
                break;
            // 邮箱忘记密码找回
            case R.id.btn_commit_emailInfo:
                String email_emailInfo = getEmail();
                String codeEmail = getCodeEmail();
                String newPsdEmail = getNewPsdEmail();
                String newPsdAgainEmail = getNewPsdAgainEmail();

                if (TextUtils.isEmpty(email_emailInfo)) {
                    GlobalToast.toastShort(getActivity(), "邮箱不能为空");
                    return;
                }
                if (TextUtils.isEmpty(codeEmail)) {
                    GlobalToast.toastShort(getActivity(), "请输入验证码");
                    return;
                }
                if (TextUtils.isEmpty(newPsdEmail)) {
                    GlobalToast.toastShort(getActivity(), "密码不能为空");
                    return;
                }
                if (TextUtils.isEmpty(newPsdAgainEmail)) {
                    GlobalToast.toastShort(getActivity(), "请再次输入新密码");
                    return;
                }
                if (!newPsdEmail.equals(newPsdAgainEmail)) {
                    GlobalToast.toastShort(getActivity(), "密码不一致");
                    return;
                }
                if (newPsdAgainEmail.length() < 4 || newPsdAgainEmail.length() > 16) {
                    GlobalToast.toastShort(getActivity(), "请输入由4-16位字母、数字或下划线组成的密码。");
                    return;
                }

                if (outDate(time.get(securityCode))) {
                    GlobalToast.toastShort(getActivity(), "验证码已过期，请重新获取");
                    return;
                }
                showLoading(getActivity());
                commitEmailInfo(userInfo, email_emailInfo, codeEmail, newPsdEmail);
                break;

            case R.id.phone:
                llayoutPhonePart.setVisibility(View.VISIBLE);
                llayoutEmailPart.setVisibility(View.GONE);
                phone.setTextColor(Color.BLACK);
                phone.setBackground(getResources().getDrawable(R.drawable.xml_click_btn));
                email.setTextColor(Color.WHITE);
                email.setBackground(getResources().getDrawable(R.drawable.xml_phone_email));
                break;
            case R.id.email:
                llayoutPhonePart.setVisibility(View.GONE);
                llayoutEmailPart.setVisibility(View.VISIBLE);
                phone.setTextColor(Color.WHITE);
                phone.setBackground(getResources().getDrawable(R.drawable.xml_phone_email));
                email.setTextColor(Color.BLACK);
                email.setBackground(getResources().getDrawable(R.drawable.xml_click_btn));
                break;
            default:
                break;
        }
    }

    @NonNull
    private String getNewPsdAgainEmail() {return editEmailNewPsdAgain.getText().toString().trim();}

    @NonNull
    private String getNewPsdEmail() {return editEmailNewPsd.getText().toString().trim();}

    @NonNull
    private String getCodeEmail() {return editEmailCode.getText().toString().trim();}

    @NonNull
    private String getEmail() {return editEmail.getText().toString().trim();}

    @NonNull
    private String getNewPsdAgain() {return editPhoneNewPsdAgain.getText().toString().trim();}

    @NonNull
    private String getNewPsd() {return editPhoneNewPsd.getText().toString().trim();}

    @NonNull
    private String getVerification() {return editPhoneCode.getText().toString().trim();}

    @NonNull
    private String getPhone() {return editPhone.getText().toString().trim();}

    /**
     * @Description: (获取手机验证码)
     * 1.获取clientToken
     * 2.获取手机验证码
     * @author 李巷阳
     * @date 2016/11/18 16:11
     * @version V1.0
     */
    private void getPhoneClientToken(final UserInfo userInfo, final String phone) {

        String clientTokenUrl = Constant.UserTokenHost;

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "client_credentials");
        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(PhoneInfo.testID + ":" + PhoneInfo.testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        GlobalHttp.post(clientTokenUrl, bundle, headers, new CommonCallback<String>() {
            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    clientToken = (String) object.get("access_token");

                    getPhoneSecurity(userInfo, phone);

                    System.out.println("ClientToken=" + clientToken);

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
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void getPhoneSecurity(final UserInfo uinfo, final String phone) {


        String phoneSecurityUrl = Constant.UserSourceHost + "api/v1/userphonecode";

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("phone", phone);

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + clientToken);

        headers.put("Content-Type", "application/x-www-form-urlencoded");
        //	headers.put("Content-Type", "application/json");


        GlobalHttp.get(phoneSecurityUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                System.out.println("获取手机验证码-返回obj:" + arg0);

                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    String status = (String) object.get("Status");

                    if (status.equals("1")) {
                        System.out.println("返回的status=" + status);
                        GlobalToast.toastShort(getActivity(), "获取成功");
                        //							finish();
                        hideLoading();


                        // 当前时间
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentTime = sdf.format(new Date()).toString();
                        securityCode = "codeSend";
                        time.put(securityCode, currentTime);
                        //							masgid.put(securityCode, msgId);
                        //							str = securityCode;

                        start();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                btnCommitPhoneInfo.setTextColor(getResources().getColor(R.color.white));
                                btnCommitPhoneInfo.setClickable(true);
                            }
                        });
                    } else if (status.equals("0")) {
                        GlobalToast.toastShort(getActivity(), (String) object.get("Message"));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
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
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    /**
     * @Description: (提交手机号找回密码)
     * @author 李巷阳
     * @date 2016/11/18 16:14
     * @version V1.0
     */
    private void commitPhoneInfo(final UserInfo userinfo, final String phone, final String code, final String newpsd) {
        GlobalTask.executeNetTask(getActivity(), new Runnable() {
            @Override
            public void run() {

                String commitPhoneInfoUrl = Constant.UserSourceHost + "api/v1/userresetpasswordbyphone";

                // 请求参数
                Bundle bundle = new Bundle();
                bundle.putString("phone", phone);
                bundle.putString("code", code);
                bundle.putString("newpassword", newpsd);

                // 请求头
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + clientToken);

                //			headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Content-Type", "application/json");


                GlobalHttp.post(commitPhoneInfoUrl, bundle, headers, new CommonCallback<String>() {

                    @Override
                    public void onSuccess(String arg0) {
                        System.out.println("commitPhoneInfo返回obj:" + arg0);

                        // 解析Json
                        try {
                            JSONObject object = new JSONObject(arg0);
                            String status = (String) object.get("Status");

                            if (status.equals("1")) {
                                System.out.println("返回的status=" + status);
                                userinfo.setPhone(phone);
                                userinfo.setPhoneBinded(true);
                                userinfo.setPhoneBinded(1);
                                db.saveUserInfo(userInfo);
                                GlobalToast.toastShort(getActivity(), "成功找回");
                                getActivity().finish();
                            } else if (status.equals("0")) {
                                GlobalToast.toastShort(getActivity(), (String) object.get("Message"));
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinished() {
                        // TODO Auto-generated method stub
                        hideLoading();
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        System.out.println(arg0.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                        // TODO Auto-generated method stub
                    }
                });
            }
        });
    }

    /**
     * @Description: (通过邮箱找回密码)
     * 1.先获取clientToken。
     * 2.获取邮箱验证码。
     * @author 李巷阳
     * @date 2016/11/18 16:15
     * @version V1.0
     */
    private void getEmailClientToken(final UserInfo userInfo, final String email) {
        String clientTokenUrl = Constant.UserTokenHost;

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "client_credentials");

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(PhoneInfo.testID + ":" + PhoneInfo.testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.post(clientTokenUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    clientToken = (String) object.get("access_token");

                    getEmailSecurity(userInfo, email);

                    System.out.println("ClientToken=" + clientToken);

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
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * @Description: (获取邮箱验证码)
     * @author 李巷阳
     * @date 2016/11/18 16:16
     */
    private void getEmailSecurity(final UserInfo userInfo, final String email) {
        GlobalTask.executeNetTask(getActivity(), new Runnable() {
            @Override
            public void run() {

                String emailSecurityUrl = Constant.UserSourceHost + "api/v1/useremailcode";

                // 请求参数
                Bundle bundle = new Bundle();
                bundle.putString("email", email);

                // 请求头
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + clientToken);

                headers.put("Content-Type", "application/x-www-form-urlencoded");
                //			headers.put("Content-Type", "application/json");


                GlobalHttp.get(emailSecurityUrl, bundle, headers, new CommonCallback<String>() {

                    @Override
                    public void onSuccess(String arg0) {
                        System.out.println("获取邮箱验证码-返回obj:" + arg0);

                        // 解析Json
                        try {
                            JSONObject object = new JSONObject(arg0);
                            String status = (String) object.get("Status");

                            if (status.equals("1")) {
                                System.out.println("返回的status=" + status);
                                GlobalToast.toastShort(getActivity(), "获取成功");
                                //									finish();
                                hideLoading();


                                // 当前时间
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String currentTime = sdf.format(new Date()).toString();
                                securityCode = "codeSend";
                                time.put(securityCode, currentTime);
                                //									masgid.put(securityCode, msgId);
                                str = securityCode;

                                start();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        btnCommitEmailInfo.setTextColor(getResources().getColor(R.color.white));
                                        btnCommitEmailInfo.setClickable(true);
                                    }
                                });
                            } else if (status.equals("0")) {
                                GlobalToast.toastShort(getActivity(), (String) object.get("Message"));
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
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
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                        // TODO Auto-generated method stub
                    }
                });


            }
        });
    }

    /**
     * @Description: (通过邮箱找回密码)
     * @author 李巷阳
     * @date 2016/11/18 16:17
     * @version V1.0
     */
    private void commitEmailInfo(final UserInfo userinfo, final String email, final String codeEmail, final String newpsdEmail) {
        GlobalTask.executeNetTask(getActivity(), new Runnable() {
            @Override
            public void run() {

                String commitEmailInfoUrl = Constant.UserSourceHost + "api/v1/userresetpasswordbyemail";

                // 请求参数
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                bundle.putString("code", codeEmail);
                bundle.putString("newpassword", newpsdEmail);

                // 请求头
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + clientToken);

                //			headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Content-Type", "application/json");


                GlobalHttp.post(commitEmailInfoUrl, bundle, headers, new CommonCallback<String>() {

                    @Override
                    public void onSuccess(String arg0) {
                        System.out.println("commitEmailInfo返回obj:" + arg0);

                        // 解析Json
                        try {
                            JSONObject object = new JSONObject(arg0);
                            String status = (String) object.get("Status");

                            if (status.equals("1")) {
                                System.out.println("返回的status=" + status);
                                userinfo.setEmail(email);
                                userinfo.setEmailBinded(true);
                                userinfo.setEmailBinded(1);
                                db.saveUserInfo(userInfo);
                                GlobalToast.toastShort(getActivity(),"成功找回");
                                getActivity().finish();
                            } else if (status.equals("0")) {
                                GlobalToast.toastShort(getActivity(), (String) object.get("Message"));
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinished() {
                        // TODO Auto-generated method stub
                        hideLoading();
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        System.out.println(arg0.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                        // TODO Auto-generated method stub
                    }
                });
            }
        });
    }

    private boolean outDate(String time) {
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

    /**********************************
     * 验证手机时：获取验证码后立马返回然后再进入
     *******************************/

    private void start() {
        handler.post(refresh);
    }

    private void interrupt() {
        handler.removeCallbacks(refresh);
    }

    private void stop() {
        handler.removeCallbacks(refresh);
        handler.post(new Runnable() {
            @Override
            public void run() {
                btnGetPhoneSecurity.setBackgroundResource(R.drawable.xml_imb_green);
                btnGetEmailSecurity.setBackgroundResource(R.drawable.xml_imb_green);
                btnGetPhoneSecurity.setText("获取验证码");
                btnGetPhoneSecurity.setClickable(true);
                btnGetEmailSecurity.setText("获取验证码");
                btnGetEmailSecurity.setClickable(true);
                Security_Time = 60;
            }
        });
    }

    private Runnable refresh = new Runnable() {
        public void run() {
            if (Security_Time > 0) {
                btnGetPhoneSecurity.setBackgroundColor(Color.GRAY);
                btnGetPhoneSecurity.setText(Security_Time + "秒");
                btnGetPhoneSecurity.setClickable(false);

                btnGetEmailSecurity.setBackgroundColor(Color.GRAY);
                btnGetEmailSecurity.setText(Security_Time + "秒");
                btnGetEmailSecurity.setClickable(false);

                Security_Time--;
                handler.postDelayed(refresh, 1000);
            } else {
                btnGetPhoneSecurity.setBackgroundResource(R.drawable.xml_imb_green);
                btnGetPhoneSecurity.setText("获取验证码");
                btnGetPhoneSecurity.setClickable(true);

                btnGetEmailSecurity.setBackgroundResource(R.drawable.xml_imb_green);
                btnGetEmailSecurity.setText("获取验证码");
                btnGetEmailSecurity.setClickable(true);

                Security_Time = 60;
            }
        }
    };

    private boolean isMobileNum(String phone) {
        // Pattern pattern = Pattern.compile("1\\d{10}");
        // Matcher matcher = pattern.matcher(phone);
        // return matcher.matches();
        return phone.matches("1\\d{10}");
    }

	/*-
     * 传输结构约定：
	 * 		传入userName和email（email可以为空）
	 * 		如果两者皆空，则必须提醒“输入邮箱”
	 * 		如果userName不为空（即有钥匙）则邮箱
	 * 		可以为空
	 
	private void findPsdBack(final String email)
	{
		if (userInfo.isKeyNull() && TextUtils.isEmpty(email))
		{
			GlobalToast.toastCenter(getActivity(), "请填写账号的绑定邮箱");
			return;
		}

		GlobalTask.executeNetTask(getActivity(), new Runnable()
		{
			@Override
			public void run()
			{
				UserResult ur = new UserConnect(getActivity()).findPsdBack(
						email, false);
				if (ur.succeed())
				{
					BaseActivity.UIHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							getActivity().onBackPressed();
						}
					});
					GlobalToast.toastLong(getActivity(), ur.getSuccessReason());
				}
				else
				{
					GlobalToast.toastShort(getActivity(), ur.getFailureReason());
				}
			}
		});
	}*/
}
