package cn.com.pyc.suizhi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.view.widget.ProgressWebView;

import java.util.Map;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.pay.OrderInfoUtil2_0;
import cn.com.pyc.pay.PayResult;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.key.KeyActivity;
import de.greenrobot.event.EventBus;


/**
 * Created by hudaqiang on 2017/9/1.
 */

public class SZWebViewActivity extends ExtraBaseActivity {

    private static final String TAG = "szWeb";
    private static final String TEMP_WEB_URL = "temp_web_url";
    private ProgressWebView mWebView;
//    private Handler mHandler = new Handler();
    private boolean gotoLogin = false;
    private boolean discoverBuy = false;
    private String url;

    public static final String RSA2_PRIVATE = "";
    public static final String RSA_PRIVATE = "";
    private static final int SDK_PAY_FLAG = 1;
    /** 支付宝支付业务：入参app_id */
    public static final String APPID = "";

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(SZWebViewActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(SZWebViewActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                default:
                    break;
            }
        };
    };
    //注销，通知清除cookie和cache
    public void onEventMainThread(ConductUIEvent event) {
        if (mWebView != null && event.getType() == BaseEvent.Type.UI_BROWSER_FINISH) {
            mWebView.clearCookies(this, null);
            mWebView.clearCache();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sz_webview);
        ViewHelp.showAppTintStatusBar(this);

        url = getIntent().getStringExtra("url");
        discoverBuy = getIntent().getBooleanExtra("discover_buy", false);
        Log.d(TAG, "url = " + url + ", discover_buy = " + discoverBuy);

        mWebView = (ProgressWebView) findViewById(R.id.sz_webView);
        mWebView.setTitleTextView((TextView) findViewById(cn.com.pyc.pbb.reader.R.id.title_tv));
        findViewById(cn.com.pyc.pbb.reader.R.id.back_img).setOnClickListener(new View
                .OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SPUtil.save(TEMP_WEB_URL, url); //临时保存一个url
        mWebView.loadUrl(url);
        mWebView.addJavascriptInterface(new JsClassHookInterface(), "androidJsControl");
        mWebView.setWebViewClient(new WebViewClient()); //添加客户端支持
    }

    // 定义JS需要调用的方法
    // 被JS调用的方法必须加入@JavascriptInterface注解
    public final class JsClassHookInterface {

        //支付完成,去逛逛----发现页
        @JavascriptInterface
        public void gotoDiscoveryView_Android() {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    //通知homeUI,切换到tab 1;
                    EventBus.getDefault().post(new ConductUIEvent((BaseEvent.Type.UI_HOME_TAB_1)));
                }
            }, 500);
        }

        //我的内容
        @JavascriptInterface
        public void gotoContentView_Android() {
        }

        //没登录去登录页面
        @JavascriptInterface
        public void gotoLoginView_Android() {
            gotoLogin = true;
            Intent intent = new Intent(SZWebViewActivity.this, KeyActivity.class);
            intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN);
            startActivityForResult(intent, 10);
        }

        //支付完成，去阅读(目前跳转到主页面的列表)
        @JavascriptInterface
        public void gotoReadView_Android(String myProId) {
            //通知主页面刷新列表
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    //通知homeUI,切换到tab2;
                    EventBus.getDefault().post(new ConductUIEvent((BaseEvent.Type.UI_HOME_TAB_2)));
                }
            }, 500);
        }

        @JavascriptInterface
        public void payWX(String myProId) {
            //TODO
        }
        @JavascriptInterface
        public void AliPay(String myProId) {
            payV2();
        }
    }

    //H5转Native支付
    private void fetchOrderFromH5Url(String url) {
        final PayTask task = new PayTask(SZWebViewActivity.this);
        final String ex = task.fetchOrderInfoFromH5PayUrl(url);
        if (!TextUtils.isEmpty(ex)) {
            //Log.d(TAG, "paytask::" + ex);
            new Thread(new Runnable() {
                public void run() {
                    final H5PayResultModel result = task.h5Pay(ex, true);
                    final String returnUrl = result.getReturnUrl();
                    Log.d(TAG, "pay—return:" + returnUrl + "|—code: " + result.getResultCode());
                    if (!TextUtils.isEmpty(returnUrl)) {
                        SZWebViewActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.loadUrl(returnUrl);
                            }
                        });
                    }
                }
            }).start();
        } else {
            mWebView.loadUrl(url);
        }
    }


    // webView重载设置。方法根据需要选取重载。
    private class WebViewClient extends android.webkit.WebViewClient {
        // 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。
        // 这个函数我们可以做很多操作，比如我们读取到某些特殊的URL，于是就可以不打开地址，取消这个操作，进行预先定义的其他操作，这对一个程序是非常必要的。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            SZLog.i("shouldOverrideUrl: " + url);
            SPUtil.save(TEMP_WEB_URL, url); //临时保存一个url
            if (url.startsWith("mqqwpa://")) {
                //例如：mqqwpa://im/chat?chat_type=wpa&uin=1002164327&version=1
                if (CommonUtil.isQQClientAvailable(SZWebViewActivity.this)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    showToast("请先安装QQ客户端");
                }
            } else {
                fetchOrderFromH5Url(url);
            }
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果发现页去购买，则不清除cookie，否则清除
        if (!discoverBuy) {
            mWebView.destroyed();
        }
        mHandler.removeCallbacksAndMessages(null);
        SPUtil.remove(TEMP_WEB_URL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
//            mWebView.onPause();
            mWebView.loadUrl(url);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!gotoLogin) return;
        if (mWebView == null) return;
        String overrideUrl = (String) SPUtil.get(TEMP_WEB_URL, "");
        if (overrideUrl != null && !overrideUrl.startsWith("mqqwpa://")) {
            String url = SZDiscoverActivity.getFullUrlByToken(overrideUrl);
            Log.d(TAG, "onRestart: " + url);
            if (!TextUtils.isEmpty(url)) {
                mWebView.loadUrl(url);
            }
        }
    }

    /**
     * 支付宝支付业务
     *
     */
    public void payV2() {
        /*if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))) {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            //
                            finish();
                        }
                    }).show();
            return;
        }*/
        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, rsa2);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);

        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
        final String orderInfo = orderParam + "&" + sign;

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {//调用支付宝的SDK的支付功能
                PayTask alipay = new PayTask(SZWebViewActivity.this);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);//支付宝返回支付的结果通过handler同步到主线程
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
}
