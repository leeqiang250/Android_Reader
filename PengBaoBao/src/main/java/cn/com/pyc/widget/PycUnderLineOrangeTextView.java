package cn.com.pyc.widget;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import cn.com.pyc.pbb.R;

public class PycUnderLineOrangeTextView extends TextView
{
	public PycUnderLineOrangeTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		TextPaint tp = getPaint();
		tp.setAntiAlias(true);
//		tp.setFlags(Paint.UNDERLINE_TEXT_FLAG);
		setTextColor(getResources().getColor(R.color.orange));
		setClickable(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			setTextColor(getResources().getColor(R.color.lightblue));
		}
		else if (event.getAction() == MotionEvent.ACTION_UP)
		{
			setTextColor(getResources().getColor(R.color.orange));
		}
		return super.onTouchEvent(event);
	}

}
