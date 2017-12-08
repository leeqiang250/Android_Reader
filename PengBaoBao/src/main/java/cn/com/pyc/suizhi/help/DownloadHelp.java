package cn.com.pyc.suizhi.help;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.sz.help.KeyHelp;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.SecurityUtil;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.service.DownloadPatService;


/**
 * 下载帮助类（证书分离后）
 * <p>
 * Created by hudq on 2017/4/1.
 */

public class DownloadHelp {

    /**
     * 开启下载
     *
     * @param ctx
     * @param o
     */
    public static void startDownload(Context ctx, FileData o) {
        Intent intent = new Intent(ctx, DownloadPatService.class);
        intent.putExtra(KeyHelp.DF_FILEDATA, o);
        ctx.startService(intent);
    }

    /**
     * 停止其中一个下载任务
     *
     * @param ctx
     * @param fileId
     */
    public static void stopDownload(Context ctx, String fileId) {
        Intent intent = new Intent(ctx, DownloadPatService.class);
        intent.setAction(DownloadPatService.ACTION_STOP);
        intent.putExtra(KeyHelp.DF_TASK_ID, fileId);
        ctx.startService(intent);
    }

    /**
     * 停止所有下载任务
     *
     * @param ctx
     */
    public static void stopAllDownload(Context ctx) {
        Intent intent = new Intent(ctx, DownloadPatService.class);
        intent.setAction(DownloadPatService.ACTION_ALL_STOP);
        ctx.startService(intent);
    }

    //------------------------
    //临时保存正在下载文件的进度
    private static boolean saveFileProgress(String itemId, int progress) {
        return SPUtil.save("progress-" + itemId, progress);
    }

    public static int findFileProgress(String itemId) {
        return (int) SPUtil.get("progress-" + itemId, 0);
    }

    public static boolean removeFileProgress(String itemId) {
        return SPUtil.remove("progress-" + itemId);
    }

    //------------------------
    //临时保存正在解析的文件id
    private static boolean saveParserId(String itemId) {
        return SPUtil.save("parser-" + itemId, itemId);
    }

    public static String findParserId(String itemId) {
        return (String) SPUtil.get("parser-" + itemId, "");
    }

    private static boolean removeParserId(String itemId) {
        return SPUtil.remove("parser-" + itemId);
    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    /**
     * 正在连接
     */
    public static void connecting(LocalBroadcastManager mLocalBroadcastManager, FileData
            mFileData) {
        mLocalBroadcastManager.sendBroadcast(new Intent(DownloadPatService.ACTION_CONNECTING)
                .putExtra(KeyHelp.DF_FILEDATA, mFileData));
    }

    /**
     * 正在解析
     */
    public static void parsering(LocalBroadcastManager mLocalBroadcastManager, FileData
            mFileData) {
        saveParserId(mFileData.getItemId());
        mLocalBroadcastManager.sendBroadcast(new Intent(DownloadPatService
                .ACTION_PARSERING).putExtra(KeyHelp.DF_FILEDATA, mFileData));
    }

    /**
     * 下载结束
     */
    public static void finished(LocalBroadcastManager mLocalBroadcastManager, FileData
            mFileData) {
        removeParserId(mFileData.getItemId());
        removeFileProgress(mFileData.getItemId());
        mLocalBroadcastManager.sendBroadcast(new Intent(DownloadPatService
                .ACTION_FINISHED).putExtra(KeyHelp.DF_FILEDATA, mFileData));
    }

    /**
     * 下载错误
     */
    public static void error(LocalBroadcastManager mLocalBroadcastManager, FileData
            mFileData) {
        SZLog.e("", "download error.");
        removeParserId(mFileData.getItemId());
        removeFileProgress(mFileData.getItemId());
        mLocalBroadcastManager.sendBroadcast(new Intent(DownloadPatService.ACTION_ERROR)
                .putExtra(KeyHelp.DF_FILEDATA, mFileData));
    }


    /**
     * 连接server失败,实际等同error
     */
    public static void connectError(LocalBroadcastManager mLocalBroadcastManager, FileData
            mFileData) {
        SZLog.e("", "connect Server failed");
        removeFileProgress(mFileData.getItemId());
        mLocalBroadcastManager.sendBroadcast(new Intent(DownloadPatService.ACTION_ERROR)
                .putExtra(KeyHelp.DF_FILEDATA, mFileData));
    }

    /**
     * 请求错误，code !=1000
     */
    public static void requestError(LocalBroadcastManager mLocalBroadcastManager, FileData
            mFileData, String code) {
        removeFileProgress(mFileData.getItemId());
        Intent intent = new Intent(DownloadPatService.ACTION_REQUEST_ERROR);
        intent.putExtra(KeyHelp.DF_FILEDATA, mFileData);
        intent.putExtra(KeyHelp.DF_CODE, code);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    /**
     * 正在打包
     */
    public static void packaging(LocalBroadcastManager mLocalBroadcastManager, FileData mFileData) {
        Intent intent = new Intent(DownloadPatService.ACTION_PACKAGING);
        intent.putExtra(KeyHelp.DF_FILEDATA, mFileData);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    /**
     * 发送进度
     *
     * @param mPercentage    进度百分比
     * @param mCurrentSize   当前大小
     * @param isLastProgress 是否是最后一次保存的进度(发送最后一次的进度更新,防止界面显示进度误差)
     */
    public static void sendProgress(LocalBroadcastManager mLocalBroadcastManager,
                                    FileData mFileData,
                                    int mPercentage,
                                    long mCurrentSize,
                                    boolean isLastProgress) {
        Intent intent = new Intent(DownloadPatService.ACTION_UPDATE);
        intent.putExtra(KeyHelp.DF_FILEDATA, mFileData);
        intent.putExtra(KeyHelp.DF_PROGRESS, mPercentage);
        intent.putExtra(KeyHelp.DF_CURRENTSIZE, mCurrentSize);
        intent.putExtra(KeyHelp.DF_ISLAST, isLastProgress);
        mLocalBroadcastManager.sendBroadcast(intent);
        if (isLastProgress) {
            //暂停状态，清除临时保存的进度
            removeFileProgress(mFileData.getItemId());
        } else {
            saveFileProgress(mFileData.getItemId(), mPercentage);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    /**
     * 文件下载接口 请求下载的参数（顺序不可变）
     *
     * @param o
     * @return
     */
    public static LinkedHashMap<String, String> createDownloadParams(FileData o) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("myProductId", o.getMyProId());
        map.put("itemId", o.getItemId());
        map.put("username", SZConstant.getName());
        map.put("device", DrmPat.APP_DEVICE);
        map.put("intention", DrmPat.APP_INTENTION);
        map.put("IMEI", Constant.TOKEN);
        String token = SZConstant.getToken();
        map.put("token", token);
        String authKey = SecurityUtil.encodeMD5BySalt(getMapParamsString(map), token);
        map.put("authKey", authKey);
        return map;
    }

    /**
     * 请求证书接口 请求的参数 （顺序不可变）
     *
     * @param o
     * @return
     */
    public static LinkedHashMap<String, String> createCertificateParams(FileData o) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("myProductId", o.getMyProId());
        map.put("itemId", o.getItemId());
        map.put("username", SZConstant.getName());
        map.put("application_name", DrmPat.APP_FULLNAME);
        map.put("app_version", CommonUtil.getAppVersionName(SZApplication.getInstance()));
        String token = SZConstant.getToken();
        map.put("token", token);
        String authKey = SecurityUtil.encodeMD5BySalt(getMapParamsString(map), token);
        map.put("authKey", authKey);
        return map;
    }

    /**
     * 获取Map内添加的字符串，进行拼接
     *
     * @param params
     * @return
     */
    public static String getMapParamsString(LinkedHashMap params) {
        String preParams = "";
        Iterator iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object val = entry.getValue();
            preParams += String.valueOf(val);
        }
        //DRMLog.d("preParams: " + preParams);
        return preParams;
    }


}
