package com.mobile.android.smartick.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.EFStrokeTextView;
import com.mobile.android.smartick.network.GetFreemiumSessionStatusResponse;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.util.Constants;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class WelcomeActivity extends ActionBarActivity {

    private SystemInfo sysInfo;
    private Boolean disableButtons = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Inicializamos systemInfo
        sysInfo = new SystemInfo(this.getApplicationContext());

        // Cambiamos la fuente de los botones de registro e invitado
        Typeface tfBoogaloo = Typeface.createFromAsset(getAssets(), "fonts/Boogaloo-Regular.otf");
        Button botonInvitado = (Button)findViewById(R.id.main_button_invitado);
        botonInvitado.setTypeface(tfBoogaloo);
        Button botonRegistro = (Button)findViewById(R.id.main_button_registro);
        botonRegistro.setTypeface(tfBoogaloo);
        TextView textIntro = (TextView)findViewById(R.id.main_intro_text);
        textIntro.setTypeface(tfBoogaloo);
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
            }
        });
    }

    public void irFreemium() {
        if (!disableButtons) {
            startActivity(new Intent(this, FreemiumActivity.class));
        }
    }

    public void showFreemiumSessionAlert(){
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);

        alertDialog.setCancelText("Cancelar");
        alertDialog.setConfirmText("Ir a Mundo Virtual");

        alertDialog.setContentText(getString(R.string.already_completed_freemium_session));

        alertDialog.setTitleText(getString(R.string.Warning));
        alertDialog.show();

    }
}
