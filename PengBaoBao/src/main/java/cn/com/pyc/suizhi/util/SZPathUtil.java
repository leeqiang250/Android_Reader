package cn.com.pyc.suizhi.util;

import android.os.Environment;

import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.TimeUtil;

import java.io.File;
import java.util.Date;

import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.utils.CrashHandler;

/**
 * 路径操作util类
 *
 * @author hudq
 */
public class SZPathUtil {

    /**
     * 文件解析保存路径,登陆后需重新初始化,注销后置为null <br/>
     * eg: Android/data/DRM/username/file
     */
    private static String DEF_FILE_PATH = null;
    /**
     * DRM包下载保存路径，登陆后需重新初始化 ,注销后置为null <br/>
     * eg: Android/data/DRM/username/Download
     */
    private static String DEF_DOWNLOAD_PATH = null;
    /**
     * 歌词保存路径 username初始值为null，登陆后需重新初始化 ,注销后置为null <br/>
     * eg: Android/data/DRM/username/lyric
     */
    private static String DEF_LYRIC_PATH = null;

    /**
     * 文件存储路径（使用accountId代替userName作为唯一）
     *
     * @return SDCard/Android/data/DRM/accountId/file
     */
    public static String getFilePrefixPath() {
        if (DEF_FILE_PATH == null) {
            //使用accountId作为唯一用户标识
            DEF_FILE_PATH = getFilePrefixPath(SZConstant.getAccountId());
        }
        FileUtil.createDirectory(DEF_FILE_PATH);
        return DEF_FILE_PATH;
    }

    /**
     * 下载DRM包保存路径
     *
     * @return SDCard/Android/data/DRM/accountId/download
     */
    public static String getDRMPrefixPath() {
        if (DEF_DOWNLOAD_PATH == null) {
            //使用accountId作为唯一用户标识
            DEF_DOWNLOAD_PATH = getDRMPrefixPath(SZConstant.getAccountId());
        }
        FileUtil.createDirectory(DEF_DOWNLOAD_PATH);
        return DEF_DOWNLOAD_PATH;
    }

    /**
     * 歌词文件路径
     *
     * @return SDCard/Android/data/DRM/accountId/lyric
     */
    public static String getLrcPrefixPath() {
        if (DEF_LYRIC_PATH == null) {
            //使用accountId作为唯一用户标识
            DEF_LYRIC_PATH = getLrcPrefixPath(SZConstant.getAccountId());
        }
        FileUtil.createDirectory(DEF_LYRIC_PATH);
        return DEF_LYRIC_PATH;
    }

    /**
     * 图片的缓存路径
     */
    public final static String DEF_IMAGE_PATH = new StringBuilder()
            .append(getSDCardRootPath()).append(getDefaultOffset())
            .append("img_cache").toString();
    /**
     * 音乐高斯模糊img的缓存路径
     */
    public final static String DEF_FUZZY_PATH = new StringBuilder()
            .append(getSDCardRootPath()).append(getDefaultOffset())
            .append("img_fuzzy").toString();

    /**
     * sd卡根目录： SDCard/
     */
    public static String getSDCardRootPath() {
        return Environment.getExternalStorageDirectory().toString() + "/";
    }

    /**
     * 存储位置路径偏移 ：Android/data/DRM/
     */
    public static String getDefaultOffset() {
        //return "Android/data/DRM/";
        return "Android/data/cn.com.pyc.pbb/DRM/";
    }

    /**
     * 图片默认缓存路径,同DEF_IMAGE_PATH
     *
     * @return SDCard/Android/data/DRM/img_cache
     */
    public static String getDefaultImageCacheOffset() {
        return getDefaultOffset() + "img_cache";
    }

    /**
     * 应用sd卡缓存目录： SDCard/suizhi/
     */
    public static String getDefaultSDCardRootPath() {
        return getSDCardRootPath() + "suizhi/";
    }

    /**
     * 创建保存下载文件的目录
     */
    public static void createFilePath() {
        destoryFilePath();
        String userId = SZConstant.getAccountId();//使用accountId作为唯一用户标识
        if (DEF_FILE_PATH == null) {
            DEF_FILE_PATH = getFilePrefixPath(userId);
        }
        if (DEF_DOWNLOAD_PATH == null) {
            DEF_DOWNLOAD_PATH = getDRMPrefixPath(userId);
        }
        if (DEF_LYRIC_PATH == null) {
            DEF_LYRIC_PATH = getLrcPrefixPath(userId);
        }
        createDirectory();
        createDirectoryAfterLogin();
    }

    /**
     * 用户名切换后，对应的用户目录重新初始化
     */
    public static void destoryFilePath() {
        DEF_FILE_PATH = null;
        DEF_DOWNLOAD_PATH = null;
        DEF_LYRIC_PATH = null;
    }

    /**
     * 创建应用的缓存目录
     */
    private static void createDirectory() {
        FileUtil.createDirectory(getDefaultSDCardRootPath());
        FileUtil.createDirectory(DEF_IMAGE_PATH);
        FileUtil.createDirectory(DEF_FUZZY_PATH);
    }

    /**
     * 登录之后创建用户名称的目录
     */
    private static void createDirectoryAfterLogin() {
        FileUtil.createDirectory(DEF_FILE_PATH);
        FileUtil.createDirectory(DEF_DOWNLOAD_PATH);
        FileUtil.createDirectory(DEF_LYRIC_PATH);
    }

    /**
     * 检查sd卡上存储的日志文件
     */
    public static void checkSDCardCrashLog() {
        // sdcard/suizhi/crash/
        final int maxLogNum = 30;
        String path = getDefaultSDCardRootPath() + "crash" + File.separator;
        File dir = new File(path);
        if (!dir.exists()) return;
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        int fileCount = files.length;
        SZLog.i("log count: " + fileCount);
        if (fileCount > maxLogNum) {
            clearCrashLogs(files);
        }
    }

    // 清除保存的log
    private static void clearCrashLogs(File[] files) {
        try {
            for (File file : files) {
                if (file != null && file.isFile()) {
                    // name: 2015-11-19-16-52-39-453
                    String fileName = FileUtil.getNameFromFileName(file
                            .getName());
                    Date date = TimeUtil.getDateFromDateString(fileName,
                            CrashHandler.LOGPATTERN);
                    if (date != null && new Date().after(date)) {
                        // 删除除今天之前记录的log
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载DRM文件保存路径
     *
     * @return SDCard/Android/data/DRM/accountId/download
     */
    private static String getDRMPrefixPath(String userName) {
        StringBuilder mDownloadSb = new StringBuilder();
//        String toDir = getSZPathOffset(Constant.getLoginName(), userName);
//        return mDownloadSb
//                .append(toDir)
//                .append("/")
//                .append("download").toString();
        return mDownloadSb.append(getSDCardRootPath())
                .append(getDefaultOffset())
                .append(userName)
                .append("/")
                .append("download").toString();
    }

    /**
     * 文件存储路径
     *
     * @return eg: SDCard/Android/data/DRM/accountId/file
     */
    private static String getFilePrefixPath(String userName) {
        StringBuilder mFileSb = new StringBuilder();
//        String toDir = getSZPathOffset(Constant.getLoginName(), userName);
//        return mFileSb
//                .append(toDir)
//                .append("/")
//                .append("file").toString();
        return mFileSb.append(getSDCardRootPath())
                .append(getDefaultOffset())
                .append(userName)
                .append("/")
                .append("file").toString();
    }

    /**
     * 歌词文件路径
     *
     * @return SDCard/Android/data/DRM/accountId/lyric
     */
    private static String getLrcPrefixPath(String userName) {
        StringBuilder mLrcSb = new StringBuilder();
//        String toDir = getSZPathOffset(Constant.getLoginName(), userName);
//        return mLrcSb
//                .append(toDir)
//                .append("/")
//                .append("lyric").toString();
        return mLrcSb.append(getSDCardRootPath())
                .append(getDefaultOffset())
                .append(userName)
                .append("/")
                .append("lyric").toString();
    }


    /*
     * 更改文件保存的路径，如果不是在唯一用户名下，修改成唯一用户名的路径
     *
     * @param loginName 登录输入的名称
     * @param name      唯一用户名标识（超级用户名）
     * @return eg：SDCard/Android/data/DRM/username
     */
//    private static String getSZPathOffset(String loginName, String name) {
//        String fromDir = new StringBuilder()
//                .append(getSDCardRootPath())
//                .append(getDefaultOffset())
//                .append(loginName)
//                .toString();
//        String toDir = new StringBuilder()
//                .append(getSDCardRootPath())
//                .append(getDefaultOffset())
//                .append(name)
//                .toString();
//        if (FileUtils.checkFilePathExists(toDir))
//            return toDir;
//        File fl = new File(fromDir);
//        if (fl.exists() && fl.isDirectory()) {
//            FileUtils.renameDirectory(fromDir, toDir);
//        }
//        return toDir;
//    }

}
