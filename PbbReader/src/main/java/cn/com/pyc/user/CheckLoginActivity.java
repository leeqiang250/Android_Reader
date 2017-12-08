package cn.com.pyc.user;

import org.xutils.common.Callback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.bean.LoginBean;
import cn.com.pyc.pbbonline.common.Code;
import cn.com.pyc.pbbonline.util.Util_;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.widget.PycEditText;

import com.alibaba.fastjson.JSON;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.StringUtil;
import com.sz.mobilesdk.util.UIHelper;
import com.sz.mobilesdk.util.UIHelper.DialogCallBack;

/**
 * 校验手机登录，领取分享
 */
public class CheckLoginActivity extends PbbBaseActivity implements OnClickListener
{
	public static final int CODE_FOR_LOGIN = 111;
	private static final int CODE_FOR_REGISTER = 113;
	private static final int CODE_FOR_SIGNOUT = 115;
	private TextView tv_rednotice; //红色提示文字
	private PycEditText edt_phone;//
	private Button btn_next;//下一步按钮

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checklogin);
		ViewHelp.showAppTintStatusBar(this);
		findViewAndSetListeners();
	}

	@Override
	public void findViewAndSetListeners()
	{
		((TextView) findViewById(R.id.title_tv)).setText("验证登录");
		tv_rednotice = (TextView) findViewById(R.id.al_tv_rednotice);
		edt_phone = (PycEditText) findViewById(R.id.al_edt_phone);
		btn_next = (Button) findViewById(R.id.al_btn_next);
		findViewById(R.id.back_img).setOnClickListener(this);
		btn_next.setOnClickListener(this);
	}

	private String getPhoneNumber()
	{
		return edt_phone.getText().toString().trim();
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.back_img)
		{
			finish();
		}
		else if (id == R.id.al_btn_next)
		{
			receiveShared(getPhoneNumber());
		}
		else
		{
		}
	}

	private void receiveShared(String phone)
	{
		if (tv_rednotice.getVisibility() == View.VISIBLE)
			tv_rednotice.setVisibility(View.INVISIBLE);
		if (!CommonUtil.isNetConnect(CheckLoginActivity.this))
		{
			UIHelper.showToast(getApplicationContext(), "网络不给力！");
			return;
		}
		if (TextUtils.isEmpty(phone))
		{
			UIHelper.showToast(getApplicationContext(), "手机号码不能为空");
			return;
		}
		if (!StringUtil.isMobileNO(phone))
		{
			UIHelper.showToast(getApplicationContext(), "请您输入正确的手机号");
			return;
		}
		showBgLoading(this, "正在验证");
		Util_.receiveVerifyShared(phone, new Callback.CommonCallback<String>()
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
				SZLog.d("checklogin receive success:" + arg0);
				LoginBean o = JSON.parseObject(arg0, LoginBean.class);

				judgeCodes(o.getCode(), o);
			}
		});
	}

	protected void judgeCodes(String code, LoginBean c)
	{
		switch (code)
		{
			case Code._SUCCESS:
			{
			}
				break;
			case Code._9001:
			case Code._9002:
			case Code._9003:
			case Code._9004:
			case Code._9005:
			case Code._9006:
			case Code._9008:
			case Code._9009:
			case Code._9010:
			case Code._9011:
			case Code._9012:
			case Code._9013:
			case Code._9014:
			case Code._9015:
			case Code._9016:
			case Code._9017:
			case Code._9018:
			case Code._9019:
			case Code._9020:
			case Code._9021:
			{
				if (tv_rednotice.getVisibility() == View.INVISIBLE)
					tv_rednotice.setVisibility(View.VISIBLE);
				tv_rednotice.setText(c.getMsg());
			}
				break;
			case Code._8006:
			case Code._8003:
			{
				//跳转登录界面;
				Intent intent = new Intent(CheckLoginActivity.this, LoginVerifyCodeActivity.class);
				intent.putExtra("phone_number", getPhoneNumber());
				startActivityForResult(intent, CODE_FOR_LOGIN);
			}
				break;
			case Code._8004:
			{
				//跳转注册界面.
				Intent intent = new Intent(CheckLoginActivity.this, LoginVerifyCodeActivity.class);
				intent.putExtra("phone_number", getPhoneNumber());
				startActivityForResult(intent, CODE_FOR_REGISTER);
			}
				break;
			case Code._8005:
				UIHelper.showCommonDialog(CheckLoginActivity.this, null, c.getMsg(), "退出",
						new DialogCallBack()
						{
							@Override
							public void onConfirm()
							{
								Intent intent = new Intent(CheckLoginActivity.this,
										UserCenterActivity.class);
								startActivityForResult(intent, CODE_FOR_SIGNOUT);
							}
						});
				break;
			default:
				UIHelper.showToast(getApplicationContext(), "服务器校验错误");
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode)
		{
			case CODE_FOR_LOGIN:
			case CODE_FOR_REGISTER:
			case CODE_FOR_SIGNOUT:
				if (data == null)
					return;
				if (data.getBooleanExtra("opt_flag", false))
				{
					setResult(Activity.RESULT_OK, data);
					finish();
				}
				else
					showToast("操作失败~");
				break;
			//			case CODE_FOR_SIGNOUT:
			//				if (data == null)
			//					return;
			//				if (data.getBooleanExtra("opt_flag", false))
			//				{
			//					setResult(Activity.RESULT_OK, data);
			//					finish();
			//				}
			//				else
			//					showToast("退出失败~");
			//				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}
}
