package com.sz.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.qlk.util.R;

public class LoadingDialog extends Dialog {

	private Activity context;

	public LoadingDialog(Activity context) {
		super(context, R.style.SZ_LoadDialog);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setOwnerActivity(context);
		this.setContentView(R.layout.sz_loading);

		setCancelable(true);
		setCanceledOnTouchOutside(false);

		setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});
	}

}
