package cn.com.pyc.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Applies a pressed state color filter or disabled state alpha for the button's
 * background drawable.
 * 
 * 自定义button，一张图片，实现按下颜色加深的selector效果。
 * 
 * @author shiki
 */
public class HighlightButton extends Button
{

    public HighlightButton(Context context)
    {
        super(context);
    }

    public HighlightButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public HighlightButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void setBackgroundDrawable(Drawable d)
    {
        // Replace the original background drawable (e.g. image) with a
        // LayerDrawable that
        // contains the original drawable.
        
        if(d == null)
        {
            super.setBackgroundDrawable(null);
        }
        else
        {
            HighlightDrawable layer = new HighlightDrawable(d);
            super.setBackgroundDrawable(layer);
        }
    }
}
