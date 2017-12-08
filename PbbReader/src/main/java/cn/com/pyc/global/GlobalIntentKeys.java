package cn.com.pyc.global;

import android.app.Activity;
import android.content.Intent;

public class GlobalIntentKeys
{
	public static final String BUNDLE_FLAG_CHANGE_LIMIT = "change_limit";
	public static final String BUNDLE_FLAG_GET_OLD_KEY = "get_old_key";
	public static final String BUNDLE_FLAG_CIPHER = "cipher"; // 密文环境，其实应该是BUNDLE_CIPHER_ENVIRONMENT
	public static final String BUNDLE_FLAG_FROM_READER = "from_reader";
	public static final String BUNDLE_FLAG_FORM_SM = "from_sm";
	public static final String BUNDLE_FLAG_PAY_MODE = "pay_mode"; // 是否是付款文件的模式
	public static final String BUNDLE_FLAG_AUTO_READ = "auto_read";	//是否能自动阅读（第12位）

	public static final String BUNDLE_DATA_PATHS = "paths";
	public static final String BUNDLE_DATA_PATH = "path"; // 本程序传路径皆用此bundle，不再使用intent.getData()
	public static final String BUNDLE_DATA_EXTINFO = "extinfo";
	public static final String BUNDLE_DATA_TOTAL = "total";
	public static final String BUNDLE_DATA_FOLDER = "folder";
	public static final String BUNDLE_DATA_PROGRESS = "current";
	public static final String BUNDLE_DATA_TYPE = "type";
	public static final String BUNDLE_DATA_BYTES = "bytes";
	public static final String BUNDLE_DATA_BYTES_PDF = "bytes_pdf";

	public static final String BUNDLE_OBJECT_MEDIA_TYPE = "media_type";
	public static final String BUNDLE_OBJECT_SM_INFO = "sm_info";
	public static final String BUNDLE_OBJECT_PDF_OUTLINE = "pdf_outline";
	public static final String BUNDLE_OBJECT_WEB_PAGE = "web_page";
	public static final String USER_REGISTER = "user_register";
	public static final String PASSWORD_REGISTER = "password_register";

	public static Intent reUseIntent(Intent intent, Activity from, Class<?> to)
	{
		return intent == null ? new Intent(from, to) : intent
				.setClass(from, to);
	}

	public static Intent reUseIntent(Activity from, Class<?> to)
	{
		return reUseIntent(from.getIntent(), from, to);
	}

}
