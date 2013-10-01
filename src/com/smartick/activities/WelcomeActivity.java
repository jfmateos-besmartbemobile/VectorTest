package com.smartick.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.smartick.utils.NetworkUtils;

public class WelcomeActivity extends Activity{
	
	
	private View loginButton;
	private View registerButton;
	private View alumnoLoginButton; 
	private View tutorLoginButton;
	private View botonVolver;
	private View textoEres;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(!NetworkUtils.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))){
        	NetworkUtils.toOfflineActivity(this);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        
        //establece elementos de la vista
        prepareView();
     }
     
     /*Prepara los elementos del login*/
     private void prepareView(){
    	loginButton = findViewById(R.id.login);
    	registerButton = findViewById(R.id.registerUser);
    	alumnoLoginButton = findViewById(R.id.loginAlumno);
 		tutorLoginButton = findViewById(R.id.loginTutor);
 		botonVolver = findViewById(R.id.back);
 		textoEres = findViewById(R.id.youAre);
 		
 		//hacer login -> se ocultan los botones y se muestran las dos opciones de login
        loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				loginButton.setVisibility(View.GONE);
				registerButton.setVisibility(View.GONE);
				alumnoLoginButton.setVisibility(View.VISIBLE);
				tutorLoginButton.setVisibility(View.VISIBLE);
				textoEres.setVisibility(View.VISIBLE);
				botonVolver.setVisibility(View.VISIBLE);
			}
		});
 		

         //nuevo usuario -> registerActivity
         registerButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				toMainActivity();
 			}
 		});
         
  		//hacer login alumno
         alumnoLoginButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 toLoginActivity();
             }
         });
         
   		//hacer login tutor
         tutorLoginButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 toTutorLoginActivity();
             }
         });
         
    	//boton volver
         botonVolver.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 				loginButton.setVisibility(View.VISIBLE);
 				registerButton.setVisibility(View.VISIBLE);
 				alumnoLoginButton.setVisibility(View.GONE);
 				tutorLoginButton.setVisibility(View.GONE);
 				textoEres.setVisibility(View.GONE);
 				botonVolver.setVisibility(View.GONE);
             }
         });
     }
     
     // intents para paso a Login y Main activities
     private void toMainActivity(){
     	Intent intent = new Intent(this, MainActivity.class);
     	String urlResult = null;
     	intent.putExtra("url", urlResult);
     	startActivity(intent);
     }
     
     private void toLoginActivity(){
     	Intent intent = new Intent(this, LoginActivity.class);
     	startActivity(intent);
     }
     
     private void toTutorLoginActivity(){
    	 Intent intent = new Intent(this, TutorLoginActivity.class);
    	 startActivity(intent);
     }
     
     
     
     
}