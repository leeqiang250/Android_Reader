package com.aspsine.swipetoloadlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sz.mobilesdk.util.SZLog;
import com.sz.view.widget.AVLoadingIndicatorView;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.utils.ViewUtil;

public class ClassicLoadMoreFooterView extends FrameLayout implements
        SwipeLoadMoreTrigger, SwipeTrigger {

    private TextView tvLoadMore;
    private AVLoadingIndicatorView progressBar;

    private int mFooterHeight;
    private String pullLoadText;
    private String releaseLoadText;
    private String loadingText;

    public ClassicLoadMoreFooterView(Context context) {
        this(context, null, 0);
    }

    public ClassicLoadMoreFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicLoadMoreFooterView(Context context, AttributeSet attrs,
                                     int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFooterHeight = getResources().getDimensionPixelOffset(
                R.dimen.swip_footer_height);
        SZLog.v("mFooterHeigh", "" + mFooterHeight);
        pullLoadText = getContext().getString(
                R.string.xlistview_footer_hint_normal);
        releaseLoadText = getContext().getString(
                R.string.xlistview_footer_hint_ready);
        loadingText = getContext().getString(
                R.string.xlistview_footer_hint_loadmore);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tvLoadMore = (TextView) findViewById(R.id.classic_footer_hint_textview);
        progressBar = (AVLoadingIndicatorView) findViewById(R.id.classic_footer_progressbar);
    }

    @Override
    public void onLoadMore() {
        tvLoadMore.setText(loadingText);
        ViewUtil.showWidget(progressBar);
    }

    @Override
    public void onPrepare() {
        tvLoadMore.setText(pullLoadText);
    }

    @Override
    public void onMove(int y, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            ViewUtil.hideWidget(progressBar);
            if (Math.abs(y) >= mFooterHeight) {
                tvLoadMore.setText(releaseLoadText);
            } else {
                tvLoadMore.setText(pullLoadText);
            }
        }
    }

    @Override
    public void onRelease() {
    }

    @Override
    public void onComplete() {
        ViewUtil.hideWidget(progressBar);
    }

    @Override
    public void onReset() {
        tvLoadMore.setText(pullLoadText);
        ViewUtil.hideWidget(progressBar);
    }
}
