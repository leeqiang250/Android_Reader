package cn.com.pyc.suizhi.widget.lrc;

import android.content.Context;

/**
 * Created by hudq on 2017/3/7.
 */

public class LrcHelp {

    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
