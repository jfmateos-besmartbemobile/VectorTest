package com.smartick.activities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.smartick.utils.NetworkUtils;
import com.smartick.utils.UsersListAdapter;

@SuppressLint("NewApi")
public class LoginActivity extends ListActivity {

	private EditText username;
	private EditText password;
	private String urlResult;
	private String avatarUrl;
	Map<String, String> usersMap = new HashMap<String, String>();

	
	private static final String URL_CONTEXT = "http://192.168.1.148/";
	private static final String URL_SMARTICK_LOGIN = URL_CONTEXT+"smartick_login";
	private static final String URL_DEFAULT_AVATAR = "images/avatares/login-default/s_azul_t.png";
	private static final String USERS_FILE = "usersSmk";
	private static final String TOKEN_SEPARATOR = "###";
	private static final String USER_SEPARATOR = "@@@";
	
    
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!NetworkUtils.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))){
        	toOfflineActivity();
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

    private void doForm(){
    	try {
			new LoginSmartick().execute(new URL(URL_SMARTICK_LOGIN));
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
    
    /*Pasa a la actividad principal*/
    private void toMainActivity(){
    	Intent intent = new Intent(this, MainActivity.class);
    	intent.putExtra("url", urlResult);
    	startActivity(intent);
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
			HttpResponse response = httpClient.execute(httpost);
			if(!urlResult.contains("acceso")){
				findAvatarUrl(response.getEntity().getContent());
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void findAvatarUrl(InputStream is) throws IOException {
        String line = "";
        StringBuilder total = new StringBuilder();
        
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        while ((line = rd.readLine()) != null) { 
            total.append(line); 
        }
        String html = total.toString();
        try{
        	avatarUrl = html.substring(html.indexOf("<img id=\"avatarImgId\" class=\"avatarimg\" src=\"")+"<img id=\"avatarImgId\" class=\"avatarimg\" src=\"".length(), html.indexOf("\" alt=\"Avatar\" />"));
        	avatarUrl = avatarUrl.replace("gra", "peq");
        }catch(Exception e){
        	avatarUrl = URL_DEFAULT_AVATAR;
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
		String token = username.getText().toString()+TOKEN_SEPARATOR+password.getText().toString()+TOKEN_SEPARATOR+avatarUrl+USER_SEPARATOR;
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
		FileOutputStream usersFile = openFileOutput(USERS_FILE, Context.MODE_APPEND);
		usersFile.write(token.getBytes());
		usersFile.close();
    }
    
    private String readUsersInStore() throws FileNotFoundException, IOException{
    	FileInputStream usersFile = openFileInput(USERS_FILE);
		StringBuffer fileContent = new StringBuffer("");
		byte[] buffer = new byte[1024];
		int length;
		while ((length = usersFile.read(buffer)) != -1) {
		    fileContent.append(new String(buffer));
		}
		return fileContent.toString();
    }
    
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
		for (String userPassword : fileContent.toString().split(USER_SEPARATOR)) {
			if(!userPassword.isEmpty() && userPassword.contains(TOKEN_SEPARATOR)){
				usersMap.put(userPassword.split(TOKEN_SEPARATOR)[0], userPassword.split(TOKEN_SEPARATOR)[1]);
				ListUser listUser = new ListUser(userPassword.split(TOKEN_SEPARATOR)[0], userPassword.split(TOKEN_SEPARATOR)[1], userPassword.split(TOKEN_SEPARATOR)[2]);
				usersList.add(listUser);
			}
		}
		if(usersList.isEmpty()){
			hideOldUsers();
		}else{
			prepareListView(usersList);
		}
    }
    
    private void hideOldUsers(){
    	TextView tv = (TextView) findViewById(R.id.otherUser);
    	ListView lv = (ListView) findViewById(android.R.id.list);
    	tv.setVisibility(View.GONE);
    	lv.setVisibility(View.GONE);
    }
    
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
    
    private void toOfflineActivity(){
    	Intent intent = new Intent(this, OfflineActivity.class);
    	startActivity(intent);
    }
}