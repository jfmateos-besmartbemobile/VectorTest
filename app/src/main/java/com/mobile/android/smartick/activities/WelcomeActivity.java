package com.mobile.android.smartick.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
        TextView textIntro = (TextView)findViewById(R.id.main_intro_text);
        textIntro.setTypeface(tfBoogaloo);
    }

    public void irLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void irRegistro(View view) {
        startActivity(new Intent(this, RegistroActivity.class));
    }

    public void irIntro(View view) {
        startActivity(new Intent(this, IntroActivity.class));
    }
}
