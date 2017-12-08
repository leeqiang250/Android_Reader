package cn.com.pyc.suizhi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sz.help.KeyHelp;

import cn.com.pyc.suizhi.help.DownloadHelp;
import cn.com.pyc.suizhi.manager.DownloadTaskManagerPat;
import cn.com.pyc.suizhi.manager.db.DownDataPatDBManager;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.service.DownloadPatService;

/**
 * 注册下载的广播 <br/>
 * IntentFilter downloadFilter = new IntentFilter(); <br/>
 * downloadFilter.addAction(ACTION_FINISH); <br/>
 * registerReceiver(downloadReceiver, downloadFilter); <br/>
 */
public abstract class DownloadPatReceiver extends BroadcastReceiver {

    public DownloadPatReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (!intent.hasExtra(KeyHelp.DF_FILEDATA))
            throw new IllegalArgumentException(
                    "hasExtra, 'FileData' must be required.");

        FileData o = intent.getParcelableExtra(KeyHelp.DF_FILEDATA);
        if (null == intent.getAction()) return;
        //DRMLog.d("dpr", "position = " + o.getPosition());

        switch (intent.getAction()) {
            case DownloadPatService.ACTION_UPDATE: {
                long currentSize = intent.getLongExtra(KeyHelp.DF_CURRENTSIZE, 0);
                int progress = intent.getIntExtra(KeyHelp.DF_PROGRESS, 0);
                boolean isLast = intent.getBooleanExtra(KeyHelp.DF_ISLAST, false);
                // 最后一次进度只是用来显示，不刷新界面view控件
                int state = isLast ? DownloadTaskManagerPat.PAUSE : DownloadTaskManagerPat
                        .DOWNLOADING;
                o.setTaskState(state);
                o.setProgress(progress);
                //o.setCurrentSize(currentSize);
                updateProgress(o, progress, currentSize, isLast);
            }
            break;
            case DownloadPatService.ACTION_ERROR: {
                o.setTaskState(DownloadTaskManagerPat.DOWNLOAD_ERROR);
                downloadError(o);
            }
            break;
            case DownloadPatService.ACTION_CONNECTING: {
                o.setTaskState(DownloadTaskManagerPat.CONNECTING);
                connecting(o);
            }
            break;
            case DownloadPatService.ACTION_PACKAGING: {
                o.setTaskState(DownloadTaskManagerPat.PACKAGING);
                packaging(o);
            }
            break;
            case DownloadPatService.ACTION_PARSERING: {
                // 删除下载记录（如果有的话）
                DownDataPatDBManager.Builder().deleteByFileId(o.getItemId());
                o.setTaskState(DownloadTaskManagerPat.PARSERING);
                parsering(o);
            }
            break;
            case DownloadPatService.ACTION_FINISHED: {
                o.setTaskState(DownloadTaskManagerPat.FINISHED);
                DownloadHelp.stopDownload(context, o.getItemId());
                downloadFinished(o);
            }
            break;
            case DownloadPatService.ACTION_REQUEST_ERROR: {
                o.setTaskState(DownloadTaskManagerPat.INIT);
                String code = intent.getStringExtra(KeyHelp.DF_CODE);
                requestError(o, code);
            }
            break;
            default:
                break;
        }
    }

    /**
     * 进度更新
     *
     * @param data           FileData
     * @param progress       进度
     * @param currentSize    当前下载大小
     * @param isLastProgress 是否是最后一次发送进度
     */
    protected abstract void updateProgress(FileData data, int progress,
                                           long currentSize, boolean isLastProgress);

    /**
     * 失败
     *
     * @param data FileData
     */
    protected abstract void downloadError(FileData data);

    /**
     * 正在连接中
     *
     * @param data FileData
     */
    protected abstract void connecting(FileData data);

    /**
     * 解析
     *
     * @param data FileData.java
     */
    protected abstract void parsering(FileData data);

    /**
     * 下载完成
     *
     * @param data FileData
     */
    protected abstract void downloadFinished(FileData data);

    /**
     * 正在打包.....
     *
     * @param data FileData
     */
    protected abstract void packaging(FileData data);


    /**
     * 其他错误
     *
     * @param data FileData
     * @param code Code
     */
    protected abstract void requestError(FileData data, String code);

}
