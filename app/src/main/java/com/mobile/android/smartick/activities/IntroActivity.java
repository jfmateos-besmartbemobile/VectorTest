package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.IntroScrollView;
import com.mobile.android.smartick.UI.IntroScrollViewListener;
import com.mobile.android.smartick.util.Constants;

import org.w3c.dom.Text;

import java.lang.reflect.Type;

/**
 * Created by sbarrio on 25/02/15.
 */
public class IntroActivity extends Activity implements IntroScrollViewListener {

    private IntroScrollView scrollView1 = null;

    //scrollView control
    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;
    private GestureDetector mGestureDetector;
    private int mActiveFeature = 0;
    private int pageWidth = 0;
    private int numPages = 6;


    //loaded flags
    private boolean page0Loaded = false;
    private boolean page1Loaded = false;
    private boolean page2Loaded = false;
    private boolean page3Loaded = false;
    private boolean page4Loaded = false;
    private boolean page5Loaded = false;

    //TextViews
    private TextView page1Title;
    private TextView page1SubTitle;
    private TextView page1SlideText;
    private TextView page2Title;
    private TextView page2SubTitle;
    private TextView page3Title;
    private TextView page3SubTitle;
    private TextView page4Title;
    private TextView page4SubTitle;
    private TextView page5Title;
    private TextView page5SubTitle;


//ImageViews
    //page 1
    private ImageView page1LogoSmartick;

    //page 5
    private ImageView page5bg;
    private View page5lower;
    private ImageView page5sky;

    //page 6
    private ImageView mountainBase;
    private View mountainTop;
    private Button startButton;
    private ImageView kidJumping;
    private TextView startTodayText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(info);

        //gets window width and adapts width of every page
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pageWidth = size.x;

        ViewGroup rootView = (ViewGroup) findViewById(R.id.intro_horizontalScroll_linear);
        adaptScrollViewPageWidth(rootView, pageWidth);

        scrollView1 = (IntroScrollView) findViewById(R.id.intro_horizontalScroll);
        scrollView1.setIntroScrollViewListener(this);
        setTouchListeners(scrollView1);


        linkIntroElements();

        //sets up animated elements
        mountainTop.setY(-mountainTop.getHeight());

        prepareTextFonts();
        loadImagesForPage(0);
        loadImagesForPage(1);
    }

    private void prepareTextFonts(){
        Typeface tfBoogaloo = Typeface.createFromAsset(getAssets(), "fonts/Boogaloo-Regular.otf");
        Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");

        page1Title = (TextView) findViewById(R.id.page1TitleText);
        page1Title.setTypeface(tfDidact);
        page1SubTitle = (TextView) findViewById(R.id.page1SubTitleText);
        page1SubTitle.setTypeface(tfDidact);
        page1SlideText = (TextView) findViewById(R.id.page1SlideText);
        page1SlideText.setTypeface(tfDidact);

        page2Title = (TextView) findViewById(R.id.page2TitleText);
        page2Title.setTypeface(tfDidact);
        page2SubTitle = (TextView) findViewById(R.id.page2SubTitleText);
        page2SubTitle.setTypeface(tfDidact);

        page3Title = (TextView) findViewById(R.id.page3TitleText);
        page3Title.setTypeface(tfDidact);
        page3SubTitle = (TextView) findViewById(R.id.page3SubTitleText);
        page3SubTitle.setTypeface(tfDidact);

        page4Title = (TextView) findViewById(R.id.page4TitleText);
        page4Title.setTypeface(tfDidact);
        page4SubTitle = (TextView) findViewById(R.id.page4SubTitleText);
        page4SubTitle.setTypeface(tfDidact);

        page5Title = (TextView) findViewById(R.id.page5TitleText);
        page5Title.setTypeface(tfDidact);
        page5SubTitle = (TextView) findViewById(R.id.page5SubTitleText);
        page5SubTitle.setTypeface(tfDidact);

        startTodayText = (TextView) findViewById(R.id.startTodayText);
        startTodayText.setTypeface(tfDidact);

        startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setTypeface(tfBoogaloo);
    }

    private void linkIntroElements(){
        //page 1
        page1LogoSmartick = (ImageView) findViewById(R.id.logoSmartick);

        //page 5
        page5bg = (ImageView) findViewById(R.id.page5_bg);
        page5lower = findViewById(R.id.page5lower);
        page5sky = (ImageView) findViewById(R.id.page5_sky);

        //page 6
        mountainBase = (ImageView) findViewById(R.id.mountain_base);
        mountainTop = (View) findViewById(R.id.mountain_top);
        kidJumping = (ImageView) findViewById(R.id.kid_jumping);
    }

    private void loadImagesForPage(int pageNum){
        Log.d(Constants.INTRO_LOG_TAG, "LOAD images for page " + pageNum);
        switch (pageNum){
            case 0:
                if (page0Loaded){
                    return;
                }
                page1LogoSmartick.setImageResource(R.drawable.logo);
                page1LogoSmartick.invalidate();
                page0Loaded = true;
                break;
            case 1:
                if (page1Loaded){
                    return;
                }
                page1Loaded = true;
                break;
            case 2:
                if (page2Loaded){
                    return;
                }
                page2Loaded = true;
                break;
            case 3:
                if (page3Loaded){
                    return;
                }
                page3Loaded = true;
                break;
            case 4:
                if (page4Loaded){
                    return;
                }
                page5bg.setImageResource(R.drawable.intro_page5_bg);
                page5bg.invalidate();
                page5sky.setImageResource(R.drawable.intro_page6_bg_sky);
                page5sky.invalidate();
                page5sky.setY(-mountainTop.getHeight());
                page4Loaded = true;
                break;
            case 5:
                if (page5Loaded){
                    return;
                }
                mountainBase.setImageResource(R.drawable.intro_page6_bg_mountain_base);
                mountainBase.invalidate();
                kidJumping.setImageResource(R.drawable.intro_page7_kid_jumping);
                kidJumping.invalidate();
                page5Loaded = true;
                break;
            default: break;
        }
    }

    private void freeImagesForPage(int pageNum){
        Log.d(Constants.INTRO_LOG_TAG, "free images for page " + pageNum);
        switch (pageNum){
            case 0:
                if (!page0Loaded){
                    return;
                }
                page1LogoSmartick.setImageDrawable(null);
                page0Loaded = false;
                break;
            case 1:
                if (!page1Loaded){
                    return;
                }

                page1Loaded = false;
                break;
            case 2:
                if (!page2Loaded){
                    return;
                }
                page2Loaded = false;
                break;
            case 3:
                if (!page3Loaded){
                    return;
                }
                page3Loaded = false;
                break;
            case 4:
                if (!page4Loaded){
                    return;
                }
                page5bg.setImageDrawable(null);
                page5sky.setImageDrawable(null);
                page4Loaded = false;
                break;
            case 5:
                if (!page5Loaded){
                    return;
                }
                mountainBase.setImageDrawable(null);
                kidJumping.setImageDrawable(null);
                page5Loaded = false;
                break;
            default: break;
        }
    }

    public void startButtonPressed(View view){
        for (int i=0;i<numPages;i++){
            freeImagesForPage(i);
        }
        finish();
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

//       Log.d(Constants.INTRO_LOG_TAG,"scroll changed: " + oldx +  " -> " + x);
        if(scrollView == scrollView1) {
            Log.d(Constants.INTRO_LOG_TAG,"currentPage: " + Math.round(x / pageWidth));
            int currentPage = Math.round(x / pageWidth);

            int x_aux = x - currentPage * pageWidth;
            float percent = (float)x_aux / (float)pageWidth;
            if (x_aux == pageWidth){
                percent = 1;
            }
            Log.d(Constants.INTRO_LOG_TAG, "page_width " + pageWidth + " x_aux " + x_aux + " percent " + percent + " mountainBaseY " + mountainBase.getY());


            if (x % pageWidth == 0){
                for (int i = numPages; i > -1;i--){
                    if (i != currentPage){
                        freeImagesForPage(i);
                    }
                }
                loadImagesForPage(currentPage -1);
                loadImagesForPage(currentPage);
                loadImagesForPage(currentPage +1);
            }


            if (currentPage < 4){
                page5sky.setY(-mountainTop.getHeight());
            }

            if (currentPage == 4){
                Log.d(Constants.INTRO_LOG_TAG, "moving");
                mountainTop.setY((mountainTop.getHeight() * percent) - mountainTop.getHeight());
                mountainBase.setY(mountainBase.getHeight() * percent);
                page5lower.setY(page5lower.getHeight() * percent);
                page5sky.setY((page5sky.getHeight() * percent) - page5sky.getHeight());
            }
        }
    }

    @Override
    public void onBackPressed() {
        for (int i=0;i<numPages;i++){
         freeImagesForPage(i);
        }
        finish();
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
                    scrollView.customSmoothScroll(scrollTo,IntroScrollView.SMOOTH_SCROLL_SPEED_MID);
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
                    scrollView1.customSmoothScroll(mActiveFeature*featureWidth,IntroScrollView.SMOOTH_SCROLL_SPEED_MID);
                    Log.d(Constants.INTRO_LOG_TAG,"going right!");
                    return true;
                }
                //left to right
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    int featureWidth = scrollView1.getMeasuredWidth();
                    mActiveFeature = (mActiveFeature > 0)? mActiveFeature - 1:0;
                    scrollView1.customSmoothScroll(mActiveFeature*featureWidth,IntroScrollView.SMOOTH_SCROLL_SPEED_MID);
                    Log.d(Constants.INTRO_LOG_TAG,"going left!");
                    return true;
                }
            } catch (Exception e) {
                Log.d(Constants.INTRO_LOG_TAG,"There was an error processing the Fling event:" + e.getMessage());
            }
            return false;
        }
    }

    private void goNextPage(){
        int featureWidth = scrollView1.getMeasuredWidth();
        mActiveFeature = (mActiveFeature < (numPages - 1))? mActiveFeature + 1:numPages -1;
        scrollView1.customSmoothScroll(mActiveFeature*featureWidth,IntroScrollView.SMOOTH_SCROLL_SPEED_MID);
        Log.d(Constants.INTRO_LOG_TAG,"going right!");
    }

    private void goBackPage(){
        int featureWidth = scrollView1.getMeasuredWidth();
        mActiveFeature = (mActiveFeature > 0)? mActiveFeature - 1:0;
        scrollView1.customSmoothScroll(mActiveFeature*featureWidth,IntroScrollView.SMOOTH_SCROLL_SPEED_MID);
        Log.d(Constants.INTRO_LOG_TAG,"going left!");
    }

    public void leftButtonPressed(View view){
        goBackPage();
    }

    public void rightButtonPressed(View view){
        goNextPage();
    }

}
