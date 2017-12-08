package cn.com.pyc.suizhi.common;

import com.sz.mobilesdk.util.AESUtil;

import java.lang.reflect.Field;

import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern._LogHttp;

/**
 * 日志系统基础参数
 * <p>
 * Created by hudq on 2016/11/29.
 */

public class LogConfig {

    /**
     * 反射调用，设置请求的URL
     *
     * @param url
     */
    public static void setLogHttpUrl(String url) {
        setField(_LogHttp.create(), "sUrl", url);
    }

    /**
     * 获取基本的额外参数，包括用户名、密码、登录方式
     *
     * @return ExtraParams
     */
    public static ExtraParams getBaseExtraParams() {
        ExtraParams extraParams = new ExtraParams();
        extraParams.account_password = AESUtil.encrypt(SZConstant.getLoginPwd());
        extraParams.account_name = SZConstant.getName();
        //extraParams.login_type = getLoginType();
        return extraParams;
    }

    /**
     * 获取登录类型
     *
     * @return
     */
//    public static String getLoginType() {
//        if (SZConfig.LoginConfig.type == DrmPat.LOGIN_GENERAL) {
//            return LoginType.ACCOUNT.toString();
//        } else if (SZConfig.LoginConfig.type == DrmPat.LOGIN_WECHAT) {
//            return LoginType.WECHAT.toString();
//        } else if (SZConfig.LoginConfig.type == DrmPat.LOGIN_QQ) {
//            return LoginType.QQ.toString();
//        }
//        return LoginType.NO.toString();
//    }


    private static void setField(Object owner, String fieldName, Object value) {
        try {
            Class<?> ownerClass = owner.getClass();
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
