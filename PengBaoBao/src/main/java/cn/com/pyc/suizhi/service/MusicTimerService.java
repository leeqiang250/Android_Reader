package cn.com.pyc.suizhi.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.sz.mobilesdk.util.SPUtil;

import cn.com.pyc.suizhi.help.MusicHelp;
import cn.com.pyc.suizhi.receiver.MusicTimerReceiver;
import cn.com.pyc.suizhi.util.DRMUtil;

/**
 * @author Xiong
 * @date 2017/4/1 19:13
 */
public class MusicTimerService extends Service {

    private long reclean;
    private CountDownTimer timer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SPUtil.remove(MusicHelp.TIMER_KEY);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            //throw new IllegalArgumentException("start service failed.");
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        reclean = intent.getLongExtra(MusicHelp.TIMER_COUNTDOWN, 0L);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new CountDownTimer(reclean, 1000) {
            @Override
            public void onTick(long l) {
                reclean = l;
                String action = DRMUtil.BROADCAST_MUSIC_TIMER;
                sendMyBroadcast(action, reclean);
            }

            @Override
            public void onFinish() {
                String action_end = DRMUtil.BROADCAST_MUSIC_TIMER_END;
                //sendMyBroadcastTxt(action_end, "定时");
                sendMyBroadcastTxt(action_end, " ");

                timer.cancel();
                timer = null;

                MusicHelp.release(MusicTimerService.this);
                MusicHelp.stopMusicView(MusicTimerService.this);
                stopSelf();
            }
        };

        timer.start();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 发送倒计时剩余时间
     */
    private void sendMyBroadcast(String action, long rec) {
        Intent intent = new Intent(action);
        intent.putExtra(MusicTimerReceiver.TAG_CURRENT, rec);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * 发送倒计时结束后文字显示
     */
    private void sendMyBroadcastTxt(String action, String rec) {
        Intent intent = new Intent(action);
        intent.putExtra(MusicTimerReceiver.TAG_CURRENT_END_TXT, rec);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
