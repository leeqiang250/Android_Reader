package cn.com.pyc.pbbonline.util;

import org.xutils.image.ImageOptions;

import cn.com.pyc.pbb.reader.R;

public class BitmapHelp
{
	private BitmapHelp()
	{
	}

	public static ImageOptions getImageOptions()
	{
		ImageOptions imageOptions = new ImageOptions.Builder()
				// 如果ImageView的大小不是定义为wrap_content, 不要crop.
				// 加载中或错误图片的ScaleType
				//.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
				.setLoadingDrawableId(R.drawable.transparent)
				.setFailureDrawableId(R.drawable.transparent).build();
		return imageOptions;
	}

}
