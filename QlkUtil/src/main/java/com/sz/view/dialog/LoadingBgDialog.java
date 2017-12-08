package com.sz.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.qlk.util.R;

public class LoadingBgDialog extends Dialog
{

	private Activity context;
	private String content;
	private TextView tvText;

	private boolean mHasStarted;

	public LoadingBgDialog(Activity context)
	{
		super(context, R.style.SZ_LoadBgDialog);
		this.context = context;
	}

	public LoadingBgDialog(Activity context, String content)
	{
		super(context, R.style.SZ_LoadBgDialog);
		this.context = context;
		this.content = content;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		mHasStarted = true;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		mHasStarted = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		this.setOwnerActivity(context);
		this.setContentView(R.layout.sz_bgloading);
		tvText = (TextView) this.findViewById(R.id.tv_loading_content);
		if (TextUtils.isEmpty(content))
		{
			tvText.setVisibility(View.GONE);
		} else
		{
			tvText.setVisibility(View.VISIBLE);
			tvText.setText(content);
		}

		setCancelable(true);
		setCanceledOnTouchOutside(false);

		setOnDismissListener(new OnDismissListener()
		{

			@Override
			public void onDismiss(DialogInterface dialog)
			{
				if (dialog != null)
				{
					dialog.dismiss();
				}
			}
		});
	}

	/**
	 * 更新dialog中textview的显示
	 * 
	 * @param content
	 */
	public void setContentText(String content)
	{
		this.content = content;

		if (mHasStarted)
		{
			tvText.setText(content);
		}
	}
}
