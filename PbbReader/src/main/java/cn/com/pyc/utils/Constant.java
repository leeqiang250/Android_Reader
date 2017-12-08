package cn.com.pyc.utils;

/**
 * 主要存放一些服务器地址
 */
public class Constant
{
	public static final int C_FALSE = 0;
	public static final int C_TRUE = 1;

	//http://www.pyc.com.cn/
	private static final String RealeaseWebHost = "http://www.pyc.com.cn";
	private static final String RealeaseServerHost = "s.pyc.com.cn";
//	private static final String RealeaseServerHost = "www.pyc.com.cn";
	private static final String TestWebHost = "http://192.168.80.104";	// 广告和个人中心
	private static final String TestServerHost_31 = "192.168.86.31";	// 通讯协议测试服务器
	private static final String TestServerHost2 = "192.168.85.78";	// 连工电脑
	public static final String WebHost = RealeaseWebHost;
	public static final String ServerHost = RealeaseServerHost;
	
	
	public static final String TestUserSourceHost = "http://192.168.80.104:8084/"; 
	public static final String TestUserTokenHost = "http://192.168.80.104:8083/token";
	public static final String RealeaseUserSourceHost = "http://api.pyc.com.cn/";
	public static final String RealeaseUserTokenHost = "http://login.pyc.com.cn/token";
	
	public static final String UserSourceHost = RealeaseUserSourceHost;
	public static final String UserTokenHost = RealeaseUserTokenHost;
	

//	public static final String URL_ARTICAL = "http://api.pyc.com.cn/api/v1/articlenum?hdid=%s";
//	public static final String URL_RECOMMEND = WebHost + "/sj/recommend.aspx?hdid=%s";
	public static final String URL_IDEA = WebHost + "/sj/Feedback.aspx?logname=%s&email=%s";
	public static final String URL_MODE_RETURN = WebHost + "/does.aspx?mode=%s&returnurl=%s";
	public static final String URL_MANUALATIVE = WebHost + "/sj/myspace/manualactive.aspx?id=%d";
	public static final String URL_MANUALLIST = WebHost + "/sj/myspace/manuallist.aspx";
	public static final String URL_FREE_LIST = WebHost + "/sj/myspace/freelist.aspx";
	public static final String URL_READ_LIST = WebHost + "/sj/myspace/readlist.aspx";
	public static final String URL_ORDER_FROM = WebHost + "/sj/myspace/orderfrom.aspx";
	public static final String URL_MESSAGE = WebHost + "/sj/myspace/mymessage.aspx";
	public static final String URL_RECHARGE = WebHost + "/sj/myspace/Recharge.aspx";
	public static final String URL_BALANCE = WebHost + "/sj/myspace/balance.aspx";
//	public static final String WEIXIN_ID = "wx49b46f184e65e4de";
	public static final String WEIXIN_ID = "wx8e7cab3c5d3fbe7f";//Reader
//	public static final String QQ_ID = "100569483";
	public static final String QQ_ID = "1103998765";//Reader1103998765
}
