package cn.com.pyc.global;

import android.os.Bundle;

import com.sz.mobilesdk.util.SZLog;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Map;
import java.util.Map.Entry;

/**
 * http 请求类 （依赖开源库xUtils3）
 * <p>
 * https://github.com/wyouflf/xUtils3/tree/master
 *
 * @author hudq
 */
public class GlobalHttp {

    private static final String TAG = "GHttp";

    /**
     * post请求,不添加请求头
     *
     * @param url
     * @param params
     * @param callback org.xutils.common.Callback.CommonCallback<T>
     * @return
     */
    public static Callback.Cancelable post(String url, Bundle params,
                                           Callback.CommonCallback<String> callback) {
        return post(url, params, null, callback);
    }

    /**
     * post请求
     *
     * @param url
     * @param params
     * @param header   请求头
     * @param callback org.xutils.common.Callback.CommonCallback<T>
     * @return
     */
    public static Callback.Cancelable post(String url, Bundle params, Map<String, String> header,
                                           Callback.CommonCallback<String> callback) {
        RequestParams arg0 = createPostParamsUrl(url, params, header);
        return x.http().post(arg0, callback);
    }

    /**
     * post请求（仅添加一个已知请求头from_client）<br/>
     * 用于pbbonline接口访问<br/>
     * (getShareInfo,getShareInfo2,bindDevice,login,register,
     * checkLogin需要添加一个header：from_client)
     *
     * @param url
     * @param params
     * @param callback org.xutils.common.Callback.CommonCallback<T>
     * @return
     */
    public static Callback.Cancelable postOn(String url, Bundle params,
                                             Callback.CommonCallback<String> callback) {
        RequestParams arg0 = createPostParamsHeaderUrl(url, params);
        return x.http().post(arg0, callback);
    }

    /**
     * get请求,不添加请求头header
     *
     * @param url
     * @param params
     * @param callback org.xutils.common.Callback.CommonCallback<T>
     * @return
     */
    public static Callback.Cancelable get(String url, Bundle params,
                                          Callback.CommonCallback<String> callback) {
        return get(url, params, null, callback);
    }

    /**
     * get请求
     *
     * @param url
     * @param params
     * @param header   请求头
     * @param callback org.xutils.common.Callback.CommonCallback<T>
     * @return
     */
    public static Callback.Cancelable get(String url, Bundle params, Map<String, String> header,
                                          Callback.CommonCallback<String> callback) {
        RequestParams arg0 = createGetParamsUrl(url, params, header);
        return x.http().get(arg0, callback);
    }

    /**
     * get请求（仅添加一个已知请求头from_client）<br/>
     * 用于pbbonline接口访问<br/>
     * (getShareInfo,getShareInfo2,bindDevice,login,register,
     * checkLogin需要添加一个header：from_client)
     *
     * @param url
     * @param params
     * @param callback org.xutils.common.Callback.CommonCallback<T>
     * @return
     */
    public static Callback.Cancelable getOn(String url, Bundle params,
                                            Callback.CommonCallback<String> callback) {
        RequestParams arg0 = createGetParamsHeaderUrl(url, params);
        return x.http().get(arg0, callback);
    }

    /**
     * 同步get请求
     *
     * @param url
     * @return
     */
    public static String getSyncString(String url) {
        return getSyncString(url, null);
    }

    /**
     * 同步get请求
     *
     * @param url
     * @param params
     * @return
     */
    public static String getSyncString(String url, Bundle params) {
        return getSync(url, params, String.class);
    }

    /**
     * get请求，同步方式
     *
     * @param url
     * @param params
     * @param resultType
     * @return
     */
    private static <T> T getSync(String url, Bundle params, Class<T> resultType) {
        try {
            RequestParams arg0 = createGetParamsUrl(url, params, null);
            return x.http().getSync(arg0, resultType);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Post： params
     *
     * @param url
     * @param params
     * @return 仅添加一个请求头from_client，（用于pbbonline接口访问）
     */
    private static RequestParams createPostParamsHeaderUrl(String url, Bundle params) {
        RequestParams requestParams = createPostParamsUrl(url, params, null);
        requestParams.setHeader("from_client", "pbbreader-android");
        return requestParams;
    }

    /**
     * Get： params
     *
     * @param url
     * @param params
     * @return 仅添加一个请求头from_client，（用于pbbonline接口访问）
     */
    private static RequestParams createGetParamsHeaderUrl(String url, Bundle params) {
        RequestParams requestParams = createGetParamsUrl(url, params, null);
        requestParams.setHeader("from_client", "pbbreader-android");
        return requestParams;
    }

    /**
     * Post: Params
     *
     * @param url
     * @param params
     * @param header 请求头，参数Map，为空则不传
     * @return
     */
    private static RequestParams createPostParamsUrl(String url, Bundle params,
                                                     Map<String, String> header) {
        SZLog.d(TAG, "url: " + url);
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
        if (params != null) {
            for (String key : params.keySet()) {
                Object obj = params.get(key);
                // 添加到请求body体的参数, 只有POST, PUT, PATCH, DELETE请求支持.
                requestParams.addBodyParameter(key, String.valueOf(params.get(key)));
                SZLog.d("params", key + " = " + obj);
            }
        }
        SZLog.d(TAG, "POST: " + requestParams.getBodyParams().toString());
        return requestParams;
    }

    /**
     * Get：Params
     *
     * @param url
     * @param params
     * @param header 请求头，参数Map，为空则不传
     * @return
     */
    private static RequestParams createGetParamsUrl(String url, Bundle params,
                                                    Map<String, String> header) {
        SZLog.i(TAG, "url: " + url);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setMethod(HttpMethod.GET);
        requestParams.setConnectTimeout(30 * 1000);
        requestParams.setUseCookie(false);
        if (header != null) {
            for (Entry<String, String> e : header.entrySet()) {
                requestParams.addHeader(e.getKey(), e.getValue());
                SZLog.i("header", e.getKey() + " = " + e.getValue());
            }
        }
        if (params != null) {
            for (String key : params.keySet()) {
                Object obj = params.get(key);
                // 加到url里的参数, http://xxxx/s?key=value&key2=value2
                requestParams.addQueryStringParameter(key, String.valueOf(params.get(key)));
                SZLog.i("params", key + " = " + obj);
            }
        }
        SZLog.i(TAG, "GET: " + requestParams.toString());
        return requestParams;
    }

    /**
     * 取消对应的http请求
     */
    public static void cancelHttp(Callback.Cancelable cancelable) {
        if (cancelable != null && !cancelable.isCancelled()) {
            cancelable.cancel();
            cancelable = null;
        }
    }
}
