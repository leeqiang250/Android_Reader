package com.qlk.util.global;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 将SharedPreferences操作简化为putValue和getValue两个操作
 * <p>
 * 如果觉得麻烦，可以调用getSharedPreferences()自己操作
 * 
 * @author QiLiKing 2015-6-30 下午5:20:40
 */
public final class GlobalSp
{
	private static final String DEFAULT_NAME = "qlk";

	protected Context mContext;
	private String mSpName;

	private static GlobalSp gsp;

	/**
	 * 以”qlk“命名的SharedPreferences
	 * 
	 * @param context
	 * @return
	 */
	public static GlobalSp getDefaultSP(Context context)
	{
		return getGSP(context, DEFAULT_NAME);
	}

	/**
	 * 自定义SharedPreferences的名字
	 * 
	 * @param context
	 * @param spName
	 * @return
	 */
	public static GlobalSp getGSP(Context context, String spName)
	{
		if (gsp == null)
		{
			gsp = new GlobalSp();
		}
		gsp.mContext = context;
		gsp.mSpName = spName;

		return gsp;
	}

	private GlobalSp()
	{
	}

	/**
	 * 会根据value的类型自动put值相应类型
	 * 
	 * @param mContext
	 * @param key
	 * @param value
	 *            如果是long，value建议带上”L“后缀（其他类型同理）；如果是集合，只接受String类型的Set
	 */
	@SuppressWarnings("unchecked")
	public void putValue(String key, Object value)
	{
		SharedPreferences sp = getSharedPreferences();
		Editor editor = sp.edit();
		if (value instanceof Boolean)
		{
			editor.putBoolean(key, (Boolean) value);
		}
		else if (value instanceof String)
		{
			editor.putString(key, (String) value);
		}
		else if (value instanceof Integer)
		{
			editor.putInt(key, (Integer) value);
		}
		else if (value instanceof Long)
		{
			editor.putLong(key, (Long) value);
		}
		else if (value instanceof Float)
		{
			editor.putFloat(key, (Float) value);
		}
		else if (value instanceof Set<?>)
		{
			editor.putStringSet(key, (Set<String>) value);
		}
		else
		{
			//
		}
		editor.commit();
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return 强转成key对应的类型即可
	 */
	public Object getValue(String key, Object defaultValue)
	{
		SharedPreferences sp = getSharedPreferences();
		Object value = sp.getAll().get(key);
		if (value == null)
		{
			value = defaultValue;
		}
		return value;
	}

	public SharedPreferences getSharedPreferences()
	{
		return mContext.getSharedPreferences(mSpName, Context.MODE_PRIVATE);
	}
}
