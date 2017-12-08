package cn.com.pyc.suizhi;

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
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qlk.util.tool.Util;
import com.sz.help.KeyHelp;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.FormatterUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.TimeUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.widget.ToastShow;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.SysVolumeLightUtil;
import cn.com.pyc.suizhi.adapter.VideoItemAdapter;
import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.help.LoadDataHelp;
import cn.com.pyc.suizhi.help.MusicHelp;
import cn.com.pyc.suizhi.model.FileData;
import cn.com.pyc.suizhi.widget.video.SZMediaController;
import cn.com.pyc.suizhi.widget.video.SZMediaHandler;
import cn.com.pyc.suizhi.widget.video.SZVideoView;
import cn.com.pyc.utils.ViewUtil;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by hudaqiang on 2017/8/24.
 */

public class SZVideoPlayerActivity extends ExtraBaseActivity {

    private static final String TAG = SZVideoPlayerActivity.class.getSimpleName();
    private static final int CONTROL_DURATION = 6000;
    private static final String TIME_FORMATTER = "HH:mm";
    private Drawable playDrawable, pauseDrawable;
    private SZMediaController mController;
    /**
     * DRMFile
     */
    private volatile DrmFile curPlay;
    private List<DrmFile> mDrmFiles;
    //private PopupWindow pwInfo;

    private SZVideoView mVideoView;
    private TextView g_txtCount;
    private TextView g_txtTitle;
    private TextView g_txtCurPos;
    private TextView g_txtDuration;
    //private View g_floatView;
    //private TextView g_txtFloatView;
    //private HighlightImageView g_imbInfo;
    private SeekBar g_skbProgress;
    private View g_lytControlTop;
    private View g_lytControlBottom;
    private ImageView g_imbPlayPause;
    private View g_lytList;
    private ProgressBar g_pbBuffering;
    private ImageView g_imbBattery;
    private TextView g_txtSystime;
    private View g_sysControll;
    private ImageView g_imgControll;
    private TextView g_txtControll;

    private String curFileId;
    private String myProductId;
    private List<FileData> dataList;
    private boolean needInit = true;
    private VideoItemAdapter videoAdapter;
    private BatteryReceiver batteryReceiver;
    private Drawable volumeD, xVolumeD, lightD, quickD, retreatD;


    private SZMediaHandler handler = new SZMediaHandler(this) {
        @Override
        protected void progressPlay(long progress, long duration) {
            // 进度更新
            if (progress < duration) {
                g_txtCurPos.setText(FormatterUtil.formatTime(progress));
                g_skbProgress.setProgress((int) progress);
            } else {
                g_txtCurPos.setText(FormatterUtil.formatTime(duration));
                g_skbProgress.setProgress((int) duration);
            }
        }

        @Override
        protected void preparePlay(DrmFile drmFile, long duration) {
            // 切换，准备播放
            g_txtTitle.setText(drmFile.getFileName());
            g_txtDuration.setText(FormatterUtil.formatTime(duration));
            g_skbProgress.setMax((int) duration);
        }

        @Override
        protected void pauseOrPlay(boolean statePlay) {
            // 暂停或播放
            g_imbPlayPause.setImageDrawable(statePlay ? pauseDrawable
                    : playDrawable);
        }

        @Override
        protected void noPermissionPlay(DrmFile drmFile) {
            // 无权限播放
            g_txtTitle.setText(drmFile.getFileName());
            g_txtCurPos.setText(FormatterUtil.formatTime(0));
            g_txtDuration.setText(FormatterUtil.formatTime(0));
            g_skbProgress.setProgress(0);

            showList(true);
            ToastShow.getToast().showBusy(getApplicationContext(), "没有播放权限~");
            if (mController != null) {
                mController.stop();
                mVideoView.reset();
            }
        }

        @Override
        protected void bufferPlay(boolean statePlay) {
            // 缓冲
            final Runnable bufferRunnable = new Runnable() {
                @Override
                public void run() {
                    ViewUtil.hideWidget(g_pbBuffering);
                }
            };
            if (statePlay) {
                g_pbBuffering.postDelayed(bufferRunnable, 100);
                ViewUtil.hideWidget(g_pbBuffering);
            } else {
                ViewUtil.showWidget(g_pbBuffering);
                g_pbBuffering.removeCallbacks(bufferRunnable);
            }
        }

        @Override
        protected void completePlay(long duration) {
            if (mController == null || curPlay == null) return;
            mController.seek(0);
            mController.pause();
            g_txtCurPos.setText(FormatterUtil.formatTime(duration));
            g_skbProgress.setProgress((int) duration);
        }
    };

    // 监听电话状态，来电，暂停视频播放,通话结束，视频恢复播放前状态
    private static class MyPhoneStateListener extends PhoneStateListener {
        private WeakReference<SZVideoPlayerActivity> reference;

        private MyPhoneStateListener(SZVideoPlayerActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (null == reference) return;
            SZVideoPlayerActivity activity = reference.get();
            if (null == activity) return;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: // 来电
                case TelephonyManager.CALL_STATE_OFFHOOK: // 电话已经接通
                    if (activity.mController != null) {
                        activity.mController.pause();
                    }
                    //activity.mVideoView.pause();
                    break;
                case TelephonyManager.CALL_STATE_IDLE: // call state 是空闲状态
                    if (activity.mController != null) {
                        activity.mController.start();
                    }
                    //activity.mVideoView.start();
                    break;
                default:
                    break;
            }
        }
    }

    private class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0); // 获取当前电量
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100); // 电量的总刻度
                int status = intent
                        .getIntExtra(BatteryManager.EXTRA_STATUS, -1); // 电池的状态
                SZLog.v(TAG, "current Battery：" + level + ", status: " + status);
                int s = (level * 100) / scale;
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    g_imbBattery.setImageResource(R.drawable.battery_charge);
                    return;
                }
                setBatteryStatus(s, g_imbBattery);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConfig();
        getValue();
        initView();
        loadData();
    }

    protected void initView() {
        setContentView(R.layout.activity_video_player);
        findViewAndSetListeners();
        setPlayerGesture();
    }

    protected void loadData() {
        ViewUtil.showWidget(g_pbBuffering);
        UIHelper.setEnableUI(this, false);
        LoadDataHelp help = new LoadDataHelp.Builder()
                .setMyProductId(myProductId)
                .setCurrentFileId(curFileId)
                .setDateList(dataList)
                .init();
        help.load(new LoadDataHelp.OnLoadDataListener() {
            @Override
            public void onLoadSuccess(List<DrmFile> drmFiles, int currentPosition) {
                UIHelper.setEnableUI(SZVideoPlayerActivity.this, true);
                mDrmFiles = drmFiles;
                setPlayer(currentPosition);
            }
        });
    }

    protected void getValue() {
        Intent intent = getIntent();
        curFileId = intent.getStringExtra(KeyHelp.KEY_FILE_ID);
        myProductId = intent.getStringExtra(KeyHelp.KEY_MYPRO_ID);
        dataList = intent.getParcelableArrayListExtra(KeyHelp.KEY_FILE_LIST); //列表数据
        Log.e(TAG, "sz video play...");
    }

    private void initConfig() {
        if (getWindow() != null) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        // init library loader
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        playDrawable = getResources().getDrawable(R.drawable.ic_play);
        pauseDrawable = getResources().getDrawable(R.drawable.ic_pause);
        volumeD = getResources().getDrawable(R.drawable.ic_volume);
        xVolumeD = getResources().getDrawable(R.drawable.ic_volume_x);
        lightD = getResources().getDrawable(R.drawable.ic_brightness);
        quickD = getResources().getDrawable(R.drawable.ic_quick);
        retreatD = getResources().getDrawable(R.drawable.xml_video_retreat);
    }

    /**
     * 设置播放器
     *
     * @param mCurrentPos
     */
    private void setPlayer(int mCurrentPos) {
        MusicHelp.release(this);
        // 去分享此刻开始播放（存在的话）
//        String sharePosition = ShareMomentEngine.getSharePosition(DrmPat.VIDEO);
//        if (!TextUtils.isEmpty(sharePosition)) {
//            //保存进度，播放时会读取
//            ProgressHelp.saveProgress(curFileId, ConvertToUtil.toLong(sharePosition));
//        }
        if (!mDrmFiles.isEmpty()) {
            g_txtCount.setVisibility(View.VISIBLE);
            g_txtCount.setText(String.valueOf(mDrmFiles.size()));
        }
        SZLog.d(TAG, "play video index at " + (mCurrentPos + 1));
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // 1.设置播放状态回调
        mVideoView.setHandler(handler);
        // 2.初始化播放控件
        mController = new SZMediaController(mVideoView, mDrmFiles);
        mController.start(mCurrentPos);

        curPlay = mVideoView.getCurrentPlayFile();
        if (null == curPlay) curPlay = mDrmFiles.get(mCurrentPos);

        showControl(true);
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(new MyPhoneStateListener(this),
                PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, intentFilter);
    }

    protected void findViewAndSetListeners() {
        // 返回
        findViewById(R.id.amc_imb_back).setOnClickListener(dispatchClickListener);
        findViewById(R.id.amc_iv_back).setOnClickListener(dispatchClickListener);
        mVideoView = (SZVideoView) findViewById(R.id.avp_lyt_videoview);
        // 标题
        g_txtTitle = (TextView) findViewById(R.id.amc_txt_title);
        // 控制按钮
        findViewById(R.id.amc_imb_previous).setOnClickListener(dispatchClickListener);
        findViewById(R.id.amc_imb_next).setOnClickListener(dispatchClickListener);
        g_imbPlayPause = (ImageView) findViewById(R.id.amc_imb_start_pause);
        g_imbPlayPause.setOnClickListener(dispatchClickListener);
        // 时间
        g_txtCurPos = (TextView) findViewById(R.id.amc_txt_current);
        g_txtDuration = (TextView) findViewById(R.id.amc_txt_duration);
        //浮窗
//        g_floatView = findViewById(R.id.share_layout);
//        g_txtFloatView = (TextView) findViewById(R.id.share_text_pos);
//        findViewById(R.id.share_image_btn).setOnClickListener(floatViewOnClickListener);
        // 进度条
        g_skbProgress = ((SeekBar) findViewById(R.id.amc_skb_progress));
        g_skbProgress.setOnSeekBarChangeListener(progressChangedListener);
        // 视频信息
        //g_imbInfo = (HighlightImageView) findViewById(R.id.amc_imb_info);
        //g_imbInfo.setOnClickListener(dispatchClickListener);
        // 列表文件数
        g_txtCount = (TextView) findViewById(R.id.amc_text_count);
        // 列表信息
        findViewById(R.id.amc_imb_list).setOnClickListener(dispatchClickListener);
        g_lytList = findViewById(R.id.amc_lyt_list);
        g_lytList.setOnClickListener(dispatchClickListener);
        // 控制界面
        // g_lytControl = findViewById(R.id.avp_lyt_control);
        g_lytControlTop = findViewById(R.id.amc_lyt_top);
        g_lytControlBottom = findViewById(R.id.amc_lyt_bottom);
        // 缓冲
        g_pbBuffering = (ProgressBar) findViewById(R.id.avp_pb_buffing);
        // 电量
        g_imbBattery = (ImageView) findViewById(R.id.amc_imb_battery);
        // 系统时间
        g_txtSystime = (TextView) findViewById(R.id.amc_txt_systime);
        g_txtSystime.setText(TimeUtil.getDateString(new Date(), TIME_FORMATTER));
        // 系统亮度和声音控制
        g_sysControll = findViewById(R.id.avp_sys_vlcontroll);
        g_imgControll = (ImageView) findViewById(R.id.avp_img_controll);
        g_txtControll = (TextView) findViewById(R.id.avp_txt_controll);

    }

    private View.OnClickListener dispatchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (CommonUtil.isFastDoubleClick(600)) return;
            int id = v.getId();
            if (id != R.id.amc_imb_back) // 点击activity_media_control的非amc_imb_back， Control界面存在
            {
                showControl(true);
            }
//            if (id != R.id.amc_imb_info) //
// 点击activity_media_control的非amc_imb_info，视频信息的popWindow消失
//            {
//                showInfo(false);
//            }
            switch (id) {
                case R.id.amc_imb_back: // 返回 视频退出
                {
                    if (mController != null)
                        mController.stop();
                    showList(false);
                    finish();
                }
                break;
//                case R.id.amc_imb_info: // 显示视频信息
//                    showInfo();
//                    break;
                case R.id.amc_imb_list: // 显示视频列表
                    showList();
                    break;
                case R.id.amc_iv_back: // 视频列表信息消失
                    showList(false);
                    break;
                case R.id.amc_imb_next: {
                    if (mController != null) {
                        mController.next();
                        curPlay = mController.getCurrentFile();
                    }
                }
                break;
                case R.id.amc_imb_start_pause: {
                    if (curPlay != null && !curPlay.isCheckOpen()) {
                        ToastShow.getToast().showBusy(SZVideoPlayerActivity.this,
                                "没有权限播放《" + curPlay.getFileName() + "》");
                        return;
                    }
                    if (mController != null)
                        mController.startOrPause();
                }
                break;
                case R.id.amc_imb_previous: {
                    if (mController != null) {
                        mController.previous();
                        curPlay = mController.getCurrentFile();
                    }
                }
                break;
                default:
                    break;
            }
            if (id != R.id.amc_imb_list && id != R.id.amc_lyt_list) {
                // 点击非列表按钮和列表view,有权限，列表消失。否则显示
                // 点击amc_lyt_list，视频列表仍会存在
                if (mVideoView.hasPermitt()) showList(false);
            }
        }
    };

//    private View.OnClickListener floatViewOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (!CommonUtil.isNetConnect(VideoPlayerActivity.this)) {
//                return;
//            }
//            if (mController != null) mController.pause();
//            ShareMomentEngine engine = new ShareMomentEngine.Intern()
//                    .setProId(ShareMomentEngine.getSelectProId())
//                    .setMyProId(myProductId)
//                    .setItemId(curFileId)
//                    .setCategory(DrmPat.VIDEO)
//                    .setSharePosition(String.valueOf(g_skbProgress.getProgress()))
//                    .launch();
//            engine.work(VideoPlayerActivity.this);
//        }
//    };

    private SeekBar.OnSeekBarChangeListener progressChangedListener = new SeekBar
            .OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mController != null) {
                mController.postCallback();
                mController.seek(seekBar.getProgress());
            }
//            ViewUtil.hideWidget(g_floatView);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mController != null)
                mController.removeCallback();
            g_lytControlTop.removeCallbacks(controlShowRunnable);
//            g_floatView.clearAnimation();
//            ViewUtil.showWidget(g_floatView);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                String progressText = FormatterUtil.formatTime(progress);
                g_txtCurPos.setText(progressText);
//                g_txtFloatView.setText(progressText);
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
//            showInfo(false);
            if (mVideoView.hasPermitt()) showList(false);
            if (isClickControllView(event)) {
                showControl(true);
            } else {
                showControl(!ViewUtil.isVisible(g_lytControlTop));
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isClickControllView(MotionEvent event) {
        final int titleHeight = getResources().getDimensionPixelSize(
                R.dimen.title_bar_height);
        final int y = (int) event.getY();
        //DisplayMetrics dm = new DisplayMetrics();
        //((WindowManager) getSystemService(Context.WINDOW_SERVICE))
        //        .getDefaultDisplay().getMetrics(dm);
        final int screenY = Constant.screenHeight;
        return y < titleHeight || (screenY - y) < titleHeight;
    }

    /**
     * 列表信息
     */
    public void showList() {
        if (!needInit) {
            showList(!ViewUtil.isVisible(g_lytList));
        } else {
            showList(true);
        }
    }

    public void showList(boolean show) {
        if (null == mDrmFiles) return;

        if (show) {
            if (needInit) {
                ListView lv = (ListView) findViewById(R.id.amc_lv_list);
                videoAdapter = new VideoItemAdapter(this, mDrmFiles);
                lv.setAdapter(videoAdapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if (mController == null) return;
                        curPlay = videoAdapter.getItem(position);
                        if (!curPlay.isCheckOpen()) {
                            showToast(getString(R.string.file_expired));
                            return;
                        }
                        if (curPlay.isInEffective()) {
                            showToast(getString(R.string.file_ineffective));
                            return;
                        }

                        if (curPlay.isCheckOpen() && !TextUtils.isEmpty(curPlay.getPrivateKey())) {
                            showList(false); // 有权限就消失列表
                        }
                        showControl(true); // 重新计时
                        mController.pause();
                        mController.stop();
                        mController.start(position);
                    }
                });
                lv.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view,
                                                     int scrollState) {
                        showControl(true); // 重新计时
                    }

                    @Override
                    public void onScroll(AbsListView view,
                                         int firstVisibleItem, int visibleItemCount,
                                         int totalItemCount) {
                    }
                });
                needInit = false;
            }
            videoAdapter.setCurPosition(mDrmFiles.indexOf(curPlay));
            if (!ViewUtil.isVisible(g_lytList)) {
                Animation ta = Util.AnimationUtil.translate(g_lytList, false, show, Util
                        .AnimationUtil
                        .Location.Right);
                Animation aa = Util.AnimationUtil.alpha(g_lytList, false, show);
                Util.AnimationUtil.group(g_lytList, show, ta, aa);
            }
        } else {
            if (ViewUtil.isVisible(g_lytList)) {
                Animation ta = Util.AnimationUtil.translate(g_lytList, false, show, Util
                        .AnimationUtil
                        .Location.Right);
                Animation aa = Util.AnimationUtil.alpha(g_lytList, false, show);
                Util.AnimationUtil.group(g_lytList, show, ta, aa);
            }
        }
    }

    private void showControl(boolean show) {
        g_lytControlTop.removeCallbacks(controlShowRunnable);
        if (show) {
            if (!ViewUtil.isVisible(g_lytControlTop)) {
                Util.AnimationUtil.translate(g_lytControlTop, true, show, Util.AnimationUtil
                        .Location.Top);
                Util.AnimationUtil.translate(g_lytControlBottom, true, show, Util.AnimationUtil
                        .Location.Bottom);
            }
            g_lytControlTop.postDelayed(controlShowRunnable, CONTROL_DURATION);
            g_txtSystime.setText(TimeUtil.getDateString(new Date(), TIME_FORMATTER));
        } else {
            if (ViewUtil.isVisible(g_lytControlTop)) {
                // 这里不能直接gone，否则没有动画，但如果不Gone，则其所在位置仍有焦点，所以放在AnimationListener中执行Gone操作
                Util.AnimationUtil.translate(g_lytControlTop, true, show, Util.AnimationUtil
                        .Location.Top);
                Util.AnimationUtil.translate(g_lytControlBottom, true, show, Util.AnimationUtil
                        .Location.Bottom);
            }
//            showInfo(false);
            showList(false);
            // ViewUtil.hideWidget(g_floatView);
        }
    }

    private final Runnable controlShowRunnable = new Runnable() {
        @Override
        public void run() {
            showControl(false);
        }
    };

    /***
     * 设置电池电量
     *
     * @param level
     * @param image
     */
    private void setBatteryStatus(int level, ImageView image) {
        ViewUtil.showWidget(image);
        if (level < 10) {
            image.setImageResource(R.drawable.battery_0);
        } else if (level < 40) {
            image.setImageResource(R.drawable.battery_1);
        } else if (level < 90) {
            image.setImageResource(R.drawable.battery_2);
        } else {
            image.setImageResource(R.drawable.battery_3);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //if (mVideoView != null) mVideoView.pause();
        if (mController != null) mController.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseSources();
    }

    private void releaseSources() {
        //dismissPop();
        ToastShow.getToast().cancel();
        unregisterReceiver(batteryReceiver);
        handler.removeCallbacksAndMessages(null);
        IjkMediaPlayer.native_profileEnd();
        if (mController != null) {
            mController.removeCallback();
            mController.release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mController != null && mController.isPlaying()) {
            mController.pause();
        }
    }

    /**
     * 设置手势事件（处理视频快进后退，音量和亮度大小）
     */
    private void setPlayerGesture() {
        if (getWindow() == null) return;
        if (getWindow().getDecorView() == null) return;

        final GestureDetector gestureDetector = new GestureDetector(this,
                new PlayerGestureListener());
        View decorView = getWindow().getDecorView();
        decorView.setClickable(true);
        decorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                g_lytControlTop.removeCallbacks(controlShowRunnable);
                if (gestureDetector.onTouchEvent(event)) return true;
                // 处理手势结束,此处手势事件只会是up或者down.
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
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
    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        /**
         * @param e1        第一次运动的事件。
         * @param e2        运动后的事件。
         * @param distanceX 沿x轴滚动的距离。
         * @param distanceY 沿y轴滚动的距离。
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // 如果视频高或者宽为0,则不做操作。
            if (mVideoView.getHeight() == 0 || mVideoView.getHeight() == 0)
                return super.onScroll(e1, e2, distanceX, distanceY);
            // 如果停止播放,则不做操作。
            if (!mVideoView.isPlaying())
                return super.onScroll(e1, e2, distanceX, distanceY);
            // mOldX 滚动前的x坐标。mOldY 滚动前的y坐标
            float mOldX = e1.getX(), mOldY = e1.getY();
            // deltaX x轴滚动的距离，deltaY y轴滚动的距离
            float deltaX = mOldX - e2.getX();
            float deltaY = mOldY - e2.getY();
            // 第一次触发 初始化参数
            if (firstTouch) {
                // toSeek x轴运动距离大于或等于y轴距离 toSeek为true
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                // 滚动前 mOldX 坐标大于一半的屏幕
                volumeControl = mOldX > Constant.screenWidth * 0.5f;
                firstTouch = false;
            }
            // x轴距离大于y轴距离。为true
            if (toSeek) {
                // 计算x轴移动的距离占屏幕宽度的百分比
                float percent = -deltaX / mVideoView.getWidth();
                // 通过移动比例，计算视频需要快进或退的时间。p0 为新时间，p1 为总时间。
                long[] p = SysVolumeLightUtil.getProgressSlide(mVideoView.getDuration(),
                        mVideoView.getCurrentProgress(), percent);
                if (mController != null) {
                    // 跳转的时间。
                    int progress = (int) p[0];
                    // 设置跳转时间。
                    mController.seek(progress);
                    g_txtCurPos.setText(FormatterUtil.formatTime(progress));
                    g_skbProgress.setProgress(progress);
                }
                if (p[1] != 0) {
                    g_imgControll
                            .setImageDrawable(p[1] > 0 ? quickD : retreatD);
                    g_txtControll.setText(p[1] > 0 ? "+" + p[1] + "s" : p[1]
                            + "s");
                }
            }
            // 如果在y轴滑动的多点。
            else {
                float percent = deltaY / mVideoView.getHeight();
                // 滚动前 mOldX 坐标大于一半的屏幕
                if (volumeControl) {
                    int volume = SysVolumeLightUtil.setVolumeSlide(
                            SZVideoPlayerActivity.this, percent);
                    g_imgControll.setImageDrawable(volume > 0 ? volumeD
                            : xVolumeD);
                    g_txtControll.setText(volume + "%");
                } else {
                    int brightness = SysVolumeLightUtil.setBrightnessSlide(
                            SZVideoPlayerActivity.this, percent);
                    g_imgControll.setImageDrawable(lightD);
                    g_txtControll.setText(brightness + "%");
                }
            }
            showSysVolumeControll(true);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);
        }
    }

    /**
     * 显示设置音量、亮度、进度的对话框
     *
     * @param show
     */
    private void showSysVolumeControll(boolean show) {
        if (show) {
            if (g_sysControll.getVisibility() == View.GONE) {
                g_sysControll.setVisibility(View.VISIBLE);
            }
        } else {
            g_sysControll.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) return;
        if (intent.getBooleanExtra(KeyHelp.KEY_FROM_CHECK, false)) {
            getValue();
            loadData();
        }
    }


}
