package cn.com.pyc.sm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.qlk.util.global.GlobalTask;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.conn.SmResult;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.reader.R;
/**
* @Description: (离线验证)
* @author 李巷阳
* @date 2016/12/12 11:10
*/
public class VerifyOfflineActivity extends PbbBaseActivity
{

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_verify_offline);
		findViewById(R.id.avo_btn_sure).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						GlobalTask.executeNetTask(VerifyOfflineActivity.this,
								new Runnable()
								{

									@Override
									public void run()
									{
										SmInfo smInfo = (SmInfo) getIntent()
												.getSerializableExtra(
														GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
										SmResult sr = new SmConnect(
												getApplicationContext())
												.verifyOffline(smInfo, true);
										if (sr.canOpen())
										{
											Intent intent = GlobalIntentKeys
													.reUseIntent(
															VerifyOfflineActivity.this,
															SmReaderActivity.class);
											startActivity(intent);
											finish();
										}
										else
										{
											// 如果是网络原因，则仍可以继续验证
											if (sr.getSuc() > 0)
											{
												finish();		
											}
										}
									}
								});
					}
				});

		findViewById(R.id.avo_btn_cancel).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						finish();
					}
				});
	}
}
