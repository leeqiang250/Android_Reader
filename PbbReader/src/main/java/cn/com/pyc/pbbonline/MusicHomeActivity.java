package cn.com.pyc.pbbonline;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.bean.event.ListAlbumSelectEvent;
import cn.com.pyc.pbbonline.bean.event.MusicCircleEvent;
import cn.com.pyc.pbbonline.bean.event.MusicSwitchNameEvent;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.common.IMusicConst;
import cn.com.pyc.pbbonline.fragment.FragmentMusicImg;
import cn.com.pyc.pbbonline.fragment.MusicViewPagerAdaper;
import cn.com.pyc.pbbonline.manager.NotificationPatManager;
import cn.com.pyc.pbbonline.util.ImageUtils;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.pbbonline.widget.RoundCornerIndicaor;
import cn.com.pyc.pbbonline.widget.WaveView;

import com.sz.mobilesdk.common.BroadCastAction;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.receiver.MusicPlayReceiver;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.view.widget.ToastShow;

import de.greenrobot.event.EventBus;

/**
 * 音乐主界面
 */
public class MusicHomeActivity extends FragmentActivity implements OnClickListener
{
	public static int bgId = 0;
	private static final int[] drawableBgIds = new int[]
	{ R.drawable.music_bg, R.drawable.music_bg_2, R.drawable.music_bg_3 };
	private static final String TAG = "MusicHomeUI";
	private static final int MUSIC_LIST_CODE = 99;
	private TextView musicname;
	private ImageButton prompt;
	private ViewPager mViewPager;
	private RoundCornerIndicaor indicaor;
	private TextView currTimeTextView;
	private TextView totalTimeTextView;
	private SeekBar progressSeekBar;
	private ImageButton pausebtn;
	private WaveView waveView;
	private MusicViewPagerAdaper adapter;
	private NotificationPatManager notifyPatManager;
	private List<SZFile> musicFiles;
	private int totalCount;
	private volatile SZFile mCurrentFile;
	private volatile int mCurrentIndex = 0;
	private boolean isActivityStart;
	private boolean isInitPlaying;

	private MusicPlayReceiver receiver = new MusicPlayReceiver()
	{
		@Override
		protected void playProgress(int current, int duration)
		{
			if (K.playState == IMusicConst.OPTION_STOP)
			{
				if (isActivityStart)
					initProgress();
				return;
			}
			if (isActivityStart)
			{
				//totalTimeTextView.setText(FormatterUtil.formatTime(duration));
				//progressSeekBar.setMax(duration);
				currTimeTextView.setText(FormatterUtil.formatSeconds(current));
				progressSeekBar.setProgress(current);
			}
		}

		@Override
		protected void obtainTime(int current, int duration)
		{
			if (isActivityStart)
			{
				totalTimeTextView.setText(FormatterUtil.formatSeconds(duration));
				currTimeTextView.setText(FormatterUtil.formatSeconds(current));
				progressSeekBar.setMax(duration);
				progressSeekBar.setProgress(current);
			}
		}

		@Override
		protected void onStatusBar(int buttonId)
		{
			if (buttonId == Fields.BUTTON_CLOSE_ID)
			{
				delMusicNotify();
				hideWaveVIew();
				OpenPageUtil.stopMediaService(getApplicationContext());
				EventBus.getDefault().post(new MusicCircleEvent(false));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pbbonline_activity_music_play);
		initials();
		getParams();
		getObtainTime();
		initView();

		if (isInitPlaying)
		{
			initPlaying();
		}
		else
		{
			// 点击同一个文件，已经在播放。
			showWaveView();
			showMusicNotify(getMusicName(), hasPermitted());
		}

		//当前音乐主界面，去掉悬浮窗
	}

	private void initials()
	{
		int index = new Random().nextInt(drawableBgIds.length);
		if (bgId == 0)
			bgId = drawableBgIds[index];
		ViewHelp.showAppTintStatusBar(this, R.color.touming);
		EventBus.getDefault().register(this);
		notifyPatManager = new NotificationPatManager(this);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// 监听电话状态, 如果接通了电话, 暂停
		TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);

		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadCastAction.ACTION_MUSIC_PROGRESS);// 进度条广播
		filter.addAction(BroadCastAction.ACTION_MUSIC_OBTAIN_TIME);// 第一次进入显示总时间
		filter.addAction(BroadCastAction.ACTION_MUSIC_STATUSBAR);// 音乐状态栏显示
		registerReceiver(receiver, filter);
	}

	private void initPlaying()
	{
		//检查本地文件是否存在？
		if (!FileUtil.checkFilePathExists(getMusicPath()))
		{
			ToastShow.getToast().showFail(getApplicationContext(),
					getString(R.string.msg_file_not_find));
			return;
		}

		//正在播放，则停止
		if (K.playState != IMusicConst.OPTION_STOP)
		{
			startMusicService(IMusicConst.RELEASE);
		}
		if (!hasPermitted())
		{
			releaseHasnotPermit();
			return;
		}
		// 播放
		startMusicService(IMusicConst.OPTION_PLAY);
		showWaveView();

		// notification
		showMusicNotify(getMusicName(), true);
		setMusicName(getMusicName());
	}

	private void getParams()
	{
		Intent intent = getIntent();
		String folderName = intent.getStringExtra("folderName");
		String contentId = intent.getStringExtra("contentId");
		musicFiles = intent.getParcelableArrayListExtra("musicFiles");
		isInitPlaying = intent.getBooleanExtra("is_init", true);
		if (musicFiles == null || contentId == null)
		{
			ToastShow.getToast().showFail(getApplicationContext(), "文件资源可能为空(0.0)");
			finish();
			return;
		}
		((TextView) findViewById(R.id.music_folder_name)).setText(folderName);
		totalCount = musicFiles.size();
		mCurrentIndex = Util_.getStartIndex(contentId, musicFiles);
		mCurrentFile = musicFiles.get(mCurrentIndex);
		SZLog.e(TAG, "open musiclist curPos = " + mCurrentIndex);
		//设置：notification赋值list
		notifyPatManager.setMusicFiles(musicFiles);
	}

	private void initView()
	{
		View musicbackground = findViewById(R.id.musicRL);
		musicbackground.setBackgroundDrawable(getResources().getDrawable(bgId));
		musicname = (TextView) findViewById(R.id.music_name);
		currTimeTextView = (TextView) findViewById(R.id.currTimeTextView);// 当前播放时间
		totalTimeTextView = (TextView) findViewById(R.id.totalTimeTextView);// 歌曲总时间
		progressSeekBar = (SeekBar) findViewById(R.id.progressSeekBar);// 播放进度条。
		pausebtn = (ImageButton) findViewById(R.id.pausebtn);// 播放prevbtn
		prompt = (ImageButton) findViewById(R.id.prompt);// 播放模式
		mViewPager = (ViewPager) findViewById(R.id.viewpager);// fragment模块
		indicaor = (RoundCornerIndicaor) findViewById(R.id.indicator_square);
		waveView = (WaveView) findViewById(R.id.wave_view);
		waveView.setVisibility(View.INVISIBLE);
		TextView tv_number = (TextView) findViewById(R.id.tv_number);// 显示子专辑个数。
		tv_number.setText(totalCount + "");

		prompt.setOnClickListener(this);// 播放模式
		pausebtn.setOnClickListener(this);// 播放/暂停
		findViewById(R.id.music_back).setOnClickListener(this);
		findViewById(R.id.music_close).setOnClickListener(this);
		findViewById(R.id.menu).setOnClickListener(this);
		findViewById(R.id.prevbtn).setOnClickListener(this);// 播放上一曲
		findViewById(R.id.nextbtn).setOnClickListener(this); // 下一曲

		if (adapter == null)
			adapter = new MusicViewPagerAdaper(getSupportFragmentManager(), "", hasPermitted());
		mViewPager.setAdapter(adapter);
		indicaor.setViewPager(mViewPager, 2);
		FragmentMusicImg fg = (FragmentMusicImg) adapter.getItem(0); // 设置背景
		ImageUtils.getGaussambiguity(this, "", musicbackground, fg); //imageurl: mCurrentFile.getAlbum_pic()

		progressSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				if (!CommonUtil.isFastDoubleClick(600))
				{
					OpenPageUtil.startMediaService(getApplicationContext(), mCurrentFile,
							IMusicConst.OPTION_CHANGE, progressSeekBar.getProgress(), musicFiles);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				currTimeTextView.setText(FormatterUtil.formatSeconds(progress));
			}
		});
	}

	/**
	 * 是否有权限
	 */
	private boolean hasPermitted()
	{
		return mCurrentFile != null ? mCurrentFile.isCheckOpen() : false;
	}

	/**
	 * 当前文件名称
	 * 
	 * @return
	 */
	private String getMusicName()
	{
		return mCurrentFile != null ? mCurrentFile.getName() : "";
	}

	private String getMusicPath()
	{
		return mCurrentFile != null ? mCurrentFile.getFilePath() : "";
	}

	private void getObtainTime()
	{
		if (!hasPermitted())
		{
			// 本用户没有在本设备播放该音乐的权限
			ToastShow.getToast().showBusy(getApplicationContext(),
					getString(R.string.play_miss_authority));
			EventBus.getDefault().post(new MusicCircleEvent(false));
			return;
		}
		// init time.
		startMusicService(IMusicConst.OBTAIN_TIME);
	}

	/*
	 * 设置fg中歌曲名称的切换
	 */
	//	private void setFgMusicName(String name)
	//	{
	//		Fragment fragment = adapter.getItem(0);
	//		if (fragment instanceof FragmentMusicImg)
	//		{
	//			FragmentMusicImg fm = (FragmentMusicImg) fragment;
	//			fm.switchMusicName(name);
	//		}
	//		if (musicname != null)
	//			musicname.setText(name);
	//	}

	/**
	 * 设置标题歌曲名
	 * 
	 * @param musicName
	 */
	private void setMusicName(String musicName)
	{
		if (musicname != null)
		{
			musicname.setText(musicName);
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		isActivityStart = true;
		continuePlay();
	}

	private void continuePlay()
	{
		switch (K.playState)
		{
			case IMusicConst.OPTION_STOP:// 之前状态为停止 应该处理播放
			case IMusicConst.OPTION_PAUSE: // 当前状态是暂停 应该继续播放
				pausebtn.setBackgroundResource(R.drawable.pbbonline_music_play);
				break;
			case IMusicConst.OPTION_PLAY:// 当前状态如果是播放的情况 处理暂停
			case IMusicConst.OPTION_CONTINUE:
				pausebtn.setBackgroundResource(R.drawable.pbbonline_music_pause);
				break;
		}
		// 顺序播放--随机播放--单曲播放 -- 顺序播放
		switch (K.playMode)
		{
			case IMusicConst.RANDOM:
				/*** 随机播放 */
				prompt.setBackgroundResource(R.drawable.pbbonline_sequence);// 随机播放
				break;
			case IMusicConst.SINGLE_R:
				/*** 单曲循环 */
				prompt.setBackgroundResource(R.drawable.pbbonline_single);// 单曲
				break;
			case IMusicConst.CIRCLE:
				/*** 列表循环 */
				prompt.setBackgroundResource(R.drawable.pbbonline_shunxu);// 列表循环
				break;
			default:
				break;
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		isActivityStart = false;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		bgId = 0;
		unregisterReceiver(receiver);
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.music_back)
		{
			finishUI();
		}
		else if (id == R.id.music_close)
		{
			EventBus.getDefault().post(new MusicCircleEvent(false));
			OpenPageUtil.stopMediaService(this);
			delMusicNotify();
			hideWaveVIew();
			finishUI();
		}
		else if (id == R.id.menu)
		{
			openFileList(); // 文件列表
		}
		else if (id == R.id.prompt)
		{
			setPlayModel();
		}
		else if (id == R.id.pausebtn)
		{
			setPausebtn();
		}
		else if (id == R.id.prevbtn)
		{
			setPrevbtn();
		}
		else if (id == R.id.nextbtn)
		{
			setNextbtn();
		}
	}

	private void openFileList()
	{
		Intent it = new Intent(this, ListSZFileActivity.class);
		it.putExtra(K.JUMP_FLAG, K.UI_MUSIC);
		it.putParcelableArrayListExtra(ListSZFileActivity.FILE_FLAGS,
				(ArrayList<? extends Parcelable>) musicFiles);
		it.putExtra("title_name", getMusicName());
		it.putExtra("cur_contentId", mCurrentFile.getContentId());
		startActivityForResult(it, MUSIC_LIST_CODE);
	}

	/**
	 * 关闭当前activity
	 */
	private void finishUI()
	{
		finish();
		overridePendingTransition(R.anim.fade_in_scale, R.anim.acy_music_close);
	}

	// 顺序播放--随机播放--单曲播放 -- 顺序播放
	private void setPlayModel()
	{
		if (CommonUtil.isFastDoubleClick(600))
			return;
		switch (K.playMode)
		{
			case IMusicConst.CIRCLE:
			{
				/*** 随机播放 */
				K.playMode = IMusicConst.RANDOM;
				prompt.setBackgroundResource(R.drawable.pbbonline_sequence);// 随机播放
				ToastShow.getToast().showOk(getApplicationContext(),
						getString(R.string.play_random));
			}
				break;
			case IMusicConst.RANDOM:
			{
				/*** 单曲循环 */
				K.playMode = IMusicConst.SINGLE_R;
				prompt.setBackgroundResource(R.drawable.pbbonline_single);// 单曲
				ToastShow.getToast().showOk(getApplicationContext(),
						getString(R.string.play_single_cycle));
			}
				break;
			case IMusicConst.SINGLE_R:
			{
				/*** 列表循环 */
				K.playMode = IMusicConst.CIRCLE;
				prompt.setBackgroundResource(R.drawable.pbbonline_shunxu);// 列表循环
				ToastShow.getToast().showOk(getApplicationContext(),
						getString(R.string.play_list_cycle));
			}
				break;
			default:
				break;
		}
	}

	// 下一首
	private void setNextbtn()
	{
		if (CommonUtil.isFastDoubleClick(600))
			return;
		mCurrentIndex = (mCurrentIndex < (totalCount - 1)) ? ++mCurrentIndex : 0;
		SZLog.d(TAG, "next start pos = " + mCurrentIndex);
		mCurrentFile = musicFiles.get(mCurrentIndex);
		boolean isPermit = hasPermitted();
		if (!isPermit)
		{
			releaseHasnotPermit();
			return;
		}
		if (K.playState != IMusicConst.OPTION_STOP)
		{
			startMusicService(IMusicConst.RELEASE);// 释放资源
		}
		startMusicService(IMusicConst.OPTION_PLAY);// 播放
		setMusicName(getMusicName());
		showMusicNotify(getMusicName(), isPermit);
		// 发送通知，保存位置：
		EventBus.getDefault().post(
				new ListAlbumSelectEvent(mCurrentFile.getContentId(), Fields.MP3));
	}

	// 上一首
	private void setPrevbtn()
	{
		if (CommonUtil.isFastDoubleClick(600))
			return;
		mCurrentIndex = (mCurrentIndex >= 1) ? --mCurrentIndex : (mCurrentIndex = (totalCount - 1));
		SZLog.d(TAG, "pre start pos = " + mCurrentIndex);
		mCurrentFile = musicFiles.get(mCurrentIndex);
		boolean isPermit = hasPermitted();
		if (!isPermit)
		{
			releaseHasnotPermit();
			return;
		}
		if (K.playState != IMusicConst.OPTION_STOP)
		{
			startMusicService(IMusicConst.RELEASE);// 释放资源
		}
		startMusicService(IMusicConst.OPTION_PLAY);// 播放
		setMusicName(getMusicName());
		showMusicNotify(getMusicName(), isPermit);
		// 发送通知，保存位置：
		EventBus.getDefault().post(
				new ListAlbumSelectEvent(mCurrentFile.getContentId(), Fields.MP3));
	}

	/**
	 * 无权限，释放资源
	 */
	private void releaseHasnotPermit()
	{
		K.playState = IMusicConst.OPTION_STOP;
		startMusicService(IMusicConst.RELEASE);// 释放资源
		initProgress();
		hideWaveVIew();
		ToastShow.getToast().showBusy(getApplicationContext(),
				getString(R.string.play_miss_authority));
		EventBus.getDefault().post(new MusicCircleEvent(false));
	}

	// 播放按钮
	private void setPausebtn()
	{
		if (CommonUtil.isFastDoubleClick(600))
			return;
		switch (K.playState)
		{
			case IMusicConst.OPTION_STOP:// 之前状态为停止 --->应该处理播放
			{
				if (hasPermitted())
				{
					// 有播放权限
					startMusicService(IMusicConst.OPTION_PLAY);// 播放
					showWaveView();
					showMusicNotify(getMusicName(), true);
				}
				else
				{ // 本用户没有在本设备播放该音乐的权限
					ToastShow.getToast().showBusy(getApplicationContext(),
							getString(R.string.play_miss_authority));
				}
				EventBus.getDefault().post(new MusicCircleEvent(hasPermitted()));
				// pausebtn.setBackgroundResource(R.drawable.pbbonline_music_pause);
			}
				break;
			case IMusicConst.OPTION_PAUSE: // 当前状态是暂停 ---->应该继续播放
			{
				startMusicService(IMusicConst.OPTION_CONTINUE);
				showWaveView();
				showMusicNotify(getMusicName(), true);
				EventBus.getDefault().post(new MusicCircleEvent(true));
				// pausebtn.setBackgroundResource(R.drawable.pbbonline_music_pause);
			}
				break;
			case IMusicConst.OPTION_PLAY:// 当前状态如果是播放的情况----> 处理暂停
			case IMusicConst.OPTION_CONTINUE:
			{
				startMusicService(IMusicConst.OPTION_PAUSE);
				hideWaveVIew();
				delMusicNotify();
				EventBus.getDefault().post(new MusicCircleEvent(false));
				// pausebtn.setBackgroundResource(R.drawable.pbbonline_music_play);
			}
				break;
		}
	}

	private void showWaveView()
	{
		if (waveView == null)
			return;
		if (waveView.getVisibility() == View.INVISIBLE)
			waveView.setVisibility(View.VISIBLE);
	}

	private void hideWaveVIew()
	{
		if (waveView == null)
			return;
		if (waveView.getVisibility() == View.VISIBLE)
			waveView.setVisibility(View.INVISIBLE);
	}

	/**
	 * 初始显示进度、时间
	 */
	private void initProgress()
	{
		totalTimeTextView.setText("00:00");
		currTimeTextView.setText("00:00");
		progressSeekBar.setMax(0);
		progressSeekBar.setProgress(0);
		pausebtn.setBackgroundResource(R.drawable.pbbonline_music_play);
	}

	/**
	 * 开启服务
	 * 
	 * @param path
	 * @param key
	 * @param option
	 */
	private void startMusicService(int option)
	{
		OpenPageUtil.startMediaService(this, mCurrentFile, option, -1, musicFiles);
		switch (option)
		{
			case IMusicConst.OPTION_CONTINUE:
			case IMusicConst.OPTION_PLAY:
				K.playState = IMusicConst.OPTION_PLAY;
				if (pausebtn != null)
					pausebtn.setBackgroundResource(R.drawable.pbbonline_music_pause);
				break;
			case IMusicConst.RELEASE:
			case IMusicConst.OPTION_PAUSE:
				if (pausebtn != null)
					pausebtn.setBackgroundResource(R.drawable.pbbonline_music_play);
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == MUSIC_LIST_CODE && data != null)
		{
			String contentId = data.getStringExtra("contentId");
			musicFiles = data.getParcelableArrayListExtra("musicFiles");
			mCurrentIndex = Util_.getStartIndex(contentId, musicFiles);
			SZLog.v(TAG, "select startPos = " + mCurrentIndex);
			mCurrentFile = musicFiles.get(mCurrentIndex);
			totalCount = musicFiles.size();
			startMusicService(IMusicConst.RELEASE);
			initProgress();
			setMusicName(getMusicName());
			showMusicNotify(getMusicName(), hasPermitted());
			startMusicService(IMusicConst.OPTION_PLAY);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 显示带按钮的通知栏
	 * 
	 * @param hasPermitted
	 */
	private void showMusicNotify(String musicName, boolean hasPermitted)
	{
		notifyPatManager.updateNotification(musicName, "", hasPermitted); //mCurrentFile.getAlbum_pic()
		notifyPatManager.setContentId(mCurrentFile.getContentId());
		// notifyPatManager.setMusicFiles(musicFiles);
	}

	/**
	 * 关闭音乐的notification
	 */
	private void delMusicNotify()
	{
		SZLog.v(TAG, "delMusicNotify");
		notifyPatManager.cancelNotification(getApplicationContext());
	}

	private class MyPhoneStateListener extends PhoneStateListener
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch (state)
			{
				case TelephonyManager.CALL_STATE_RINGING: // 震铃
					switch (K.playState)
					{
						case IMusicConst.OPTION_PLAY:// 当前状态如果是播放的情况 处理暂停
						case IMusicConst.OPTION_CONTINUE:
							startMusicService(IMusicConst.OPTION_PAUSE);
							break;
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					break;
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		finishUI();
	}

	/**
	 * 歌曲名称切换
	 * 
	 * @param event
	 */
	public void onEventMainThread(MusicSwitchNameEvent event)
	{
		if (musicFiles == null)
			return;
		SZLog.e(TAG, "switch music name");
		mCurrentFile = event.getSZFile();
		mCurrentIndex = Util_.getStartIndex(mCurrentFile.getContentId(), musicFiles);

		setMusicName(getMusicName());
		showMusicNotify(getMusicName(), hasPermitted());
	}

	/**
	 * statusbar状态切换.主要是权限切换时。
	 * 
	 * @param event
	 */
	//	public void onEventMainThread(MusicUpdateStatusEvent event)
	//	{
	//		if (musicFiles == null)
	//			return;
	//		SZLog.e(TAG, "update status bar");
	//		mCurrentFile = event.getSZFile();
	//		mCurrentIndex = getStartIndex(mCurrentFile.getContentId(), musicFiles);
	//
	//		//showMusicNotify(mCurrentFile.getName(), mCurrentFile.isPermitted());
	//		if (!isActivityStart)
	//			return;
	//		initProgress();
	//	}

}
