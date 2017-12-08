package cn.com.pyc.utils;

import com.sz.help.KeyHelp;
import com.sz.mobilesdk.util.SPUtil;

public class ClearKeyUtil {

    /**
     * 登录方式所有的值的key
     */
    public static void removeKey() {
        removeUserKey();
        removeWXKey();
        removeQQKey();
    }

    /**
     * 账号密码登录保存值key
     */
    public static void removeUserKey() {
        //SPUtils.remove(DRMUtil.KEY_REMEMBER_NAME);
        //SPUtils.remove(DRMUtil.KEY_REMEMBER_PWD);
        SPUtil.remove(KeyHelp.KEY_ACCOUNT_ID);
        SPUtil.remove(KeyHelp.KEY_SUPER_TOKEN);
        SPUtil.remove(KeyHelp.KEY_SUPER_NAME);
        SPUtil.remove(KeyHelp.KEY_SELECT_PRO_ID);
    }

    /**
     * 微信登录保存值key
     */
    public static void removeWXKey() {
        SPUtil.remove(KeyHelp.KEY_WECHAT_ACCESS_TOKEN);
        SPUtil.remove(KeyHelp.KEY_WECHAT_EXPIRES_IN);
        SPUtil.remove(KeyHelp.KEY_WECHAT_OPENID);
        SPUtil.remove(KeyHelp.KEY_WECHAT_REFRESH_TOKEN);
        SPUtil.remove(KeyHelp.KEY_WECHAT_ACCESS_TOKEN);
        SPUtil.remove(KeyHelp.KEY_WECHAT_UNIONID);
    }

    /**
     * qq登录值key
     */
    public static void removeQQKey() {
        SPUtil.remove(KeyHelp.KEY_QQ_ACCESS_TOKEN);
        SPUtil.remove(KeyHelp.KEY_QQ_OPENID);
        SPUtil.remove(KeyHelp.KEY_QQ_EXPIRES_IN);
    }

}
