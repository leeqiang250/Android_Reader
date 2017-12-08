package com.sz.mobilesdk.util;

import android.os.Environment;

import java.io.File;

public class PathUtil {

    /**
     * 文件解析保存路径,name = "guestpbb_shareId" <br/>
     * eg:sdcard/Android/data/SZOnline/name/file
     */
    public static String DEF_SAVE_FILE_PATH = null;

    /**
     * DRM包下载保存路径，name = "guestpbb_shareId"<br/>
     * eg:sdcard/Android/data/SZOnline/name/download
     */
    public static String DEF_DOWNLOAD_DRM_PATH = null;

    /**
     * sdCard
     *
     * @return
     */
    public static final String getSDCard() {
        return Environment.getExternalStorageDirectory().toString();
    }

    /**
     * TODO:
     * 文件存储位置路径偏移:文件存储
     * <p>
     * <p>
     * 下载文件开始时放在Android/data/SZOnline文件夹下的，<br/>
     * 如果需要保存到Android/data/cn.com.pyc.pbb/SZOnline目录下<br/>
     * 因为新老版本的兼容性问题,可能需要迁移数据。
     *
     * @return Android/data/cn.com.pyc.pbb/SZOnline
     */
    public static final String getSZOffset() {
        return "Android/data/SZOnline";
    }

    /**
     * 图片默认缓存路径
     *
     * @return sdcard/Android/data/cn.com.pyc.pbb/SZOnlineImg/cache
     */
    public static final String getSZImageCachePath() {
        return getSDCard() + "/Android/data/cn.com.pyc.pbb/SZOnlineImg/cache";
    }

    /**
     * 高斯处理图片保存路径
     *
     * @return sdcard/Android/data/cn.com.pyc.pbb/SZOnlineImg/fuzzy
     */
    public static final String getSZFuzzyCachePath() {
        return getSDCard() + "/Android/data/cn.com.pyc.pbb/SZOnlineImg/fuzzy";
    }

    /**
     * 应用sdcard默认前置路径
     *
     * @return sdCard/PBBReader
     */
    public static final String getSZSDRootOffset() {
        return getSDCard() + "/PBBReader";
    }

    /**
     * 应用sd卡缓存目录
     *
     * @return SDCard/PBBReader/SZOnline/
     */
    public static final String getSZSDRootPath() {
        return getSZSDRootOffset() + "/SZOnline/";
    }

    /**
     * 应用sd卡崩溃日志记录目录
     *
     * @return SDCard/PBBReader/SZOnline/crash/
     */
    public static final String getSZCrashPath() {
        return getSZSDRootPath() + "crash/";
    }

    /**
     * 创建应用的缓存目录
     */
    public static void createCacheDirectory() {
        FileUtil.createDirectory(PathUtil.getSZSDRootPath());
        FileUtil.createDirectory(PathUtil.getSZImageCachePath());
        FileUtil.createDirectory(PathUtil.getSZFuzzyCachePath());
    }

    /**
     * 创建保存下载文件的目录； 登录之后创建以name命名的目录
     *
     * @param name 唯一名称
     */
    public static void createSaveFilePath(String name) {
        if ("".equals(name))
            throw new IllegalArgumentException("name is not allow empty string");

        if (DEF_SAVE_FILE_PATH == null) {
            DEF_SAVE_FILE_PATH = getFilePath(name);
        }

        if (DEF_DOWNLOAD_DRM_PATH == null) {
            DEF_DOWNLOAD_DRM_PATH = getDRMPath(name);
        }

        FileUtil.createDirectory(DEF_SAVE_FILE_PATH);
        FileUtil.createDirectory(DEF_DOWNLOAD_DRM_PATH);
    }

    /**
     * 检查保存路径
     *
     * @param name
     */
    public static void checkSaveFilePath(String name) {
        if ("".equals(name))
            throw new IllegalArgumentException("name is not allow empty string");

        if (DEF_SAVE_FILE_PATH == null) {
            DEF_SAVE_FILE_PATH = getFilePath(name);
            SZLog.v("", "check fileDirs success");
        }
        FileUtil.createDirectory(DEF_SAVE_FILE_PATH);

        if (DEF_DOWNLOAD_DRM_PATH == null) {
            DEF_DOWNLOAD_DRM_PATH = getDRMPath(name);
            SZLog.v("", "check downloadDirs success");
        }
        FileUtil.createDirectory(DEF_DOWNLOAD_DRM_PATH);
    }

    /**
     * 保存下载文件的目录； 用户名切换后，对应的用户目录重新初始化
     */
    public static void destorySaveFilePath() {
        DEF_SAVE_FILE_PATH = null;
        DEF_DOWNLOAD_DRM_PATH = null;
    }

    /**
     * 解析后文件存储路径
     *
     * @param name
     * @return sdCard/Android/data/cn.com.pyc.pbb/SZOnline/name/file
     */
    private static final String getFilePath(String name) {
        return new StringBuffer()
                .append(getSDCard())
                .append(File.separator)
                .append(getSZOffset())
                .append(File.separator)
                .append(name)
                .append("/file").toString();
    }

    /**
     * drm保存路径
     *
     * @param name
     * @return sdCard/Android/data/cn.com.pyc.pbb/SZOnline/name/download
     */
    private static final String getDRMPath(String name) {
        return new StringBuffer()
                .append(getSDCard())
                .append(File.separator)
                .append(getSZOffset())
                .append(File.separator)
                .append(name)
                .append("/download").toString();
    }
}
