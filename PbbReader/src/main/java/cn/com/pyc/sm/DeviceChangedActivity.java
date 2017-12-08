package cn.com.pyc.sm;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.pbb.reader.R;
/**
*
* @Description: (离线文件移动到其他设备上了)
* @author 李巷阳
* @date 2016/12/12 11:11
*/
public class DeviceChangedActivity extends PbbBaseActivity
{
	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_device_change);

		findViewById(R.id.adc_btn_yes).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				GlobalTask.executeNetTask(DeviceChangedActivity.this,
						new Runnable()
						{

							@Override
							public void run()
							{
//								clear();
								GlobalToast.toastShort(DeviceChangedActivity.this, "再次打开/阅读时进行申请");
								finish();
							}
						});
			}
		});

		findViewById(R.id.adc_btn_no).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}

//	// 清空方法
//	private void clear()
//	{
//		SmInfo smInfo = (SmInfo) getIntent().getSerializableExtra(
//				GlobalIntentKeys.BUNDLE_OBJECT_SM_INFO);
//		OfflineManager.updateFile(smInfo, new OfflineInfo());
//	}
}
