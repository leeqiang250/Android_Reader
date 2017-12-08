package cn.com.pyc.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.tool.Util;
import com.qlk.util.tool.Util.IOUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SecurityUtil;
import com.sz.mobilesdk.util.UIHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import cn.com.pyc.base.ExtraBaseApplication;
import cn.com.pyc.base.PbbBaseActivity;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.global.PbbSP;
import cn.com.pyc.pbb.R;
import cn.com.pyc.user.InsertPsdActivity;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.user.key.KeyActivity;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.utils.Dirs;
import cn.com.pyc.xcoder.XCoder;
import cn.jpush.android.api.JPushInterface;
/**
 *
 * @Date 创建时间：2017/6/26 上午9:56
 * @Author 作者：大熊
 * @Desc 描述：程序加载的第一个页面（几个轮播页面临时去掉了）
 *
 */


public class WelcomeActivity extends PbbBaseActivity {

	private UserDao db = UserDao.getDB(WelcomeActivity.this);
	private UserInfo userInfo ;
	private String clientToken;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UIHelper.showTintStatusBar(this,R.color.function_bg);

		userInfo = db.getUserInfo();
//		if (!Dirs.isStorageInUse()) {
//			GlobalToast.toastShort(this, getResources().getString(R.string.no_storage_available));
//			((ExtraBaseApplication) getApplication()).safeExit();// 删除pc端生成的临时文件
//			return;
//		}
//		FileUtil.deleteFile(Dirs.getPycSucPath()); // 虽然退出时已经删除过，但保险起见，再删一次

		/**
		 * @Desc 描述：根据不同条件判断去往哪个页面
		 * @Param 参数：
		 * @Return 返回：
		 * Created by 大熊 at : 2017-6-26
		 **/
//		goToWhere();

	}

	private void goToWhere() {
		// 判断本地是否有每次输入密码，如果有则 就存放在本地.../pyc.temp。（此功能临时去掉了，逻辑直接进else）
		if (getIsPycTempPath()) {
			tempFileExists();
		} else {
			// 判断是否是第一次进入程序
			// true:第一次进入，进入欢迎界面
			// false:进入主界面
			//如果阅读过也直接进入主界面
			if ((boolean) PbbSP.getGSP(this).getValue(PbbSP.SP_FIRST_FUNCTION, true)&& SPUtil.get("read","") == "") {
				// 进入滑动选择界面
				//需求要临时去掉轮播,直接进入登录界面，故下一行暂时注释掉
//				startFunction();
				initSelectedView();
			} else {
				Intent intent = new Intent(this, InsertPsdActivity.class);
				intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_TYPE, Pbb_Fields.TYPE_INSERT_START);// 传输的程序启动，表示直接进入主界面
				startActivity(intent);
				finish();
			}
		}
	}
	/**
	 *	在个人中心设置里面有个功能是：每次运行,需要输入密码。如果选择,则确定。则把密码存放在本地".../pyc.temp"里面。
	 *  此功能已临时去掉
	 * @author 李巷阳
	 * @date 2016-11-7 下午3:14:13
	 */
	private boolean getIsPycTempPath() {

		File f = new File(Dirs.getPycTempPath());
		if(f.exists())
		{
			return true;
		}
		else{
			return false;
		}
	}

	private void tempFileExists() {
		// tempFile是经过加密处理的
		String temPath = XCoder.decryptTempFile(this, Dirs.getPycTempPath());
		if (temPath != null && analysisTempFile(temPath)) {
			startActivity(new Intent(WelcomeActivity.this, InsertPsdActivity.class));
			PbbSP.getGSP(this).putValue(PbbSP.SP_FIRST_FUNCTION, false);
			PbbSP.getGSP(this).putValue(PbbSP.SP_FIRST_LOGIN, false);
			finish();
		} else {
			GlobalToast.toastShort(this, "启动鹏保宝失败，请重试");
			((ExtraBaseApplication) getApplication()).safeExit();
		}
	}

	/*-
	 * 返回文件格式：
	 *   第一行：用户名
	 *   第二行：密码
	 *   第三行：uid
	 *   第四行：邮箱
	 *   第五行：手机号
	 *   第六行：suc 1、8、16、24、32
	 *
	 *   虽然通信协议的邮箱、手机号、qq等是否绑定的表现变化了，但这里仍沿用老协议
	 */
	private boolean analysisTempFile(String path) {
		UserInfo userInfo = UserDao.getDB(this).getUserInfo();
		File file = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			userInfo.setUserName(br.readLine());
			userInfo.setPsd(br.readLine());
			userInfo.setUid(br.readLine().getBytes());
			userInfo.setEmail(br.readLine());
			userInfo.setPhone(br.readLine());
			userInfo.setBindedValue(Integer.valueOf(br.readLine()));
			UserDao.getDB(this).saveUserInfo(userInfo);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			file.delete();
			IOUtil.close(br, null);
		}
	}

	/**
	*
	* @Description: (进入轮播图滑动选择界面)
	* @author 李巷阳
	* @date 2016/12/2 11:02
	*/
	@Deprecated
	private void startFunction() {
		setContentView(R.layout.activity_welcome);
		final ViewPager vpg = (ViewPager) findViewById(R.id.aw_vpg_function);
		vpg.setAdapter(new FunctionPagerAdapter());
		vpg.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				RadioGroup rdg = (RadioGroup) findViewById(R.id.aw_rdg_function);
				rdg.getChildAt(0).setBackgroundResource(R.drawable.function0);
				rdg.getChildAt(1).setBackgroundResource(R.drawable.function0);
				rdg.getChildAt(2).setBackgroundResource(R.drawable.function0);
				rdg.getChildAt(3).setBackgroundResource(R.drawable.function0);
				rdg.getChildAt(arg0).setBackgroundResource(R.drawable.function1);
				if (arg0 == 3) {// 滑动到第四页的时候，展示选择页面
					initSelectedView();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
	/**
	 * @Desc 描述：轮播页面结束后，来到此页面
	 * @Param 参数：
	 * @Return 返回：
	 * Created by 大熊 at :2017-6-26
	 **/
	protected void initSelectedView() {
//		PbbSP.getGSP(this).putValue(PbbSP.SP_FIRST_FUNCTION, false);
		setContentView(R.layout.activity_welcome2);
		// 首次使用
		findViewById(R.id.aw_btn_new).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Util.NetUtil.isNetInUse(WelcomeActivity.this)){
					//注册默认账户
					getDefaultAccount();
				}else{
					GlobalToast.toastShort(WelcomeActivity.this,"无网络连接");
				}


			}
		});
		// 已有账号登陆
		findViewById(R.id.aw_btn_old).setOnClickListener(new OnClickListener() { // 老用户用email登陆
			@Override
			public void onClick(View v) {
				// KeyActivity.TAG_KEY_EMAIL 标识跳转到登陆界面的fragment。
				Intent intent = new Intent(WelcomeActivity.this, KeyActivity.class);
				intent.putExtra(Pbb_Fields.TAG_KEY_CURRENT, Pbb_Fields.TAG_KEY_LOGIN); 
				startActivity(intent);
			}
		});
	}

	//注册默认账户
	private void getDefaultAccount(){
		showBgLoading(WelcomeActivity.this);
		GlobalTask.executeBackground(new Runnable()
		{
			@Override
			public void run()
			{
				getClientToken(userInfo);

			}

			private void getClientToken(final UserInfo userInfo) {

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
						new Callback.CommonCallback<String>() {

							@Override
							public void onSuccess(String arg0) {
								// 解析Json
								try {
									JSONObject object = new JSONObject(arg0);
									clientToken = (String) object
											.get("access_token");

									//昵称注册
									registByNick(userInfo);

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
								hideBgLoading();
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
			 */
			private void registByNick(final UserInfo uinfo) {

				String url = Constant.UserSourceHost + "api/v1/userregister";

				// 请求参数
				Bundle bundle = new Bundle();
				bundle.putString("usernick", "匿名用户");
				bundle.putString("userfrom", "PbbAndroid");
				bundle.putString("regtype", "nick");

				// 请求头
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Authorization",
						"Bearer " + clientToken);

				headers.put("Content-Type", "application/json");


				GlobalHttp.post(url, bundle, headers,
						new Callback.CommonCallback<String>() {

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
										uinfo.setNick("匿名用户");
										db.saveUserInfo(uinfo);
										getUID(uinfo);


										startActivity(new Intent(WelcomeActivity.this,
												HomeActivity.class));
//										startActivity(new Intent(WelcomeActivity.this,
//												MainActivity2.class));
										finish();

//										return;

									} else if (status.equals("0")) {
										GlobalToast.toastShort(WelcomeActivity.this,
												(String) object.get("Message"));
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
								hideBgLoading();
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
						new Callback.CommonCallback<String>() {

							@Override
							public void onSuccess(String arg0) {

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
								hideBgLoading();
							}

							@Override
							public void onError(Throwable arg0, boolean arg1) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onCancelled(CancelledException arg0) {
								// TODO Auto-generated method stub
							}
						});

			}

		});
	}

	private class FunctionPagerAdapter extends PagerAdapter {
		private final int[] ids = new int[] { R.drawable.function_one, R.drawable.function_two, R.drawable.function_three, R.drawable.function_three };

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView v = new ImageView(WelcomeActivity.this);
			v.setBackgroundResource(ids[position]);
			container.addView(v);
			return v;
		}

		@Override
		public int getCount() {
			return ids.length;
		}
	};

	@Override
	protected void onResume() {
		JPushInterface.onResume(getApplicationContext());
		super.onResume();
	}

	@Override
	protected void onPause() {
		JPushInterface.onPause(getApplicationContext());
		super.onPause();
	}

}
