package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.IntroScrollView;
import com.mobile.android.smartick.UI.ScrollViewListener;
import com.mobile.android.smartick.util.Constants;

/**
 * Created by sbarrio on 25/02/15.
 */
public class IntroActivity extends Activity implements ScrollViewListener{

    private IntroScrollView scrollView1 = null;

    //scrollView control
    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;
    private GestureDetector mGestureDetector;
    private int mActiveFeature = 0;
    private int pageWidth = 0;
    private int numPages = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);

        //gets window width and adapts width of every page
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pageWidth = size.x;

        ViewGroup rootView = (ViewGroup) findViewById(R.id.horizontalScroll_linear);
        adaptScrollViewPageWidth(rootView,pageWidth);

        scrollView1 = (IntroScrollView) findViewById(R.id.horizontalScroll);
        scrollView1.setScrollViewListener(this);
        setTouchListeners(scrollView1);
    }

    private void adaptScrollViewPageWidth(ViewGroup parent, int width){

        int count = parent.getChildCount();
        for(int i = 0; i < parent.getChildCount(); i++)
        {
            View child = parent.getChildAt(i);
            LinearLayout.LayoutParams paramsLinear = (LinearLayout.LayoutParams)child.getLayoutParams();

            paramsLinear.width= width;
            child.setLayoutParams(paramsLinear);
        }
    }

    //scrollView listener methods
    public void onScrollChanged(IntroScrollView scrollView, int x, int y, int oldx, int oldy) {

/*        Log.d(Constants.INTRO_LOG_TAG,"scroll changed: " + oldx +  " -> " + x);
        if(scrollView == scrollView1) {
            scrollView1.scrollTo(x, y);
        }*/
    }

    public void setTouchListeners(final IntroScrollView scrollView){

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //If the user swipes
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    int scrollX = v.getScrollX();
                    int featureWidth = v.getMeasuredWidth();
                    mActiveFeature = ((scrollX + (featureWidth / 2)) / featureWidth);
                    int scrollTo = mActiveFeature * featureWidth;
                    scrollView.smoothScrollTo(scrollTo,0);
                    return true;
                } else {
                    return false;
                }
            }
        });
        mGestureDetector = new GestureDetector(new MyGestureDetector());
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                //right to left
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    int featureWidth = scrollView1.getMeasuredWidth();
                    mActiveFeature = (mActiveFeature < (numPages - 1))? mActiveFeature + 1:numPages -1;
                    scrollView1.smoothScrollTo(mActiveFeature*featureWidth, 0);
                    Log.d(Constants.INTRO_LOG_TAG,"going right!");
                    return true;
                }
                //left to right
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    int featureWidth = scrollView1.getMeasuredWidth();
                    mActiveFeature = (mActiveFeature > 0)? mActiveFeature - 1:0;
                    scrollView1.smoothScrollTo(mActiveFeature*featureWidth, 0);
                    Log.d(Constants.INTRO_LOG_TAG,"going left!");
                    return true;
                }
            } catch (Exception e) {
                Log.d(Constants.INTRO_LOG_TAG,"There was an error processing the Fling event:" + e.getMessage());
            }
            return false;
        }
    }

}
