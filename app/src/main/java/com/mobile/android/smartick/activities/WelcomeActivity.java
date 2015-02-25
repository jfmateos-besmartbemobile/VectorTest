package com.mobile.android.smartick.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.EFStrokeTextView;


public class WelcomeActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Cambiamos la fuente de los botones de registro e invitado
        Typeface tfBoogaloo = Typeface.createFromAsset(getAssets(), "fonts/Boogaloo-Regular.otf");
        Button botonInvitado = (Button)findViewById(R.id.main_button_invitado);
        botonInvitado.setTypeface(tfBoogaloo);
        Button botonRegistro = (Button)findViewById(R.id.main_button_registro);
        botonRegistro.setTypeface(tfBoogaloo);

//        LinearLayout layout = (LinearLayout)findViewById(R.id.activity_welcome_linear_layout);
//
//        EFStrokeTextView invitadoTextView = new EFStrokeTextView(getApplicationContext());
//        invitadoTextView.setText("invitadoStroke");
//        invitadoTextView.setTextColor(0xFFFFFF);
//        invitadoTextView.setStrokeColor(0xFF0000);
//        layout.addView(invitadoTextView);

/*        Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");
        TextView textMasInformacion = (TextView)findViewById(R.id.main_label_mas_informacion);
        textMasInformacion.setTypeface(tfDidact);*/

    }

    /** Ya es usuario de smartick */
    public void irLogin(View view) {
        // Cambiamos a la pantalla de login
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void irRegistro(View view) {
        // Cambiamos a la pantalla de registro
        startActivity(new Intent(this, RegistroActivity.class));
    }
}
