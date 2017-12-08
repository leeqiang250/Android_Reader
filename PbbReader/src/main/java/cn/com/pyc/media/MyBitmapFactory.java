package cn.com.pyc.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;

public class MyBitmapFactory
{
	// 获取Resources的缩略图，本程序主要用在获取“默认缩略图”上
	public static Bitmap getResourcesThumbnail(Context context, int resId,
			int width)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resId,
				options);
		options.inSampleSize = options.outHeight / width <= 0 ? 1
				: options.outHeight / width;
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeResource(context.getResources(), resId,
				options);
		bm = ThumbnailUtils.extractThumbnail(bm, width, width,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bm;
	}

	// 获得缩略图---所有图片大小一致
	public static Bitmap getImageThumbnail(String path, int width)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bm = BitmapFactory.decodeFile(path, options); // 此时返回bm为空
		if (options.outWidth < options.outHeight)
		{
			options.inSampleSize = options.outHeight < width ? 1
					: options.outHeight / width + 1;
		}
		else
		{
			options.inSampleSize = options.outWidth < width ? 1
					: options.outWidth / width + 1;
		}
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.ALPHA_8;
		bm = BitmapFactory.decodeFile(path, options);
		bm = ThumbnailUtils.extractThumbnail(bm, width, width,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bm;
	}

	// 获得缩略图---所有图片大小一致
	public static Bitmap getImageThumbnail(byte[] data, int width)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bm = BitmapFactory
				.decodeByteArray(data, 0, data.length, options);
		if (options.outWidth < options.outHeight)
		{
			options.inSampleSize = options.outHeight < width ? 1
					: options.outHeight / width + 1;
		}
		else
		{
			options.inSampleSize = options.outWidth < width ? 1
					: options.outWidth / width + 1;
		}
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.ALPHA_8;
		bm = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		bm = ThumbnailUtils.extractThumbnail(bm, width, width,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bm;
	}

	// 获得全景图
	public static Bitmap getBitmap(Context context, int resId, int width,
			int height)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), resId, opt);
		if (opt.outWidth > opt.outHeight)
		{
			opt.inSampleSize = opt.outWidth > width ? opt.outWidth / width + 1
					: 1;
		}
		else
		{
			opt.inSampleSize = opt.outHeight > height ? opt.outHeight / height
					+ 1 : 1;
		}
		opt.inJustDecodeBounds = false;
		opt.inPreferredConfig = Config.ARGB_8888;
		return BitmapFactory.decodeResource(context.getResources(), resId, opt);
	}

	// 获得全景图
	public static Bitmap getBitmap(String path, int width, int height)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opt);
		if (opt.outWidth > opt.outHeight)
		{
			opt.inSampleSize = opt.outWidth > width ? opt.outWidth / width + 1
					: 1;
		}
		else
		{
			opt.inSampleSize = opt.outHeight > height ? opt.outHeight / height
					+ 1 : 1;
		}
		opt.inJustDecodeBounds = false;
		opt.inPreferredConfig = Config.ARGB_8888;
		return BitmapFactory.decodeFile(path, opt);
	}

	// 获得全景图
	public static Bitmap getBitmap(byte[] data, int width, int height)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
				opt);
		if (opt.outWidth > opt.outHeight)
		{
			opt.inSampleSize = opt.outWidth > width ? opt.outWidth / width + 1
					: 1;
		}
		else
		{
			opt.inSampleSize = opt.outHeight > height ? opt.outHeight / height
					+ 1 : 1;
		}

		opt.inJustDecodeBounds = false;
		opt.inPreferredConfig = Config.ARGB_8888;
		mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
		return mBitmap;
	}

	// 获取视频缩略图
	public static Bitmap getVideoThumbnail(String path, int width)
	{
		Bitmap bm = ThumbnailUtils.createVideoThumbnail(path,
				Images.Thumbnails.MINI_KIND);
		bm = ThumbnailUtils.extractThumbnail(bm, width, width,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bm;
	}
}
