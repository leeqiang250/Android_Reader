package cn.com.pyc.plain.record;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

/**
 * 本程序播放器的一个基本类
 * <p>
 * 实现了播放所需的基本操作以及操作后的回调工作<br>
 * 自动实现了时间进度的管理（调用start、stop方法后会自动开始、结束计时）
 * 
 * @author QiLiKing 2015-3-24
 */
public abstract class PlayerBase
{
	public enum PlayerCmd
	{
		Reset,

		StartPause,
		/**
		 * 用户没有pause就干其他事情了
		 */
		Pause, //

		Seek,

		Release;

		public Object prmt;
	}

	private static final int TIMER_PERIOD = 1000;

	protected int curProgress;
	protected int duration;

	protected Context context;
	protected String filePath;
	protected OnPlayerListener listener;

	private boolean isWorking = false;
	private Timer timer;

	protected abstract void start();

	protected abstract void pause();

	protected abstract void seek();

	protected abstract void release();

	public PlayerBase(Context context)
	{
		this.context = context;
	}

	public PlayerBase(Context context, String filePath)
	{
		this.context = context;
		this.filePath = filePath;
	}

	public boolean isWorking()
	{
		return isWorking;
	}

	public void executeCmd(PlayerCmd cmd)
	{
		switch (cmd)
		{
			case Reset:
				filePath = (String) PlayerCmd.Reset.prmt;
				break;

			case StartPause:
				if (!isWorking)
				{
					start();
				}
				else
				{
					pause();
				}
				break;

			case Pause:
				pause();
				break;

			case Seek:
				curProgress = (Integer) PlayerCmd.Seek.prmt;
				seek();
				break;

			case Release:
				release();
				break;

			default:
				break;
		}
	}

	protected void startTimer()
	{
		// listener.onProgressChanged(curProgress, duration); //
		// 先将0的状态传出去，可以做相关的事情。比如auditionWave的reset功能
		timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (listener != null)
				{
					listener.onProgressChanged(curProgress, duration);
					curProgress += TIMER_PERIOD;
				}
			}
		}, 0, TIMER_PERIOD);

		isWorking = true;
		if (listener != null)
		{
			listener.onStateChanged(isWorking);
		}
	}

	protected void stopTimer()
	{
		if (timer != null)
		{
			timer.cancel();
		}

		isWorking = false;
		if (listener != null)
		{
			listener.onStateChanged(isWorking);
		}
	}

	public void setOnPlayerListener(OnPlayerListener listener)
	{
		this.listener = listener;
	}

	public interface OnPlayerListener
	{
		/**
		 * 此方法是在线程中调用的
		 * 
		 * @param volume
		 *            当前录制时声音的大小。范围：0～9
		 */
		void onVolumeChanged(float volume);

		/**
		 * 此方法是在线程中调用的
		 * 
		 * @param curProgress
		 *            当前录音进度
		 */
		void onProgressChanged(int... progress);

		void onError(String reason);

		/**
		 * @param isWorking
		 *            true：正在录音或者正在试听
		 */
		void onStateChanged(boolean isWorking);

	}
}
