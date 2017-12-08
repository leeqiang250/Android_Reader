package cn.com.pyc.utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tencent.mm.opensdk.utils.Log;

import cn.com.pyc.pbb.R;
import cn.com.pyc.user.key.LoginFragment;

/**
 * Created by songyumei on 2017/11/16.
 */
public class LayOutUtil {

    private final int screenHeight;
    private final int screenWidth;
    private final int keyHeight;
    private final Context context;
    private final LinearLayout rootLl;
    private  LinearLayout ll_pbb;
    private  int imageViewHeight = 70;

    public LayOutUtil(Context context, LinearLayout ll_pbb, LinearLayout imageView) {
        this.ll_pbb = ll_pbb;
        this.context = context;
        this.rootLl = imageView;
        //获取屏幕高度
        screenHeight = ((Activity)context).getWindowManager().getDefaultDisplay().getHeight();
        screenWidth = ((Activity)context).getWindowManager().getDefaultDisplay().getWidth();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight /3;

        /*int bottom = imageView.getBottom();
        int top = imageView.getTop();
        imageViewHeight = ( bottom - top ) / 2;*/
    }


    public void playUpAnimator(final boolean isUp) {
        int translationValue = screenWidth/5;
        float startTranValue = isUp ? 0F : -translationValue;
        float endTranValue = isUp ? -translationValue : 0F;

        float startScalValue = isUp ? 1F : 0.5F;
        float endScalValue = isUp ? 0.5F : 1F;
        ObjectAnimator animator = ObjectAnimator.ofFloat(ll_pbb, "translationX", startTranValue, endTranValue);


        float resultStartT = isUp ? 0f : -imageViewHeight;
        float resultEndT = isUp ? -imageViewHeight : 0f;

        float resultStartTL = isUp ? 0f : -2*imageViewHeight;
        float resultEndTL = isUp ? -2*imageViewHeight : 0f;
        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(animator).with(ObjectAnimator.ofFloat(ll_pbb, "scaleX", startScalValue, endScalValue));
        animatorSet.play(animator).with(ObjectAnimator.ofFloat(ll_pbb, "scaleY", startScalValue, endScalValue));
        animatorSet.play(animator).with(ObjectAnimator.ofFloat(ll_pbb, "translationY", resultStartT, resultEndT));
        animatorSet.play(animator).with(ObjectAnimator.ofFloat(rootLl, "translationY", resultStartTL, resultEndTL));
        animatorSet.setDuration(200);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                LoginFragment.tv_login.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                LoginFragment.tv_login.setVisibility(isUp ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animatorSet.start();
    }
}
