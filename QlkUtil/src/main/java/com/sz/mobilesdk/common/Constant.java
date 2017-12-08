package com.sz.mobilesdk.common;

import android.content.Context;
import android.util.DisplayMetrics;

import com.sz.mobilesdk.util.DeviceUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.StringUtil;

import java.util.Locale;

/**
 * 存储一下临时常量或值<br/>
 * <br/>
 * <br/>
 * 在APP的onCreate()开始时调用一下
 *
 * @author hudq
 */
public final class Constant {

    //xml证书加密秘钥
    public static final String XML_SECRET = "819A4D283457A9DFB704C1B4F11CB512";
    //文件秘钥加密秘钥
    public static final String FILE_KEY_SECRET = "CB28646F6674C796BFAA44FC686992AD";
    //albumInfo加密秘钥
    public static final String ALBUMINFO_SECRET = "80F008F8C906098FCE93A89B3DB2EF4E";



    /**
     * 登录方式(区分账号登录和扫码)
     *
     * @author hudq
     */
    public static class LoginConfig {
        // 默认
        public static volatile int way = Fields._SCANING;
    }

    /**
     * 设备imei号（如果imei取值为空，则会取值mac地址）
     */
    public static String TOKEN;
    public static int screenWidth;
    public static int screenHeight;

    private static final String ID = "1DEVICE1";
    private static final int DEFAULT_TASKCOUNT = 3;
    // 默认值
    public static int sTaskCount = DEFAULT_TASKCOUNT;

    /***
     * 初始化一些值，在APP的onCreate()开始时调用
     *
     * @param context Context
     */
    public static void init(Context context) {
        sTaskCount = getTaskCount();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        initDeviceId(context);
    }

    /**
     * 初始化设备号
     *
     * @param context Context
     */
    public static void initDeviceId(Context context) {
        if (!StringUtil.isEmptyOrNull(TOKEN)) {
            return;
        }
        String imei = DeviceUtil.getIMEI(context);
        String mac = DeviceUtil.getLocalMacAddress(context);
        if (StringUtil.isEmptyOrNull(imei)) {
            if (!StringUtil.isEmptyOrNull(mac)) {
                TOKEN = mac.replace(":", "-").toLowerCase(Locale.getDefault());
            } else {
                TOKEN = (String) SPUtil.get(ID, "");
            }
        } else {
            TOKEN = imei;
        }
        if (!StringUtil.isEmptyOrNull(TOKEN)) {
            SPUtil.save(ID, TOKEN);
        }
    }

    /**
     * 设置能同时下载的任务数量
     *
     * @return int
     */
    private static int getTaskCount() {
        final int LOW_COUNT = 2;
        final int MIDDLE_COUNT = 3;
        final int HIGH_COUNT = 4;
        int cpu = DeviceUtil.getCPUCoresNum();
        if (cpu > 4) return MIDDLE_COUNT;
        if (cpu > 8) return HIGH_COUNT;
        return LOW_COUNT;
    }

    /**
     * 数据库加密key
     *
     * @return String
     */
    public static String getDBCliperValue() {
        return "mykey";
    }

}
