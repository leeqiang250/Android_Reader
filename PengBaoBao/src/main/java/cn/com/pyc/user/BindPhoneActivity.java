package cn.com.pyc.user;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.qlk.util.global.GlobalTask;
import com.qlk.util.global.GlobalToast;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.com.pyc.pbb.R;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.pbbonline.util.ViewHelp;
import cn.com.pyc.utils.Constant;
import cn.com.pyc.xcoder.XCoder;

public class BindPhoneActivity extends UserBaseActivity implements
        OnClickListener {
    private UserDao db = UserDao.getDB(this);
    private UserInfo userInfo;
    private String userToken;
    private String clientToken;

    public static final String SECURITY_CODE = "security_code";

    private static short Security_Time = 60;
    private static long Exit_Time;

    private EditText g_edtPhone;
    private EditText g_edtSecurity;
    private Button g_btnGetSecurity;
    private ImageView g_imvBack;
    private TextView g_txtSubmit;
    private String securityCode;
    private String str;

    private Handler handler;

    private final HashMap<String, String> time = new HashMap<String, String>();
    private final HashMap<String, String> masgid = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_check_phone);
        ViewHelp.showAppTintStatusBar(this);

        userInfo = db.getUserInfo();
        handler = new Handler();

        g_txtSubmit = (TextView) findViewById(R.id.aucp_txt_submit);
        g_txtSubmit.setOnClickListener(this);
//        g_txtSubmit.setTextColor(getResources().getColor(R.color.disabled));
        g_txtSubmit.setClickable(false);

        g_edtPhone = (EditText) findViewById(R.id.aucp_edt_phone);
        g_edtSecurity = (EditText) findViewById(R.id.aucp_edt_security);
        g_btnGetSecurity = (Button) findViewById(R.id.aucp_btn_get_security);
        g_btnGetSecurity.setOnClickListener(this);

        g_imvBack = (ImageView) findViewById(R.id.acp_imb_back);
        g_imvBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aucp_btn_get_security:
                String phone = g_edtPhone.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    GlobalToast.toastShort(this, "请输入手机号");
                } else if (!isMobileNum(phone)) {
                    GlobalToast.toastShort(this, "手机号不合法");
                } else {
//					getSecurity(phone);
                    showLoading(BindPhoneActivity.this);
                    getClientToken(userInfo, phone);
                }
                break;

            case R.id.aucp_txt_submit:
                String security = g_edtSecurity.getText().toString().trim();
                String phone1 = g_edtPhone.getText().toString().trim();
                if (TextUtils.isEmpty(securityCode)
                        || TextUtils.isEmpty(security)) {
                    GlobalToast.toastShort(this, "请输入验证码");
                }
                if (TextUtils.isEmpty(security)) {
                    GlobalToast.toastShort(this, "请输入验证码");
                }

//				if ( !security.equals(securityCode))
//				{
//					GlobalToast.toastShort(this, "请输入正确的验证码");
//				}
//				else
//				{
                if (outDate(time.get(securityCode))) {
                    GlobalToast.toastShort(this, "验证码已过期，请重新获取");
                } else {
                    showLoading(BindPhoneActivity.this);
                    getPhonebindUserToken(userInfo, security, phone1);
                }

//				}
                break;
            case R.id.acp_imb_back:

                finish();
                break;

            default:
                break;
        }
    }

//	private String msgId;

    private void getClientToken(final UserInfo userInfo, final String phone) {

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
                        // 解析Json
                        try {
                            JSONObject object = new JSONObject(arg0);
                            clientToken = (String) object
                                    .get("access_token");
                            getSecurity(userInfo, phone);
                            System.out.println("ClientToken=" + clientToken);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinished() {
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        hideLoading();
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                    }
                });
    }

    private void getSecurity(final UserInfo uinfo, final String phone) {


        String securityUrl = Constant.UserSourceHost + "api/v1/userphonecode";

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("phone", phone);

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization",
                "Bearer " + clientToken);

        headers.put("Content-Type", "application/x-www-form-urlencoded");
//		headers.put("Content-Type", "application/json");


        GlobalHttp.get(securityUrl, bundle, headers,
                new CommonCallback<String>() {

                    @Override
                    public void onSuccess(String arg0) {
                        System.out.println("获取验证码-返回obj:" + arg0);

                        // 解析Json
                        try {
                            JSONObject object = new JSONObject(arg0);
                            String status = (String) object
                                    .get("Status");

                            if (status.equals("1")) {
                                System.out
                                        .println("返回的status=" + status);
                                GlobalToast.toastShort(getApplicationContext(), "获取成功");
//								finish();
                                hideLoading();


                                // 当前时间
                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                String currentTime = sdf.format(new Date()).toString();
                                securityCode = "codeSend";
                                time.put(securityCode, currentTime);
//								masgid.put(securityCode, msgId);
                                str = securityCode;

                                start();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
//                                        g_txtSubmit.setTextColor(getResources().getColor(
//                                                R.color.white));
                                        g_txtSubmit.setClickable(true);
                                    }
                                });
                            } else if (status.equals("0")) {
                                GlobalToast.toastShort(getApplicationContext(), (String) object
                                        .get("Message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinished() {
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        hideLoading();
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                    }
                });

    }
//	private void getSecurity(final String phone)
//	{
//		GlobalTask.executeNetTask(this, new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				SmInfo info = new SmInfo();
//				info.setPhone(phone);
//				System.out.println("aaa");
//				SmResult sr = new SmConnect(getApplicationContext())
//						.getPhoneSecurityCode(info, false);
//				if (sr.succeed())
//				{
//					userInfo.setPhone(phone);
//					UserDao.getDB(BindPhoneActivity.this)
//							.saveUserInfo(userInfo);
//
//					msgId = sr.getSmInfo().getMsgId();
//					securityCode = sr.getSmInfo().getSecurityCode();
//
//					// 当前时间
//					SimpleDateFormat sdf = new SimpleDateFormat(
//							"yyyy-MM-dd HH:mm:ss");
//					String currentTime = sdf.format(new Date()).toString();
//
//					time.put(securityCode, currentTime);
//					masgid.put(securityCode, msgId);
//					str = securityCode;
//
//					start();
//					handler.post(new Runnable()
//					{
//						@Override
//						public void run()
//						{
//							g_txtSubmit.setTextColor(getResources().getColor(
//									R.color.white));
//							g_txtSubmit.setClickable(true);
//						}
//					});
//				}
//				else
//				{
//					if (sr.getSuc() == 256)
//					{
//						GlobalToast.toastShort(getApplicationContext(),
//								"验证失败，该手机号已使用!");
//					}
//					// else
//					// {
//					// GlobalToast.toastShort(getApplicationContext(),
//					// sr.getFailureReason());
//					// }
//				}
//			}
//		});
//	}

    private boolean outDate(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        String systemTime = sdf.format(new Date());


        Date begin = null;
        Date end = null;
        try {
            begin = sdf.parse(time);
            end = sdf.parse(systemTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long between = (end.getTime() - begin.getTime()) / 1000;// 除以1000是为了转换成秒

        if (between / 60 >= 5) // 如果超过5分钟
        {
            return true;
        }

        return false;
    }

    /**********************************
     * 验证手机时：获取验证码后立马返回然后再进入
     *******************************/

    private void start() {
        handler.post(refresh);
    }

    private void interrupt() {
        handler.removeCallbacks(refresh);
    }

    private void stop() {
        handler.removeCallbacks(refresh);
        handler.post(new Runnable() {
            @Override
            public void run() {
                g_btnGetSecurity
                        .setBackgroundResource(R.drawable.xml_imb_green);
                g_btnGetSecurity.setText("获取验证码");
                g_btnGetSecurity.setClickable(true);
                Security_Time = 60;
            }
        });
    }

    private Runnable refresh = new Runnable() {
        public void run() {
            if (Security_Time > 0) {
                g_btnGetSecurity.setBackgroundColor(Color.GRAY);
                g_btnGetSecurity.setText(Security_Time + "秒");
                g_btnGetSecurity.setClickable(false);
                Security_Time--;
                handler.postDelayed(refresh, 1000);
            } else {
                g_btnGetSecurity
                        .setBackgroundResource(R.drawable.xml_imb_green);
                g_btnGetSecurity.setText("获取验证码");
                g_btnGetSecurity.setClickable(true);
                Security_Time = 60;
            }
        }
    };

    private void checkSecurityRight(final String security, final String phone) {
        GlobalTask.executeNetTask(this, new Runnable() {
            @Override
            public void run() {

                String checkSecurityUrl = Constant.UserSourceHost + "api/v1/userphone";

                // 请求参数
                Bundle bundle = new Bundle();
                bundle.putString("phone", phone);
                bundle.putString("code", security);
                bundle.putString("type", "bind");

                // 请求头
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",
                        "Bearer " + userToken);

//				headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Content-Type", "application/json");


                GlobalHttp.post(checkSecurityUrl, bundle, headers,
                        new CommonCallback<String>() {

                            @Override
                            public void onSuccess(String arg0) {
                                System.out.println("checkSecurity返回obj:" + arg0);

                                // 解析Json
                                try {
                                    JSONObject object = new JSONObject(arg0);
                                    String status = (String) object
                                            .get("Status");

                                    if (status.equals("1")) {
                                        userInfo.setPhone(phone);
                                        userInfo.setPhoneBinded(true);
                                        userInfo.setPhoneBinded(1);
                                        db.saveUserInfo(userInfo);
                                        GlobalToast.toastShort(getApplicationContext(), "绑定成功");
                                        finish();
                                    } else if (status.equals("0")) {
                                        GlobalToast.toastShort(getApplicationContext(), (String) object.get("Message"));
//                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFinished() {
                                hideLoading();
                            }

                            @Override
                            public void onError(Throwable arg0, boolean arg1) {
                                System.out.println(arg0.getMessage());
                            }

                            @Override
                            public void onCancelled(CancelledException arg0) {
                            }
                        });


				/*SmInfo info = new SmInfo();
				info.setPhone(security);
				info.setMsgId(msgId);

				SmResult sr = new SmConnect(getApplicationContext())
						.sendPhoneSecurityCode(info, true);
				if ( (sr.getSuc()&1)>0)
				{
					finish();
				}
				// else if (sr.getSuc() == 256)
				// {
				// GlobalToast.toastShort(getApplicationContext(),
				// "验证失败，该手机号已使用");
				// }
				else
				{
					GlobalToast.toastShort(getApplicationContext(), "手机验证失败");
				}*/
            }
        });
    }


    private void getPhonebindUserToken(final UserInfo userInfo, final String security, final String phone1) {
        String userTokenUrl = Constant.UserTokenHost;

        // 请求参数
        Bundle bundle = new Bundle();
        bundle.putString("grant_type", "password");
        bundle.putString("username", userInfo.getUserName());
        if (!TextUtils.isEmpty(userInfo.getPsd())) {
            bundle.putString("password", userInfo.getPsd());
        } else {
            bundle.putString("password", "n|" + XCoder.getHttpEncryptText(userInfo.getUserName()));
        }

        // 请求头
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(PhoneInfo.testID + ":" + PhoneInfo.testPSD));
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        GlobalHttp.post(userTokenUrl, bundle, headers, new CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                // 解析Json
                try {
                    JSONObject object = new JSONObject(arg0);
                    userToken = (String) object.get("access_token");
                    checkSecurityRight(security, phone1);
//					bindQQ(userInfo);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFinished() {
                hideLoading();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void onCancelled(CancelledException arg0) {
            }
        });

    }

    private boolean isMobileNum(String phone) {
        // Pattern pattern = Pattern.compile("1\\d{10}");
        // Matcher matcher = pattern.matcher(phone);
        // return matcher.matches();
        return phone.matches("1\\d{10}");
    }
}
