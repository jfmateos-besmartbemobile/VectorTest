package com.mobile.android.smartick.activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.IntroScrollView;
import com.mobile.android.smartick.UI.IntroScrollViewListener;
import com.mobile.android.smartick.UI.RegisterScrollView;
import com.mobile.android.smartick.UI.RegisterScrollViewListener;

public class RegistroActivity extends Activity implements RegisterScrollViewListener {

    private RegisterScrollView scrollView1 = null;

    //scroll view control
    private int pageWidth = 0;
    private int numPages = 9;  //5 alumno 4 alumno
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //Changes font of text
        setUpTextFontForView("fonts/DidactGothic.ttf");

        //gets window width and adapts width of every page
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pageWidth = size.x;

        ViewGroup rootView = (ViewGroup) findViewById(R.id.register_horizontalScroll_linear);
        adaptScrollViewPageWidth(rootView,pageWidth);

        scrollView1 = (RegisterScrollView) findViewById(R.id.register_horizontalScroll);
        scrollView1.setRegisterScrollViewListener(this);
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

    public void goNext(View view) {
        currentPage++;
        if (currentPage > numPages){
            currentPage = numPages;
        }
        int scrollTo = currentPage * pageWidth;
        scrollView1.customSmoothScroll(scrollTo,RegisterScrollView.SMOOTH_SCROLL_SPEED_MID);
    }

    public void goBack(View view) {
        currentPage--;
        if (currentPage < 0){
            currentPage = 0;
        }

        int scrollTo = currentPage * pageWidth;
        scrollView1.customSmoothScroll(scrollTo,RegisterScrollView.SMOOTH_SCROLL_SPEED_MID);
    }

    public void goHome(View view) {
        finish();
    }

    public void setUpTextFontForView(String fontPath){
        Typeface dGothic = Typeface.createFromAsset(getAssets(), fontPath);
        TextView titleStudentCred = (TextView) findViewById(R.id.student_credentials_title);
        titleStudentCred.setTypeface(dGothic);
    }

    //scrollView listener methods
    public void onScrollChanged(RegisterScrollView scrollView, int x, int y, int oldx, int oldy) {

    }
}
