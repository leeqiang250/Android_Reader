package cn.com.pyc.reader.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util.FileUtil;

import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.reader.IPlayerStateListener;
import cn.com.pyc.reader.PlayFile;
import cn.com.pyc.reader.ReaderBaseActivity;
import cn.com.pyc.reader.music.MusicPlayerService.LocalBinder;
import cn.com.pyc.widget.MySeekBar;
import cn.com.pyc.xcoder.XCoder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MusicPlayerActivity extends ReaderBaseActivity
{
	private static final String MUSIC_PROGRESS = "music_progress";

	private final Handler mHandler = new Handler();

	private TextView g_txtCurTime;
	private TextView g_txtDuration;
	private ImageButton g_imbPlay;
	private SeekBar g_skbProgress;
	private ImageView g_imvVolume;
	private MusicPlayerService service;
	private MusicPlayer musicPlayer;
	private ServiceConnection connection;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		System.out.println("444444444444444444");
		setContentView(R.layout.activity_reader_music);
		super.onCreate(savedInstanceState);

		LogerEngine.info(getApplication(),"音乐界面",null);

		findViewAndSetListeners();

		initVolumeBar();

		initService();
		
	}

	private Runnable mProgressRunnable = new Runnable()
	{

		@Override
		public void run()
		{
			int cur = musicPlayer.getCurPos();
			int max = musicPlayer.getDuration();
			g_skbProgress.setMax(max);
			g_skbProgress.setProgress(cur);
			g_txtCurTime.setText(formatTime(cur, max));
			g_txtDuration.setText(formatTime(max, max));

			mHandler.postDelayed(mProgressRunnable, 1000);
		}
	};

	private IPlayerStateListener mStateListener = new IPlayerStateListener()
	{

		@Override
		public void onStateChanged(boolean isPlaying)
		{
			if (isPlaying)
			{
				mHandler.post(mProgressRunnable);
				g_imbPlay.setBackgroundResource(R.drawable.music_play);
			}
			else
			{
				mHandler.removeCallbacks(mProgressRunnable);
				g_imbPlay.setBackgroundResource(R.drawable.music_pause);
			}
		}

		@Override
		public void onProgressChanged(int progress, int duration)
		{

		}

		@Override
		public void onError(int what)
		{
			GlobalToast.toastShort(MusicPlayerActivity.this,
					String.format(Locale.CHINA, "无法播放该文件（errorCode=%d）", what));
			finish();
		}

		@Override
		public void onComplete()
		{
			SharedPreferences sp = getSharedPreferences(MUSIC_PROGRESS,
					Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = sp.edit();
			edit.remove(mCurPath);	// 清零记忆播放
			edit.commit();
		}
	};

	private void initService()
	{
		System.out.println("55555555555555");
		connection = new ServiceConnection()
		{

			@Override
			public void onServiceDisconnected(ComponentName name)
			{
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder binder)
			{
				service = ((LocalBinder) binder).getService();
				musicPlayer = service.getPlayer();
				musicPlayer.setListener(mStateListener);

				startPlayer(mCurPath);
			}
		};

		Intent intent = new Intent(this, MusicPlayerService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	private void startPlayer(String path)
	{
		System.out.println("6666666666666666");
		PlayFile playFile = null;
		if (isFromSm && smInfo != null)
		{
//			String path1 = Environment.getExternalStorageDirectory()+File.separator+"123.mp3.pbb";
//			try
//			{
//				FileOutputStream fileOutputStream = new FileOutputStream(path1);
//				FileInputStream fileInputStream = new FileInputStream(path);
//				fileInputStream.skip(smInfo.getOffset());
//				byte[] data = new byte[2<<20];
//				int read = 0;
//				while((read = fileInputStream.read(data))!=-1)
//				{
//					fileOutputStream.write(data,0,read);
//				}
//				path = path1;
//				fileInputStream.close();
//				fileOutputStream.close();
//			}
//			catch (Exception e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			//倒计时
			showLimitView((TextView) findViewById(R.id.arm_txt_countdown));
			playFile = new PlayFile(path, XCoder.wrapEncodeKey(smInfo
					.getEncodeKey()), smInfo.getCodeLen());
			playFile.setOffset(smInfo.getOffset());
			playFile.setFileLen(smInfo.getFileLen());
		}
		else if (isCipher)
		{
			playFile = new XCoder(this).getPlayFileInfo(path);	// Ŀǰ��û��isCipher��
		}
		else
		{
			playFile = new PlayFile(path);
		}
		playFile.setMemoryPos(getSharedPreferences(MUSIC_PROGRESS,
				Context.MODE_PRIVATE).getLong(mCurPath, 0));
		musicPlayer.play(playFile);

		String fileName = FileUtil.getFileName(mCurPath);
		if (isFromSm)
		{
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}
		((TextView) findViewById(R.id.arm_txt_title)).setText(fileName);
	}

	@Override
	protected void onTimerFinished()
	{
		super.onTimerFinished();
		stopServer();
	}

	@Override
	public void findViewAndSetListeners()
	{
		System.out.println("8888888888888888");
		g_txtCurTime = (TextView) findViewById(R.id.arm_txt_cur);
		g_txtDuration = (TextView) findViewById(R.id.arm_txt_duration);
		g_imbPlay = (ImageButton) findViewById(R.id.arm_imb_play);
		g_skbProgress = (SeekBar) findViewById(R.id.arm_skb_progress);
		mVolumeBar = (MySeekBar) findViewById(R.id.arm_skb_volume);
		g_imvVolume = (ImageView) findViewById(R.id.arm_imv_volume);

		g_skbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				musicPlayer.seekTo(seekBar.getProgress());
				g_imvVolume.setBackgroundResource(seekBar.getProgress() > 0 ? R.drawable.music_volume_yes
						: R.drawable.music_volume_no);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
			}
		});
		mVolumeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				if (fromUser)
				{
					getAudioManager().setStreamVolume(
							AudioManager.STREAM_MUSIC, progress, 0);
				}
			}
		});
		g_imbPlay.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				musicPlayer.startOrPause();
			}
		});
	}

	/*-********************************************
	 * 声音
	 *********************************************/
	private AudioManager am;
	private MySeekBar mVolumeBar;

	private AudioManager getAudioManager()
	{
		if (am == null)
		{
			am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}
		return am;
	}

	private void initVolumeBar()
	{
		System.out.println("999999999999999999");
		mVolumeBar.setMax(getAudioManager().getStreamMaxVolume(
				AudioManager.STREAM_MUSIC));
		mVolumeBar.setProgress(getAudioManager().getStreamVolume(
				AudioManager.STREAM_MUSIC));
	}

	private void changeVolume(boolean up)
	{
		int curVolume = getAudioManager().getStreamVolume(
				AudioManager.STREAM_MUSIC);
		curVolume = up ? curVolume + 1 : curVolume - 1;
		getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, curVolume,
				0);
		mVolumeBar.setProgress(curVolume);
	}

	/* onStop()时surfaceView就destory了 */
	@Override
	protected void onPause()
	{
		super.onPause();
		if(musicPlayer == null) return;
		SharedPreferences sp = getSharedPreferences(MUSIC_PROGRESS,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = sp.edit();
		edit.putLong(mCurPath, musicPlayer.getCurPos());
		edit.commit();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		service.stopBackground();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
		mHandler.removeCallbacksAndMessages(null);
	}

	/*-********************************
	 * BroadcastReceiver
	 *********************************/

	// private BroadcastReceiver playerReceiver = new BroadcastReceiver()
	// {
	// @Override
	// public void onReceive(Context context, Intent intent)
	// {
	// String error = intent
	// .getStringExtra(MediaNotifier.MEDIA_ERROR_INFO);
	// if (!TextUtils.isEmpty(error))
	// {
	// GlobalToast.toastShort(context, error);
	// finish();
	// return;
	// }
	//
	// String filePath = intent
	// .getStringExtra(MediaNotifier.MEDIA_COMPLETE);
	// if (!TextUtils.isEmpty(filePath))
	// {
	// SharedPreferences sp = getSharedPreferences(MUSIC_PROGRESS,
	// Context.MODE_PRIVATE);
	// SharedPreferences.Editor edit = sp.edit();
	// edit.remove(g_strCurPath); // ��ս��ȣ��´δ�0��ʼ����
	// edit.commit();
	// }
	//
	// g_imbPlay
	// .setBackgroundResource(intent.getBooleanExtra(
	// MediaNotifier.MEDIA_IS_PLAYING, false) ? R.drawable.music_pause
	// : R.drawable.music_play);
	//
	// int cur = intent.getIntExtra(MediaNotifier.MEDIA_CURRENT_PROGRESS,
	// 0);
	// int max = intent.getIntExtra(MediaNotifier.MEDIA_DURATION, 0);
	// if (cur > max)
	// {
	// cur = max;
	// }
	// // ��ȻseekTo(0)����������ʱȴ����0
	// if (cur > g_skbProgress.getProgress() || cur <= 300)
	// {
	// //
	// ��seekʱ����������������Ȼ������ǰ�ߣ���Ϊ��������Ӧǰ��notifier��֪ͨ���£����ʼ�������ж�
	// if (cur <= 300)
	// {
	// cur = 0;
	// }
	// g_skbProgress.setProgress(cur);
	// }
	// g_skbProgress.setMax(max);
	// g_txtCurTime.setText(formatTime(cur, max));
	// g_txtDuration.setText(formatTime(max, max));
	// }
	// };

	protected String formatTime(int progress, int max)
	{
		progress /= 1000;
		int minute = progress / 60;
		int hour = minute / 60;
		int second = progress % 60;
		minute %= 60;
		if (max / 3600000 == 0)
		{
			return String.format(Locale.CHINESE, "%02d:%02d", minute, second);
		}
		else
		{
			return String.format(Locale.CHINESE, "%02d:%02d:%02d", hour,
					minute, second);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			changeVolume(true);
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			changeVolume(false);
			return true;	
		}
		else if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			stopServer();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackButtonClick(View v)
	{
		super.onBackButtonClick(v);
		stopServer();
	}

	private void stopServer()
	{
		stopService(new Intent(this, MusicPlayerService.class));
	}

	@Override
	protected void afterDeXXX()
	{

	}

}
