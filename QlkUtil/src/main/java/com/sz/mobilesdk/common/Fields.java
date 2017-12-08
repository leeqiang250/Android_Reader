package com.sz.mobilesdk.common;

/**
 * 字段设置
 * 
 * @author hudq
 * 
 */
public class Fields
{
	// 登录方式类型 (@Deprecated)
	public static final int _ACCOUNT = 0x17a; // 账户密码
	public static final int _SCANING = 0x18a; // 扫码

	// 常量类型字符串
	public static final String GUEST_PBB = "guestpbb";
	public static final String _LINE = "_";
	public static final String DOT = ".";
	public static final String UTF_8 = "UTF-8";
	public static final String sZipFileMimeType = "application/zip";

	/********************** 以下参数值的key ***********************/
	// 保存扫描二维码用户名、或用户sz账号登录用户名
	public static final String FIELDS_USER_NAME = "fields_user_name";
	// 保存的分享id
	public static final String FIELDS_ID = "fields_id";
	// 保存的登录用户名
	public static final String FIELDS_LOGIN_USER_NAME = "fields_login_user_name";
	// 保存的登录密码
	public static final String FIELDS_LOGIN_PASSWORD = "fields_login_password";
	// 保存的token,一般是在登陆成功后返回的token值。
	public static final String FIELDS_LOGIN_TOKEN = "fields_login_token";
	// 程序版本号
	public static final String FIELDS_APP_VERSION = "fields_app_version";
	// 扫描获取的主机名字
	@Deprecated
	public static final String FIELDS_SCAN_HOST = "scan_url_for_host";
	// 扫描获取的端口
	@Deprecated
	public static final String FIELDS_SCAN_PORT = "scan_url_for_port";
	// sharedUrl
	public static final String FIELDS_SCAN_URL = "fields_scan_url";
	// 接收ID
	public static final String FIELDS_RECEIVE_ID = "fields_receive_id";
	// 保存极光极光返回的registerID
	public static final String FIELDS_JPUSH_REGISTERID = "jpush_registerid";

	// 浏览器网页跳转参数：source,weixin
	public static final String FIELDS_WEB_SOURCE = "fields_web_source";
	public static final String FIELDS_WEB_WEIXIN = "fields_web_weixin";

	// 记录pbb title和页面标签
	public static final String FIELDS_PBB_TAB = "fields_pbb_lable";
	// //////////////////// 以上参数值的key////////////////////////

	// 扩展名
	public static final String _DRM = ".drm";
	public static final String _MP4 = ".mp4";
	public static final String _MP3 = ".mp3";
	public static final String _PDF = ".pdf";

	/********************** 定义一些常量 ********************/
	// 支持类型， 移动会议Meeting，课件Distribute
	public static final String MEETING = "Meeting";
	public static final String DISTRIBUTE = "Distribute";
	public static final String PBBOBLINE = "PBBONLINE";

	// 专辑类别
	public static final String VIDEO = "VIDEO";
	public static final String MUSIC = "MUSIC";
	public static final String BOOK = "BOOK";

	// 文件类型
	public static final String PDF = "PDF";
	public static final String MP3 = "MP3";
	public static final String MP4 = "MP4";

	/** 按钮点击key */
	public final static String NOTIFY_BUTTONID_TAG = "ButtonId";
	/** 上一首 按钮点击 ID */
	public final static int BUTTON_PREV_ID = 1;
	/** 播放/暂停 按钮点击 ID */
	public final static int BUTTON_PALY_ID = 2;
	/** 下一首 按钮点击 ID */
	public final static int BUTTON_NEXT_ID = 3;
	/** 关闭按钮 */
	public final static int BUTTON_CLOSE_ID = 4;

}
