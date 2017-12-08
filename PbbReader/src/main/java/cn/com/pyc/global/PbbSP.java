package cn.com.pyc.global;

import java.util.HashSet;
import java.util.Set;

import com.qlk.util.global.GlobalSp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PbbSP
{
	public static final String SP_FIRST_FUNCTION = "first_function";
	public static final String SP_FIRST_LOGIN = "first_login";
	public static final String SP_IS_FROM_CHANGE_KEY = "is_from_change_key";
	public static final String SP_FIRST_ENCRYPT = "first_encrypt";
	public static final String SP_SET_NICKNAME = "set_nickname";
	public static final String SP_CAN_MAKE_SAFE_SEND_FILE = "can_make_safe_send_file";
	public static final String SP_HELP_VERSION = "help_version";
	public static final String SP_GUIDE_CODE_CLICK = "guide_code";
	public static final String SP_GUIDE_USER_FIRST_CLICK = "guide_user_first_click";
	public static final String SP_GUIDE_CIPHERTTEXT_FIRST_CLICK = "guide_cipherttext_first_click";
	public static final String SP_GUIDE_DECRYPT_LAYOUT_FIRST_SHOW = "guide_decrypt_layout_first_show";
	public static final String SP_GUIDE_SAFE_SEND_FIRST_SHOW = "guide_safe_send_first_show";
	public static final String SP_SHOW_WX_DIALOG = "show_wx_dialog";
	public static final String SP_BINDED_SUCCESS = "binded_success";
	public static final String SP_NEED_PASSWORD = "need_password";
	public static final String SP_AUTO_SEARCH = "auto_search";
	public static final String SP_SHOW_GUIDE_NOTICE = "show_guide_notice";
	public static final String SP_PUSH_WRITINGS = "push_writings";
	public static final String SP_PUSH_VOICE = "push_voice";
	public static final String SP_PUSH_ZHEN = "push_zhen";
	public static final String SP_PUSH_SETALIS_SUCCESS = "set_alias_success";
	public static final String SP_LOGIN = "login";

	private static final String SP = "CBaseFragmentActivity";

	// 这两个在第二次迭代中被SP_BINDED_SUCCESS替代
	public static final String SP_EMAIL_BINDED = "check_email";
	public static final String SP_PHONE_BINDED = "check_phone";

	public static GlobalSp getGSP(Context context)
	{
		return GlobalSp.getGSP(context, SP);
	}

	public static void clearSP(Context context)
	{
		getGSP(context).putValue(SP_FIRST_ENCRYPT, true);
		getGSP(context).putValue(SP_GUIDE_CIPHERTTEXT_FIRST_CLICK, true);
		getGSP(context).putValue(SP_GUIDE_CODE_CLICK, true);
		getGSP(context).putValue(SP_GUIDE_DECRYPT_LAYOUT_FIRST_SHOW, true);
		getGSP(context).putValue(SP_GUIDE_USER_FIRST_CLICK, true);
	}

	public static void setAutoSearchText(Context context, String searchText)
	{
		SharedPreferences sp = getGSP(context).getSharedPreferences();
		Set<String> autos = sp.getStringSet(PbbSP.SP_AUTO_SEARCH,
				new HashSet<String>());

		if (!autos.contains(searchText))
		{
			if (autos.size() == 20)
			{
				autos.remove(autos.iterator().next());	// 最多20个
			}
			autos.add(searchText);
			Editor editor = sp.edit();
			editor.putStringSet(PbbSP.SP_AUTO_SEARCH, autos);
			editor.commit();
		}
	}
}
