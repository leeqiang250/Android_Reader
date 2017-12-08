package cn.com.pyc.pbbonline.widget;

import java.util.ArrayList;
import java.util.List;

import com.sz.mobilesdk.util.CommonUtil;

import cn.com.pyc.pbb.reader.R;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 滑动tabWidget
 * 
 * @author hudq
 */
@Deprecated
public class SlideTabWidget extends LinearLayout
{

	private final String TAG = SlideTabWidget.class.getSimpleName();

	private int mCurrentTabIndex = 0;

	private SlideTabListener mListener;
	private LinearLayout mTabButtonLayout;
	private List<ImageView> mButtonList = new ArrayList<ImageView>();

	private int dotSize;

	public SlideTabWidget(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public SlideTabWidget(Context context)
	{
		super(context);
		init(context);
	}

	private void init(Context context)
	{
		dotSize = CommonUtil.dip2px(getContext(), 8f);
		int lineHeight = CommonUtil.dip2px(getContext(), 0.5f);
		setGravity(Gravity.CENTER_HORIZONTAL);
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(getResources().getColor(android.R.color.transparent));

		ImageView mLineView = new ImageView(context);
		mLineView.setImageDrawable(new ColorDrawable(0xffd7d7d7));
		mLineView.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, lineHeight));
		addView(mLineView);

		mTabButtonLayout = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		mTabButtonLayout.setGravity(Gravity.CENTER);
		mTabButtonLayout.removeAllViews();
		mTabButtonLayout.setOrientation(LinearLayout.HORIZONTAL);
		addView(mTabButtonLayout, lp);

	}

	public static interface SlideTabListener
	{
		public void onTabChanged(int oldIndex, int newIndex);
	}

	public void setListener(SlideTabListener listener)
	{
		mListener = listener;
	}

	/**
	 * 添加tab
	 * 
	 * @param isSelected
	 */
	public void addTab(boolean isSelected)
	{
		ImageView button = new ImageView(getContext());
		button.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotSize, dotSize);
		lp.leftMargin = CommonUtil.dip2px(getContext(), 15f);
		mTabButtonLayout.addView(button, lp);
		button.setBackgroundResource(R.drawable.point_index_background);
		button.setTag(mButtonList.size());
		if (isSelected)
		{
			button.setSelected(true);
			mCurrentTabIndex = mButtonList.size();
		}
		mButtonList.add(button);
	}

	/**
	 * 选择第几个tab
	 * 
	 * @param index
	 */
	public void selectTab(int index, boolean tiggerOnTabChanged)
	{
		Log.d(TAG, "selectTab: " + index);
		if (mCurrentTabIndex == index)
		{
			return;
		}
		int oldIndex = mCurrentTabIndex;
		mCurrentTabIndex = index;
		int size = mButtonList.size();
		for (int i = 0; i < size; i++)
		{
			ImageView button = mButtonList.get(i);
			if (mCurrentTabIndex != i)
			{
				button.setSelected(false);
			}
			else
			{
				button.setSelected(true);
			}
		}
		if (tiggerOnTabChanged)
		{
			if (mListener != null)
			{
				mListener.onTabChanged(oldIndex, mCurrentTabIndex);
			}
		}
	}

}
