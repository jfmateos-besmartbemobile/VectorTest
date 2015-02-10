package com.smartick.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gorgue.myapplication.R;


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

        Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");
        TextView textMasInformacion = (TextView)findViewById(R.id.main_label_mas_informacion);
        textMasInformacion.setTypeface(tfDidact);

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
