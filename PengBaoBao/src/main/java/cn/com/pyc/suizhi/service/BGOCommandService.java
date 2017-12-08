package cn.com.pyc.suizhi.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.sz.mobilesdk.util.SZLog;

import java.io.Serializable;

import cn.com.pyc.model.Userinfo_Model;
import cn.com.pyc.suizhi.bean.event.DBMakerEvent;
import cn.com.pyc.suizhi.help.DRMDBHelper;
import de.greenrobot.event.EventBus;

/**
 * 服务，执行一些的耗时操作
 *
 * @author hudq
 */
public class BGOCommandService extends IntentService {

    public static final int CREATE_DB = 0xa3;               //创建DB
    private static final String TAG = BGOCommandService.class.getSimpleName();

    /**
     * 开启
     *  @param context
     * @param option
     * @param userLogin
     */
    public static void startBGOService(Context context, int option) {
        DRMDBHelper.destroyDBHelper();
        Intent i = new Intent(context, BGOCommandService.class);
        i.putExtra("option", option);
        context.startService(i);
    }

    public BGOCommandService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SZLog.d(TAG + "  onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    // 处理完onStartCommand()后执行，注意onHandlerIntent是在后台线程中运行,异步操作。
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        if (!intent.hasExtra("option"))
            throw new IllegalArgumentException("hasExtra,'option' must be required.");
        int opt = intent.getIntExtra("option", 0);
        switch (opt) {
            case CREATE_DB: // 创建db
            {
                DRMDBHelper drmDB = new DRMDBHelper(this.getApplicationContext());
                drmDB.createDBTable();
                // 创建成功，发送通知信息，通知登录页面开始登录
                EventBus.getDefault().post(new DBMakerEvent(true));
            }
            break;
            default:
                break;
        }
    }

}
