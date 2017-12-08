package cn.com.pyc.suizhi.service;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.conowen.libmad.MusicPlayer;
import com.sz.help.KeyHelp;
import com.sz.mobilesdk.database.bean.AlbumContent;
import com.sz.mobilesdk.database.practice.AlbumContentDAOImpl;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.bean.event.MusicChangeNameEvent;
import cn.com.pyc.suizhi.bean.event.MusicCurrentPlayEvent;
import cn.com.pyc.suizhi.bean.event.MusicLrcEvent;
import cn.com.pyc.suizhi.common.MusicMode;
import cn.com.pyc.suizhi.help.DRMFileHelp;
import cn.com.pyc.suizhi.help.MusicHelp;
import cn.com.pyc.suizhi.help.ProgressHelp;
import cn.com.pyc.suizhi.receiver.MusicProgressReceiver;
import cn.com.pyc.suizhi.util.DRMUtil;
import de.greenrobot.event.EventBus;

/**
 * 音乐播放服务
 * <p>
 * Created by hudq on 2017/3/23.
 */

public class MusicPlayService extends BaseMusicService implements AudioManager
        .OnAudioFocusChangeListener {

    private static final String MYPRO_ID = "cur_myProId";

    //private Context mContext;
    private String fileId;
    private String myProductId;
    private String productUrl;
    private int progress;
    private List<DrmFile> files;

    private Timer timer = null;
    private MusicPlayer mMusicPlayer;

    private int mCurrentIndex = 0;
    private volatile int totalCount = 0;
    private Handler handler = new Handler();

    private boolean setMyProductId(String myProductId) {
        return SPUtil.save(MYPRO_ID, myProductId);
    }

    private String getMyProductId() {
        return (String) SPUtil.get(MYPRO_ID, "");
    }

    private boolean removeMyProductId() {
        return SPUtil.remove(MYPRO_ID);
    }

    private String getLrcId() {
        if (files == null || files.isEmpty()) {
            return null;
        }
        return files.get(mCurrentIndex).getLyricId();
    }

    private String getFileId() {
        if (files == null || files.isEmpty()) {
            return null;
        }
        return files.get(mCurrentIndex).getFileId();
    }

    private String getFileName() {
        if (files == null || files.isEmpty()) {
            return null;
        }
        return files.get(mCurrentIndex).getFileName();
    }

    private String getFilePath() {
        if (files == null || files.isEmpty()) {
            return null;
        }
        return files.get(mCurrentIndex).getFilePath();
    }

    private String getPrivateKey() {
        if (files == null || files.isEmpty()) {
            return null;
        }
        return files.get(mCurrentIndex).getPrivateKey();
    }

    private boolean checkOpen() {
        return files.get(mCurrentIndex).isCheckOpen();
    }

    public int getCurrentPosition() {
        return mMusicPlayer != null ? mMusicPlayer.getCurrentPosition() : 0;
    }

    public int getTotalPosition() {
        return mMusicPlayer != null ? mMusicPlayer.getDuration() : 0;
    }

    public void seek(int position) {
        if (mMusicPlayer != null)
            mMusicPlayer.seekTo(position);
    }

//    public boolean isPlaying() {
//        return mMusicPlayer != null && mMusicPlayer.isPlaying();
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mContext = this;
//        if (handler != null) {
//            handler.post(saveProgressRunnable);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(saveProgressRunnable);
        }
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(this);
        }
        removeMyProductId();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            //throw new IllegalArgumentException("start service failed.");
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        if (intent.hasExtra(KeyHelp.MPS_FILES))
            files = intent.getParcelableArrayListExtra(KeyHelp.MPS_FILES);

        if (intent.hasExtra(KeyHelp.MPS_FILE_ID)) {
            fileId = intent.getStringExtra(KeyHelp.MPS_FILE_ID);
            //点击音乐列表，需要重新计算index
            checkPlayFiles();
        }
        if (intent.hasExtra(KeyHelp.MPS_MYPRODUCT_ID)) {
            myProductId = intent.getStringExtra(KeyHelp.MPS_MYPRODUCT_ID);
            checkPlayFiles();
        }
        if (intent.hasExtra(KeyHelp.MPS_PRODUCT_URL))
            productUrl = intent.getStringExtra(KeyHelp.MPS_PRODUCT_URL);

        if (intent.hasExtra(KeyHelp.MPS_PROGRESS)) {
            progress = intent.getIntExtra(KeyHelp.MPS_PROGRESS, 0);
        }
        if (intent.hasExtra(KeyHelp.MPS_OPTION)) {
            int option = intent.getIntExtra(KeyHelp.MPS_OPTION, -1);
            SZLog.d("music option = " + option);
            openService(option);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void checkPlayFiles() {
        if (myProductId != null) {
            setMyProductId(myProductId);
            checkData();
            mCurrentIndex = DRMFileHelp.getStartPosition(fileId, files);
        }
    }

    private void checkData() {
        if (files == null) {
            if (myProductId == null) myProductId = getMyProductId();
            List<AlbumContent> albumContents = AlbumContentDAOImpl.getInstance()
                    .findAlbumContentByMyProId(myProductId);
            files = DRMFileHelp.convert2DrmFileList(albumContents, productUrl);
            mCurrentIndex = DRMFileHelp.getStartPosition(MusicHelp.getCurrentPlayId(), files);
        }
        totalCount = files != null ? files.size() : 0;
        SZLog.v("","totalCount: " + totalCount);
    }

    private void openService(int option) {
        if (option == -1)
            throw new IllegalArgumentException("option == -1; start service failed.");

        switch (option) {
            case MusicMode.Status.PLAY:
                play();
                break;
            case MusicMode.Status.PAUSE:
                pause();
                break;
            case MusicMode.Status.STOP:
                stop();
                break;
            case MusicMode.Status.RELEASE:
                release();
                break;
            case MusicMode.Status.CONTINUE:
                continuePlay();
                break;
            case MusicMode.Status.PROGRESS:
                progressPlay(progress);
                break;
            case MusicMode.PREVIOUS:
                previous();
                break;
            case MusicMode.NEXT:
                next();
                break;
            default:
                break;
        }
    }

    //选择播完当前时，上一首由下一首重置定时器
    private void reSetNextMusicTimer(long total) {
        //9: 播放完当前声音(musicUI)
        if ((int) SPUtil.get(MusicHelp.TIMER_KEY, 0) == 9) {
            Intent intent = new Intent(this, MusicTimerService.class);
            intent.putExtra(MusicHelp.TIMER_COUNTDOWN, total);
            startService(intent);
        }
    }

    private void play() {
        MusicMode.STATUS = MusicMode.Status.PLAY;
        //播放时请求焦点，如果请求成功，其他播放器将失去焦点，暂停播放，暂停时丢弃焦点。
        if (mAudioManager != null)
            mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager
                    .AUDIOFOCUS_GAIN);
        //存储当前播放Id
        MusicHelp.setCurrentPlayId(getFileId());
        if (mMusicPlayer == null) {
            mMusicPlayer = new MusicPlayer(getFilePath(), getPrivateKey());
        }
        //发送消息，通知页面更新总时间
        final int mDuration = getTotalPosition();
        String action = DRMUtil.BROADCAST_MUSIC_OBTAIN_TIME;
        sendMyBroadcast(action, 0, mDuration);
        //发送下通知，通知切歌
        EventBus.getDefault().post(new MusicChangeNameEvent(getFileName()));
        EventBus.getDefault().post(new MusicCurrentPlayEvent(getFileId()));
        EventBus.getDefault().post(new MusicLrcEvent(getLrcId(), MusicLrcEvent.Way.LRC_CHANGE));

        checkData();
        reSetNextMusicTimer((mDuration - 2) * 1000L);

        mMusicPlayer.play();
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if ((MusicMode.STATUS == MusicMode.Status.PAUSE)) return;
                    int mCurrentPosition = getCurrentPosition();
                    if (mCurrentPosition < ((mDuration - 2))) {
                        // 发送进度
                        String action = DRMUtil.BROADCAST_MUSIC_PROGRESS;
                        sendMyBroadcast(action, mCurrentPosition, mDuration);
                        // 发送进度时间，更新歌词
                        EventBus.getDefault().post(new MusicLrcEvent(mCurrentPosition *
                                1000L, MusicLrcEvent.Way.LRC_UPDATE));
                    } else {
                        // 根据不同的播放模式 切换不同的歌曲
                        changePlay();
                    }
                }
            }, 0L, 1000L);
        }

        //播放状态，执行此消息
        if (handler != null) {
            handler.postDelayed(saveProgressRunnable, 3000);
        }
    }

    /**
     * 发送进度和总刻度通知
     */
    private void sendMyBroadcast(String action, int currentPos, int duration) {
        Intent intent = new Intent(action);
        intent.putExtra(MusicProgressReceiver.TAG_CURRENT, currentPos);
        intent.putExtra(MusicProgressReceiver.TAG_DURATION, duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void continuePlay() {
        MusicMode.STATUS = MusicMode.Status.CONTINUE;
        if (mMusicPlayer == null) {
            mMusicPlayer = new MusicPlayer(getFilePath(), getPrivateKey());
        }
        mMusicPlayer.play();

        //暂停后继续播放，执行此消息
        if (handler != null) {
            handler.post(saveProgressRunnable);
        }
    }

    private void progressPlay(int progress) {
        MusicMode.STATUS = MusicMode.Status.PROGRESS;
        if (mMusicPlayer != null) {
            mMusicPlayer.seekTo(progress);
        }
    }

    private void pause() {
        MusicMode.STATUS = MusicMode.Status.PAUSE;
        if (mMusicPlayer != null) {
            mMusicPlayer.pause();
        }
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(this);
        }
        //暂停移除此消息
        if (handler != null) {
            handler.removeCallbacks(saveProgressRunnable);
        }
    }

    private void stop() {
        MusicMode.STATUS = MusicMode.Status.STOP;
        if (mMusicPlayer != null) {
            mMusicPlayer.stop();
            mMusicPlayer = null;
        }
        seek(0);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        //停止移除此消息
        if (handler != null) {
            handler.removeCallbacks(saveProgressRunnable);
        }
    }

    private void release() {
        MusicMode.STATUS = MusicMode.Status.RELEASE;
        saveProgress();
        if (mMusicPlayer != null) {
            if (mMusicPlayer.isPlaying()) {
                mMusicPlayer.stop();
            }
            mMusicPlayer.release();
            mMusicPlayer = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        MusicHelp.setCurrentPlayId("");
        MusicHelp.stopMusicView(this);

        stopSelf();
    }

    private void saveProgress() {
        String fileId = getFileId();
        if (fileId != null) {
            //保存文件播放的进度
            ProgressHelp.saveProgress(fileId, getCurrentPosition());
            if (myProductId != null) {
                //保存播放的文件id，对应ListFileActivity续播功能：againPlaying
                ProgressHelp.saveProgress("ap_" + myProductId, fileId);
            }
        }
    }

    //每隔3秒保存一次进度
    Runnable saveProgressRunnable = new Runnable() {
        @Override
        public void run() {
            saveProgress();
            handler.postDelayed(saveProgressRunnable, 3000);
        }
    };

    public void previous() {
        if (mCurrentIndex >= 1) {
            mCurrentIndex--;
        } else {
            mCurrentIndex = totalCount - 1;
        }
        //mCurrentIndex = (mCurrentIndex >= 1) ? --mCurrentIndex : (mCurrentIndex = (totalCount -
        // 1));
        playing();
    }

    public void next() {
        if (mCurrentIndex < totalCount - 1) {
            mCurrentIndex++;
        } else {
            mCurrentIndex = 0;
        }
        //DRMLog.i("mCurrentIndex:" + mCurrentIndex);
        //mCurrentIndex = (mCurrentIndex < (totalCount - 1)) ? ++mCurrentIndex : 0;
        playing();
    }

    private void playing() {
        if (checkOpen()) {
            stop();
            play();
            //查询是否存在播放进度记录
            int sProgress = (int) ProgressHelp.getProgress(getFileId(), 0);
            if (sProgress > 0) {
                MusicHelp.progressPlay(this, sProgress);
                ProgressHelp.removeProgress(getFileId());
            }
        } else {
            stop();
            //showToast(getString(R.string.play_miss_authority));
            //没有权限，继续播放其他的
            changePlay();
        }
        EventBus.getDefault().post(new MusicChangeNameEvent(getFileName()));
        EventBus.getDefault().post(new MusicCurrentPlayEvent(getFileId()));
    }

    private void changePlay() {
        if (MusicHelp.isCircle()) {
            //mCurrentIndex = (mCurrentIndex < (totalCount - 1)) ? ++mCurrentIndex : 0;
            if (mCurrentIndex < totalCount - 1) {
                mCurrentIndex++;
            } else {
                mCurrentIndex = 0;
            }
            playing();
        } else if (MusicHelp.isRandom()) {
            mCurrentIndex = new Random().nextInt(totalCount); // [0-size())
            playing();
        } else if (MusicHelp.isSingle()) {
            playing();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            //失去了Audio Focus，并将会持续很长的时间
            case AudioManager.AUDIOFOCUS_LOSS: {
                break;
            }
            //暂时失去AudioFocus，并会很快再次获得。必须停止Audio的播放，
            // 但是因为可能会很快再次获得AudioFocus，这里可以不释放Media资源；
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
//                if (isPlaying()) {
//                    pause();
//                }
                break;
            }
            //暂时失去AudioFocus，但是可以继续播放，不过要在降低音量
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if (mAudioManager != null) {
                    //int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, );
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_PLAY_SOUND);
                }
            }
            break;
            //获得了Audio Focus
            case AudioManager.AUDIOFOCUS_GAIN: {
//                if (MusicMode.STATUS == MusicMode.Status.PAUSE) {
//                    continuePlay();
//                }
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_PLAY_SOUND);
                }
            }
            break;
        }
    }


}
