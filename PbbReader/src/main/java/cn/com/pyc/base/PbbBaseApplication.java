package cn.com.pyc.base;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.qlk.util.base.BaseApplication;
import com.qlk.util.global.GlobalObserver;
import com.qlk.util.tool.Util.FileUtil;
import com.sz.mobilesdk.SZInitInterface;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.DeviceUtil;
import com.sz.mobilesdk.util.SPUtil;

import org.xutils.x;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Set;

import cn.com.pyc.db.DiskDB;
import cn.com.pyc.db.DiskDB_2;
import cn.com.pyc.global.ObTag;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.pbbonline.util.DirsUtil;
import cn.com.pyc.utils.CrashHandler;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.utils.ExampleUtil;
import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * 主要是注册各种广播：极光、锁屏、后台、SD卡、电话。开启日志，检测硬盘数目等
 *
 * @author QiLiKing 2015-7-29 上午11:04:52
 */
public class PbbBaseApplication extends BaseApplication {

    /**
     * 开发模式，true调试状态；false线上
     */
    private static final boolean DEVELOPER_MODE = cn.com.pyc.pbb.reader.BuildConfig.DEBUG; //reader的BuildConfig
    private static final String TAG = "pbb reader";
    public static final String MESSAGE_RECEIVED_ACTION = "cn.com.pyc.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";
    private static final int MSG_SET_ALIAS = 1001;
    private SafetyHandler mHandler = new SafetyHandler(this);
    private static final String D_URL = "http://192.168.85.5:82/HostMonitor/client/log/addLog";
    private static final String R_URL = "http://114.112.104.138:6001/HostMonitor/client/log/addLog";

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(false);
//        Debug.startMethodTracing("PengBB");
        Dirs.reGetCardsPaths(this);
        registerReceiver();
        copyFile2TargetPath();
        initJPush();
        init4Online();
//        Debug.stopMethodTracing();
    }

    private void init4Online() {
        try {
            SZInitInterface.setDebugMode(DEVELOPER_MODE);
            SZInitInterface.init(this);
            LOGConfig.setLogHttpUrl(DEVELOPER_MODE ? D_URL : R_URL);

            if (!DEVELOPER_MODE) {
                CrashHandler crashHandler = CrashHandler.getInstance();
                crashHandler.init(this);
                DirsUtil.checkSDCardCrashLog(CrashHandler.LOGPATTERN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initJPush() {
        try {
            JPushInterface.setDebugMode(DEVELOPER_MODE);
            JPushInterface.init(this);

            if (!(boolean) PbbSP.getGSP(getApplicationContext()).getValue(
                    PbbSP.SP_PUSH_SETALIS_SUCCESS, false)) {
                setAlias();
            }
            registerMessageReceiver();  // used for receive msg

            // pbbonline,获取registrationid,注册成功接收广播也能获取
            String reg_id = JPushInterface.getRegistrationID(this);
            SPUtil.save(Fields.FIELDS_JPUSH_REGISTERID, reg_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFile2TargetPath() {
        try {

            if (getPackageName().equals("cn.com.pyc.pbb")) {
                if (!new File(DiskDB_2.DB_PATH).exists()) {
                    DiskDB_2.makeDbDir();
                    new File(Dirs.getPrivacyDir(Dirs.getDefaultBoot())).mkdirs();
                    if (new File(DiskDB.DB_PATH).exists()) {
                        FileUtil.copyFile(DiskDB.DB_PATH, DiskDB_2.DB_PATH);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAlias() {
        String alias = DeviceUtil.getIMEI(getApplicationContext());
        if (TextUtils.isEmpty(alias)) {
            //Toast.makeText(this, "别名为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ExampleUtil.isValidTagAndAlias(alias)) {
            //Toast.makeText(this, "标签为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用 Handler 来异步设置别名
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
    }

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0:
                    // logs = "Set tag and alias success";
                    // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    PbbSP.getGSP(getApplicationContext()).putValue(PbbSP.SP_PUSH_SETALIS_SUCCESS,
                            true);
                    System.out.println("TagAlias设置成功！");
                    break;
                case 6002:
                    // logs = "Failed to set alias and tags due to timeout. Try again
                    // after 10s.";
                    // 延迟 10 秒来调用 Handler 设置别名
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias),
                            1000 * 10);
                    break;
                default:
                    // logs = "Failed with errorCode = " + code;
                    break;
            }
        }
    };

    private static class SafetyHandler extends Handler {
        private WeakReference<PbbBaseApplication> reference;

        private SafetyHandler(PbbBaseApplication app) {
            reference = new WeakReference<>(app);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (reference.get() == null) return;
            PbbBaseApplication mContext = reference.get();
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.d(TAG, "Set alias in handler.");
                    // 调用 JPush 接口来设置别名。
                    JPushInterface.setAliasAndTags(mContext, (String) msg.obj, null,
                            mContext.mAliasCallback);
                    break;
                default:
                    Log.i(TAG, "Unhandled msg - " + msg.what);
            }
        }
    }

    public void registerMessageReceiver() {
        MessageReceiver mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String message = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + message + "\n");
                if (!ExampleUtil.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
                Toast.makeText(getApplicationContext(), showMsg.toString(), Toast.LENGTH_LONG)
                        .show();
                // setCostomMsg(showMsg.toString());
            }
        }
    }

    private void registerReceiver() {
        try {

        /* 锁屏、后台 */
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                        String reason = intent.getStringExtra("reason");
                        if (reason != null && reason.equals("homekey")) {
                            GlobalObserver.getGOb().postNotifyObservers(ObTag.Home);
                        }
                    } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.ScreenLockOff);
                    } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.ScreenLockOn);
                    }
                }
            }, filter);

		/* SD卡插拔监听 */
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction(Intent.ACTION_MEDIA_EJECT);
            filter2.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter2.addDataScheme("file");
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Dirs.reGetCardsPaths(context);
                    if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.SdCardOff);
                    } else {
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.SdCardOn);
                    }
                }
            }, filter2);

		/* 电话 */
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            tm.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);
                    if (state != TelephonyManager.CALL_STATE_IDLE) {
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.PhoneOn);
                    } else {
                        GlobalObserver.getGOb().postNotifyObservers(ObTag.PhoneOff);
                    }
                }

            }, PhoneStateListener.LISTEN_CALL_STATE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清楚缓存、退出各个activity
     */
    @Override
    public void safeExit() {
        super.safeExit();
    }

    /**
     * 设置jpush的基础属性
     *
     * @param ctx Context
     */
    @Deprecated
    private void initBaseJPushSetting(Context ctx) {
        //设置静音时段:23:00~6:00
        // JPushInterface.setSilenceTime(ctx, 23, 0, 6, 0);
        //限制保留的通知条数。默认为保留最近 5 条通知。
        JPushInterface.setLatestNotificationNumber(ctx, 3);

        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(ctx);
        builder.notificationDefaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;    // 设置为自动消失
        //JPushInterface.setPushNotificationBuilder(0, builder);
        // 设置builder的样式编号为0，发送时指定编号发送。
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }

}
