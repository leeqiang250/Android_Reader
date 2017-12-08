package cn.com.pyc.web;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.TextView;

import com.sz.mobilesdk.util.SZLog;
import com.sz.view.widget.ProgressWebView;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.OpenPageUtil;
import cn.com.pyc.pbbonline.util.ViewHelp;

/**
 * app内置浏览网页浏览器
 *
 * @author hudq
 */
public class BrowserActivity extends Activity implements OnClickListener {
    private final String TAG = BrowserActivity.class.getSimpleName();
    private ProgressWebView mWebView;
    //private HighlightImageView mBackButton;
    //private HighlightImageView mForwardButton;
    //private HighlightImageView mRefreshButton;

    public static void openAppBrowser(Context context, String url) {
        context.startActivity(new Intent(context, BrowserActivity.class).putExtra("url", url));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        ViewHelp.showAppTintStatusBar(this);

        String url = getIntent().getStringExtra("url");
        SZLog.d(TAG, "url = " + url);

        mWebView = (ProgressWebView) findViewById(R.id.content_webView);
        mWebView.setTitleTextView((TextView) findViewById(R.id.title_tv));
        loadUrl(url);
        // View mOccupyView = findViewById(R.id.occupy_view);
        // View topBar = findViewById(R.id.include_topbar);
        // View footer = findViewById(R.id.linear_bootombar);
        findViewById(R.id.back_img).setOnClickListener(this);
        //mBackButton = (HighlightImageView) findViewById(R.id.img_goBack);
        //mBackButton.setOnClickListener(this);
        //mForwardButton = (HighlightImageView) findViewById(R.id.img_goForward);
        //mForwardButton.setOnClickListener(this);
        //HighlightImageView mRefreshButton = (HighlightImageView) findViewById(R.id
        // .img_refreshPage);
        //mRefreshButton.setOnClickListener(this);

        mWebView.setWebViewClient(new WebViewClient());
        //if (!"Letv".equalsIgnoreCase(Build.MANUFACTURER)) {
        //CustomWebFingerOnTouchListener onTouchListener = new CustomWebFingerOnTouchListener(
        //        this, footer);
        //mWebView.setOnTouchListener(onTouchListener);
        //}

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                SZLog.d(TAG, "onDownloadStart url:" + url);
                SZLog.d(TAG, "userAgent:" + userAgent + ";"
                        + "contentDisposition:" + contentDisposition + ";"
                        + "mimetype" + mimetype);
                OpenPageUtil.openBrowserOfSystem(BrowserActivity.this, url);
            }
        });
    }

    private void loadUrl(String url) {
        if (url == null) {
            url = "";
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        mWebView.loadUrl(url);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_img) {
            finish();
        }/*else if (id == R.id.img_goBack){
            if (mWebView.canGoBack()) mWebView.goBack();
        }else if (id == R.id.img_goForward){
            if (mWebView.canGoForward()) mWebView.goForward();
        }else if (id == R.id.img_refreshPage){
            mWebView.reload();
        }*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按返回键时， 不退出程序而是返回上一浏览页面：
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mWebView != null && mWebView.getSettings() != null) {
//            mWebView.onPause();
//            mWebView.getSettings().setJavaScriptEnabled(false);
//        }
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mWebView != null && mWebView.getSettings() != null) {
//            mWebView.onResume();
//            mWebView.getSettings().setJavaScriptEnabled(true);
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroyed();
    }

    // webView重载设置。方法根据需要选取重载。
    private class WebViewClient extends android.webkit.WebViewClient {

        // 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。
        // 这个函数我们可以做很多操作，比如我们读取到某些特殊的URL，于是就可以不打开地址，取消这个操作，进行预先定义的其他操作，这对一个程序是非常必要的。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 在当前webview处理链接。
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //mBackButton.setEnabled(mWebView.canGoBack());
            //mForwardButton.setEnabled(mWebView.canGoForward());
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //mBackButton.setEnabled(mWebView.canGoBack());
            //mForwardButton.setEnabled(mWebView.canGoForward());
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // 在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次。
            super.onLoadResource(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // // (报告错误信息)
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend,
                                       Message resend) {
            // //(应用程序重新请求网页数据)
            super.onFormResubmission(view, dontResend, resend);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url,
                                           boolean isReload) {
            // //(更新历史记录)
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            // 重写此方法可以让webview处理https请求。
            super.onReceivedSslError(view, handler, error);
            handler.proceed(); // 表示等待证书响应
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                                              HttpAuthHandler handler, String host, String realm) {
            // （获取返回信息授权请求）
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            // 重写此方法才能够处理在浏览器中的按键事件。
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            // （Key事件未被加载时调用）
            super.onUnhandledKeyEvent(view, event);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            // (WebView发生改变时调用)
            super.onScaleChanged(view, oldScale, newScale);
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm,
                                           String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
        }
    }
}
