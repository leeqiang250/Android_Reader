package cn.com.pyc.suizhi.common;

/**
 * 定义一些普通常量（只针对绥知应用）
 */
public final class DrmPat {
    public static final String APP_FULLNAME = "Reader_for_Android";
    public static final String APP_DEVICE = "android";
    public static final String APP_INTENTION = "CLIENT_DOWNLOAD";

    /**
     * 普通登录（账号登录）
     */
    public static final int LOGIN_GENERAL = 0x17a;
    /* 扫描登录 */
    // //public static final int LOGIN_SCANING = 0x18a;
    /**
     * 微信登录
     */
    public static final int LOGIN_WECHAT = 0x10a;
    /**
     * qq登录
     */
    public static final int LOGIN_QQ = 0x11a;

    public static final String UTF_8 = "UTF-8";
    public static final String UNDER_LINE = "_";

    // 扩展名
    public static final String _DRM = ".drm";
    public static final String _MP4 = ".mp4";
    public static final String _MP3 = ".mp3";
    public static final String _PDF = ".pdf";
    public static final String _LRC = ".lrc";
    public static final String _XML = ".xml";

    // 移动会议Meeting，课件Distribute
    // public static final String MEETING = "Meeting";
    // public static final String DISTRIBUTE = "Distribute";
    //文件类型
    public static final String MP4 = "MP4";
    public static final String MP3 = "MP3";
    public static final String PDF = "PDF";

    // 专辑类型
    public static final String VIDEO = "VIDEO";
    public static final String MUSIC = "MUSIC";
    public static final String BOOK = "BOOK";

    //public static String TAG_MUSICID = "SZMUSICID"; // 正在播放音乐id tag
    //public static volatile String CURRENT_MUSICID = null; // 正在播放音乐id

    // -1 购买的； 32 搜索结果；64 推荐
    public static final int BUYED = 0xffffffff;
    public static final int SEARCHED = 0x20;
    public static final int RECOMMONED = 0x40;

}
