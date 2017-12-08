package cn.com.pyc.suizhi.help;

import android.content.Context;
import android.content.SharedPreferences;

import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.util.SPUtil;

/**
 * Created by hudq on 2017/4/13.
 */

public class ProgressHelp {

    public ProgressHelp() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    private static final String PREFS_NAME = "Progress_Preferences";

    public static boolean saveProgress(String key, Object obj) {
        Context context = SZApplication.getInstance();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (obj instanceof String) {
            editor.putString(key, String.valueOf(obj));
        }
        if (obj instanceof Integer) {
            editor.putInt(key, Integer.valueOf(String.valueOf(obj)));
        }
        if (obj instanceof Float) {
            editor.putFloat(key, Float.valueOf(String.valueOf(obj)));
        }
        if (obj instanceof Long) {
            editor.putLong(key, Long.valueOf(String.valueOf(obj)));
        }

        return SPUtil.SharedPreferencesCompat.apply(editor);
    }

    public static Object getProgress(String key, Object defaultObject) {
        Context context = SZApplication.getInstance();
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return defaultObject;
    }

    public static boolean removeProgress(String key) {
        Context context = SZApplication.getInstance();
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        return SPUtil.SharedPreferencesCompat.apply(editor);
    }

    public static boolean clearProgress() {
        Context context = SZApplication.getInstance();
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        return SPUtil.SharedPreferencesCompat.apply(editor);
    }

}
