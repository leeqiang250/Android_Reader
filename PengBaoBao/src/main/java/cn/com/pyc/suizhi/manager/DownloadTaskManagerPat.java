package cn.com.pyc.suizhi.manager;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.models.xml.OEX_Rights;
import com.sz.mobilesdk.models.xml.XML2JSON_Album;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.SecurityUtil;
import com.sz.mobilesdk.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LogerHelp;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.LogConfig;
import cn.com.pyc.suizhi.help.DownloadHelp;
import cn.com.pyc.suizhi.manager.db.DownDataPat;
import cn.com.pyc.suizhi.manager.db.DownDataPatDBManager;
import cn.com.pyc.suizhi.model.DataModel;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.util.ParserDRMUtil;
import cn.com.pyc.suizhi.util.SZAPIUtil;
import cn.com.pyc.suizhi.util.SZPathUtil;

import static cn.com.pyc.loger.intern.LogerHelp.getLineNumber;

/**
 * desc:  下载任务管理类(证书分离后，使用http下载)      <br/>
 * author: hudaqiang       <br/>
 * update at 2017/6/23 15:58
 */
public class DownloadTaskManagerPat {
    // 开始，默认
    public static final int INIT = 0;
    // 等待
    public static final int WAITING = 1;
    // 连接
    public static final int CONNECTING = 2;
    // 暂停
    public static final int PAUSE = 3;
    // 下载中,更新进度中
    public static final int DOWNLOADING = 4;
    // 解析
    public static final int PARSERING = 5;
    // 下载异常，ftpPath关闭,服务端异常
    public static final int DOWNLOAD_ERROR = 6;
    // 正在打包
    public static final int PACKAGING = 7;
    // 下载完成
    public static final int FINISHED = 8;

    private static final String TAG = "dtmp";
    private Context mContext;
    private FileData mFileData;
    private DecimalFormat mDecimalFormat;
    /**
     * 标示线程是否暂停
     */
    public boolean isPause = false;
    public boolean isClose = false;

    private static ExecutorService mFixedExecutor;
    private LocalBroadcastManager mLocalBroadcastManager;
    private DownDataPatDBManager mDBManager;
    // 线程池大小
    private static final int POOL_SIZE = 4;

    private ReentrantLock sLock;

    public DownloadTaskManagerPat(Context context, FileData data) {
        mContext = context;
        mFileData = data;
        mDecimalFormat = new DecimalFormat("#.#");
        if (mFixedExecutor == null) {
            mFixedExecutor = Executors.newFixedThreadPool(POOL_SIZE);
        }
        if (mLocalBroadcastManager == null) {
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        }
        if (mDBManager == null) {
            mDBManager = DownDataPatDBManager.Builder();
        }
        if (sLock == null) {
            sLock = new ReentrantLock(true);
        }
    }

    private void lock() {
        if (sLock != null) {
            sLock.lock();
        }
    }

    private void unlock() {
        if (sLock != null && sLock.isLocked()) {
            sLock.unlock();
            sLock = null;
        }
    }

    /**
     * 关闭
     */
    public static void shutdownPool() {
        if (mFixedExecutor != null && !mFixedExecutor.isShutdown()) {
            try {
                mFixedExecutor.shutdown();
                mFixedExecutor.awaitTermination(3 * 1000, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mFixedExecutor.shutdownNow();
                mFixedExecutor = null;
            }
        }
    }

    /**
     * 开始下载
     */
    public synchronized void download() {
        DownloadThreadPat thread = new DownloadThreadPat(this.mFileData);
        thread.setPriority(Thread.NORM_PRIORITY + 2);
        mFixedExecutor.execute(thread);
    }

    //下载线程类
    private class DownloadThreadPat extends Thread {

        private boolean isThreadDownloading = false; // 标示线程是否在下载中
        private FileData o;

        private DownloadThreadPat(FileData o) {
            this.o = o;
        }

        @Override
        public void run() {
            super.run();
            if (isInterrupted() || isClose) {
                isThreadDownloading = false;
                return;
            }
            // 一、正在连接，获取下载路径
            DownloadHelp.connecting(mLocalBroadcastManager, o);
            SZLog.i("2.connect server,request url.");
            //获取下载链接地址
            String result = cn.com.pyc.suizhi.manager.HttpEngine.getSyncMapString(SZAPIUtil
                    .getFileDownloadUrl(),
                    DownloadHelp.createDownloadParams(o));
            SZLog.d(TAG, "Server Result: " + result);
            if (StringUtil.isEmptyOrNull(result)) {
                //请求服务器返回信息为null，NULL，"";
                DownloadHelp.connectError(mLocalBroadcastManager, o);
                addLog("请求下载地址异常，服务器返回错误: " + result, getLineNumber());
            } else {
                DataModel model = JSON.parseObject(result, DataModel.class);
                if (cn.com.pyc.suizhi.common.Code.SUCCESS.equals(model.getCode())) {
                    String downloadUrl = model.getData().getUrl();
                    final String localPath = new StringBuilder()
                            .append(SZPathUtil.getDRMPrefixPath())
                            .append(File.separator)
                            //.append(o.getItemId())
                            //.append(DrmPat._DRM)
                            .toString();
                    DownDataPat data = mDBManager.findByFileId(o.getItemId());
                    if (data == null) {
                        Log.v(TAG, "first download...");
                        data = new DownDataPat();
                        data.setCompleteSize(0L);
                        data.setFileId(o.getItemId());
                        data.setFileName(o.getContent_name());
                        data.setFileSize(o.getContent_size());
                        data.setDownloadUrl(downloadUrl);
                        data.setLocalPath(localPath);
                        data.setMyProId(o.getMyProId());
                    }
                    // 三、开始下载。
                    startDownloadDRM(data);
                } else if (cn.com.pyc.suizhi.common.Code._16010.equals(model.getCode())) {
                    DownloadHelp.packaging(mLocalBroadcastManager, o);
                } else {
                    SZLog.e("", "ErrorCode：" + model.getCode());
                    DownloadHelp.requestError(mLocalBroadcastManager, o, model.getCode());
                    addLog("ErrorCode：" + model.getCode(), getLineNumber());
                }
            }
        }

        /**
         * 开始下载DRM包
         */
        private void startDownloadDRM(DownDataPat data) {
            SZLog.i("3.connect success,start download task.");
            isThreadDownloading = true;
            String localPath = data.getLocalPath();
            String httpUrl = data.getDownloadUrl();
            long fileSize = data.getFileSize();

            RandomAccessFile rAcFile = null;
            InputStream stream = null;
            HttpURLConnection conn = null;
            try {
                //在本地创建一个与资源同样大小的文件来占位
                File tmpFile = FileUtil.createFile(localPath, data.getFileId() + DrmPat._DRM);
                rAcFile = new RandomAccessFile(tmpFile, "rw");
                // 文件完成大小，即是下载的起始位置
                long offsetSize = data.getCompleteSize();
                //指向的读取位置
                rAcFile.seek(offsetSize);
                long mCurrentSize = offsetSize;
                //请求服务器就加载文件流
                SZLog.d("httpUrl: " + httpUrl);
                URL url = new URL(httpUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setReadTimeout(60 * 1000);
                conn.setConnectTimeout(60 * 1000);
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Charset", "UTF-8");
                //conn.setRequestProperty("Connection", "Keep-Alive");
                //conn.setRequestProperty("Accept-Encoding", "gzip");
                if (offsetSize > 0) {
                    String property = "bytes=" + offsetSize + "-";
                    conn.setRequestProperty("Range", property);
                }
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK
                        || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    stream = conn.getInputStream();
                    //String encoding = conn.getContentEncoding();
                    //if (encoding != null && encoding.contains("gzip")) {
                    //首先判断服务器返回的数据是否支持gzip压缩，如果支持则应该使用GZIPInputStream解压，否则会出现乱码无效数据
                    //    stream = new GZIPInputStream(conn.getInputStream());
                    //}
                } else {
                    Log.e(TAG, "requestServer failed: code = " + responseCode);
                }
                byte[] buffer = new byte[2048];
                int length = -1;
                SZLog.i("4.get stream, start write files.");
                long time = 0;
                double mTempPercent = 0d;
                while (isThreadDownloading && (length = stream.read(buffer)) != -1) {
                    rAcFile.write(buffer, 0, length);
                    mCurrentSize += length;
                    int mPercentage = (int) (mCurrentSize * 100 / fileSize);
                    double mCachePercent = ConvertToUtil.toDouble(mDecimalFormat.format
                            (mCurrentSize * 100d / fileSize));
                    if (isPause) {
                        isThreadDownloading = false;
                        data.setCompleteSize(mCurrentSize);
                        data.setProgress(mPercentage);
                        DownDataPatDBManager.Builder().saveOrUpdate(data);
                        DownloadHelp.sendProgress(mLocalBroadcastManager, o, mPercentage,
                                mCurrentSize, true);
                        return;
                    }
                    // 通知ui更新进度
                    if (System.currentTimeMillis() - time > 700) {
                        SZLog.d(TAG, "mCachePercent = " + mCachePercent);
                        if (mCachePercent > mTempPercent) {
                            DownloadHelp.sendProgress(mLocalBroadcastManager, o, mPercentage,
                                    mCurrentSize, false);
                        }
                        time = System.currentTimeMillis();
                        mTempPercent = mCachePercent;
                    }
                }
                SZLog.i("5.download complete，mCurrentSize = "
                        + mCurrentSize + ";fileSize = " + fileSize);

                if (isThreadDownloading) {
                    //通知界面开始解析
                    DownloadHelp.parsering(mLocalBroadcastManager, o);
                    lock();
                    downloadRightAndResolving();
                }
            } catch (Exception e) {
                e.printStackTrace();
                DownloadHelp.connectError(mLocalBroadcastManager, o);
                File temp = new File(localPath, data.getFileId() + DrmPat._DRM);
                if (temp.exists()) {
                    temp.delete();
                }
            } finally {
                if (rAcFile != null) {
                    try {
                        rAcFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //deleteDrm(localPath);
                //deleteRight(o);
                unlock();
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        //下载drm完毕，开始下载证书并解析drm
        private void downloadRightAndResolving() throws Exception {
            //1.下载证书
            //OEX_Rights right = startDownloadCertificate(o);
            String certificate = cn.com.pyc.suizhi.manager.HttpEngine.getSyncMapString(SZAPIUtil
                    .getFileCertificate(),
                    DownloadHelp.createCertificateParams(o));
            SZLog.d(TAG, "Certificate: " + certificate);
            DataModel model = JSON.parseObject(certificate, DataModel.class);
            OEX_Rights rights = null;
            if (StringUtil.isEmptyOrNull(certificate)) {
                //请求服务器返回信息为null，NULL，"";
                DownloadHelp.connectError(mLocalBroadcastManager, o);
                addLog("请求证书异常，服务器返回错误: " + certificate, getLineNumber());
            } else {
                if (cn.com.pyc.suizhi.common.Code.SUCCESS.equals(model.getCode())) {
                    String desFileInfo = model.getData().getFileInfo();
                    if (desFileInfo != null) {
                        byte[] byteArray = SecurityUtil.hexStringtoByteArray(model.getData()
                                .getFileInfo());
                        String xmlStream = AESUtil.decrypt(byteArray, Constant.XML_SECRET);
                        rights = ParserDRMUtil.getInstance().parserRightUnique(o, xmlStream);
                    } else {
                        DownloadHelp.error(mLocalBroadcastManager, o);
                        addLog("请求证书失败，服务器返回错误: " + certificate, getLineNumber());
                    }
                } else {
                    SZLog.e("", "ErrorCode：" + model.getCode());
                    DownloadHelp.requestError(mLocalBroadcastManager, o, model.getCode());
                    addLog("请求证书失败，服务器返回错误: " + model.getCode(), getLineNumber());
                }
            }
            if (rights != null) {
                //2.解析DRM
                List<cn.com.pyc.suizhi.manager.ParserEngine.CommonFile> files = ParserDRMUtil
                        .getInstance().parserDRMFileUnique(o);
                SZLog.e("", "parser file size: " + files.size());
                //3.解析AlbumInfo
                XML2JSON_Album albumInfo = ParserDRMUtil.getInstance().parserAlbumInfoUnique(files);
                //4.插入数据库
                if (albumInfo != null) {
                    boolean result = ParserDRMUtil.getInstance().insertFileData(rights,
                            albumInfo, files, o);
                    if (result) {
                        //插入成功,解析完成！
                        DownloadHelp.finished(mLocalBroadcastManager, o);
                    } else {
                        DownloadHelp.error(mLocalBroadcastManager, o);
                    }
                } else {
                    DownloadHelp.error(mLocalBroadcastManager, o);
                    addLog("albumInfo is null；obj: " + o.toString(), getLineNumber());
                }
            }
        }
    }

    //清除下载的drm包,如果存在的话
//    private void deleteDrm(String drmPath) {
//        FileUtils.deleteFileWithPath(drmPath);
//    }

    //删除Right.xml，如果存在的话
//    private void deleteRight(FileData o) {
//        final String rightPath = new StringBuilder()
//                .append(PathUtil.getFilePrefixPath())
//                .append(File.separator)
//                .append(o.getMyProId())
//                .append(File.separator)
//                .append(o.getItemId())
//                .append(DrmPat._XML).toString();
//        FileUtils.deleteFileWithPath(rightPath);
//    }

        /*
         * 请求下载证书，一般在文件下载解析完成后
         */
//        private OEX_Rights startDownloadCertificate(FileData o) {
//            String result = HttpEngine.getSyncMapString(APIUtil.getFileCertificate(),
//                    DownloadHelp.createCertificateParams(o));
//            DRMLog.d(TAG, "Certificate Result: " + result);
//            DataModel model = JSON.parseObject(result, DataModel.class);
//            OEX_Rights rights = null;
//            if (StringUtil.isEmptyOrNull(result)) {
//                //请求服务器返回信息为null，NULL，"";
//                DownloadHelp.connectError(mLocalBroadcastManager, o);
//                int line = getLineNumber();
//                addLog("请求证书异常，服务器返回错误: " + result, line);
//            } else {
//                if (Code.SUCCESS.equals(model.getCode())) {
//                    String desFileInfo = model.getData().getFileInfo();
//                    if (desFileInfo != null) {
//                        byte[] byteArray = SecurityUtil.hexStringtoByteArray(model.getData()
//                                .getFileInfo());
//                        String xmlStream = AESUtil.decrypt(byteArray, SZConfig.XML_SECRET);
//                        //DRMLog.e("xmlStream：" + xmlStream);
//                        rights = ParserDRMUtil.getInstance().parserRightUnique(o, xmlStream);
//                    } else {
//                        addLog("请求证书失败，服务器返回错误: " + result, getLineNumber());
//                    }
//                } else {
//                    DRMLog.e("错误码：" + model.getCode());
//                    DownloadHelp.requestError(mLocalBroadcastManager, o, model.getCode());
//                }
//            }
//            return rights;
//        }

            /*
             * 请求下载的drm文件流
             */
//            private InputStream requestServer(String httpUrl, long range) throws Exception {
//            DRMLog.e("httpUrl: " + httpUrl);
//            URL url = new URL(httpUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setUseCaches(false);
//            conn.setReadTimeout(60 * 1000);
//            conn.setConnectTimeout(60 * 1000);
//            conn.setRequestProperty("Accept", "*/*");
//            conn.setRequestProperty("Charset", "UTF-8");
//            //conn.setRequestProperty("Connection", "Keep-Alive");
//            //conn.setRequestProperty("Accept-Encoding", "gzip");
//            if (range > 0) {
//                String property = "bytes=" + range + "-";
//                conn.setRequestProperty("Range", property);
//            }
//
//            InputStream stream = null;
//            int responseCode = conn.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK
//                    || responseCode == HttpURLConnection.HTTP_PARTIAL) {
//                stream = conn.getInputStream();
//                //DRMLog.e("Content-Length: " + conn.getContentLength());
//                //String encoding = conn.getContentEncoding();
//                //if (encoding != null && encoding.contains("gzip")) {
//                //首先判断服务器返回的数据是否支持gzip压缩，如果支持则应该使用GZIPInputStream解压，否则会出现乱码无效数据
//                //    stream = new GZIPInputStream(conn.getInputStream());
//                //}
//            } else {
//                Log.e(TAG, "requestServer failed: code = " + responseCode);
//            }
//            //conn.disconnect();
//            return stream;
//        }


    private void addLog(String content, int line) {
        ExtraParams params = LogConfig.getBaseExtraParams();
        params.file_name = LogerHelp.getFileName();
        params.lines = line;
        LogerEngine.debug(this.mContext, content, params);
    }
}
