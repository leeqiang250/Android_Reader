package cn.com.pyc.widget;

import cn.com.pyc.pbb.reader.R;
import android.content.Context;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class PycUnderLineTextView extends TextView
{
	public PycUnderLineTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		TextPaint tp = getPaint();
		tp.setAntiAlias(true);
//		tp.setFlags(Paint.UNDERLINE_TEXT_FLAG);
		setTextColor(getResources().getColor(R.color.weakgreen));
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
			setTextColor(getResources().getColor(R.color.weakgreen));
		}
		return super.onTouchEvent(event);
	}

}
