package cn.com.pyc.pbbonline.manager;

import android.graphics.Bitmap;

/**
 * 
 * @author https://github.com/qiujuer/ImageBlurring
 * 
 */
public class ImageBlur {

	static {
		System.loadLibrary("ImageBlur");
	}

	public static native void blurIntArray(int[] pixelArray, int width,
			int height, int radius);

	public static native void blurBitMap(Bitmap bitmap, int radius);

}
