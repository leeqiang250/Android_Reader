package cn.com.pyc.widget;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeText extends TextView
{

	public MarqueeText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setEllipsize(TruncateAt.MARQUEE);
		setSingleLine();
		// setFocusableInTouchMode(true);
		// setMarqueeRepeatLimit(-1);
	}

	@Override
	public boolean isFocused()
	{
		return true;
		// return super.isFocused();
	}

}
