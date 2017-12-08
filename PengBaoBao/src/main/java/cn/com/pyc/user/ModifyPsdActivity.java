package cn.com.pyc.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;

import java.util.HashMap;

import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.media.MediaActivity;
import cn.com.pyc.pbb.R;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

public class ModifyPsdActivity extends UserBaseActivity
{
	
	private UserDao db = UserDao.getDB(this);
	private UserInfo userInfo ;
	private String tokenString;
	
	// 这个Type的含义见InsertPsdActivity
	public static final String TYPE_FROM_CIPHER = "from_cipher";
	public static final String TYPE_FROM_USER = "from_user";

	private String g_strType;
	private EditText g_edtNewPsd;
	private EditText g_edtNewPsdAgain;
	private EditText g_edtOldPsd;

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		g_strType = getIntent().getStringExtra(
				GlobalIntentKeys.BUNDLE_DATA_TYPE);
		setContentView(R.layout.activity_user_modify_psd);
		ViewHelp.showAppTintStatusBar(this);
		
		userInfo = db.getUserInfo();

		findViewById(R.id.ipt_imb_refresh).setVisibility(View.GONE);
		g_edtNewPsd = (EditText) findViewById(R.id.aump_edt_new_psd);
		g_edtOldPsd = (EditText) findViewById(R.id.aump_edt_old_psd);
		g_edtNewPsdAgain = (EditText) findViewById(R.id.aump_edt_new_psd_again);

		if (userInfo.isPsdNull())
		{
			((TextView) findViewById(R.id.ipt_txt_title)).setText("创建密码");
		} else
		{
			((TextView) findViewById(R.id.ipt_txt_title)).setText("修改密码");
		}
		findViewById(R.id.aump_btn_modify).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						String oldPsd = g_edtOldPsd.getText().toString().trim();
						String newPsd = g_edtNewPsd.getText().toString().trim();
						String newPsdAgain = g_edtNewPsdAgain.getText()
								.toString().trim();
						if (TextUtils.isEmpty(newPsd)
								|| TextUtils.isEmpty(newPsdAgain))
						{
							GlobalToast.toastShort(ModifyPsdActivity.this,
									"密码不能为空！");
						} else if (!newPsd.equals(newPsdAgain))
						{
							GlobalToast.toastShort(ModifyPsdActivity.this,
									"新密码不一致！");
						} else if (!checkString(newPsd)
								|| !checkString(newPsdAgain))
						{
							GlobalToast.toastShort(ModifyPsdActivity.this,
									"请输入由4-16位字母、数字或下划线组成的密码");
						} else if (!TextUtils.isEmpty(oldPsd) && !TextUtils.equals(oldPsd,userInfo.getPsd())) {
							GlobalToast.toastShort(ModifyPsdActivity.this,
									"旧密码输入错误");
						}else if (!userInfo.isPsdNull() && TextUtils.isEmpty(oldPsd)) {
							GlobalToast.toastShort(ModifyPsdActivity.this,
									"请输入旧密码");
						}else
						{
							modifyPsd(newPsd);
						}
					}
				});
	}

	

	


	private boolean checkString(String s) // 判断输入是否符合约定规则
	{
		return s.matches("[a-zA-Z0-9_]{4,16}"); // 正则
	}

	private void modifyPsd(final String newPsd)
	{
		showLoading(ModifyPsdActivity.this);
		GlobalTask.executeNetTask(this, new Runnable()
		{

			@Override
			public void run()
			{
				getUserToken(userInfo);
				
				/*UserResult ur = new UserConnect(ModifyPsdActivity.this)
						.modifyPassword(newPsd, true);
				if (ur.succeed())
				{
					GlobalToast.toastShort(ModifyPsdActivity.this, g_strType
							.equals(TYPE_FROM_CIPHER) ? "创建密码成功！" : "修改密码成功！");
					if (g_strType.equals(TYPE_FROM_CIPHER))
					{
						// 不再输入密码
						Intent intent = new Intent(ModifyPsdActivity.this,
								MediaActivity.class);
						intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE,
								MediaActivity.TAG_CIPHER_TOTAL);
						intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER,
								true);
						intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM,
								false);
						startActivity(intent);
					}
					finish();
				} else
				{
					if (!ur.isBusinessSucceed())
					{
						GlobalToast.toastShort(ModifyPsdActivity.this,
								g_strType.equals(TYPE_FROM_CIPHER) ? "创建密码失败！"
										: "修改密码失败！");
					} else
					{
						GlobalToast.toastShort(ModifyPsdActivity.this,
								ur.getFailureReason());
					}
				}*/
			}
			
			private void getUserToken(final UserInfo userInfo) {
				
				String userTokenUrl = Constant.UserTokenHost;

				// 请求参数
				Bundle bundle = new Bundle();
				bundle.putString("grant_type", "password");
				bundle.putString("username", userInfo.getUserName());
				if (!TextUtils.isEmpty(userInfo.getPsd())) {
					bundle.putString("password", userInfo.getPsd());
				} else {
					bundle.putString(
							"password",
							"n|"
									+ XCoder.getHttpEncryptText(userInfo
											.getUserName()));
				}

				// 请求头
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put(
						"Authorization",
						"Basic "
								+ SecurityUtil.encryptBASE64(PhoneInfo.testID
										+ ":" + PhoneInfo.testPSD));
				headers.put("Content-Type", "application/x-www-form-urlencoded");

				GlobalHttp.post(userTokenUrl, bundle, headers,
						new CommonCallback<String>() {

							@Override
							public void onSuccess(String arg0) {
								// 解析Json
								try {
									JSONObject object = new JSONObject(arg0);
									tokenString = (String) object
											.get("access_token");
									
									changePsd(userInfo);
									
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
							}

							@Override
							public void onCancelled(CancelledException arg0) {
								// TODO Auto-generated method stub
							}
						});
			}
			
			private void changePsd(final UserInfo uinfo) {
				
				System.out.println("changePSD方法执行。。。。。。。。。。"+newPsd);
				
				String changeNickUrl = Constant.UserSourceHost + "api/v1/resetpassword";
				
				// 请求参数
				Bundle bundle = new Bundle();
				if(uinfo.isPsdNull()){
					bundle.putString("OldPassword", "");
				}else{

					bundle.putString("OldPassword", uinfo.getPsd());
				}
//				bundle.putString("OldPassword", uinfo.getPsd());
				bundle.putString("NewPassword", newPsd);


				System.out.println("getPSD="+uinfo.getPsd());

				// 请求头
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Authorization",
						"Bearer " + tokenString);
				
				headers.put("Content-Type", "application/json");


				GlobalHttp.post(changeNickUrl, bundle, headers,
						new CommonCallback<String>() {

							@Override
							public void onSuccess(String arg0) {
								System.out.println("changePSD返回obj:"+arg0);
								
								// 解析Json
								try {
									JSONObject object = new JSONObject(arg0);
									String status =  (String) object
											.get("Status");
									
									if (status.equals("1")) {
										uinfo.setPsd(newPsd);
										db.saveUserInfo(uinfo);
										System.out
												.println("保存的新密码为："+newPsd);
										Log.i("ModifyPSD","保存的新密码为："+newPsd);
										
										GlobalToast.toastShort(ModifyPsdActivity.this, g_strType
												.equals(TYPE_FROM_CIPHER) ? "创建密码成功！" : "修改密码成功！");
										
										if (g_strType.equals(TYPE_FROM_CIPHER))
										{
											// 不再输入密码
											Intent intent = new Intent(ModifyPsdActivity.this,
													MediaActivity.class);
											intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE,
													Pbb_Fields.TAG_CIPHER_TOTAL);
											intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER,
													true);
											intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM,
													false);
											startActivity(intent);
										}	
										finish();
										
									}else if (status.equals("0")) {
										GlobalToast.toastShort(ModifyPsdActivity.this,
												g_strType.equals(TYPE_FROM_CIPHER) ? "创建密码失败！"
														: "修改密码失败！");
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
								System.out.println(arg0.getMessage()+"----------");
							}

							@Override
							public void onCancelled(CancelledException arg0) {
								// TODO Auto-generated method stub
							}
						});

			}
			
		});
	}
}
