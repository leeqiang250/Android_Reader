package cn.com.pyc.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import cn.com.pyc.pbb.R;


public class SearchEditText extends EditText implements OnFocusChangeListener,
        OnKeyListener, TextWatcher {
    private static final String TAG = "SearchEditText";
    /**
     * 图标是否默认在左边
     */
    private boolean isIconLeft = false;
    /**
     * 是否点击软键盘搜索
     */
    private boolean pressSearch = false;
    /**
     * 软键盘搜索键监听
     */
    private OnSearchClickListener listener;

    private Drawable[] drawables; // 控件的图片资源
    private Drawable drawableLeft, drawableDel; // 搜索图标和删除按钮图标
    private int eventX, eventY; // 记录点击坐标
    private Rect rect; // 控件区域
    private Drawable drawableDelIcon;

    private TextView tvCancel;
    private Animation fadeInAnim;
    private Animation fadeOutAnim;

    private boolean showCancel = false; //默认是否显示’取消按钮‘

    public void setOnSearchClickListener(OnSearchClickListener listener) {
        this.listener = listener;
    }

    public void setCancelTextView(TextView tvCancel) {
        //this.tvCancel = tvCancel;
        //this.tvCancel.setVisibility(View.VISIBLE);
        setCancelTextView(tvCancel, false);
    }

    public void setCancelTextView(TextView tvCancel, boolean showCancel) {
        this.tvCancel = tvCancel;
        this.showCancel = showCancel;
        this.tvCancel.setVisibility(this.showCancel ? View.VISIBLE : View.GONE);
    }

    public interface OnSearchClickListener {
        void onSearchClick(View view);
    }

    public SearchEditText(Context context) {
        this(context, null);
        init();
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
        init();
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fadeInAnim = AnimationUtils.loadAnimation(getContext(),
                android.R.anim.fade_in);
        fadeOutAnim = AnimationUtils.loadAnimation(getContext(),
                android.R.anim.fade_out);

        drawableDelIcon = getResources().getDrawable(R.drawable.ic_clear_action);
        setGravity(Gravity.CENTER_VERTICAL);
        setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        setSingleLine(true);
        if (drawables == null) drawables = getCompoundDrawables();
        if (drawableLeft == null) drawableLeft = drawables[0];

        setOnFocusChangeListener(this);
        setOnKeyListener(this);
        addTextChangedListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isIconLeft) { // 如果是默认样式，直接绘制
            if (length() < 1) {
                drawableDel = null;
            }
            this.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null,
                    drawableDel, null);
        } else { // 如果不是默认样式，需要将图标绘制在中间
            float textWidth = getPaint().measureText(getHint().toString());
            int drawablePadding = getCompoundDrawablePadding();
            int drawableWidth = drawableLeft.getIntrinsicWidth();
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            canvas.translate(
                    (getWidth() - bodyWidth - getPaddingLeft() - getPaddingRight()) / 2,
                    0);
        }
        super.onDraw(canvas);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.i(TAG, "execute hasFocus: " + hasFocus);
        // 被点击时，恢复默认样式
        if (TextUtils.isEmpty(getText().toString())) // !pressSearch &&
        {
            isIconLeft = hasFocus;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.i(TAG, "execute onKeyCode: " + keyCode);
        pressSearch = (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH);
        if (pressSearch && listener != null) {
            /* 隐藏软键盘 */
            InputMethodManager imm = (InputMethodManager) v.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                // isIconLeft = false;
                postInvalidate();
            }
            listener.onSearchClick(v);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawableDel != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                boolean touchable = event.getX() > (getWidth()
                        - getPaddingRight() - drawableDel.getIntrinsicWidth())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {
                    this.setText("");
                }
            }
        }
//        // 清空edit内容
//        if (drawableDel != null && event.getAction() == MotionEvent.ACTION_UP) {
//            eventX = (int) event.getRawX();
//            eventY = (int) event.getRawY();
//            // Log.i(TAG, "eventX = " + eventX + "; eventY = " + eventY);
//            if (rect == null) rect = new Rect();
//            getGlobalVisibleRect(rect);
//            rect.left = rect.right - drawableDel.getIntrinsicWidth();
//            if (rect.contains(eventX, eventY)) {
//                setText("");
//            }
//        }
        // 删除按钮被按下时改变图标样式
        if (drawableDel != null && event.getAction() == MotionEvent.ACTION_DOWN) {
//            eventX = (int) event.getRawX();
//            eventY = (int) event.getRawY();
//            // Log.i(TAG, "eventX = " + eventX + "; eventY = " + eventY);
//            if (rect == null) rect = new Rect();
//            getGlobalVisibleRect(rect);
//            rect.left = rect.right - drawableDel.getIntrinsicWidth();
//            if (rect.contains(eventX, eventY)) drawableDel = drawableDelIcon;
        } else {
            drawableDel = drawableDelIcon;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void afterTextChanged(Editable arg0) {
        if (arg0.length() < 1) {
            drawableDel = null;

            if (!showCancel && tvCancel != null && tvCancel.getVisibility() == View.VISIBLE) {
                tvCancel.startAnimation(fadeOutAnim);
                tvCancel.setVisibility(View.GONE);
            }
        } else {
            drawableDel = drawableDelIcon;

            if (!showCancel && tvCancel != null && tvCancel.getVisibility() == View.GONE) {
                tvCancel.startAnimation(fadeInAnim);
                tvCancel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

}
