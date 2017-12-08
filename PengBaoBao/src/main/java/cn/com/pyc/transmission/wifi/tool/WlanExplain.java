package cn.com.pyc.transmission.wifi.tool;

import cn.com.pyc.pbb.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class WlanExplain extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_wifi_explain);
	}

	public void exitbutton0(View v)
	{
		this.finish();
	}
}
