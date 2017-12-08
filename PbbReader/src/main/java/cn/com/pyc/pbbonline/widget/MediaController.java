/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2012 YIXIA.COM
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.com.pyc.pbbonline.widget;

import com.sz.mobilesdk.util.SPUtil;

import java.util.List;

import cn.com.pyc.pbbonline.bean.SZFile;

/**
 * Functions like show() and hide() have no effect when MediaController is
 * created in an xml layout. <br/>
 * Video Controller.<br/>
 * next(); <br/>
 * previous(); <br/>
 * start(); <br/>
 * stop(); <br/>
 */
public class MediaController
{
	private VideoView videoView;
	private List<SZFile> drmFiles;
	private int currentIndex;
	private SZFile currentFile;
	private int totalSize;

	// public static final int MODE_SINGLE = 0xa; // 单个循环
	// public static final int MODE_CYCLE = 0xb; // 列表循环
	// public static final int MODE_LIST = 0xc; // 顺序播放
	// public static final int MODE_RANDOM = 0xd; // 随机播放

	public MediaController(VideoView videoView, List<SZFile> drmFiles)
	{
		this.videoView = videoView;
		this.drmFiles = drmFiles;
		totalSize = this.drmFiles.size();
	}

	/**
	 * @param videoView
	 * @param drmFiles
	 * @param currentIndex
	 */
	public MediaController(VideoView videoView, List<SZFile> drmFiles, int currentIndex)
	{
		this.videoView = videoView;
		this.drmFiles = drmFiles;
		this.currentIndex = currentIndex;
		totalSize = this.drmFiles.size();

		start();
	}

	/**
	 * 下一个
	 */
	public void next()
	{
		SPUtil.save(currentFile.getContentId(), videoView.getCurrentProgress());//保存正在播放的文件进度
		nextPlay();
	}

	/**
	 * 上一个
	 */
	public void previous()
	{
		SPUtil.save(currentFile.getContentId(), videoView.getCurrentProgress()); //保存正在播放的文件进度
		previousPlay();
	}

	/**
	 * 暂停则播放；播放则暂停
	 */
	public void startOrPause()
	{
		videoView.startOrPause();
	}

	/**
	 * 停止
	 */
	public void stop()
	{
		videoView.stop();
	}

	/**
	 * 指定位置播放
	 * 
	 * @param msec
	 */
	public void seek(int msec)
	{
		videoView.seek(msec);
	}

	/**
	 * 开始播放
	 * 
	 * @param index
	 *            开始播放文件索引位置
	 */
	public void start(int index)
	{
		currentIndex = index;
		start();
	}

	/**
	 * 暂停
	 */
	public void pause()
	{
		videoView.pause();
	}

	private void nextPlay()
	{
		currentIndex = (currentIndex + 1 < totalSize) ? currentIndex + 1 : 0;
		start();
	}

	private void previousPlay()
	{
		currentIndex = (currentIndex - 1 < 0) ? totalSize - 1 : currentIndex - 1;
		start();
	}

	private void start()
	{
		currentFile = drmFiles.get(currentIndex);
		videoView.start(currentFile);
	}

}
