package com.qlk.util.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView extends AdapterView<ListAdapter>
{
	protected ListAdapter mAdapter;
	private final GestureDetector mGesture;
	private OnItemSelectedListener mOnItemSelected;
	private OnItemClickListener mOnItemClicked;
	private boolean mDataChanged = false;
	private float mScrolledDx;
	private final Scroller mScroller;

	/* 可向左向右滑动的距离，皆为正值 */
	private float mLeftOffset;
	private float mRightOffset;

	public HorizontalListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		mScroller = new Scroller(context);
		mGesture = new GestureDetector(getContext(), mOnGesture);
	}

	@Override
	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener)
	{
		mOnItemSelected = listener;
	}

	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener)
	{
		mOnItemClicked = listener;
	}

	private DataSetObserver mDataObserver = new DataSetObserver()
	{

		@Override
		public void onChanged()
		{
			synchronized (HorizontalListView.this)
			{
				mDataChanged = true;
			}
			invalidate();
			requestLayout();
		}

		@Override
		public void onInvalidated()
		{
			reset();
			invalidate();
			requestLayout();
		}

	};

	@Override
	public ListAdapter getAdapter()
	{
		return mAdapter;
	}

	@Override
	public View getSelectedView()
	{
		// TODO: implement
		return null;
	}

	@Override
	public void setAdapter(ListAdapter adapter)
	{
		if (mAdapter != null)
		{
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataObserver);
		reset();
	}

	private synchronized void reset()
	{
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public void setSelection(int position)
	{
		// TODO: implement
	}

	private void addAndMeasureChild(final View child, int viewIndex)
	{
		//		_SysoXXX.message("addAndMeasureChild:" + viewIndex);
		LayoutParams params = child.getLayoutParams();
		if (params == null)
		{
			params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		addViewInLayout(child, viewIndex, params, true);
		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		super.onLayout(changed, left, top, right, bottom);

		if (mAdapter == null)
		{
			return;
		}
		if (mDataChanged)
		{
			removeAllViewsInLayout();
			mDataChanged = false;
		}
		layoutChildren();

		//		if (!mScroller.isFinished())
		//		{
		//			post(new Runnable()
		//			{
		//				@Override
		//				public void run()
		//				{
		//					requestLayout();
		//				}
		//			});
		//
		//		}
	}

	private void layoutChildren()
	{
		scrollTo(0, 0);	//复原位置
		mScrolledDx = 0;

		final int count = mAdapter.getCount();
		final int maxWidth = getWidth();	//不能超过视图
		int needWidth = 0;
		mLeftOffset = 0;
		mRightOffset = 0;

		for (int i = 0; i < count; i++)
		{
			View child = mAdapter.getView(i, null, this);
			addAndMeasureChild(child, -1);
			needWidth += child.getMeasuredWidth();
		}
		if (needWidth > maxWidth)
		{
			//从右向左布局
			int remainWidth = maxWidth;
			for (int j = count - 1; j >= 0; j--)
			{
				View child = getChildAt(j);
				int childWidth = child.getMeasuredWidth();
				int top = (getHeight() - child.getMeasuredHeight()) / 2;
				child.layout(remainWidth - childWidth, top, remainWidth,
						top + child.getMeasuredHeight());	//这四个参数都是相对于父视图的
				remainWidth -= childWidth;
			}
			mLeftOffset = Math.abs(getChildAt(0).getLeft() - getLeft());
		}
		else
		{
			//从左向右布局
			int usedWidth = 0;
			for (int k = 0; k < count; k++)
			{
				View child = getChildAt(k);
				int childWidth = child.getMeasuredWidth();
				child.layout(usedWidth, 0, usedWidth + childWidth, child.getMeasuredHeight());	//这四个参数都是相对于父视图的
				usedWidth += childWidth;
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		boolean handled = mGesture.onTouchEvent(ev);
		return handled;
	}

	@Override
	public void computeScroll()
	{
		//		if (!mScroller.isFinished())
		//		{
		//			if (mScroller.computeScrollOffset())
		//			{
		//				int oldX = getScrollX();
		//				int oldY = getScrollY();
		//				int x = mScroller.getCurrX();
		//				int y = mScroller.getCurrY();
		//				if (oldX != x || oldY != y)
		//				{
		//					scrollTo(x, y);
		//				}
		//				invalidate();
		//			}
		//		}
	}

	//右负---正左
	protected boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		if (distanceX < 0)
		{
			//向右滑动
			if (Math.abs(distanceX) > mLeftOffset)
			{
				distanceX = -mLeftOffset;
			}
			mLeftOffset -= Math.abs(distanceX);
			mRightOffset += Math.abs(distanceX);
		}
		else
		{
			//向左滑动
			if (Math.abs(distanceX) > mRightOffset)
			{
				distanceX = mRightOffset;
			}
			mRightOffset -= distanceX;
			mLeftOffset += distanceX;
		}

		if (distanceX != 0)
		{
			mScrolledDx += distanceX;
			scrollBy((int) distanceX, 0);
		}
		return true;
	}

	//向右正，向左负
	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		//		synchronized (HorizontalListView.this)
		//		{
		//			_SysoXXX.message("velocityX:" + velocityX);
		//			if (velocityX > 0)
		//			{
		//				//向右滑动
		//				mScroller.fling(getScrollX(), 0, (int) -velocityX, 0, 0, (int) mLeftOffset, 0, 0);
		//			}
		//			else
		//			{
		//				//向左滑动
		//				mScroller.fling(getScrollX(), 0, (int) -velocityX, 0, 0, (int) mRightOffset, 0, 0);
		//			}
		//
		//		}
		//		requestLayout();

		return true;
	}

	protected boolean onDown(MotionEvent e)
	{
		return true;
	}

	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener()
	{

		@Override
		public boolean onDown(MotionEvent e)
		{
			return HorizontalListView.this.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			return HorizontalListView.this.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e)
		{
			RectF viewRect = new RectF();
			for (int i = 0; i < getChildCount(); i++)
			{
				View child = getChildAt(i);

				viewRect.left = child.getLeft() - mScrolledDx;
				viewRect.right = child.getRight() - mScrolledDx;
				viewRect.top = child.getTop();
				viewRect.bottom = child.getBottom();
				if (viewRect.contains(e.getX(), e.getY()))
				{
					if (mOnItemClicked != null)
					{
						mOnItemClicked.onItemClick(HorizontalListView.this, child, i,
								mAdapter.getItemId(i));
					}
					if (mOnItemSelected != null)
					{
						mOnItemSelected.onItemSelected(HorizontalListView.this, child, i,
								mAdapter.getItemId(i));
					}
					break;
				}

			}
			return true;
		}

	};

}
