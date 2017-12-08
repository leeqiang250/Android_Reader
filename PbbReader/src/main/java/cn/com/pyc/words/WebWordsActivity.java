package cn.com.pyc.words;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.pyc.pbb.reader.R;

public class WebWordsActivity extends Activity
{
	private WebView webView;
	private TextView tv_title_word;
	private ImageButton imb_share;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final String urlWords = (String) getIntent().getExtras().get("wordsUrl");
		setContentView(R.layout.activity_web_words);

		tv_title_word = (TextView) findViewById(R.id.aww_txt_title);
		tv_title_word.setText("精彩博文");
		

		webView = (WebView) findViewById(R.id.aww_webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(urlWords);
		
		imb_share = (ImageButton) findViewById(R.id.aw_imb_share);
		imb_share.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
//				AndroidShare as = new AndroidShare(WebWordsActivity.this, "Share", urlWords);
//				as.show();
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("*/*");
//				Bundle extras = new Bundle();
//				extras.putString("url", urlWords);
				intent.putExtra("url", urlWords);
				startActivity(intent);
				
			}
		});
	}

	public void onBackButtonClick(View v)
	{
		finish();
	}
}
