package com.sz.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qlk.util.R;
import com.sz.mobilesdk.util.FormatterUtil;

import java.text.NumberFormat;

/**
 * 显示progressbar的dialog
 * <p>
 * Created by hudq on 2017/1/18.
 */

public class ProgressBarDialog extends Dialog {

    private Activity context;

    private String title;
    private String message;
    private int progress;
    private int max;
    private long currentSize, totalSize;

    private TextView mTitleTextView;
    private TextView mMsgTextView;
    private TextView mNumberTextView;
    private TextView mPercentTextView;
    private ProgressBar mProgressBar;

    private boolean mHasStarted;
    private Handler mViewUpdateHandler;
    private NumberFormat mProgressPercentFormat;
    private String mProgressNumberFormat;

    public ProgressBarDialog(Activity context) {
        super(context, R.style.SZ_LoadBgDialog);
        this.context = context;

        mProgressNumberFormat = "%1s/%2s";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setOwnerActivity(context);
        this.setContentView(R.layout.dialog_progressbar);
        setCanceledOnTouchOutside(false);

        mTitleTextView = (TextView) findViewById(R.id.dialog_pb_title);
        mMsgTextView = (TextView) findViewById(R.id.dialog_pb_msg);
        mNumberTextView = (TextView) findViewById(R.id.dialog_pb_number);
        mPercentTextView = (TextView) findViewById(R.id.dialog_pb_percent);
        mProgressBar = (ProgressBar) findViewById(R.id.dialog_pb_progress);

        mViewUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mProgressNumberFormat != null) {
                    String format = mProgressNumberFormat;
                    mNumberTextView.setText(String.format(format, FormatterUtil.formatSize
                            (currentSize), FormatterUtil.formatSize(totalSize)));
                } else {
                    mNumberTextView.setText("");
                }
                int progress = mProgressBar.getProgress();
                int max = mProgressBar.getMax();
                if (mProgressPercentFormat != null) {
                    double percent = (double) progress / (double) max;
                    percent = (percent == 0) ? 0.01 : percent;
                    SpannableString tmp = new SpannableString(mProgressPercentFormat.format
                            (percent));
                    tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, tmp.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mPercentTextView.setText(tmp);
                } else {
                    mPercentTextView.setText("");
                }
            }
        };

        if (max > 0) {
            setMax(max);
        }
        if (progress > 0) {
            setProgress(progress);
        }
        if (title != null) {
            setTitle(title);
        }
        if (message != null) {
            setMessage(message);
        }
        if (totalSize > 0) {
            setTotalSize(totalSize);
        }
        if (currentSize > 0) {
            setCurrentSize(currentSize);
        }
        onProgressChanged();
    }


    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
        if (mViewUpdateHandler != null) {
            mViewUpdateHandler.removeCallbacksAndMessages(null);
        }
        dismiss();
    }

    public void setTitle(String title) {
        this.title = title;
        if (mTitleTextView != null)
            mTitleTextView.setText(title);
    }

    public void setMessage(String message) {
        this.message = message;
        if (mMsgTextView != null)
            mMsgTextView.setText(message);
    }

    public void setMax(int max) {
        this.max = max;
        if (mProgressBar != null) {
            mProgressBar.setMax(max);
            onProgressChanged();
        } else {
            this.max = max;
        }
    }

    public void setProgress(int progress) {
        if (mHasStarted) {
            this.progress = progress;
            if (mProgressBar != null)
                mProgressBar.setProgress(this.progress);
            onProgressChanged();
        } else {
            this.progress = progress;
        }
    }

    public void setCurrentSize(long currentSize) {
        if (mHasStarted) {
            this.currentSize = currentSize;
            onProgressChanged();
        } else {
            this.currentSize = currentSize;
        }
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public void setProgressNumberFormat(String progressNumberFormat) {
        mProgressNumberFormat = progressNumberFormat;
        onProgressChanged();
    }

    private void onProgressChanged() {
        if (mViewUpdateHandler != null && !mViewUpdateHandler.hasMessages(0)) {
            mViewUpdateHandler.sendEmptyMessage(0);
        }
    }
}
