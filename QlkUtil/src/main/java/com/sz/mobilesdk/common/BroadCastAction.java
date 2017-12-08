package com.sz.mobilesdk.common;

/**
 * 定义广播Action值，所有自定义广播action
 * 
 * @author hudq
 * 
 */
public class BroadCastAction {

	// -----主页面相关广播----
	// 关闭main activity
	public static final String ACTION_CLOSE_ACTIVITY = "com.main.broadcast.close_activity";
	// 清除下载缓存
	public static final String ACTION_CLEAR_DOWNLOADED = "com.main.broadcast.clear_downloaded";

	
	// -----音乐相关广播-----
	// 音乐播放器进度条显示。
	public static final String ACTION_MUSIC_PROGRESS = "com.main.broadcast.receiver.Music_Progress";
	// 第一次进入显示总时间
	public static final String ACTION_MUSIC_OBTAIN_TIME = "com.main.broadcast.receiver.Music_Obtain_Time";
	// 音乐状态栏notification广播
	public static final String ACTION_MUSIC_STATUSBAR = "com.main.broadcast.receiver.Music_Statusbar";

}
