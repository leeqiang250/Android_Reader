package cn.com.pyc.reader.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ImageZoomView extends ImageView
{
	private static final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private static final int EDGE = 10; // ��ʼʱͼƬ�ı߾�
	private static final int DOUBLE_CLICK_INTERVAL = 250; // �ж��Ƿ���˫��������
	private final Rect mRectSrc = new Rect();

	private Bitmap mBitmap;

	public ImageZoomView(Context context)
	{
		super(context);
	}

	public ImageZoomView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	// Ҫ��ʾ��ͼƬ
	public void setBitmap(Bitmap bitmap)
	{
		mBitmap = bitmap;
		init = true; // ���ǰһ������ʱ��ת�ˣ������ٻ�����Ҫ���¶���View�Ŀ�͸�
		invalidate();
	}

	private Matrix matrix = new Matrix();

	public void rotate(int degree)
	{
		if (mBitmap == null || mBitmap.isRecycled())
		{
			return;
		}
		matrix.setRotate(degree);
		mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
				mBitmap.getHeight(), matrix, true);
		init = true;
		invalidate();
	}

	private int bitmapWidth;
	private int bitmapHeight;
	private double ratio; // ʹ��double����߾���
	private boolean init = true;
	private int ViewWidth;
	private int ViewHeight;
	private double zoomX = 0; // ����ֵ
	private double zoomY = 0;
	private double zoomDx = 0; // ����ֵ
	private double zoomDy = 0;

	protected void onDraw(Canvas canvas)
	{
		if (mBitmap == null || mBitmap.isRecycled())
		{
			return;
		}

		// ��ʱgetWidth��0
		if (init && getWidth() != 0 && getHeight() != 0)
		{
			// ��ʼ��
			zoomX = 0;
			zoomY = 0;
			zoomDx = 0;
			zoomDy = 0;
			ViewWidth = getWidth();
			ViewHeight = getHeight();
			bitmapWidth = mBitmap.getWidth();
			bitmapHeight = mBitmap.getHeight();

			// ԭʼ��С---û�Ŵ���СʱͼƬ��ʾ�Ĵ�С
			mRectSrc.left = (ViewWidth - bitmapWidth) / 2;
			mRectSrc.top = (ViewHeight - bitmapHeight) / 2;
			mRectSrc.right = (ViewWidth + bitmapWidth) / 2;
			mRectSrc.bottom = (ViewHeight + bitmapHeight) / 2;

			// ����---��ΪViewPager���һ�������left��right���ٽ�ֵ���жϵ�
			// Ĭ��ʹͼƬ����������Ļ
			if (mRectSrc.left != EDGE)
			{
				zoomY = (double) (mRectSrc.left - EDGE) * bitmapHeight
						/ bitmapWidth;
				zoomDy = zoomY % 1;

				mRectSrc.left = EDGE;
				mRectSrc.right = ViewWidth - EDGE;
				mRectSrc.top = mRectSrc.top - (int) zoomY;
				mRectSrc.bottom = mRectSrc.bottom + (int) zoomY;
			}

			ratio = (((double) mBitmap.getWidth()) / mBitmap.getHeight())
					/ (((double) getWidth()) / getHeight()) * 0.5;

			init = false;
		}

		if (zoom != 0)
		{
			zoom *= ratio * (mRectSrc.right - mRectSrc.left) / bitmapWidth; // �����ٶ���ͼƬ���д�С������
			// zoom *= ratio;

			zoomX = bitmapWidth * zoom + zoomDx;
			zoomDx = zoomX % 1;

			zoomY = bitmapHeight * zoom + zoomDy;
			zoomDy = zoomY % 1;

			// ����
			mRectSrc.left = mRectSrc.left - (int) zoomX;
			mRectSrc.top = mRectSrc.top - (int) zoomY;
			mRectSrc.right = mRectSrc.right + (int) zoomX;
			mRectSrc.bottom = mRectSrc.bottom + (int) zoomY;

			// �����л���
			if (mRectSrc.right < ViewWidth && mRectSrc.left < 0)
			{
				dx = ViewWidth - mRectSrc.right;
			}
			else if (mRectSrc.left > 0 && mRectSrc.right > ViewWidth)
			{
				dx = 0 - mRectSrc.left;
			}
			else
			{
				dx = 0;
			}

			if (mRectSrc.bottom < ViewHeight && mRectSrc.top < 0)
			{
				dy = ViewHeight - mRectSrc.bottom;
			}
			else if (mRectSrc.top > 0 && mRectSrc.bottom > ViewHeight)
			{
				dy = 0 - mRectSrc.top;
			}
			else
			{
				dy = 0;
			}

			// ���Żص�
			if (roomResilence)
			{
				if (mRectSrc.width() > 8 * bitmapWidth)
				{
					zoom = -0.06f;
					invalidate();
				}
				else if (mRectSrc.width() < 0.25 * bitmapWidth)
				{
					zoom = 0.06f;
					invalidate();
				}
				else
				{
					roomResilence = false;
					zoom = 0;
				}
			}
		}

		// ���Ի���
		if (mRectSrc.left < 0 || mRectSrc.top < 0 || mRectSrc.right > ViewWidth
				|| mRectSrc.bottom > ViewHeight)
		{
			// ����
			mRectSrc.left = mRectSrc.left + (int) dx;
			mRectSrc.top = mRectSrc.top + (int) dy;
			mRectSrc.right = mRectSrc.right + (int) dx;
			mRectSrc.bottom = mRectSrc.bottom + (int) dy;

			if (mRectSrc.right < ViewWidth - EDGE && mRectSrc.left < 0
					&& dx < 0)
			{
				ImageReaderViewPager.intercept = true;
			}
			else if (mRectSrc.left > EDGE && mRectSrc.right > ViewWidth
					&& dx > 0)
			{
				ImageReaderViewPager.intercept = true;
			}

			// �����ص�---ֻ����isOnePointer=true��ʱ��Ż�ִ��
			if (slideResilence)
			{
				// ���һص�
				if (mRectSrc.right < ViewWidth && mRectSrc.left < 0)
				{
					dx = ViewWidth - EDGE - mRectSrc.right;
				}
				else if (mRectSrc.left > 0 && mRectSrc.right > ViewWidth)
				{
					dx = EDGE - mRectSrc.left;
				}

				// ���»ص�
				if (mRectSrc.bottom < ViewHeight && mRectSrc.top < 0)
				{
					dy = ViewHeight - mRectSrc.bottom;
				}
				else if (mRectSrc.top > 0 && mRectSrc.bottom > ViewHeight)
				{
					dy = 0 - mRectSrc.top;
				}

				slideResilence = false;
				invalidate();
			}
			else
			{
				dx = 0;
				dy = 0;
			}
		}
		else
		{
			ImageReaderViewPager.intercept = true;
		}

		// ˫���ص�
		if (doubleClickResilence)
		{
			if (reduce && mRectSrc.width() > 0.25 * bitmapWidth)
			{
				zoom = -0.2f;
				invalidate();
			}
			else if (!reduce && mRectSrc.left > 5)
			{
				zoom = 0.2f;
				invalidate();
			}
			else
			{
				doubleClickResilence = false;
				zoom = 0;
				if (!reduce)
				{
					init = true;
					invalidate();
				}
			}
		}

		canvas.drawBitmap(mBitmap, null, mRectSrc, mPaint);

		// // TODO ������---�����ã���ɾ��
		// float ratio1 = (float) (mRectSrc.right - mRectSrc.left)
		// / (mRectSrc.bottom - mRectSrc.top);
		// final float ratio2 = (float) bitmapWidth / bitmapHeight;
		// System.out.println("����ʣ�"
		// + Math.abs((int) ((ratio1 - ratio2) / ratio2 * 10000) * 0.01)
		// + "%");
		// super.onDraw(canvas);
	}

	private boolean reduce = false;

	private float dxy;
	/**
	 * ������С�������Ŵ�
	 */
	private double zoom;

	private float mX;
	private float mY;
	private float dx;
	private float dy;
	private boolean isOnePointer = true;
	private boolean roomResilence = false;
	private boolean slideResilence = false;
	private long timeUp;
	private boolean doubleClickResilence = false;

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (doubleClickResilence)
		{
			return true;
		}

		final int action = event.getAction();
		final int pointCount = event.getPointerCount();

		if (pointCount == 1 && isOnePointer)
		{
			final float x = event.getX();
			final float y = event.getY();
			switch (action)
			{
				case MotionEvent.ACTION_DOWN:
					mX = x;
					mY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					dx = x - mX;
					dy = y - mY;
					mX = x;
					mY = y;
					if (dx != 0 || dy != 0)
					{
						invalidate();
					}
					break;

				case MotionEvent.ACTION_UP:
					if (System.currentTimeMillis() - timeUp < DOUBLE_CLICK_INTERVAL)
					{
						if (mRectSrc.width() < bitmapWidth * 0.5)
						{
							reduce = false;
							doubleClickResilence = true;
						}
						else
						{
							reduce = true;
							doubleClickResilence = true;
						}
					}
					else
					{
						timeUp = System.currentTimeMillis();
					}
					break;
			}
		}
		if (pointCount == 2)
		{
			isOnePointer = false;
			try
			{
				final float x0 = event.getX(event.getPointerId(0));
				final float y0 = event.getY(event.getPointerId(0));

				final float x1 = event.getX(event.getPointerId(1));
				final float y1 = event.getY(event.getPointerId(1));

				switch (action)
				{
					case 261:
						dxy = (float) Math.hypot(x0 - x1, y0 - y1);
						break;

					case MotionEvent.ACTION_POINTER_DOWN:
						dxy = (float) Math.hypot(x0 - x1, y0 - y1);
						break;

					case MotionEvent.ACTION_MOVE:

						float dxyTemp = (float) Math.hypot(x0 - x1, y0 - y1);
						if (dxyTemp != dxy)
						{
							zoom = (dxyTemp - dxy) / getWidth();
							dxy = dxyTemp;
							invalidate();
						}

						break;
				}
			}
			catch (Exception e)	//������ָ����ʱ�����
			{
				e.printStackTrace();
			}
		}

		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL)
		{
			slideResilence = true;
			isOnePointer = true;
			roomResilence = true;
			invalidate();
		}

		return true;
	}
}
