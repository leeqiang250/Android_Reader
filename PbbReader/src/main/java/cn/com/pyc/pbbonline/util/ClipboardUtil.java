package cn.com.pyc.pbbonline.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.sz.mobilesdk.util.CommonUtil;
import com.sz.mobilesdk.util.SZLog;

public class ClipboardUtil
{
	/**
	 * 监视剪贴板内容
	 * 
	 * @param context
	 * @return
	 */
	public static String eyesClipboard(Context context)
	{
		final ClipboardManager cm = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData data = cm.getPrimaryClip();
		if (data == null)
			return "";
		ClipData.Item item = data.getItemAt(0);
		if (item == null)
			return "";
		if (item.getText() == null)
			return "";
		String clipContent = item.getText().toString();
		SZLog.e("clipboard", "content: " + clipContent);
		return clipContent;
	}

	/**
	 * 清空剪贴板内容
	 * 
	 * @param context
	 */
	public static void clearClipboard(Context context)
	{
		CommonUtil.copyText(context, "");
	}
}
