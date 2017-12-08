package cn.com.pyc.suizhi.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.sz.help.KeyHelp;
import com.sz.mobilesdk.util.SZLog;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.com.pyc.suizhi.manager.DownloadTaskManagerPat;
import cn.com.pyc.suizhi.model.FileData;

/**
 * 下载任务服务类(证书分离后，使用http下载)
 *
 * @author hudq
 */
public class DownloadPatService extends Service {

    private static final String TAG = DownloadPatService.class.getSimpleName();
    private Context context;

    // private final int count = Runtime.getRuntime().availableProcessors() * 3;

    // 下载异常
    public static final String ACTION_ERROR = "cn.com.pyc.drm.Action_Error";
    // 正在连接
    public static final String ACTION_CONNECTING = "cn.com.pyc.drm.Action_Connecting";
    // 更新ui
    public static final String ACTION_UPDATE = "cn.com.pyc.drm.Action_Update";
    // 停止某一个下载任务
    public static final String ACTION_STOP = "cn.com.pyc.drm.Action_Stop";
    // 停止所有下载(退出应用主页面时)
    public static final String ACTION_ALL_STOP = "cn.com.pyc.drm.Action_All_Stop";
    // 正在打包
    public static final String ACTION_PACKAGING = "cn.com.pyc.drm.Action_Packaging";
    // 解析
    public static final String ACTION_PARSERING = "cn.com.pyc.drm.Action_Parsering";
    // 完成下载
    public static final String ACTION_FINISHED = "cn.com.pyc.drm.Action_Finished";
    // 其他错误
    public static final String ACTION_REQUEST_ERROR = "cn.com.pyc.drm.Action_Request_Error";

    /**
     * 下载任务的集合(key=文件id，value=任务)
     */
    private static Map<String, DownloadTaskManagerPat> sTasks;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if (sTasks == null) {
            sTasks = new LinkedHashMap<>();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sTasks != null) {
            sTasks.clear();
            sTasks = null;
        }
        DownloadTaskManagerPat.shutdownPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return super.onStartCommand(intent, flags, startId);
        FileData data = intent.getParcelableExtra(KeyHelp.DF_FILEDATA);
        if (data != null) {
            startTask(data);
        }
        // action
        actionService(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void actionService(Intent intent) {
        String action = intent.getAction();
        if (ACTION_STOP.equalsIgnoreCase(action)) {
            if (!intent.hasExtra(KeyHelp.DF_TASK_ID))
                throw new IllegalArgumentException(
                        "hasExtra,'taskId' must be required.");

            // 通过taskId，取出下载任务，暂停下载
            String id = intent.getStringExtra(KeyHelp.DF_TASK_ID);
            DownloadTaskManagerPat task = sTasks.get(id);
            if (task != null) {
                task.isPause = true;
                task.isClose = true;
            }
        } else if (ACTION_ALL_STOP.equalsIgnoreCase(action)) {
            Iterator<String> iterator = sTasks.keySet().iterator();
            while (iterator.hasNext()) {
                DownloadTaskManagerPat task = sTasks.get(iterator.next());
                if (task != null) {
                    task.isPause = true;
                    task.isClose = true;
                }
            }
            // 停止所有下载
            SZLog.d(TAG, "stop all task ,size = " + sTasks.size());
            sTasks.clear();
            stopSelf();
        }
    }

    /**
     * 启动下载任务
     *
     * @param o
     */
    private void startTask(FileData o) {
        SZLog.i("1.start download : " + o.getContent_name());
        DownloadTaskManagerPat task = new DownloadTaskManagerPat(context, o);
        task.download();
        sTasks.put(o.getItemId(), task);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
