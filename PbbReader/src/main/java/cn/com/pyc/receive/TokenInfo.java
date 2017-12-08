package cn.com.pyc.receive;

import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 与服务器连接是需要Token
 * Created by QiLiKing on 15/11/10.
 */
public class TokenInfo implements Serializable
{
	private static final String TOKEN_VALUE = "token_value";
	private static final String TOKEN_TIME = "token_time";  //过期时间
	private static final long serialVersionUID = 6537564043934715964L;

	private String token;
	private long expires_in;

	public static TokenInfo getSavedToken(Context context)
	{
		TokenInfo info = new TokenInfo();
		SharedPreferences sp = context.getSharedPreferences("token_pbb", Context.MODE_PRIVATE);
		info.setToken(sp.getString(TOKEN_VALUE, null));
		info.setExpires_in(sp.getLong(TOKEN_TIME, 0));
		return info;
	}

	public static void saveToken(Context context, TokenInfo info)
	{
		SharedPreferences sp = context.getSharedPreferences("token_pbb", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(TOKEN_VALUE, info.getToken());
		editor.putLong(TOKEN_TIME, info.getExpires_in());
		editor.apply();
	}

	public String getValidToken()
	{
		if (System.currentTimeMillis() < getExpires_in())
		{
			return getToken();
		}

		return null;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public long getExpires_in()
	{
		return expires_in;
	}

	public void setExpires_in(long expires_in)
	{
		this.expires_in = expires_in;
	}
}
