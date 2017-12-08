package cn.com.pyc.web;


/*-
 * 与网页链接的部分都由此类完成
 * 注意一点：向网页传入重要数据时都要加密。加密的部分可以参照decoder.getHttpEncryptText的参数
 */

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qlk.util.base.BaseApplication;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;

import java.util.Locale;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.conn.UserConnect;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (pbb 所有访问网页的界面都会使用此activity打开。)
 * @date 2016/11/17 11:22
 */
public class WebActivity extends PbbBaseActivity implements OnClickListener {
	public static final String WEB_URL = "web_url";

	private WebView webView;

	private LinearLayout sucLayout;
	private LinearLayout failLayout;
	private ImageView imv_anim;
	AnimationDrawable anim;
	private TextView tvTitle;

	private UserInfo userInfo;
	private WebPage wp;
	private boolean loadSuc = true;
	private ImageView iv_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		ViewHelp.showAppTintStatusBar(this);
		init_data();
		init_view();
		init_listener();


		executeAnimProgress();// 开启等待动画
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setSupportZoom(true);
		webSettings.setUseWideViewPort(true);
		setWebView();
		webView.loadUrl(getUrl(wp));
		getNotice = wp.equals(WebPage.MyNoticeRecord);

	}


	private void init_data() {
		userInfo = UserDao.getDB(this).getUserInfo(); // 调用这个的时候UserDao应该已经实例化类，所以可以传入null
		wp = (WebPage) getIntent().getSerializableExtra(GlobalIntentKeys.BUNDLE_OBJECT_WEB_PAGE);
	}

	private void init_view() {
		// ((TextView) findViewById(R.id.ipt_txt_title)).setText(wp.title);
		tvTitle = (TextView) findViewById(R.id.aw_txt_title);
		tvTitle.setText(wp.title);
		webView = (WebView) findViewById(R.id.aw_webview);
		sucLayout = (LinearLayout) findViewById(R.id.aw_ll_no_net);
		failLayout = (LinearLayout) findViewById(R.id.aw_ll_failure);
		imv_anim = (ImageView) findViewById(R.id.aw_imv_anim_netless);
		iv_back = (ImageView) findViewById(R.id.iv_back);
//		imv_anim.setBackgroundResource(R.drawable.no_net_anim);
		imv_anim.setBackgroundResource(R.drawable.no_net_anim1);
		anim = (AnimationDrawable) imv_anim.getBackground();

		if (wp.title.equals("发现")) {
			iv_back.setVisibility(View.GONE);
			findViewById(R.id.aw_imb_refresh).setVisibility(View.GONE);
		}
	}

	private void init_listener() {
		findViewById(R.id.aw_imb_refresh).setOnClickListener(this);
		findViewById(R.id.aw_imv_failure).setOnClickListener(this);
	}


	private void executeAnimProgress() {
		anim.start();
	}

	private String getUrl(WebPage wp) {

		String url = null;
		switch (wp) {
			// 意见反馈
			case Idea:
				url = String.format(Constant.URL_IDEA, userInfo.getUserName(), userInfo.getEmail());    // 以前业务不加密
				break;
			// 激活与查询
			case PayRecordSingle:
				int id = getIntent().getIntExtra(GlobalIntentKeys.BUNDLE_DATA_EXTINFO, 0);
				String returnUrl = String.format(Locale.US, Constant.URL_MANUALATIVE, id);
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(returnUrl));
				break;
			// 手动激活记录
			case PayRecord:
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(Constant.URL_MANUALLIST));
				break;
			// 自由传播记录
			case FreeRecord:
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(Constant.URL_FREE_LIST));
				break;
			// 阅读记录
			case ReadRecord:
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(Constant.URL_READ_LIST));
				break;
			// 我的订单
			case MyOrderRecord:
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(Constant.URL_ORDER_FROM));
				break;
			// 我的消息
			case MyNoticeRecord:
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(Constant.URL_MESSAGE));
				break;
			// 订单编号链接
			case OrderNumber:
				String order = getIntent().getStringExtra(GlobalIntentKeys.BUNDLE_DATA_EXTINFO);
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(Constant.URL_ORDER_FROM + "#" + order));
				break;
			// 充值
			case Recharge:
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(Constant.URL_RECHARGE));
				break;
			// 账户余额
			case AccountBalance:
				url = String.format(Constant.URL_MODE_RETURN, XCoder.getHttpEncryptText(userInfo.getUserName()), XCoder.getHttpEncryptText(Constant.URL_BALANCE));
				break;
			//          推荐阅读
			//			case Recommend:
			//				url = String.format(Constant.URL_RECOMMEND, PhoneInfo.getUUID(this));
			//				break;

			default:
				url = wp.url;
				break;
		}

		return url;
	}



	@SuppressLint("JavascriptInterface")
	private void setWebView() {
        /*-
         * webview成功加载：onPageStarted-->onPageFinished
		 * 加载失败：onPageStarted-->onReceivedError-->onPageFinished
		 * 所以要设定标志loadSuc
		 */
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				setNetState(NetState.Connect);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				setNetState(loadSuc ? NetState.Success : NetState.Failure);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				loadSuc = false;
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				loadSuc = false;
			}

		});
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				return super.onJsAlert(view, url, message, result);
			}

		});

	}

	public enum WebPage {
		Recommend("推荐阅读", ""), //
		Help("功能介绍", Constant.WebHost + "/sj/Help.aspx"), //
		Idea("意见反馈", ""), //
		Privacy("条款和隐私政策", Constant.WebHost + "/sj/Rule.aspx"), //
		ChargeIntroduce("资费介绍", Constant.WebHost + "/sj/myspace/payinfo.aspx"), //
		ActiveCodeManager("激活码管理", Constant.WebHost + "/sj/myspace/myactivecode.aspx"), //
		FreeRecord("自由传播记录", ""), //
		PayRecordSingle("激活与查询", ""), //
		PayRecord("手动激活记录", ""), //
		ReadRecord("阅读记录", ""), //
		MyOrderRecord("我的订单", ""), //
		MyNoticeRecord("我的消息", ""), //
		OrderNumber("订单编号链接", ""), //
		AccountBalance("账户余额", ""), //
		Recharge("充值", ""), //
		//http://www.pyc.com.cn/  http://a.app.qq.com/o/simple.jsp?pkgname=cn.com.pyc.pbb
		Reader("PBB Reader",Constant.WebHost+"/Application/download.aspx"),
		PC("了解PC版",Constant.WebHost),
		Link("消息推送", ""),
		Discover("发现","http://kaifa3001.suizhi.com/DRM/mobile/store/discovery?username=18310329084&token=4e270073a649c8814b1ebc35e58ebbbd&currentPageNum=1&application_name=SuiZhi_for_Android&app_version=2.3.0&IMEI=864394024493894");

		WebPage(String title, String url) {
			this.title = title;
			this.url = url;
		}

		private final String title;
		private String url;

		public void setUrl(String url) {
			this.url = url;
		}

	}

	private enum NetState {
		Connect(View.GONE, View.VISIBLE, View.GONE), // 联网中
		Success(View.VISIBLE, View.GONE, View.GONE), // 联网成功
		Failure(View.GONE, View.GONE, View.VISIBLE); // 联网失败

		NetState(int webViewVisible, int sucVisible, int failureVisible) {
			web = webViewVisible;
			suc = sucVisible;
			fail = failureVisible;
		}

		final int web;
		final int suc;
		final int fail;
	}

	private void setNetState(NetState state) {
		webView.setVisibility(state.web);
		sucLayout.setVisibility(state.suc);
		failLayout.setVisibility(state.fail);
	}

	@Override
	public void onClick(View v) {
		webView.reload();
		loadSuc = true;
	}

	@Override
	public void onBackButtonClick(View v) {
		finish();
	}

	private long mExitTime;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
				loadSuc = true;
				return true;
			}

			/*songyumei 2017\7\10调整*/
			if (wp.title.equals("发现")){
				long curTime = System.currentTimeMillis();
				if (curTime - mExitTime < 2000 &&curTime - mExitTime > 0){
					((BaseApplication) getApplication()).safeExit();
				}else {
					GlobalToast.toastShort(this, "再按一次退出程序");
					mExitTime = curTime;
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean getNotice = false;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (getNotice) {
			GlobalTask.executeBackground(new Runnable() {

				@Override
				public void run() {
					new UserConnect(WebActivity.this).getNoticeNum();
				}
			});
		}
		webView.destroy();
	}

}
