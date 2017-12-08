package cn.com.pyc.utils;

import android.view.View;

/**
 * 控件工具处理
 * 
 */
public class ViewUtil
{

	/**
	 * 获取控件宽;wrap_content
	 */
	public static int getWidth(View view)
	{
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		return view.getMeasuredWidth();
	}

	/**
	 * 获取控件高,wrap_content时候
	 */
	public static int getHeight(View view)
	{
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		return view.getMeasuredHeight();
	}

	/**
	 * visible view,if this view is gone or invisible.
	 * 
	 * @param view
	 */
	public static final void showWidget(View view)
	{
		if (view.getVisibility() == View.GONE
				|| view.getVisibility() == View.INVISIBLE)
		{
			view.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * gone view,if this view is visible.
	 * 
	 * @param view
	 */
	public static final void hideWidget(View view)
	{
		if (isVisible(view))
		{
			view.setVisibility(View.GONE);
		}
	}

	/**
	 * invisible view,if this view is visible.
	 * 
	 * @param view
	 */
	public static final void inVisibleWidget(View view)
	{
		if (isVisible(view))
		{
			view.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * if this view is visible
	 * 
	 * @param v
	 * @return
	 */
	public static boolean isVisible(View v)
	{
		return v.getVisibility() == View.VISIBLE;
	}

}
