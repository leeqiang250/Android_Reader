package cn.com.pyc.suizhi.common;

/**
 * 配置设置类
 *
 * @author hudq
 */
public final class SZConfig {
    /**
     * 开发模式<br/>
     * <p>
     * 调试模式=true；发布线上版本=false关闭日志打印记录
     */
    public static final boolean DEVELOPER_MODE = false;

    //weixin 登录相关
    public static final String WEIXIN_APPID = "wx13032c3eb10bdafe";
    public static final String WEIXIN_APPSECRET = "130d6023d7cd25a30b4434ba967f7e86";
    public static final String WEIXIN_SCOPE = "snsapi_userinfo"; // snsapi_base;snsapi_userinfo

    //QQ 测试：101316083；QQ_KEY = 66a195dae7e4aed8896885858be624d8
    //QQ 正式：101320602，QQ_KEY = f0a6d4ad15f70b552a9d89fc609f88a6
    public static final String QQ_APPID = "101320602";
    public static final String QQ_SCOPE = "all"; // “get_user_info,add_t”；所有权限用“all”

    //登录使用盐值
    public static final String LOGIN_SALT = "A7070C3015B81AB0E5C1CF5F94D84BF7";

    //测试的password
    public static final String PASSWORD = "A7070C3015B81AB0E5C1CF5F94D84BF7";

    //下载最新版apk地址
    public static final String APK_NEW_URL = "http://114.112.104.137:8080/android/suizhi.apk";

    /**
     * 登录方式(区分账号登录和扫码登录)
     *
     * @author hudq
     */
    public static class LoginConfig {
        // 默认账号密码登录
        public static int type = DrmPat.LOGIN_GENERAL;
    }

}
