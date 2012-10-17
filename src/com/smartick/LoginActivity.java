package com.smartick;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import android.R.drawable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;


public class LoginActivity extends Activity {

	private EditText username;
	private EditText password;
	private String urlResult;
	
	private static final String URL_CONTEXT = "http://10.0.2.2/";
	private static final String URL_SMARTICK_LOGIN = URL_CONTEXT+"smartick_login";
	private static final String USERS_FILE = "usersSmk";
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
		View button = findViewById(R.id.buttonLogin);
		username = (EditText) findViewById(R.id.loginUsername);
		password = (EditText) findViewById(R.id.loginPassword);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	doForm();
            }
        });
    }

    private void doForm(){
    	try {
			new LoginSmartick().execute(new URL(URL_SMARTICK_LOGIN));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    }
    
    private class LoginSmartick extends AsyncTask<URL, Integer, Long> {
    	@Override
        protected Long doInBackground(URL... urls) {
            doHttpPost();
			return null;
        }
    	
        @Override
        protected void onPostExecute(Long result) {
        	redirectLogin();
        }
    }
    
    private void redirectLogin(){
        if(!urlResult.contains("acceso")){
        	try {
				negotiateStoreUsers();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	Intent intent = new Intent(this, MainActivity.class);
        	intent.putExtra("url", urlResult);
        	startActivity(intent);
        }else{
        	showAlertDialog();
        }
    }
    
    private void showAlertDialog(){
    	AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
    	alertDialog.setMessage("Has introducido un usuario o una contraseña incorrectas");
    	alertDialog.setIcon(drawable.ic_delete);
    	alertDialog.show();
    }
    
    private void doHttpPost(){
    	urlResult = null;
    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	MyRedirectHandler handler = new MyRedirectHandler();
    	httpClient.setRedirectHandler(handler);

    	HttpPost httpost = new HttpPost(URL_SMARTICK_LOGIN);
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("j_username", username.getText().toString()));
        nvps.add(new BasicNameValuePair("j_password", password.getText().toString()));
        try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps));
			httpClient.execute(httpost);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }
    
    public class MyRedirectHandler extends DefaultRedirectHandler {

        public URI lastRedirectedUri;

        @Override
        public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
            return super.isRedirectRequested(response, context);
        }

        @Override
        public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
        	lastRedirectedUri = super.getLocationURI(response, context);
        	if(urlResult == null){
        		urlResult = lastRedirectedUri.toString();
        	}
            return lastRedirectedUri;
        }
    }

    private void negotiateStoreUsers() throws IOException{
		String token = username.getText().toString()+"***"+password.getText().toString();
    	try {
			String fileContent = readUsersInStore();
			if(!fileContent.toString().contains(token)){
				saveUserInStore(token);
			}
		} catch (FileNotFoundException e) {
			saveUserInStore(token);
		}
    }
    
    private void saveUserInStore(String token) throws IOException{
		FileOutputStream usersFile = openFileOutput(USERS_FILE, Context.MODE_PRIVATE);
		usersFile.write(token.getBytes());
		usersFile.close();
    }
    
    private String readUsersInStore() throws IOException{
    	FileInputStream usersFile = openFileInput(USERS_FILE);
		StringBuffer fileContent = new StringBuffer("");
		byte[] buffer = new byte[1024];
		int length;
		while ((length = usersFile.read(buffer)) != -1) {
		    fileContent.append(new String(buffer));
		}
		return fileContent.toString();
    }
    
}