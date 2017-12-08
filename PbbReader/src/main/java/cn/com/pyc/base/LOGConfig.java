package cn.com.pyc.base;

import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SecurityUtil;

import java.lang.reflect.Field;

import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern._LogHttp;

/**
 * Created by hudq on 2017/1/20.
 */

public class LOGConfig {

    /**
     * 获取日志额外参数的基础参数值（只包含用户名和密码）
     *
     * @return
     */
    public static ExtraParams getBaseExtraParams() {
        ExtraParams params = new ExtraParams();
        params.account_name = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
        params.account_password = AESUtil.encrypt(SecurityUtil.decryptBASE64((String) SPUtil.get
                (Fields.FIELDS_LOGIN_PASSWORD, "")));
        return params;
    }

    public static void setLogHttpUrl(String url) {
        setField(_LogHttp.create(), "sUrl", url);
    }


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
