package cn.com.pyc.widget;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView {

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MarqueeTextView(Context context) {
		super(context);
		init();
	}

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setEllipsize(TruncateAt.MARQUEE);
		setSingleLine(true);
	}

	@Override
	public boolean isFocused() {
		return true;
	}

}
