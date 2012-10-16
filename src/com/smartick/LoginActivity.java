package com.smartick;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;


public class LoginActivity extends Activity {

	private EditText username;
	private EditText password;
	private String urlResult;
	
	private static final String URL_CONTEXT = "http://www.smartick.es/";
	private static final String URL_SMARTICK_LOGIN = URL_CONTEXT+"smartick_login";
	private static final String USERS_FILE = "usersSmk";
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private class LoginSmartick extends AsyncTask<URL, Integer, Long> {
    	@Override
        protected Long doInBackground(URL... urls) {
    		try{
                String urlEncodedContent = preparePostContent();
                HttpURLConnection urlConnection = doHttpPost(URL_SMARTICK_LOGIN, urlEncodedContent);
                urlConnection.connect();
                urlConnection.getInputStream();
                urlResult = urlConnection.getURL().toString();
            }
            catch(IOException ioException){
    			// TODO Auto-generated catch block
            }
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
    
    
    private HttpURLConnection doHttpPost(String targetUrl, String content) throws IOException{
        HttpURLConnection urlConnection = null;
        DataOutputStream dataOutputStream = null;
        try{
            urlConnection = (HttpURLConnection)(new URL(targetUrl).openConnection());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());

            // throws IOException
            dataOutputStream.writeBytes(content);
            dataOutputStream.flush();
            dataOutputStream.close();

            return urlConnection;
        }
        catch(IOException ioException){
            if (dataOutputStream != null){
                try{
                    dataOutputStream.close();
                }
                catch(Throwable ignore){
                }
            }
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            throw ioException;
        }
    }
    
    private String preparePostContent() throws UnsupportedEncodingException{
        String encodedLoginUserName = URLEncoder.encode(username.getText().toString(), "UTF-8");
        String encodedLoginPassword = URLEncoder.encode(password.getText().toString(), "UTF-8");
        return "j_username="+encodedLoginUserName+"&j_password="+encodedLoginPassword;
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