package com.sz.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.qlk.util.R;

/**
 * 带进度的进度条，线程安全的View，可直接在线程中更新进度
 * 
 * 
 */
public class RoundProgressBar extends View
{

	private static int maxProgress = 100;
	/**
	 * 圆环的颜色
	 */
	private int roundColor;
	/**
	 * 圆环进度的颜色
	 */
	private int roundProgressColor;
	/**
	 * 中间进度百分比的字符串的颜色
	 */
	private int textColor;
	/**
	 * 中间进度百分比的字符串的字体
	 */
	private float textSize;
	/**
	 * 圆环的宽度
	 */
	private float roundWidth;
	/**
	 * 当前进度
	 */
	private int progress;
	/**
	 * 是否显示中间的进度
	 */
	private boolean textDisplayable;
	/**
	 * 进度的风格，实心或者空心
	 */
	private int style;

	public static final int STROKE = 0;
	public static final int FILL = 1;

	private int centerX, centerY;
	private int radius;
	private RectF ovalRectF = new RectF();
	private Paint mTextPaint = new Paint();
	private Paint mRoundPaint = new Paint();
	private Paint mProgressPaint = new Paint();

	public RoundProgressBar(Context context)
	{
		this(context, null);
	}

	public RoundProgressBar(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.RoundProgressBar);
		roundColor = mTypedArray.getColor(
				R.styleable.RoundProgressBar_roundColor, 0x3156C3EA);
		roundProgressColor = mTypedArray.getColor(
				R.styleable.RoundProgressBar_roundProgressColor, 0xFF52A5FF);
		textColor = mTypedArray.getColor(
				R.styleable.RoundProgressBar_textColor, 0xFF52A5FF);
		textSize = mTypedArray.getDimension(
				R.styleable.RoundProgressBar_textSize, 12);
		roundWidth = mTypedArray.getDimension(
				R.styleable.RoundProgressBar_roundWidth, 3);
		textDisplayable = mTypedArray.getBoolean(
				R.styleable.RoundProgressBar_textDisplayable, true);
		style = mTypedArray.getInt(R.styleable.RoundProgressBar_style, STROKE);
		mTypedArray.recycle();

		// 外部圆环画笔
		mRoundPaint.setColor(roundColor); // 设置圆环的颜色
		mRoundPaint.setStyle(Paint.Style.STROKE); // 设置空心
		mRoundPaint.setStrokeWidth(roundWidth); // 设置圆环的宽度
		mRoundPaint.setAntiAlias(true); // 消除锯齿

		// 进度画笔
		mProgressPaint.setStrokeWidth(roundWidth); // 设置圆环的宽度
		mProgressPaint.setColor(roundProgressColor); // 设置进度的颜色
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeJoin(Join.ROUND);

		// 文字画笔
		mTextPaint.setColor(textColor);
		mTextPaint.setStrokeWidth(0);
		mTextPaint.setStrokeJoin(Join.ROUND);
		mTextPaint.setTextSize(textSize);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTypeface(Typeface.DEFAULT); // 设置字体
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		centerX = getWidth() / 2; // 获取圆心的x坐标
		centerY = centerX;
		radius = (int) (centerX - roundWidth / 2); // 圆环的半径

		ovalRectF.set(new RectF(centerX - radius, centerX - radius, centerX
				+ radius, centerX + radius));

		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		drawCircle(canvas);

		drawProgress(canvas);

		drawText(canvas);

	}

	/**
	 * 画出中间进度百分比文字
	 * 
	 * @param canvas
	 */
	private void drawText(Canvas canvas)
	{
		int percent = (int) (((float) progress / (float) maxProgress) * 100); // 中间的进度百分比，先转换成float在进行除法运算，不然都为0
		float textWidth = mTextPaint.measureText(percent + "%"); // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间

		if (textDisplayable && percent != 0 && style == STROKE)
		{
			canvas.drawText(percent + "%", centerX - textWidth / 2, centerY
					+ textSize / 2, mTextPaint); // 画出进度百分比
		}
	}

	/**
	 * 画出圆环
	 * 
	 * @param canvas
	 */
	private void drawCircle(Canvas canvas)
	{
		canvas.drawCircle(centerX, centerY, radius, mRoundPaint);
	}

	/**
	 * 画出进度圆弧
	 * 
	 * @param canvas
	 */
	private void drawProgress(Canvas canvas)
	{
		switch (style)
		{
		case STROKE:
		{
			mProgressPaint.setStyle(Paint.Style.STROKE);
			canvas.drawArc(ovalRectF, -90, 360 * progress / maxProgress, false,
					mProgressPaint);
			break;
		}
		case FILL:
		{
			mProgressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			if (progress != 0)
				canvas.drawArc(ovalRectF, -90, 360 * progress / maxProgress,
						true, mProgressPaint);
			break;
		}
		default:
			break;
		}
	}

	/**
	 * 获取进度.需要同步
	 * 
	 * @return
	 */
	public synchronized int getProgress()
	{
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
	 * 
	 * @param progress
	 */
	public synchronized void setProgress(int progress)
	{
		if (progress < 0) { throw new IllegalArgumentException(
				"progress not less than 0"); }
		if (progress > maxProgress)
		{
			progress = maxProgress;
		}
		if (progress <= maxProgress)
		{
			this.progress = progress;
			postInvalidate();
		}
	}

	/**
	 * 设置环的颜色
	 * 
	 * @param cricleColor
	 */
	public void setCricleColor(int circleColor)
	{
		if (this.roundColor == circleColor) return;

		this.roundColor = circleColor;
		if (mRoundPaint != null)
		{
			mRoundPaint.setColor(this.roundColor);
		}
	}

	/**
	 * 设置文字颜色
	 * 
	 * @param textColor
	 */
	public void setTextColor(int textColor)
	{
		if (this.textColor == textColor) return;

		this.textColor = textColor;
		if (mTextPaint != null)
		{
			mTextPaint.setColor(this.textColor);
		}
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		final Bundle bundle = new Bundle();
		bundle.putParcelable("instance_state", super.onSaveInstanceState());
		bundle.putInt("centerX", centerX);
		bundle.putInt("centerY", centerY);
		bundle.putInt("radius", radius);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		if (state instanceof Bundle)
		{
			Bundle bundle = (Bundle) state;
			centerX = bundle.getInt("centerX");
			centerY = bundle.getInt("centerY");
			radius = bundle.getInt("radius");
		}
		super.onRestoreInstanceState(state);
	}

}
