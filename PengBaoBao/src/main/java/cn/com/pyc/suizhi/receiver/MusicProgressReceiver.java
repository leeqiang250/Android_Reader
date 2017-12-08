package cn.com.pyc.suizhi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.com.pyc.suizhi.util.DRMUtil;

public abstract class MusicProgressReceiver extends BroadcastReceiver {

    public static final String TAG_CURRENT = "CurrentPositionInt";
    public static final String TAG_DURATION = "DurationInt";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;

        int currentPos = intent.getIntExtra(TAG_CURRENT, -1);
        int duration = intent.getIntExtra(TAG_DURATION, -1);
        // String currentPosintStr =
        // intent.getStringExtra("CurrentPositionStr");
        // String DurationintStr = intent.getStringExtra("DurationStr");
        switch (action) {
            case DRMUtil.BROADCAST_MUSIC_PROGRESS:
                progressTime(currentPos, duration);
                break;
            case DRMUtil.BROADCAST_MUSIC_OBTAIN_TIME:
                obtainTime(currentPos, duration);
                break;
            default:
                break;
        }
    }

    protected abstract void obtainTime(int currentPosition, int duration);

    protected abstract void progressTime(int currentPosition, int duration);

}
