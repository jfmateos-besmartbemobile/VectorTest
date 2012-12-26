package com.smartick.activities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.smartick.pojos.ListUser;
import com.smartick.utils.Constants;
import com.smartick.utils.NetworkUtils;
import com.smartick.utils.UsersListAdapter;

@SuppressLint("NewApi")
public class LoginActivity extends ListActivity {

	private EditText username;
	private EditText password;
	private String urlResult;
	private String avatarUrl;
	Map<String, String> usersMap = new HashMap<String, String>();

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!NetworkUtils.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))){
        	NetworkUtils.toOfflineActivity(this);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        restoreSaveUsers();
        prepareView();
    }
    
    /*Prepara los elementos del login*/
    private void prepareView(){
		View button = findViewById(R.id.buttonLogin);
		TextView singIn = (TextView) findViewById(R.id.singIn);
		singIn.setMovementMethod(LinkMovementMethod.getInstance());
		username = (EditText) findViewById(R.id.loginUsername);
		password = (EditText) findViewById(R.id.loginPassword);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	doForm();
            }
        });
        singIn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				toMainActivity();
			}
		});
    }

    /*Envía el formulario*/
    private void doForm(){
    	try {
			new LoginSmartick().execute(new URL(Constants.URL_SMARTICK_LOGIN));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    }
    
    
    /*Se hace en otra clase porque las llamadas http hay que hacerlas en un hilo asíncrono*/
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
    
    /*Si el login es correcto se pasa al webview*/
    private void redirectLogin(){
        if(!urlResult.contains("acceso")){
        	try {
				negotiateStoreUsers();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	toMainActivity();
        }else{
        	showAlertDialog();
        }
    }
    
    /*Pasa a la actividad principal pasándole la url de destino como parámetro*/
    private void toMainActivity(){
    	Intent intent = new Intent(this, MainActivity.class);
    	intent.putExtra("url", urlResult);
    	startActivity(intent);
    }
    
    /*Dialog de login incorrecto*/
    private void showAlertDialog(){
    	AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
    	alertDialog.setMessage("Has introducido un usuario o una contraseña incorrectas");
    	alertDialog.setIcon(drawable.ic_delete);
    	alertDialog.show();
    }
    
    /*Llamada al login del servidor*/
    private void doHttpPost(){
    	urlResult = null;
    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	MyRedirectHandler handler = new MyRedirectHandler();
    	httpClient.setRedirectHandler(handler);

    	HttpPost httpost = new HttpPost(Constants.URL_SMARTICK_LOGIN);
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
    
    
    /*Captura las redirecciones que se producen. Nos quedamos con la primera porque en las siguientes llamadas el urlrewrite del servidor borra la jsessionid*/
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

    /*Guarda, si no está ya guardado, un usuario en local*/
    private void negotiateStoreUsers() throws IOException{
		String token = username.getText().toString()+Constants.TOKEN_SEPARATOR+password.getText().toString()+Constants.TOKEN_SEPARATOR+avatarUrl+Constants.USER_SEPARATOR;
    	try {
			String fileContent = readUsersInStore();
			if(!fileContent.toString().contains(token)){
				saveUserInStore(token);
			}
		} catch (FileNotFoundException e) {
			saveUserInStore(token);
		}
    }
    
    /*Guarda un usuario*/
    private void saveUserInStore(String token) throws IOException{
		FileOutputStream usersFile = openFileOutput(Constants.USERS_FILE, Context.MODE_APPEND);
		usersFile.write(token.getBytes());
		usersFile.close();
    }
    
    /*Lee del almacenanimiento local los usuarios*/
    private String readUsersInStore() throws FileNotFoundException, IOException{
    	FileInputStream usersFile = openFileInput(Constants.USERS_FILE);
		StringBuffer fileContent = new StringBuffer("");
		byte[] buffer = new byte[1024];
		int length;
		while ((length = usersFile.read(buffer)) != -1) {
		    fileContent.append(new String(buffer));
		}
		return fileContent.toString();
    }
    
    /*Recoge los usuarios almacenados y los añade a la lista*/
	private void restoreSaveUsers(){
    	String fileContent = "";
    	List<ListUser> usersList = new ArrayList<ListUser>();
		try {
			fileContent = readUsersInStore();
		} catch (FileNotFoundException fe){
			try {
				saveUserInStore("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String userPassword : fileContent.toString().split(Constants.USER_SEPARATOR)) {
			if(!userPassword.isEmpty() && userPassword.contains(Constants.TOKEN_SEPARATOR)){
				usersMap.put(userPassword.split(Constants.TOKEN_SEPARATOR)[0], userPassword.split(Constants.TOKEN_SEPARATOR)[1]);
				ListUser listUser = new ListUser(userPassword.split(Constants.TOKEN_SEPARATOR)[0], userPassword.split(Constants.TOKEN_SEPARATOR)[1], userPassword.split(Constants.TOKEN_SEPARATOR)[2]);
				usersList.add(listUser);
			}
		}
		if(usersList.isEmpty()){
			hideOldUsers();
		}else{
			prepareListView(usersList);
		}
    }
    
	/*Esconde, si el fichero de almacenamiento está vacío, la parte de la pantalla de usuarios ya logados*/
    private void hideOldUsers(){
    	TextView tv = (TextView) findViewById(R.id.otherUser);
    	ListView lv = (ListView) findViewById(android.R.id.list);
    	tv.setVisibility(View.GONE);
    	lv.setVisibility(View.GONE);
    }
    
    /*Prepara la lista de usuarios*/
    private void prepareListView(List<ListUser> usersList){
    	setListAdapter(new UsersListAdapter(this, R.layout.users_list, usersList));
    	ListView lv = getListView();  
    	lv.setTextFilterEnabled(true);  
    	lv.setOnItemClickListener(new OnItemClickListener() {    
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {      
    			setUserValues((String) ((TextView)view.findViewById(R.id.nameUser)).getText());
    			}  
    		});
    }    
    
    private void setUserValues(String user){
    	username.setText(user);
    	password.setText(usersMap.get(user));
    	doForm();
    }
    
}