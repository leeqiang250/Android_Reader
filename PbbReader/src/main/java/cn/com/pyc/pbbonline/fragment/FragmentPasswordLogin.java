package cn.com.pyc.pbbonline.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.APIUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SecurityUtil;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.view.widget.FlatButton;

import org.xutils.common.Callback;

import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.LoginBean;
import cn.com.pyc.pbbonline.bean.event.LoginSuccessRefeshRecordEvent;
import cn.com.pyc.user.RegisterActivity;
import cn.com.pyc.widget.PycEditText;
import cn.com.pyc.widget.PycPsdEditText;
import cn.com.pyc.widget.PycUnderLineTextView;
import de.greenrobot.event.EventBus;

@Deprecated
public class FragmentPasswordLogin extends BaseFragment implements OnClickListener
{

	private PycEditText alr_edt_phone;
	private PycPsdEditText alr_edt_psd;
	private String phoneNumber;
	private FlatButton alr_btn_login;
	private PycUnderLineTextView regist_ll;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.pbbonline_fragment_password_login, container, false);
		initView(view);
		initValue();
		findViewAndSetListeners();

		return view;
	}

	private void findViewAndSetListeners()
	{
		alr_btn_login.setOnClickListener(this);
		regist_ll.setOnClickListener(this);
	}

	private void initView(View view)
	{

		alr_edt_phone = (PycEditText) view.findViewById(R.id.alr_edt_phone);
		alr_edt_psd = (PycPsdEditText) view.findViewById(R.id.alr_edit_psd);
		alr_btn_login = (FlatButton) view.findViewById(R.id.alr_btn_login);
		regist_ll = (PycUnderLineTextView) view.findViewById(R.id.alr_utv_new_regist);

	}

	private void initValue()
	{
		phoneNumber = getArguments().getString("phoneNumber");
		if (!TextUtils.isEmpty(phoneNumber) && TextUtils.isDigitsOnly(phoneNumber))
		{
			alr_edt_phone.setText(phoneNumber);
			alr_edt_psd.requestFocus();
		}

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		//		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.alr_btn_login)
		{
			loging();
		}
		else if (id == R.id.alr_utv_new_regist)
		{
			getActivity().startActivity(new Intent(getActivity(), RegisterActivity.class));
			getActivity().finish();
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
		final String psd_set = alr_edt_psd.getText().toString();
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
			UIHelper.showToast(getActivity().getApplicationContext(), "密码不能为空");
			return;
		}
		showBgLoading(getActivity(), "正在登录");
		Bundle bundle = new Bundle();
		bundle.putString("username", phone_number);
		bundle.putString("password", SecurityUtil.encryptBASE64(psd_set));
		bundle.putString("mytoken", (String) SPUtil.get(Fields.FIELDS_LOGIN_TOKEN, ""));
		bundle.putString("device", Constant.TOKEN);
		bundle.putString("registrationid", (String) SPUtil.get(Fields.FIELDS_JPUSH_REGISTERID, ""));

		GlobalHttp.post(APIUtil.getLoginPath(), bundle, new Callback.CommonCallback<String>()
		{

			@Override
			public void onCancelled(CancelledException arg0)
			{
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				UIHelper.showToast(getActivity().getApplicationContext(), "服务器连接失败");
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
					SPUtil.save(Fields.FIELDS_LOGIN_PASSWORD, psd_set);

					//RequestHttpManager.init().connectJpush();
					Intent data = new Intent();
					data.putExtra("opt_flag", true);
					getActivity().setResult(Activity.RESULT_OK, data);
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

}
