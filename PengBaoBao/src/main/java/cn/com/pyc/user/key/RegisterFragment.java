package cn.com.pyc.user.key;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.util.SecurityUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import cn.com.pyc.pbb.R;
import cn.com.pyc.base.PbbBaseFragment;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.widget.WidgetTool;

/**
 * 新用户注册页
 * @author Administrator
 *
 */
@Deprecated
public class RegisterFragment extends PbbBaseFragment implements OnCheckedChangeListener,
		OnClickListener
{

	private UserDao db = UserDao.getDB(getActivity());
	private UserInfo userInfo ;
	private String clientToken;

	public static final String TYPE_NICK = "nick";
	public static final String TYPE_PASSWORD = "password";

	private EditText g_edtNick;
	private EditText g_edtPsd;
	private EditText g_edtRePsd;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_key_register, container, false);
		findViewAndSetListeners(v);
		g_edtNick.requestFocus();
		((KeyActivity) getActivity()).showKeyboard();
		userInfo = db.getUserInfo();
		return v;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
            // 注册
			case R.id.fkr_btn_get:
				checkNick(getText(g_edtNick));
				break;
            // 进入
			case R.id.fkr_btn_insert:
				checkPassword(getText(g_edtPsd), getText(g_edtRePsd));
				break;
            // 已有账号登陆
			case R.id.fkr_txt_old_user:
				changeFragment(Pbb_Fields.TAG_KEY_LOGIN);
				break;

			default:
				break;
		}
	}

	@Override
	protected void findViewAndSetListeners(View v)
	{
		// nick
		g_edtNick = (EditText) v.findViewById(R.id.fkr_edt_nick);
		v.findViewById(R.id.fkr_btn_get).setOnClickListener(this);

		// password
		g_edtPsd = (EditText) v.findViewById(R.id.fkr_edt_create);
		g_edtRePsd = (EditText) v.findViewById(R.id.fkr_edt_recreate);
		((CheckBox) v.findViewById(R.id.fkr_cbx_show)).setOnCheckedChangeListener(this);
		v.findViewById(R.id.fkr_txt_old_user).setOnClickListener(this);
		v.findViewById(R.id.fkr_btn_insert).setOnClickListener(this);

		String type = getArguments().getString(GlobalIntentKeys.BUNDLE_DATA_TYPE);
		if (type.equals(TYPE_NICK))
		{
			v.findViewById(R.id.fkr_lyt_nick).setVisibility(View.VISIBLE);
			((KeyActivity) getActivity()).setMyTitle("注册新用户");
		}
		else
		{
			((KeyActivity) getActivity()).setMyTitle("隐私空间");
			v.findViewById(R.id.fkr_lyt_password).setVisibility(View.VISIBLE);
		}
	}
    /**
     *
     * @Description: (在fragment  hide或者show时调用)
     * @author 李巷阳
     * @date 2016/11/18 17:07
     */
    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if (!hidden)
        {
            String type = getArguments().getString(GlobalIntentKeys.BUNDLE_DATA_TYPE);
            if (type.equals(TYPE_NICK))
            {
                ((KeyActivity) getActivity()).setMyTitle("注册新用户");
            }
            else
            {
                ((KeyActivity) getActivity()).setMyTitle("隐私空间");
            }
        }
    }
	private String getText(TextView txt)
	{
		return txt.getText().toString().trim();
	}

	private void checkNick(String nick)
	{
		if (TextUtils.isEmpty(nick) || nick.length() > 10)
		{
			GlobalToast.toastCenter(getActivity(), "昵称不合法（1-10位）！");
			return;
		}

		register(nick, "");
	}

	private void checkPassword(String psd, String rePsd)
	{
		String prompt = null;
		if (TextUtils.isEmpty(psd) || TextUtils.isEmpty(rePsd))
		{
			prompt = "密码不能为空";
		}
		else if (!psd.equals(rePsd))
		{
			prompt = "密码不一致";
		}
		else if (!isPsdRightful(psd) || !isPsdRightful(rePsd))
		{
			prompt = "请输入由4-16位字母、数字或下划线组成的密码";
		}

		if (prompt != null)
		{
			GlobalToast.toastCenter(getActivity(), prompt);
			return;
		}

		register("", psd);
	}

	private boolean isPsdRightful(String psd)
	{
		return psd.matches("[a-zA-Z0-9_]{4,16}");
	}

	private void register(final String nick, final String psd)
	{
		showLoading(getActivity());
		GlobalTask.executeNetTask(getActivity(), new Runnable()
		{
			@Override
			public void run()
			{
				getClientToken(userInfo);

//				UserResult ur = new UserConnect(getActivity()).register(nick, psd, false, true);
//				if (ur.succeed())
//				{
//					startActivity(new Intent(getActivity(), KeySuccessActivity.class));
//				}

			}

			private void getClientToken(final UserInfo userInfo) {

				System.out.println("getClientToken方法执行.............");

				String clientTokenUrl = Constant.UserTokenHost;

				// 请求参数
				Bundle bundle = new Bundle();
				bundle.putString("grant_type", "client_credentials");

				// 请求头
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put(
						"Authorization",
						"Basic "
								+ SecurityUtil.encryptBASE64(PhoneInfo.testID
										+ ":" + PhoneInfo.testPSD));
				headers.put("Content-Type", "application/x-www-form-urlencoded");

				GlobalHttp.post(clientTokenUrl, bundle, headers,
						new CommonCallback<String>() {

							@Override
							public void onSuccess(String arg0) {
								System.out.println("getClientToken_SUCCESS___________________");
								// 解析Json
								try {
									JSONObject object = new JSONObject(arg0);
									clientToken = (String) object
											.get("access_token");

									//昵称注册
									registByNick(userInfo);
//									checkNickInfo(userInfo);

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinished() {
								// TODO Auto-generated method stub
							}

							@Override
							public void onError(Throwable arg0, boolean arg1) {
								// TODO Auto-generated method stub
								hideLoading();
								System.out.println("getClientToken_ERROR___________________");
								System.out.println(arg0.getMessage());
							}

							@Override
							public void onCancelled(CancelledException arg0) {
								// TODO Auto-generated method stub
							}
						});
			}



			private void checkNickInfo(final UserInfo uinfo) {

				String uidUrl = Constant.UserSourceHost + "api/v1/userregister";

				// 请求参数
				Bundle bundle = new Bundle();
				bundle.putString("username", nick);

				// 请求头
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Authorization",
						"Bearer " + clientToken);

				headers.put("Content-Type", "application/x-www-form-urlencoded");


				GlobalHttp.get(uidUrl, bundle, headers,
						new CommonCallback<String>() {

							@Override
							public void onSuccess(String arg0) {
								System.out.println("CheckNick返回obj:"+arg0);

								// 解析Json
								try {
									JSONObject object = new JSONObject(arg0);
									String Result =  (String) object
											.get("Result");

									if (Result.equals("true")) {
										//昵称注册
										registByNick(userInfo);

									}else if (Result.equals("false")) {
										GlobalToast.toastShort(getActivity(), (String)object.get("Message"));
										return;

									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onFinished() {
								// TODO Auto-generated method stub
							}

							@Override
							public void onError(Throwable arg0, boolean arg1) {
								// TODO Auto-generated method stub
								System.out.println(arg0.getMessage());
							}

							@Override
							public void onCancelled(CancelledException arg0) {
								// TODO Auto-generated method stub
							}
						});

			}
			/**
			 *
			 * @param uinfo
			 * @param tokenInfo
			 */
			private void registByNick(final UserInfo uinfo) {

				System.out.println("regist方法执行....");

				String url = Constant.UserSourceHost + "api/v1/userregister";

				// 请求参数
				Bundle bundle = new Bundle();
				bundle.putString("usernick", nick);
				bundle.putString("userfrom", "PbbAndroid");
				bundle.putString("regtype", "nick");

				// 请求头
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Authorization",
						"Bearer " + clientToken);

				headers.put("Content-Type", "application/json");


				GlobalHttp.post(url, bundle, headers,
						new CommonCallback<String>() {

							@Override
							public void onSuccess(String arg0) {

								// 解析Json
								try {
									JSONObject object = new JSONObject(arg0);
									String status = (String) object
											.get("Status");
									if (status.equals("1")) {

										String Result = (String) object
												.get("Result");

										uinfo.setUserName(Result);
										uinfo.setNick(nick);
										db.saveUserInfo(uinfo);
										getUID(uinfo);

//										GlobalToast.toastShort(getActivity(),
//												"注册成功!");

										startActivity(new Intent(getActivity(),
												KeySuccessActivity.class));
										getActivity().finish();
										return;

									} else if (status.equals("0")) {
										GlobalToast.toastShort(getActivity(),
												(String) object.get("Message"));
										return;
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onFinished() {
								// TODO Auto-generated method stub
							}

							@Override
							public void onError(Throwable arg0, boolean arg1) {
								// TODO Auto-generated method stub
								hideLoading();
							}

							@Override
							public void onCancelled(CancelledException arg0) {
								// TODO Auto-generated method stub
							}
						});

			}

			private void getUID(final UserInfo uinfo) {

				String uidUrl = Constant.UserSourceHost + "api/v1/useruidkey";

				// 请求参数
				Bundle bundle = new Bundle();
				bundle.putString("logname", uinfo.getUserName());

				// 请求头
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Authorization",
						"Bearer " + clientToken);

				headers.put("Content-Type", "application/x-www-form-urlencoded");


				GlobalHttp.get(uidUrl, bundle, headers,
						new CommonCallback<String>() {

							@Override
							public void onSuccess(String arg0) {
								System.out.println("UID返回obj:"+arg0);

								// 解析Json
								try {
									JSONObject object = new JSONObject(arg0);
									String Result =  (String) object
											.get("Result");

									byte[] byteResult = Result.getBytes();

									if (!byteResult.equals(null)) {
										uinfo.setUid(byteResult);
										db.saveUserInfo(uinfo);
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onFinished() {
								// TODO Auto-generated method stub
								hideLoading();
							}

							@Override
							public void onError(Throwable arg0, boolean arg1) {
								// TODO Auto-generated method stub
								System.out.println(arg0.getMessage());
							}

							@Override
							public void onCancelled(CancelledException arg0) {
								// TODO Auto-generated method stub
							}
						});

			}

		});
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		WidgetTool.changVisible(g_edtPsd, isChecked);
		WidgetTool.changVisible(g_edtRePsd, isChecked);
	}

}
