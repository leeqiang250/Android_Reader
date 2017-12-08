package com.qlk.util.base;

import android.app.Activity;
import android.util.DisplayMetrics;

import com.qlk.util.media.QlkDirs;
import com.sz.mobilesdk.common.SZApplication;

import java.util.HashSet;

/**
 * 记录打开的activity，safeExit时清理
 *
 * @author QiLiKing 2015-7-29 上午11:31:26
 */
public class BaseApplication extends SZApplication {
    public static final HashSet<Activity> ACTIVITIES = new HashSet<Activity>();
    public static int screenWidth;
    public static int screenHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        QlkDirs.reGetCardsPaths(this);
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        initJcifsConfig();

        //initImageLoader(this);
    }

    private void initJcifsConfig() {
        //System.setProperty("jcifs.smb.client.dfs.disabled", "true");
        //System.setProperty("jcifs.smb.client.soTimeout", "60000");
        //System.setProperty("jcifs.smb.client.responseTimeout", "60000");
        //......
        jcifs.Config.setProperty("jcifs.smb.client.dfs.disabled", "true");
        jcifs.Config.setProperty( "jcifs.netbios.cachePolicy", "1200" );
        jcifs.Config.setProperty("jcifs.smb.client.soTimeout", "10000");
        jcifs.Config.setProperty("jcifs.smb.client.responseTimeout", "60000");
        jcifs.Config.setProperty("jjcifs.smb.lmCompatibility", "2");
        jcifs.Config.setProperty("jcifs.smb.client.useExtendedSecurity", "false");
        jcifs.Config.setProperty("jcifs.resolveOrder", "LMHOSTS,BCAST,DNS");

    }

//    private void initImageLoader(Context context) {
//        File cacheDir = StorageUtils.getOwnCacheDirectory(context, PathUtil.getSZImageCachePath());
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
//                .diskCacheExtraOptions(480, 800, null)
//                // .memoryCacheExtraOptions(320, 640)
//                .denyCacheImageMultipleSizesInMemory()
//                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
//                // .memoryCacheSize(2 * 1024 * 1024)
//                // .memoryCacheSizePercentage(13)
//                .diskCache(new UnlimitedDiskCache(cacheDir))
//                // .diskCacheSize(50 * 1024 * 1024)
//                // .diskCacheFileCount(100)
//                .defaultDisplayImageOptions(
//                        new DisplayImageOptions.Builder()
//                                .showImageForEmptyUri(getResources().getDrawable(android.R.color.transparent))
//                                .showImageOnFail(getResources().getDrawable(android.R.color.transparent))
//                                .showImageOnLoading(getResources().getDrawable(android.R.color.transparent))
//                                .resetViewBeforeLoading(false)
//                                .cacheInMemory(false)
//                                .cacheOnDisk(true)
//                                // .displayer(new SimpleBitmapDisplayer())
//                                .displayer(new FadeInBitmapDisplayer(600))
//                                .bitmapConfig(Bitmap.Config.RGB_565).build())
//                .build();
//        ImageLoader.getInstance().init(config);
//    }

    public void safeExit() {
        // 这里最好清理一下缓存

        for (Activity activity : ACTIVITIES) {
            activity.finish();
        }
        ACTIVITIES.clear();
    }

}
