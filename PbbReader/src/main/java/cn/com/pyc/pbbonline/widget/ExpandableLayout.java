package cn.com.pyc.pbbonline.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.com.pyc.pbb.reader.R;

public class ExpandableLayout extends LinearLayout {

	private Context mContext;
	private RelativeLayout mHandleView;
	private RelativeLayout mContentView;
	private ImageView mIconExpand;
	int mContentHeight = 0;
	int mTitleHeight = 0;
	private boolean isExpand;
	private Animation animationDown;
	private Animation animationUp;

	public ExpandableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (this.mContentHeight == 0) {
			this.mContentView.measure(widthMeasureSpec, 0);
			this.mContentHeight = this.mContentView.getMeasuredHeight();
		}
		if (this.mTitleHeight == 0) {
			this.mHandleView.measure(widthMeasureSpec, 0);
			this.mTitleHeight = this.mHandleView.getMeasuredHeight();
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.mHandleView = (RelativeLayout) this.findViewById(R.id.Trigger_start_off);// 触发事件的view
		this.mContentView = (RelativeLayout) this.findViewById(R.id.Share_details);// 隐藏的内容
		this.mIconExpand = (ImageView) this.findViewById(R.id.iv_trigg);

		this.mHandleView.setOnClickListener(new ExpandListener());
		this.mContentView.setOnClickListener(new ExpandListener());
	}

	private class ExpandListener implements View.OnClickListener {
		@Override
		public final void onClick(View paramView) {
			//clearAnimation是view的方法
			clearAnimation();
			if (!isExpand) {
				if (animationUp == null) {
					animationUp = new DropDownAnim(mContentView,
							mContentHeight, false);
					animationUp.setDuration(200); // SUPPRESS CHECKSTYLE
				}
				startAnimation(animationUp);
				mIconExpand.setImageResource(R.drawable.zhank);
				
			
				isExpand = true;
			} else {
				isExpand = false;
				if (animationDown == null) {
					animationDown = new DropDownAnim(mContentView,
							mContentHeight, true);
					animationDown.setDuration(200); // SUPPRESS CHECKSTYLE
				}
				startAnimation(animationDown);
				mContentView.startAnimation(AnimationUtils.loadAnimation(
						mContext, R.anim.animalpha));
				mIconExpand.setImageResource(R.drawable.shouq);
			}
		}
	}

	class DropDownAnim extends Animation {
		/** 目标的高度 */
		private int targetHeight;
		/** 目标view */
		private View view;
		/** 是否向下展开 */
		private boolean down;

		/**
		 * 构造方法
		 * 
		 * @param targetview
		 *            需要被展现的view
		 * @param vieweight
		 *            目的高
		 * @param isdown
		 *            true:向下展开，false:收起
		 */
		public DropDownAnim(View targetview, int vieweight, boolean isdown) {
			this.view = targetview;
			this.targetHeight = vieweight;
			this.down = isdown;
		}
		//down的时候，interpolatedTime从0增长到1，这样newHeight也从0增长到targetHeight
		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			int newHeight;
			if (down) {
				newHeight = (int) (targetHeight * interpolatedTime);
			} else {
				newHeight = (int) (targetHeight * (1 - interpolatedTime));
			}
			view.getLayoutParams().height = newHeight;
			view.requestLayout();
			if (view.getVisibility() == View.GONE) {
				view.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}
}
