package cn.com.pyc.widget;

import android.content.Context;
import android.util.AttributeSet;

public class PycPsdEditText extends PycEditText
{

	public PycPsdEditText(Context context, AttributeSet attrs)
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
