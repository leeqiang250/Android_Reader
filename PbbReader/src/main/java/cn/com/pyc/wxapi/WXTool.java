package cn.com.pyc.wxapi;

import com.qlk.util.global.GlobalToast;

import cn.com.pyc.global.PbbSP;
import cn.com.pyc.pbb.reader.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class WXTool
{
	//	public static final String WX_APP_ID = "wx49b46f184e65e4de"; // 正版
	// public static final String WX_APP_ID = "wx9e493aeb1e1d77ca"; // 测试
	//	public static final String WX_TITLE = "Hi !给你传个文件认真看哦";
	public static boolean isFromWxMake = false;
	public static boolean isFromWxRead = false;

	//	public static String wxFilePath;
	//	public static String wxFileName;
	//	public static Bundle wxBundle;
	//
	//	public static void clear()
	//	{
	//		isFromWxMake = false;
	//		isFromWxRead = false;
	//		wxFileName = null;
	//		wxFilePath = null;
	//		wxBundle = null;
	//	}
	//
	//	public static IWXAPI registerToWX(Context context)
	//	{
	//		// 第三个参数检查是否为官方签名版本
	//		IWXAPI api = WXAPIFactory.createWXAPI(context, WXTool.WX_APP_ID, false);
	//		api.registerApp(WX_APP_ID);
	//		return api;
	//	}

	public static void payAttentionToWeiXin(final Context context)
	{
		if ((boolean) PbbSP.getGSP(context).getValue(PbbSP.SP_SHOW_WX_DIALOG, true))
		{
			View v = LayoutInflater.from(context).inflate(R.layout.dialog_openwx, null);
			final Dialog dialog = new Dialog(context, R.style.no_frame_small);
			dialog.setContentView(v);
			dialog.show();
			((CheckBox) v.findViewById(R.id.do_cbx_notprompt))
					.setOnCheckedChangeListener(new OnCheckedChangeListener()
					{
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
						{
							PbbSP.getGSP(context).putValue(PbbSP.SP_SHOW_WX_DIALOG, !isChecked);
						}
					});
			v.findViewById(R.id.do_btn_know).setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dialog.cancel();
					openWeiXin(context);
				}
			});
		}
		else
		{
			openWeiXin(context);
		}

	}

	private static void openWeiXin(Context context)
	{
		try
		{
			/*-
			 * 这些东西对新版微信可能已不起作用，但该如何变动，还没找到方法
			 */
			Intent intent = new Intent("gh_51c072bea49e");
			intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
			intent.putExtra("LauncherUI_From_Biz_Shortcut", true);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 这句话不能少
			context.startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			GlobalToast.toastShort(context, "您还未安装微信！");
		}
	}

}
