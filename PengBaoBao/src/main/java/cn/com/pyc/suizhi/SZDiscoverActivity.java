package cn.com.pyc.suizhi;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.SZApplication;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.widget.ProgressWebView;

import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.event.BaseEvent;
import cn.com.pyc.bean.event.ConductUIEvent;
import cn.com.pyc.pbb.R;
import cn.com.pyc.suizhi.common.DrmPat;
import cn.com.pyc.suizhi.common.SZConstant;
import cn.com.pyc.suizhi.util.OpenUIUtil;
import cn.com.pyc.suizhi.util.SZAPIUtil;
import cn.com.pyc.utils.ViewUtil;
import de.greenrobot.event.EventBus;

/**
 * 发现
 */
public class SZDiscoverActivity extends ExtraBaseActivity {

    private static final String TAG = "SZDiscover";
    private SwipeToLoadLayout mSwipeLayout;
    private ProgressWebView mWebView;
    private View mNoNetLayout;

    //更新发现页
    public void onEventMainThread(ConductUIEvent event) {
        if (mWebView != null && event.getType() == BaseEvent.Type.UPDATE_DISCOVER) {
            mWebView.clearCookies(this, new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    mWebView.clearCache();
                    loadWebUI();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        EventBus.getDefault().register(this);
        ((TextView) findViewById(R.id.title_tv)).setText(getString(R.string.discover_label));
        findViewById(R.id.back_img).setVisibility(View.INVISIBLE);
        mSwipeLayout = ((SwipeToLoadLayout) findViewById(R.id.swipeToLoadLayout));
        mWebView = ((ProgressWebView) findViewById(R.id.swipe_target));
        mNoNetLayout = findViewById(R.id.sd_ll_failure);
        mSwipeLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (CommonUtil.isNetConnect(SZDiscoverActivity.this)) {
                    loadWebUI();
                } else {
                    loadOffUI();
                }
                mSwipeLayout.setRefreshing(false);
            }
        });
        if (CommonUtil.isNetConnect(this)) {
            loadWebUI();
        } else {
            loadOffUI();
        }
    }

    private void loadWebUI() {
        ViewUtil.hideWidget(mNoNetLayout);
        ViewUtil.showWidget(mWebView);
        mWebView.loadUrl(SZAPIUtil.getDiscoverUrl(1));
        Log.d(TAG, "loadUrl: " + SZAPIUtil.getDiscoverUrl(1));
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "click url: " + url);
                OpenUIUtil.openWebViewOfApp2Buy(SZDiscoverActivity.this, url);
                return true;
            }
        });
    }

    private void loadOffUI() {
        ViewUtil.hideWidget(mWebView);
        ViewUtil.showWidget(mNoNetLayout);
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        UIHelper.showExitTips(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroyed();
        EventBus.getDefault().unregister(this);
    }

    //通过前置存在的url获取整个带token的url
    public static String getFullUrlByToken(String prefixUrl) {
        String userName = SZConstant.getName(), token = SZConstant.getToken();
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(token)) {
            String url = prefixUrl
                    + "?username=" + userName
                    + "&token=" + token
                    + "&application_name=" + DrmPat.APP_FULLNAME
                    + "&app_version=" + CommonUtil.getAppVersionName(SZApplication.getInstance())
                    + "&IMEI=" + Constant.TOKEN;
            SZLog.e("", "full url: " + url);
            return url;
        }
        return null;
    }
}
