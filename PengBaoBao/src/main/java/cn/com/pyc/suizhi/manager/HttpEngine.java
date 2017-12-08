package cn.com.pyc.suizhi.manager;

import android.os.Bundle;
import android.text.TextUtils;

import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SZLog;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConstant;


/**
 * http 请求类
 *
 * @author hudaqiang
 */
public class HttpEngine {

    private static final String TAG = "http";

    /**
     * post请求,不添加请求头
     *
     * @param url      请求地址url
     * @param params   请求参数Bundle
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable post(String url, Bundle params,
                                           Callback.CommonCallback<String> callback) {
        return post(url, params, null, callback);
    }

    /**
     * post请求
     *
     * @param url      请求地址url
     * @param params   请求参数 Bundle
     * @param header   请求头
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable post(String url, Bundle params,
                                           Map<String, String> header,
                                           Callback.CommonCallback<String> callback) {
        RequestParams arg0 = createPostParamsUrl(url, params, header);
        return x.http().post(arg0, callback);
    }


    /**
     * post请求
     *
     * @param url      请求地址url
     * @param params   请求参数Map
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable post(String url, Map<String, String> params,
                                           Callback.CommonCallback<String> callback) {
        return post(url, params, null, callback);
    }

    /**
     * post 请求
     *
     * @param url      请求地址url
     * @param params   请求参数Bundle
     * @param header   请求头
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable post(String url, Map<String, String> params,
                                           Map<String, String> header,
                                           Callback.CommonCallback<String> callback) {
        RequestParams arg0 = createPostParamsUrl(url, params, header);
        return x.http().post(arg0, callback);
    }

    /**
     * get请求,不添加请求头header和params
     *
     * @param url      请求地址url
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable get(String url, Callback.CommonCallback<String> callback) {
        return get(url, new Bundle(), null, callback);
    }


    /**
     * get请求,不添加请求头header
     *
     * @param url      请求地址url
     * @param params   请求参数Bundle
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable get(String url, Bundle params,
                                          Callback.CommonCallback<String> callback) {
        return get(url, params, null, callback);
    }

    /**
     * get请求(可添加请求头header)
     *
     * @param url      请求地址url
     * @param params   请求参数 Bundle
     * @param header   请求头
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable get(String url, Bundle params,
                                          Map<String, String> header,
                                          Callback.CommonCallback<String> callback) {
        RequestParams arg0 = createGetParamsUrl(url, params, header);
        return x.http().get(arg0, callback);
    }


    /**
     * Get 请求
     *
     * @param url      请求地址url
     * @param params   请求参数 Map
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable get(String url, Map<String, String> params,
                                          Callback.CommonCallback<String> callback) {
        return get(url, params, null, callback);
    }

    /**
     * Get 请求
     *
     * @param url      请求地址url
     * @param params   请求参数 Map
     * @param header   请求头，没有则null
     * @param callback Callback.CommonCallback
     * @return Callback.Cancelable
     */
    public static Callback.Cancelable get(String url, Map<String, String> params,
                                          Map<String, String> header,
                                          Callback.CommonCallback<String> callback) {
        RequestParams arg0 = createGetParamsUrl(url, params, header);
        return x.http().get(arg0, callback);
    }

    //五个必须添加的参数
    private static RequestParams addBaseParams(RequestParams params) {
        //1.application_name
        String appName = params.getStringParameter("application_name");
        //DRMLog.e("appName： " + appName);
        if (appName == null || !DrmPat.APP_FULLNAME.equals(appName)) {
            if (HttpMethod.POST.toString().equals(params.getMethod().toString())) {
                params.addBodyParameter("application_name", DrmPat.APP_FULLNAME);
            } else {
                params.addQueryStringParameter("application_name", DrmPat.APP_FULLNAME);
            }
        }
        //2.app_version
        String version = params.getStringParameter("app_version");
        //DRMLog.e("version： " + version);
        if (version == null) {
            if (HttpMethod.POST.toString().equals(params.getMethod().toString())) {
                params.addBodyParameter("app_version", CommonUtil.getAppVersionName(SZApplication
                        .getInstance()));
            } else {
                params.addQueryStringParameter("app_version", CommonUtil.getAppVersionName(SZApplication
                        .getInstance()));
            }
        }
        //3.IMEI
        String imei = params.getStringParameter("IMEI");
        //DRMLog.e("imei： " + imei);
        if (imei == null) {
            if (HttpMethod.POST.toString().equals(params.getMethod().toString())) {
                params.addBodyParameter("IMEI", Constant.TOKEN);
            } else {
                params.addQueryStringParameter("IMEI", Constant.TOKEN);
            }
        }
        //4.用户名
        String name = SZConstant.getName();
        if (!TextUtils.isEmpty(name) && params.getStringParameter("username") == null) {
            if (HttpMethod.POST.toString().equals(params.getMethod().toString())) {
                params.addBodyParameter("username", name);
            } else {
                params.addQueryStringParameter("username", name);
            }
        }
        //5.token
        String token = SZConstant.getToken();
        if (!TextUtils.isEmpty(token) && params.getStringParameter("token") == null) {
            if (HttpMethod.POST.toString().equals(params.getMethod().toString())) {
                params.addBodyParameter("token", token);
            } else {
                params.addQueryStringParameter("token", token);
            }
        }
        return params;
    }

//    private static RequestParams addGetBaseParams(RequestParams params) {
//        params.addQueryStringParameter("application_name", DrmPat.APP_FULLNAME);
//        params.addQueryStringParameter("app_version", CommonUtil.getAppVersionName(App
//                .getInstance()));
//        params.addQueryStringParameter("IMEI", Constant.IMEI);
//        String name = Constant.getName();
//        if (!TextUtils.isEmpty(name))
//            params.addQueryStringParameter("username", name);
//        String token = Constant.getToken();
//        if (!TextUtils.isEmpty(token))
//            params.addQueryStringParameter("token", token);
//        return params;
//    }

    /**
     * Post: Params
     *
     * @param url    请求地址url
     * @param params 请求参数Bundle
     * @param header 请求头，Map集合，为空则不传
     * @return RequestParams
     */
    private static RequestParams createPostParamsUrl(String url, Bundle params,
                                                     Map<String, String> header) {
        SZLog.d(TAG, "POST: " + url);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setMethod(HttpMethod.POST);
        requestParams.setConnectTimeout(30 * 1000);
        requestParams.setUseCookie(false);

        if (header != null) {
            for (Entry<String, String> e : header.entrySet()) {
                requestParams.addHeader(e.getKey(), e.getValue());
                SZLog.d("header", e.getKey() + " = " + e.getValue());
            }
        }
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                Object obj = params.get(key);
                String value = String.valueOf(params.get(key));
                if (!TextUtils.isEmpty(value)) {
                    // 添加到请求body体的参数, 只有POST, PUT, PATCH, DELETE请求支持.
                    requestParams.addBodyParameter(key, String.valueOf(params.get(key)));
                    SZLog.d("params", key + " = " + obj);
                }
            }
        }
        addBaseParams(requestParams);
        SZLog.d(TAG, "RequestParams: "
                + requestParams.getBodyParams().toString());
        return requestParams;
    }

    /**
     * Post: params
     *
     * @param url    请求地址url
     * @param params 请求参数Map
     * @param header 请求头，为空则不传
     * @return RequestParams
     */
    private static RequestParams createPostParamsUrl(String url, Map<String, String> params,
                                                     Map<String, String> header) {
        SZLog.d(TAG, "POST: " + url);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setMethod(HttpMethod.POST);
        requestParams.setConnectTimeout(30 * 1000);
        requestParams.setUseCookie(false);
        if (header != null) {
            for (Entry<String, String> e : header.entrySet()) {
                requestParams.addHeader(e.getKey(), e.getValue());
                SZLog.d("header", e.getKey() + " = " + e.getValue());
            }
        }
        if (params != null && !params.isEmpty()) {
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                Object key = entry.getKey();
                String val = String.valueOf(entry.getValue());
                if (!TextUtils.isEmpty(val)) {
                    // 添加到请求body体的参数, 只有POST, PUT, PATCH, DELETE请求支持.
                    requestParams.addBodyParameter(String.valueOf(key), val);
                    SZLog.d("params", key + " = " + val);
                }
            }
        }
        addBaseParams(requestParams);
        SZLog.d(TAG, "RequestParams: " + requestParams.toString());
        return requestParams;
    }

    /**
     * Get：Params
     *
     * @param url    请求地址url
     * @param params 参数 Bundle
     * @param header 请求头，Map集合，为空则不传
     * @return RequestParams
     */
    private static RequestParams createGetParamsUrl(String url, Bundle params,
                                                    Map<String, String> header) {
        SZLog.d(TAG, "GET: " + url);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setMethod(HttpMethod.GET);
        requestParams.setConnectTimeout(30 * 1000);
        requestParams.setUseCookie(false);
        if (header != null) {
            for (Entry<String, String> e : header.entrySet()) {
                requestParams.addHeader(e.getKey(), e.getValue());
                SZLog.d("header", e.getKey() + " = " + e.getValue());
            }
        }
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                String value = String.valueOf(params.get(key));
                if (!TextUtils.isEmpty(value)) {
                    // 加到url里的参数, http://xxxx/s?key=value&key2=value2
                    requestParams.addQueryStringParameter(key, value);
                    SZLog.d("params", key + " = " + value);
                }
            }
        }
        addBaseParams(requestParams);
        SZLog.d(TAG, "RequestParams: " + requestParams.toString());
        return requestParams;
    }

    /**
     * Get: params
     *
     * @param url    请求地址url
     * @param params 请求参数Map
     * @param header 请求头，为空则不传
     * @return RequestParams
     */
    private static RequestParams createGetParamsUrl(String url, Map<String, String> params,
                                                    Map<String, String> header) {
        SZLog.d(TAG, "GET: " + url);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setMethod(HttpMethod.GET);
        requestParams.setConnectTimeout(30 * 1000);
        requestParams.setUseCookie(false);
        if (header != null) {
            for (Entry<String, String> e : header.entrySet()) {
                requestParams.addHeader(e.getKey(), e.getValue());
                SZLog.d("header", e.getKey() + " = " + e.getValue());
            }
        }
        if (params != null && !params.isEmpty()) {
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                Object key = entry.getKey();
                String val = String.valueOf(entry.getValue());
                if (!TextUtils.isEmpty(val)) {
                    requestParams.addQueryStringParameter(String.valueOf(key), val);
                    SZLog.d("params", key + " = " + val);
                }
            }
        }
        addBaseParams(requestParams);
        SZLog.d(TAG, "RequestParams: " + requestParams.toString());
        return requestParams;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////同步请求，需在非主线程使用///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 同步get请求，不传参数
     *
     * @param url 请求地址url
     * @return String
     */
    public static String getSyncString(String url) {
        return getSyncString(url, null);
    }

    /**
     * 同步get请求，传参数
     *
     * @param url    请求地址url
     * @param params 请求参数Bundle
     * @return String
     */
    public static String getSyncString(String url, Bundle params) {
        return getSync(url, params, String.class);
    }

    /**
     * get请求，同步方式
     */
    private static <T> T getSync(String url, Bundle params, Class<T> resultType) {
        RequestParams arg0 = createGetParamsUrl(url, params, null);
        try {
            return x.http().getSync(arg0, resultType);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
     * 同步请求
     *
     * @param url
     * @return
     */
//    public static String getSyncMapString(String url) {
//        return getSyncMapString(url, null);
//    }

    /**
     * 同步请求
     *
     * @param url    请求地址url
     * @param params 请求参数Map
     * @return String
     */
    public static String getSyncMapString(String url, Map<String, String> params) {
        return getSyncMap(url, params, String.class);
    }

    /**
     * get请求
     */
    private static <T> T getSyncMap(String url, Map<String, String> params, Class<T> resultType) {
        RequestParams arg0 = createGetParamsUrl(url, params, null);
        try {
            return x.http().getSync(arg0, resultType);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 取消对应的http请求
     */
    public static void cancelHttp(Callback.Cancelable cancelable) {
        if (cancelable != null) {
            if (!cancelable.isCancelled()) {
                cancelable.cancel();
            }
            cancelable = null;
        }
    }
}
