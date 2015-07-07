package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.EFStrokeTextView;
import com.mobile.android.smartick.network.ClearFreemiumSessionResponse;
import com.mobile.android.smartick.network.GetFreemiumSessionStatusResponse;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.FreemiumProfile;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.util.Constants;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class WelcomeActivity extends Activity {

    private SystemInfo sysInfo;
    private FreemiumProfile freemiumProfile;
    private Boolean disableButtons = false;
    private AlertDialog freemiumAlertDialog;
    private View buttonIntro;

    private static final int MIN_INTRO_RAM = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);

        //systemInfo init
        sysInfo = new SystemInfo(this.getApplicationContext());

        //defaults to intro if its the first time running
        //Checks RAM available
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.d(Constants.WELCOME_LOG_TAG,"Memory available: " + memoryClass + " MB");
        if (sysInfo.isFirstTimeRunning()){
            if (memoryClass > MIN_INTRO_RAM){
                //enough ram to run intro
                goToIntro();
            }else{
                //bot enough ram, disable intro button
                buttonIntro = findViewById(R.id.intro_button);
                buttonIntro.setVisibility(View.GONE);
            }
        }

        //Get freemium profile info
        freemiumProfile = new FreemiumProfile(this.getApplicationContext());

        // Adapt every textView to custom font
        Typeface tfBoogaloo = Typeface.createFromAsset(getAssets(), "fonts/Boogaloo-Regular.otf");
        Button botonInvitado = (Button)findViewById(R.id.main_button_invitado);
        botonInvitado.setTypeface(tfBoogaloo);
        Button botonRegistro = (Button)findViewById(R.id.main_button_registro);
        botonRegistro.setTypeface(tfBoogaloo);
        TextView textIntro = (TextView)findViewById(R.id.main_intro_text);
        textIntro.setTypeface(tfBoogaloo);
        Button botonLogin = (Button)findViewById(R.id.main_button_login);
        botonLogin.setTypeface(tfBoogaloo);
    }

    public void irLogin(View view) {
        if (!disableButtons){
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    public void irRegistro(View view) {
        if (!disableButtons) {
            startActivity(new Intent(this, RegistroActivity.class));
        }
    }

    public void irIntro(View view) {
        if (!disableButtons) {
            startActivity(new Intent(this, IntroActivity.class));
        }
    }

    public void freemiumButtonPressed(View view){
        disableButtons = true;
        //Check for today's freemium session status
        SmartickRestClient.get().getFreemiumSessionStatus(sysInfo.getInstallationId(),
        new Callback<GetFreemiumSessionStatusResponse>() {
            @Override
            public void success(GetFreemiumSessionStatusResponse getFreemiumSessionStatusResponse, Response response) {
                Log.d(Constants.WELCOME_LOG_TAG, "getFreemiumSessionSatus RESPONSE: last Freemium session on - : " + getFreemiumSessionStatusResponse.getLastSessionDate());
                disableButtons = false;
                if (getFreemiumSessionStatusResponse.getSessionFinished() != null && getFreemiumSessionStatusResponse.getSessionFinished() == true){
                    //today's session has been completed -> show alert with options to user
                    showFreemiumSessionAlert();
                }else{
                    //go to freemium activity
                    irFreemium();
                }
            }
            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.WELCOME_LOG_TAG, "getFreemiumSessionSatus ERROR: " + error);
                disableButtons = false;

                // network error
                showAlertDialog(getString(R.string.Notice),
                        SweetAlertDialog.ERROR_TYPE,
                        getString(R.string.You_must_be_connected_to_the_internet),
                        null, null, getString(R.string.OK), null);
            }
        });
    }

    public void irFreemium() {
        if (!disableButtons) {
            startActivity(new Intent(this, FreemiumActivity.class));
        }
    }

    public void goToFreemiumSession(){
        Intent intent = new Intent(this, FreemiumMainActivity.class);
        intent.putExtra("selectedAvatar", freemiumProfile.getAvatar());
        intent.putExtra("selectedAge", freemiumProfile.getAge());
        startActivity(intent);
    }

    public void showFreemiumSessionAlert(){

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

    public void freemiumWarningVWButtonPressed(View view){
        WelcomeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                freemiumAlertDialog.dismiss();
                goToFreemiumSession();
            }
        });
    }

    public void freemiumWarningRestartPressed(View view){
        WelcomeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableButtons = true;
                SmartickRestClient.get().clearFreemoumSessionStatus(sysInfo.getInstallationId(),
                        new Callback<ClearFreemiumSessionResponse>() {
                            @Override
                            public void success(ClearFreemiumSessionResponse clearFreemiumSessionResponse, Response response) {
                                Log.d(Constants.WELCOME_LOG_TAG, "clearFreemiumSession RESPONSE: Freemium session deleted - : " + clearFreemiumSessionResponse.getDeleted());
                                disableButtons = false;
                                freemiumAlertDialog.dismiss();

                                if (clearFreemiumSessionResponse.getDeleted()) {
                                    irFreemium();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d(Constants.WELCOME_LOG_TAG, "clearFreemiumSession ERROR: " + error);
                                freemiumAlertDialog.dismiss();
                                disableButtons = false;

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

    public void freemiumWarningCancelPressed(View view){
        WelcomeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                freemiumAlertDialog.dismiss();
            }
        });
    }

    private void showAlertDialog(String titleText,int type,String contentText,String cancelButtonText, SweetAlertDialog.OnSweetClickListener cancelListener, String confirmButtonText,SweetAlertDialog.OnSweetClickListener confirmListener){
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, type);
        if (cancelButtonText != null){
            alertDialog.setCancelText(cancelButtonText);
        }
        if (cancelListener != null){
            alertDialog.setCancelClickListener(cancelListener);
        }
        if (confirmButtonText != null){
            alertDialog.setConfirmText(confirmButtonText);
        }
        if (contentText != null){
            alertDialog.setContentText(contentText);
        }
        if (confirmListener != null){
            alertDialog.setConfirmClickListener(confirmListener);
        }

        alertDialog.setTitleText(titleText);
        alertDialog.show();
    }

    private void goToIntro(){
        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
    }
}
