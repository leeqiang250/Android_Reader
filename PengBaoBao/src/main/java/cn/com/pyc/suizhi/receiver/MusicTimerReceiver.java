package cn.com.pyc.suizhi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.com.pyc.suizhi.util.DRMUtil;

/**
 * Created by hudq on 2017/4/11.
 */

public abstract class MusicTimerReceiver extends BroadcastReceiver {

    public static final String TAG_CURRENT = "current_time";
    public static final String TAG_CURRENT_END_TXT = "current_time_end_txt";  //倒计时结束时文字显示


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;

        long currentPos = intent.getLongExtra(TAG_CURRENT, 0L);
        if (DRMUtil.BROADCAST_MUSIC_TIMER.equals(action)) {
            onTick(currentPos);
        }

        String string = intent.getStringExtra(TAG_CURRENT_END_TXT);
        if (DRMUtil.BROADCAST_MUSIC_TIMER_END.equals(action)){
            onFinish(string);
        }
    }
    protected abstract void onTick(long current);
    protected abstract void onFinish(String txt);
}
