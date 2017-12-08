package cn.com.pyc.pbbonline.common;

public interface IMusicConst
{
	/**
	 * 播放
	 */
	int OPTION_PLAY = 1;
	/**
	 * 暂停
	 */
	int OPTION_PAUSE = 2;
	/**
	 * 停止
	 */
	int OPTION_STOP = 3;
	/**
	 * 继续播放
	 */
	int OPTION_CONTINUE = 4;

	/**
	 * 让Activity更新进度
	 */
	int MSG_UPDATE_PROCESS = 5;

	/**
	 * 进度的改变
	 */
	int OPTION_CHANGE = 6;

	/**
	 * 释放资源
	 */
	int RELEASE = 98;

	/**
	 * 获取歌曲时间
	 */
	int OBTAIN_TIME = 99;

	// ///////////播放模式///////////////////
	/**
	 * 列表循环
	 */
	int CIRCLE = 17;
	/**
	 * 单曲循环
	 */
	int SINGLE_R = 18;
	/**
	 * 随机播放
	 */
	int RANDOM = 19;
	/**
	 * 顺序播放 1次
	 */
	int ORDER = 20;

	// ///////////播放模式///////////////////

}
