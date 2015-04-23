package com.mobile.android.smartick.UI;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by sbarrio on 23/04/15.
 */
public class RegisterScrollView extends HorizontalScrollView {

        private RegisterScrollViewListener registerScrollViewListener = null;

        public static final  int SMOOTH_SCROLL_SPEED_FAST = 150;
        public static final int SMOOTH_SCROLL_SPEED_MID = 300;
        public static final int SMOOTH_SCROLL_SPEED_SLOW = 600;

        public RegisterScrollView(Context context) {
            super(context);
        }

        public RegisterScrollView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public RegisterScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void setRegisterScrollViewListener(RegisterScrollViewListener registerScrollViewListener) {
            this.registerScrollViewListener = registerScrollViewListener;
        }

        @Override
        protected void onScrollChanged(int x, int y, int oldx, int oldy) {
            super.onScrollChanged(x, y, oldx, oldy);
            if(registerScrollViewListener != null) {
                registerScrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            // Do not allow touch events.
            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            // Do not allow touch events.
            return false;
        }

        public void customSmoothScroll(int scrollTo,int duration){
            ObjectAnimator animator=ObjectAnimator.ofInt(this, "scrollX",scrollTo);
            animator.setDuration(duration);
            animator.start();
        }

    }

