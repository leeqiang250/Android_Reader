package cn.com.pyc.sm;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util.FormatUtil;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.SmInfo;
import cn.com.pyc.conn.SmConnect;
import cn.com.pyc.conn.SmResult;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.util.ViewHelp;

/**
*
* @Description: (验证手机界面)
* @author 李巷阳
* @date 2016/12/12 11:10 
*/
public class SecurityCodeActivity extends PbbBaseActivity
{
	public static final String SECURITY_CODE = "security_code";
	public static final String MSG_ID = "msg_id";
	public static final String FROM_SECURITY = "from_security";

	private static short Security_Time = 60;
	private static long Exit_Time;

	private EditText g_edtPhone;
	private EditText g_edtSecurity;
	private Button g_btnGetSecurity;
	private TextView g_txtSubmit;

	private Handler handler;

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_security_code);
		ViewHelp.showAppTintStatusBar(this);
		handler = new Handler();

		g_edtPhone = (EditText) findViewById(R.id.asc_edt_phone);
		g_edtSecurity = (EditText) findViewById(R.id.asc_edt_security);
		g_btnGetSecurity = (Button) findViewById(R.id.asc_btn_get_security);
		g_txtSubmit = (TextView) findViewById(R.id.asc_txt_submit);
		g_txtSubmit.setTextColor(getResources().getColor(R.color.disabled));
		g_txtSubmit.setClickable(false);

		g_txtSubmit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String security = g_edtSecurity.getText().toString().trim();
				if (TextUtils.isEmpty(securityCode)
						|| TextUtils.isEmpty(security))
				{
					GlobalToast.toastShort(SecurityCodeActivity.this, "请输入验证码");
				}
				else
				{
					checkSecurityRight(security);
				}
			}
		});
		g_btnGetSecurity.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String phone = g_edtPhone.getText().toString().trim();
				if (TextUtils.isEmpty(phone))
				{
					GlobalToast.toastShort(SecurityCodeActivity.this, "请输入手机号");
				}
				else if (!FormatUtil.isPhoneFormat((phone)))
				{
					GlobalToast.toastShort(SecurityCodeActivity.this, "手机号不合法");
				}
				else
				{
					getSecurity(phone);
				}
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		int spaceTime = (int) ((System.currentTimeMillis() - Exit_Time) / 1000);
		if (Security_Time < 60 && spaceTime < Security_Time)
		{
			Security_Time -= spaceTime;
			start();
		}
		else
		{
			Security_Time = 60;
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		interrupt();
		Exit_Time = System.currentTimeMillis();
	}

	private void checkSecurityRight(final String security)
	{
		if (!security.equals(securityCode))
		{
			GlobalToast.toastShort(this, "验证码不正确");
		}
		else
		{
			Intent intent = GlobalIntentKeys.reUseIntent(this,
					SmReaderActivity.class);
			intent.putExtra(FROM_SECURITY, true);
			intent.putExtra(MSG_ID, msgId);
			intent.putExtra(SECURITY_CODE, securityCode);
			startActivity(intent);
			stop();
			finish();
		}
	}

	private void getSecurity(final String phone)
	{
		showLoading(this);
		GlobalTask.executeBackground(new Runnable() {
			@Override
			public void run() {
				SmInfo info = new SmInfo();
				info.setPhone(phone);
				SmResult sr = new SmConnect(getApplicationContext())
						.getSecurityCode(info, true);
				if (sr.succeed())
				{
					msgId = sr.getSmInfo().getMsgId();
					securityCode = sr.getSmInfo().getSecurityCode();
					start();
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							g_txtSubmit.setTextColor(getResources().getColor(
									R.color.white));
							g_txtSubmit.setClickable(true);
						}
					});
					hideLoading();
				}
				else
				{
					if (sr.getSuc() == 0)
					{
						GlobalToast.toastShort(getApplicationContext(),
								"请检查手机号是否正确");
					}
					else
					{
						GlobalToast.toastShort(getApplicationContext(),
								sr.getFailureReason());
					}
					hideLoading();
				}
			}
		});
		/*GlobalTask.executeNetTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				SmInfo info = new SmInfo();
				info.setPhone(phone);
				SmResult sr = new SmConnect(getApplicationContext())
						.getSecurityCode(info, true);
				if (sr.succeed())
				{
					msgId = sr.getSmInfo().getMsgId();
					securityCode = sr.getSmInfo().getSecurityCode();
					start();
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							g_txtSubmit.setTextColor(getResources().getColor(
									R.color.white));
							g_txtSubmit.setClickable(true);
						}
					});
				}
				else
				{
					if (sr.getSuc() == 0)
					{
						GlobalToast.toastShort(getApplicationContext(),
								"请检查手机号是否正确");
					}
					else
					{
						GlobalToast.toastShort(getApplicationContext(),
								sr.getFailureReason());
					}
				}
			}
		});*/
	}

	private String msgId;
	private String securityCode;

	/**********************************
	 * 验证手机时：获取验证码后立马返回然后再进入
	 *******************************/

	private void start()
	{
		handler.post(refresh);
	}

	private void interrupt()
	{
		handler.removeCallbacks(refresh);
	}

	private void stop()
	{
		handler.removeCallbacks(refresh);
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				g_btnGetSecurity
						.setBackgroundResource(R.drawable.xml_imb_green);
				g_btnGetSecurity.setText("获取验证码");
				g_btnGetSecurity.setClickable(true);
				Security_Time = 60;
			}
		});
	}

	private Runnable refresh = new Runnable()
	{
		public void run()
		{
			if (Security_Time > 0)
			{
				g_btnGetSecurity.setBackgroundColor(Color.GRAY);
				g_btnGetSecurity.setText(Security_Time + "秒");
				g_btnGetSecurity.setClickable(false);
				Security_Time--;
				handler.postDelayed(refresh, 1000);
			}
			else
			{
				g_btnGetSecurity
						.setBackgroundResource(R.drawable.xml_imb_green);
				g_btnGetSecurity.setText("获取验证码");
				g_btnGetSecurity.setClickable(true);
				Security_Time = 60;
			}
		}
	};

}
