package cn.com.pyc.media;

import java.util.ArrayList;

import cn.com.pyc.pbb.R;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.transmission.wifi.tool.ServerControlActivity;
import cn.com.pyc.user.UserInfoActivity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ExtraDelSndFile
{

	public static void sendFile(Context context, String path)
	{
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path)); // value必须是Parcelable
		context.startActivity(intent);
	}

	/**
	 * 发送普通文件
	 * 
	 * @param act
	 * @param paths
	 * @param type
	 */
	public void sendFiles(final Context context, final ArrayList<String> paths,
			final GlobalData type)
	{
		UserInfo userInfo = UserDao.getDB(context).getUserInfo();
		/*-
		 * 需求规定：发送文件时，三个至少验证其一
		 */
		if (userInfo.isEmailBinded() || userInfo.isPhoneBinded() || userInfo.isQqBinded())
		{
			View v = LayoutInflater.from(context)
					.inflate(R.layout.dialog_select_transmission, null);
			final RadioGroup rdg = (RadioGroup) v.findViewById(R.id.dst_rdg_transmit);
			final Dialog dialog = new Dialog(context, R.style.no_frame_small);
			dialog.setContentView(v);
			dialog.show();

			v.findViewById(R.id.dst_btn_ok).setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					switch (rdg.getCheckedRadioButtonId())
					{
						case R.id.dst_rdb_wifi:	// wifi发送
							Intent intent = new Intent(context, ServerControlActivity.class);
							intent.putStringArrayListExtra(GlobalIntentKeys.BUNDLE_DATA_PATHS,
									paths);
							context.startActivity(intent);
							break;

						case R.id.dst_rdb_bluetooth:	// 蓝牙发送
							sendFile(context, paths, type);
							break;

						default:
							break;
					}

					dialog.cancel();
				}
			});
			v.findViewById(R.id.dst_btn_cancel).setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dialog.cancel();
				}
			});
		}
		else
		{
			showCheckDialog(context);
		}
	}

	private void showCheckDialog(final Context context)
	{
		// 验证邮箱
		View v = LayoutInflater.from(context).inflate(R.layout.dialog_receive, null);
		final Dialog dialog = new Dialog(context, R.style.no_frame_small);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(v);
		dialog.show();
		((TextView) v.findViewById(R.id.dd_txt_content)).setText("您还未完成主人信息验证，不能进行此操作！");
		((Button) v.findViewById(R.id.dd_btn_sure)).setText("现在验证");
		((Button) v.findViewById(R.id.dd_btn_cancel)).setText("稍后验证");
		v.findViewById(R.id.dd_btn_sure).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				context.startActivity(new Intent(context, UserInfoActivity.class));
				dialog.cancel();
			}
		});
		v.findViewById(R.id.dd_btn_cancel).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.cancel();
			}
		});
	}

	// 用蓝牙发送文件
	private void sendFile(Context context, ArrayList<String> paths, GlobalData type)
	{
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		String mime = type.equals(GlobalData.Image) ? "image/*"
				: type.equals(GlobalData.Pdf) ? "application/pdf" : "video/*";
		intent.setType(mime);
		// 这里setClassName就是指定蓝牙，不写这句就弹出选择用什么发送
		// 有蓝牙啊，gmail啊，彩信之类的
		intent.setClassName("com.android.bluetooth",
				"com.android.bluetooth.opp.BluetoothOppLauncherActivity");
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (String path : paths)
		{
			uris.add(Uri.parse("file://" + path));
		}
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		context.startActivity(intent);
	}

}
