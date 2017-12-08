package com.sz.mobilesdk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * 系统设备信息类
 */
public class DeviceUtil {

    public static final int DEVICEINFO_UNKNOWN = -1;

    public static final String EMPTY = "";

    public static final int SDK_VERSION_1_5 = 3;

    public static final int SDK_VERSION_1_6 = 4;

    public static final int SDK_VERSION_2_0 = 5;

    public static final int SDK_VERSION_2_0_1 = 6;

    public static final int SDK_VERSION_2_1 = 7;

    public static final int SDK_VERSION_2_2 = 8;

    public static final int SDK_VERSION_2_3 = 9;

    public static final int SDK_VERSION_2_3_3 = 10;

    public static final int SDK_VERSION_3_0 = 11;

    public static final int SDK_VERSION_3_1 = 12;

    public static final int SDK_VERSION_3_2 = 13;

    public static final int SDK_VERSION_4_0 = 14;

    public static final int SDK_VERSION_4_0_3 = 15;

    public static final int SDK_VERSION_4_1_2 = 16;

    public static final int SDK_VERSION_4_2_2 = 17;

    public static final int SDK_VERSION_4_3 = 18;

    public static final int SDK_VERSION_4_4_2 = 19;

    public static final int SDK_VERSION_4_4_W = 20;

    public static final int SDK_VERSION_5_0 = 21;

    public static final int SDK_VERSION_5_0_1 = 22;

    // /** >=2.2 8 */
    // public static boolean hasFroyo() {
    // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    // }
    //
    // /** >=2.3 */
    // public static boolean hasGingerbread() {
    // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    // }

    /**
     * >=3.0 LEVEL:11
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    // /** >=3.1 */
    // public static boolean hasHoneycombMR1() {
    // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    // }
    //
    // /** >=4.0 14 */
    // public static boolean hasICS() {
    // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    // }
    //
    // /**
    // * >= 4.1 16
    // *
    // * @return
    // */
    // public static boolean hasJellyBean() {
    // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    // }
    //
    // /** >= 4.2 17 */
    // public static boolean hasJellyBeanMr1() {
    // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    // }
    //
    // /** >= 4.3 18 */
    // public static boolean hasJellyBeanMr2() {
    // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    // }

    /**
     * >=4.4 19
     */
    public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * 获得Android版本
     *
     * @return eg 14,19
     */
    public static int getSDKVersionInt() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获得设备的固件版本号 eg 4.3 这样的
     */
    public static String getReleaseVersion() {
        return Build.VERSION.RELEASE == null ? EMPTY : Build.VERSION.RELEASE;
    }

    /**
     * 判断是否是平板电脑
     *
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration
                .SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return hasHoneycomb() && isTablet(context);
    }

    /**
     * 获得设备型号
     *
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL == null ? EMPTY : Build.MODEL;
    }

    /**
     * 获得设备制造商
     *
     * @return
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER == null ? EMPTY : Build.MANUFACTURER;
    }

    /**
     * 获取屏幕宽高
     *
     * @param context
     * @return outSize.x = width; outSize.y = height.
     */
    public static Point getScreenSize(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        return outSize;
    }

    /**
     * 获得屏幕尺寸
     *
     * @param context
     * @return eg:480x320;1280x720
     */
    public static String getScreenResolution(Context context) {
        Point outSize = getScreenSize(context);
        return outSize.y + "x" + outSize.x;
    }

    /**
     * 获得设备屏幕密度
     */
    public static float getScreenDensity(Context context) {
        DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
        return metrics.density;
    }

    /**
     * 设置屏幕亮度
     */
    public static void setBrightness(final Activity context, float f) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.screenBrightness = f;
        if (lp.screenBrightness > 1.0f)
            lp.screenBrightness = 1.0f;
        else if (lp.screenBrightness < 0.01f)
            lp.screenBrightness = 0.01f;
        context.getWindow().setAttributes(lp);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(Context ctx, View v) {
        if (v != null && ctx != null) {
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            // 这个方法可以实现输入法在窗口上切换显示，如果输入法在窗口上已经显示，则隐藏，如果隐藏，则显示输入法到窗口上
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager
                    .HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 显示软键盘
     */
    public static void showSoftInput(Context ctx) {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    public static void showSoftInput(Context ctx, View v) {
        v.requestFocus();

        InputMethodManager m = (InputMethodManager) v.getContext().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

    }

    /**
     * 软键盘是否已经打开
     *
     * @return
     */
    protected boolean isHardKeyboardOpen(Context ctx) {
        return ctx.getResources().getConfiguration().hardKeyboardHidden == Configuration
                .HARDKEYBOARDHIDDEN_NO;
    }

    /**
     * 获取设备的cpu核心数
     *
     * @return
     */
    public static int getCPUCoresNum() {
        // Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support giving a single application access to
            // both cores, but a
            // handful of devices (Atrix 4G and Droid X2 for example) were
            // released with a dual-core
            // chipset and Gingerbread; that can let an app in the background
            // run without impacting
            // the foreground application. But for our purposes, it makes them
            // single core.
            return 1;
        }
        int cores;
        try {
            // Get directory containing CPU info
            // adb shell 下执行 ls /sys/devices/system/cpu/查看cpu个数
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            cores = files.length;
            SZLog.d("CPU Core Count: " + cores);
        } catch (SecurityException e) {
            e.printStackTrace();
            cores = DEVICEINFO_UNKNOWN;
        } catch (NullPointerException e) {
            e.printStackTrace();
            cores = DEVICEINFO_UNKNOWN;
        }
        // Return the number of cores (virtual CPU devices)
        return cores;
    }

    /**
     * 获取CPU时钟频率
     *
     * @return
     */
    public static int getCPUMaxFreqKHz() {
        int maxFreq = 0;
        try {
            int coreCount = getCPUCoresNum();
            for (int i = 0; i < coreCount; i++) {
                String filename = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
                File cpuInfoMaxFreqFile = new File(filename);
                if (cpuInfoMaxFreqFile.exists()) {
                    byte[] buffer = new byte[128];
                    FileInputStream stream = new FileInputStream(cpuInfoMaxFreqFile);
                    try {
                        stream.read(buffer);
                        int endIndex = 0;
                        // Trim the first number out of the byte buffer.
                        while (buffer[endIndex] >= '0' && buffer[endIndex] <= '9' && endIndex <
                                buffer.length)
                            endIndex++;
                        String str = new String(buffer, 0, endIndex);
                        Integer freqBound = Integer.parseInt(str);
                        if (freqBound > maxFreq) {
                            maxFreq = freqBound;
                        }
                    } catch (NumberFormatException e) {
                        // Fall through and use /proc/cpuinfo.
                    } finally {
                        stream.close();
                    }
                }
            }
            if (maxFreq == 0) {
                FileInputStream stream = new FileInputStream("/proc/cpuinfo");
                try {
                    int freqBound = parseFileForValue("cpu MHz", stream);
                    freqBound *= 1000; // MHz -> kHz
                    if (freqBound > maxFreq) {
                        maxFreq = freqBound;
                    }
                } finally {
                    stream.close();
                }
            }
        } catch (IOException e) {
            maxFreq = 0; // Fall through and return unknown.
        }
        return maxFreq;
    }

    /**
     * 获取cpu的信息 eg armv7a
     *
     * @return armv7a或者 ""
     */
    public static String getCpuInfo() {
        String cpuInfo = "";
        try {
            // adb shell 下执行 cat /proc/cpuinfo查看
            if (new File("/proc/cpuinfo").exists()) {
                FileReader fr = new FileReader("/proc/cpuinfo");
                BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
                cpuInfo = localBufferedReader.readLine();
                localBufferedReader.close();

                if (cpuInfo != null) {
                    cpuInfo = cpuInfo.split(":")[1].trim().split(" ")[0];
                }
            }
        } catch (Exception e) {
        }
        return cpuInfo;
    }

    /**
     * 获取sd卡可用大小和总大小
     *
     * @return long[]
     * <p>
     * long[0]:总大小 <br/>
     * long[1]:可用大小
     */
    public long[] getSDCardStorage() {
        long[] sdCardSize = new long[2];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSizeLong();
            long bCount = sf.getBlockCountLong();
            long availBlocks = sf.getAvailableBlocksLong();

            sdCardSize[0] = bSize * bCount;// 总大小
            sdCardSize[1] = bSize * availBlocks;// 可用大小
        }
        return sdCardSize;
    }

    /**
     * 获取系统内存（byte）
     *
     * @param c
     * @return
     */
    public static long getTotalMemory(Context c) {
        // memInfo.totalMem not supported in pre-Jelly Bean APIs.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            am.getMemoryInfo(memInfo);
            return memInfo != null ? memInfo.totalMem : DEVICEINFO_UNKNOWN;
        } else {
            long totalMem = DEVICEINFO_UNKNOWN;
            try {
                FileInputStream stream = new FileInputStream("/proc/meminfo");
                try {
                    totalMem = parseFileForValue("MemTotal", stream);
                    totalMem *= 1024;
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
            }
            return totalMem;
        }
    }

    /**
     * 获取内存信息，单位Kb
     */
    @Deprecated
    public static long getTotalMemory2(Context context) {
        long total = 0;
        try {
            if (new File("/proc/meminfo").exists()) {
                FileReader fr = new FileReader("/proc/meminfo");
                BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
                String meminfo = localBufferedReader.readLine();
                localBufferedReader.close();

                if (meminfo != null) {
                    meminfo = meminfo.split(":")[1].trim().split(" ")[0];
                    total = ConvertToUtil.toLong(meminfo == null ? EMPTY : meminfo.trim());
                }
            }
        } catch (Exception e) {
        }

        return total;
    }

    /**
     * Helper method for reading values from system files, using a minimised
     * buffer.
     *
     * @param textToMatch - Text in the system files to read for.
     * @param stream      - FileInputStream of the system file being read from.
     * @return A numerical value following textToMatch in specified the system
     * file. -1 in the event of a failure.
     */
    private static int parseFileForValue(String textToMatch, FileInputStream stream) {
        byte[] buffer = new byte[1024];
        try {
            int length = stream.read(buffer);
            for (int i = 0; i < length; i++) {
                if (buffer[i] == '\n' || i == 0) {
                    if (buffer[i] == '\n')
                        i++;
                    for (int j = i; j < length; j++) {
                        int textIndex = j - i;
                        // Text doesn't match query at some point.
                        if (buffer[j] != textToMatch.charAt(textIndex)) {
                            break;
                        }
                        // Text matches query here.
                        if (textIndex == textToMatch.length() - 1) {
                            return extractValue(buffer, j);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Ignore any exceptions and fall through to return unknown value.
        } catch (NumberFormatException e) {
        }
        return DEVICEINFO_UNKNOWN;
    }

    /**
     * Helper method used by {@link #parseFileForValue(String, FileInputStream)
     * parseFileForValue}. Parses the next available number after the match in
     * the file being read and returns it as an integer.
     *
     * @param index - The index in the buffer array to begin looking.
     * @return The next number on that line in the buffer, returned as an int.
     * Returns DEVICEINFO_UNKNOWN = -1 in the event that no more numbers
     * exist on the same line.
     */
    private static int extractValue(byte[] buffer, int index) {
        while (index < buffer.length && buffer[index] != '\n') {
            if (Character.isDigit(buffer[index])) {
                int start = index;
                index++;
                while (index < buffer.length && Character.isDigit(buffer[index])) {
                    index++;
                }
                String str = new String(buffer, 0, start, index - start);
                return Integer.parseInt(str);
            }
            index++;
        }
        return DEVICEINFO_UNKNOWN;
    }

    /**
     * 开启外部activity
     *
     * @param ctx
     * @param packageName
     */
    public static void startApkActivity(final Context ctx, String packageName) {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(packageName, 0);
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setPackage(pi.packageName);

            List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                String className = ri.activityInfo.name;
                intent.setComponent(new ComponentName(packageName, className));
                ctx.startActivity(intent);
            }
        } catch (NameNotFoundException e) {
            SZLog.e("", e.getMessage());
        }
    }

    /**
     * 获得国际移动设备身份码
     *
     * @param context
     * @return
     */
    public static String getIMEI(Context context) {
        try {
            return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获得android_id
     *
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    /**
     * 获取一个activity的截图
     *
     * @param activity
     * @return
     */
    public static Bitmap getScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap sourceB = view.getDrawingCache();

        if (sourceB == null)
            return null;
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        int width = outSize.x;
        int height = outSize.y;
        Bitmap bitmap = Bitmap.createBitmap(sourceB, 0, statusBarHeight, width, height -
                statusBarHeight);
        view.destroyDrawingCache();
        return bitmap;
    }

    /**
     * 获取mac地址
     *
     * @param context
     * @return
     */
    public static String getLocalMacAddress(Context context) {
        try {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            return info.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 判断是否支持闪光灯
     */
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) //
                        // 判断设备是否支持闪光灯
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测设备是否支持相机
     */
    public static boolean isSupportCameraHardware(Context context) {
        if (context != null && context.getPackageManager().hasSystemFeature(PackageManager
                .FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    // @TargetApi(11)
    // public static void showSystemUi(View mViewRoot, boolean visible) {
    // if (DeviceUtils.hasHoneycomb() && mViewRoot != null) {
    // int flag = visible ? 0 : View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    // | View.SYSTEM_UI_FLAG_LOW_PROFILE;
    // mViewRoot.setSystemUiVisibility(flag);
    // }
    // }

    /**
     * 获取外置的sdcard列表 参考：http://blog.csdn.net/boystarzq09/article/details/9837535
     */
    public static ArrayList<String> getExtExternalStorage() {
        ArrayList<String> result = new ArrayList<String>();
        Process proc = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            proc = runtime.exec("mount");// 检测系统挂载
            is = proc.getInputStream();
            isr = new InputStreamReader(is);
            String line;

            final String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure") || line.contains("asec"))// /mnt/secure/asec
                    continue;

                if (line.contains("fat")) {// line.contains("fuse")
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        // ZET[/mnt/sdcard-ext, /mnt/sdcard]
                        // SAMSUNG
                        final String path = columns[1];
                        File f = new File(path);
                        if (f != null && f.canRead() && f.canWrite() && f.exists() && !sdcard
                                .equals(path))
                            result.add(path);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
                br = null;
            }

            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
                isr = null;
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
                is = null;
            }

            if (proc != null) {
                try {
                    proc.destroy();
                } catch (Exception e) {

                }
                proc = null;
            }
        }
        return result;
    }
}
