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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.IntroScrollView;
import com.mobile.android.smartick.UI.IntroScrollViewListener;
import com.mobile.android.smartick.UI.RegisterScrollView;
import com.mobile.android.smartick.UI.RegisterScrollViewListener;

import org.w3c.dom.Text;

public class RegistroActivity extends Activity implements RegisterScrollViewListener {

    private RegisterScrollView scrollView1 = null;

    //scroll view control
    private int pageWidth = 0;
    private int numPages = 8;  //5 alumno 4 tutor
    private int currentPage = 0;

    //register view elements
    private TextView titleStudentCred;
    private ImageView studentUsernameIcon;
    private ImageView studentPasswordIcon;
    private EditText studentAliasEditText;
    private EditText studentPasswordEditText;
    private TextView titleStudentInfo;
    private EditText studentNameEditText;
    private EditText studentLastNameEditText;
    private TextView titleStudentSex;
    private ImageView iconBoy;
    private ImageView iconGirl;
    private TextView titleStudentAge;
    private DatePicker studentAgeDatePicker;
    private TextView titleStudentConfirm;
    private ImageView confirmSexGirl;
    private ImageView confirmSexBoy;
    private TextView confirmStudentUsernameTextView;
    private TextView confirmStudentNameTextView;
    private TextView confirmStudentBirthDate;
    private TextView titleTutorCred;
    private ImageView tutorMailIcon;
    private ImageView tutorPasswordIcon;
    private EditText tutorMailEditText;
    private EditText tutorPasswordEditText;
    private TextView tutorInfotitle;
    private ImageView tutorInfoIcon;
    private EditText tutorNameEditText;
    private EditText tutorLastNameEditText;
    private EditText tutorPhoneEditText;
    private TextView tutorConfirmTitle;
    private ImageView confirmTutorIcon;
    private TextView confirmTutorMail;
    private TextView confirmTutorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //sets up view elements
        setUpRegisterViewElements();

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

    public void setUpRegisterViewElements(){
        titleStudentCred = (TextView) findViewById(R.id.student_credentials_title);
        studentUsernameIcon = (ImageView) findViewById(R.id.student_username_icon);
        studentPasswordIcon = (ImageView) findViewById(R.id.student_password_icon);
        studentAliasEditText = (EditText) findViewById(R.id.student_alias_edittext);
        studentPasswordEditText = (EditText) findViewById(R.id.student_password_edittext);
        titleStudentInfo = (TextView) findViewById(R.id.student_info_title);
        studentNameEditText = (EditText) findViewById(R.id.student_name_edittext);
        studentLastNameEditText = (EditText) findViewById(R.id.student_lastname_edittext);
        titleStudentSex = (TextView) findViewById(R.id.student_sex_title);
        iconBoy = (ImageView) findViewById(R.id.icon_boy);
        iconGirl = (ImageView) findViewById(R.id.icon_girl);
        titleStudentAge = (TextView) findViewById(R.id.student_age_title);
        studentAgeDatePicker = (DatePicker) findViewById(R.id.student_age_datepicker);
        titleStudentConfirm = (TextView) findViewById(R.id.student_confirm_title);
        confirmSexGirl = (ImageView) findViewById(R.id.confirm_sex_girl);
        confirmSexBoy = (ImageView) findViewById(R.id.confirm_sex_boy);
        confirmStudentUsernameTextView = (TextView) findViewById(R.id.confirm_student_username);
        confirmStudentNameTextView = (TextView) findViewById(R.id.confirm_student_name);
        confirmStudentBirthDate = (TextView) findViewById(R.id.confirm_student_birthdate);
        titleTutorCred = (TextView) findViewById(R.id.tutor_credentials_title);
        tutorMailIcon = (ImageView) findViewById(R.id.tutor_mail_icon);
        tutorPasswordIcon = (ImageView) findViewById(R.id.tutor_password_icon);
        tutorMailEditText = (EditText) findViewById(R.id.tutor_mail_edittext);
        tutorPasswordEditText = (EditText) findViewById(R.id.tutor_password_edittext);
        tutorInfotitle = (TextView) findViewById(R.id.tutor_info_title);
        tutorInfoIcon = (ImageView) findViewById(R.id.tutor_info_icon);
        tutorNameEditText = (EditText) findViewById(R.id.tutor_name_edittext);
        tutorLastNameEditText = (EditText) findViewById(R.id.tutor_lastname_edittext);
        tutorPhoneEditText = (EditText) findViewById(R.id.tutor_phone_edittext);
        tutorConfirmTitle = (TextView) findViewById(R.id.tutor_confirm_title);
        confirmTutorIcon = (ImageView) findViewById(R.id.confirm_tutor_icon);
        confirmTutorMail = (TextView) findViewById(R.id.confirm_tutor_mail);
        confirmTutorName = (TextView) findViewById(R.id.confirm_tutor_name);
    }

    public void setUpTextFontForView(String fontPath){
        Typeface dGothic = Typeface.createFromAsset(getAssets(), fontPath);
        titleStudentCred.setTypeface(dGothic);
        studentAliasEditText.setTypeface(dGothic);
        studentPasswordEditText.setTypeface(dGothic);
        titleStudentInfo.setTypeface(dGothic);
        studentNameEditText.setTypeface(dGothic);
        studentLastNameEditText.setTypeface(dGothic);
        titleStudentSex.setTypeface(dGothic);
        titleStudentAge.setTypeface(dGothic);
        titleStudentConfirm.setTypeface(dGothic);
        confirmStudentUsernameTextView.setTypeface(dGothic);
        confirmStudentNameTextView.setTypeface(dGothic);
        confirmStudentBirthDate.setTypeface(dGothic);
        titleTutorCred.setTypeface(dGothic);
        tutorInfotitle.setTypeface(dGothic);
        tutorConfirmTitle.setTypeface(dGothic);
        confirmTutorMail.setTypeface(dGothic);
        confirmTutorName.setTypeface(dGothic);
    }

    //scrollView listener methods
    public void onScrollChanged(RegisterScrollView scrollView, int x, int y, int oldx, int oldy) {

    }
}
