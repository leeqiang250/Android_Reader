package cn.com.pyc.bean;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sz.mobilesdk.common.Fields;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SPUtil;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;

import java.util.HashMap;
import java.util.UUID;

import cn.com.pyc.global.GlobalHttp;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.receive.TokenInfo;
import cn.com.pyc.xcoder.XCoder;

/**
 * 服务器需要的手机设备信息
 * 
 * @author QiLiKing 2015-7-29 下午2:47:13
 */
public class PhoneInfo
{
	public static final int appType = 28;		// 服务器区分客户端，andorid端是28

	public static final String sysInfo = Build.BRAND + "---" + android.os.Build.MODEL;		// 山寨手机只靠Build.MODEL看不出任何东西

	public static final int version = 12;		// ͨ服务器以此判断客户端是否需要升级（suc的第10位）

	public static final int fileVersion = 1;	// 1表示制作的外发文件尾部是三个结构：文件头结构、文件头扩展结构、离线结构（该结构已废除）

	public static final String testID = "pbbandroid0";
	public static final String testPSD = "84n109f3";

/*
* 要给服务器传的API 版本
*
* */
	public static int getAPIVersion(Context context)
	{
		//2.3.7.20170317_Release   6.3.0_Debug
		String versionStr = getVersionStr(context);

		String seperator = "\\.";

		String recources[] = versionStr.split(seperator);

		int apiVer = Integer.parseInt(recources[0]) * 1000000 + Integer.parseInt(recources[1]) * 10000 + Integer.parseInt(recources[2]) * 100;
		Log.i("getAPIVersion","-----VERSION:"+versionStr + "----APIVERSION:" + apiVer);

		return apiVer;
//		return 2030700;
	}

	/**
	 * 设备唯一标识，没有sim卡的手机没有DeviceId，例如pad
	 * 
	 * @param context
	 * @return
	 */
	public static String getUUID(Context context)
	{
		String deviceId_ = (String)SPUtil.get("phone_deviceId","");
		if (!TextUtils.isEmpty(deviceId_)) {
			return deviceId_;
		}
		final TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (tm.getDeviceId() != null)
		{
			SPUtil.save("phone_deviceId",tm.getDeviceId());
			return tm.getDeviceId();
		}
		/* 网上找的方法 */

		final String deviceId, simSerial, androidId;
		deviceId = "" + tm.getDeviceId();
		simSerial = "" + tm.getSimSerialNumber();
		androidId = "" + Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long) deviceId.hashCode() << 32)
				| simSerial.hashCode());
		String uniqueId = deviceUuid.toString();
		SPUtil.save("phone_deviceId",uniqueId);
		return uniqueId;
	}

	public static String getVersionStr(Context context)
	{
		//直接使用版本号
		//return context.getResources().getString(R.string.app_version);
		return CommonUtil.getAppVersionName(context);
	}

	/**
	 * @deprecated Use "getUUID" instead.
	 */
	public static String getDeviceID(Context context)
	{
		final TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	/**
	 * 获取clientToken
	 * @param context
	 * @return
	 */
//	public static String getClientToken(Context context)
//	{
//		String url = "http://login.pyc.com.cn/token";
//		 String clientToken;
//
//		//请求参数
//		Bundle bundle = new Bundle();
//		bundle.putString("grant_type", "client_credentials");
//
//		//请求头
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(testID + ":" + testPSD));
//		headers.put("Content-Type", "application/x-www-form-urlencoded");
//
//		HttpEngine.post(url, bundle, headers, new CommonCallback<String>()
//		{
//
//			@Override
//			public void onSuccess(String arg0)
//			{
//				//解析Json
//				try
//				{
//					JSONObject object = new JSONObject(arg0);
//					String tokenString  = (String) object.get("access_token");
//					
//				}
//				catch (JSONException e)
//				{
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void onFinished()
//			{
//				// TODO Auto-generated method stub
//			}
//
//			@Override
//			public void onError(Throwable arg0, boolean arg1)
//			{
//				// TODO Auto-generated method stub
//			}
//
//			@Override
//			public void onCancelled(CancelledException arg0)
//			{
//				// TODO Auto-generated method stub
//			}
//		});
		
//		TokenInfo tokenInfo = TokenInfo.getSavedToken(context);
//		String token = tokenInfo.getValidToken();
//		if (token == null)
//		{
//			getMyClientToken(context);
//			tokenInfo = TokenInfo.getSavedToken(context);
//			token = tokenInfo.getValidToken();
//		}
		
		
//	}

	private static void getMyClientToken(final Context context)
	{
		String url = "http://login.pyc.com.cn/token";

		//请求参数
		Bundle bundle = new Bundle();
		bundle.putString("grant_type", "client_credentials");

		//请求头
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(testID + ":" + testPSD));
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		GlobalHttp.post(url, bundle, headers, new CommonCallback<String>()
		{

			@Override
			public void onSuccess(String arg0)
			{
				//解析Json
				try
				{
					JSONObject object = new JSONObject(arg0);
					String tokenString = (String) object.get("access_token");
					int timestmp = object.getInt("expires_in");
					TokenInfo tokenInfo = new TokenInfo();
					tokenInfo.setToken(tokenString);
					tokenInfo.setExpires_in(System.currentTimeMillis() + timestmp);
					TokenInfo.saveToken(context, tokenInfo);
					System.out.println("access_token_client:"+tokenInfo.getToken());
				}
				catch (JSONException e)
				{
					ExtraParams ep = new ExtraParams();
					ep.account_name = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
					String password = (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, "");
					ep.account_password = AESUtil.encrypt(password);
					StackTraceElement[] trace =e.getStackTrace();
					if(trace==null||trace.length==0){
						ep.lines = -1;
					}else {
						ep.lines = trace[0].getLineNumber();
					}
					LogerEngine.error(context, "解析获取token的json数据失败" + Log.getStackTraceString(e), true, ep);
					e.printStackTrace();
				}
			}

			@Override
			public void onFinished()
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onCancelled(CancelledException arg0)
			{
				// TODO Auto-generated method stub
			}
		});
	}

	
	/**
	 * 获取UserToken
	 * @param context
	 * @return
	 */
	public static String getUserToken(Context context,UserInfo userinfo)
	{
		TokenInfo tokenInfo = TokenInfo.getSavedToken(context);
		String token = tokenInfo.getValidToken();
		if (token == null)
		{
			getMyUserToken(context,userinfo);
			tokenInfo = TokenInfo.getSavedToken(context);
			token = tokenInfo.getValidToken();
		}

		return token;
	}

	private static void getMyUserToken(final Context context,final UserInfo userInfo)
	{
		String url = "http://login.pyc.com.cn/token";

		//请求参数
		Bundle bundle = new Bundle();
		bundle.putString("grant_type", "password");
		bundle.putString("username", userInfo.getUserName());
		if (userInfo.getPsd()!=null || userInfo.getPsd().trim().length()!=0)
		{
			bundle.putString("password", userInfo.getPsd());
		}else {
			bundle.putString("password", "m|"+XCoder.getHttpEncryptText(userInfo.getUserName()));
		}

		//请求头
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Basic " + SecurityUtil.encryptBASE64(testID + ":" + testPSD));
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		GlobalHttp.post(url, bundle, headers, new CommonCallback<String>()
		{

			@Override
			public void onSuccess(String arg0)
			{
				//解析Json
				try
				{
					JSONObject object = new JSONObject(arg0);
					String tokenString = (String) object.get("access_token");
					int timestmp = object.getInt("expires_in");
					TokenInfo tokenInfo = new TokenInfo();
					tokenInfo.setToken(tokenString);
					tokenInfo.setExpires_in(System.currentTimeMillis() + timestmp);
					TokenInfo.saveToken(context, tokenInfo);
				}
				catch (JSONException e)
				{
					ExtraParams ep = new ExtraParams();
					ep.account_name = (String) SPUtil.get(Fields.FIELDS_LOGIN_USER_NAME, "");
					String password = (String) SPUtil.get(Fields.FIELDS_LOGIN_PASSWORD, "");
					ep.account_password = AESUtil.encrypt(password);
					StackTraceElement[] trace =e.getStackTrace();
					if(trace==null||trace.length==0){
						ep.lines = -1;
					}else {
						ep.lines = trace[0].getLineNumber();
					}
					LogerEngine.error(context, "获取MyUserToken解析json失败" + Log.getStackTraceString(e), true, ep);
					e.printStackTrace();
				}
			}

			@Override
			public void onFinished()
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onError(Throwable arg0, boolean arg1)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onCancelled(CancelledException arg0)
			{
				// TODO Auto-generated method stub
			}
		});
	}
}
