package com.sz.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qlk.util.R;

/**
 * Created by hudq on 2017/2/28.
 */

public class InputEditDialog extends Dialog {

    private Activity mContext;
    private String mTitle;
    private String mNegvText;
    private String mPosvText;
    private InputDialogCallback mCallback;

    private InputEditDialog(Activity context, String title, String posvText, String negvText,
                            InputDialogCallback callback) {
        super(context, R.style.SZ_LoadBgDialog);
        mContext = context;
        mTitle = title;
        mNegvText = negvText;
        mPosvText = posvText;
        mCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setOwnerActivity(mContext);
        setCanceledOnTouchOutside(false);
        this.setContentView(R.layout.pc_dialog_input_layout);

        final TextView tvTitle = ((TextView) findViewById(R.id.dialog_tv_title));
        tvTitle.setText(mTitle);
        final EditText name = (EditText) findViewById(R.id.dialog_et_name);
        final EditText pwd = (EditText) findViewById(R.id.dialog_et_pwd);
        if (!TextUtils.isEmpty(mPosvText)) {
            ((Button) findViewById(R.id.dialog_btn_positive)).setText(mNegvText);
        }
        if (!TextUtils.isEmpty(mNegvText)) {
            ((Button) findViewById(R.id.dialog_btn_negative)).setText(mNegvText);
        }

        findViewById(R.id.dialog_btn_negative).setOnClickListener(new View.OnClickListener
                () {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        findViewById(R.id.dialog_btn_positive).setOnClickListener(new View.OnClickListener
                () {
            @Override
            public void onClick(View v) {
                String nameText = name.getText().toString();
                String pwdText = pwd.getText().toString();
                if (mCallback != null) {
                    mCallback.onConfirm(nameText, pwdText);
                }
                dismiss();
            }
        });
    }

    public interface InputDialogCallback {
        void onConfirm(String name1, String pwd2);
    }


    public static class Builder {
        private Activity mContext;
        private String mTitle;
        private String mNegvText;
        private String mPosvText;
        private InputDialogCallback mCallback;

        public Builder setActivity(Activity context) {
            mContext = context;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setNegvText(String negvText) {
            mNegvText = negvText;
            return this;
        }

        public Builder setPosvText(String posvText) {
            mPosvText = posvText;
            return this;
        }

        public Builder setCallback(InputDialogCallback callback) {
            mCallback = callback;
            return this;
        }

        public InputEditDialog create() {
            return new InputEditDialog(mContext, mTitle, mPosvText, mNegvText, mCallback);
        }
    }
}
