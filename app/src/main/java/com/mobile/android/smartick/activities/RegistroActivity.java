package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.RegisterScrollView;
import com.mobile.android.smartick.UI.RegisterScrollViewListener;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.network.LoginStatusResponse;
import com.mobile.android.smartick.network.NetworkStatus;
import com.mobile.android.smartick.network.RegisterAlumnoResponse;
import com.mobile.android.smartick.network.RegisterTutorResponse;
import com.mobile.android.smartick.network.SmartickAPI;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.pojos.UserType;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.util.LocaleHelper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroActivity extends Activity implements RegisterScrollViewListener {

  private RegisterScrollView scrollView1 = null;

  private final String MALE = "MASCULINO";
  private final String FEMALE = "FEMENINO";

  //scroll view control
  private int pageWidth = 0;
  private int pageHeight = 0;
  private int numPages = 8;  //5 alumno 4 tutor
  private int currentPage = 0;

  //username existenece check
  private boolean studentUsernameValid = false;
  private boolean tutorMailValid = false;

  //validation
  private boolean validationStudentPending = false;
  private boolean validationTutorPending = false;

  //register data
  private String studentUsername;
  private String studentPassword;
  private String studentName;
  private int studentBirthDay;
  private int studentBirthMonth;
  private int studentBirthYear;
  private String studentSex = "FEMALE";
  private boolean studentCanRead = false;

  private String tutorMail;
  private String tutorPassword;
  private String tutorName;
  private String tutorLastName;
  private String tutorPhone;

  //register view elements
  private TextView titleStudentCred;
  private ImageView studentUsernameIcon;
  private ImageView studentPasswordIcon;
  private EditText studentAliasEditText;
  private EditText studentPasswordEditText;
//  private TextView titleStudentInfo;
  private EditText studentNameEditText;
//  private EditText studentLastNameEditText;
  private TextView studentCanReadLabelText;
  private Switch studentCanReadSwitch;
//  private TextView titleStudentSex;
  private TextView textBoy, textGirl;
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
  private TextView confirmStudentCanRead;
  private TextView studentCanReadText;
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
  private TextView confirmTutorPhone;

  //validation
  private TextWatcher aliasTextWatcher;
  private TextWatcher mailTextWatcher;

  //sysinfo
  private SystemInfo sysInfo;

  //local User DB
  private UsersDBHandler localDB = new UsersDBHandler(this);

  //Loading dialog
  private SweetAlertDialog pDialog;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Set up selected language
    LocaleHelper.onCreate(this);

    setContentView(R.layout.activity_registro);

    // Inicializamos systemInfo
    sysInfo = new SystemInfo(this.getApplicationContext());

    //sets up view elements
    setUpRegisterViewElements();

    //Changes font of text
    setUpTextFontForView("fonts/DidactGothic.ttf");

    //sets up canRead switch
    studentCanReadSwitch.setChecked(studentCanRead);
    studentCanReadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        studentCanReadSwitch.setChecked(isChecked);
        studentCanRead = isChecked;
        if (isChecked) {
          studentCanReadText.setText(getString(R.string.Yes));
          confirmStudentCanRead.setText(getString(R.string.CanRead) + ": " + getString(R.string.Yes));
        } else {
          studentCanReadText.setText(getString(R.string.No));
          confirmStudentCanRead.setText(getString(R.string.CanRead) + ": " + getString(R.string.No));
        }

      }
    });
    confirmStudentCanRead.setText(getString(R.string.CanRead) + ": " + getString(R.string.No));

    //sets uo datePicker initial and maximum dates
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);
    int day = cal.get(Calendar.DAY_OF_MONTH);
    studentAgeDatePicker.updateDate(year - 5, month, day);
    studentAgeDatePicker.setMaxDate(new Date().getTime());
    studentAgeDatePicker.getCalendarView().setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
      @Override
      public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        int curenntYear = Calendar.getInstance().get(Calendar.YEAR);
        if (curenntYear - year >= Constants.CAN_READ_MIN_AGE) {
          studentCanReadSwitch.setChecked(true);
          studentCanRead = true;
          confirmStudentCanRead.setText(getString(R.string.CanRead) + ": " + getString(R.string.Yes));
        } else {
          studentCanReadSwitch.setChecked(false);
          studentCanRead = false;
          confirmStudentCanRead.setText(getString(R.string.CanRead) + ": " + getString(R.string.No));
        }
      }
    });

    //sets up text watchers
    setUpTextWatchers();

    //gets window width and adapts width of every page
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    pageWidth = size.x;
    pageHeight = size.y;

    ViewGroup rootView = (ViewGroup) findViewById(R.id.register_horizontalScroll_linear);
    adaptScrollViewPageWidth(rootView, pageWidth);

    scrollView1 = (RegisterScrollView) findViewById(R.id.register_horizontalScroll);
    scrollView1.setRegisterScrollViewListener(this);

    //initial focus on student alias

    studentAliasEditText.requestFocus();

//        studentAliasEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus){
//                    scrollView1.setY(scrollView1.getY() - pageHeight / 4);
//                }else{
//                    resetScrollViewPositiuon();
//                }
//            }
//        });

    //DEBUG
//        setUpDebugData();
  }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)  {
//        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
//                && keyCode == KeyEvent.KEYCODE_BACK
//                && event.getRepeatCount() == 0) {
//            Log.d("CDA", "onKeyDown Called");
//            onBackPressed();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    private void resetScrollViewPositiuon(){
//        clearEditTextFocus();
//        hideSoftKeyboard();
//        scrollView1.setY(0);
//    }

  //Soft keyboard
//    private void hideSoftKeyboard() {
//        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(studentAliasEditText.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(studentPasswordEditText.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(studentNameEditText.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(studentLastNameEditText.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(tutorMailEditText.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(tutorPasswordEditText.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(tutorNameEditText.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(tutorLastNameEditText.getWindowToken(), 0);
//        imm.hideSoftInputFromWindow(tutorPhoneEditText.getWindowToken(), 0);
//    }
//
//    public void onBackPressed() {
//        resetScrollViewPositiuon();
//    }


  public void goBack(View view) {
    clearEditTextFocus();
    scrollToPreviousPage();
  }

  public void goHome(View view) {
    finish();
  }

  public void goToLogin() {
    startActivity(new Intent(this, LoginActivity.class));
    finish();
  }

  public void goNext(View view) {

    //disables any edit text that is hodling the keyboard focus
    clearEditTextFocus();

    boolean passwordStudentValid = isValidPassword(studentPasswordEditText.getText().toString());
    boolean passwordTutorValid = isValidPassword(tutorPasswordEditText.getText().toString());

    //depending on which page we are on we validate its content and decide wether or not we allow the user to keep going
    switch (currentPage) {
      case 0:
        //user and password ok
        if (studentUsernameValid && passwordStudentValid) {
          updateStudentConfirmData();
          scrollToNextPage();
          studentNameEditText.requestFocus();
          break;
        }

        //user needs validation but password is ok
        if (!studentUsernameValid && passwordStudentValid) {
          validateStudentUsername(studentAliasEditText.getText().toString().trim());
          break;
        }

        //default
        showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getText(R.string.Fill_in_the_fields_to_contiune).toString(), null, null, null, null);
        break;
      case 1:
        if (isValidName(studentNameEditText.getText().toString())) {
          updateStudentConfirmData();
          scrollToNextPage();
        } else {
          showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getText(R.string.Fill_in_the_fields_to_contiune).toString(), null, null, null, null);
        }
        break;
      case 2:
        updateStudentConfirmData();
        scrollToNextPage();
        break;
      case 3:
        updateStudentConfirmData();
        scrollToNextPage();
        break;
      case 4:
        scrollToNextPage();
        tutorMailEditText.requestFocus();
        break;
      case 5:
        //user and password ok
        if (tutorMailValid && passwordTutorValid) {
          updateTutorConfirmData();
          scrollToNextPage();
          tutorNameEditText.requestFocus();
          break;
        }

        //user needs validation but password is ok
        if (!tutorMailValid && passwordTutorValid) {
          validateTutorMail(tutorMailEditText.getText().toString().trim());
          break;
        }

        //default
        showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getText(R.string.Fill_in_the_fields_to_contiune).toString(), null, null, null, null);
        break;


      case 6:
        if (isValidName(tutorNameEditText.getText().toString()) && isValidName(tutorLastNameEditText.getText().toString())) {
          updateTutorConfirmData();
          scrollToNextPage();
          tutorPhoneEditText.requestFocus();
        } else {
          showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getText(R.string.Fill_in_the_fields_to_contiune).toString(), null, null, null, null);
        }
        break;
      case 7:
        if (isValidPhone(tutorPhoneEditText.getText().toString())) {
          updateTutorConfirmData();
          scrollToNextPage();
        } else {
          showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getText(R.string.Invalid_phone_number).toString(), null, null, null, null);
        }
        break;
      default:
        break;
    }
  }

  public void clearEditTextFocus() {
    studentAliasEditText.clearFocus();
    studentPasswordEditText.clearFocus();
    studentNameEditText.clearFocus();
//    studentLastNameEditText.clearFocus();
    tutorMailEditText.clearFocus();
    tutorPasswordEditText.clearFocus();
    tutorPhoneEditText.clearFocus();
  }

  public void setUpDebugData() {
    studentAliasEditText.setText("testAndroid2");
    studentPasswordEditText.setText("Sm1rt3cK");
    studentNameEditText.setText("testAndroid1");
//    studentLastNameEditText.setText("testAndroid1");
    studentSex = MALE;
    tutorMailEditText.setText("test@Android4.es");
    tutorPasswordEditText.setText("Sm1rt3cK");
    tutorNameEditText.setText("testAndroid1");
    tutorLastNameEditText.setText("testAndroid1");
    tutorPhoneEditText.setText("123456789");
  }

  public void selectSex(View view) {

    //sets new background and selection
    if (view.getId() == R.id.icon_girl) {
      studentSex = FEMALE;
      view.setBackground(getResources().getDrawable(R.drawable.sex_selector_background_selected));
      View v = (View) findViewById(R.id.icon_boy);
      v.setBackground(getResources().getDrawable(R.drawable.sex_selector_background));
    }
    if (view.getId() == R.id.icon_boy) {
      studentSex = MALE;
      view.setBackground(getResources().getDrawable(R.drawable.sex_selector_background_selected));
      View v = (View) findViewById(R.id.icon_girl);
      v.setBackground(getResources().getDrawable(R.drawable.sex_selector_background));
    }

    view.refreshDrawableState();
    goNext(null);
  }

  public void finishRegister(View view) {
    Log.d(Constants.REGISTER_LOG_TAG, "Finished -> Register tutor");
    registerTutor();
  }

  public void scrollToNextPage() {
    currentPage++;
    if (currentPage > numPages) {
      currentPage = numPages;
    }
    int scrollTo = currentPage * pageWidth;
    scrollView1.customSmoothScroll(scrollTo, RegisterScrollView.SMOOTH_SCROLL_SPEED_MID);
  }

  public void scrollToPreviousPage() {
    currentPage--;
    if (currentPage < 0) {
      currentPage = 0;
    }

    int scrollTo = currentPage * pageWidth;
    scrollView1.customSmoothScroll(scrollTo, RegisterScrollView.SMOOTH_SCROLL_SPEED_MID);
  }

  public void updateStudentConfirmData() {
    studentUsername = studentAliasEditText.getText().toString().trim();
    studentPassword = studentPasswordEditText.getText().toString();
    studentName = studentNameEditText.getText().toString();
//    studentLastName = studentLastNameEditText.getText().toString();
    studentCanRead = studentCanReadSwitch.isChecked();
    studentBirthDay = studentAgeDatePicker.getDayOfMonth();
    studentBirthMonth = studentAgeDatePicker.getMonth() + 1;
    studentBirthYear = studentAgeDatePicker.getYear();

    //Confirm tab update
    if (studentSex.equals(MALE)) {
      confirmSexGirl.setVisibility(View.GONE);
      confirmSexBoy.setVisibility(View.VISIBLE);
    } else {
      confirmSexGirl.setVisibility(View.VISIBLE);
      confirmSexBoy.setVisibility(View.GONE);
    }

    confirmStudentNameTextView.setText(studentName );
    confirmStudentBirthDate.setText(studentBirthDay + "/" + studentBirthMonth + "/" + studentBirthYear);
    confirmStudentUsernameTextView.setText(studentUsername);

  }

  public void updateTutorConfirmData() {
    tutorMail = tutorMailEditText.getText().toString().trim();
    tutorPassword = tutorPasswordEditText.getText().toString();
    tutorName = tutorNameEditText.getText().toString();
    tutorLastName = tutorLastNameEditText.getText().toString();
    tutorPhone = tutorPhoneEditText.getText().toString();
    confirmTutorMail.setText(tutorMail);
    confirmTutorName.setText(tutorName + " " + tutorLastName);
    confirmTutorPhone.setText(tutorPhone);
  }

  //init
  private void adaptScrollViewPageWidth(ViewGroup parent, int width) {

    int count = parent.getChildCount();
    for (int i = 0; i < parent.getChildCount(); i++) {
      View child = parent.getChildAt(i);
      LinearLayout.LayoutParams paramsLinear = (LinearLayout.LayoutParams) child.getLayoutParams();

      paramsLinear.width = width;
      child.setLayoutParams(paramsLinear);
    }
  }

  public void setUpRegisterViewElements() {
    titleStudentCred = (TextView) findViewById(R.id.student_credentials_title);
    studentUsernameIcon = (ImageView) findViewById(R.id.student_username_icon);
    studentPasswordIcon = (ImageView) findViewById(R.id.student_password_icon);
    studentAliasEditText = (EditText) findViewById(R.id.student_alias_edittext);
    studentPasswordEditText = (EditText) findViewById(R.id.student_password_edittext);
//    titleStudentInfo = (TextView) findViewById(R.id.student_info_title);
    studentNameEditText = (EditText) findViewById(R.id.student_name_edittext);
//    studentLastNameEditText = (EditText) findViewById(R.id.student_lastname_edittext);
    studentCanReadLabelText = (TextView) findViewById(R.id.student_canread_label);
    studentCanReadSwitch = (Switch) findViewById(R.id.student_canread_switch);
//    titleStudentSex = (TextView) findViewById(R.id.student_sex_title);
    textBoy = (TextView) findViewById(R.id.text_boy);
    textGirl = (TextView) findViewById(R.id.text_girl);
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
    confirmStudentCanRead = (TextView) findViewById(R.id.confirm_student_can_read);
    titleTutorCred = (TextView) findViewById(R.id.tutor_credentials_title);
    tutorMailIcon = (ImageView) findViewById(R.id.tutor_mail_icon);
    studentCanReadText = (TextView) findViewById(R.id.student_canread_text);
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
    confirmTutorPhone = (TextView) findViewById(R.id.confirm_tutor_phone);
  }

  public void setUpTextFontForView(String fontPath) {
    Typeface dGothic = Typeface.createFromAsset(getAssets(), fontPath);
    titleStudentCred.setTypeface(dGothic);
    studentAliasEditText.setTypeface(dGothic);
    studentPasswordEditText.setTypeface(dGothic);
//    titleStudentInfo.setTypeface(dGothic);
    studentNameEditText.setTypeface(dGothic);
//    studentLastNameEditText.setTypeface(dGothic);
    studentCanReadLabelText.setTypeface(dGothic);
    studentAliasEditText.setTypeface(dGothic);
//    titleStudentSex.setTypeface(dGothic);
    textBoy.setTypeface(dGothic);
    textGirl.setTypeface(dGothic);
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
    confirmTutorPhone.setTypeface(dGothic);
  }

  //validation
  public void setUpTextWatchers() {

    aliasTextWatcher = new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(Constants.REGISTER_LOG_TAG, "student alias changed to: " + s);

        studentUsernameValid = false;
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    };

    studentAliasEditText.addTextChangedListener(aliasTextWatcher);

    mailTextWatcher = new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(Constants.REGISTER_LOG_TAG, "tutor mail changed to: " + s);
        tutorMailValid = false;
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    };

    tutorMailEditText.addTextChangedListener(mailTextWatcher);
  }

  public boolean validateStudentUsername(String username) {


    if (username.length() >= Constants.USERNAME_MIN_LENGTH) {
      if (!studentUsernameValid) {
        if (!validationTutorPending && !validationStudentPending) {
          validationStudentPending = true;
          checkLoginStatus(username, UserType.ALUMNO);
        }
      } else {
        return true;
      }
    } else {
      showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getText(R.string.Invalid_username).toString(), null, null, null, null);
    }

    return false;
  }

  public boolean validateTutorMail(String mail) {

    if (mail.length() > Constants.USERNAME_MIN_LENGTH && isValidEmail(mail)) {
      if (!tutorMailValid) {
        if (!validationStudentPending && !validationTutorPending) {
          validationTutorPending = true;
          checkLoginStatus(mail, UserType.TUTOR);
        }
      } else {
        return true;
      }
    } else {
      showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getText(R.string.Invalid_mail_address).toString(), null, null, null, null);
    }

    return false;
  }

  public boolean isValidName(String name) {
    if (name.length() > 0) {
      return true;
    }
    return false;
  }

  public boolean isValidPassword(String password) {
    if (password.length() >= Constants.PASSWORD_MIN_LENGTH) {
      return true;
    }
    return false;
  }

  public boolean isValidPhone(String phone) {
    if (phone.length() > 0) {
      return true;
    }
    return false;
  }

  public boolean isValidEmail(String mail) {
    boolean isValid = false;

    String expression = "^[\\_a-zA-Z0-9-\\.-]+@([\\_a-zA-Z0-9-\\-]+\\.)+[A-Z]{2,4}$";

    CharSequence inputStr = mail;

    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputStr);
    if (matcher.matches()) {
      isValid = true;
    }
    return isValid;
  }

  //scrollView listener methods
  public void onScrollChanged(RegisterScrollView scrollView, int x, int y, int oldx, int oldy) {
    clearEditTextFocus();
  }

  //Check login status
  private void checkLoginStatus(final String username, final UserType type) {

    if (!NetworkStatus.isConnected(this.getApplicationContext())) {
      showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.You_must_be_connected_to_the_internet), null, null, null, null);
      return;
    }

    SmartickRestClient.get().getLoginStatus(username,
        " ",
        sysInfo.getInstallationId(),
        sysInfo.getDevice(),
        sysInfo.getVersion(),
        sysInfo.getOsVersion(),
        new Callback<LoginStatusResponse>() {
          @Override
          public void success(LoginStatusResponse loginStatusResponse, Response response) {
            Log.d(Constants.LOGIN_LOG_TAG, "check username " + username + " -> " + loginStatusResponse.getStatus());
            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_INVALID)) {
              if (type.equals(UserType.ALUMNO)) {
                studentUsernameValid = true;
                validationStudentPending = false;
                goNext(null);
              }
              if (type.equals(UserType.TUTOR)) {
                tutorMailValid = true;
                validationTutorPending = false;
                goNext(null);
              }
            } else {
              if (type.equals(UserType.ALUMNO)) {
                studentUsernameValid = false;
                validationStudentPending = false;
                showAlertDialog(getString(R.string.Invalid_username), SweetAlertDialog.WARNING_TYPE, null, null, null, null, null);
              }
              if (type.equals(UserType.TUTOR)) {
                tutorMailValid = false;
                validationTutorPending = false;
                showAlertDialog(getString(R.string.Invalid_username), SweetAlertDialog.WARNING_TYPE, null, null, null, null, null);
              }
            }
          }

          @Override
          public void failure(RetrofitError error) {
            // something went wrong
            validationTutorPending = false;
            validationStudentPending = false;
            showAlertDialog(getString(R.string.Notice), SweetAlertDialog.ERROR_TYPE, getString(R.string.Something_went_wrong_try_again_later), null, null, null, null);
          }
        });
  }

  //Register request
  public void registerTutor() {

    pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
    pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.BlueColor));
    pDialog.getProgressHelper().setRimColor(getResources().getColor(R.color.LightBlueColor));
    pDialog.setTitleText(getString(R.string.Loading));
    pDialog.setCancelable(false);
    pDialog.show();

    if (!NetworkStatus.isConnected(this.getApplicationContext())) {
      pDialog.dismiss();
      showAlertDialog(getString(R.string.You_must_be_connected_to_the_internet), SweetAlertDialog.WARNING_TYPE, null, null, null, null, null);
      return;
    }

    SmartickRestClient.get().registerTutorMobile(tutorMail,
        tutorPassword,
        tutorName,
        tutorLastName,
        tutorPhone,
        sysInfo.getLocale(),
        sysInfo.getInstallationId(),
        sysInfo.getDevice(),
        sysInfo.getVersion(),
        sysInfo.getOsVersion(),
        new Callback<RegisterTutorResponse>() {
          @Override
          public void success(RegisterTutorResponse registerTutorResponse, Response response) {
            if (registerTutorResponse != null && registerTutorResponse.getStatus().equals(SmartickAPI.REGISTER_OK)) {
              //adds new tutor to userList stored locally
              negotiateStoreUsers(registerTutorResponse.getTutorMail(), registerTutorResponse.getPassword(), UserType.TUTOR);
              registerStudent();
            } else {
              pDialog.dismiss();
              showAlertDialog(getString(R.string.Notice), SweetAlertDialog.ERROR_TYPE, getString(R.string.Something_went_wrong_try_again_later), null, null, null, null);
            }
          }

          @Override
          public void failure(RetrofitError error) {
            pDialog.dismiss();
            showAlertDialog(getString(R.string.Notice), SweetAlertDialog.ERROR_TYPE, getString(R.string.Something_went_wrong_try_again_later), null, null, null, null);
          }
        });
  }

  public void registerStudent() {
    Log.d(Constants.REGISTER_LOG_TAG, "Register student");

    SmartickRestClient.get().registerAlumnoMobile(studentUsername,
        studentPassword,
        studentName,
        "",
        String.valueOf(studentBirthDay),
        String.valueOf(studentBirthMonth),
        String.valueOf(studentBirthYear),
        studentSex,
        String.valueOf(studentCanRead),
        tutorMail,
        sysInfo.getLocale(),
        sysInfo.getInstallationId(),
        sysInfo.getDevice(),
        sysInfo.getVersion(),
        sysInfo.getOsVersion(),
        new Callback<RegisterAlumnoResponse>() {
          @Override
          public void success(RegisterAlumnoResponse registerAlumnoResponse, Response response) {

            pDialog.dismiss();

            if (registerAlumnoResponse != null && registerAlumnoResponse.getStatus().equals(SmartickAPI.REGISTER_OK)) {
              negotiateStoreUsers(registerAlumnoResponse.getUsername(), registerAlumnoResponse.getPassword(), UserType.ALUMNO);
              new AsyncTrackConversion().execute();
              goToLogin();
            } else {
              showAlertDialog(getString(R.string.Notice), SweetAlertDialog.ERROR_TYPE, getString(R.string.Something_went_wrong_try_again_later), null, null, null, null);
            }
          }

          @Override
          public void failure(RetrofitError error) {
            pDialog.dismiss();
            showAlertDialog(getString(R.string.Notice), SweetAlertDialog.ERROR_TYPE, getString(R.string.Something_went_wrong_try_again_later), null, null, null, null);
          }
        });
  }

  //Local storage
  private boolean negotiateStoreUsers(String username, String password, UserType type) {
    User newUser = new User(username, password, type.toString());
    if (!isStoredUser(newUser)) {
      localDB.addUser(newUser);
      return true;
    }
    return false;
  }

  /**
   * busca un usuario en db y devuelve su id si existe
   */
  private boolean isStoredUser(User newUser) {
    return (localDB.getAllUsers().contains(newUser));
  }

  //Alert Dialog
  private void showAlertDialog(String titleText, int type, String contentText, String cancelButtonText, SweetAlertDialog.OnSweetClickListener cancelListener, String confirmButtonText, SweetAlertDialog.OnSweetClickListener confirmListener) {
    SweetAlertDialog alertDialog = new SweetAlertDialog(this, type);

    if (cancelButtonText != null) {
      alertDialog.setCancelText(cancelButtonText);
    }

    if (cancelListener != null) {
      alertDialog.setCancelClickListener(cancelListener);
    }

    if (confirmButtonText != null) {
      alertDialog.setConfirmText(confirmButtonText);
    }

    if (contentText != null) {
      alertDialog.setContentText(contentText);
    }

    if (confirmListener != null) {
      alertDialog.setConfirmClickListener(confirmListener);
    }

    alertDialog.setTitleText(titleText);
    alertDialog.show();
  }

  //Conversion tracking
  private void trackConversion() {

    if (sysInfo != null && sysInfo.getExtra() != null) {

      try {
        HttpGet getPixel = new HttpGet(Constants.EMMA_PIXEL_URL);
        HttpClient client = new DefaultHttpClient();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        client.execute(getPixel, responseHandler);
      } catch (IOException e) {
        Log.d(Constants.REGISTER_LOG_TAG, "eMMa conversion tracking failed - " + e.toString());
      }
    }
  }

  private class AsyncTrackConversion extends AsyncTask<String, Integer, String> {
    @Override
    protected String doInBackground(String... params) {
      Log.d(Constants.REGISTER_LOG_TAG, "eMMa conversion tracking started...");
      trackConversion();
      return null;
    }

    @Override
    protected void onPostExecute(String urlRedirect) {
      Log.d(Constants.REGISTER_LOG_TAG, "eMMa conversion tracking finsihed");
    }
  }
}
