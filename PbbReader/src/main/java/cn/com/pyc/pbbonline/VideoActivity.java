package cn.com.pyc.pbbonline;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.qlk.util.tool.Util.AnimationUtil;
import com.qlk.util.tool.Util.AnimationUtil.Location;
import com.qlk.util.tool.Util.ViewUtil;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.TimeUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.widget.ToastShow;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.adapter.VideoListAdapter;
import cn.com.pyc.pbbonline.bean.SZFile;
import cn.com.pyc.pbbonline.util.SysVolumeLightUtil;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.widget.MediaController;
import cn.com.pyc.pbbonline.widget.MediaHandler;
import cn.com.pyc.pbbonline.widget.VideoView;
import cn.com.pyc.widget.HighlightImageView;
import cn.com.pyc.widget.MarqueeText;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 视频播放
 */
public class VideoActivity extends Activity
{
	private static final String TAG = "VideoUI";
	private static final int CONTROL_DURATION = 4000;
	private static final String TIME_FORMATTER = "HH:mm";
	private boolean needInit = true;
	private MediaController mController;
	private VideoView mVideoView;
	private SZFile curPlayFile; 			//current SZFile
	private List<SZFile> mVideoFiles;		//total SZFile

	private TextView g_txtCount;
	private TextView g_txtTitle;
	private TextView g_txtCurPos;
	private TextView g_txtDuration;
	private SeekBar g_skbProgress;
	private HighlightImageView g_imbInfo;
	private View g_lytControlTop;
	private View g_lytControlBottom;
	private ImageButton g_imbPlayPause;
	private ImageView g_amcPlayPause;
	private View g_lytList;
	private ProgressBar g_pbBuffering;
	private ImageView g_imbBattery;
	private TextView g_txtSystime;
	private View g_sys_controll;
	private ImageView g_imgControll;
	private TextView g_txtControll;

	private PopupWindow pwInfo;
	private VideoListAdapter videoAdapter;
	private BatteryReceiver batteryReceiver;
	private Drawable volumeD, xVolumeD, lightD, quickD, retreatD;

	private MediaHandler handler = new MediaHandler(this)
	{
		@Override
		protected void progressPlay(long progress, long duration)
		{
			// 进度更新
			if (progress < duration)
			{
				g_txtCurPos.setText(FormatterUtil.formatTime(progress));
				g_skbProgress.setProgress((int) progress);
			}
			else
			{
				g_txtCurPos.setText(FormatterUtil.formatTime(duration));
				g_skbProgress.setProgress((int) duration);
			}
		}

		@Override
		protected void preparePlay(SZFile drmFile, long duration)
		{
			// 切换，准备播放
			g_txtTitle.setText(drmFile.getName());
			g_txtDuration.setText(FormatterUtil.formatTime(duration));
			g_skbProgress.setMax((int) duration);
			showCenterImagePlay(false);
		}

		@Override
		protected void pauseOrPlay(boolean statePlay)
		{
			// 暂停或播放
			g_imbPlayPause.setBackgroundResource(statePlay ? R.drawable.pbbonline_video_pause
					: R.drawable.pbbonline_video_play);
			g_amcPlayPause
					.setImageDrawable(getResources().getDrawable(statePlay ? R.drawable.pbbonline_music_pause
							: R.drawable.pbbonline_music_play));
			if (statePlay) //正在播放，中间按钮消失
			{
				showCenterImagePlay(false);
			}
		}

		@Override
		protected void noPermissionPlay(SZFile drmFile)
		{
			// 无权限播放
			g_txtTitle.setText(drmFile.getName());
			g_txtCurPos.setText(FormatterUtil.formatTime(0));
			g_skbProgress.setProgress(0);
			g_txtDuration.setText(FormatterUtil.formatTime(0));
			ToastShow.getToast().showBusy(getApplicationContext(), "没有播放权限~");
			showList(true);
		}

		@Override
		protected void bufferPlay(boolean statePlay)
		{
			// 缓冲
			final Runnable bufferRunnable = new Runnable()
			{
				@Override
				public void run()
				{
					ViewUtil.invisible(g_pbBuffering);
				}
			};
			if (statePlay)
			{
				g_pbBuffering.postDelayed(bufferRunnable, 100);
			}
			else
			{
				ViewUtil.visible(g_pbBuffering);
				g_pbBuffering.removeCallbacks(bufferRunnable);
			}
		}

		@Override
		protected void completePlay(long duration)
		{
			if (mController == null)
				return;
			mController.seek(0);
			mController.pause();
			showCenterImagePlay(true);
			g_txtCurPos.setText(FormatterUtil.formatTime(duration));
			g_skbProgress.setProgress((int) duration);
			SPUtil.remove(curPlayFile.getContentId());
		}
	};

	private class BatteryReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction()))
			{
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);  	// 获取当前电量
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100); 	// 电量的总刻度
				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1); 	// 电池的状态
				SZLog.v(TAG, "current Battery：" + level + ", status: " + status);
				int s = (level * 100) / scale;
				if (status == BatteryManager.BATTERY_STATUS_CHARGING)
				{
					g_imbBattery.setImageResource(R.drawable.battery_charge);
					return;
				}
				setBatteryStatus(s, g_imbBattery);
			}
		}
	}

	// 电话状态监听器
	private static class MyPhoneStateListener extends PhoneStateListener
	{
		private WeakReference<VideoActivity> reference;

		public MyPhoneStateListener(VideoActivity activity)
		{
			reference = new WeakReference<VideoActivity>(activity);
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			super.onCallStateChanged(state, incomingNumber);
			if (null == reference)
				return;
			VideoActivity activity = reference.get();
			if (null == activity)
				return;
			switch (state)
			{
				case TelephonyManager.CALL_STATE_RINGING: // 来电
					activity.mVideoView.pause();
					break;
				case TelephonyManager.CALL_STATE_IDLE: // call state 是空闲状态
					activity.mVideoView.start();
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK: // 电话已经接通
					break;
				default:
					break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String contentId = intent.getStringExtra("contentId");
		SZLog.d(TAG, "current-contentId = " + contentId);
		mVideoFiles = intent.getParcelableArrayListExtra("videoFiles");
		if (mVideoFiles == null || contentId == null)
		{
			ToastShow.getToast().showFail(getApplicationContext(), "文件资源为空(0.0)");
			finish();
			return;
		}
		setContentView(R.layout.pbbonline_activity_video_player);
		initParams();
		findViewAndSetListeners();
		setPlayer(contentId);
		setPlayerGesture();
	}

	private void initParams()
	{
		if (getWindow() != null)
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		// init library loader
		IjkMediaPlayer.loadLibrariesOnce(null);
		IjkMediaPlayer.native_profileBegin("libijkplayer.so");
		volumeD = getResources().getDrawable(R.drawable.ic_volume);
		xVolumeD = getResources().getDrawable(R.drawable.ic_volume_x);
		lightD = getResources().getDrawable(R.drawable.ic_brightness);
		quickD = getResources().getDrawable(R.drawable.ic_quick);
		retreatD = getResources().getDrawable(R.drawable.xml_video_retreat);
	}

	/**
	 * 设置播放器
	 * 
	 * @param contentId
	 *            当前的文件id
	 */
	private void setPlayer(String contentId)
	{
		//计算播放位置。
		int startPos = Util_.getStartIndex(contentId, mVideoFiles);
		SZLog.e(TAG, "start play curPos = " + startPos);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// TODO:检查文件存在本地吗？
		SZFile tempFile = mVideoFiles.get(startPos);
		if (!Util_.checkFileExist(this, tempFile.getFilePath(), new UIHelper.DialogCallBack()
		{
			@Override
			public void onConfirm()
			{
			}
		}))
			return;

		mVideoView.setHandler(handler);
		mController = new MediaController(mVideoView, mVideoFiles);
		mController.start(startPos);
		curPlayFile = mVideoView.getCurrentPlayFile();
		if (null == curPlayFile)
			curPlayFile = tempFile;

		showControl(true);

		// 监听电话状态，来电，暂停视频播放,通话结束，视频恢复播放前状态
		TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		manager.listen(new MyPhoneStateListener(this), PhoneStateListener.LISTEN_CALL_STATE);
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		batteryReceiver = new BatteryReceiver();
		registerReceiver(batteryReceiver, intentFilter);
	}

	private void findViewAndSetListeners()
	{
		// 返回
		findViewById(R.id.amc_imb_back).setOnClickListener(dispatchClickListener);
		findViewById(R.id.amc_iv_back).setOnClickListener(dispatchClickListener);
		g_txtTitle = (TextView) findViewById(R.id.amc_txt_title);
		// 控制按钮
		findViewById(R.id.amc_imb_previous).setOnClickListener(dispatchClickListener);
		g_imbPlayPause = (ImageButton) findViewById(R.id.amc_imb_start_pause);
		g_imbPlayPause.setOnClickListener(dispatchClickListener);
		findViewById(R.id.amc_imb_next).setOnClickListener(dispatchClickListener);
		g_amcPlayPause = (ImageView) findViewById(R.id.amc_img_play_pause);
		g_amcPlayPause.setOnClickListener(dispatchClickListener);

		// 进度时间
		g_txtCurPos = (TextView) findViewById(R.id.amc_txt_current);
		g_txtDuration = (TextView) findViewById(R.id.amc_txt_duration);
		g_skbProgress = ((SeekBar) findViewById(R.id.amc_skb_progress));
		g_skbProgress.setOnSeekBarChangeListener(progressChangedListener);

		// 视频信息
		g_imbInfo = (HighlightImageView) findViewById(R.id.amc_imb_info);
		g_imbInfo.setOnClickListener(dispatchClickListener);
		// 电量，系统时间
		g_imbBattery = (ImageView) findViewById(R.id.amc_imb_battery);
		g_txtSystime = (TextView) findViewById(R.id.amc_txt_systime);
		g_txtSystime.setText(TimeUtil.getDateString(new Date(), TIME_FORMATTER));

		// 列表信息
		findViewById(R.id.amc_imb_list).setOnClickListener(dispatchClickListener);
		g_lytList = findViewById(R.id.amc_lyt_list);
		g_lytList.setOnClickListener(dispatchClickListener);
		// 控制界面
		g_lytControlTop = findViewById(R.id.amc_lyt_top);
		g_lytControlBottom = findViewById(R.id.amc_lyt_bottom);
		g_pbBuffering = (ProgressBar) findViewById(R.id.avp_pb_buffing);
		// 系统亮度和声音控制
		g_sys_controll = findViewById(R.id.avp_sys_vlcontroll);
		g_imgControll = (ImageView) findViewById(R.id.avp_img_controll);
		g_txtControll = (TextView) findViewById(R.id.avp_txt_controll);
		mVideoView = (VideoView) findViewById(R.id.avp_lyt_videoview);
		// 视频文件数量
		g_txtCount = (TextView) findViewById(R.id.amc_text_count);
		if (!mVideoFiles.isEmpty())
		{
			g_txtCount.setVisibility(View.VISIBLE);
			g_txtCount.setText(String.valueOf(mVideoFiles.size()));
		}
	}

	private OnClickListener dispatchClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (CommonUtil.isFastDoubleClick(600))
				return;

			int id = v.getId();
			if (id == R.id.amc_imb_back)
			{
				backActivity();
				ToastShow.getToast().cancel();
			}

			if (mController == null)
				return;

			if (id != R.id.amc_imb_back) // 点击activity_media_control的非amc_imb_back,Control界面存在
			{
				showControl(true);
			}
			if (id != R.id.amc_imb_info) // 点击activity_media_control的非amc_imb_info，视频信息的popWindow消失
			{
				showInfo(false);
			}
			if (id == R.id.amc_imb_info)
			{
				showInfo();
			}
			else if (id == R.id.amc_imb_list)
			{
				showList();
			}
			else if (id == R.id.amc_iv_back)
			{
				showList(false);
			}
			else if (id == R.id.amc_imb_next)
			{
				mController.next();
			}
			else if (id == R.id.amc_imb_start_pause || id == R.id.amc_img_play_pause)
			{
				mController.startOrPause();
				showCenterImagePlay(!mVideoView.isPlaying());
			}
			else if (id == R.id.amc_imb_previous)
			{
				mController.previous();
			}

			if (id != R.id.amc_imb_list && id != R.id.amc_lyt_list)
			{
				// 点击非列表按钮和列表,有权限，列表消失，否则显示
				if (mVideoView.hasPermitt())
					showList(false);
			}
		}
	};

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		backActivity();
	}

	private void backActivity()
	{
		if (mVideoView != null)
			mVideoView.stop();
		showList(false);
		finish();
	}

	private OnSeekBarChangeListener progressChangedListener = new OnSeekBarChangeListener()
	{
		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			if (mController == null)
				return;
			mController.seek(g_skbProgress.getProgress());
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
			g_lytControlTop.removeCallbacks(controlShowRunnable);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			g_txtCurPos.setText(FormatterUtil.formatTime(progress));
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (mController == null)
			return super.onTouchEvent(event);

		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			showInfo(false);
			if (mVideoView.hasPermitt())
				showList(false);
			if (isClickControllView(event))
			{
				showControl(true);
			}
			else
			{
				showControl(!ViewUtil.isShown(g_lytControlTop));
			}
		}
		return super.onTouchEvent(event);
	}

	private boolean isClickControllView(MotionEvent event)
	{
		final int contollHeight = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
		final int y = (int) event.getY();
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
				dm);
		final int screenY = dm.heightPixels;
		return y < contollHeight || (screenY - y) < contollHeight;
	}

	/**
	 * 视频信息
	 */
	public void showInfo()
	{
		if (pwInfo == null)
		{
			showInfo(true);
		}
		else
		{
			showInfo(!pwInfo.isShowing());
		}
	}

	public void showInfo(boolean show)
	{
		if (show)
		{
			// 下一曲时信息会变，每次都要重新加载
			String ext = (curPlayFile != null) ? curPlayFile.getFormat() : Fields.MP4;
			View infoView = getLayoutInflater().inflate(R.layout.pbbonline_dialog_video_info, null);
			((MarqueeText) infoView.findViewById(R.id.dvi_txt_2)).setText(ext);
			((MarqueeText) infoView.findViewById(R.id.dvi_txt_3)).setText(FormatterUtil
					.toCHNTime(mVideoView.getDuration()));
			if (pwInfo == null)
			{
				pwInfo = new PopupWindow(infoView, ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				pwInfo.setAnimationStyle(android.R.style.Animation_Dialog);
			}
			pwInfo.showAtLocation(g_imbInfo, Gravity.BOTTOM | Gravity.RIGHT,
					(g_imbInfo.getTop() + (int) (g_imbInfo.getWidth() * 1.9)),
					(g_imbInfo.getBottom() + 10));
		}
		else
		{
			dismissPopInfo();
		}
	}

	private void dismissPopInfo()
	{
		if (pwInfo != null)
		{
			pwInfo.dismiss();
			pwInfo = null;
		}
	}

	/**
	 * 列表信息
	 */
	public void showList()
	{
		if (!needInit)
		{
			showList(!ViewUtil.isShown(g_lytList));
		}
		else
		{
			showList(true);
		}
	}

	public void showList(boolean show)
	{
		if (null == mVideoFiles)
			return;

		if (show)
		{
			if (needInit)
			{
				ListView lv = (ListView) findViewById(R.id.amc_lv_list);
				videoAdapter = new VideoListAdapter(this, mVideoFiles);
				lv.setAdapter(videoAdapter);
				lv.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						if (mController == null)
							return;

						curPlayFile = videoAdapter.getItem(position);
						if (curPlayFile.isCheckOpen()
								&& !TextUtils.isEmpty(curPlayFile.getCek_cipher_value()))
						{
							// 有权限就消失列表
							showList(false);
						}
						showCenterImagePlay(false);
						showControl(true); // 重新计时
						mController.pause();
						mController.stop();
						mController.start(position);
					}
				});
				lv.setOnScrollListener(new OnScrollListener()
				{
					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState)
					{
						if (scrollState == OnScrollListener.SCROLL_STATE_IDLE)
							showControl(true); // 重新计时
					}

					@Override
					public void onScroll(AbsListView view, int firstVisibleItem,
							int visibleItemCount, int totalItemCount)
					{
					}
				});
				needInit = false;
			}
			videoAdapter.setCurPosition(mVideoFiles.indexOf(curPlayFile));
			if (!ViewUtil.isShown(g_lytList))
			{
				Animation ta = AnimationUtil.translate(g_lytList, false, show, Location.Right);
				Animation aa = AnimationUtil.alpha(g_lytList, false, show);
				AnimationUtil.group(g_lytList, show, ta, aa);
			}
		}
		else
		{
			if (ViewUtil.isShown(g_lytList))
			{
				Animation ta = AnimationUtil.translate(g_lytList, false, show, Location.Right);
				Animation aa = AnimationUtil.alpha(g_lytList, false, show);
				AnimationUtil.group(g_lytList, show, ta, aa);
			}
		}
	}

	private void showControl(boolean show)
	{
		g_lytControlTop.removeCallbacks(controlShowRunnable);
		if (show)
		{
			if (!ViewUtil.isShown(g_lytControlTop))
			{
				AnimationUtil.translate(g_lytControlTop, true, show, Location.Top);
				AnimationUtil.translate(g_lytControlBottom, true, show, Location.Bottom);
			}
			g_lytControlTop.postDelayed(controlShowRunnable, CONTROL_DURATION);
			g_txtSystime.setText(TimeUtil.getDateString(new Date(), TIME_FORMATTER));
		}
		else
		{
			if (ViewUtil.isShown(g_lytControlTop))
			{
				// //这里不能直接gone，否则没有动画ViewUtil.gone(g_lytControlTop);
				// 但如果不Gone，则其所在位置仍有焦点，所以放在AnimationListener中执行Gone操作
				AnimationUtil.translate(g_lytControlTop, true, show, Location.Top);
				AnimationUtil.translate(g_lytControlBottom, true, show, Location.Bottom);
			}
			showInfo(false);
			showList(false);
		}
	}

	private final Runnable controlShowRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			showControl(false);
		}
	};

	/**
	 * 屏幕中间的播放暂停按钮
	 * 
	 * @param show
	 */
	private void showCenterImagePlay(boolean show)
	{
		if (mVideoView == null)
			return;
		if (show)
		{
			g_amcPlayPause
					.setImageDrawable(getResources().getDrawable(mVideoView.isPlaying() ? R.drawable.pbbonline_music_pause
							: R.drawable.pbbonline_music_play));
			AnimationUtil.alpha(g_amcPlayPause, true, true);
		}
		else
		{
			if (ViewUtil.isShown(g_amcPlayPause))
				AnimationUtil.alpha(g_amcPlayPause, true, false);
		}
	}

	/***
	 * 设置电池电量，监听充电状态
	 * 
	 * @param level
	 * @param image
	 */
	private void setBatteryStatus(int level, ImageView image)
	{
		if (level < 10)
		{
			image.setImageResource(R.drawable.battery_0);
		}
		else if (level < 40)
		{
			image.setImageResource(R.drawable.battery_1);
		}
		else if (level < 90)
		{
			image.setImageResource(R.drawable.battery_2);
		}
		else
		{
			image.setImageResource(R.drawable.battery_3);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		if (mVideoView != null)
			mVideoView.pause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		dismissPopInfo();
		unregisterReceiver(batteryReceiver);
		mVideoView.release();
		handler.removeCallbacksAndMessages(null);
		IjkMediaPlayer.native_profileEnd();
	}

	/**
	 * 设置手势事件（处理视频快进后退，音量和亮度大小）
	 */
	private void setPlayerGesture()
	{
		if (getWindow() == null)
			return;
		if (getWindow().getDecorView() == null)
			return;

		final GestureDetector gestureDetector = new GestureDetector(this,
				new PlayerGestureListener());
		View decorView = getWindow().getDecorView();
		decorView.setClickable(true);
		decorView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				g_lytControlTop.removeCallbacks(controlShowRunnable);
				if (gestureDetector.onTouchEvent(event))
					return true;
				// 处理手势结束,此处手势事件只会是up或者down.
				switch (event.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_UP:
						showSysVolumeControll(false);
						break;
				}
				return false;
			}
		});
	}

	/**
	 * 手势控制（音量，亮度，进度）
	 * 
	 * @author hudq
	 */
	public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		private boolean firstTouch;
		private boolean volumeControl;
		private boolean toSeek;

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			if (mVideoView.getHeight() == 0 || mVideoView.getHeight() == 0)
				return super.onScroll(e1, e2, distanceX, distanceY);
			if (!mVideoView.isPlaying())
				return super.onScroll(e1, e2, distanceX, distanceY);

			float mOldX = e1.getX(), mOldY = e1.getY();
			float deltaY = mOldY - e2.getY();
			float deltaX = mOldX - e2.getX();
			if (firstTouch)
			{
				toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
				volumeControl = mOldX > Constant.screenWidth * 0.5f;
				firstTouch = false;
			}
			if (toSeek)
			{
				float percent = -deltaX / mVideoView.getWidth();
				long[] p = SysVolumeLightUtil.getProgressSlide(mVideoView, percent);
				if (mController != null)
				{
					int progress = (int) p[0];
					mController.seek(progress);
					g_txtCurPos.setText(FormatterUtil.formatTime(progress));
					g_skbProgress.setProgress(progress);
				}
				if (p[1] != 0)
				{
					g_imgControll.setImageDrawable(p[1] > 0 ? quickD : retreatD);
					g_txtControll.setText(p[1] > 0 ? "+" + p[1] + "s" : p[1] + "s");
				}
			}
			else
			{
				float percent = deltaY / mVideoView.getHeight();
				if (volumeControl)
				{
					int volume = SysVolumeLightUtil.setVolumeSlide(VideoActivity.this, percent);
					g_imgControll.setImageDrawable(volume > 0 ? volumeD : xVolumeD);
					g_txtControll.setText(volume + "%");
				}
				else
				{
					int brightness = SysVolumeLightUtil.setBrightnessSlide(VideoActivity.this,
							percent);
					g_imgControll.setImageDrawable(lightD);
					g_txtControll.setText(brightness + "%");
				}
			}
			showSysVolumeControll(true);
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onDown(MotionEvent e)
		{
			firstTouch = true;
			return super.onDown(e);
		}
	}

	/**
	 * 显示设置音量、亮度、进度的对话框
	 * 
	 * @param show
	 */
	private void showSysVolumeControll(boolean show)
	{
		if (show)
		{
			if (g_sys_controll.getVisibility() == View.GONE)
			{
				g_sys_controll.setVisibility(View.VISIBLE);
			}
		}
		else
		{
			g_sys_controll.setVisibility(View.GONE);
		}
	}
}
