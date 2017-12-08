package cn.com.pyc.setting;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.pbb.reader.R;
import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PushSettingActivity extends PbbBaseActivity
{
	private CheckBox cbx_push_writing, cbx_push_voice, cbx_push_zhen;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push_setting);

		final BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(
				PushSettingActivity.this);

		cbx_push_writing = (CheckBox) findViewById(R.id.aps_cbx_push_writings);
		cbx_push_voice = (CheckBox) findViewById(R.id.aps_cbx_push_voice);
		// cbx_push_zhen = (CheckBox) findViewById(R.id.aps_cbx_push_zhen);

		// 文章推送设置开关
		cbx_push_writing.setChecked((Boolean) PbbSP.getGSP(this).getValue(
				PbbSP.SP_PUSH_WRITINGS, true));
		cbx_push_writing
				.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked)
					{

						if (!isChecked)
						{
							JPushInterface.stopPush(getApplicationContext());
							System.out.println("Push---stop");
						}
						else
						{
							JPushInterface.resumePush(getApplicationContext());
							System.out.println("Push---resume");
						}
						PbbSP.getGSP(PushSettingActivity.this).putValue(
								PbbSP.SP_PUSH_WRITINGS, isChecked);
					}
				});

		// 声音开关
		cbx_push_voice.setChecked((Boolean) PbbSP.getGSP(this).getValue(
				PbbSP.SP_PUSH_VOICE, true));
		cbx_push_voice.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
			{
				if (!isChecked)
				{
					// builder.notificationDefaults =
					// Notification.DEFAULT_SOUND;
					JPushInterface.setSilenceTime(getApplicationContext(), 0,
							0, 23, 59);
				}
				PbbSP.getGSP(PushSettingActivity.this).putValue(
						PbbSP.SP_PUSH_VOICE, isChecked);
			}
		});

		// //震动开关
		// cbx_push_zhen.setChecked(GlobalSP.getSP(this).getBoolean(GlobalSP.SP_PUSH_ZHEN,
		// true));
		// cbx_push_zhen.setOnCheckedChangeListener(new
		// OnCheckedChangeListener()
		// {
		//
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView, boolean
		// isChecked)
		// {
		// if (isChecked)
		// {
		// builder.notificationDefaults = Notification.DEFAULT_VIBRATE;
		// }
		// GlobalSP.putSPValue(PushSettingActivity.this, GlobalSP.SP_PUSH_ZHEN,
		// isChecked);
		// }
		// });

		// //各设置逻辑实现
		// if (cbx_push_voice.isChecked() && cbx_push_zhen.isChecked())
		// {
		// builder.notificationDefaults = Notification.DEFAULT_SOUND |
		// Notification.DEFAULT_VIBRATE;
		// }else if (cbx_push_voice.isChecked() && !cbx_push_zhen.isChecked())
		// {
		// builder.notificationDefaults = Notification.DEFAULT_SOUND;
		// }else if (!cbx_push_voice.isChecked() && cbx_push_zhen.isChecked())
		// {
		// builder.notificationDefaults = Notification.DEFAULT_VIBRATE;
		// }
		//
		//
		// if (!cbx_push_writing.isChecked())
		// {
		// JPushInterface.stopPush(getApplicationContext());
		// }else {
		// JPushInterface.resumePush(getApplicationContext());
		// }

		JPushInterface.setPushNotificationBuilder(1, builder);
	}

	@Override
	public void onBackPressed()
	{
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}
}
