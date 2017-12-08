package cn.com.pyc.pbbonline.util;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.Window;
import android.view.WindowManager;

import cn.com.pyc.pbbonline.widget.VideoView;

/**
 * 根据手势滑动，设置声音和亮度,进度值
 *
 * @author hudq
 */
public class SysVolumeLightUtil {

    /**
     * 设置声音大小
     *
     * @param ctx
     * @param percent 滑动距离百分比
     */
    public static int setVolumeSlide(Context ctx, float percent) {
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        int mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume = -1;
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        int percentVolume = (int) ((index * 1.0 / mMaxVolume) * 100); // 0.1--->10
        return percentVolume;
    }

    /**
     * 设置亮度
     *
     * @param activity
     * @param percent  滑动距离百分比
     */
    public static int setBrightnessSlide(Activity activity, float percent) {
        if (activity.getWindow() == null)
            return 0;

        Window window = activity.getWindow();
        if (window.getAttributes() == null)
            return 0;

        float brightness = -1;
        if (brightness < 0) {
            brightness = window.getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        // 改变亮度
        WindowManager.LayoutParams lpa = window.getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        window.setAttributes(lpa);
        int percentBrightness = (int) (lpa.screenBrightness * 100);  // 0.21--->21
        return percentBrightness;
    }

    /**
     * 获取当前位置和前进后退数值
     * <p>
     *
     * @param videoView 播放控件
     * @param percent   百分比
     * @return params[0]: 当前位置，long <br/>
     * params[1]: 前进/后退数值，eg：可能为"+10","-10"
     */
    public static long[] getProgressSlide(VideoView videoView, float percent) {
        long position = videoView.getCurrentProgress();
        long duration = videoView.getDuration();
//        long deltaMax = Math.min(100 * 1000, duration - position);
//        long delta = (long) (deltaMax * percent);
//
//        long newPosition = delta + position;
//        if (newPosition > duration) {
//            newPosition = duration;
//        } else if (newPosition <= 0) {
//            newPosition = 0;
//            delta = -position;
//        }
//        long showDelta = delta / 1000;
//        long[] params = new long[2];
//        params[0] = newPosition;
//        params[1] = showDelta;
        return getProgressSlide(duration, position, percent);
    }

    /**
     * 获取当前位置和前进后退数值
     * <p>
     *
     * @param duration 总长度
     * @param position 当前长度
     * @param percent  百分比
     * @return params[0]: 当前位置，long <br/>
     * params[1]: 前进/后退数值，eg：可能为"+10","-10"
     */
    public static long[] getProgressSlide(long duration, long position, float percent) {
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);

        long newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        long showDelta = delta / 1000;
        long[] params = new long[2];
        params[0] = newPosition;
        params[1] = showDelta;
        return params;
    }

}
