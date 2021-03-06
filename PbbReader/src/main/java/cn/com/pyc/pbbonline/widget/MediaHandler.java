package cn.com.pyc.pbbonline.widget;

import java.lang.ref.WeakReference;

import cn.com.pyc.pbbonline.bean.SZFile;

import com.sz.mobilesdk.util.UIHelper;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

/**
 * Video Controller,send Msg.
 */
public abstract class MediaHandler extends Handler
{
	private WeakReference<Activity> reference;
	// 这两个值不能设为0（arg1默认是0）
	public static final int POSITIVE = 200; // 播放、显示buffer
	public static final int NEGATIVE = -200; // 暂停、隐藏buffer
	// 播放状态state
	public static final int STATE_ERROR = -1;
	public static final int STATE_PLAY_PAUSE = 0;
	public static final int STATE_PROGRESS = 1; // 当前进度
	public static final int STATE_PREPARED_PLAY = 2; // 切换视频，准备播放
	public static final int STATE_BUFFERING = 3;
	public static final int STATE_NO_PLAY_PERMISSION = 4;// 没有播放权限　
	public static final int STATE_PLAY_COMPLETE = 5;// 播放完毕

	public MediaHandler(Activity activity)
	{
		reference = new WeakReference<Activity>(activity);
	}

	@Override
	public void handleMessage(Message msg)
	{
		super.handleMessage(msg);

		if (reference == null)
			return;
		Activity ac = reference.get();
		if (null == ac)
			return;

		switch (msg.what)
		{
			case STATE_ERROR:
			{
				SZFile drmFile = (SZFile) msg.obj;
				error(ac, drmFile != null ? drmFile.getName() : "");
			}
				break;
			case STATE_PLAY_PAUSE: // 暂停/开始按钮更新
			{
				long state = msg.getData().getLong("arg1");
				boolean play = (state == MediaHandler.POSITIVE) ? true : false;
				pauseOrPlay(play);
			}
				break;
			case STATE_PROGRESS: // 更新进度
			{
				long progress = msg.getData().getLong("arg1");
				long duration = msg.getData().getLong("arg2");
				progressPlay(progress, duration);
			}
				break;
			case STATE_PREPARED_PLAY: // 切换。准备播放
			{
				SZFile drmFile = (SZFile) msg.obj;
				long duration = msg.getData().getLong("arg2");
				preparePlay(drmFile, duration);
			}
				break;
			case STATE_BUFFERING: // 缓冲
			{
				long state = msg.getData().getLong("arg1");
				boolean buffer = (state == MediaHandler.POSITIVE) ? true : false;
				bufferPlay(!buffer);
			}
				break;
			case STATE_NO_PLAY_PERMISSION: // 无权限
			{
				SZFile drmFile = (SZFile) msg.obj;
				noPermissionPlay(drmFile);
			}
				break;

			case STATE_PLAY_COMPLETE:
			{
				long duration = msg.getData().getLong("arg2");
				completePlay(duration);
			}
				break;
			default:
				break;
		}
	}

	protected void error(Activity ac, String title)
	{
		UIHelper.showSingleCommonDialog(ac, title, "播放出错(资源可能已经损坏或不存在！)", null, null);
	}

	/**
	 * 暂停或播放
	 * 
	 * @param statePlay
	 */
	protected abstract void pauseOrPlay(boolean statePlay);

	/**
	 * 切换视频。准备播放
	 * 
	 * @param drmFile
	 * @param duration
	 */
	protected abstract void preparePlay(SZFile drmFile, long duration);

	/**
	 * 进度更新
	 * 
	 * @param progress
	 * @param duration
	 */
	protected abstract void progressPlay(long progress, long duration);

	/**
	 * 缓冲
	 * 
	 * @param statePlay
	 */
	protected abstract void bufferPlay(boolean statePlay);

	/**
	 * 无权限
	 * 
	 * @param drmFile
	 */
	protected abstract void noPermissionPlay(SZFile drmFile);

	/**
	 * 播放完毕
	 * 
	 * @param duration
	 */
	protected abstract void completePlay(long duration);

}
