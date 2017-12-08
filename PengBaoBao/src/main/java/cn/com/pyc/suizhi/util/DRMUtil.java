package cn.com.pyc.suizhi.util;

import android.os.Build;
import android.os.Bundle;

import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.util.DeviceUtil;

import cn.com.pyc.suizhi.common.SZConstant;

/**
 * App应用工具类
 */
public class DRMUtil {
    /**
     * 获取设备信息
     *
     * @return bundle
     */
    public static Bundle getEquipmentInfos() {
        Bundle bundle = new Bundle();
        bundle.putString("username", SZConstant.getName()); // 必传
        bundle.putString("IMEI", Constant.TOKEN); // 必传
        bundle.putString("opVersion", Build.VERSION.RELEASE); // 必传
        bundle.putString("SDKversion", "SDK " + Build.VERSION.SDK_INT); // 必传
        bundle.putString("system", "ANDROID");
        bundle.putString("board", Build.BOARD);
        bundle.putString("device", Build.DEVICE);
        bundle.putString("host", Build.HOST);
        bundle.putString("model", Build.MODEL);
        bundle.putString("serial", Build.SERIAL);
        bundle.putString("hardware", Build.HARDWARE);
        bundle.putString("manufacturer", Build.MANUFACTURER);
        bundle.putString("release", "android " + Build.VERSION.RELEASE);
        bundle.putString("product", Build.PRODUCT);
        bundle.putString("wifimac", DeviceUtil.getLocalMacAddress(SZApplication.getInstance()));
        return bundle;
    }

    /**
     * 标示：未登录时第一次进入APP,在登陆成功后=false <br/>
     * 如果已经登录，保存密码的情况下，isFirstToMain值为true.
     */
    public static boolean isFirstToMain = true;
    // 插入数据
    public static boolean isInsertDRMData = true;

    /* outline目录位置 */
    public static int OUTLINE_POSITION = 0;

    // 解析完成刷新界面
    public static final String BROADCAST_PARSER_OVER_RELOAD = "com.drm.parser_over_reload";
    // 发送广播通知主界面，停止下载，清除数据，关闭主界面。
    public static final String BROADCAST_CLOSE_ACTIVITY = "com.drm.close_activity";
    // 发送广播通知主界面，停止下载，清除数据，关闭主界面。
    public static final String BROADCAST_CLOSE_ACTIVITY2 = "com.drm.close_activity2";
    // 清除缓存后，重发送广播，通知main页面重新加载内容
    public static final String BROADCAST_CLEAR_DOWNLOADED = "com.drm.clear_downloaded";
    // 删除单个专辑
    public static final String BROADCAST_CLEAR_DOWNLOADED_ALBUM = "com.drm" +
            ".clear_downloaded_album";
    // 音乐进度
    public static final String BROADCAST_MUSIC_PROGRESS = "com.drm.receiver.Music_Progress";
    // 第一次进入显示总时间
    public static final String BROADCAST_MUSIC_OBTAIN_TIME = "com.drm.receiver" +
            ".Music_Obtain_Time";
    // 音乐定时
    public static final String BROADCAST_MUSIC_TIMER = "com.drm.receiver.Music_Timer";
    public static final String BROADCAST_MUSIC_TIMER_END = "com.drm.receiver.Music_Timer_End";

    /**
     * 根据cpu的核心数，判断同时下载的任务数量
     */
    public static int getTaskCount() {
        final int LOW_COUNT = 2;
        final int MIDDLE_COUNT = 3;
        final int HIGH_COUNT = 4;
        int cpu = DeviceUtil.getCPUCoresNum();
        if (cpu > 4) // 大于等于4核，小于等于8核
            return MIDDLE_COUNT;
        if (cpu > 8) // 大于8核
            return HIGH_COUNT;
        return LOW_COUNT;
    }

}