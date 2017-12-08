package cn.com.pyc.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 中转类：由网页启动此类，再转至主页
 * @author jerry
 *
 */
public class TransferActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		startActivity(new Intent(this, HomeActivity.class));
		finish();
		super.onCreate(savedInstanceState);
	}
}
