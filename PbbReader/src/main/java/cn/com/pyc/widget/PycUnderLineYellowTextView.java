package cn.com.pyc.widget;

import cn.com.pyc.pbb.reader.R;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class PycUnderLineYellowTextView extends TextView
{
	public PycUnderLineYellowTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		setTextColor(getResources().getColor(R.color.yellow));
		setClickable(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			setTextColor(getResources().getColor(R.color.green));
		}
		else if (event.getAction() == MotionEvent.ACTION_UP)
		{
			setTextColor(getResources().getColor(R.color.yellow));
		}
		return super.onTouchEvent(event);
	}

}
