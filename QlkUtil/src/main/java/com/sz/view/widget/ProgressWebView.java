package com.sz.view.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qlk.util.R;
import com.sz.mobilesdk.util.CommonUtil;

public class ProgressWebView extends WebView {
    private ProgressBar mProgressBar;

    private TextView titleText;

    /**
     * 设置标题textview
     *
     * @param textView
     */
    public void setTitleTextView(TextView textView) {
        this.titleText = textView;
    }

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) return;

        mProgressBar = new ProgressBar(context, null,
                android.R.attr.progressBarStyleHorizontal);
        int h = CommonUtil.dip2px(context, 3.0f);
        mProgressBar.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, h, 0, 0));

        Drawable drawable = context.getResources().getDrawable(
                R.drawable.xml_progressbar_states);
        mProgressBar.setProgressDrawable(drawable);
        addView(mProgressBar);

        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true); // 支持js
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true); // 支持缩放
        settings.setUseWideViewPort(true); // 支持调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 关闭webview中缓存
        settings.setLoadsImagesAutomatically(true); // 支持自动加载图片
        // settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL); //支持内容重新布局
        settings.supportMultipleWindows(); // 支持多窗口
        settings.setAllowFileAccess(true); // 设置可以访问文件
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过JS打开新窗口

        // 如果webView中需要用户手动输入用户名、密码或其他，则webview必须设置支持获取手势焦点。
        requestFocusFromTouch();

        setWebChromeClient(new WebChromeClient());
    }

    private class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                mProgressBar.setVisibility(GONE);
            } else {
                if (mProgressBar.getVisibility() == GONE)
                    mProgressBar.setVisibility(VISIBLE);
                mProgressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.d("", "onReceivedTitle: " + title);
            if (titleText != null)
                titleText.setText(TextUtils.isEmpty(title) ? "浏览信息" : title);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) mProgressBar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        mProgressBar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }


    /**
     * destroy webView
     */
    public void destroyed() {
        clearCache();
        clearCookies(getContext(), null);
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
        destroy();
    }

    /**
     * 清除缓存数据
     */
    public void clearCache() {
        //ctx.deleteDatabase("webview.db");
        //ctx.deleteDatabase("webviewCache.db");
        clearCache(true);
        clearSslPreferences();
        clearFormData();
        clearHistory();
        //releaseAllWebViewCallback();
    }

    /**
     * 清除cookie，清除全部vc=null；
     */
    public void clearCookies(Context ctx, ValueCallback<Boolean> vc) {
        @SuppressWarnings("unused")
        CookieSyncManager cookieSyncMng = CookieSyncManager.createInstance(ctx);
        CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.removeAllCookie();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(vc);
            //cookieManager.removeSessionCookies(vc);
        } else {
            cookieManager.removeAllCookie();
            //cookieManager.removeSessionCookie();
            if (vc != null) {
                vc.onReceiveValue(false);
            }
        }
    }

	/*
     * 尽可能防止webview内存泄露
	 */
//	private void releaseAllWebViewCallback()
//	{
//		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
//		{
//			try
//			{
//				Field field = WebView.class.getDeclaredField("mWebViewCore");
//				field = field.getType().getDeclaredField("mBrowserFrame");
//				field = field.getType().getDeclaredField("sConfigCallback");
//				field.setAccessible(true);
//				field.set(null, null);
//			} catch (NoSuchFieldException e)
//			{
//				e.printStackTrace();
//			} catch (IllegalAccessException e)
//			{
//				e.printStackTrace();
//			}
//		} else
//		{
//			try
//			{
//				Field sConfigCallback = Class.forName(
//						"android.webkit.BrowserFrame").getDeclaredField(
//						"sConfigCallback");
//				if (sConfigCallback != null)
//				{
//					sConfigCallback.setAccessible(true);
//					sConfigCallback.set(null, null);
//				}
//			} catch (NoSuchFieldException e)
//			{
//				e.printStackTrace();
//			} catch (ClassNotFoundException e)
//			{
//				e.printStackTrace();
//			} catch (IllegalAccessException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}

}
