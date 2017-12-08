package cn.com.pyc.utils;

import android.text.TextUtils;

import com.sz.help.KeyHelp;
import com.sz.mobilesdk.util.SPUtil;

/**
 * Created by hudaqiang on 2017/9/21.
 */

public class LoginControll {

    /**
     * 检查登录
     *
     * @return
     */
    public static boolean checkLogin() {
        String accountId = (String) SPUtil.get(KeyHelp.KEY_ACCOUNT_ID, "");
        String token = (String) SPUtil.get(KeyHelp.KEY_SUPER_TOKEN, "");
        return !TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token);
    }

}
