package cn.com.pyc.suizhi.help;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.TextUtils;

import com.sz.help.KeyHelp;
import com.sz.mobilesdk.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.common.MusicMode;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.service.MusicPlayService;
import cn.com.pyc.suizhi.service.MusicViewService;

/**
 * 音乐播放控制
 * <p>
 * Created by hudq on 2017/3/24.
 */

public class MusicHelp {

    private static final String MUSIC_PLAY_MODE = "music_play_mode";    //保存播放的模式
    private static final String MUSIC_PLAY_ID = "music_play_id";        //保存当前播放的文件Id

    public static final String TIMER_COUNTDOWN = "count_down"; //传递倒计时数值TAG
    public static final String TIMER_KEY = "timer_key"; //用sharedPrefrences 保存 定时器选项

    /**
     * 初始化音乐播放Service
     *
     * @param context
     * @param myProId
     * @param myProUrl
     * @param fileId
     * @param files
     */
    public static void initMusicService(Context context, String myProId, String myProUrl, String
            fileId, List<DrmFile> files) {
        Intent service = new Intent(context, MusicPlayService.class);
        service.putExtra(KeyHelp.MPS_FILE_ID, fileId);
        service.putExtra(KeyHelp.MPS_MYPRODUCT_ID, myProId);
        service.putExtra(KeyHelp.MPS_PRODUCT_URL, myProUrl);
        service.putParcelableArrayListExtra(KeyHelp.MPS_FILES, (ArrayList<? extends
                Parcelable>) files);

        context.startService(service);
    }

    public static void play(Context context) {
        start(context, MusicMode.Status.PLAY);
    }

    public static void pause(Context context) {
        start(context, MusicMode.Status.PAUSE);
    }

    public static void continuePlay(Context context) {
        start(context, MusicMode.Status.CONTINUE);
    }

    public static void stop(Context context) {
        start(context, MusicMode.Status.STOP);
    }

    public static void release(Context context) {
        start(context, MusicMode.Status.RELEASE);
    }

    public static void next(Context context) {
        start(context, MusicMode.NEXT);
    }

    public static void previous(Context context) {
        start(context, MusicMode.PREVIOUS);
    }

    public static void progressPlay(Context context, int progress) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.putExtra(KeyHelp.MPS_OPTION, MusicMode.Status.PROGRESS);
        intent.putExtra(KeyHelp.MPS_PROGRESS, progress);
        context.startService(intent);
    }

    private static void start(Context context, int option) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.putExtra(KeyHelp.MPS_OPTION, option);
        context.startService(intent);
    }


    public static boolean setPlayMode(int playMode) {
        return SPUtil.save(MUSIC_PLAY_MODE, playMode);
    }

    public static int getPlayMode() {
        return (int) SPUtil.get(MUSIC_PLAY_MODE, MusicMode.CIRCLE);
    }

    public static boolean isCircle() {
        return MusicMode.CIRCLE == getPlayMode();
    }

    public static boolean isSingle() {
        return MusicMode.SINGLE == getPlayMode();
    }

    public static boolean isRandom() {
        return MusicMode.RANDOM == getPlayMode();
    }

    /**
     * 存储当前播放的音乐文件Id
     *
     * @param fileId
     * @return
     */
    public static boolean setCurrentPlayId(String fileId) {
        return SPUtil.save(MUSIC_PLAY_ID, fileId);
    }

    public static String getCurrentPlayId() {
        return (String) SPUtil.get(MUSIC_PLAY_ID, "");
    }

    public static boolean isSameMusic(String fileId) {
        return TextUtils.equals(getCurrentPlayId(), fileId);
    }

    /**
     * 初始化悬浮图标Service
     *
     * @param context
     * @param name
     * @param myProId
     * @param myProUrl
     * @param dataList 加载的文件列表数据
     */
    public static void initMusicView(Context context, String name,
                                     String myProId, String myProUrl,
                                     List<FileData> dataList) {
        Intent service = new Intent(context, MusicViewService.class);
        service.putExtra(KeyHelp.MVS_NAME, name);
        service.putExtra(KeyHelp.MVS_MYPROID, myProId);
        service.putExtra(KeyHelp.MVS_MYPROURL, myProUrl);
        service.putParcelableArrayListExtra(KeyHelp.MVS_FILE_LIST, (ArrayList<? extends
                Parcelable>) dataList);
        //service.putExtra(KeyHelp.MVS_FILEID, fileId);
        context.startService(service);
    }

    public static void showMusicView(Context context) {
        startMusicView(context, MusicMode.Suspend.SHOW);
    }

    public static void rotateMusicView(Context context) {
        startMusicView(context, MusicMode.Suspend.ROTATE);
    }

    /**
     * 暂停图标旋转动画
     *
     * @param context
     */
    public static void haltMusicView(Context context) {
        startMusicView(context, MusicMode.Suspend.HALT);
    }

    /**
     * 关闭悬浮音乐图标
     *
     * @param context
     */
    public static void removeMusicView(Context context) {
        startMusicView(context, MusicMode.Suspend.REMOVE);
    }

    public static void stopMusicView(Context context) {
        startMusicView(context, MusicMode.Suspend.END);
    }

    private static void startMusicView(Context context, int option) {
        Intent service = new Intent(context, MusicViewService.class);
        service.putExtra(KeyHelp.MVS_OPTION, option);
        context.startService(service);
    }

    public static void showFloatView(Context context) {
        MusicHelp.showMusicView(context);
        switch (MusicMode.STATUS) {
            case MusicMode.Status.PLAY:
            case MusicMode.Status.CONTINUE:
            case MusicMode.Status.PROGRESS:
                MusicHelp.rotateMusicView(context);
                break;
            case MusicMode.Status.PAUSE:
                MusicHelp.haltMusicView(context);
                break;
            default:
                MusicHelp.removeMusicView(context);
                break;
        }
    }

    public static void setForeground(Service service) {
        Notification notification = new Notification();
        //notification.flags = Notification.FLAG_ONGOING_EVENT;
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        service.startForeground(0, notification);
    }

    public static void stopForeground(Service service) {
        service.stopForeground(true);
    }
}
