package cn.com.pyc.user;

import android.os.Bundle;
import android.text.TextUtils;
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

import cn.com.pyc.pbb.R;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

public class BindEmailActivity extends UserBaseActivity
{
	private UserDao db = UserDao.getDB(this);
	private UserInfo userInfo ;
	private String tokenString;
	
	private EditText g_edtEmail;

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_user_check_email);
		ViewHelp.showAppTintStatusBar(this);
		
		userInfo = db.getUserInfo();
		
		g_edtEmail = (EditText) findViewById(R.id.auce_edt_email);
		((TextView) findViewById(R.id.ipt_txt_title)).setText("验证邮箱");
		findViewById(R.id.ipt_imb_refresh).setVisibility(View.GONE);
		String email = userInfo.getEmail().trim();
		if (!TextUtils.isEmpty(email))
		{
			g_edtEmail.setText(email);
		}

		findViewById(R.id.auce_btn_check_email).setOnClickListener(
				new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						final String email = g_edtEmail.getText().toString()
								.trim();
						if (TextUtils.isEmpty(email))
						{
							GlobalToast.toastShort(BindEmailActivity.this,
									"请输入邮箱");
							return;
						}
						else
						{
							showLoading(BindEmailActivity.this);
							GlobalTask.executeNetTask(BindEmailActivity.this,
									new Runnable()
									{
										@Override
										public void run()
										{
											System.out.println("getUserINfo-------");

											getUserToken(userInfo);
											/*UserResult uc = new UserConnect(
													BindEmailActivity.this)
													.bindEmail(email, true,
															true);
											if (uc.succeed())
											{
												finish();
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
																
																bindEmail(userInfo);
																
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
										
										
										private void bindEmail(final UserInfo uinfo) {
											
											String changeNickUrl = Constant.UserSourceHost + "api/v1/useremail";
											
											// 请求参数
											Bundle bundle = new Bundle();
											bundle.putString("email", email);
											bundle.putString("type", "bind");

											// 请求头
											HashMap<String, String> headers = new HashMap<String, String>();
											headers.put("Authorization","Bearer " + tokenString);
											
											headers.put("Content-Type", "application/x-www-form-urlencoded");
//											headers.put("Content-Type", "application/json");


											GlobalHttp.post(changeNickUrl, bundle, headers,
													new CommonCallback<String>() {

														@Override
														public void onSuccess(String arg0) {
															
															// 解析Json
															try {
																JSONObject object = new JSONObject(arg0);
																String status =  (String) object
																		.get("Status");
																
																if (status.equals("1")) {
																	
																	GlobalToast.toastShort(getApplicationContext(),"已发送验证信息至邮箱。");
																	finish();
																	
																}else if (status.equals("0")) {
																	GlobalToast.toastShort(getApplicationContext(), (String)object.get("Message"));
																}
															} catch (JSONException e) {
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
					}
				});
	}

}
