package cn.com.pyc.widget;

import android.content.Context;
import android.util.AttributeSet;

public class PsdEditText extends LoginEditText
{

	public PsdEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected void init()
	{
		super.init();
		WidgetTool.changVisible(this, false);
	}

}
