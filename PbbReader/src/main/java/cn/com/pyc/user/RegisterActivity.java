package cn.com.pyc.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.models.BaseModel;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.SecurityUtil;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.mobilesdk.util.UIHelper;

import org.xutils.common.Callback;

import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.common.Code;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.widget.PycEditText;
import cn.com.pyc.widget.PycUnderLineTextView;

/*
 * 注册
 */
@Deprecated
public class RegisterActivity extends PbbBaseActivity implements OnClickListener
{
	private PycUnderLineTextView ar_utv_old_login;
	private PycEditText ar_edt_phone;
	private PycEditText ar_edt_secutrity;
	private Button ar_btn_get_secutity;
	private PycEditText ar_edt_psd_set;
	private PycEditText ar_edt_psd_confirm;
	private Button ar_btn_ok;
	private TextView tvNotice;
	private MyCount mc;
	private String phoneNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		ViewHelp.showAppTintStatusBar(this);
		phoneNumber = getIntent().getStringExtra("phone_number");
		findViewAndSetListeners();
	}

	public void findViewAndSetListeners()
	{
		((TextView) findViewById(R.id.title_tv)).setText("注 册");
		ar_utv_old_login = (PycUnderLineTextView) findViewById(R.id.ar_utv_old_login);
		ar_edt_phone = (PycEditText) findViewById(R.id.ar_edt_phone);
		ar_edt_secutrity = (PycEditText) findViewById(R.id.ar_edt_security);
		if (!TextUtils.isEmpty(phoneNumber))
		{
			ar_edt_phone.setText(phoneNumber);
			ar_edt_secutrity.requestFocus();
		}
		ar_btn_get_secutity = (Button) findViewById(R.id.ar_btn_get_security);
		ar_edt_psd_set = (PycEditText) findViewById(R.id.ar_edt_psd_set);
		ar_edt_psd_confirm = (PycEditText) findViewById(R.id.ar_edt_psd_conform);
		tvNotice = (TextView) findViewById(R.id.al_tv_notice);
		ar_btn_ok = (Button) findViewById(R.id.ar_btn_ok);
		ar_utv_old_login.setOnClickListener(this);
		ar_btn_get_secutity.setOnClickListener(this);
		ar_btn_ok.setOnClickListener(this);
		findViewById(R.id.back_img).setOnClickListener(this);
	}

	public String getPhoneNo()
	{
		return ar_edt_phone.getText().toString().trim();
	}

	private void showNotice(String text)
	{
		if (tvNotice.getVisibility() == View.INVISIBLE)
			tvNotice.setVisibility(View.VISIBLE);
		tvNotice.setText(text);
	}

	private void hideNotice()
	{
		if (tvNotice.getVisibility() == View.VISIBLE)
			tvNotice.setVisibility(View.INVISIBLE);
		tvNotice.setText(null);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.ar_utv_old_login)
		{
			// 老用户点此登录
			startActivity(new Intent(this, LoginVerifyCodeActivity.class));
			finish();
		}
		else if (id == R.id.ar_btn_get_security)
		{
			// 获取手机验证码
			getVerfyCode();
		}
		else if (id == R.id.ar_btn_ok)
		{
			// 注册
			toRegister();
		}
		else if (id == R.id.back_img)
		{
			setMyCountTimerCancel();
			hideBgLoading();
			finish();
		}
		else
		{
		}
	}

	private void toRegister()
	{
		hideNotice();
		if (!CommonUtil.isNetConnect(RegisterActivity.this))
		{
			UIHelper.showToast(getApplicationContext(), "网络不给力！");
			return;
		}
		// 手机号
		final String phonenumber = getPhoneNo();
		// 手机验证码
		String secutrity = ar_edt_secutrity.getText().toString().trim();
		// 设置密码
		String psd_set = ar_edt_psd_set.getText().toString();
		// 确认密码
		String psd_confirm = ar_edt_psd_confirm.getText().toString();

		if (!StringUtil.isMobileNO(phonenumber))
		{
			UIHelper.showToast(getApplicationContext(), "请您输入正确的手机号");
			return;
		}
		if (TextUtils.isEmpty(secutrity))
		{
			UIHelper.showToast(getApplicationContext(), "手机验证码不能为空");
			return;
		}
		if (TextUtils.isEmpty(psd_set))
		{
			UIHelper.showToast(getApplicationContext(), "设置密码不能为空");
			return;
		}
		if (TextUtils.isEmpty(psd_set))
		{
			UIHelper.showToast(getApplicationContext(), "确认密码不能为空");
			return;
		}
		if (!psd_set.equals(psd_confirm))
		{
			UIHelper.showToast(getApplicationContext(), "设置密码和确认密码必须相同");
			return;
		}
		showBgLoading(RegisterActivity.this, "正在注册");
		Bundle bundle = new Bundle();
		bundle.putString("username", phonenumber);
		bundle.putString("validateCode", secutrity);
		bundle.putString("password", SecurityUtil.encryptBASE64(psd_set));
		bundle.putString("deviceIdentifier", Constant.TOKEN);
		bundle.putString("registrationid", (String) SPUtil.get(Fields.FIELDS_JPUSH_REGISTERID, ""));
		//add20160526：网页启动app和扫码
		bundle.putString("source", (String) SPUtil.get(Fields.FIELDS_WEB_SOURCE, ""));
		bundle.putString("shareId", (String) SPUtil.get(Fields.FIELDS_ID, ""));
		if (Util_.isWebBrowser())
		{
			bundle.putString("weixin", (String) SPUtil.get(Fields.FIELDS_WEB_WEIXIN, ""));
		}

		GlobalHttp.postOn(APIUtil.getRegisterPath(), bundle, new Callback.CommonCallback<String>()
		{

			@Override
			public void onCancelled(CancelledException arg0)
			{
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				UIHelper.showToast(getApplicationContext(), getString(R.string.load_server_failed));
			}

			@Override
			public void onFinished()
			{
				hideBgLoading();
			}

			@Override
			public void onSuccess(String arg0)
			{
				SZLog.d("registe: " + arg0);
				BaseModel o = JSON.parseObject(arg0, BaseModel.class);
				if (o != null && o.isSuccess())
				{
					//注册成功，去登录获取token。
					Intent intent = new Intent(RegisterActivity.this, LoginPasswordActivity.class);
					intent.putExtra("phone_number", phonenumber);
					startActivityForResult(intent, CheckLoginActivity.CODE_FOR_LOGIN);
					UIHelper.showToast(getApplicationContext(), o.getMsg());
				}
				else
				{
					setRegisterReturnCode(o.getCode());
				}
			}
		});

		//		RequestHttpManager.init().postData(APIUtil.getRegisterPath(), bundle,
		//				new RequestCallBack<String>()
		//				{
		//					@Override
		//					public void onSuccess(ResponseInfo<String> arg0)
		//					{
		//						SZLog.d("registe: " + arg0.result);
		//						BaseModel o = JSON.parseObject(arg0.result, BaseModel.class);
		//						if (o != null && o.isSuccess())
		//						{
		//							//注册成功，去登录获取token。
		//							Intent intent = new Intent(RegisterActivity.this,
		//									LoginPasswordActivity.class);
		//							intent.putExtra("phone_number", phonenumber);
		//							startActivityForResult(intent, CheckLoginActivity.CODE_FOR_LOGIN);
		//							UIHelper.showToast(getApplicationContext(), o.getMsg());
		//						}
		//						else
		//						{
		//							setRegisterReturnCode(o.getCode());
		//						}
		//						hideBgLoading();
		//					}
		//
		//					public void onFailure(HttpException arg0, String arg1)
		//					{
		//						UIHelper.showToast(getApplicationContext(), "服务器连接失败");
		//						hideBgLoading();
		//					}
		//				});
	}

	private void getVerfyCode()
	{
		hideNotice();
		String phonenumber = getPhoneNo();
		if (!CommonUtil.isNetConnect(RegisterActivity.this))
		{
			UIHelper.showToast(getApplicationContext(), "网络不给力！");
			return;
		}
		if (!StringUtil.isMobileNO(phonenumber))
		{
			UIHelper.showToast(getApplicationContext(), "请输入正确的手机号");
			return;
		}
		ar_btn_get_secutity
				.setBackgroundDrawable(getResources().getDrawable(R.drawable.imb_white1));
		// 设置获取验证码按钮的颜色。
		ar_btn_get_secutity.setEnabled(false);
		ar_btn_get_secutity.setTextColor(getResources().getColor(R.color.black));
		mc = new MyCount(60000, 1000);
		mc.start();
		// 获取手机验证码的时候，设置手机号不可编辑
		ar_edt_phone.setEnabled(false);
		ar_edt_phone.setTextColor(getResources().getColor(R.color.gray_stroke));
		Bundle bundle = new Bundle();
		bundle.putString("username", phonenumber);
		GlobalHttp.get(APIUtil.getPhoneVerificationCode(), bundle,
				new Callback.CommonCallback<String>()
				{

					@Override
					public void onCancelled(CancelledException arg0)
					{
					}

					@Override
					public void onError(Throwable arg0, boolean arg1)
					{
						UIHelper.showToast(getApplicationContext(), "获取短信验证码失败");
						setMyCountTimerCancel();
					}

					@Override
					public void onFinished()
					{
					}

					@Override
					public void onSuccess(String arg0)
					{
						BaseModel o = JSON.parseObject(arg0, BaseModel.class);
						if (o != null && o.isSuccess())
						{
							UIHelper.showToast(getApplicationContext(), "短信验证码发送成功");
						}
						else
						{
							setVerfyFailCode(o.getCode());
							setMyCountTimerCancel();
						}
					}
				});

		//		RequestHttpManager.init().getData(APIUtil.getPhoneVerificationCode(), bundle,
		//				new RequestCallBack<String>()
		//				{
		//					@Override
		//					public void onSuccess(ResponseInfo<String> arg0)
		//					{
		//						BaseModel o = JSON.parseObject(arg0.result, BaseModel.class);
		//						if (o != null && o.isSuccess())
		//						{
		//							UIHelper.showToast(getApplicationContext(), "短信验证码发送成功");
		//						}
		//						else
		//						{
		//							setVerfyFailCode(o.getCode());
		//							setMyCountTimerCancel();
		//						}
		//					}
		//
		//					public void onFailure(HttpException arg0, String arg1)
		//					{
		//						UIHelper.showToast(getApplicationContext(), "获取短信验证码失败");
		//						setMyCountTimerCancel();
		//					}
		//				});
	}

	private void setRegisterReturnCode(String code)
	{
		switch (code)
		{
			case Code._9106:
				//UIHelper.showToast(getApplicationContext(), "注册失败");
				showNotice("注册失败");
				break;
			case Code._9107:
				//UIHelper.showToast(getApplicationContext(), "手机短信验证码错误");
				showNotice("手机短信验证码错误");
				break;
			default:
				break;
		}
	}

	private void setVerfyFailCode(String code)
	{
		switch (code)
		{
			case Code._9103:
				//UIHelper.showToast(getApplicationContext(), "该用户已经被注册");
				showNotice("该用户已经被注册");
				break;
			case Code._9104:
				//UIHelper.showToast(getApplicationContext(), "手机短信验证码发送失败");
				showNotice("手机短信验证码发送失败");
				break;
			case Code._9105:
				//UIHelper.showToast(getApplicationContext(), "手机号格式错误");
				showNotice("手机号格式错误");
				break;
			case Code._9009:
				//UIHelper.showToast(getApplicationContext(), "图片验证码错误");
				showNotice("验证码错误");
				break;
			case Code._9110:
				//UIHelper.showToast(getApplicationContext(), "从session中获取图片验证码失败");
				showNotice("参数传递错误");
				break;
			default:
				//UIHelper.showToast(getApplicationContext(), "获取短信验证码失败");
				showNotice("获取短信验证码失败");
				break;
		}
	}

	/* 定义一个倒计时的内部类 */
	class MyCount extends CountDownTimer
	{
		public MyCount(long millisInFuture, long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish()
		{
			setMyCountTimerCancel();
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			ar_btn_get_secutity.setText("重新获取 (" + millisUntilFinished / 1000 + ")");
		}
	}

	private void setMyCountTimerCancel()
	{
		ar_btn_get_secutity.setEnabled(true);
		// 设置手机号可以编辑setFocusable
		ar_edt_phone.setEnabled(true);
		ar_edt_phone.setTextColor(getResources().getColor(R.color.black));
		ar_btn_get_secutity.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.xml_imb_green));
		ar_btn_get_secutity.setTextColor(getResources().getColor(R.color.white));
		ar_btn_get_secutity.setText("获取验证码");
		if (mc != null)
		{
			mc.cancel();
			mc = null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Activity.RESULT_OK != resultCode)
			return;
		switch (requestCode)
		{
			case CheckLoginActivity.CODE_FOR_LOGIN:
			{
				if (data == null)
					return;
				if (data.getBooleanExtra("opt_flag", false))
				{
					setResult(Activity.RESULT_OK, data);
					finish();
				}
			}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}
}
