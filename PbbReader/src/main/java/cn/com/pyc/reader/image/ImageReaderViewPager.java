package cn.com.pyc.reader.image;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ImageReaderViewPager extends ViewPager
{
	//��Ҫ����ͼƬ�Ŵ���С�ϣ���ʶͼƬ�Ƿ񻬶����߽硣������߽磬���ٻ���ʱ���ǻ��Ȼ����ˣ�������ͼƬ����
	public static boolean intercept = false;

	public ImageReaderViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		intercept = false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			intercept = false;
			super.onInterceptTouchEvent(event);
		}
		if (intercept)
		{
			return super.onInterceptTouchEvent(event);
		}
		else
		{
			return false;
		}
	}
}
