package cn.com.pyc.reader.video;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool._SysoXXX;
import com.qlk.util.tool.Util.ScreenUtil;

import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import cn.com.pyc.reader.IPlayer;
import cn.com.pyc.reader.IPlayerStateListener;
import cn.com.pyc.reader.PlayFile;

public class VideoPlayer extends SurfaceView implements IPlayer
{
	private IjkMediaPlayer mMediaPlayer;
	private PlayFile mPlayFile;
	private Context mContext;
	private IPlayerStateListener mStateListener;

	public VideoPlayer(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		getHolder().addCallback(callback);
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
	}

	@Override
	public void play(PlayFile playFile)
	{
		if (mPlayFile != playFile)
		{
			mPlayFile = playFile;
		}
		if (!isShown())
		{
			return;
		}

		release();	// 必须release
		mMediaPlayer = new IjkMediaPlayer();
		mMediaPlayer.setDisplay(getHolder());
		mMediaPlayer.setScreenOnWhilePlaying(true);
		mMediaPlayer.setOnPreparedListener(preparedListener);
		mMediaPlayer.setOnVideoSizeChangedListener(sizeChangedListener);
		mMediaPlayer.setOnCompletionListener(completionListener);
		mMediaPlayer.setOnErrorListener(errorListener);
		try
		{
			_SysoXXX.message("视频的偏移量：" + playFile.getOffset()+"---"+playFile.getFileLen());
			mMediaPlayer.setDataSource(playFile.getFilePath(), playFile.getKey(),
					playFile.getCodeLen(), playFile.getOffset(),playFile.getFileLen());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mMediaPlayer.prepareAsync();
	}

	@Override
	public void start()
	{
		if (mMediaPlayer != null && !mMediaPlayer.isPlaying())
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

	/*-****************************************
	 * 横竖屏切换
	 *****************************************/

	public void onConfigurationChanged()
	{
		setVideoLayout(mVideoLayout);
	}

	private int mVideoLayout = VIDEO_LAYOUT_SCALE;
	public static final int VIDEO_LAYOUT_ORIGIN = 0;
	public static final int VIDEO_LAYOUT_SCALE = 1;
	public static final int VIDEO_LAYOUT_STRETCH = 2;
	public static final int VIDEO_LAYOUT_ZOOM = 3;

	private int mVideoWidth;
	private int mVideoHeight;
	private int mVideoSarNum;
	private int mVideoSarDen;
	private int mSurfaceWidth;
	private int mSurfaceHeight;

	/**
	 * Set the display options
	 * 
	 * @param layout
	 *            <ul>
	 *            <li>{@link #VIDEO_LAYOUT_ORIGIN}
	 *            <li>{@link #VIDEO_LAYOUT_SCALE}
	 *            <li>{@link #VIDEO_LAYOUT_STRETCH}
	 *            <li>{@link #VIDEO_LAYOUT_ZOOM}
	 *            </ul>
	 * @param aspectRatio
	 *            video aspect ratio, will audo detect if 0.
	 */
	public void setVideoLayout(int layout)
	{
		LayoutParams lp = getLayoutParams();
		DisplayMetrics dm = ScreenUtil.getScreenRect(mContext);
		int windowWidth = dm.widthPixels;
		int windowHeight = dm.heightPixels;
		float windowRatio = (float) windowWidth / windowHeight;
		int sarNum = mVideoSarNum;
		int sarDen = mVideoSarDen;
		if (mVideoHeight > 0 && mVideoWidth > 0)
		{
			float videoRatio = (float) mVideoWidth / mVideoHeight;
			if (sarNum > 0 && sarDen > 0)
			{
				videoRatio = videoRatio * sarNum / sarDen;
			}
			mSurfaceHeight = mVideoHeight;
			mSurfaceWidth = mVideoWidth;
			if (VIDEO_LAYOUT_ORIGIN == layout && mSurfaceWidth < windowWidth
					&& mSurfaceHeight < windowHeight)
			{
				lp.width = (int) (mSurfaceHeight * videoRatio);
				lp.height = mSurfaceHeight;
			}
			else if (layout == VIDEO_LAYOUT_ZOOM)
			{
				lp.width = windowRatio > videoRatio ? windowWidth
						: (int) (videoRatio * windowHeight);
				lp.height = windowRatio < videoRatio ? windowHeight
						: (int) (windowWidth / videoRatio);
			}
			else
			{
				boolean full = layout == VIDEO_LAYOUT_STRETCH;
				lp.width = (full || windowRatio < videoRatio) ? windowWidth
						: (int) (videoRatio * windowHeight);
				lp.height = (full || windowRatio > videoRatio) ? windowHeight
						: (int) (windowWidth / videoRatio);
			}
			setLayoutParams(lp);
			getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
		}
		mVideoLayout = layout;
	}

	OnVideoSizeChangedListener sizeChangedListener = new OnVideoSizeChangedListener()
	{
		public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum,
				int sarDen)
		{
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			mVideoSarNum = sarNum;
			mVideoSarDen = sarDen;
			if (mVideoWidth != 0 && mVideoHeight != 0)
			{
				setVideoLayout(mVideoLayout);
			}
		}
	};

	private OnCompletionListener completionListener = new OnCompletionListener()
	{

		@Override
		public void onCompletion(IMediaPlayer mp)
		{
			_SysoXXX.message(getClass(), "onCompletion");
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
			final long mem = mPlayFile.getMemoryPos();
			if (mem > 0)
			{
				GlobalToast.toastShort(mContext, "跳转到上次播放位置");
			}
			mMediaPlayer.seekTo(mem);
			if (mp.getVideoHeight() != 0 && mp.getVideoWidth() != 0)
			{
				setVideoLayout(mVideoLayout);
			}

			if (mStateListener != null)
			{
				mStateListener.onStateChanged(true);
			}
		}
	};

	private SurfaceHolder.Callback callback = new Callback()
	{

		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{
			mPlayFile.setMemoryPos(getCurPos());
			release();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{
			play(mPlayFile);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			if (mMediaPlayer != null)
			{
				mMediaPlayer.setDisplay(holder);
			}
		}
	};

}
