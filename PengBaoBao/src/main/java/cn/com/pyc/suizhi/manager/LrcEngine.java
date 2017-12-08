package cn.com.pyc.suizhi.manager;

import android.os.Bundle;
import android.text.TextUtils;

import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

import cn.com.pyc.suizhi.bean.event.MusicLrcEvent;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.util.SZAPIUtil;
import cn.com.pyc.suizhi.util.SZPathUtil;
import de.greenrobot.event.EventBus;

/**
 * 歌词管理
 *
 * @author hudq
 */
public class LrcEngine {

    /**
     * 歌词是否存在本地
     *
     * @param myProductId 我的购买专辑id
     * @param lrcId       歌词id
     * @return Boolean
     */
    public static boolean existLyric(String myProductId, String lrcId) {
        if (StringUtil.isEmptyOrNull(lrcId)) return false;
        String lrcPath = getLyricPath(myProductId, lrcId);
        return FileUtil.checkFilePathExists(lrcPath);
    }

    /**
     * 删除歌词
     *
     * @param myProductId 我的购买专辑id
     * @param lrcId       歌词id
     * @return Boolean
     */
    public static boolean deleteLyric(String myProductId, String lrcId) {
        return !StringUtil.isEmptyOrNull(lrcId)
                && FileUtil.deleteFileWithPath(getLyricPath(myProductId, lrcId));
    }

    /**
     * 歌词的保存路径
     *
     * @param myProductId 我的购买专辑id
     * @param lrcId       歌词名，即歌词id
     * @return String  eg: SDCard/Android/data/DRM/username/lyric/myProductId/lrcId.lrc
     */
    public static String getLyricPath(String myProductId, String lrcId) {
        return getLyricPrefixPath(myProductId) + File.separator + lrcId + DrmPat._LRC;
    }

    /**
     * 从本地路径获取歌词id
     *
     * @param lrcPath 歌词保存的本地路径
     * @return String
     */
    public static String getLrcIdByPath(String lrcPath) {
        if (FileUtil.checkFilePathExists(lrcPath)) {
            int start = lrcPath.lastIndexOf('/');
            int end = lrcPath.lastIndexOf('.');
            if (start != -1 && end != -1) {
                return lrcPath.substring(start + 1, end);
            }
        }
        return "";
    }

    /**
     * 下载歌词
     *
     * @param myProductId 我的购买专辑id
     * @param lrcId       歌词id
     */
    public static void getLyric(final String myProductId, final String lrcId) {
        if (TextUtils.isEmpty(lrcId)) return;
        Bundle bundle = new Bundle();
        bundle.putString("username", SZConstant.getName());
        bundle.putString("token", SZConstant.getToken());
        bundle.putString("musicLyric_id", lrcId);
        final String apiUrl = SZAPIUtil.getLrcHttpUrl(lrcId);
        HttpEngine.post(apiUrl, bundle, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //DRMLog.d("lrc-http: " + result);
                //result = "http://192.168.85.13/DRM/告白气球.lrc";
//                try {
//                    //result = Uri.encode(result, "-![.:/,%?&=]");
//                    //result = URLEncoder.encode(result, "utf-8");
//                    //Log.e("result", result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                downloadLrc(result, myProductId, lrcId);
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                SZLog.e("","onError:" + throwable.getMessage());
            }

            @Override
            public void onCancelled(CancelledException e) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    //eg: SDCard/Android/data/DRM/username/lyric/myProductId
    private static String getLyricPrefixPath(String myProductId) {
        return new StringBuilder()
                .append(SZPathUtil.getLrcPrefixPath())
                .append(File.separator)
                .append(myProductId)
                .toString();
    }

    //下载歌词
    private static void downloadLrc(final String sourceUrl, final String myProductId, final String
            lrcName) {
        FileUtil.createDirectory(getLyricPrefixPath(myProductId));
        RequestParams params = new RequestParams(sourceUrl);
        params.setUseCookie(false);
        params.setConnectTimeout(30 * 1000);
        params.setAutoResume(false);            // 设置是否断点续传
        params.setAutoRename(false);            // 设置是否自动命名
        params.setSaveFilePath(getLyricPath(myProductId, lrcName));
        x.http().get(params, new Callback.ProgressCallback<File>() {

            @Override
            public void onWaiting() {
            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onSuccess(File file) {
                //提示加载歌词
                EventBus.getDefault().post(new MusicLrcEvent(file.getAbsolutePath(),
                        MusicLrcEvent.Way.LRC_LOAD));
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                //下载歌词失败
                SZLog.e("","onError:" + throwable.getMessage());
            }

            @Override
            public void onCancelled(CancelledException e) {
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onLoading(long l, long l1, boolean b) {
            }
        });
    }
}
