package com.mobile.android.smartick.activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.emma.android.eMMa;
import com.emma.android.eMMaUserInfoInterface;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.mobile.android.smartick.R;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.network.ClearFreemiumSessionResponse;
import com.mobile.android.smartick.network.GetFreemiumSessionStatusResponse;
import com.mobile.android.smartick.network.RegisterAppActivationResponse;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.FreemiumProfile;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.services.SmartickRegistrationIntentService;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.util.LocaleHelper;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.List;

public class WelcomeActivity extends Activity implements eMMaUserInfoInterface {

  private SystemInfo sysInfo;
  private FreemiumProfile freemiumProfile;
  private Boolean disableButtons = false;
  private AlertDialog freemiumAlertDialog;
  private View buttonIntro;

  private Button freemiumButton;
  private Button loginButton;
  private Button registerButton;

  //GCM
  private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
  private BroadcastReceiver mRegistrationBroadcastReceiver;

  private static final int MIN_INTRO_RAM = 40;

  private UsersDBHandler localDB = new UsersDBHandler(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Set up selected language
    LocaleHelper.onCreate(this);

    //Inits Facebook SDK
    FacebookSdk.sdkInitialize(getApplicationContext());

    //Register for GCM
    if (checkPlayServices()) {
      SharedPreferences sharedPreferences =
          PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
      boolean sentToken = sharedPreferences
          .getBoolean(Constants.SENT_GCM_TOKEN_PREF_NAME, false);
      if (sentToken) {
        Log.d(Constants.WELCOME_LOG_TAG, "already registered for GCM!");
      } else {
        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, SmartickRegistrationIntentService.class);
        startService(intent);
      }
    }

    //Inits eMMa tracking
    eMMa.starteMMaSession(this.getApplication(), Constants.EMMA_KEY);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_welcome);


    //systemInfo init
    sysInfo = new SystemInfo(this.getApplicationContext());

    eMMa.getUserInfo(this);

    //defaults to intro if its the first time running
    //Checks RAM available
    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    int memoryClass = am.getMemoryClass();
    float scrDensity = getResources().getDisplayMetrics().density;
    Log.d(Constants.WELCOME_LOG_TAG, "Screen density is: " + scrDensity);
    Log.d(Constants.WELCOME_LOG_TAG, "Memory available: " + memoryClass + " MB");
    if (sysInfo.isFirstTimeRunning()) {
      if (memoryClass > MIN_INTRO_RAM * scrDensity) {
        //enough ram to run intro
        goToIntro();
      } else {
        //not enough ram, disable intro button
        buttonIntro = findViewById(R.id.intro_button);
        buttonIntro.setVisibility(View.GONE);
      }

      //Register app activation with server
      registerAppActivation();
    } else {
      //if there are already stored users we default to loginActivity
      List<User> users = localDB.getAllUsers();
      if (!users.isEmpty()) {
        goToLogin();
      }
    }

    //not enough ram, disable intro button
    if (memoryClass < MIN_INTRO_RAM * scrDensity) {
      buttonIntro = findViewById(R.id.intro_button);
      buttonIntro.setVisibility(View.GONE);
    }

    //Get freemium profile info
    freemiumProfile = new FreemiumProfile(this.getApplicationContext());

    //Sets upf button reference variables
    freemiumButton = (Button) findViewById(R.id.main_button_invitado);
    loginButton = (Button) findViewById(R.id.main_button_login);
    registerButton = (Button) findViewById(R.id.main_button_registro);

    // Adapt every textView to custom font
    Typeface tfBoogaloo = Typeface.createFromAsset(getAssets(), "fonts/Boogaloo-Regular.otf");
    Typeface tfDidactGothic = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");

    Button botonInvitado = (Button) findViewById(R.id.main_button_invitado);
    botonInvitado.setTransformationMethod(null);
    botonInvitado.setTypeface(tfBoogaloo);
    Button botonRegistro = (Button) findViewById(R.id.main_button_registro);
    botonRegistro.setTypeface(tfBoogaloo);
    botonRegistro.setTransformationMethod(null);
    TextView textIntro = (TextView) findViewById(R.id.main_intro_text);
    textIntro.setTypeface(tfBoogaloo);
    textIntro.setTransformationMethod(null);
    Button botonLogin = (Button) findViewById(R.id.main_button_login);
    botonLogin.setTypeface(tfDidactGothic);
    botonLogin.setTransformationMethod(null);
  }


  @Override
  protected void onResume() {
    super.onResume();

    disableButtons = false;
    enableButtons(true);

    // Facebook logs 'install' and 'app activate' App Events.
    AppEventsLogger.activateApp(this);
  }

  @Override
  protected void onPause() {

    // Facebook logs 'app deactivate' App Event.
    AppEventsLogger.deactivateApp(this);

    super.onPause();

  }

  private boolean isFacebookAppInstalled() {
    try {
      ApplicationInfo info = getPackageManager().
          getApplicationInfo("com.facebook.katana", 0);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
  }

  public void irLogin(View view) {
    if (!disableButtons) {
      disableButtons = true;
      enableButtons(false);
      startActivity(new Intent(this, LoginActivity.class));
    }
  }

  public void irRegistro(View view) {
    if (!disableButtons) {
      disableButtons = true;
      enableButtons(false);
      startActivity(new Intent(this, RegistroActivity.class));
    }
  }

  public void irIntro(View view) {
    if (!disableButtons) {
      disableButtons = true;
      enableButtons(false);
      startActivity(new Intent(this, IntroActivity.class));
    }
  }

  public void freemiumButtonPressed(View view) {
    disableButtons = true;
    enableButtons(false);

    //Check for today's freemium session status
    SmartickRestClient.get().getFreemiumSessionStatus(sysInfo.getInstallationId(),
        new Callback<GetFreemiumSessionStatusResponse>() {
          @Override
          public void success(GetFreemiumSessionStatusResponse getFreemiumSessionStatusResponse, Response response) {
            Log.d(Constants.WELCOME_LOG_TAG, "getFreemiumSessionSatus RESPONSE: last Freemium session on - : " + getFreemiumSessionStatusResponse.getLastSessionDate());
            disableButtons = false;
            enableButtons(true);
            if (getFreemiumSessionStatusResponse.getSessionFinished() != null && getFreemiumSessionStatusResponse.getSessionFinished() == true) {
              //today's session has been completed -> show alert with options to user
              showFreemiumSessionAlert();
            } else {
              //go to freemium activity
              irFreemium();
            }
          }

          @Override
          public void failure(RetrofitError error) {
            Log.d(Constants.WELCOME_LOG_TAG, "getFreemiumSessionSatus ERROR: " + error);
            disableButtons = false;
            enableButtons(true);

            // network error
            showAlertDialog(getString(R.string.Notice),
                SweetAlertDialog.ERROR_TYPE,
                getString(R.string.You_must_be_connected_to_the_internet),
                null, null, getString(R.string.OK), null);
          }
        });
  }

  public void irFreemium() {
    enableButtons(false);
    startActivity(new Intent(this, FreemiumActivity.class));
  }

  public void goToFreemiumSession() {
    enableButtons(false);
    Intent intent = new Intent(this, FreemiumMainActivity.class);
    intent.putExtra("selectedAvatar", freemiumProfile.getAvatar());
    intent.putExtra("selectedAge", freemiumProfile.getAge());
    startActivity(intent);

  }

  public void showFreemiumSessionAlert() {

    WelcomeActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Context context = WelcomeActivity.this;
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View freemiumWarningView = li.inflate(R.layout.freemium_warning_dialog, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setView(freemiumWarningView);

        //sets modal content
        Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");
        TextView titleWarning = (TextView) freemiumWarningView.findViewById(R.id.titleWarning);
        titleWarning.setTypeface(tfDidact);
        titleWarning.setText(getString(R.string.Warning));

        TextView textWarning = (TextView) freemiumWarningView.findViewById(R.id.textFreemiumWarning);
        textWarning.setTypeface(tfDidact);
        textWarning.setText(getString(R.string.already_completed_freemium_session));

        ((Button) freemiumWarningView.findViewById(R.id.freemiumWarningVWButton)).setTypeface(tfDidact);
        ((Button) freemiumWarningView.findViewById(R.id.freemiumWarningRestartButton)).setTypeface(tfDidact);
        ((Button) freemiumWarningView.findViewById(R.id.freemiumWarningCancelButton)).setTypeface(tfDidact);

        freemiumAlertDialog = alertBuilder.create();
        freemiumAlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        freemiumAlertDialog.setCanceledOnTouchOutside(false);

        //shows dialog
        freemiumAlertDialog.show();
      }
    });
  }

  public void freemiumWarningVWButtonPressed(View view) {
    WelcomeActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        freemiumAlertDialog.dismiss();
        goToFreemiumSession();
      }
    });
  }

  public void freemiumWarningRestartPressed(View view) {
    WelcomeActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        disableButtons = true;
        enableButtons(false);
        SmartickRestClient.get().clearFreemoumSessionStatus(sysInfo.getInstallationId(),
            new Callback<ClearFreemiumSessionResponse>() {
              @Override
              public void success(ClearFreemiumSessionResponse clearFreemiumSessionResponse, Response response) {
                Log.d(Constants.WELCOME_LOG_TAG, "clearFreemiumSession RESPONSE: Freemium session deleted - : " + clearFreemiumSessionResponse.getDeleted());
                freemiumAlertDialog.dismiss();

                if (clearFreemiumSessionResponse.getDeleted()) {
                  irFreemium();
                } else {
                  disableButtons = false;
                  enableButtons(true);
                }
              }

              @Override
              public void failure(RetrofitError error) {
                Log.d(Constants.WELCOME_LOG_TAG, "clearFreemiumSession ERROR: " + error);
                freemiumAlertDialog.dismiss();
                disableButtons = false;
                enableButtons(true);

                // error conexion
                showAlertDialog(getString(R.string.You_must_be_connected_to_the_internet),
                    SweetAlertDialog.ERROR_TYPE,
                    null,
                    null, null, getString(R.string.OK), null);
              }
            });
      }
    });
  }

  public void freemiumWarningCancelPressed(View view) {
    WelcomeActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        freemiumAlertDialog.dismiss();
      }
    });
  }

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

  private void goToIntro() {
    Intent intent = new Intent(this, IntroActivity.class);
    startActivity(intent);
  }

  private void goToLogin() {
    startActivity(new Intent(this, LoginActivity.class));
  }


  private boolean checkPlayServices() {
    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
        GooglePlayServicesUtil.getErrorDialog(resultCode, this,
            PLAY_SERVICES_RESOLUTION_REQUEST).show();
      } else {
        Log.i(Constants.WELCOME_LOG_TAG, "This device is not supported.");
        finish();
      }
      return false;
    }
    return true;
  }

  private void registerAppActivation() {
    SmartickRestClient.get().registerAppActivation("Android", sysInfo.getVersion(), sysInfo.getDevice(), sysInfo.getInstallationId(), sysInfo.getExtra(),
        new Callback<RegisterAppActivationResponse>() {
          @Override
          public void success(RegisterAppActivationResponse registerAppActivationResponse, Response response) {
            Log.d(Constants.WELCOME_LOG_TAG, "registerAppActivation RESPONSE: " + registerAppActivationResponse.getResponse());
          }

          @Override
          public void failure(RetrofitError error) {
            Log.d(Constants.WELCOME_LOG_TAG, "registerAppActivation ERROR: " + error);
          }
        });
  }

  private void enableButtons(boolean enabled) {
    freemiumButton.setActivated(enabled);
    registerButton.setActivated(enabled);
    loginButton.setActivated(enabled);
  }


  //Installation origin tracking

  @Override
  public void OnGetUserInfo(JSONObject jsonObject) {

    String referrerId = null;

    if (sysInfo != null) {
      try {
        if (jsonObject.has("emma_referrer_id")) {
          referrerId = (String) jsonObject.get("emma_referrer_id");
          if (referrerId != null) {
            sysInfo.setExtra(referrerId);
          } else {
            sysInfo.setExtra(null);
          }
        } else {
          sysInfo.setExtra(null);
        }
      } catch (JSONException e) {
        e.printStackTrace();
        sysInfo.setExtra(null);
      }
    }
  }

  @Override
  public void OnGetUserID(int i) {
    Log.d(Constants.WELCOME_LOG_TAG, "user id " + i);
  }

}
