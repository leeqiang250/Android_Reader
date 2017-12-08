package com.sz.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.sz.mobilesdk.util.SZLog;

/**
 * 自定义水印内容View
 * Created by hudq on 2017/1/11.
 */
public class WaterMaskView extends View {

    private static final String TAG = "WaterMaskView";
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String text;
    private float textWidth, textHeight; //文字宽高度
    private Rect rect = new Rect();
    private int screenWidth, screenHeight;
    private float maxX, maxY;
    private int centerX, centerY;
    private float textOffsetY;

    public WaterMaskView(Context context) {
        this(context, null, 0);
    }

    public WaterMaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterMaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextPaint.setColor(Color.argb(51, 188, 188, 188));
        mTextPaint.setStrokeWidth(0);
        mTextPaint.setTextSize(dp2px(context, 30f));
        mTextPaint.setTypeface(Typeface.DEFAULT); // 设置字体

        screenWidth = getScreenSize(context).x;
        screenHeight = getScreenSize(context).y;

        //textOffsetX = (int) (textWidth * Math.cos(45 * Math.PI / 180));//文字的水平高度
        //textOffsetY = (int) (textWidth * Math.sin(45 * Math.PI / 180));// 文字的垂直高度
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        SZLog.d(TAG, "centerX: " + centerX + ", centerY: " + centerY);
    }

    private static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private static Point getScreenSize(Context ctx) {
        WindowManager wm = (WindowManager) ctx.getApplicationContext().getSystemService(Context
                .WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        return outSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (textWidth == 0 || textHeight == 0)
            return;

        canvas.translate(centerX, centerY); //画布原点移动到画布中心坐标点
        canvas.clipRect(rect);

        //中间水印
        canvas.save();
        canvas.rotate(-45f); //逆时针旋转45度
        canvas.drawText(text, -textWidth * 0.5f, textHeight * 0.5f, mTextPaint);
        canvas.restore();

        //顶部水印
        canvas.save();
        canvas.translate(0, -(maxY / 3 - textOffsetY * 0.13f));//画布原点移动到顶部1/3处
        canvas.rotate(-45f);
        canvas.drawText(text, -textWidth * 0.5f, textHeight * 0.5f, mTextPaint);
        canvas.restore();

        //底部水印
        canvas.save();
        canvas.translate(0, maxY / 3 - textOffsetY * 0.13f); //画布原点移动到底部1/3处
        canvas.rotate(-45f);
        canvas.drawText(text, -textWidth * 0.5f, textHeight * 0.5f, mTextPaint);
        canvas.restore();
    }

    /**
     * 设置最大区域X,Y大小
     *
     * @param pageSizeX 页宽
     * @param pageSizeY 页高
     */
    public void setMaxSize(float pageSizeX, float pageSizeY) {
        SZLog.d(TAG, "page(x,y): " + pageSizeX + ", " + pageSizeY);
        float mSourceScale = Math.min(screenWidth / pageSizeX, screenHeight / pageSizeY);
        SZLog.d(TAG, "mSourceScale = " + mSourceScale);
        maxX = pageSizeX * mSourceScale - dp2px(getContext(), 10f);
        maxY = pageSizeY * mSourceScale - dp2px(getContext(), 10f);
        SZLog.d(TAG, "max(x,y): " + maxX + ", " + maxY);
        rect.set((int) (-maxX * 0.5f), (int) (-maxY * 0.5f),
                (int) (maxX * 0.5f), (int) (maxY * 0.5f));
    }

    /**
     * 设置显示水印内容
     *
     * @param text 水印文本
     */
    public void setContentText(String text) {
        if (text == null)
            throw new IllegalArgumentException("args not allow null.");

        this.text = text;
        //textWidth = mTextPaint.measureText(this.text); // 粗略测量文字宽度
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), bounds); //获取文字所占矩形区域
        textWidth = bounds.width();
        textHeight = bounds.height();
        textOffsetY = (float) (textWidth * Math.sin(45 * Math.PI / 180));// 文字的垂直高度
        SZLog.d(TAG, "text(w,h) = " + textWidth + ", " + textHeight);
        invalidate();
    }
}
