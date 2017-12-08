package cn.com.pyc.user;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.util.CommonUtil;

import java.util.Observable;

import cn.com.pyc.pbb.R;
import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.connect.NetworkRequest;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.model.ClientCredentials_Model;
import cn.com.pyc.model.Userinfo_Model;
import cn.com.pyc.model.Useruidkey_Model;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.user.key.KeyActivity;
import cn.com.pyc.user.key.QqFragment;
import cn.com.pyc.widget.PsdEditText;
import cn.com.pyc.widget.PycPsdEditText;
import cn.com.pyc.xcoder.XCoder;

public class UserInfoActivity extends ExtraBaseActivity implements OnClickListener {

    private String clientToken;
    private UserInfo userInfo;
    private UserDao db;

    private TextView g_txtEmailState;
    private TextView g_txtPhoneState; // 取消了手机验证服务
    private TextView g_txtNickState;
    private TextView g_txtQQState;
    private TextView g_txtQQNick;
    private TextView g_txtPhoneRedBag;
    private TextView g_txtEmailRedBag;
    private CheckBox cbx;

    private boolean isConnecting = false;
    private TextView username;
    private static String phonenumber;
    private static String emailnum;


    private enum InfoState {
        PHONECHECKED(phonenumber, R.color.green, R.drawable.tick_small), //
        EMAILCHECKED(emailnum, R.color.green, R.drawable.tick_small), //
        UNCHECK("未绑定", R.color.red, R.drawable.warning), //
        CONNECT("正在联网", R.color.black, 0);

        private InfoState(String text, int color, int drawable) {
            this.text = text;
            this.color = color;
            this.drawable = drawable;
        }

        final String text;
        final int color;
        final int drawable;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ViewHelp.showAppTintStatusBar(this);
        getDBUserInfo();
        init_view();
        init_listener();
        init_data();
    }


    private void init_view() {
        ((TextView) findViewById(R.id.ipt_txt_title)).setText("个人信息");
        //findViewById(R.id.ipt_imb_refresh).setVisibility(View.GONE);//暂时隐藏联网刷新按钮
        g_txtEmailState = (TextView) findViewById(R.id.aui_txt_email_state);
        g_txtPhoneState = (TextView) findViewById(R.id.aui_txt_phone_state);
        g_txtQQState = (TextView) findViewById(R.id.aui_txt_qq_state);
        g_txtQQNick = (TextView) findViewById(R.id.aui_txt_qq_nick);
        g_txtPhoneRedBag = (TextView) findViewById(R.id.aui_txt_phone_redbag);
        g_txtEmailRedBag = (TextView) findViewById(R.id.aui_txt_email_redbag);
        // g_txtKey = (TextView) v.findViewById(R.id.fui_txt_key);
        g_txtNickState = (TextView) findViewById(R.id.aui_txt_nick_state);
        g_txtNickState.setText(userInfo.getNick());
        cbx = (CheckBox) findViewById(R.id.aui_cbx_input);

        //用户
        username = (TextView) findViewById(R.id.aui_txt_user_state);
        username.setText(userInfo.getUserName());
        cbx.setChecked((boolean) PbbSP.getGSP(this).getValue(PbbSP.SP_NEED_PASSWORD, false)); // 启动或者后台进入鹏保宝是否需要密码
        // 如果是企业账户，隐藏相关界面内容
        if (userInfo.getStatus() == 1) {
            findViewById(R.id.aui_txt_phone_redbag).setVisibility(View.GONE);
            findViewById(R.id.aui_txt_email_redbag).setVisibility(View.GONE);
        }

    }

    private void init_listener() {
        findViewById(R.id.ipt_imb_refresh).setOnClickListener(this);
        findViewById(R.id.aui_lyt_email).setOnClickListener(this);
        findViewById(R.id.aui_lyt_phone).setOnClickListener(this);
        findViewById(R.id.aui_lyt_modify_psd).setOnClickListener(this);
        findViewById(R.id.aui_lyt_nick).setOnClickListener(this); // 昵称
        findViewById(R.id.aui_lyt_qq).setOnClickListener(this);
        cbx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PbbSP.getGSP(UserInfoActivity.this).putValue(PbbSP.SP_NEED_PASSWORD, isChecked);
            }
        });
    }

    private void init_data() {
        // 判断联网
        if (IsNetworking()) return;
        showBgLoading(UserInfoActivity.this);
        getClientToken();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aui_lyt_nick:
                startActivity(new Intent(this, ModifyNickActivity.class));
                break;
            case R.id.aui_lyt_email:
                checkPassword(v);
                break;
            case R.id.aui_lyt_phone:
                // if (userInfo.isPhoneBinded())
                // {
                // GlobalToast.toastShort(this, "手机已绑定！");
                // }
                // else
                // {
                // checkPassword(v);
                // }
                checkPassword(v);
                //startActivity(new Intent(g_actMain, CheckPhoneActivity.class));
                break;
            case R.id.aui_lyt_qq:
                if (userInfo.isQqBinded()) {
                    GlobalToast.toastShort(this, "QQ已绑定！");
                } else {
                    checkPassword(v);
                }
                // checkPassword(v);
                break;
            case R.id.aui_lyt_modify_psd:
                checkPassword(v);
                break;
            case R.id.ipt_imb_refresh:
                // 判断联网
                if (IsNetworking()) return;
                showBgLoading(UserInfoActivity.this);
                getClientToken();
                break;
            default:
                break;
        }
    }

    private boolean IsNetworking() {
        if (!CommonUtil.isNetConnect(UserInfoActivity.this)) {
            GlobalToast.toastCenter(UserInfoActivity.this, UserInfoActivity.this.getResources().getString(R.string.pbb_net_disconnected));
            hideBgLoading();
            return true;
        }
        return false;
    }

    private void checkPassword(final View v) {
        /**
         * 下面注释部分代码，是原来“输入密码提示窗口”，
         * 根据需求暂时注释，以防后续要求再添加回来
         */
        if (!userInfo.isPsdNull()) {
            View v1 = getLayoutInflater().inflate(R.layout.dialog_userinfo_inputpsd, null);
            final Dialog dialog = new Dialog(this, R.style.no_frame_small);
            dialog.setContentView(v1);
            dialog.show();

            final PsdEditText ppet = (PsdEditText) v1.findViewById(R.id.dui_edt_psd);
            ppet.setFocusable(true);
            ppet.setFocusableInTouchMode(true);
            ppet.requestFocus();

            Button btn = (Button) v1.findViewById(R.id.dui_btn_go);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v1) {
                    String psd = ppet.getText().toString().trim();
                    if (!TextUtils.isEmpty(psd) && psd.equals(userInfo.getPsd())) {
                        onMyClick(v);
                        dialog.dismiss();
                        dialog.cancel();
                    } else {
                        GlobalToast.toastShort(UserInfoActivity.this, "密码错误!");
                        dialog.dismiss();
                        dialog.cancel();
                        checkPassword(v);
                    }
                }
            });
        } else {
            onMyClick(v);
        }
    }

    private void onMyClick(View v) {
        switch (v.getId()) {
            case R.id.aui_lyt_email:
                startActivity(new Intent(this, BindEmailActivity.class));
                break;
            case R.id.aui_lyt_phone:
                startActivity(new Intent(UserInfoActivity.this, BindPhoneActivity.class));
                break;
            case R.id.aui_lyt_qq:
                Intent intent = new Intent(this, KeyActivity.class);
                intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_QQ);
                intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, QqFragment.TYPE_BIND);
                startActivity(intent);
                break;
            case R.id.aui_lyt_modify_psd:
                Intent intent2 = new Intent(this, ModifyPsdActivity.class);
                intent2.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, ModifyPsdActivity.TYPE_FROM_USER);
                startActivity(intent2);
                break;
            default:
                break;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        if (data.equals(ObTag.Key)) {
            getDBUserInfo();
            init_binding_display();
            // checkUserInfoStateByLocal();
        }
    }

    public void getDBUserInfo() {
        db = UserDao.getDB(this);
        userInfo = db.getUserInfo();
    }


    private void show_error() {
        GlobalToast.toastShort(UserInfoActivity.this, "对不起,获取个人信息失败。");
        hideBgLoading();
    }

    /**
     * @Description: (初始化显示)
     * @author 李巷阳
     * @date 2016/12/20 17:45
     */
    private void init_binding_display() {
        if (userInfo.isPsdNull()) // 有钥匙无密码时Checkbox不可点并灰显
        {
            cbx.setClickable(false);
            cbx.setButtonDrawable(R.drawable.cbx_grey);
        } else {
            cbx.setClickable(true);
            cbx.setButtonDrawable(R.drawable.xml_cbx_green_big);
        }
        g_txtNickState.setText(userInfo.getNick());
        if (userInfo.isPhoneBinded()) {
            phonenumber = userInfo.getPhone().substring(0, 3) + "**" + userInfo.getPhone().substring(7);
        }

        if (userInfo.isEmailBinded()) {
            String email = userInfo.getEmail();
            if ((email.substring(0,email.indexOf("@"))).length() > 6) {
                emailnum = email.substring(0,2)+"**"+email.substring(6);
            }else if ((email.substring(0,email.indexOf("@"))).length() >= 3) {
                emailnum = email.substring(0,2)+"**"+email.substring(email.indexOf("@"));
            }else {
                emailnum = email;
            }
        }
        InfoState emailState = userInfo.isEmailBinded() ? InfoState.EMAILCHECKED : InfoState.UNCHECK;
        InfoState phoneState = userInfo.isPhoneBinded() ? InfoState.PHONECHECKED : InfoState.UNCHECK;
//        InfoState qqState = userInfo.isQqBinded() ? InfoState.CHECKED : InfoState.UNCHECK;
        setState(g_txtEmailState, emailState);
        setState(g_txtPhoneState, phoneState);
//        setState(g_txtQQState, qqState);
        g_txtQQNick.setText(userInfo.getQqNick());

        g_txtPhoneRedBag.setVisibility(userInfo.isPhoneBinded() ? View.GONE : View.VISIBLE);
        String email = userInfo.getEmail().toLowerCase();
        if (userInfo.isEmailBinded() && email.endsWith(".com")) {
            g_txtEmailRedBag.setVisibility(View.GONE);
        } else {
            g_txtEmailRedBag.setVisibility(View.VISIBLE);
        }
    }

    private void setState(TextView txt, InfoState state) {
        txt.setText(state.text);
//        txt.setCompoundDrawablesWithIntrinsicBounds(state.drawable, 0, R.drawable.arrows_green, 0);
//        txt.setTextColor(state.color);
    }

    private void getClientToken() {
        NetworkRequest.getClientToken(new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    ClientCredentials_Model mClientCredentials_Model = JSONObject.parseObject(arg0, ClientCredentials_Model.class);
                    String clientToken = mClientCredentials_Model.getAccess_token();
                    getUid(clientToken);
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
                hideBgLoading();
            }
        });
    }

    private void getUid(final String clientToken) {
        NetworkRequest.getUID(userInfo, clientToken, new NetworkRequest.CallBack() {
            @Override
            public void onSuccess(String arg0) {
                try {
                    Useruidkey_Model mUseruidkey_Model = JSONObject.parseObject(arg0, Useruidkey_Model.class);
                    String status = mUseruidkey_Model.getStatus();
                    if (status.equals("1")) {
                        String Result = mUseruidkey_Model.getResult();
                        byte[] byteResult = Result.getBytes();
                        if (!byteResult.equals(null)) {
                            userInfo.setUid(byteResult);
                            getUserInfo(clientToken);
                        }
                    } else {
                        show_error();
                        return;
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

            }

            @Override
            public void onCancelled(Exception arg0) {

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
                        getDBUserInfo();
                        init_binding_display();
                    } else if (status.equals("0")) {
                        show_error();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    show_error();
                }
            }

            @Override
            public void onFinished() {
                hideBgLoading();
            }

            @Override
            public void onError() {
                show_error();
            }

            @Override
            public void onCancelled(Exception arg0) {
                hideBgLoading();
            }
        });


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
        if (!TextUtils.isEmpty((String) userLogin.getResult().getQQNick())) {
            uinfo.setQqBinded(true);
            uinfo.setQqBinded(1);
            uinfo.setQqNick((String) userLogin.getResult().getQQNick());
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
        System.out.println("解密前的密码==================" + password);
        // 解密服务器返回的密码
        uinfo.setPsd(XCoder.getHttpDecryptText(userLogin.getResult().getPassword()));
        System.out.println("解密后密码==================" + uinfo.getPsd());
    }

    //    private void checkStateByInternet() {
    //        if (isConnecting) {
    //            return;
    //        }
    //        showLoading(UserInfoActivity.this);
    //
    //        GlobalTask.executeBackground(new Runnable() {
    //            @Override
    //            public void run() {
    //                getClientToken(userInfo);
    //            }
    //
    //            private void getClientToken(final UserInfo userInfo) {
    //
    //                System.out.println("getClientToken方法执行.............");
    //                String clientTokenUrl = Constant.UserTokenHost;
    //                // 请求参数
    //                Bundle bundle = new Bundle();
    //                bundle.putString("grant_type", "client_credentials");
    //                // 请求头
    //                HashMap<String, String> headers = new HashMap<String, String>();
    //                headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(PhoneInfo.testID + ":" + PhoneInfo.testPSD));
    //                headers.put("Content-Type", "application/x-www-form-urlencoded");
    //                HttpEngine.post(clientTokenUrl, bundle, headers, new Callback.CommonCallback<String>() {
    //                    @Override
    //                    public void onSuccess(String arg0) {
    //                        // 解析Json
    //                        try {
    //                            JSONObject object = new JSONObject(arg0);
    //                            clientToken = (String) object.get("access_token");
    //                            getUID(userInfo);
    //                        } catch (JSONException e) {
    //                            e.printStackTrace();
    //                        }
    //                    }
    //
    //                    @Override
    //                    public void onFinished() {
    //                    }
    //
    //                    @Override
    //                    public void onError(Throwable arg0, boolean arg1) {
    //                        //						load_error();
    //                    }
    //
    //
    //                    @Override
    //                    public void onCancelled(CancelledException arg0) {
    //                    }
    //                });
    //            }
    //
    //            private void getUID(final UserInfo uinfo) {
    //                System.out.println("getUID方法执行。。。。。。。。。。。。。。。。。");
    //                String uidUrl = Constant.UserSourceHost + "api/v1/useruidkey";
    //                // 请求参数
    //                Bundle bundle = new Bundle();
    //                bundle.putString("logname", uinfo.getUserName());
    //
    //                HttpEngine.get(uidUrl, bundle, getHeaders(), new Callback.CommonCallback<String>() {
    //                    @Override
    //                    public void onSuccess(String arg0) {
    //                        System.out.println("UID_Success--------------");
    //                        try {
    //                            JSONObject object = new JSONObject(arg0);
    //                            String Result = (String) object.get("Result");
    //                            byte[] byteResult = Result.getBytes();
    //                            if (!byteResult.equals(null)) {
    //                                uinfo.setUid(byteResult);
    //                                getUserInfo(uinfo);
    //                            }
    //                        } catch (JSONException e) {
    //                            e.printStackTrace();
    //                        }
    //                    }
    //
    //                    @Override
    //                    public void onFinished() {
    //                        hideBgLoading();
    //                    }
    //
    //                    @Override
    //                    public void onError(Throwable arg0, boolean arg1) {
    //
    //                    }
    //
    //                    @Override
    //                    public void onCancelled(CancelledException arg0) {
    //                    }
    //                });
    //            }
    //
    //            private void getUserInfo(final UserInfo uinfo) {
    //                System.out.println("getUserInfo方法执行。。。。。。。。。。。。。。。。。。");
    //                String url = Constant.UserSourceHost + "api/v1/userinfo";
    //                // 请求参数
    //                Bundle bundle = new Bundle();
    //                bundle.putString("username", uinfo.getUserName());
    //                // 请求头
    //                HttpEngine.get(url, bundle, getHeaders(), new Callback.CommonCallback<String>() {
    //                    @Override
    //                    public void onSuccess(String arg0) {
    //                        System.out.println("USERINFO_Success-----------------");
    //                        // 解析Json
    //                        try {
    //                            JSONObject object = new JSONObject(arg0);
    //                            String status = (String) object.get("Status");
    //                            if (status.equals("1")) {
    //                                JSONObject Result = (JSONObject) object.get("Result");
    //                                uinfo.setUserName((String) Result.get("UserName"));
    //                                uinfo.setNick((String) Result.get("UserNick"));
    //                                uinfo.setPhone((String) Result.get("MobilePhone"));
    //                                if (Result.get("MobileStatus").equals("1")) {
    //                                    uinfo.setPhoneBinded(true);
    //                                    uinfo.setPhoneBinded(1);
    //                                } else if (Result.get("MobileStatus").equals("0")) {
    //                                    uinfo.setPhoneBinded(false);
    //                                    uinfo.setPhoneBinded(0);
    //                                }
    //                                uinfo.setEmail((String) Result.get("Email"));
    //                                if (Result.get("EmailStatus").equals("1")) {
    //                                    uinfo.setEmailBinded(true);
    //                                    uinfo.setEmailBinded(1);
    //                                } else if (Result.get("EmailStatus").equals("0")) {
    //                                    uinfo.setEmailBinded(false);
    //                                    uinfo.setEmailBinded(0);
    //                                }
    //                                System.out.println("邮箱=======" + Result.get("Email") + "|EmailStatus=" + Result.get("EmailStatus"));
    //
    //
    //                                if (!TextUtils.isEmpty((String) Result.get("QQNick"))) {
    //                                    uinfo.setQqBinded(true);
    //                                    uinfo.setQqBinded(1);
    //                                    uinfo.setQqNick((String) Result.get("QQNick"));
    //                                } else {
    //                                    uinfo.setQqBinded(false);
    //                                    uinfo.setQqBinded(0);
    //                                }
    //
    //
    //                                if (Result.get("isEnterpriseChild").equals("1")) {
    //                                    uinfo.setStatus(1);
    //                                } else if (Result.get("isEnterpriseChild").equals("0")) {
    //                                    uinfo.setStatus(0);
    //                                }
    //                                String password = (String) Result.get("Password");
    //                                System.out.println("解密前的密码==================" + password);
    //                                // 解密服务器返回的密码
    //                                uinfo.setPsd(XCoder.getHttpDecryptText((String) Result.get("Password")));
    //                                System.out.println("解密后密码==================" + uinfo.getPsd());
    //                                db.saveUserInfo(uinfo);
    //
    //
    //                            } else if (status.equals("0")) {
    //                                return;
    //                            }
    //                        } catch (JSONException e) {
    //                            e.printStackTrace();
    //                        }
    //                    }
    //
    //                    @Override
    //                    public void onFinished() {
    //                        hideLoading();
    //
    //                    }
    //
    //                    @Override
    //                    public void onError(Throwable arg0, boolean arg1) {
    //
    //                    }
    //
    //                    @Override
    //                    public void onCancelled(CancelledException arg0) {
    //                    }
    //                });
    //
    //            }
    //
    //            private HashMap<String, String> getHeaders() {
    //                // 请求头
    //                HashMap<String, String> headers = new HashMap<String, String>();
    //                headers.put("Authorization", "Bearer " + clientToken);
    //                headers.put("Content-Type", "application/x-www-form-urlencoded");
    //                return headers;
    //            }
    //        });
    //
    //        checkStateByLocal();
    //
    //
    //    }


    //    @Override
    //    protected void onResume() {
    //        // TODO Auto-generated method stub
    //        super.onResume();
    //        init_data();
    //    }
    // 更新userinfo界面
    //    private void checkUserInfoStateByLocal() {
    //        if (userInfo.isPsdNull()) // 有钥匙无密码时Checkbox不可点并灰显
    //        {
    //            cbx.setClickable(false);
    //            cbx.setButtonDrawable(R.drawable.cbx_grey);
    //        } else {
    //            cbx.setClickable(true);
    //            cbx.setButtonDrawable(R.drawable.xml_cbx_green_big);
    //        }
    //        UserInfo userInfo = UserDao.getDB(UserInfoActivity.this).getUserInfo();
    //        g_txtNickState.setText(userInfo.getNick());
    //
    //        InfoState emailState = userInfo.isEmailBinded() ? InfoState.CHECKED : InfoState.UNCHECK;
    //        InfoState phoneState = userInfo.isPhoneBinded() ? InfoState.CHECKED : InfoState.UNCHECK;
    //        InfoState qqState = userInfo.isQqBinded() ? InfoState.CHECKED : InfoState.UNCHECK;
    //        setState(g_txtEmailState, emailState);
    //        setState(g_txtPhoneState, phoneState);
    //        setState(g_txtQQState, qqState);
    //        g_txtQQNick.setText(userInfo.getQqNick());
    //
    //        if (userInfo.isPhoneBinded()) {
    //            g_txtPhoneRedBag.setVisibility(View.GONE);
    //        }
    //
    //        if (userInfo.isEmailBinded() && userInfo.getEmail().endsWith("@qq.com")) {
    //            g_txtEmailRedBag.setVisibility(View.GONE);
    //        }
    //    }
    //		setState(g_txtEmailState, InfoState.CONNECT);
    //		setState(g_txtPhoneState, InfoState.CONNECT);
    //		setState(g_txtQQState, InfoState.CONNECT);

    //		Runnable netTask = new Runnable() {
    //			@Override
    //			public void run() {
    //
    ////				new UserConnect(UserInfoActivity.this).synchronizedUserInfo(
    ////						true, true);
    //			}
    //		};
    //
    //		GlobalTask.executeBackground(netTask);
}
