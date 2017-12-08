package cn.com.pyc.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.PathUtil;
import com.sz.mobilesdk.util.SPUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LogerHelp;

public class CrashHandler implements UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE = new CrashHandler();

    //online
    public static final String LOGPATTERN = "yyyy-MM-dd-HH-mm-ss-SSS";
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat(LOGPATTERN, Locale.getDefault());

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ex != null) {
                    final String error = "http://api.pyc.com.cn/api/v1/errorlog";
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("apptype", "28");
                        obj.put("errormsg", Log.getStackTraceString(ex));
                        httpPost(error, obj.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * online添加；
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = (pi.versionName == null) ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                //Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存错误信息到本地目录的文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);

        // 增加日志监控
        ExtraParams ep = new ExtraParams();
        ep.account_name = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
        String password = (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, "");
        ep.account_password = AESUtil.encrypt(password);
        ep.file_name = LogerHelp.getFileName();
        ep.lines = LogerHelp.getLineNumber();
        LogerEngine.debug(mContext, result, true, ep);

        try {
            String time = formatter.format(new Date());
            String fileName = time + ".log";
            String path = PathUtil.getSZCrashPath();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(sb.toString().getBytes());
            fos.close();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void httpPost(String url, String sndJsonData) {
        HttpURLConnection conn = null;
        try {
            System.out.println("发送错误日志: " + sndJsonData);
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(5000);
            conn.connect();

            conn.getOutputStream().write(sndJsonData.getBytes());
            conn.getOutputStream().flush();

            InputStream iStream = conn.getInputStream();
            byte[] bs = new byte[24];
            iStream.read(bs);
            System.out.println("send log：" + (new String(bs)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
