package cn.com.pyc.web;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;

import com.sz.mobilesdk.util.SZLog;

import java.util.ArrayList;

/**
 * 监听触摸滑动事件。
 */
public class CustomWebFingerOnTouchListener implements View.OnTouchListener
{

	private final int DEFAULT_SLOP = 20;
	private int touchSlop = DEFAULT_SLOP;

	private float lastY = 0f;
	private float currentY = 0f;
	// 下面两个表示滑动的方向，大于0表示向下滑动，小于0表示向上滑动，等于0表示未滑动
	private int lastDirection = 0;
	private int currentDirection = 0;

	// 顶部和底部的bar
	// private View topBar;
	private View footer;

	// 占位view
	// private View mOccupyView;
	// private int mOccupyViewHeight;

	/*
	 * 
	 * @param context
	 * @param topBar
	 *            顶部bar
	 * @param footer
	 *            底部bar
	 * @param occupyView
	 *            作为和topBar同高度的占位View。
	 */
	// public CustomWebFingerOnTouchListener(Context context, View topBar,
	// View footer, View mOccupyView)
	// {
	// this.topBar = topBar;
	// this.footer = footer;
	// this.mOccupyView = mOccupyView;
	// // 滚动过多少距离后才开始计算是否隐藏/显示头尾元素。这里用了默认touchslop的0.9倍。
	// touchSlop = (int) (ViewConfiguration.get(context).getScaledTouchSlop() *
	// 0.9);
	// // 占位View的高度
	// mOccupyViewHeight = (int) context.getResources().getDimension(
	// R.dimen.title_bar_height);
	//
	// // 不能为null
	// if (this.toolbar == null || this.footer == null
	// || this.mOccupyView == null) { throw new UnsupportedOperationException(
	// "must be instantiated"); }
	// }

	public CustomWebFingerOnTouchListener(Context context, View footer)
	{
		this.footer = footer;
		// 滚动过多少距离后才开始计算是否隐藏/显示头尾元素。这里用了默认touchslop的0.9倍。
		touchSlop = (int) (ViewConfiguration.get(context).getScaledTouchSlop() * 0.9);

		// 不能为null
		if (this.footer == null) { throw new UnsupportedOperationException(
				"'footer' must be instantiated"); }
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			lastY = event.getY();
			currentY = event.getY();
			currentDirection = 0;
			lastDirection = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			float tmpCurrentY = event.getY();
			if (Math.abs(tmpCurrentY - lastY) > touchSlop)
			{// 滑动距离大于touchslop时才进行判断
				currentY = tmpCurrentY;
				currentDirection = (int) (currentY - lastY);
				if (lastDirection != currentDirection)
				{
					// 如果与上次方向不同，则执行显/隐动画
					if (currentDirection < 0)
					{
						AnimatorSetAddListenerUtil.create().animateHide(footer);
					} else
					{
						AnimatorSetAddListenerUtil.create().animateBack(footer);
					}
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// 手指抬起的时候要把currentDirection设置为0，这样下次不管向哪拉，都与当前的不同（其实在ACTION_DOWN里写了之后这里就用不着了……）
			currentDirection = 0;
			lastDirection = 0;
			break;
		}
		return false;
	}

	private static class AnimatorSetAddListenerUtil
	{
		private AnimatorSet backAnimatorSet;// 这是显示头尾元素使用的动画
		private AnimatorSet hideAnimatorSet;// 这是隐藏头尾元素使用的动画

		private int occupyHeight;

		private static AnimatorSetAddListenerUtil animatorSetsUtil;

		public static AnimatorSetAddListenerUtil create()
		{
			if (null == animatorSetsUtil)
				animatorSetsUtil = new AnimatorSetAddListenerUtil();
			return animatorSetsUtil;
		}

		private AnimatorSetAddListenerUtil()
		{
		}

		// public static AnimatorSetAddListenerUtil create(final int
		// occupyHeight)
		// {
		// if (null == animatorSetsUtil)
		// animatorSetsUtil = new AnimatorSetAddListenerUtil(occupyHeight);
		// return animatorSetsUtil;
		// }

		// private AnimatorSetAddListenerUtil(int occupyHeight)
		// {
		// this.occupyHeight = occupyHeight;
		// Log.v("TAG", "occupyViewHeight = " + occupyHeight);
		// if (this.occupyHeight <= 0)
		// throw new IllegalArgumentException(
		// "occupy height is not allow <=0");
		// }

		public void animateBack(View footerBar)
		{
			// 先清除其他动画
			if (hideAnimatorSet != null && hideAnimatorSet.isRunning())
			{
				hideAnimatorSet.cancel();
			}
			if (backAnimatorSet != null && backAnimatorSet.isRunning())
			{
				// 如果这个动画已经在运行了，就不管它
			} else
			{
				backAnimatorSet = new AnimatorSet();
				// 下面两句是将头尾元素放回初始位置。
				ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(
						footerBar, "translationY", footerBar.getTranslationY(),
						0f);
				ArrayList<Animator> animators = new ArrayList<>();
				animators.add(footerAnimator);
				// 添加一个动画的减速器
				backAnimatorSet
						.setInterpolator(new DecelerateInterpolator(1.6f));
				backAnimatorSet.setDuration(300);
				backAnimatorSet.playTogether(animators);
				backAnimatorSet.start();
			}
		}

		public void animateHide(View footerBar)
		{
			// 先清除其他动画
			if (backAnimatorSet != null && backAnimatorSet.isRunning())
			{
				backAnimatorSet.cancel();
			}
			if (hideAnimatorSet != null && hideAnimatorSet.isRunning())
			{
				// 如果这个动画已经在运行了，就不管它
			} else
			{
				hideAnimatorSet = new AnimatorSet();
				ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(
						footerBar, "translationY", footerBar.getTranslationY(),
						footerBar.getHeight());// 将Button隐藏到下面
				ArrayList<Animator> animators = new ArrayList<>();
				animators.add(footerAnimator);
				// hideAnimatorSet.setInterpolator(new
				// AccelerateInterpolator(1.6f));
				hideAnimatorSet.setDuration(200);
				hideAnimatorSet.playTogether(animators);
				hideAnimatorSet.start();
			}
		}

		/**
		 * 恢复显示
		 * 
		 * @param topBar
		 * @param footerBar
		 * @param occupyView
		 *            占位view
		 */
		@Deprecated
		public void animateBack(View topBar, View footerBar,
				final View occupyView)
		{
			// 先清除其他动画
			if (hideAnimatorSet != null && hideAnimatorSet.isRunning())
			{
				hideAnimatorSet.cancel();
			}
			if (backAnimatorSet != null && backAnimatorSet.isRunning())
			{
				// 如果这个动画已经在运行了，就不管它
			} else
			{
				backAnimatorSet = new AnimatorSet();
				// 下面两句是将头尾元素放回初始位置。
				ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(topBar,
						"translationY", topBar.getTranslationY(), 0f);
				ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(
						footerBar, "translationY", footerBar.getTranslationY(),
						0f);
				ArrayList<Animator> animators = new ArrayList<>();
				animators.add(headerAnimator);
				animators.add(footerAnimator);
				// 添加一个动画的减速器
				backAnimatorSet
						.setInterpolator(new DecelerateInterpolator(1.6f));
				backAnimatorSet.setDuration(300);
				backAnimatorSet.playTogether(animators);
				backAnimatorSet.start();

				// 对动画的整个实现过程进行监听。
				headerAnimator.addUpdateListener(new AnimatorUpdateListener()
				{
					int tempOffsetHeight = occupyHeight;

					@Override
					public void onAnimationUpdate(ValueAnimator animation)
					{
						float v = (Float) animation.getAnimatedValue();
						SZLog.d("TAG", "show = " + v);
						int offsetHeight = (int) v;
						// 当bar全部隐藏之后，不需要再刷新布局。
						if (tempOffsetHeight == 0) { return; }

						// 不断增加fixView所在viewgroup的高度
						LayoutParams layoutParams = occupyView
								.getLayoutParams();
						layoutParams.height = occupyHeight + offsetHeight;
						// 重新布局
						occupyView.requestLayout();

						tempOffsetHeight = Math.abs(offsetHeight);

						Log.v("TAG", "tempOffsetHeight = " + tempOffsetHeight);
					}
				});
			}
		}

		/**
		 * 隐藏动画
		 * 
		 * @param topBar
		 * @param footerBar
		 * @param occupyView
		 *            占位view
		 * 
		 */
		@Deprecated
		public void animateHide(View topBar, View footerBar,
				final View occupyView)
		{
			// 先清除其他动画
			if (backAnimatorSet != null && backAnimatorSet.isRunning())
			{
				backAnimatorSet.cancel();
			}
			if (hideAnimatorSet != null && hideAnimatorSet.isRunning())
			{
				// 如果这个动画已经在运行了，就不管它
			} else
			{
				hideAnimatorSet = new AnimatorSet();
				ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(topBar,
						"translationY", topBar.getTranslationY(),
						-topBar.getHeight());// 将ToolBar隐藏到上面
				ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(
						footerBar, "translationY", footerBar.getTranslationY(),
						footerBar.getHeight());// 将Button隐藏到下面
				ArrayList<Animator> animators = new ArrayList<>();
				animators.add(headerAnimator);
				animators.add(footerAnimator);
				// hideAnimatorSet.setInterpolator(new
				// AccelerateInterpolator(1.6f));
				hideAnimatorSet.setDuration(200);
				hideAnimatorSet.playTogether(animators);
				hideAnimatorSet.start();

				headerAnimator.addUpdateListener(new AnimatorUpdateListener()
				{
					int tempOffsetHeight = 0;

					@Override
					public void onAnimationUpdate(ValueAnimator animation)
					{
						float v = (Float) animation.getAnimatedValue();
						SZLog.d("TAG", "hide = " + v);
						int offsetHeight = (int) v;

						// 当bar全部隐藏之后，不需要再刷新布局。
						if (tempOffsetHeight >= occupyHeight) { return; }

						LayoutParams layoutParams = occupyView
								.getLayoutParams();
						// 不断减小occupyView所在viewgroup的高度
						layoutParams.height = occupyHeight + offsetHeight;
						occupyView.requestLayout();

						tempOffsetHeight = Math.abs(offsetHeight);

						Log.v("TAG", "tempOffsetHeight = " + tempOffsetHeight);
					}
				});
			}
		}
	}
}
