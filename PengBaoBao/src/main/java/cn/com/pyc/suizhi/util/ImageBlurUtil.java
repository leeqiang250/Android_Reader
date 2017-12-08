package cn.com.pyc.suizhi.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.View;

import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.SZLog;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.manager.ImageBlurManager;
import cn.com.pyc.pbbonline.util.ImageUtils;

/**
 * Created by hudaqiang on 2017/8/28.
 */

public class ImageBlurUtil {

    /**
     * 获取设置高斯模糊图片
     *
     * @param pictureUrl 原始图片下载路径
     * @param musicBg
     */
    public static void getGaussambiguity(final Context ctx, String pictureUrl, final View
            musicBg) {
        // 加载SD卡中的图片缓存
        final String fileName = FileUtil.getNameFromFilePath(pictureUrl);
        String filePath = SZPathUtil.DEF_FUZZY_PATH + File.separator + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            SZLog.i("picture file exist: " + filePath);
            setMusicBlurBackground(ctx, fileName, musicBg);
            //缓存图片已存在本地，直接加载显示；
//            if (fg != null) {
//                // 文件存在，加载本地
//                fg.setImageWithFilePath(filePath);
//            }
        } else {
            // 缓存图片不存在本地，加载网络专辑图片，存储路径filePath
            SZLog.i("picture file not exist.");
            RequestParams params = new RequestParams(pictureUrl);
            params.setUseCookie(false);
            params.setConnectTimeout(30 * 1000);
            params.setAutoResume(true); // 设置断点续传
            params.setAutoRename(true);
            params.setSaveFilePath(filePath);
            x.http().get(params, new Callback.ProgressCallback<File>() {

                @Override
                public void onWaiting() {
                }

                @Override
                public void onStarted() {
                }

                @Override
                public void onLoading(long l, long l1, boolean b) {
                }

                @Override
                public void onCancelled(CancelledException arg0) {
                }

                @Override
                public void onError(Throwable arg0, boolean arg1) {
                }

                @Override
                public void onFinished() {
                }

                @Override
                public void onSuccess(File arg0) {
                    setMusicBlurBackground(ctx, fileName, musicBg);
//                    if (fg != null) {
//                        fg.setImageWithUrl(pictureUrl);
//                    }
                }
            });
        }
    }

    /**
     * 设置高斯模糊背景图片
     *
     * @param ctx
     * @param fileName
     * @param musicBg
     */
    private static void setMusicBlurBackground(final Context ctx,
                                               final String fileName, final View musicBg) {

        //final String imageName = FileUtils.getNameFromFilePath(pictureUrl);
        final String filePath = SZPathUtil.DEF_FUZZY_PATH + File.separator + fileName;
        // 高斯模糊图片路径名
        final String filePathFuzzy = filePath + "BLUR" + ".jpg";
        File files = new File(filePathFuzzy);
        if (!files.exists()) {
            // 不存在，创建高斯模糊图片背景
            //final int getLeft = musicBg.getLeft();
            //final int getTop = musicBg.getTop();
            new AsyncTask<String, String, BitmapDrawable>() {
                @Override
                protected BitmapDrawable doInBackground(String... params) {
                    if (isCancelled()) return null;
                    Bitmap decodeFile = BitmapFactory.decodeFile(filePath);
                    if (decodeFile == null) {
                        decodeFile = BitmapFactory.decodeResource(ctx.getResources(),
                                R.drawable.music_default_bg);
                    }
                    int brightness = -80;
                    ColorMatrix cMatrix = new ColorMatrix();
                    cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0,
                            0, brightness,// 改变亮度
                            0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});

                    float scaleFactor = 2f;
                    // float radius = 20;
                    // 设置告诉模糊的图片
                    Bitmap bmp = Bitmap.createBitmap(
                            (int) (decodeFile.getWidth() / scaleFactor),
                            (int) (decodeFile.getHeight() / scaleFactor),
                            Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bmp);
                    canvas.translate(-musicBg.getLeft() / scaleFactor,
                            -musicBg.getTop() / scaleFactor);
                    //canvas.translate(-getLeft / scaleFactor, -getTop / scaleFactor);
                    canvas.scale(1 / scaleFactor, 1 / scaleFactor);
                    Paint paint = new Paint();
                    paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));
                    paint.setFlags(Paint.FILTER_BITMAP_FLAG);
                    canvas.drawBitmap(decodeFile, 0, 0, paint);
                    Bitmap bmps = ImageBlurManager.doBlur(bmp, 70, false);

                    if (decodeFile != null) {
                        decodeFile.recycle();
                        decodeFile = null;
                    }
                    if (bmp != null) {
                        bmp.recycle();
                        bmp = null;
                    }

                    BitmapDrawable background = new BitmapDrawable(ctx.getResources(), bmps);
                    // 保存文件到sd卡
                    ImageUtils.saveImage(FileUtil.getNameFromFilePath(filePathFuzzy), bmps,
                            1);
                    return background;
                }

                @Override
                protected void onPostExecute(BitmapDrawable result) {
                    if (result == null) return;
                    // 移除原背景
                    if (musicBg.getBackground() != null)
                        musicBg.setBackgroundDrawable(null);
                    musicBg.setBackgroundDrawable(result);
                    cancel(true);
                }
            }.execute();
        } else {
            // 高斯模糊图片已存在本地
            BitmapDrawable background = new BitmapDrawable(ctx.getResources(), ImageUtils
                    .getBitmap(files));
            // 移除原背景
            if (musicBg.getBackground() != null)
                musicBg.setBackgroundDrawable(null);
            musicBg.setBackgroundDrawable(background);
        }
    }

}
