package cn.com.pyc.sm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AdView extends ImageView
{
	private static final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private final Rect mRectSrc = new Rect();

	private Bitmap mBitmap;

	public AdView(Context context)
	{
		super(context);
	}

	public AdView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void setBitmap(Bitmap bitmap)
	{
		mBitmap = bitmap;
		invalidate();
	}

	protected void onDraw(Canvas canvas)
	{
		if (mBitmap == null || mBitmap.isRecycled())
		{
			return;
		}
		int bmWidth = mBitmap.getWidth();
		int bmHeigth = mBitmap.getHeight();
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		if (1.0f * bmHeigth * viewWidth / bmWidth > viewHeight)
		{
			mRectSrc.top = 0;
			mRectSrc.bottom = getHeight();
			int desWidth = (int) (1.0f * bmWidth * viewHeight / bmHeigth);
			int dx = (viewWidth - desWidth) / 2;
			mRectSrc.left = dx;
			mRectSrc.right = getWidth() - dx;
		}
		else
		{
			mRectSrc.left = 0;
			mRectSrc.top = 0;
			mRectSrc.right = getWidth();
			int desHeight = (int) (1.0f * viewWidth * bmHeigth / bmWidth);
			mRectSrc.bottom = desHeight;
		}

		canvas.drawBitmap(mBitmap, null, mRectSrc, mPaint);
	}
}
