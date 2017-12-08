package cn.com.pyc.reader.video;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util.AnimationUtil;
import com.qlk.util.tool.Util.AnimationUtil.Location;
import com.qlk.util.tool.Util.FileUtil;
import com.qlk.util.tool.Util.ScreenUtil;
import com.qlk.util.tool.Util.ViewUtil;
import com.qlk.util.tool._SysoXXX;

import java.util.Locale;
import java.util.Observable;
import java.util.Random;

import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pcshare.help.PCFileHelp;
import cn.com.pyc.reader.IPlayerStateListener;
import cn.com.pyc.reader.PlayFile;
import cn.com.pyc.reader.ReaderBaseActivity;
import cn.com.pyc.widget.MySeekBar;
import cn.com.pyc.xcoder.XCoder;

public class VideoPlayerActivity extends ReaderBaseActivity {
    private static final String VIDEO_PROGRESS = "video_progress";

    private final Handler mHandler = new Handler();

    private View g_lytControlLeft;
    private View g_lytControlTop;
    private View g_lytControlRight;
    private View g_lytControlBottom;
    private TextView g_txtCurName;
    private TextView g_txtCurTime;
    private TextView g_txtDuration;
    private TextView g_txtInfo;
    private TextView g_txtWater;
    private ImageButton g_imbPlay;
    private ImageButton g_imbBack;
    private ImageButton g_imbDelete;
    private ImageButton g_imbLockState;
    private ImageButton g_imbFullScreenState;
    private SeekBar g_skbProgress;
    private VideoPlayer mVideoPlayer;
    private GestureDetector mDetector;
    private MyGesture myGesture;

    private int width;
    private int height;
    private int widthRange;
    private int heightRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_reader_video);
        super.onCreate(savedInstanceState);
        findViewAndSetListeners();
        initView();

        //_SysoXXX.message("video activity");
        startPlayer();
        myGesture = new MyGesture();
        mDetector = new GestureDetector(this, myGesture);

        WindowManager wm = this.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();

        widthRange = width - g_txtWater.getMeasuredWidth();
        heightRange = height - g_txtWater.getMeasuredHeight();

        g_txtWater.setX(0);
        g_txtWater.setY(0);

    }

    @Override
    public void findViewAndSetListeners() {
        g_lytControlLeft = findViewById(R.id.arv_lyt_control_left);
        g_lytControlRight = findViewById(R.id.arv_lyt_control_right);
        g_lytControlBottom = findViewById(R.id.arv_lyt_control_bottom);
        g_lytControlTop = findViewById(R.id.arv_lyt_control_top);

        g_txtCurName = (TextView) findViewById(R.id.arv_txt_cur_name);
        g_txtCurTime = (TextView) findViewById(R.id.arv_txt_cur_time);
        g_txtDuration = (TextView) findViewById(R.id.arv_txt_duration);
        g_txtInfo = (TextView) findViewById(R.id.arv_txt_info);
        g_txtWater = (TextView) findViewById(R.id.arv_txt_water);
        g_imbPlay = (ImageButton) findViewById(R.id.arv_imb_video_play);
        g_imbBack = (ImageButton) findViewById(R.id.arv_imb_back);
        g_imbDelete = (ImageButton) findViewById(R.id.arv_imb_delete);
        g_imbLockState = (ImageButton) findViewById(R.id.arv_imb_unlock);
        g_imbFullScreenState = (ImageButton) findViewById(R.id.arv_imb_fullScreen);
        g_skbProgress = (SeekBar) findViewById(R.id.arv_skb_progress);
        g_skbVolume = (MySeekBar) findViewById(R.id.arv_skb_volume);

        try {
            //如果自动切换横竖屏按钮关了，锁按钮隐藏，全屏按钮显示
            if (Settings.System.getInt(getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION) != 1) {
                g_imbLockState.setVisibility(View.GONE);
                g_imbFullScreenState.setVisibility(View.VISIBLE);
            }
        } catch (SettingNotFoundException e) {

            e.printStackTrace();
        }

        g_imbDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mVideoPlayer.pause();
                delete(mCurPath);
                showControl(true);
            }
        });
        findViewById(R.id.arv_imb_decrypt).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mVideoPlayer.pause();
                decrypt(mCurPath);
                showControl(true);
            }
        });
        findViewById(R.id.arv_imb_send).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mVideoPlayer.pause();
                send(mCurPath);
                showControl(true);
            }
        });
        findViewById(R.id.arv_imb_video_last).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playLast();
                showControl(true);
            }
        });
        findViewById(R.id.arv_imb_video_next).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playNext();
                showControl(true);
            }
        });

        g_imbPlay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mVideoPlayer.startOrPause();
                showControl(true);
            }
        });

        g_imbLockState.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                changeLockState();
            }
        });

        g_imbFullScreenState.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ScreenUtil.getScreenHeight(VideoPlayerActivity.this) > ScreenUtil
                        .getScreenWidth(VideoPlayerActivity.this)) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    //					if (VideoPlayerActivity.this.getResources()
                    // .getConfiguration().orientation == 1)//竖屏
                    //					{
                    //						g_imbFullScreenState.setImageResource(R.drawable
                    // .xml_video_fullscreen);
                    //					}
                    //					else
                    //					{
                    //						g_imbFullScreenState.setImageResource(R.drawable
                    // .xml_video_smallscreen);
                    //					}
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    //					if (VideoPlayerActivity.this.getResources()
                    // .getConfiguration().orientation == 1)//竖屏
                    //					{
                    //						g_imbFullScreenState.setImageResource(R.drawable
                    // .xml_video_fullscreen);
                    //					}
                    //					else
                    //					{
                    //						g_imbFullScreenState.setImageResource(R.drawable
                    // .xml_video_smallscreen);
                    //					}
                }
            }
        });

        g_skbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoPlayer.seekTo(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    showControl(true);
                    showInfo(formatTime(progress, seekBar.getMax()) + "/"
                            + formatTime(seekBar.getMax(), seekBar.getMax()));
                }
            }
        });
        g_skbVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    showControl(true);
                    getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }
        });
        g_skbLight = ((SeekBar) findViewById(R.id.arv_skb_light));
        g_skbLight.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    showControl(true);
                    LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = 1.0f * progress / MAX_LIGHT;
                    getWindow().setAttributes(params);
                }
            }
        });

    }

    private void initView() {
        if (isCipher) {
            findViewById(R.id.arv_imb_decrypt).setVisibility(View.VISIBLE);
            g_imbDelete.setVisibility(View.VISIBLE);
            findViewById(R.id.arv_imb_send).setVisibility(View.VISIBLE);
        }

        if (mPaths.size() > 1) {
            findViewById(R.id.arv_imb_video_last).setVisibility(View.VISIBLE);
            findViewById(R.id.arv_imb_video_next).setVisibility(View.VISIBLE);
        }

        mVideoPlayer = (VideoPlayer) findViewById(R.id.arv_sfv_video);
        mVideoPlayer.setListener(mStateListener);

        initLightBar();
        initVolumeBar();

    }

	/*-*************************************
     * TODO 播放控制
	 **************************************/

    private Runnable mProgressRunnable = new Runnable() {

        @Override
        public void run() {
            if(mVideoPlayer == null) return;
            int cur = mVideoPlayer.getCurPos();
            int max = mVideoPlayer.getDuration();
            g_skbProgress.setMax(max);
            g_skbProgress.setProgress(cur);
            g_txtCurTime.setText(formatTime(cur, max));
            g_txtDuration.setText(formatTime(max, max));

            mHandler.postDelayed(mProgressRunnable, 1000);
        }
    };

    //	long secends = 0;
    //	boolean b = true;
    //
    //	Handler handle = new Handler();
    //	Runnable runnable = new Runnable()
    //	{
    //
    //		@Override
    //		public void run()
    //		{
    //			if (b)
    //			{
    //				secends = 20000;
    //				handle.postDelayed(runnable, secends);
    //				g_txtWater.setVisibility(View.INVISIBLE);
    //				b = false;
    //			}
    //			else
    //			{
    //				secends = 4000;
    //				handle.postDelayed(runnable, secends);
    //				g_txtWater.setVisibility(View.VISIBLE);
    //				b = true;
    //			}
    //		}
    //	};

    private WaterHander waterHander;
    private int waterWidth, waterHeight;
    private final Rect mSurfaceRect = new Rect();
    private final Rect mWaterRect = new Rect();

    class WaterHander extends Handler {
        //变量1记录postDelay使的时刻
        //
        private long delayTime = 0;
        private long duration = 0;

        public void reset() {
            removeMessages(0);
            delayTime = System.currentTimeMillis();    //记录状态更新时的时间
            duration = 24000;
            sendEmptyMessageDelayed(0, duration);
        }

        public void changeState(boolean playing) {
            removeMessages(0);
            if (playing) {
                //开始
                sendEmptyMessageDelayed(0, duration);
                delayTime = System.currentTimeMillis();
            } else {
                //中断
                duration -= (System.currentTimeMillis() - delayTime);
            }

        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            try {
                delayTime = System.currentTimeMillis();    //记录状态更新时的时间
                if (g_txtWater.getVisibility() == View.VISIBLE) {
                    //隐藏
                    g_txtWater.setVisibility(View.INVISIBLE);
                    duration = 20000;
                    sendEmptyMessageDelayed(0, duration);
                } else {
                    //显示
                    g_txtWater.setVisibility(View.VISIBLE);

                    waterWidth = g_txtWater.getWidth();
                    waterHeight = g_txtWater.getHeight();
                    if (waterWidth == 0 || waterHeight == 0) {
                        g_txtWater.measure(0, 0);
                        waterWidth = g_txtWater.getMeasuredWidth();
                        waterHeight = g_txtWater.getMeasuredHeight();
                    }

                    if (waterHeight > 0 && waterWidth > 0) {
                        Random random = new Random();
                        //这里有可能是负数，引起Crash
                        int x = width - waterWidth;
                        if (x > 0) {
                            g_txtWater.setX(random.nextInt(x));
                        }
                        int y = height - waterHeight;
                        if (y > 0) {
                            g_txtWater.setY(random.nextInt(y));
                        }
                    }

                    duration = 4000;
                    sendEmptyMessageDelayed(0, duration);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private IPlayerStateListener mStateListener = new IPlayerStateListener() {

        @Override
        public void onStateChanged(boolean isPlaying) {
            if (waterHander != null) {
                waterHander.changeState(isPlaying);
            }
            if (isPlaying) {
                mHandler.post(mProgressRunnable);    //手动更新时间
                g_imbPlay.setBackgroundResource(R.drawable.video_start);
            } else {
                mHandler.removeCallbacks(mProgressRunnable);
                g_imbPlay.setBackgroundResource(R.drawable.video_pause);
            }
        }

        @Override
        public void onProgressChanged(int progress, int duration) {

        }

        @Override
        public void onError(int what) {
            GlobalToast.toastShort(VideoPlayerActivity.this,
                    String.format(Locale.CHINA, "文件播放失败（errorCode=%d）", what));
            mVideoPlayer.release();
        }

        @Override
        public void onComplete() {
            SharedPreferences sp = getSharedPreferences(VIDEO_PROGRESS, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.remove(mCurPath);    // 记忆清零
            edit.commit();

            if (isFromSm) {
                GlobalToast.toastShort(VideoPlayerActivity.this, "视频已播放完毕");
                mVideoPlayer.pause();
                showControl(true);
            } else {
                playNext();
            }
        }
    };

    private void startPlayer() {
        PlayFile playFile = null;
        if (isFromSm && smInfo != null) {
            //			String path = Environment.getExternalStorageDirectory()+File
            // .separator+"123.mp4.pbb";
            //			try
            //			{
            //				FileOutputStream fileOutputStream = new FileOutputStream(path);
            //				FileInputStream fileInputStream = new FileInputStream(mCurPath);
            //				fileInputStream.skip(smInfo.getOffset());
            //				byte[] data = new byte[2<<20];
            //				int read = 0;
            //				while((read = fileInputStream.read(data))!=-1)
            //				{
            //					fileOutputStream.write(data,0,read);
            //				}
            //				mCurPath = path;
            //				fileInputStream.close();
            //				fileOutputStream.close();
            //			}
            //			catch (Exception e)
            //			{
            //				// TODO Auto-generated catch block
            //				e.printStackTrace();
            //			}

            playFile = new PlayFile(mCurPath, XCoder.wrapEncodeKey(smInfo.getEncodeKey()),
                    smInfo.getCodeLen());
            playFile.setOffset(smInfo.getOffset());
            playFile.setFileLen(smInfo.getFileLen());
            showLimitView((TextView) findViewById(R.id.arv_txt_countdown));
            showWaterView(g_txtWater);
            if (g_txtWater.getVisibility() == View.VISIBLE) {
                //开启水印
                if (waterHander == null) {
                    waterHander = new WaterHander();
                }
                waterHander.reset();
            }
            _SysoXXX.message("codeLen;" + smInfo.getCodeLen());
            _SysoXXX.array(XCoder.wrapEncodeKey(smInfo.getEncodeKey()), "");
        } else if (isCipher) {
            playFile = new XCoder(this).getPlayFileInfo(mCurPath);
        } else {
            playFile = new PlayFile(mCurPath);
        }
        playFile.setMemoryPos(
                getSharedPreferences(VIDEO_PROGRESS, Context.MODE_PRIVATE).getLong(mCurPath, 0));
        mVideoPlayer.play(playFile);

        showControl(true);

        String fileName = FileUtil.getFileName(mCurPath);
        if (isFromSm) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        g_txtCurName.setText(fileName);
    }

    private void playLast() {
        int index = mPaths.indexOf(mCurPath);
        index--;
        if (index == -1) {
            GlobalToast.toastShort(this, "没有可播放的视频了");
            return;
        }
        mCurPath = mPaths.get(index);
        startPlayer();
    }

    private void playNext() {
        int index = mPaths.indexOf(mCurPath);
        index++;
        if (index >= mPaths.size()) {
            GlobalToast.toastShort(this, "已是最后一个视频");
            return;
        }
        mCurPath = mPaths.get(index);
        startPlayer();
    }

    @SuppressWarnings("unused")
    private void fastSeek(boolean isForward) {
        int curPos = mVideoPlayer.getCurPos();
        int duration = mVideoPlayer.getDuration();
        if (isForward) {
            curPos = curPos + 5000 <= duration ? curPos + 5000 : duration;
        } else {
            curPos = curPos - 5000 >= 0 ? curPos - 5000 : 0;
        }

        mVideoPlayer.seekTo(curPos);
        showInfo(formatTime(curPos, duration) + "/" + formatTime(duration, duration));
    }

    private final Handler handler = new Handler();
    private boolean isControlShown = false;
    private boolean isInLockState = false;

    private void changeLockState() {
        isInLockState = !isInLockState;
        showControl(true);
        if (isInLockState) {
            if (ScreenUtil.getScreenHeight(this) > ScreenUtil.getScreenWidth(this)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    /**
     * @param show 当处于锁状态时（isInLockState==true)，show是无效参数
     */
    private void showControl(boolean show) {
        isControlShown = show;
        handler.removeCallbacks(dismissRunnable);
        if (isInLockState) {
            ViewUtil.invisible(g_imbBack);
            g_imbDelete.setVisibility(isCipher ? View.INVISIBLE : View.GONE);
            g_imbLockState.setImageResource(R.drawable.xml_video_lock);

            if (show) {
                if (!ViewUtil.isShown(g_lytControlTop)) {
                    AnimationUtil.translate(g_lytControlTop, true, show, Location.Top);
                }
                handler.postDelayed(dismissRunnable, 3000);
            } else {
                if (ViewUtil.isShown(g_lytControlTop)) {
                    AnimationUtil.translate(g_lytControlTop, true, show, Location.Top);
                }
            }

            if (ViewUtil.isShown(g_lytControlLeft)) {
                AnimationUtil.translate(g_lytControlLeft, true, false, Location.Left);
            }
            if (ViewUtil.isShown(g_lytControlRight)) {
                AnimationUtil.translate(g_lytControlRight, true, false, Location.Right);
            }
            if (ViewUtil.isShown(g_lytControlBottom)) {
                AnimationUtil.translate(g_lytControlBottom, true, false, Location.Bottom);
            }
        } else {
            ViewUtil.visible(g_imbBack);
            g_imbDelete.setVisibility(isCipher ? View.VISIBLE : View.GONE);
            g_imbLockState.setImageResource(R.drawable.xml_video_unlock);
            if (show) {
                if (!ViewUtil.isShown(g_lytControlLeft)) {
                    AnimationUtil.translate(g_lytControlLeft, true, show, Location.Left);
                }
                if (!ViewUtil.isShown(g_lytControlTop)) {
                    AnimationUtil.translate(g_lytControlTop, true, show, Location.Top);
                }
                if (!ViewUtil.isShown(g_lytControlRight)) {
                    AnimationUtil.translate(g_lytControlRight, true, show, Location.Right);
                }
                if (!ViewUtil.isShown(g_lytControlBottom)) {
                    AnimationUtil.translate(g_lytControlBottom, true, show, Location.Bottom);
                }
                handler.postDelayed(dismissRunnable, 3000);
            } else {
                if (ViewUtil.isShown(g_lytControlLeft)) {
                    AnimationUtil.translate(g_lytControlLeft, true, show, Location.Left);
                }
                if (ViewUtil.isShown(g_lytControlTop)) {
                    AnimationUtil.translate(g_lytControlTop, true, show, Location.Top);
                }
                if (ViewUtil.isShown(g_lytControlRight)) {
                    AnimationUtil.translate(g_lytControlRight, true, show, Location.Right);
                }
                if (ViewUtil.isShown(g_lytControlBottom)) {
                    AnimationUtil.translate(g_lytControlBottom, true, show, Location.Bottom);
                }
            }
        }

    }

    private Runnable dismissRunnable = new Runnable() {
        @Override
        public void run() {
            showControl(false);
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == 1) {//竖屏
            g_imbFullScreenState.setImageResource(R.drawable.xml_video_fullscreen);
        } else {
            g_imbFullScreenState.setImageResource(R.drawable.xml_video_smallscreen);
        }
        mVideoPlayer.onConfigurationChanged();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showControl(true);
    }

    protected String formatTime(int progress, int max) {
        progress /= 1000;
        int minute = progress / 60;
        int hour = minute / 60;
        int second = progress % 60;
        minute %= 60;
        if (max / 3600000 == 0) {
            return String.format(Locale.CHINESE, "%02d:%02d", minute, second);
        } else {
            return String.format(Locale.CHINESE, "%02d:%02d:%02d", hour, minute, second);
        }
    }

    private void showInfo(String info) {
        handler.removeCallbacks(infoDismissRunnable);
        g_txtInfo.setVisibility(View.VISIBLE);
        g_txtInfo.setText(info);
        handler.postDelayed(infoDismissRunnable, 1000);
    }

    private Runnable infoDismissRunnable = new Runnable() {

        @Override
        public void run() {
            g_txtInfo.setVisibility(View.GONE);
        }
    };

	/*-*************************************
     * TODO 亮度调节
	 **************************************/

    private static final int MAX_LIGHT = 255;    // // 屏幕亮度0～255

    private static final int PER_LIGHT = 50;

    private SeekBar g_skbLight;

    private void initLightBar() {
        SeekBar light = (SeekBar) findViewById(R.id.arv_skb_light);
        light.setMax(MAX_LIGHT);
        light.setProgress(getScreenBrightness());
    }

    private int getScreenBrightness() {
        int nowBrightnessValue = 0;
        ContentResolver resolver = getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    @SuppressWarnings("unused")
    private void changeLight(boolean up) {
        int curBrightness = g_skbLight.getProgress();
        if (up) {
            curBrightness = curBrightness + PER_LIGHT < MAX_LIGHT ? curBrightness + PER_LIGHT
                    : MAX_LIGHT;
        } else {
            curBrightness = curBrightness - PER_LIGHT > 0 ? curBrightness - PER_LIGHT : 0;
        }
        LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 1.0f * curBrightness / MAX_LIGHT;
        getWindow().setAttributes(params);
        g_skbLight.setProgress(curBrightness);

        int percent = curBrightness * 100 / MAX_LIGHT;
        showInfo("亮度：" + percent + "%");
    }

    /**
     * 因为亮度的Max值比较大，就不用offset了
     *
     * @param perOffset
     */
    private void changeLight(float perOffset) {
        int curBrightness = g_skbLight.getProgress();
        curBrightness += (int) (perOffset * MAX_LIGHT);    // 记得强转，留意括号的位置
        if (curBrightness > MAX_LIGHT) {
            curBrightness = MAX_LIGHT;
        }
        if (curBrightness < 0) {
            curBrightness = 0;
        }
        LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 1.0f * curBrightness / MAX_LIGHT;
        getWindow().setAttributes(params);
        g_skbLight.setProgress(curBrightness);

        int percent = curBrightness * 100 / MAX_LIGHT;
        showInfo("亮度：" + percent + "%");
    }

	/*-*************************************
     * TODO 音量调节
	 **************************************/

    private AudioManager am;
    private MySeekBar g_skbVolume;

    private AudioManager getAudioManager() {
        if (am == null) {
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return am;
    }

    private void initVolumeBar() {
        g_skbVolume.setMax(getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        g_skbVolume.setProgress(getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    private void changeVolume(boolean up) {
        int curVolume = g_skbVolume.getProgress();
        if (up) {
            int max = g_skbVolume.getMax();
            curVolume = curVolume + 1 < max ? curVolume + 1 : max;
        } else {
            curVolume = curVolume - 1 > 0 ? curVolume - 1 : 0;
        }
        getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
        g_skbVolume.setProgress(curVolume);

        int percent = 100 * curVolume / g_skbVolume.getMax();
        showInfo("音量：" + percent + "%");
    }

    /**
     * 此值在滑动结束后就应该置0
     */
    private float volOffset;        // 因为float转成int后会舍掉小数位，造成数据不准确，设此值来保存小数位

    /**
     * @param perOffset 负值减小，正值增大
     */
    private void changeVolume(float perOffset) {
        int curVolume = g_skbVolume.getProgress();
        final int maxVolume = g_skbVolume.getMax();
        volOffset += perOffset * maxVolume;
        curVolume += (int) volOffset;    // 加上累计的和（一定要强转舍弃小数位，当perOffset为负的时候会不正常）
        volOffset %= 1;        // 加上之后就要去掉
        if (curVolume > maxVolume) {
            curVolume = maxVolume;
        }
        if (curVolume < 0) {
            curVolume = 0;
        }
        getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
        g_skbVolume.setProgress(curVolume);

        int percent = 100 * curVolume / g_skbVolume.getMax();
        showInfo("音量：" + percent + "%");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            changeVolume(true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            changeVolume(false);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isInLockState) {
                GlobalToast.toastShort(this, "先解锁，再退出");
                showControl(true);
                return true;
            }
            //TODO:删除播放的PC共享临时文件
            deletePCShareTempFile();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void deletePCShareTempFile() {
        if (mCurPath.contains(PCFileHelp.getPCShareOffset())) {
            String fileName = com.sz.mobilesdk.util.FileUtil.getNameFromFilePath(mCurPath);
            PCFileHelp.deleteFile(fileName);
        }
    }

    @Override
    public void onBackButtonClick(View v) {
        super.onBackButtonClick(v);
        deletePCShareTempFile();
    }

    @Override
    protected void afterDeXXX() {
        finish();
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
        if (data.equals(ObTag.PhoneOn) || data.equals(ObTag.ScreenLockOff)) {
            mVideoPlayer.pause();
        }
    }

    /* onStop()的时候sfv就destory了 */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sp = getSharedPreferences(VIDEO_PROGRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putLong(mCurPath, mVideoPlayer.getCurPos());
        edit.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPlayer.release();
        if (waterHander != null) {
            waterHander.removeMessages(0);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

	/*-************************************
     * TODO 播放控制
	 *************************************/

    private double dxy;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            try {
                final float x0 = event.getX(event.getPointerId(0));
                final float y0 = event.getY(event.getPointerId(0));

                final float x1 = event.getX(event.getPointerId(1));
                final float y1 = event.getY(event.getPointerId(1));

                switch (event.getAction()) {
                    case 261:
                        dxy = Math.hypot(x0 - x1, y0 - y1);
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        dxy = Math.hypot(x0 - x1, y0 - y1);
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        setFullScreen(dxy < Math.hypot(x0 - x1, y0 - y1));
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.onTouchEvent(event);
        } else {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                myGesture.onUp(event);
            }
            return mDetector.onTouchEvent(event);
        }
    }

    private void setFullScreen(boolean full) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (full) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        getWindow().setAttributes(attrs);
    }

    class MyGesture extends SimpleOnGestureListener {
        private int curPos;
        private int duration;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mVideoPlayer.startOrPause();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            duration = 0;
            curPos = 0;
            volOffset = 0;
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // 横竖屏会引起X、Y变化，所以每次都获取
            int screenX = ScreenUtil.getScreenWidth(VideoPlayerActivity.this);
            int screenY = ScreenUtil.getScreenHeight(VideoPlayerActivity.this);
            int left = g_lytControlLeft.getWidth();    // onResume之后才有值
            int top = g_lytControlTop.getHeight();
            int right = screenX - g_lytControlRight.getWidth();
            int bottom = screenY - g_lytControlBottom.getHeight();

            int x = (int) e.getX();
            int y = (int) e.getY();
            if (x > left && x < right && y > top && y < bottom) {
                showControl(!isControlShown);
            } else {
                showControl(true);
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // if (!videoPlayer.isPlaying())
            // {
            // return super.onFling(e1, e2, velocityX, velocityY);
            // }
            //
            // /*
            // * velocity������/���»���Ϊ��������/���ϻ���Ϊ��
            // */
            // // ���ȵ��ڣ����ٻ�����Ч��Ŀǰ�趨>2000��
            // if (Math.abs(velocityX) > 2 * Math.abs(velocityY)
            // && Math.abs(velocityX) > 2000)
            // {
            // fastSeek(velocityX > 0);
            // }
            //
            // if (Math.abs(velocityY) > 2 * Math.abs(velocityX))
            // {
            // int screenX = ScreenUtil
            // .getScreenWidth(VideoPlayerActivity.this);
            //
            // if (e1.getX() < screenX / 2)
            // {
            // // ���ȵ���
            // changeLight(velocityY < 0);
            // }
            // else
            // {
            // // ��������
            // changeVolume(velocityY < 0);
            // }
            // }

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        private static final float PER_WIDTH = 0.15f;    // 声音和亮度占屏比

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int screenX = ScreenUtil.getScreenWidth(VideoPlayerActivity.this);
            int screenY = ScreenUtil.getScreenHeight(VideoPlayerActivity.this);
            if (e1.getX() < screenX * PER_WIDTH) {
                changeLight(distanceY / screenY);
            } else if (e1.getX() > screenX * (1 - PER_WIDTH)) {
                changeVolume(distanceY / screenY);
            } else {
                if (duration == 0) {
                    curPos = mVideoPlayer.getCurPos();
                    duration = mVideoPlayer.getDuration();
                }
                float perOffset = -distanceX / screenX;
                perOffset /= 6;
                curPos += perOffset * duration;
                if (curPos > duration) {
                    curPos = duration;
                }
                if (curPos < 0) {
                    curPos = 0;
                }

                showInfo(formatTime(curPos, duration) + "/" + formatTime(duration, duration));
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        public void onUp(MotionEvent e) {
            if (duration != 0) {
                mVideoPlayer.seekTo(curPos);
            }
        }
    }

}
