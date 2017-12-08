package com.sz.mobilesdk.util;

import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

/**
 * online时使用的接口地址管理类
 */
public class APIUtil {

    //请求服务器，true:13服；false: 正式服
    private static final boolean API_DEBUG = com.qlk.util.BuildConfig.DEBUG;

    /**
     * 实际部署服务器主机名
     *
     * @return eg: http://sz.com.cn:8567
     */
    @Deprecated
    private static String getActionPrefix() {
        String hostName = (String) SPUtil.get(Fields.FIELDS_SCAN_HOST, "");
        String portName = (String) SPUtil.get(Fields.FIELDS_SCAN_PORT, "");
        return "http://" + hostName + ":" + portName;
    }

    /**
     * 下载某个文件夹接口: <br/>
     * <br/>
     * account/downloadProductDevice2/{username}/{productId}/{device} <br/>
     * url中各个值的含义： <br/>
     * username ：用户名 <br/>
     * productId: 分享id <br/>
     * device ： 设备类型，目前都是 android，用来选择加密算法使用的。
     *
     * @param myProductId 产品id
     * @return String: request URL；eg:
     * http://sz.com.cn:8654/PBBOnline/account/downloadProductDevice2
     * /s001/3f9007b9-fc15/android?token=124563215
     */
    @Deprecated
    public static String getDownloadProductsUrl(String myProductId) {
        String name = (String) SPUtil.get(Fields.FIELDS_USER_NAME, "");
        String prefix = getActionPrefix()
                + "/PBBOnline/account/downloadProductDevice2";
        return prefix + "/" + name + "/" + myProductId + "/" + "android?token=" + Constant.TOKEN;
    }

    /**
     * 通过http路径获取ftp下载路径
     *
     * @param httpUrl http请求URL
     * @return 成功返回ftpUrl，失败返回HttpStatus.SC_BAD_REQUEST = 400；
     */
    @Deprecated
    public static String getFTPUrlByHttpUrl(String httpUrl) {
        HttpGet httpGet = new HttpGet(httpUrl);
        HttpClient httpclient = getHttpClient();
        // HttpStatus.SC_OK表示连接成功
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 取得返回的字符串
                return EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * 获取分享基本信息 <br/>
     * <p>
     * <br/>
     * 传递参数：<br/>
     * token 设备序列号<br/>
     * id 分享id <br/>
     * username 用户名称
     *
     * @return String: request URL
     */
    @Deprecated
    public static String getDistributeInfoUrl() {
        return getActionPrefix() + "/PBBOnline/client/content/getDistributeInfo";
    }

    /**
     * 文件列表（主界面显示文件夹列表接口） <br/>
     * <p>
     * <br/>
     * 传递参数：<br/>
     * token 设备序列号<br/>
     * id 分享id
     *
     * @return String: request URL
     */
    @Deprecated
    public static String getProductInfoUrl() {
        return getActionPrefix() + "/PBBOnline/client/content/getProductInfo";
    }

    /**
     * 绑定设备 <br/>
     * <br/>
     * <p>
     * 传递参数：<br/>
     * token 设备序列号<br/>
     * id 分享id
     *
     * @return String: request URL
     */
    @Deprecated
    public static String getBindDeviceUrl() {
        return getActionPrefix() + "/PBBOnline/client/content/bindDevice";
    }

    private static volatile HttpClient customerHttpClient;

    @Deprecated
    private static synchronized HttpClient getHttpClient() {
        if (null == customerHttpClient) {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, Fields.UTF_8);
            HttpProtocolParams.setUseExpectContinue(params, true);
            HttpProtocolParams.setUserAgent(params, "");
            ConnManagerParams.setTimeout(params, 60 * 1000);        // 从连接池中获取连接超时
            HttpConnectionParams
                    .setConnectionTimeout(params, 30 * 1000);       // 连接超时
            HttpConnectionParams.setSoTimeout(params, 60 * 1000);   // 请求超时
            // 设置我们的httpclient支持http和https两种连接方式
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", PlainSocketFactory
                    .getSocketFactory(), 443));
            ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(
                    params, schemeRegistry);
            customerHttpClient = new DefaultHttpClient(connectionManager,
                    params);
        }
        return customerHttpClient;
    }

    // ////////////////////////////////////////////////////////
    // /////////////////PBBOnline new interface////////////////
    // ////////////////////////////////////////////////////////

    /**
     * TODO:部署的服务器环境
     *
     * @return String: request Server
     */
    private static String getDeployServer() {
        // 正式环境： http://on.pyc.com.cn； ip:114.112.104.139
        // 测试环境： http://192.168.85.13
        // 本地调试： http://192.168.85.55:8080
        return API_DEBUG ? "http://192.168.85.13" : "http://on.pyc.com.cn";
    }

    /**
     * 扫描二维码分享url
     *
     * @return String: request URL
     */
    public static String getShareInfoUrl() {
        return getDeployServer() + "/PBBOnline/client/content/getShareInfo";
    }

    /**
     * 扫描二维码二次分享url
     *
     * @return String: request URL
     */
    public static String getShareInfo2Url() {
        return getDeployServer() + "/PBBOnline/client/content/getShareInfo2";
    }

    /**
     * 获取手机验证码的url路径<br/>
     *
     * @return String: 获取手机验证码的接口URL
     */
    public static String getPhoneVerificationCode() {
        return getDeployServer() + "/PBBOnline/client/security/registerSendValidateCode";
    }

    /**
     * 注册接口<br/>
     *
     * @return String: request URL
     */
    public static String getRegisterPath() {
        return getDeployServer() + "/PBBOnline/client/security/register";
    }

    /**
     * 退出登陆接口<br/>
     * sdz
     *
     * @return String: request URL
     */
    public static String getExitLoginPath() {
        return getDeployServer() + "/PBBOnline/client/security/logout";
    }

    /**
     * 短信验证登陆接口<br/>
     * sdz
     *
     * @return String: request URL
     */
    public static String getLoginVerifyPath() {
        return getDeployServer() + "/PBBOnline/client/security/checkLogin";
    }

    /**
     * 登陆接口<br/>
     * sdz
     *
     * @return String: request URL
     */
    public static String getLoginPath() {
        return getDeployServer() + "/PBBOnline/client/security/login";
    }

    /**
     * 检查下载的权限和条件
     *
     * @return String: request URL
     */
    public static String getDownloadCheckUrl() {
        return getDeployServer() + "/PBBOnline/client/content/downloadCheck";
    }

    /**
     * 綁定设备
     *
     * @return String: request URL
     */
    public static String bindDevicesUrl() {
        return getDeployServer() + "/PBBOnline/client/content/bindDevice";
    }

    /**
     * 检验手机号是否是被分享者
     *
     * @return String: request URL
     */
    public static String receiverByPhoneUrl() {
        return getDeployServer() + "/PBBOnline/client/content/receiveByPhone";
    }

    /**
     * updateTags（添加设备的极光分组）
     *
     * @return String: request URL
     */
    public static String updateTagsByJPush() {
        return getDeployServer() + "/PBBOnline/client/content/updateTags";
    }

    /**
     * 获取所有我接收的分享getAllReceiveShares
     *
     * @return String: request URL
     */
    public static String getAllReceiveSharesUrl() {
        return getDeployServer() + "/PBBOnline/client/content/getAllReceiveShares";
    }

    /**
     * 根据分享文件夹ID获取分享文件getShareFile
     *
     * @return String: request URL
     */
    public static String getShareFileUrl() {
        return getDeployServer() + "/PBBOnline/client/content/getShareFile";
    }

    /**
     * 根据短码请求分享的详情 getShareInfoByCode
     *
     * @return String: request URL
     */
    public static String getShareInfoByCodeUrl() {
        return getDeployServer() + "/PBBOnline/client/content/getShareInfoByCode";
    }

    /**
     * 猜您喜欢
     *
     * @return String
     */
    @Deprecated
    public static String getRecommandSearchUrl() {
        //String server = API_DEBUG ? "http://192.168.85.13" : "http://www.suizhi.net";
        return "http://www.suizhi.com" + "/DRM/client/product/search";
    }

    /**
     * 产品详情页链接
     *
     * @param proId 产品Id
     * @param logId 搜索结果返回的参数
     * @return String
     */
    @Deprecated
    public static String getProDetailsUrl(String proId, String logId) {
        //String server = API_DEBUG ? "http://192.168.85.13" : "http://www.suizhi.net";
        return "http://www.suizhi.com" + "/DRM/proQuery/productShow/proDetails/" + proId +
                "?accessLogId=" + logId;
    }

}
