package cn.com.pyc.suizhi.util;

import android.text.TextUtils;

import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.util.CommonUtil;

import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConstant;

public class SZAPIUtil {

    private static final boolean API_DEBUG = cn.com.pyc.pbb.BuildConfig.DEBUG;

    /**
     * 连接服务器环境
     * <p>
     * 1.本地服：http://kaifa1001.suizhi.com    <br/>
     * 2.测试服：http://192.168.85.13           <br/>
     * 3.线上服：http://www.suizhi.com          <br/>
     *
     * @return
     */
    public static String getHost() {
//        return API_DEBUG ? "http://192.168.85.13:81" : "http://www.suizhi.com";
//        return API_DEBUG ? "http://192.168.81.36" : "http://www.suizhi.com";
//        return API_DEBUG ? "http://192.168.81.65" : "http://www.suizhi.com";
        return API_DEBUG ? "http://kaifa3001.suizhi.com" : "http://www.suizhi.com";
    }

    /**
     * 登录
     *
     * @return
     */
    @Deprecated
    public static String getLoginUrl() {
        return getHost() + "/DRM/client/account/login";
    }

    /**
     * 新登录接口
     *
     * @return
     */
    public static String getLoginV2Url() {
        return getHost() + "/DRM/client/account/loginv2";
    }

    /**
     * 注册接口
     *
     * @return
     */
    public static String getRegisterUrl() {
        return getHost() + "/DRM/client/account/regist";
    }

    /**
     * 注册获取验证码接口
     *
     * @return
     */
    public static String getRegisterCodeUrl() {
        return getHost() + "/DRM/client/account/getRegistValidateCode";
    }

    /**
     * 找回密码获取验证码接口
     *
     * @return
     */
    public static String getFindPassWordCodeUrl() {
        return getHost() + "/DRM/client/account/getRetrieveValidateCode";
    }

    /**
     * 找回密码接口
     *
     * @return
     */
    public static String getFindPassWord() {
        return getHost() + "/DRM/client/account/retrievePassword";
    }

    /**
     * 微信登录交互
     *
     * @return
     */
    public static String getWXLoginUrl() {
        return getHost() + "/DRM/client/account/wxlogin";
    }

    /**
     * qq登录交互
     *
     * @return
     */
    public static String getQQLoginUrl() {
        return getHost() + "/DRM/client/account/qqlogin";
    }

    /**
     * 注册设备信息
     *
     * @return
     */
    public static String getEquipmentInfoUrl() {
        return getHost()
                + "/DRM/client/userEquipment/setEquipmentInfoForClient";
    }

    /**
     * 主页获取产品列表
     *
     * @return
     */
    public static String getProductsListUrl() {
        return getHost() + "/DRM/client/product/list";
    }

    /**
     * 获取文件列表接口
     * <p>
     * 已废弃使用！
     *
     * @return
     */
    @Deprecated
    public static String getItemList() {
        return getHost() + "/DRM/client/product/getItemList";
    }


    /**
     * 获取文件列表接口(附加版本号)
     *
     * @return
     */
    public static String getItemVersionList() {
        return getHost() + "/DRM/client/product/getItemVersionList";
    }


    /**
     * 下载单个文件接口
     *
     * @return
     */
    @Deprecated
    public static String getDownloadItem() {
        return getHost() + "/DRM/client/downloadItem";
    }

    /**
     * 更新单个文件接口
     * <p>
     * 已废弃使用！
     *
     * @return
     */
    @Deprecated
    public static String getUpdateItem() {
        return getHost() + "/DRM/client/product/updateItem";
    }

    /**
     * 9.搜索接口
     *
     * @return
     */
    public static String getProductSearch() {
        return getHost() + "/DRM/client/product/search";
    }

    /**
     * 产品详情页
     * <p>
     *
     * @param proId       ${proId}是产品id；
     * @param accessLogId ${accesslogid}搜索结果返回的参数
     * @return
     */
    public static String getProductDetailUrl(String proId, String accessLogId) {
        return getHost() + "/DRM/proQuery/productShow/proDetails/" + proId
                + "?accessLogId=" + accessLogId
                + "&username=" + SZConstant.getName()
                + "&token=" + SZConstant.getToken()
                + "&application_name=" + DrmPat.APP_FULLNAME
                + "&app_version=" + CommonUtil.getAppVersionName(SZApplication.getInstance())
                + "&IMEI=" + Constant.TOKEN;
    }

    /**
     * 发现页面(未登录和登录)
     *
     * @param page
     * @return
     */
    public static String getDiscoverUrl(int page) {
        String superName = SZConstant.getName();
        String token = SZConstant.getToken();
        if (TextUtils.isEmpty(superName) || TextUtils.isEmpty(token)) {
            return getHost() + "/DRM/mobile/store/discovery"
                    + "?currentPageNum=" + page
                    + "&application_name=" + DrmPat.APP_FULLNAME
                    + "&app_version=" + CommonUtil.getAppVersionName(SZApplication.getInstance())
                    + "&IMEI=" + Constant.TOKEN;
        }
        return getHost() + "/DRM/mobile/store/discovery"
                + "?username=" + superName
                + "&token=" + token
                + "&currentPageNum=" + page
                + "&application_name=" + DrmPat.APP_FULLNAME
                + "&app_version=" + CommonUtil.getAppVersionName(SZApplication.getInstance())
                + "&IMEI=" + Constant.TOKEN;

    }

    /**
     * 获取下载歌词的Http URL
     *
     * @param lrcId 歌词id
     * @return
     */
    public static String getLrcHttpUrl(String lrcId) {
        return getHost() + "/DRM/client/downloadMusicLyric/" + lrcId;
    }

    /**
     * 强制更新App接口
     *
     * @return
     */
    public static String getForceUpdateUrl() {
        return getHost() + "/DRM/client/product/verifyAppVersion";
    }

    /**
     * 获取证书
     *
     * @return
     */
    public static String getFileCertificate() {
        return getHost() + "/DRM/client/downLoadMyProduct/getFileInfo";
    }

    /**
     * 获取文件下载地址
     *
     * @return
     */
    public static String getFileDownloadUrl() {
        return getHost() + "/DRM/client/downLoadMyProduct/downloadItem";
    }


    /**
     * 获取短码(分享此刻功能)
     *
     * @return
     */
    public static String getShortCodeUrl() {
        return getHost() + "/DRM/client/tools/getShortCode";
    }


    /**
     * 获取长码(分享此刻功能)
     *
     * @return
     */
    public static String getLongParameterUrl() {
        return getHost() + "/DRM/client/tools/getLongParameter";
    }

    /**
     * 验证item权限(分享此刻功能)
     *
     * @return
     */
    public static String getItemValidUrl() {
        return getHost() + "/DRM/client/product/isItemValid";
    }


    /**
     * 检查本地旧目录结构中已经下载的文件哪些属于当前登陆用户下的,即检查myProId是否有效
     *
     * @return
     */
    public static String checkAccountAndMyProductUrl() {
        return getHost() + "/DRM/client/product/checkAccountAndMyProduct";
    }

    /**
     * 店铺页面链接
     *
     * @return eg：
     * /DRM/proQuery/sellerStore/showStoreDetails/${sellerId}?username=&token
     * =&application_name=&app_version=&IMEI=&accessLogId=${accessLogId}
     */
    public static String getStoreShopUrl(String sellerId) {
        String superName = SZConstant.getName();
        String token = SZConstant.getToken();
        if (TextUtils.isEmpty(superName) || TextUtils.isEmpty(token)) {
            return getHost() + "/DRM/proQuery/sellerStore/showStoreDetails/" + sellerId
                    + "?application_name=" + DrmPat.APP_FULLNAME
                    + "&app_version=" + CommonUtil.getAppVersionName(SZApplication.getInstance())
                    + "&IMEI=" + Constant.TOKEN;
        }
        return getHost() + "/DRM/proQuery/sellerStore/showStoreDetails/" + sellerId
                + "?username=" + superName
                + "&token=" + token
                + "&application_name=" + DrmPat.APP_FULLNAME
                + "&app_version=" + CommonUtil.getAppVersionName(SZApplication.getInstance())
                + "&IMEI=" + Constant.TOKEN;
    }
}
