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
 		View loginbutton = findViewById(R.id.loginUser);
 		View registerbutton = findViewById(R.id.registerUser);

 		//usuario existente -> hacer login
         loginbutton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 toLoginActivity();
             }
         });
         //nuevo usuario -> se le manda a la home usando MainActivity
         registerbutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				toMainActivity();
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
     
     
     
     
     
}