/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2012 YIXIA.COM
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
 *
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

import java.util.List;

import cn.com.pyc.suizhi.bean.DrmFile;
import cn.com.pyc.suizhi.help.ProgressHelp;

/**
 * A view containing controls for a MediaPlayer. Typically contains the buttons
 * like "Play/Pause" and a progress slider. It takes care of synchronizing the
 * controls with the state of the MediaPlayer.
 * <p>
 * The way to use this class is to a) instantiate it programatically or b)
 * create it in your xml layout. a) The MediaController will create a default
 * set of controls and put them in a window floating above your application.
 * Specifically, the controls will float above the view specified with
 * setAnchorView(). By default, the window will disappear if left idle for three
 * seconds and reappear when the user touches the anchor view. To customize the
 * MediaController's style, layout and controls you should extend
 * MediaController and override the {#link {@link # makeControllerView()} method.
 * b) The MediaController is a FrameLayout, you can put it in your layout xml
 * and get it through {@link # findViewById(int)}. NOTES: In each way, if you
 * want customize the MediaController, the SeekBar's id must be
 * mediacontroller_progress, the Play/Pause's must be mediacontroller_pause,
 * current time's must be mediacontroller_time_current, total time's must be
 * mediacontroller_time_total, file name's must be mediacontroller_file_name.
 * And your resources must have a pause_button drawable and a play_button
 * drawable.
 * <p>
 * Functions like show() and hide() have no effect when MediaController is
 * created in an xml layout.
 */
public class SZMediaController {
    private SZVideoView videoView;
    private int curOne;
    private int totalSize;
    private List<DrmFile> drmFiles;
    private DrmFile currentDrmFile;

    // public static final int MODE_SINGLE = 0xa; // 单个循环
    // public static final int MODE_CYCLE = 0xb; // 列表循环
    // public static final int MODE_LIST = 0xc; // 顺序播放
    // public static final int MODE_RANDOM = 0xd; // 随机播放

    /**
     * 自动播放
     *
     * @param videoView
     * @param drmFiles
     * @param curOne
     */
    public SZMediaController(SZVideoView videoView, List<DrmFile> drmFiles,
                             int curOne) {
        this.videoView = videoView;
        this.drmFiles = drmFiles;
        this.curOne = curOne;

        totalSize = this.drmFiles.size();
        start();
    }

    public SZMediaController(SZVideoView videoView, List<DrmFile> drmFiles) {
        this.videoView = videoView;
        this.drmFiles = drmFiles;

        totalSize = this.drmFiles.size();
    }

    // public void control(View v, Activity atv)
    // {
    // switch (v.getId())
    // {
    // case R.id.amc_imb_next:
    // {
    // if (!CommonUtil.isFastDoubleClick(600))
    // {
    // spm.putInt(drmFiles.get(curOne).getContentId(),
    // videoView.getPosition());
    // next();
    // // 保存视频专辑位置索引
    // }
    // }
    // break;
    //
    // case R.id.amc_imb_start_pause:
    // startOrPause();
    // break;
    //
    // case R.id.amc_imb_previous:
    // {
    // if (!CommonUtil.isFastDoubleClick(600))
    // {
    // spm.putInt(drmFiles.get(curOne).getContentId(),
    // videoView.getPosition());
    // previous();
    //  保存视频专辑位置索引
    // }
    // }
    // break;
    //
    // default:
    // break;
    // }
    // }

    public DrmFile getCurrentFile() {
        return currentDrmFile;
    }

    /**
     * 暂停或播放
     */
    public void startOrPause() {
        if (videoView != null) {
            videoView.startOrPause();
        }
    }

    public void pause() {
        if (videoView != null) {
            videoView.pause();
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (videoView != null) {
            videoView.stop();
        }
    }

    public void release() {
        if (videoView != null) {
            videoView.release();
        }
    }

    public boolean isPlaying() {
        return videoView != null && videoView.isPlaying();
    }

    /**
     * 指定位置时间播放
     *
     * @param msec Long mills
     */
    public void seek(long msec) {
        if (videoView != null) {
            videoView.seek(msec);
        }
    }

    public void removeCallback() {
        if (videoView != null) {
            videoView.removeCallback();
        }
    }

    public void postCallback() {
        if (videoView != null) {
            videoView.postCallback();
        }
    }

    public void start() {
        if (videoView != null) {
            videoView.start();
        }
    }

    /**
     * 开始播放
     *
     * @param curOne
     */
    public void start(int curOne) {
        this.curOne = curOne;
        startPlay();
    }

    /**
     * 下一个
     */
    public void next() {
        if (currentDrmFile == null) return;
        if (videoView != null) {
            ProgressHelp.saveProgress(currentDrmFile.getFileId(), videoView.getCurrentProgress());
        }
        nextPlay();
    }

    /**
     * 上一个
     */
    public void previous() {
        if (currentDrmFile == null) return;
        if (videoView != null) {
            ProgressHelp.saveProgress(currentDrmFile.getFileId(), videoView.getCurrentProgress());
        }
        previousPlay();
    }

    private void nextPlay() {
        curOne = curOne + 1 < totalSize ? curOne + 1 : 0;
        startPlay();
    }

    private void previousPlay() {
        curOne = curOne - 1 < 0 ? totalSize - 1 : curOne - 1;
        startPlay();
    }

    private void startPlay() {
        currentDrmFile = drmFiles.get(curOne);
        if (videoView != null) {
            videoView.start(currentDrmFile);
        }
    }

}
