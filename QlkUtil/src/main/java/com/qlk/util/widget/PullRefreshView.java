package com.qlk.util.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.qlk.util.R;
import com.qlk.util.tool.DataConvert;

public class PullRefreshView extends LinearLayout
{
	/* Do not change the order. */
	private enum RefreshState
	{
		PullDown("下拉刷新"), LoseRefresh("松开刷新"), Refreshing("正在刷新");

		private String prompt;

		RefreshState(String prompt)
		{
			this.prompt = prompt;
		}
	};

	private RefreshState mCurState;

	private static final int MIN_INSTANCE = 2;		//When moving, 2 pixel is valid

	private View mPullView;
	private ViewAnimator mPullIndicator;
	private TextView mPullPrompt;
	private TextView mPullDate;
	private int mPullHeight;	//The height of the pull view, and it is also the parent view's minimum value.
	private int mDownY;
	private int mDy;	//Current offset of the parent view.
	private int mScrollCurY;
	private Scroller mScroller;
	private OnRefreshListener mOnRefreshListener;
	private final Rect mHitRect = new Rect();

	//	/* When it is true, the parent will intercept the moving event. */
	private boolean interceptTouchEvent = false;

	public PullRefreshView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mScroller = new Scroller(context);
		initPullView(context);
	}

	private void initPullView(Context context)
	{
		mPullView = LayoutInflater.from(context).inflate(R.layout.view_pull_refresh, null);
		mPullIndicator = (ViewAnimator) mPullView.findViewById(R.id.vpr_van_indicator);
		mPullPrompt = (TextView) mPullView.findViewById(R.id.vpr_txt_prompt);
		mPullDate = (TextView) mPullView.findViewById(R.id.vpr_txt_date);
		/* This can give a fixed size when changing state. */
		((TextView) mPullView.findViewById(R.id.vpr_txt_fix)).setText(DataConvert.toDate(System
				.currentTimeMillis()));
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		mPullView.setLayoutParams(params);
		addView(mPullView);
		mPullView.measure(0, 0);
		mPullHeight = mPullView.getMeasuredHeight();
		scroll(-mPullHeight);
	}

	public void setOnRefreshListener(OnRefreshListener listener)
	{
		mOnRefreshListener = listener;
	}

	public void setRefreshComplete()
	{
		looseHand();
	}

	//	public void setPullable(boolean pullable)
	//	{
	//		mPullable = pullable;
	//	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		//			mScroller.forceFinished(true);

		int action = event.getAction();

		switch (action)
		{
			case MotionEvent.ACTION_DOWN:
				mDownY = (int) event.getY();
				interceptTouchEvent = false;
				/* The child's background will be set to press mode. */
				break;

			case MotionEvent.ACTION_MOVE:
				int x = (int) event.getX();
				int y = (int) event.getY();
				int dy = y - mDownY;	//<0 up, >0 down
				interceptTouchEvent = caculateInterception(x, y, dy)
						&& Math.abs(dy) >= MIN_INSTANCE;
				if (interceptTouchEvent)
				{
					scroll(dy / MIN_INSTANCE);		// "/" can make a slow slide.

					/*
					 * Reset the children's background to normal when changed by
					 * ACTION_DOWN, and avoid triggering long press event.
					 */
					event.setAction(MotionEvent.ACTION_CANCEL);	//If set to ACTION_UP,the children will execute its click task.
					super.dispatchTouchEvent(event);
					event.setAction(MotionEvent.ACTION_MOVE);		//Restore it.
				}
				mDownY = y;
				break;

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				looseHand();
				if (interceptTouchEvent)
				{
					event.setAction(MotionEvent.ACTION_CANCEL);	//If set to ACTION_UP,the children will execute its click task.
					super.dispatchTouchEvent(event);
				}
				break;

			default:
				break;
		}
		if (!interceptTouchEvent)
		{
			super.dispatchTouchEvent(event);
		}
		return true;
	}

	private boolean caculateInterception(int x, int y, int dy)
	{
		boolean pullable = false;
		if (mCurState == RefreshState.Refreshing)
		{
			return false;
		}
		if (dy < 0)
		{
			//up
			pullable = mDy != -mPullHeight;
		}
		else
		{
			//down
			View v = getHitView(x, y);
			if (v instanceof AbsListView)
			{
				AbsListView abs = (AbsListView) v;
				pullable = !abs.canScrollVertically(-1);
			}
			else if (v instanceof ScrollView)
			{
				ScrollView sv = (ScrollView) v;
				pullable = !sv.canScrollVertically(-1);
			}
			else
			{
				pullable = true;
			}
		}
		return pullable;
	}

	private View getHitView(int x, int y)
	{
		Rect hitRect = mHitRect;
		getHitRect(hitRect);
		final int count = getChildCount();
		for (int i = count - 1; i >= 0; i--)
		{
			final View child = getChildAt(i);
			if (child.getVisibility() == VISIBLE || child.getAnimation() != null)
			{
				child.getHitRect(hitRect);
				if (hitRect.contains(x, y))
				{
					return child;
				}
			}
		}

		return null;
	}

	/* Move to refresh */
	private void scroll(int dy)
	{
		mDy += dy;
		if (mDy >= -mPullHeight)
		{
			scrollTo(0, -mDy);
		}
		else
		{
			mDy -= dy;	//do nothing and reverse.
		}

		if (mDy < mPullHeight)
		{
			changeState(RefreshState.PullDown);
		}
		else
		{
			changeState(RefreshState.LoseRefresh);
		}
	}

	/* Loose hand and refresh */
	private void looseHand()
	{
		if (mDy == -mPullHeight)
		{
			//Doesn't move.
			return;
		}

		RefreshState curState = mCurState;
		int oldDy = mDy;
		if (curState == RefreshState.LoseRefresh)
		{
			mDy = 0;
			changeState(RefreshState.Refreshing);
			startScroll(mDy - oldDy, 1000);
		}
		else
		{
			mDy = -mPullHeight;
			changeState(RefreshState.PullDown);
			startScroll(mDy - oldDy, 500);
		}

	}

	private void changeState(RefreshState state)
	{
		if (mCurState != state)
		{
			mCurState = state;
			refreshState();
		}
	}

	private void refreshState()
	{
		RefreshState curState = mCurState;
		if (curState != null)
		{
			mPullIndicator.setDisplayedChild(curState.ordinal());
			mPullPrompt.setText(curState.prompt);
			if (curState == RefreshState.Refreshing)
			{
				mPullDate.setVisibility(View.VISIBLE);
				mPullDate.setText(DataConvert.toDate(System.currentTimeMillis()));
			}
			else
			{
				mPullDate.setVisibility(View.GONE);
			}
		}
	}

	private void startScroll(int dy, int duration)
	{
		mScrollCurY = 0;
		mScroller.startScroll(0, mScrollCurY, 0, dy, duration);
		invalidate();
	}

	@Override
	public void computeScroll()
	{
		if (mScroller.computeScrollOffset())
		{
			int curY = mScroller.getCurrY();
			scrollBy(0, mScrollCurY - curY);
			mScrollCurY = curY;
			invalidate();
		}
		else
		{
			if (mCurState == RefreshState.Refreshing)
			{
				new Handler().postDelayed(new Runnable()
				{

					@Override
					public void run()
					{
						setRefreshComplete();
					}
				}, 2000);
			}
			/*
			 * This must be called after the scroller's animation has been
			 * finished. If not, the view can not scroll to the correct
			 * position.
			 */
			if (mCurState == RefreshState.Refreshing && mOnRefreshListener != null)
			{
				mOnRefreshListener.onRefresh();
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		/*
		 * We scroll the parent by -mPullHeight pixels to hide the pull view,
		 * this will air a portion of the bottom.
		 */
		super.onMeasure(widthMeasureSpec, heightMeasureSpec + mPullHeight);
	}

	public interface OnRefreshListener
	{
		void onRefresh();
	}
}
