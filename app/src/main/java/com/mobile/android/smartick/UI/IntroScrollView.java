package com.mobile.android.smartick.UI;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by sbarrio on 12/03/15.
 */
public class IntroScrollView extends HorizontalScrollView {

    public static final int SMOOTH_SCROLL_SPEED_FAST = 150;
    public static final int SMOOTH_SCROLL_SPEED_MID = 300;
    public static final int SMOOTH_SCROLL_SPEED_SLOW = 600;

    private IntroScrollViewListener introScrollViewListener = null;

    public IntroScrollView(Context context) {
        super(context);
    }

    public IntroScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public IntroScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setIntroScrollViewListener(IntroScrollViewListener introScrollViewListener) {
        this.introScrollViewListener = introScrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(introScrollViewListener != null) {
            introScrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public void customSmoothScroll(int scrollTo,int duration){
        ObjectAnimator animator=ObjectAnimator.ofInt(this, "scrollX",scrollTo);
        animator.setDuration(duration);
        animator.start();
    }

}
