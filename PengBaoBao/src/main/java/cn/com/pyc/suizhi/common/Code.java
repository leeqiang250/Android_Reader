package cn.com.pyc.suizhi.common;

/**
 * 此类定义APP应用到的返回状态码
 * <p>
 * Created by hudq on 2017/3/30.
 */
public class Code {

    public static final String SUCCESS = "1000";            //成功

    //证书分离相关
    public static final String _13001 = "13001";            //参数传递错误

    public static final String _14001 = "14001";            //token验证失败
    public static final String _14002 = "14002";            //MD5验证错误

    public static final String _15001 = "15001";            //用户名不存在
    public static final String _15002 = "15002";            //密码错误
    public static final String _15003 = "15003";            //级联获取myProdcut错误
    public static final String _15004 = "15004";            //证书数据不存在
    public static final String _15005 = "15005";            //店铺不存在
    public static final String _15006 = "15006";            //获取短码失败
    public static final String _15007 = "15007";            //获取长码失败
    public static final String _15008 = "15008";            //获取文件列表失败

    public static final String _16001 = "16001";            //注册设备数超出
    public static final String _16002 = "16002";            //APP版本号太低
    public static final String _16003 = "16003";            //没有权限打开文件
    public static final String _16004 = "16004";            //已购买，但没到开始使用时间

    public static final String _16005 = "16005";            //文件已过期
    public static final String _16006 = "16006";            //设备未注册
    public static final String _16007 = "16007";            //文件格式不支持
    public static final String _16008 = "16008";            //产品更新失败
    public static final String _16009 = "16009";            //文件更新失败
    public static final String _16010 = "16010";            //打包中
    public static final String _16011 = "16011";            //请求文件服务器发生错误.错误原因见msg
    public static final String _16012 = "16012";            //文件已被更新，请重新下载
    public static final String _16014 = "16014";            //卖家禁止此文件在此端使用

    public static final String _19001 = "19001";            //系统未知异常
    public static final String _19002 = "19002";            //IO异常


}
