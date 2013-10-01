package com.smartick.activities;

import java.io.File;
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
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.smartick.pojos.DeviceInfo;
import com.smartick.pojos.Installation;
import com.smartick.pojos.ListUser;
import com.smartick.utils.Constants;
import com.smartick.utils.NetworkUtils;
import com.smartick.utils.UsersListAdapter;
import com.smartick.utils.usersDBHandler;

@SuppressLint("NewApi")
public class LoginActivity extends ListActivity {

	private EditText username;
	private EditText password;
	private String urlResult;
	private String avatarUrl;
	private String installationId;
	Map<String, String> usersMap = new HashMap<String, String>();
	
	usersDBHandler db;

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!NetworkUtils.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))){
        	NetworkUtils.toOfflineActivity(this);
        }
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        

        // Tutor tiene cuenta almacenada en accounts, de forma que pueda acceder con su cuenta de google, facebook o linkedin
        // Cada tutor almacena en la DB una serie de alumnos que le corresponden
        //1.- Nuevo tutor sin cuenta en smartick
        	//1.a - Pantalla de registro para crear nueva cuenta o login con cuenta de otro lugar
        //2.- Tutor con cuenta en smartick
        	//2.a - Cuenta guardada en account manager, recuperar y comprobar si tiene alumnos en la db
        		//2.a.1 - Si los tiene: cargar la lista normalmente y permitir que a�ada nuevos
        		//2.a.2 - No tiene alumnos - mostrar pantalla para a�adir alguno
        
        
        //Pantalla welcome

        //test obtenci�n de cuenta
        AccountManager am = AccountManager.get(this); // "this" references the current Context
        Account[] ac = am.getAccounts();
        if (ac!= null){
        	for(int i=0;i<ac.length;i++){
        		Log.d("LoginActivity", i + ": Name: " + ac[i].name + " Type: " + ac[i].type);
        	}
        }

        //Comprobacion de id de instalacion y de usuarios almacenados
        SharedPreferences accounts = getSharedPreferences(Constants.USERS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        SharedPreferences install = getSharedPreferences(Constants.INSTALLATION_ID_FILE, Context.MODE_PRIVATE);
        SharedPreferences deviceInfo = getSharedPreferences(Constants.DEVICE_INFO_FILE, Context.MODE_PRIVATE);
        
        /*Comprobaci�n de id de instalaci�n �nico*/
        installationId = install.getString(Constants.INSTALLATION_PREF_NAME, null);
        if (installationId == null){
        	Context context = this.getApplicationContext();
        	installationId = Installation.id(context);
        	editor = install.edit();
        	editor.putString(Constants.INSTALLATION_PREF_NAME, installationId);
        	editor.commit();
        }
        
        //obtencion de datos del dispositivo, se crea un fichero de texto y se guarda su path en preferencias de la app para poder acceder a el
        String devInfoFileName = deviceInfo.getString(Constants.DEVICE_INFO_FILE, null); 
        if (devInfoFileName == null){
        	//crear fichero con datos
        	Context context = this.getApplicationContext();
        	String path = DeviceInfo.createDeviceInfoFile(context);

        	//almacenar nombre de fichero en preferencias
        	editor = deviceInfo.edit();
        	editor.putString(Constants.DEVICE_INFO_FILE, path);
        	editor.commit();
        }

        //prueba de lectura de specs del dispositivo
    	File f = new File(devInfoFileName);
    	Log.d("LoginActivity",DeviceInfo.readDeviceInfoFile(f));

        //examina usuarios almacenados y crea la lista en caso de que existan
        //clear de stored preferences
        editor = accounts.edit();
        editor.clear();
        editor.commit(); 

        //cargamos usuarios almacenados en la DB
        db = new usersDBHandler(this);
        if ((db.getUserCount() == 0)){
            // test data
            db.addUser(new ListUser("user1", "1111","avatar1.png"));
            db.addUser(new ListUser("user2", "2222","avatar2.png"));
            db.addUser(new ListUser("user3", "3333","avatar3.png"));
            db.addUser(new ListUser("user4", "4444","avatar4.png"));
        }
       
        //usando DB
        restoreSavedUsers();
        prepareView();
    }
    
    /*Prepara los elementos del login*/
    private void prepareView(){
		View button = findViewById(R.id.buttonLogin);
		username = (EditText) findViewById(R.id.loginUsername);
		password = (EditText) findViewById(R.id.loginPassword);
		//usuario existente -> hacer login
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	doForm();
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
    	intent.putExtra("username", username.getText().toString());
    	startActivity(intent);
    }
    
    /*Dialog de login incorrecto*/
    private void showAlertDialog(){
    	AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
    	alertDialog.setMessage("Has introducido un usuario o una contrase�a incorrectas");
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

    	ListUser listUser= new ListUser(username.getText().toString(),password.getText().toString(),avatarUrl);
    	//si es la primera vez que se usa este usuario lo guardamos
    	if(isStoredUser(listUser) == (usersDBHandler.INVALID_USER_ID))
			db.addUser(listUser);
    }
    
    
    // busca un usuario en db y devuelve su id si existe 
    private int isStoredUser(ListUser listUser){
    	List<ListUser> userList = new ArrayList<ListUser>();
    	userList = db.getAllUsers();

		for (int i=0; i< userList.size(); i++)
		{
			if (listUser.getUserName().equals(userList.get(i).getUserName()))
			{
				return userList.get(i).getId();
			}
		}
		
		return usersDBHandler.INVALID_USER_ID;	
    }
    

    // carga usuarios desde preferencias de la app
    private void restoreSavedUsers(){
    	List<ListUser> userList = new ArrayList<ListUser>();
    	
    	userList = db.getAllUsers();
    	
        //lista vac�a
		if(userList.isEmpty()){
			hideOldUsers();
		}else{
			prepareListView(userList);
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
    	
    	//al tocar en un usuario de la lista se envia el formulario con sus datos de acceso recordados
    	lv.setOnItemClickListener(new OnItemClickListener() {    
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {     
    			setUserValues((String) ((TextView)view.findViewById(R.id.nameUser)).getText());
    			}  
    		});
    }    
    
    //rellena y env�a el formulario automaticamente
    private void setUserValues(String user){
    	username.setText(user);
    	//obtenemos su pass desde db y lo asignamos para poder loguearle
    	db = new usersDBHandler(this);
    	ListUser listUser = db.getUser(user);
    	password.setText(listUser.getUserPassword());
    	db.close();
    	doForm();
    }
    
   
    
}