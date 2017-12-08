package cn.com.pyc.pbbonline.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.models.BaseModel;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.widget.FlatButton;

import org.xutils.common.Callback;

import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.LoginBean;
import cn.com.pyc.pbbonline.bean.event.LoginSuccessRefeshRecordEvent;
import cn.com.pyc.pbbonline.common.Code;
import cn.com.pyc.user.RegisterActivity;
import cn.com.pyc.widget.PycEditText;
import cn.com.pyc.widget.PycUnderLineTextView;
import de.greenrobot.event.EventBus;

@Deprecated
public class FragmentVerifyCodeLogin extends BaseFragment implements OnClickListener
{
	private PycEditText alr_edt_phone;
	private PycEditText ar_edt_security;
	private String phoneNumber;
	private FlatButton alr_btn_login;
	private PycUnderLineTextView regist_ll;
	private Button ar_btn_get_secutity;
	private MyCount mc;
	private TextView tvNotice;
	private Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.context = getActivity();
		View view = inflater
				.inflate(R.layout.pbbonline_fragment_verifycode_login, container, false);
		initView(view);
		initValue();
		findViewAndSetListeners();
		return view;
	}

	private void findViewAndSetListeners()
	{
		alr_btn_login.setOnClickListener(this);
		regist_ll.setOnClickListener(this);
		ar_btn_get_secutity.setOnClickListener(this);
	}

	private void initView(View view)
	{
		alr_edt_phone = (PycEditText) view.findViewById(R.id.ar_edt_phone);
		ar_edt_security = (PycEditText) view.findViewById(R.id.ar_edt_security);
		alr_btn_login = (FlatButton) view.findViewById(R.id.ar_btn_login);
		regist_ll = (PycUnderLineTextView) view.findViewById(R.id.ar_utv_new_regist);
		ar_btn_get_secutity = (Button) view.findViewById(R.id.ar_btn_get_security);
		tvNotice = (TextView) view.findViewById(R.id.al_tv_notice);
		tvNotice.setVisibility(View.INVISIBLE);
	}

	private void initValue()
	{
		phoneNumber = getArguments().getString("phoneNumber");
		if (!TextUtils.isEmpty(phoneNumber) && TextUtils.isDigitsOnly(phoneNumber))
		{
			alr_edt_phone.setText(phoneNumber);
			ar_edt_security.requestFocus();
		}
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.ar_btn_login)
		{
			//登陆
			loging();
		}
		else if (id == R.id.ar_utv_new_regist)
		{
			//注册
			getActivity().startActivity(new Intent(getActivity(), RegisterActivity.class));
			getActivity().finish();
		}
		else if (id == R.id.ar_btn_get_security)
		{
			// 获取手机验证码
			getVerfyCode();
		}
	}

	/*
	 * 登录
	 */
	private void loging()
	{
		if (!CommonUtil.isNetConnect(getActivity()))
		{
			UIHelper.showToast(getActivity().getApplicationContext(), "网络不给力！");
			return;
		}
		// 手机号码
		final String phone_number = alr_edt_phone.getText().toString().trim();
		// 密码
		final String psd_set = ar_edt_security.getText().toString();
		if (TextUtils.isEmpty(phone_number))
		{
			UIHelper.showToast(getActivity().getApplicationContext(), "手机号码不能为空");
			return;
		}
		if (!StringUtil.isMobileNO(phone_number))
		{
			UIHelper.showToast(getActivity().getApplicationContext(), "请您输入正确的手机号。");
			return;
		}
		if (TextUtils.isEmpty(psd_set))
		{
			UIHelper.showToast(getActivity().getApplicationContext(), "验证码不能为空");
			return;
		}
		showBgLoading(getActivity(), "正在登录");
		Bundle bundle = new Bundle();
		bundle.putString("username", phone_number);
		bundle.putString("validateCode", psd_set);
		GlobalHttp.post(APIUtil.getLoginVerifyPath(), bundle, new Callback.CommonCallback<String>()
		{

			@Override
			public void onCancelled(CancelledException arg0)
			{
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				UIHelper.showToast(getActivity().getApplicationContext(),
						getString(R.string.load_server_failed));
			}

			@Override
			public void onFinished()
			{
				hideBgLoading();
			}

			@Override
			public void onSuccess(String arg0)
			{
				LoginBean o = JSON.parseObject(arg0, LoginBean.class);
				if (o != null && o.isSuccess())
				{
					SPUtil.save(Fields.FIELDS_LOGIN_USER_NAME, phone_number);
					SPUtil.save(Fields.FIELDS_LOGIN_TOKEN, o.getToken());
					SPUtil.save(Fields.FIELDS_LOGIN_PASSWORD, o.getPassword());

					//RequestHttpManager.init().connectJpush();
					Intent data = new Intent();
					data.putExtra("opt_flag", true);
					getActivity().setResult(Activity.RESULT_OK, data);
					setMyCountTimerCancel();
					//登录成功，通知list界面刷新数据
					EventBus.getDefault().post(new LoginSuccessRefeshRecordEvent(true));
					getActivity().finish();
					UIHelper.showToast(getActivity().getApplicationContext(), "登录成功");
				}
				else
				{
					setFailCode(o.getCode());
				}
			}
		});
	}

	private void getVerfyCode()
	{
		String phonenumber = getPhoneNo();
		if (!CommonUtil.isNetConnect(getActivity()))
		{
			UIHelper.showToast(getActivity().getApplicationContext(), "网络不给力！");
			return;
		}
		if (!StringUtil.isMobileNO(phonenumber))
		{
			UIHelper.showToast(getActivity().getApplicationContext(), "请输入正确的手机号");
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
		alr_edt_phone.setEnabled(false);
		alr_edt_phone.setTextColor(getResources().getColor(R.color.gray_stroke));
		Bundle bundle = new Bundle();
		bundle.putString("username", phonenumber);
		bundle.putString("login", "login");
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
						UIHelper.showToast(getActivity().getApplicationContext(), "获取短信验证码失败");
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
							UIHelper.showToast(getActivity().getApplicationContext(), "短信验证码发送成功");
						}
						else
						{
							setVerfyFailCode(o.getCode());
							setMyCountTimerCancel();
						}
					}
				});

	}

	public String getPhoneNo()
	{
		return alr_edt_phone.getText().toString().trim();
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
		alr_edt_phone.setEnabled(true);
		alr_edt_phone.setTextColor(context.getResources().getColor(R.color.black));
		ar_btn_get_secutity.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.xml_imb_green));
		ar_btn_get_secutity.setTextColor(context.getResources().getColor(R.color.white));
		ar_btn_get_secutity.setText("获取验证码");
		countdowntimer_cancel();
	}

	private void countdowntimer_cancel()
	{
		if (mc != null)
		{
			mc.cancel();
			mc = null;
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
				showNotice("手机短信验证码发送失败(每天最多发送10条)");
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

	private void showNotice(String text)
	{
		if (tvNotice.getVisibility() == View.INVISIBLE)
			tvNotice.setVisibility(View.VISIBLE);
		tvNotice.setText(text);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		countdowntimer_cancel();
	}

}
