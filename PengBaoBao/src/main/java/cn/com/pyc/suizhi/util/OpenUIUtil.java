package cn.com.pyc.suizhi.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sz.help.KeyHelp;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.suizhi.SZListFileActivity;
import cn.com.pyc.suizhi.SZMusicPlayActivity;
import cn.com.pyc.suizhi.SZPDFActivity;
import cn.com.pyc.suizhi.SZVideoPlayerActivity;
import cn.com.pyc.suizhi.SZWebViewActivity;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.help.MusicHelp;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.model.ProductInfo;

/**
 * 管理打开其他de页面的工具类
 *
 * @author hudq
 */
public class OpenUIUtil {

    /**
     * 开启页面
     *
     * @param ctx
     * @param cls
     */
    public static void openActivity(Context ctx, Class<?> cls) {
        openActivity(ctx, cls, null);
    }

    /**
     * 通过类名启动Activity，并且含有Bundle数据
     *
     * @param ctx
     * @param cls
     * @param bundle 传递的bundle数据
     */
    public static void openActivity(Context ctx, Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(ctx, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        ctx.startActivity(intent);
    }

    // //////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////

    /**
     * 打开系统拨号界面
     *
     * @param context
     * @param phoneNumber
     */
    public static void openSystemDialPage(Context context, String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(context, "号码为空~", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            context.startActivity(Intent.createChooser(intent, null));
        }
    }

    /**
     * 打开系统浏览器
     *
     * @param context
     * @param url
     */
    public static void openBrowserOfSystem(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(Intent.createChooser(intent, null));
    }

    //发现页打开app浏览器，去购买
    public static void openWebViewOfApp2Buy(Context context, String url) {
        Intent intent = new Intent(context, SZWebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("discover_buy", true);
        context.startActivity(intent);
    }

    // //////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////

    /**
     * 打开文件列表视图(在线)
     *
     * @param activity Context
     * @param o        ProductInfo
     */
    public static void openFileListPage(Activity activity, ProductInfo o) {
        Intent intent = new Intent(activity, SZListFileActivity.class);
        intent.putExtra("ProductInfo", o);
        activity.startActivity(intent);
    }

    /**
     * 打开文件列表视图（离线可用）
     * 2017-02-15 修改
     *
     * @param activity
     * @param myProId
     * @param albumName
     * @param albumCategory
     * @param albumPic
     */
    public static void openFileListPage(Activity activity, String myProId, String
            albumName, String albumCategory, String albumPic, String author, String ratio) {

        //构造一个Product,给其中的字段赋值
        ProductInfo o = new ProductInfo();
        o.setMyProId(myProId);
        o.setProductName(albumName);
        o.setPicture_url(albumPic);
        o.setCategory(albumCategory);
        o.setAuthors(author);
        o.setPicture_ratio(ratio);

        Intent intent = new Intent(activity, SZListFileActivity.class);
        intent.putExtra("ProductInfo", o);
        //intent.putExtra("category", albumCategory);
        activity.startActivity(intent);

        //		Intent intent = new Intent(activity, ListFilesActivity2.class);
        //		intent.putExtra("myProId", myProId);
        //		intent.putExtra("productName", albumName);
        //		intent.putExtra("productUrl", albumPic);
        //		intent.putExtra("albumType", albumCategory);
        //		activity.startActivity(intent);
        //		UIHelper.startInAnim(activity);
    }

    /**
     * 打开并下载
     *
     * @param activity
     * @param o
     * @param fileId
     */
    public static void openFileListPageToDownload(Activity activity, ProductInfo o, String fileId) {
        Intent intent = new Intent(activity, SZListFileActivity.class);
        intent.putExtra("ProductInfo", o);
        intent.putExtra("download_fileId", fileId);
        activity.startActivity(intent);
    }

    /**
     * 打开对应的播放器
     *
     * @param aty
     * @param category
     * @param myProductId
     * @param productName
     * @param productUrl
     * @param fileId
     * @param lrcId
     * @param cacheData   //增加参数文件列表数据
     */
    public static void openPage(Activity aty,
                                String category,
                                String myProductId,
                                String productName,
                                String productUrl,
                                String fileId,
                                String lrcId,
                                List<FileData> cacheData) {
        Bundle bundle = new Bundle();
        bundle.putString(KeyHelp.KEY_MYPRO_ID, myProductId); //必传
        bundle.putString(KeyHelp.KEY_PRO_NAME, productName);
        bundle.putString(KeyHelp.KEY_PRO_URL, productUrl);
        bundle.putString(KeyHelp.KEY_FILE_ID, fileId);
        bundle.putString(KeyHelp.KEY_LRC_ID, lrcId);
        if (cacheData != null) { //增加参数文件列表数据
            bundle.putParcelableArrayList(KeyHelp.KEY_FILE_LIST,
                    (ArrayList<? extends Parcelable>) cacheData);
        }
        openUI(aty, category, bundle);
    }

    //分享此刻打开播放器页面
    public static void openPageFromCheck(Activity aty,
                                         String category,
                                         String myProductId,
                                         String productName,
                                         String productUrl,
                                         String fileId,
                                         String lrcId) {
        Bundle bundle = new Bundle();
        bundle.putString(KeyHelp.KEY_MYPRO_ID, myProductId); //必传
        bundle.putString(KeyHelp.KEY_PRO_NAME, productName);
        bundle.putString(KeyHelp.KEY_PRO_URL, productUrl);
        bundle.putString(KeyHelp.KEY_FILE_ID, fileId);
        bundle.putString(KeyHelp.KEY_LRC_ID, lrcId);
        bundle.putBoolean(KeyHelp.KEY_FROM_CHECK, true); //分享此刻检测到分享进入播放器
        openUI(aty, category, bundle);
    }

    private static void openUI(Activity aty, String category, Bundle bundle) {
        switch (category) {
            case DrmPat.MUSIC: {
                OpenUIUtil.openActivity(aty, SZMusicPlayActivity.class, bundle);
                aty.overridePendingTransition(cn.com.pyc.pbb.reader.R.anim.acy_music_open, cn.com
                        .pyc.pbb.reader.R.anim.fade_out_scale);
            }
            break;
            case DrmPat.BOOK: {
                OpenUIUtil.openActivity(aty, SZPDFActivity.class, bundle);
            }
            break;
            case DrmPat.VIDEO: {
                MusicHelp.release(aty);
                OpenUIUtil.openActivity(aty, SZVideoPlayerActivity.class, bundle);
            }
            break;
            default:
                Log.e("", "error category. Album category mistake.");
                break;
        }
    }

}
