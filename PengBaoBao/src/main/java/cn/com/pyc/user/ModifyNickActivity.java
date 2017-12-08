package cn.com.pyc.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.qlk.util.global.GlobalObserver;
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
import cn.com.pyc.global.ObTag;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

public class ModifyNickActivity extends UserBaseActivity
{
	
	private UserDao db = UserDao.getDB(this);
	private UserInfo userInfo ;
	private String tokenString;
	
	private EditText g_edtNick;

	@Override
	protected void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_user_set_nick);
		ViewHelp.showAppTintStatusBar(this);
		
		userInfo = db.getUserInfo();

		g_edtNick = (EditText) findViewById(R.id.auce_edt_nick);
		((TextView) findViewById(R.id.ipt_txt_title)).setText("修改昵称");
		findViewById(R.id.ipt_imb_refresh).setVisibility(View.GONE);
		String nick = userInfo.getNick().trim();
		if (!TextUtils.isEmpty(nick))
		{
			g_edtNick.setText(nick);
		}
		findViewById(R.id.auce_btn_set_nick).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				final String nick = g_edtNick.getText().toString().trim();
				if (TextUtils.isEmpty(nick))
				{
					GlobalToast.toastShort(ModifyNickActivity.this, "请输入昵称");
				}
				else if (nick.length() > 10)
				{
					GlobalToast.toastShort(ModifyNickActivity.this, "输入昵称过长（10位以下）");
				}
				else
				{
					showLoading(ModifyNickActivity.this);
					GlobalTask.executeNetTask(ModifyNickActivity.this, new Runnable()
					{
						@Override
						public void run()
						{

							getUserToken(userInfo);
							
//							UserResult ur = new UserConnect(ModifyNickActivity.this).modifyNick(
//									nick, true);
//							if (ur.succeed())
//							{
//								GlobalToast.toastShort(ModifyNickActivity.this, "修改成功");
//								finish();	// 成功后就退出了
//							}
//							else
//							{
//								// 失败的话可以再次修改
//								if (!ur.isBusinessSucceed())
//								{
//									GlobalToast.toastShort(ModifyNickActivity.this, "修改失败");
//								}
//								else
//								{
//									GlobalToast.toastShort(ModifyNickActivity.this,
//											ur.getFailureReason());
//								}
//							}

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
												
												changeNick(userInfo);
												
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
						
						
						private void changeNick(final UserInfo uinfo) {
							
							System.out.println("changeNick方法执行。。。。。。。。。。"+nick);
							
							String changeNickUrl = Constant.UserSourceHost + "api/v1/usernick";
							
							// 请求参数
							Bundle bundle = new Bundle();
							bundle.putString("usernick", nick);

							// 请求头
							HashMap<String, String> headers = new HashMap<String, String>();
							headers.put("Authorization",
									"Bearer " + tokenString);
							
//							headers.put("Content-Type", "application/x-www-form-urlencoded");
							headers.put("Content-Type", "application/json");


							GlobalHttp.post(changeNickUrl, bundle, headers,
									new CommonCallback<String>() {

										@Override
										public void onSuccess(String arg0) {
											System.out.println("changeNick返回obj:"+arg0);
											
											// 解析Json
											try {
												JSONObject object = new JSONObject(arg0);
												String status =  (String) object
														.get("Status");
												
												if (status.equals("1")) {
													uinfo.setNick(nick);
													db.saveUserInfo(uinfo);
													finish();
													GlobalObserver.getGOb().postNotifyObservers(ObTag.Key);
													GlobalToast.toastShort(getApplicationContext(), "修改成功！");
												}else if (status.equals("0")) {
													GlobalToast.toastShort(getApplicationContext(), (String)object.get("Message"));
													finish();
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
			}
		});
	}

}
