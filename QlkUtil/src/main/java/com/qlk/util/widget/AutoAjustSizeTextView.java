package com.qlk.util.widget;

import com.qlk.util.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * 根据字数和宽度自适应
 * 
 * @author QiLiKing 2015-7-28 上午9:12:44
 */
public class AutoAjustSizeTextView extends TextView
{
	private Paint mPaint;
	private int maxTextSize;
	private int minTextSize;
	private boolean isMarquee;

	public AutoAjustSizeTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		mPaint = new Paint(getPaint());

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutoResizeView);
		maxTextSize = ta.getDimensionPixelSize(R.styleable.AutoResizeView_maxTextSize, 0);
		minTextSize = ta.getDimensionPixelSize(R.styleable.AutoResizeView_minTextSize, 0);
		isMarquee = ta.getBoolean(R.styleable.AutoResizeView_marquee, false);
		setSingleLine(ta.getBoolean(R.styleable.AutoResizeView_singleLine, false));
		ta.recycle();

		if (isMarquee)
		{
			setEllipsize(TruncateAt.MARQUEE);
		}
	}

	@Override
	public boolean isFocused()
	{
		return isMarquee;
	}

	private void fitText()
	{
		int w = getWidth();
		if (w > 0)
		{
			w = w - getPaddingLeft() - getPaddingRight();		//可用宽度
			float size = getTextSize();
			final String text = getText().toString();
			mPaint.setTextSize(size);
			if (mPaint.measureText(text) > w)
			{
				while(mPaint.measureText(text) > w)
				{
					size -= 1;
					if (minTextSize > 0 && size < minTextSize)
					{
						size = minTextSize;
						break;
					}
					mPaint.setTextSize(size);
				}
			}
			else
			{
				while(mPaint.measureText(text) < w)
				{
					size += 1;
					if (maxTextSize > 0 && size > maxTextSize)
					{
						size = maxTextSize;
						break;
					}
					mPaint.setTextSize(size);
				}
				size--;	//这个不能少
			}

			if (size != getTextSize())
			{
				setTextSize(TypedValue.COMPLEX_UNIT_PX, size);	//以像素为单位设置size
			}
		}
	};

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		if (!TextUtils.isEmpty(text))
		{
			fitText();
		}
	}

}
