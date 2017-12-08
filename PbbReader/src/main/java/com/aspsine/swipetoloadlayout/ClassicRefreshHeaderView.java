package com.aspsine.swipetoloadlayout;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.TimeUtil;
import com.sz.view.widget.AVLoadingIndicatorView;

import java.util.Date;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.utils.ViewUtil;


public class ClassicRefreshHeaderView extends SwipeRefreshHeaderLayout
{
	private String REFRESH_TIME = "classicRefreshTime";

	private Animation rotateUp;

	private Animation rotateDown;

	private TextView tvRefresh;

	private TextView tvRefreshTime;

	private ImageView ivArrow;

	private AVLoadingIndicatorView progressBar;

	private int mHeaderHeight;
	private String normalText;
	private String releaseText;
	private String refeshingText;
	private boolean rotated = false;

	public ClassicRefreshHeaderView(Context context)
	{
		this(context, null, 0);
	}

	public ClassicRefreshHeaderView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ClassicRefreshHeaderView(Context context, AttributeSet attrs,
			int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		mHeaderHeight = getResources().getDimensionPixelOffset(
				R.dimen.title_bar_height);
		rotateUp = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateUp.setDuration(200);
		rotateUp.setFillAfter(true);
		rotateDown = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateDown.setDuration(200);
		rotateDown.setFillAfter(true);

		normalText = getContext().getString(
				R.string.xlistview_header_hint_normal);
		releaseText = getContext().getString(
				R.string.xlistview_header_hint_ready);
		refeshingText = getContext().getString(
				R.string.xlistview_header_hint_loading);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		tvRefresh = (TextView) findViewById(R.id.classic_header_hint_textview);
		tvRefreshTime = (TextView) findViewById(R.id.classic_header_time);
		ivArrow = (ImageView) findViewById(R.id.classic_header_arrow);
		progressBar = (AVLoadingIndicatorView) findViewById(R.id.classic_header_progressbar);
	}

	@Override
	public void onRefresh()
	{
		super.onRefresh();
		ivArrow.clearAnimation();
		ViewUtil.hideWidget(ivArrow);
		ViewUtil.showWidget(progressBar);
		tvRefresh.setText(refeshingText);
		setRefreshTime(new Date());
	}

	@Override
	public void onPrepare()
	{
		super.onPrepare();
		String time = (String) SPUtil.get(REFRESH_TIME, "");
		if (TextUtils.isEmpty(time))
		{
			setRefreshTime(new Date());
		} else
		{
			Date dstDate = TimeUtil.getDateFromDateString(time);
			setRefreshTime(dstDate);
		}
	}

	public void setRefreshTime(Date date)
	{
		if (date == null) date = new Date();

		String time = TimeUtil.getDateString(date);
		// 保存刷新时间到sp,保存格式：yyyy-MM-dd HH:mm:ss
		SPUtil.save(REFRESH_TIME, time);

		tvRefreshTime.setText(TimeUtil.getTimeString(getContext(), date));
	}

	@Override
	public void onComplete()
	{
		super.onComplete();
		rotated = false;
		ivArrow.clearAnimation();
		ViewUtil.hideWidget(ivArrow);
		ViewUtil.hideWidget(progressBar);
		tvRefresh.setText(refeshingText);
	}

	@Override
	public void onReset()
	{
		super.onReset();
		rotated = false;
		ivArrow.clearAnimation();
		ViewUtil.hideWidget(ivArrow);
		ViewUtil.hideWidget(progressBar);
	}

	@Override
	public void onMove(int y, boolean isComplete, boolean automatic)
	{
		super.onMove(y, isComplete, automatic);
		//DRMLog.v("moveY = " + y);
		if (!isComplete)
		{
			ViewUtil.showWidget(ivArrow);
			ViewUtil.hideWidget(progressBar);
			if (y > mHeaderHeight)
			{
				if (!rotated)
				{
					ivArrow.clearAnimation();
					ivArrow.startAnimation(rotateUp);
					rotated = true;
				}
				tvRefresh.setText(releaseText);
			} else if (y < mHeaderHeight)
			{
				if (rotated)
				{
					ivArrow.clearAnimation();
					ivArrow.startAnimation(rotateDown);
					rotated = false;
				}
				tvRefresh.setText(normalText);
			}
		}
	}

}
