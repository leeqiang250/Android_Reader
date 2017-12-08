package cn.com.pyc.reader.music;

import android.content.Context;
import android.util.Log;

import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.SPUtil;

import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.reader.IPlayer;
import cn.com.pyc.reader.IPlayerStateListener;
import cn.com.pyc.reader.PlayFile;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MusicPlayer implements IPlayer
{
	private IjkMediaPlayer mMediaPlayer;
	private PlayFile mPlayFile;
	private Context mContext;
	private long mSeekPos;
	private IPlayerStateListener mStateListener;

	public MusicPlayer(Context context)
	{
		mContext = context;
	}

	public void setListener(IPlayerStateListener listener)
	{
		mStateListener = listener;
	}

	@Override
	public boolean isPlaying()
	{
		return mMediaPlayer == null ? false : mMediaPlayer.isPlaying();
	}

	@Override
	public int getCurPos()
	{
		return mMediaPlayer == null ? 0 : (int) mMediaPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration()
	{
		return mMediaPlayer == null ? 0 : (int) mMediaPlayer.getDuration();
	}

	@Override
	public void seekTo(int pos)
	{
		if (mMediaPlayer != null)
		{
			mMediaPlayer.seekTo(pos);
			start();
		}
		else
		{
			if (mPlayFile != null)
			{
				mSeekPos = pos;
				play(mPlayFile);
			}
		}
	}

	@Override
	public void play(PlayFile playFile)
	{
		System.out.println("7777777777777777");
		if (this.mPlayFile != playFile)
		{
			this.mPlayFile = playFile;
		}

		if (mMediaPlayer == null)
		{
			mMediaPlayer = new IjkMediaPlayer();
			mMediaPlayer.setOnPreparedListener(preparedListener);
			mMediaPlayer.setOnCompletionListener(completionListener);
			mMediaPlayer.setOnErrorListener(errorListener);
		}
		mMediaPlayer.reset();
		try
		{
			mMediaPlayer.setDataSource(playFile.getFilePath(), playFile.getKey(),
					playFile.getCodeLen(), playFile.getOffset(),playFile.getFileLen());
			mMediaPlayer.prepareAsync();
		}
		catch (Exception e)
		{
			ExtraParams ep = new ExtraParams();
			ep.account_name = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
			String password = (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, "");
			ep.account_password= AESUtil.encrypt(password);
			StackTraceElement[] trace =e.getStackTrace();
			if(trace==null||trace.length==0){
				ep.lines = -1;
			}else {
				ep.lines = trace[0].getLineNumber();
			}
			LogerEngine.error(mContext, "音乐播放失败，播放文件异常。" + Log.getStackTraceString(e), true, ep);
			e.printStackTrace();
		}
	}

	@Override
	public void start()
	{
		if (!isPlaying())
		{
			mMediaPlayer.start();
		}

		if (mStateListener != null)
		{
			mStateListener.onStateChanged(true);
		}
	}

	@Override
	public void pause()
	{
		if (isPlaying())
		{
			mMediaPlayer.pause();
		}

		if (mStateListener != null)
		{
			mStateListener.onStateChanged(false);
		}
	}

	@Override
	public void startOrPause()
	{
		if (mMediaPlayer == null && mPlayFile != null)
		{
			play(mPlayFile);
		}
		else
		{
			if (isPlaying())
			{
				pause();
			}
			else
			{
				start();
			}
		}
	}

	@Override
	public void release()
	{
		if (mMediaPlayer != null)
		{
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		if (mStateListener != null)
		{
			mStateListener.onStateChanged(false);
		}
	}

	private OnCompletionListener completionListener = new OnCompletionListener()
	{

		@Override
		public void onCompletion(IMediaPlayer mp)
		{
			if (mStateListener != null)
			{
				mStateListener.onComplete();
			}
		}
	};

	private OnErrorListener errorListener = new OnErrorListener()
	{

		@Override
		public boolean onError(IMediaPlayer mp, int what, int extra)
		{
			if (mStateListener != null)
			{
				mStateListener.onError(what);
			}
			return false;
		}
	};

	private OnPreparedListener preparedListener = new OnPreparedListener()
	{

		@Override
		public void onPrepared(IMediaPlayer mp)
		{
			long mem = mPlayFile.getMemoryPos();
			if (mem > 0)
			{
				GlobalToast.toastShort(mContext, "跳转到上次播放位置");
			}
			else if (mSeekPos > 0)
			{
				mem = mSeekPos;
				mSeekPos = 0;
			}
			mMediaPlayer.seekTo((int) mem);
			mMediaPlayer.start();

			if (mStateListener != null)
			{
				mStateListener.onStateChanged(true);
			}
		}
	};
}
