package cn.com.pyc.connect;

import android.os.Bundle;
import android.text.TextUtils;

import com.sz.mobilesdk.util.SecurityUtil;

import org.xutils.common.Callback;

import java.util.HashMap;

import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2016/12/7 14:40
 */
public class NetworkRequest {

    // 获取Client的token。
    public static void getClientToken(final CallBack cbk) {
        String clientTokenUrl = Constant.UserTokenHost;
        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "client_credentials");
        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(PhoneInfo.testID + ":" + PhoneInfo.testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        GlobalHttp.post(clientTokenUrl, bundle, headers, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String arg0) {

                cbk.onSuccess(arg0);
            }

            @Override
            public void onFinished() {
                cbk.onFinished();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                cbk.onError();
            }


            @Override
            public void onCancelled(CancelledException arg0) {
                cbk.onCancelled(arg0);
            }
        });
    }

    // 获取User的token
    public static void getUserToken(final UserInfo userInfo, final CallBack cbk) {
        {
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

            GlobalHttp.post(userTokenUrl, bundle, headers, new Callback.CommonCallback<String>() {

                @Override
                public void onSuccess(String arg0) {
                    cbk.onSuccess(arg0);
                }


                @Override
                public void onFinished() {
                    cbk.onFinished();
                }

                @Override
                public void onError(Throwable arg0, boolean arg1) {
                    cbk.onError();
                }

                @Override
                public void onCancelled(Callback.CancelledException arg0) {
                    cbk.onCancelled(arg0);
                }
            });

        }
    }


    // 通过ClientToken + 用户名 + 密码登陆  获取用户信息
    public static void logIn(final String email, final String psd, final String clientToken, final CallBack cbk) {
        String logInUrl = Constant.UserSourceHost + "api/v1/userlogin";
        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("username", email);
        bundle.putString("password", XCoder.getHttpEncryptText(psd));
        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + clientToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");


        GlobalHttp.post(logInUrl, bundle, headers, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String arg0) {
                cbk.onSuccess(arg0);
            }

            @Override
            public void onFinished() {
                cbk.onFinished();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                cbk.onError();
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                cbk.onCancelled(arg0);
            }
        });

    }

    //  用户名 + ClientToken  获取UID
    public static void getUID(final UserInfo uinfo, final String clientToken, final CallBack cbk) {
        String uidUrl = Constant.UserSourceHost + "api/v1/useruidkey";
        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("logname", uinfo.getUserName());
        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + clientToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.get(uidUrl, bundle, headers, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String arg0) {
                cbk.onSuccess(arg0);
            }

            @Override
            public void onFinished() {
                cbk.onFinished();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                cbk.onError();
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                cbk.onCancelled(arg0);
            }
        });
    }

    // 获取用户信息并保存
    public static void getUserInfo(final UserInfo uinfo, final String clientToken, final CallBack cbk) {
        String url = Constant.UserSourceHost + "api/v1/userinfo";
        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("username", uinfo.getUserName());
        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + clientToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        // 请求头
        GlobalHttp.get(url, bundle, headers, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String arg0) {
                cbk.onSuccess(arg0);
            }

            @Override
            public void onFinished() {
                cbk.onFinished();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                cbk.onError();
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                cbk.onCancelled(arg0);
            }
        });

    }


    // 获取用户信息并保存
    public static void getUserMoney(String userToken, final CallBack cbk) {

        String url = Constant.UserSourceHost + "api/v1/usermoney";
        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + userToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        GlobalHttp.get(url, null, headers, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                cbk.onSuccess(arg0);
            }

            @Override
            public void onFinished() {
                cbk.onFinished();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                cbk.onError();
            }

            @Override
            public void onCancelled(CancelledException arg0) {
                cbk.onCancelled(arg0);
            }
        });


    }


    /*接口*/
    public interface CallBack {
        /*定义一个获取信息的方法*/
        public void onSuccess(String arg0);// 成功

        public void onFinished();// 完成

        public void onError();// 错误

        public void onCancelled(Exception arg0);// 取消
    }


}
