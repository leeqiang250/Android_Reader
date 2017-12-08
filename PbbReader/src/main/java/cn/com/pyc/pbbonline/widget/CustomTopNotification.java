package cn.com.pyc.pbbonline.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import com.sz.mobilesdk.util.CommonUtil;

import cn.com.pyc.pbb.reader.R;

/**
 * 自定义顶部通知栏
 * 
 * @author hudq
 */
public class CustomTopNotification implements View.OnTouchListener
{

	private static final int DIRECTION_LEFT = -1;
	private static final int DIRECTION_NONE = 0;
	private static final int DIRECTION_RIGHT = 1;

	private static final int DISMISS_INTERVAL = 4000;
	private static final int HIDE_WINDOW = 99;

	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWindowParams;
	private View mContentView;
	private Context mContext;
	private int mScreenWidth = 0;
	private int mStatusBarHeight = 0;
	private int downX = 0;
	private int direction = DIRECTION_NONE;
	private long startMillis;
	private long endMillis;
	private OnQuickNotifyListener listener;

	private boolean isShowing = false;
	private ValueAnimator restoreAnimator = null;
	private ValueAnimator dismissAnimator = null;
	private ImageView mIvIcon;
	private TextView mTvTitle;
	private TextView mTvContent;
	private TextView mTvTime;

	public CustomTopNotification(Builder builder)
	{
		mContext = builder.getContext();

		mStatusBarHeight = getStatusBarHeight();
		mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;

		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		mWindowParams.gravity = Gravity.TOP;
		mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		// 设置进入和退出动画
		mWindowParams.windowAnimations = R.style.CustomTopNotificationAnim;
		mWindowParams.x = 0;
		mWindowParams.y = -mStatusBarHeight;

		initContentView(mContext, builder);
	}

	private Handler mHandler = new Handler(new Handler.Callback()
	{
		@Override
		public boolean handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case HIDE_WINDOW:
					dismiss();
					break;
			}
			return false;
		}
	});

	/***
	 * 初始化内容视图
	 * 
	 * @param context
	 * @param builder
	 */
	private void initContentView(Context context, Builder builder)
	{
		mContentView = LayoutInflater.from(context).inflate(R.layout.custom_top_notification, null);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
		{
			//不处理
		}
		else
		{
			View v_state_bar = mContentView.findViewById(R.id.v_state_bar);
			ViewGroup.LayoutParams layoutParameter = v_state_bar.getLayoutParams();
			layoutParameter.height = mStatusBarHeight - CommonUtil.dip2px(mContext, 8f);
			v_state_bar.setLayoutParams(layoutParameter);
		}

		mIvIcon = (ImageView) mContentView.findViewById(R.id.iv_icon);
		mTvTitle = (TextView) mContentView.findViewById(R.id.tv_title);
		mTvContent = (TextView) mContentView.findViewById(R.id.tv_content);
		mTvTime = (TextView) mContentView.findViewById(R.id.tv_time);

		setIcon(builder.imgRes);
		setTitle(builder.title);
		setContent(builder.content);
		setTime(builder.time);

		mContentView.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (isAnimatorRunning())
		{
			return false;
		}
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				startMillis = System.currentTimeMillis();
				downX = (int) event.getRawX();
				break;
			case MotionEvent.ACTION_MOVE:
				// 处于滑动状态就取消自动消失
				mHandler.removeMessages(HIDE_WINDOW);
				int moveX = (int) event.getRawX() - downX;
				// 判断滑动方向
				direction = moveX > 0 ? DIRECTION_RIGHT : DIRECTION_LEFT;
				updateWindowLocation(moveX, mWindowParams.y);
				break;
			case MotionEvent.ACTION_UP:
				endMillis = System.currentTimeMillis();
				if (Math.abs(mWindowParams.x) > mScreenWidth / 4)
				{
					startDismissAnimator(direction);
				}
				else
				{
					startRestoreAnimator();
				}
				if ((endMillis - startMillis) < 100)
				{
					if (listener != null)
					{
						listener.onClick();
					}
					dismiss();
				}
				break;
			default:
				break;
		}
		return true;
	}

	private void startRestoreAnimator()
	{
		if (restoreAnimator != null && restoreAnimator.isRunning())
			return;

		restoreAnimator = ValueAnimator.ofInt(mWindowParams.x, 0);
		restoreAnimator.setDuration(300);
		restoreAnimator.setEvaluator(new IntEvaluator());

		restoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				// System.out.println("onAnimationUpdate:"
				// + animation.getAnimatedValue());
				updateWindowLocation((Integer) animation.getAnimatedValue(), -mStatusBarHeight);
			}
		});
		restoreAnimator.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				restoreAnimator = null;
				autoDismiss();
			}
		});
		restoreAnimator.start();
	}

	private void startDismissAnimator(int direction)
	{
		if (dismissAnimator != null && dismissAnimator.isRunning())
			return;

		dismissAnimator = ValueAnimator.ofInt(mWindowParams.x,
				direction == DIRECTION_LEFT ? -mScreenWidth : mScreenWidth);
		dismissAnimator.setDuration(300);
		dismissAnimator.setEvaluator(new IntEvaluator());

		dismissAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				updateWindowLocation((Integer) animation.getAnimatedValue(), -mStatusBarHeight);
			}
		});
		dismissAnimator.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				restoreAnimator = null;
				dismiss();
			}
		});
		dismissAnimator.start();
	}

	private boolean isAnimatorRunning()
	{
		return (restoreAnimator != null && restoreAnimator.isRunning())
				|| (dismissAnimator != null && dismissAnimator.isRunning());
	}

	/**
	 * 更新通知在窗口里的坐标
	 * 
	 * @param x
	 * @param y
	 */
	public void updateWindowLocation(int x, int y)
	{
		if (isShowing)
		{
			mWindowParams.x = x;
			mWindowParams.y = y;
			mWindowManager.updateViewLayout(mContentView, mWindowParams);
		}
	}

	public void show()
	{
		if (!isShowing)
		{
			isShowing = true;
			mWindowManager.addView(mContentView, mWindowParams);
			autoDismiss();
		}
	}

	public void dismiss()
	{
		if (isShowing)
		{
			resetState();
			mWindowManager.removeView(mContentView);
		}
	}

	/**
	 * 重置状态
	 */
	private void resetState()
	{
		isShowing = false;
		mWindowParams.x = 0;
		mWindowParams.y = -mStatusBarHeight;
	}

	/**
	 * 自动隐藏通知
	 */
	private void autoDismiss()
	{
		mHandler.removeMessages(HIDE_WINDOW);
		mHandler.sendEmptyMessageDelayed(HIDE_WINDOW, DISMISS_INTERVAL);
	}

	/**
	 * 获取状态栏的高度
	 */
	public int getStatusBarHeight()
	{
		int height = 0;
		int resId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resId > 0)
		{
			height = mContext.getResources().getDimensionPixelSize(resId);
		}
		Log.i("", "statusBarHeight = " + height);
		return height;
	}

	/**
	 * 设置显示图片
	 * 
	 * @param imgRes
	 */
	public void setIcon(int imgRes)
	{
		if (-1 != imgRes)
		{
			if (mIvIcon.getVisibility() == View.GONE)
				mIvIcon.setVisibility(View.VISIBLE);
			mIvIcon.setImageDrawable(mContext.getResources().getDrawable(imgRes));
		}
	}

	/**
	 * 设置显示标题
	 * 
	 * @param title
	 */
	public void setTitle(String title)
	{
		if (!TextUtils.isEmpty(title))
		{
			if (mTvTitle.getVisibility() == View.GONE)
				mTvTitle.setVisibility(View.VISIBLE);
			mTvTitle.setText(title);
		}
	}

	/**
	 * 设置显示内容
	 * 
	 * @param content
	 */
	public void setContent(String content)
	{
		mHandler.removeMessages(HIDE_WINDOW);

		mTvContent.setText(content);

		startRestoreAnimator();
	}

	/**
	 * 设置时间
	 * 
	 * @param time
	 */
	public void setTime(long time)
	{
		if (0 < time)
		{
			if (mTvTime.getVisibility() == View.GONE)
				mTvTime.setVisibility(View.VISIBLE);
			SimpleDateFormat formatDateTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
			mTvTime.setText(formatDateTime.format(new Date(time)));
		}
	}

	public void removeSoure()
	{
		if (null != mHandler)
		{
			mHandler.removeMessages(HIDE_WINDOW);
		}
	}

	public static class Builder
	{
		private Context context;
		private int imgRes = -1;
		private String title;
		private String content;
		private long time = -1;

		public Context getContext()
		{
			return context;
		}

		public Builder setContext(Context context)
		{
			this.context = context;
			return this;
		}

		public Builder setImgRes(int imgRes)
		{
			this.imgRes = imgRes;
			return this;
		}

		public Builder setTitle(String title)
		{
			this.title = title;
			return this;
		}

		public Builder setContent(String content)
		{
			this.content = content;
			return this;
		}

		public Builder setTime(long time)
		{
			this.time = time;
			return this;
		}

		public CustomTopNotification build()
		{

			if (null == context)
				throw new IllegalArgumentException("the context must init.");

			return new CustomTopNotification(this);
		}
	}

	public void setOnQuickNotifyListener(OnQuickNotifyListener listener)
	{
		this.listener = listener;
	}

	public interface OnQuickNotifyListener
	{
		void onClick();
	}

}
