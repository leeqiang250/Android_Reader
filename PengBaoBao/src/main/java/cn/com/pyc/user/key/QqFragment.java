package cn.com.pyc.user.key;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.qlk.util.base.BaseActivity;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.util.SecurityUtil;
import com.tencent.connect.auth.QQAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;

import java.util.HashMap;

import cn.com.pyc.base.PbbBaseFragment;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.conn.UserConnect;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

public class QqFragment extends PbbBaseFragment {
    // 这些业务都要重新走登录流程的
    // 注意：每增加一个type，则要在getType()中更改
    public static final String TYPE_BIND = "bind";
    public static final String TYPE_REGISTER = "register";
    public static final String TYPE_FIND_KEY = "find_key";
    // public static final String TYPE_GET_NICK = "get_nick";

    private String openId;
    private String nick;

    private UserDao db = UserDao.getDB(getActivity());
    private UserInfo userInfo;
    private String qqClientToken;
    private String userToken;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        userInfo = db.getUserInfo();
        qqIn();
    }

    // QQ快登1103998765
    public static final String APP_ID = "100569483";
    // public static final String APP_KEY = "04f4c2e4364e65d496efd1629b79e9c3";
    // public static final String APP_ID = "1103998765";
    // public static final String APP_KEY = "ed202e91115537244fe17b8bfa883c6c";
    private Tencent mTencent;
    private QQAuth mQQAuth;
    private com.tencent.connect.UserInfo mInfo;

    private void qqOut(boolean success) {
        // String str1 = getType().equals(TYPE_BIND) ? "绑定" : "注册";
        // String str1 = "操作";
        // String str2 = success ? "成功！" : "失败！";
        // GlobalToast.toastShort(getActivity(), str1 + str2);

        String str = success ? "操作成功" : "该QQ号还未绑定,无法用于登录！请使用用户名／邮箱登录。";

        qqExit();
    }

    private void qqExit() {
        BaseActivity.UIHandler.post(new Runnable() {
            @Override
            public void run() {
                getActivity().onBackPressed();
            }
        });
    }

    private void qqIn() {
        mQQAuth = QQAuth.createInstance(APP_ID, getActivity());
        mTencent = Tencent.createInstance(APP_ID, getActivity());
        // mTencent = Tencent.createInstance(APP_ID, getApplicationContext());

        if (!mQQAuth.isSessionValid()) {
            // mQQAuth.login(this, "all", loginListener);
            mQQAuth.login(getActivity(), "all", loginListener);
            mTencent.loginWithOEM(getActivity(), "all", loginListener, "10000144", "10000144", "xxxx");
        } else {
            qqOut(false);
        }
    }

    private IUiListener loginListener = new IUiListener() {
        @Override
        public void onError(UiError arg0) {
            qqOut(false);
        }

        @Override
        public void onCancel() {
            qqExit();
        }

        @Override
        public void onComplete(Object arg0) {
            JSONObject json = (JSONObject) arg0;
            try {
                openId = json.getString("openid");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(openId)) {
                mInfo = new com.tencent.connect.UserInfo(getActivity(), mQQAuth.getQQToken());
                mInfo.getUserInfo(nickListener);
            } else {
                qqOut(false);
            }
        }
    };

    private IUiListener nickListener = new IUiListener() {

        @Override
        public void onError(UiError arg0) {
            commit();
        }

        @Override
        public void onComplete(Object arg0) {
            try {
                nick = ((JSONObject) arg0).getString("nickname");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            commit();
        }

        @Override
        public void onCancel() {
            commit();
        }
    };

    private void commit() {
        if (TextUtils.isEmpty(nick)) {
            nick = "";
        }
        if (TextUtils.isEmpty(openId)) {
            qqOut(false);
            return;
        }

        String type = getArguments().getString(GlobalIntentKeys.BUNDLE_DATA_TYPE);
        if (type.equals(TYPE_BIND)) {
            showLoading(getActivity());
            GlobalTask.executeNetTask(getActivity(), bindTask);
        } else if (type.equals(TYPE_REGISTER)) {
            showLoading(getActivity());
            GlobalTask.executeNetTask(getActivity(), registerTask);
        } else if (type.equals(TYPE_FIND_KEY)) {
            showLoading(getActivity());
            GlobalTask.executeNetTask(getActivity(), findKeyTask);
        } else {
            // do nothing
        }
    }

    private Runnable bindTask = new Runnable() {
        @Override
        public void run() {
            //			getQQClientToken(userInfo);
            //			new UserConnect(getActivity()).bindQQ(nick, openId, true, true);
            getQQbindUserToken(userInfo);


            qqExit();
        }


    };


    private void getQQbindUserToken(final UserInfo userInfo) {
        String userTokenUrl = Constant.UserTokenHost;

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "password");
        bundle.putString("username", userInfo.getUserName());
        if (!TextUtils.isEmpty(userInfo.getPsd())) {
            bundle.putString("password", userInfo.getPsd());
        } else {
            bundle.putString("password", "n|" + XCoder.getHttpEncryptText(userInfo.getUserName()));
        }

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(PhoneInfo.testID + ":" + PhoneInfo.testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.post(userTokenUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    userToken = (String) object.get("access_token");

                    bindQQ(userInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFinished() {
                hideLoading();
                // TODO Auto-generated method stub
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void bindQQ(final UserInfo userInfo) {
        String userTokenUrl = Constant.UserSourceHost + "api/v1/userqq";
        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("QQNick", nick);
        bundle.putString("openid", openId);
        bundle.putString("type", "bind");

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + userToken);

        headers.put("Content-Type", "application/json");

        GlobalHttp.post(userTokenUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    String status = (String) object.get("Status");
                    String Message = (String) object.get("Message");
                    if (status.equals("1")) {
                        userInfo.setQqBinded(true);
                        userInfo.setQqBinded(1);
                        db.saveUserInfo(userInfo);

                        GlobalToast.toastCenter(getActivity(),"QQ绑定成功");
                    } else {
                        GlobalToast.toastCenter(getActivity(),Message);
                    }


                } catch (JSONException e) {
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

            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
            }
        });


    }

    private Runnable registerTask = new Runnable() {
        @Override
        public void run() {
            new UserConnect(getActivity()).qqRegister(nick, openId, true, true);
            qqExit();
        }
    };

    private Runnable findKeyTask = new Runnable() {
        @Override
        public void run() {
            getQQClientToken(userInfo);

			/*
             * UserResult ur = new UserConnect(getActivity()).findKeyBackByQQ(
			 * openId, false, true); if (ur.succeed()) { startActivity(new
			 * Intent(getActivity(), KeySuccessActivity.class)); } else { if
			 * (!ur.isQQBinded()) { BaseActivity.UIHandler.post(new Runnable() {
			 * 
			 * @Override public void run() {
			 * changeFragment(KeyActivity.TAG_KEY_QQ_FAILURE); } });
			 * 
			 * } } qqExit();
			 */
        }
    };

    private void getQQClientToken(final UserInfo userInfo2) {
        // TODO Auto-generated method stub
        System.out.println("getQQClientToken方法执行.............");

        String qqclientTokenUrl = Constant.UserTokenHost;
        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "client_credentials");
        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(PhoneInfo.testID + ":" + PhoneInfo.testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");


        GlobalHttp.post(qqclientTokenUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    qqClientToken = (String) object.get("access_token");

                    getQQState(userInfo2);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinished() {
                // TODO Auto-generated method stub
                hideBgLoading();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                // TODO Auto-generated method stub
                hideBgLoading();
                System.out.println(arg0.getMessage());
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void getQQState(final UserInfo userInfo2) {

        String uidUrl = Constant.UserSourceHost + "api/v1/userloginQQ";

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("openid", openId);

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + qqClientToken);

        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.get(uidUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {

                System.out.println("QQState_Success--------------");
                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    String status = (String) object.get("Status");
                    if (status.equals("1")) {

                        String logName = (String) object.get("Result");
                        userInfo2.setUserName(logName);

                        GlobalToast.toastShort(getActivity(), (String) object.get("Message"));

                        getUID(userInfo2);

                        // startActivity(new Intent(getActivity(),
                        // KeySuccessActivity.class));

                    } else if (status.equals("0")) {
                        GlobalToast.toastShort(getActivity(), (String) object.get("Message"));
                        getActivity().onBackPressed();
                        hideLoading();
                        return;
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

            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
                hideBgLoading();
            }
        });
    }

    // 获取UID
    private void getUID(final UserInfo uinfo) {

        System.out.println("getUID方法执行。。。。。。。。。。。。。。。。。");

        String uidUrl = Constant.UserSourceHost + "api/v1/useruidkey";

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("logname", uinfo.getUserName());

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + qqClientToken);

        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.get(uidUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {

                System.out.println("UID_Success--------------");
                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    String Result = (String) object.get("Result");

                    byte[] byteResult = Result.getBytes();

                    if (!byteResult.equals(null)) {

                        uinfo.setUid(byteResult);

                        System.out.println("UID:" + uinfo.getUid());

                        db.saveUserInfo(uinfo);

                        // GlobalToast.toastShort((KeyActivity) getActivity(),
                        // "登录成功!");

                        getUserInfo(uinfo);
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
                hideBgLoading();
                System.out.println("UID_Error--------------");
                System.out.println("_______________" + arg0.getMessage());
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void getUserInfo(final UserInfo userInfo2) {
        System.out.println("getUserInfo方法执行。。。。。。。。。。。。。。。。。。");

        String url = Constant.UserSourceHost + "api/v1/userinfo";

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("username", userInfo2.getUserName());

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + qqClientToken);

        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.get(url, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {

                System.out.println("USERINFO_Success-----------------");

                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    String status = (String) object.get("Status");
                    if (status.equals("1")) {

                        JSONObject Result = (JSONObject) object.get("Result");

                        userInfo2.setUserName((String) Result.get("UserName"));

                        userInfo2.setNick((String) Result.get("UserNick"));

                        userInfo2.setPhone((String) Result.get("MobilePhone"));
                        if (Result.get("MobileStatus").equals("1")) {
                            userInfo2.setPhoneBinded(true);
                            userInfo2.setPhoneBinded(1);
                        } else if (Result.get("MobileStatus").equals("0")) {
                            userInfo2.setPhoneBinded(false);
                            userInfo2.setPhoneBinded(0);
                        }

                        userInfo2.setEmail((String) Result.get("Email"));
                        if (Result.get("EmailStatus").equals("1")) {
                            userInfo2.setEmailBinded(true);
                            userInfo2.setEmailBinded(1);
                        } else if (Result.get("EmailStatus").equals("0")) {
                            userInfo2.setEmailBinded(false);
                            userInfo2.setEmailBinded(0);
                        }

                        userInfo2.setQqNick((String) Result.get("QQNick"));

                        userInfo2.setQqBinded(true);
                        userInfo2.setQqBinded(1);

                        if (Result.get("isEnterpriseChild").equals("1")) {
                            userInfo2.setStatus(1);
                        } else if (Result.get("isEnterpriseChild").equals("0")) {
                            userInfo2.setStatus(0);
                        }

                        String password = (String) Result.get("Password");
                        System.out.println("解密前的密码==================" + password);
                        // 解密服务器返回的密码
                        userInfo2.setPsd(XCoder.getHttpDecryptText((String) Result.get("Password")));

                        System.out.println("解密后密码==================" + userInfo2.getPsd());

                        db.saveUserInfo(userInfo2);

                        startActivity(new Intent(getActivity(), KeySuccessActivity.class));

                        // GlobalToast.toastShort(getActivity(),
                        // "用户信息已保存");

                    } else if (status.equals("0")) {
                        GlobalToast.toastShort(getActivity(), (String) object.get("Message"));
                        return;
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinished() {
                // TODO Auto-generated method stub
                hideBgLoading();

            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                // TODO Auto-generated method stub
                System.out.println("USERINFO_Error-----------------");
                System.out.println("----------" + arg0.getMessage());
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mQQAuth != null) {
            mQQAuth.logout(getActivity());
        }
        if (mTencent != null) {
            mTencent.logout(getActivity());
        }
    }

}
