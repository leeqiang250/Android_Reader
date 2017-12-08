package cn.com.pyc.pbbonline.common;

/**
 * 类定义：记录常量字段(基本数据类型)
 * 
 * @author hudq
 */
public class K
{
	public static final String platform = "Android";
	public static final String SCANCODE = "appQrcode";
	public static final String JUMP_FLAG = "jump_flag"; //跳转标志,标示从那个页面跳转
	public static final int UI_MAIN = 100;	//主界面
	public static final int UI_PDF = 101;  //pdf主界面
	public static final int UI_MUSIC = 102;//音乐主界面
	public static final int UI_VIDEO = 103;//视频主界面
	
	//outline目录位置
	public static int OUTLINE_POSITION = 0;
	// 当前播放的contentId,仅仅用作判断是否点击的同一个文件;
	public volatile static String CURRENT_MUSIC_ID;
	/** 播放状态，默认停止状态 */
	public static int playState = IMusicConst.OPTION_STOP;
	/** 播放模式，默认列表循环 */
	public static int playMode = IMusicConst.CIRCLE;

}
