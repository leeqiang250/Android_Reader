package cn.com.pyc.pbbonline.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.conowen.libmad.MusicPlayer;
import com.sz.mobilesdk.common.BroadCastAction;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.view.widget.ToastShow;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.bean.event.MusicCircleEvent;
import cn.com.pyc.pbbonline.bean.event.MusicEndEvent;
import cn.com.pyc.pbbonline.bean.event.MusicListFileSelectEvent;
import cn.com.pyc.pbbonline.bean.event.MusicSwitchNameEvent;
import cn.com.pyc.pbbonline.common.IMusicConst;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.manager.NotificationPatManager;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.Util_;
import de.greenrobot.event.EventBus;

/**
 * 音乐声音播放
 */
public class MediaService extends Service implements OnErrorListener, OnCompletionListener,
		OnSeekCompleteListener
{
	private final String TAG = MediaService.class.getSimpleName();
	private Context mContext;
	private Timer timer;
	private MusicPlayer musicPlayer = null;
	private WakeLock wakeLock = null;

	private boolean isDisplay = true;
	private SZFile szFile;
	private List<SZFile> files;
	private NotificationPatManager patManager;

	@Override
	public void onCreate()
	{
		super.onCreate();
		EventBus.getDefault().register(this);
		mContext = this;
		patManager = new NotificationPatManager(mContext);
		acquireWakeLock(mContext);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null)
			return super.onStartCommand(intent, flags, startId);

		int option = intent.getIntExtra("option", -1);
		szFile = intent.getParcelableExtra("music_file");
		files = intent.getParcelableArrayListExtra("list_files");
		int process = intent.getIntExtra("process", 0);

		playMusicState(option, process);

		return super.onStartCommand(intent, flags, startId);
	}

	private void playMusicState(int option, int process)
	{
		switch (option)
		{
			case IMusicConst.OBTAIN_TIME:
				obtainTime();
				break;
			case IMusicConst.OPTION_PLAY:// 播放
				playing();
				break;
			case IMusicConst.OPTION_PAUSE:// 暂停
				pause();
				break;
			case IMusicConst.OPTION_CONTINUE:
				continuePlay();
				break;
			case IMusicConst.OPTION_CHANGE:
				changeProgress(process);
				break;
			case IMusicConst.RELEASE:// 释放资源
				releases();
				break;
			case IMusicConst.OPTION_STOP:
				stop();
				break;
			case -1:
				ToastShow.getToast().showFail(getApplicationContext(), "播放出错了！");
				break;
		}
	}

	private void stop()
	{
		SZLog.v(TAG, "stop");
		K.playState = IMusicConst.OPTION_STOP;
		K.CURRENT_MUSIC_ID = null;
		if (musicPlayer != null)
		{
			musicPlayer.stop();
			musicPlayer.release();
			musicPlayer = null;
		}
		if (timer != null)
		{
			timer.cancel();
			timer = null;
		}
		delMusicNotify();
		//	closeFloatView();
		stopSelf();
	}

	private void releases()
	{
		SZLog.v(TAG, "release");
		K.CURRENT_MUSIC_ID = null;
		if (musicPlayer != null)
		{
			musicPlayer.release();
			musicPlayer = null;
		}
		if (timer != null)
		{
			timer.cancel();
			timer = null;
		}
		// delMusicNotify();
		//	openCloseRoatate();
	}

	private void changeProgress(int process)
	{
		SZLog.v(TAG, "change prog");
		// 拖动进度改变
		if (musicPlayer != null)
		{
			musicPlayer.seekTo(process);
		}
	}

	private void continuePlay()
	{
		SZLog.v(TAG, "continue");
		K.playState = IMusicConst.OPTION_CONTINUE;
		if (musicPlayer != null)
		{
			musicPlayer.play();
		}
		//	openStartRoatate();
	}

	private void pause()
	{
		SZLog.v(TAG, "pause");
		K.playState = IMusicConst.OPTION_PAUSE;// 修改当前播放状态
		if (musicPlayer != null)
		{
			musicPlayer.pause();
		}
		//	openCloseRoatate();
	}

	private void playing()
	{
		SZLog.v(TAG, "play");
		K.CURRENT_MUSIC_ID = szFile.getContentId();
		K.playState = IMusicConst.OPTION_PLAY;
		if (musicPlayer == null)
		{
			String filePath = szFile.getFilePath();
			musicPlayer = new MusicPlayer(filePath, szFile.getCek_cipher_value());
		}
		play();
		//	openStartRoatate();
	}

	private void obtainTime()
	{
		SZLog.v(TAG, "obtain time");
		if (musicPlayer == null)
			musicPlayer = new MusicPlayer(szFile.getFilePath(), szFile.getCek_cipher_value());
		int mCurrentPosition = musicPlayer.getCurrentPosition();
		int mDuration = musicPlayer.getDuration();
		if (mCurrentPosition < ((mDuration - 1)))
		{
			String action = BroadCastAction.ACTION_MUSIC_OBTAIN_TIME;
			sendMsgBroadcast(action, mCurrentPosition, mDuration);
		}
	}

	private void play()
	{
		//播放，发送通知音乐图标转动。
		EventBus.getDefault().post(new MusicCircleEvent(true));
		try
		{
			musicPlayer.play();
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
			LogerEngine.error(mContext, "播放，发送通知音乐图标转动" + Log.getStackTraceString(e), true, ep);
			e.printStackTrace();
		}
		if (timer == null)
		{
			timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					if ((K.playState == IMusicConst.OPTION_PAUSE))
						return;
					if (musicPlayer != null)
					{
						int mCurrentPosition = musicPlayer.getCurrentPosition();
						int mDuration = musicPlayer.getDuration();
						if (mCurrentPosition < ((mDuration - 1)))
						{
							// 发送进度
							String action = BroadCastAction.ACTION_MUSIC_PROGRESS;
							sendMsgBroadcast(action, mCurrentPosition, mDuration);
						}
						else
						{
							// 根据不同的播放模式 切换不同的歌曲
							EventBus.getDefault().post(new MusicEndEvent());
							isDisplay = !isDisplay;
						}
					}
				}
			}, 5L, 500L);
		}
		// 加载歌词
		// loadLRC();
	}

	@Deprecated
	private void loadLRC()
	{
		//AlbumContent music = SZFile
		String path = "/storage/sdcard0/My Heart Will Go On.mp3";
		String name = path.substring(0, path.lastIndexOf("."));
		File lrcFile = new File(name + ".lrc");
		if (lrcFile == null || !lrcFile.exists())
		{
			lrcFile = new File(name + ".txt");
		}
		//ReadLRC(lrcFile);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		SZLog.v(TAG, TAG + " destroyed");
		EventBus.getDefault().unregister(this);
		releaseWakeLock();
		delMusicNotify();
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		SZLog.v(TAG, "music play complete!");
	}

	// 当音乐资源出现问题
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra)
	{
		ToastShow.getToast().showFail(getApplicationContext(), "资源出问题了");
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp)
	{
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/**
	 * 发送进度通知
	 * 
	 * @param action
	 * @param currentPos
	 * @param duration
	 */
	private void sendMsgBroadcast(String action, int currentPos, int duration)
	{
		Intent intent = new Intent(action);
		intent.putExtra("m.currentPosition", currentPos);
		intent.putExtra("m.duration", duration - 2);
		sendBroadcast(intent);
	}

	//	/**
	//	 * 打开悬浮图标/关闭旋转动画
	//	 * 
	//	 * @param context
	//	 */
	//	private void openCloseRoatate()
	//	{
	//		FloatViewService.openFloatView(getApplicationContext(), false);
	//	}
	//
	//	/**
	//	 * 打开并动画旋转
	//	 * 
	//	 * @param context
	//	 */
	//	private void openStartRoatate()
	//	{
	//		FloatViewService.openFloatView(getApplicationContext(), true);
	//	}
	//
	//	/**
	//	 * 关闭悬浮
	//	 */
	//	private void closeFloatView()
	//	{
	//		FloatViewService.closeFloatView(getApplicationContext());
	//	}

	private void startMediaService(SZFile szFile, int option)
	{
		OpenPageUtil.startMediaService(this, szFile, option, -1, files);
	}

	/**
	 * 播放完成 ，接收通知。
	 * 
	 * @param event
	 */
	public void onEventMainThread(MusicEndEvent event)
	{
		SZLog.e(TAG, "play one Music Finished");
		playMusicFinish();

	}

	/**
	 * 当前音乐播放结束
	 */
	private void playMusicFinish()
	{
		if (CommonUtil.isFastDoubleClick(600))
			return;

		switch (K.playMode)
		{
			case IMusicConst.SINGLE_R: // 单曲循环
				single();
				break;
			case IMusicConst.RANDOM: // 随机播放
				random();
				break;
			case IMusicConst.CIRCLE:// 列表循环
				circle();
				break;
			default:
				break;
		}
	}

	/**
	 * 单曲循环
	 */
	private void single()
	{
		if (szFile.isCheckOpen())
		{
			if (K.playState != IMusicConst.OPTION_STOP)
			{
				startMediaService(szFile, IMusicConst.RELEASE);// 释放资源
			}
			startMediaService(szFile, IMusicConst.OPTION_PLAY);// 播放
		}
		else
		{
			// 没有播放权限
			K.playState = IMusicConst.OPTION_STOP;
			startMediaService(szFile, IMusicConst.RELEASE);// 释放资源
			ToastShow.getToast().showBusy(getApplicationContext(), "没有播放权限");

			// EventBus.getDefault().post(new MusicUpdateStatusEvent(szFile));
			EventBus.getDefault().post(new MusicCircleEvent(false));
		}
	}

	/**
	 * 随机播放
	 */
	private void random()
	{
		if (files == null)
			return;
		int currentIndex = new Random().nextInt(files.size()); // [0-totalCount))
		szFile = files.get(currentIndex);
		if (szFile.isCheckOpen())
		{
			if (K.playState != IMusicConst.OPTION_STOP)
			{
				startMediaService(szFile, IMusicConst.RELEASE);// 释放资源
			}
			startMediaService(szFile, IMusicConst.OPTION_PLAY);// 播放
		}
		else
		{
			K.playState = IMusicConst.OPTION_STOP;
			startMediaService(szFile, IMusicConst.RELEASE);// 释放资源
			ToastShow.getToast().showBusy(getApplicationContext(), "没有播放权限");

			// EventBus.getDefault().post(new MusicUpdateStatusEvent(szFile));
			EventBus.getDefault().post(new MusicCircleEvent(false));

			// openCloseRoatate();
		}
		showMusicNotify(szFile);
		EventBus.getDefault().post(new MusicSwitchNameEvent(szFile));

		EventBus.getDefault().post(
				new MusicListFileSelectEvent(szFile.getContentId(), szFile.getName()));
	}

	/**
	 * 列表循环
	 */
	private void circle()
	{
		if (files == null)
			return;
		int currentIndex = Util_.getStartIndex(szFile.getContentId(), files);
		currentIndex = (currentIndex == (files.size() - 1)) ? 0 : ++currentIndex;
		SZLog.v(TAG, "circle-pos = " + currentIndex);
		szFile = files.get(currentIndex);
		if (szFile.isCheckOpen())
		{
			if (K.playState != IMusicConst.OPTION_STOP)
			{
				startMediaService(szFile, IMusicConst.RELEASE);// 释放资源
			}
			startMediaService(szFile, IMusicConst.OPTION_PLAY);// 播放
		}
		else
		{
			K.playState = IMusicConst.OPTION_STOP;
			startMediaService(szFile, IMusicConst.RELEASE);// 释放资源
			ToastShow.getToast().showBusy(getApplicationContext(), "没有播放权限");

			// EventBus.getDefault().post(new MusicUpdateStatusEvent(szFile));
			EventBus.getDefault().post(new MusicCircleEvent(false));

			//	openCloseRoatate();
		}
		showMusicNotify(szFile);
		EventBus.getDefault().post(new MusicSwitchNameEvent(szFile));

		EventBus.getDefault().post(
				new MusicListFileSelectEvent(szFile.getContentId(), szFile.getName()));
	}

	private void showMusicNotify(SZFile file)
	{
		if (patManager == null)
			patManager = new NotificationPatManager(getApplicationContext());
		patManager.updateNotification(file.getName(), "", file.isCheckOpen());//file.getAlbum_pic()
		patManager.setContentId(file.getContentId());
		patManager.setMusicFiles(files);
	}

	private void delMusicNotify()
	{
		if (patManager != null)
		{
			patManager.cancelNotification(getApplicationContext());
		}
	}

	/** 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行 */
	private void acquireWakeLock(Context ctx)
	{
		if (null == wakeLock)
		{
			PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "musicTag");
			if (null != wakeLock)
			{
				wakeLock.acquire();
			}
		}
	}

	/** 释放设备电源锁 */
	private void releaseWakeLock()
	{
		if (null != wakeLock && wakeLock.isHeld())
		{
			wakeLock.release();
			wakeLock = null;
		}
	}
}
