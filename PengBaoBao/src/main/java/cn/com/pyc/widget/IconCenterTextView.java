package cn.com.pyc.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

//中间显示图标的textview
public class IconCenterTextView extends TextView {

    private Drawable[] drawables; // 控件的图片资源
    private Drawable drawableLeft;

    public IconCenterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IconCenterTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        setSingleLine(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawables == null) drawables = getCompoundDrawables();
        if (drawableLeft == null) drawableLeft = drawables[0];
        float textWidth = getPaint().measureText(getText().toString());
        int drawablePadding = getCompoundDrawablePadding();
        int drawableWidth = drawableLeft.getIntrinsicWidth();
        float bodyWidth = textWidth + drawableWidth + drawablePadding;
        canvas.translate(
                (getWidth() - bodyWidth - getPaddingLeft() - getPaddingRight()) / 2,
                0);

        super.onDraw(canvas);
    }

}
