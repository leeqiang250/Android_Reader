package cn.com.pyc.pbbonline.common;

/**
 * 请求响应的状态码 <br/>
 * 
 * @author hudq
 */
public final class Code
{
	public static final String _SUCCESS = "1001";		//成功！
	public static final String _8001 = "8001";			//请先领取分享
	public static final String _8002 = "8002";			//请先绑定分享
	public static final String _8003 = "8003";			//您尚未登录，请用您输入的手机号登录领取分享
	public static final String _8004 = "8004";			//您输入的手机号尚未注册，请先完成注册，再领取文件
	public static final String _8005 = "8005";			//请退出当前登录账户，用您输入的手机号登录领取分享
	public static final String _8006 = "8006";			//登录状态已失效，请重新登录
	public static final String _8007 = "8007";			//文件正在打包......

	public static final String _9001 = "9001";			//参数传递错误
	public static final String _9002 = "9002";			//分享不存在
	public static final String _9003 = "9003";			//该分享已经过期
	public static final String _9004 = "9004";			//该分享设备数超过限制
	public static final String _9005 = "9005";			//余额不足,请联系分享者进行充值
	public static final String _9006 = "9006";			//您无权领取该分享
	public static final String _9007 = "9007";			//您尚未登录，无法进行该操作
	public static final String _9008 = "9008";			//您未领取该分享
	public static final String _9009 = "9009";			//日期格式不正确
	public static final String _9010 = "9010";			//分享约束不存在
	public static final String _9011 = "9011";			//该分享没有分享给任何人
	public static final String _9012 = "9012";			//当前账户不存在
	public static final String _9013 = "9013";			//您尚未绑定该设备
	public static final String _9014 = "9014";			//不能绑定尚未接收的分享
	public static final String _9015 = "9015";			//程序逻辑错误
	public static final String _9016 = "9016";			//不能重复领取
	public static final String _9017 = "9017";			//不能重复绑定
	public static final String _9018 = "9018";			//保存消费流水失败
	public static final String _9019 = "9019";			//扣费失败
	public static final String _9020 = "9020";			//server error
	public static final String _9021 = "9021";			//can not find this share
	public static final String _9022 = "9022";			//领取人数超出

	public static final String _9101 = "9101";			//密码错误
	public static final String _9102 = "9102";			//用户未注册
	public static final String _9103 = "9103";			//该用户已经被注册
	public static final String _9104 = "9104";			//手机短信验证码发送失败
	public static final String _9105 = "9105";			//手机号格式错误
	public static final String _9106 = "9106";			//注册失败
	public static final String _9107 = "9107";			//图片验证码错误
	public static final String _9109 = "9109";			//用户未登录
	public static final String _9110 = "9110";			//参数传递错误，//从session中获取图片验证码失败


}
