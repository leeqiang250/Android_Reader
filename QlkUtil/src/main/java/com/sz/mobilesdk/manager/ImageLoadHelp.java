package com.sz.mobilesdk.manager;

import android.widget.ImageView;

import org.xutils.image.ImageOptions;
import org.xutils.x;

/**
 * Created by hudaqiang on 2017/7/11.
 */

public class ImageLoadHelp {

    private static ImageOptions defaultOptions;

    static {
        defaultOptions = new ImageOptions.Builder()
                // 加载中或错误图片的ScaleType
                //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
                // 默认自动适应大小
                .setSize(480, 800)
                .setFailureDrawableId(android.R.color.transparent)
                .setLoadingDrawableId(android.R.color.transparent)
                .setIgnoreGif(false)
                // 如果使用本地文件url, 添加这个设置可以在本地文件更新后刷新立即生效.
                .setUseMemCache(true)
                .setFadeIn(true)
                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .build();
    }

    public static void loadImage(ImageView imageView, String url) {
        loadImage(imageView, url, defaultOptions);
    }

    public static void loadImage(ImageView imageView, String url, ImageOptions options) {
        x.image().bind(imageView, url, options);
    }

    public static void clearCacheFiles() {
        x.image().clearCacheFiles();
    }

    public static void clearMemCache() {
        x.image().clearMemCache();
    }

    public static void clearCache() {
        clearCacheFiles();
        clearMemCache();
    }
}
