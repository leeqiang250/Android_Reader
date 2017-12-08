package cn.com.pyc.pbbonline.listener;

import com.sz.mobilesdk.util.SZLog;

import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

/**
 * 返回手势
 * 
 * @author hudq
 * 
 */
public abstract class OnBackGestureListener implements OnGestureListener
{

	private String TAG = OnBackGestureListener.class.getSimpleName();
	private static final int FLING_MIN_DISTANCE = 250;// 移动最小距离
	private static final int FLING_MIN_VELOCITY = 200;// 移动最小速度

	@Override
	public boolean onDown(MotionEvent e)
	{
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY)
	{
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY)
	{
		// e1：第1个ACTION_DOWN MotionEvent
		// e2：最后一个ACTION_MOVE MotionEvent
		// velocityX：X轴上的移动速度（像素/秒）
		// velocityY：Y轴上的移动速度（像素/秒）
		// X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
		// 向左手势
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY)
		{
			SZLog.d(TAG, "<-----向左手势-----");
		}
		// 向右手势
		if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY)
		{
			SZLog.d(TAG, "------向右手势----->");
			onFlingRight();
		}
		return true;
	}

	public abstract void onFlingRight();

}
