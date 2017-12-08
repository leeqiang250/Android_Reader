package cn.com.pyc.suizhi.common;

import com.sz.help.KeyHelp;
import com.sz.mobilesdk.util.SPUtil;

/**
 * 存储一下临时常量或值<br/>
 * <br/>
 * <br/>
 * 在APP的onCreate()开始时调用
 *
 * @author hudq
 */
public final class SZConstant {

//    public static final int CONTENT_LOGIN = 13;//我的内容
//    public static final int DISCOVER_LOGIN = 12;//点击注销登录成功后到发现界面
//    public static final int SETTING_LOGIN = 11;//没有登录状态点击设置界面的登录
//    public static int screenWidth;
//    public static int screenHeight;
//    public static String IMEI;

    private static final int DEFAULT_TASKCOUNT = 2;
    public static int sTaskCount = DEFAULT_TASKCOUNT;
    private static final String ID = "SZDEVICEID";
//    public static final int LOGIN_TYPE = 1;
//    public static final int OTHER_TYPE = 2;

    /*
     * 初始化一些值，在APP的onCreate()开始时调用
     *
     * @param context Context
     */
//    public static void init(Context context) {
//        sTaskCount = DRMUtil.getTaskCount();
//        DisplayMetrics dm = context.getResources().getDisplayMetrics();
//        screenWidth = dm.widthPixels;
//        screenHeight = dm.heightPixels;
//        //String imei = DeviceUtils.getIMEI(context);
//        //IMEI = (imei != null) ? imei : DeviceUtils.getLocalMacAddress(context)
//        //        .replace(":", "-").toLowerCase(Locale.getDefault());
//        initDeviceId(context);
//    }

    /*
     * 初始化设备号（某些特殊设备无IMEI,则使用MAC地址）
     *
     * @param context Context
     */
//    public static void initDeviceId(Context context) {
//        if (!StringUtil.isEmptyOrNull(IMEI)) {
//            return;
//        }
//        String imei = DeviceUtils.getIMEI(context);
//        String mac = DeviceUtils.getLocalMacAddress(context);
//        if (StringUtil.isEmptyOrNull(imei)) {
//            if (!StringUtil.isEmptyOrNull(mac)) {
//                IMEI = mac.replace(":", "-").toLowerCase(Locale.getDefault());
//            } else {
//                IMEI = (String) SPUtils.get(ID, "");
//            }
//        } else {
//            IMEI = imei;
//        }
//        if (!StringUtil.isEmptyOrNull(IMEI)) {
//            SPUtils.save(ID, IMEI);
//        }
//    }

    //数据库加密key值
    public static String getCipherKey() {
        return "mykey";
    }

    /**
     * 获取登录用户名（输入的用户名称）
     */
    public static String getLoginName() {
        return (String) SPUtil.get(KeyHelp.KEY_VISIT_NAME, "");
    }

    //获取保存的密码
    public static String getLoginPwd() {
        return (String) SPUtil.get(KeyHelp.KEY_VISIT_PWD, "");
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    /**
     * 获取超级用户名
     */
    public static String getName() {
        return (String) SPUtil.get(KeyHelp.KEY_SUPER_NAME, "");
    }

    /**
     * 保存超级用户名
     */
    public static boolean setName(String userName) {
        return SPUtil.save(KeyHelp.KEY_SUPER_NAME, userName);
    }

    /**
     * 获取用户的唯一id
     */
    public static String getAccountId() {
        return (String) SPUtil.get(KeyHelp.KEY_ACCOUNT_ID, "");
    }

    /**
     * 保存用户的唯一id
     */
    public static boolean setAccountId(String accountId) {
        return SPUtil.save(KeyHelp.KEY_ACCOUNT_ID, accountId);
    }

    /**
     * 获取登录token令牌
     */
    public static String getToken() {
        return (String) SPUtil.get(KeyHelp.KEY_SUPER_TOKEN, "");
    }

    /**
     * 保存token
     */
    public static boolean setToken(String token) {
        return SPUtil.save(KeyHelp.KEY_SUPER_TOKEN, token);
    }

}
