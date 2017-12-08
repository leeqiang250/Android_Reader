/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2012 YIXIA.COM
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.com.pyc.suizhi.widget.video;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sz.mobilesdk.util.SZLog;

import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.help.ProgressHelp;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting. VideoView also
 * provide many wrapper methods for {io.vov.vitamio.MediaPlayer }, such as
 * {@link # getVideoWidth()}, {@link # setSubShown(boolean)}
 */
public class SZVideoView extends SurfaceView {
    private static final String TAG = "VideoView";
    private IjkMediaPlayer mediaPlayer;
    private boolean hasPermit = false; // 当前播放的文件是否有权限
    private boolean isStart = false;

    private DrmFile currentPlayFile; // 当前播放文件
    private long currentProgress; // 当前播放进度
    private long duration; // 时长
    private SurfaceHolder mSurfaceHolder;
    private SZMediaHandler msgHandler;

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        /**
         * 当Surface的状态（大小和格式）发生变化的时候会调用该函数，在surfaceCreated调用后该函数至少会被调用一次
         * holder:更新surface的SurfaceHolder format:新的图形格式 width:新的宽度 height：新的高度
         */
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            DebugLog.d(TAG, "surfaceChanged");
            if (mediaPlayer != null) {
                mediaPlayer.setDisplay(holder);
            }
        }

        /**
         * 当Surface第一次创建后会立即调用该函数。程序可以在该函数中做些和绘制界面相关的初始化工作
         */
        public void surfaceCreated(SurfaceHolder holder) {
            DebugLog.d(TAG, "surfaceCreated");
            if (mediaPlayer == null && currentPlayFile != null) {
                //start(currentPlayFile);
            }
        }

        /**
         * 当Surface被摧毁前会调用该函数，该函数被调用后就不能继续使用Surface了，一般在该函数中来清理使用的资源
         * holder为所要被删除的surface的SurfaceHolder
         */
        public void surfaceDestroyed(SurfaceHolder holder) {
            DebugLog.d(TAG, "surfaceDestroy");
            if (currentPlayFile != null) {
                //保存进度
                ProgressHelp.saveProgress(currentPlayFile.getFileId(), getCurrentProgress());
                //保存播放的文件id，对应ListFileActivity续播功能：againPlaying
                ProgressHelp.saveProgress("ap_" + currentPlayFile.getMyProductId(),
                        currentPlayFile.getFileId());
            }
            release_();
        }
    };

    public SZVideoView(Context context) {
        this(context, null);
    }

    /**
     * 创建一个带有attrs属性的VideoView实例 attrs 用于视图的XML标签属性集合
     */
    public SZVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(callback);
        requestFocus();
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isValid() {
        return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
    }

    public boolean hasPermitt() {
        if (!isStart)
            throw new IllegalStateException(
                    "required call method 'start(file)' first.");
        return hasPermit;
    }

    public void setHandler(SZMediaHandler msgHandler) {
        this.msgHandler = msgHandler;
    }

    /**
     * 当前播放进度
     *
     * @return
     */
    public long getCurrentProgress() {
        return currentProgress;
    }

    /**
     * 文件时长
     *
     * @return
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 当前播放的文件DrmFile
     *
     * @return
     */
    public DrmFile getCurrentPlayFile() {
        return currentPlayFile;
    }

    /**
     * 初始化MediaPlayer
     */
    private void initPlayer() {
        mediaPlayer = new IjkMediaPlayer();

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 12);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,
                "http-detect-range-support", 0);
        // mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,
        // "user_agent", mUserAgent);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC,
                "skip_loop_filter", 48);

        mediaPlayer.setDisplay(getHolder());
        // mediaPlayer.setAvOption(AvFormatOption_HttpDetectRangeSupport.Disable);
        // mediaPlayer.setOverlayFormat(AvFourCC.SDL_FCC_RV32);
        mediaPlayer.setScreenOnWhilePlaying(true);

        mediaPlayer.setOnPreparedListener(preparedListener);
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mediaPlayer.setOnErrorListener(errorListener);
        // 预加载
        mediaPlayer.setOnInfoListener(mInfoListener);
        // mediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
        // mediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
        // mediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);

        postCallback();
    }

    /**
     * 开始播放
     *
     * @param drmFile
     */
    public void start(DrmFile drmFile) {
        //DebugLog.d(TAG, "start play: " + drmFile.toString());
        currentPlayFile = drmFile;
        isStart = true;

        // 检查文件存在本地吗？

        release_();
        if (currentPlayFile != null) {
            if (TextUtils.isEmpty(currentPlayFile.getPrivateKey())) {
                // 没有权限
                hasPermit = false;
                sendMsg(SZMediaHandler.STATE_NO_PLAY_PERMISSION, 0, 0, currentPlayFile);
                return;
            }
            hasPermit = true;

            try {
                initPlayer();
                mediaPlayer.setDataSource(currentPlayFile.getFilePath(),
                        currentPlayFile.getPrivateKey());
            } catch (Exception e) {
                e.printStackTrace();
                sendMsg(SZMediaHandler.STATE_ERROR, 0, 0, currentPlayFile);
            }
            mediaPlayer.seekTo(currentProgress);
            mediaPlayer.prepareAsync();
        } else {
            sendMsg(SZMediaHandler.STATE_ERROR, 0, 0, currentPlayFile);
        }
    }

    /**
     * 开始或暂停
     */
    public void startOrPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            removeCallback();
            mediaPlayer.pause();
            sendMsg(SZMediaHandler.STATE_PLAY_PAUSE, SZMediaHandler.NEGATIVE, 0, null);
        } else {
            postCallback();
            mediaPlayer.start();
            sendMsg(SZMediaHandler.STATE_PLAY_PAUSE, SZMediaHandler.POSITIVE, 0, null);
        }
    }

    public void start() {
        if (mediaPlayer == null) return;
        postCallback();
        mediaPlayer.start();
        sendMsg(SZMediaHandler.STATE_PLAY_PAUSE, SZMediaHandler.POSITIVE, 0, null);
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mediaPlayer == null) return;
        removeCallback();
        mediaPlayer.pause();
        sendMsg(SZMediaHandler.STATE_PLAY_PAUSE, SZMediaHandler.NEGATIVE, 0, null);
    }

    /**
     * 停止
     */
    public void stop() {
        if (mediaPlayer == null) return;
        removeCallback();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    /**
     * 指定播放位置
     *
     * @param msec
     */
    public void seek(long msec) {
        if (mediaPlayer == null) return;
        mediaPlayer.seekTo(msec);
    }

    public void reset() {
        if (mediaPlayer == null) return;
        mediaPlayer.reset();
        removeCallback();
    }

    /**
     * 释放资源
     */
    public void release() {
        removeCallback();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void release_() {
        DebugLog.d(TAG, "release_");
        release();
    }

    public void removeCallback() {
        removeCallbacks(progressRunnable);
    }

    public void postCallback() {
        post(progressRunnable);
    }

    private OnInfoListener mInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    sendMsg(SZMediaHandler.STATE_BUFFERING,
                            SZMediaHandler.POSITIVE, 0, null);
                    break;

                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    sendMsg(SZMediaHandler.STATE_BUFFERING,
                            SZMediaHandler.NEGATIVE, 0, null);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private OnPreparedListener preparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            DebugLog.d(TAG, "onPrepared");
            duration = mp.getDuration();
            sendMsg(SZMediaHandler.STATE_PREPARED_PLAY, duration, 0, currentPlayFile);
            if (mediaPlayer != null) {
                // 当前播放进度
                currentProgress = (long) ProgressHelp.getProgress(currentPlayFile.getFileId(), 0L);
                sendMsg(SZMediaHandler.STATE_PROGRESS, currentProgress, duration, null);
                mediaPlayer.seekTo(currentProgress);
                ProgressHelp.removeProgress(currentPlayFile.getFileId());
            }
        }
    };

    /**
     * 在设置或播放过程中发生错误时调用的回调函数。
     */
    private OnErrorListener errorListener = new OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            release_();
            if (currentPlayFile != null) {
                ProgressHelp.removeProgress(currentPlayFile.getFileId());
            }
            sendMsg(SZMediaHandler.STATE_ERROR, 0, 0, currentPlayFile);
            return false;
        }
    };

//	private tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener
// mSizeChangedListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener()
//	{
//		@Override
//		public void onVideoSizeChanged(IMediaPlayer mp, int width, int height,
//				int sarNum, int sarDen)
//		{
//			DebugLog.e(TAG, "onVideoSizeChanged");
//			int mVideoWidth = mp.getVideoWidth();
//			int mVideoHeight = mp.getVideoHeight();
//			DRMLog.w("mpVideo: width = " + mVideoWidth + ", height = "
//					+ mVideoHeight);
//			DRMLog.w("mpVideo: sarNum = " + sarNum + ", sarDen = " + sarDen);
//
//			if (mVideoHeight > 0 && mVideoWidth > 0)
//			{
//				Point mPoint = DeviceUtils.getScreenSize(getContext());
//				DRMLog.w("screen: width = " + mPoint.x + ", height = " + mPoint.y);
//				float videoScale = (float) mVideoWidth / mVideoHeight;
//				float windowRatio = (float) mPoint.x / mPoint.y;
//				if (sarNum > 0 && sarDen > 0)
//				{
//					videoScale = videoScale * sarNum / sarDen;
//				}
//				DebugLog.e(TAG, "videoScale = " + videoScale);
//				DebugLog.e(TAG, "windowRatio = " + windowRatio);
//				if (mVideoWidth < mPoint.x && mVideoHeight < mPoint.y) // 比屏幕都小，原始显示
//				{
//					mVideoWidth = (int) (mVideoHeight * videoScale);
//				} else
//				{
//					mVideoWidth = (windowRatio < videoScale) ? mPoint.x
//							: (int) (mPoint.y * videoScale);
//					mVideoHeight = (windowRatio > videoScale) ? mPoint.y
//							: (int) (mPoint.x / videoScale);
//				}
//
//				DRMLog.w("VideoWidth2 = " + mVideoWidth + ", VideoHeight2 = "
//						+ mVideoHeight);
//
//				// int margin = (mPoint.x - mVideoWidth) / 2;
//				// DebugLog.e(TAG, "margin= " + margin);
//				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
//						mVideoWidth, mVideoHeight);
//				// lp.setMargins(margin, 0, margin, 0);
//				lp.gravity = Gravity.CENTER;
//				setLayoutParams(lp);
//				// mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
//			}
//
//			// if (mVideoHeight > 0 && mVideoWidth > 0)
//			// {
//			// Point mPoint = DeviceUtils.getScreenSize(getContext());
//			// // TODO:视频宽高，高填充还是宽填充显示屏幕
//			// float scale = (float) mVideoHeight / (float) mVideoWidth;
//			// DebugLog.e(TAG, "scale= " + scale);
//			// mVideoHeight = mPoint.y;
//			// mVideoWidth = (int) (mVideoHeight / scale);
//			// // mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
//			//
//			// // int margin = (mPoint.x - mVideoWidth) / 2;
//			// // DebugLog.e(TAG, "margin= " + margin);
//			// FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
//			// mVideoWidth, mVideoHeight);
//			// // lp.setMargins(margin, 0, margin, 0);
//			// lp.gravity = Gravity.CENTER;
//			// setLayoutParams(lp);
//			// }
//		}
//	};

    private final OnCompletionListener onCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer mp) {
            SZLog.e(TAG, "video play complete.");
            if (currentPlayFile != null) {
                ProgressHelp.removeProgress(currentPlayFile.getFileId());
            }
            long length = (duration == 0) ? mp.getDuration() : duration;
            sendMsg(SZMediaHandler.STATE_PLAY_COMPLETE, 0, length, null);
        }
    };

    // private OnCompletionListener onCompletionListener;
    //
    // /**
    // * 注册在媒体文件加载完毕，可以播放时调用的回调函数
    // */
    // public void setOnCompleteListener(OnCompletionListener
    // onCompletionListener)
    // {
    // this.onCompletionListener = onCompletionListener;
    // }

    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer == null) return;
            currentProgress = mediaPlayer.getCurrentPosition();
            long duration = mediaPlayer.getDuration();
            if (SZVideoView.this.duration == 0)
                SZVideoView.this.duration = duration;
            // if (currentProgress > duration && onCompletionListener != null)
            // {
            // // 如果是有问题的视频，则可能会出现此情况
            // onCompletionListener.onCompletion(mediaPlayer);
            // }
            sendMsg(SZMediaHandler.STATE_PROGRESS, currentProgress, duration, null);
            final int playState = mediaPlayer.isPlaying() ? SZMediaHandler.POSITIVE
                    : SZMediaHandler.NEGATIVE;
            sendMsg(SZMediaHandler.STATE_PLAY_PAUSE, playState, 0, null);
            postDelayed(progressRunnable, 1000);
        }
    };

    /**
     * msgHandler sendMsg
     *
     * @param what
     * @param arg1 long参数1,putLong("arg1", arg1);
     * @param arg2 long参数2,putLong("arg2", arg2);
     * @param obj
     */

    private void sendMsg(int what, long arg1, long arg2, Object obj) {
        Message msg = Message.obtain();
        Bundle data = new Bundle();
        msg.what = what;
        data.putLong("arg1", arg1);
        data.putLong("arg2", arg2);
        // msg.arg1 = arg1;
        // msg.arg2 = arg2;
        msg.setData(data);
        msg.obj = obj;
        msgHandler.sendMessage(msg);
    }
}
