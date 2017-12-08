package cn.com.pyc.suizhi.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import cn.com.pyc.suizhi.common.MusicMode;
import cn.com.pyc.suizhi.help.MusicHelp;

/**
 * 音乐服务基类
 * <p>
 * Created by hudq on 2017/3/28.
 */

public class BaseMusicService extends Service {

    private PowerManager.WakeLock wakeLock = null;
    private TelephonyManager mTelephonyManager = null;
    protected AudioManager mAudioManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //acquireWakeLock(this);
        MusicHelp.setForeground(this);
        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        // 监听电话状态, 如果接通了电话, 暂停
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //releaseWakeLock();
        MusicHelp.stopForeground(this);
        mAudioManager = null;
        mTelephonyManager.listen(null, PhoneStateListener.LISTEN_NONE);
    }

    /**
     * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
     */
    private void acquireWakeLock(Context ctx) {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) ctx
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "musicTag");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    /**
     * 释放设备电源锁
     */
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    // 监听电话状态
    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING: // 震铃
                    switch (MusicMode.STATUS) {
                        case MusicMode.Status.PLAY:// 当前状态如果是播放的情况 处理暂停
                        case MusicMode.Status.CONTINUE:
                        case MusicMode.Status.PROGRESS:
                            MusicHelp.pause(BaseMusicService.this);
                            break;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (MusicMode.STATUS == MusicMode.Status.PAUSE) {
                        MusicHelp.continuePlay(BaseMusicService.this);
                    }
                    break;
            }
        }
    }
}
