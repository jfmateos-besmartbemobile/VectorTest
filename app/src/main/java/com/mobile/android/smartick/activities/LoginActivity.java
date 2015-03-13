package com.mobile.android.smartick.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.pojos.DeviceInfo;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.util.Network;
import com.mobile.android.smartick.util.RedirectHandler;
import com.mobile.android.smartick.widgets.adapters.UsersListAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends ListActivity {

    enum TipoLogin {ALUMNO, TUTOR};

    private TipoLogin tipoLogin = TipoLogin.ALUMNO;

    private SystemInfo sysInfo;

    private String username;
    private String password;
    private String avatarURL;
    private String resultURL;

    private UsersDBHandler localDB = new UsersDBHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializamos systemInfo
        sysInfo = new SystemInfo(this.getApplicationContext());

        // Inicialmente mostramos el login para alumnos
        mostrarLoginAlumno(null);

        // Inicialmente ocultamos el panel de login
        findViewById(R.id.panel_login_alumno).setVisibility(View.GONE);
        findViewById(R.id.panel_login_tutor).setVisibility(View.GONE);

        // TODO: temporal para pruebas!!
        //localDB.deleteAll();

        prepareListView(localDB.getAllUsers());
    }


    /** Volver a la pantalla de inicio */
    public void irWelcome(View view) {
        // Cambiamos a la pantalla de inicio
        startActivity(new Intent(this, WelcomeActivity.class));
    }

    /** Vamos a la pantalla de webview con la aplicacion */
    public void irMain(View view) {
        // Cambiamos a la pantalla de inicio
        // TODO: revisar
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("url", resultURL);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);

        //startActivity(new Intent(this, MainActivity.class));
    }

    /** Muestra el panel de login de alumnos */
    public void mostrarLoginAlumno(View view) {

        View layoutLogin = findViewById(R.id.login_layout);
        layoutLogin.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_bg));

        View panelLoginTutor = findViewById(R.id.login_panel_tutores);
        panelLoginTutor.setVisibility(LinearLayout.GONE);
        View panelLoginAlumno = findViewById(R.id.login_panel_alumnos);
        panelLoginAlumno.setVisibility(LinearLayout.VISIBLE);

        Button botonFlipAlumno = (Button)findViewById(R.id.login_student_flip_button);
        botonFlipAlumno.setVisibility(Button.VISIBLE);
        Button botonFlipTutor = (Button)findViewById(R.id.login_tutor_flip_button);
        botonFlipTutor.setVisibility(Button.GONE);
    }

    /** Muestra el panel de login de tutores */
    public void mostrarLoginTutor(View view) {

        View layoutLogin = findViewById(R.id.login_layout);
        layoutLogin.setBackgroundDrawable(getResources().getDrawable(R.drawable.tutor_login_bg));

        View panelLoginTutor = findViewById(R.id.login_panel_tutores);
        panelLoginTutor.setVisibility(LinearLayout.VISIBLE);
        View panelLoginAlumno = findViewById(R.id.login_panel_alumnos);
        panelLoginAlumno.setVisibility(LinearLayout.GONE);

        Button botonFlipAlumno = (Button)findViewById(R.id.login_student_flip_button);
        botonFlipAlumno.setVisibility(Button.GONE);
        Button botonFlipTutor = (Button)findViewById(R.id.login_tutor_flip_button);
        botonFlipTutor.setVisibility(Button.VISIBLE);
    }

    /**
     * Prepara la listView de usuarios
     */
    private void prepareListView(List<User> usersList){
        setListAdapter(new UsersListAdapter(this, R.layout.users_list, usersList));
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        //al tocar en un usuario de la lista se envia el formulario con sus datos de acceso recordados
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos el usuario seleccionado y cargamos sus datos
                User user = (User) getListAdapter().getItem(position);
                System.out.println("Click en el usuario " + user.getUsername());
                username = user.getUsername();
                password = user.getPassword();
                avatarURL = user.getUrlAvatar();
                loginAlumno();
                //setUserValues((String) ((TextView)view.findViewById(R.id.username)).getText());
            }
        });
    }

    /** Muestra panel de login */
    public void entrarComoOtroAlumno(View view) {

        findViewById(R.id.panel_login_alumno).setVisibility(View.VISIBLE);
    }

    /** Muestra panel de login */
    public void entrarComoOtroTutor(View view) {

        findViewById(R.id.panel_login_tutor).setVisibility(View.VISIBLE);
    }

    /**
     * Comprueba las credenciales del alumno
     * Añade el alumno al listado junto con su avatar
     * @param view vista origen
     */
    public void loginAlumno(View view) {
        // usuario y pass
        username = ((EditText)findViewById(R.id.login_alias)).getText().toString();
        password = ((EditText)findViewById(R.id.login_password)).getText().toString();

        loginAlumno();
    }

    /**
     * Comprueba las credenciales del tutor
     * Añade al tutor al listado de tutores
     * @param view vista origen
     */
    public void loginTutor(View view) {
        // usuario y pass
        username = ((EditText)findViewById(R.id.login_alias)).getText().toString();
        password = ((EditText)findViewById(R.id.login_password)).getText().toString();

        // TODO
        //loginTutor();
    }

    /**
     * Comprueba las credenciales
     */
    private void loginAlumno() {

        // TODO: revisar si tiene sentido aqui la comprobacion de conectividad
        if (Network.isConnected(this)) {
            irMain(null);
        } else {
            // error conexion
            showAlertDialog(R.string.error_conexion);
        }

    }

    /**
     * Oculta el panel de nuevo alumnos
     * @param view
     */
    public void cancelar(View view) {
        findViewById(R.id.panel_login_alumno).setVisibility(View.GONE);
    }

    /*Llamada al login del servidor*/
    private String doHttpPost(String url){

        DefaultHttpClient httpClient = new DefaultHttpClient();
        RedirectHandler handler = new RedirectHandler();
        httpClient.setRedirectHandler(handler);

        HttpPost post = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("j_username", username));
        nvps.add(new BasicNameValuePair("j_password", password));
        // id de dispositivo para la visualizacion adaptada a tablets
        post.addHeader("android-app", sysInfo.getInstallationId());
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
            httpClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        return handler.lastRedirectedUri.toString();
    }

    /**
     * Si el login es correcto se pasa al webview
     */
    private void redirectLogin(String urlRedirect){

        if(!urlRedirect.contains("acceso")) {
            System.out.println("login OK!");

            // Si no tenemos el avatar hay que recuperarlo
            if (avatarURL == null || avatarURL.equals("")) {
                new GetAvatarImageForUser().execute();
            } else {
                // login!!
                irMain(null);
            }
        } else {
            showAlertDialog(R.string.error_usuario);
        }
    }

    /**
     * Guarda, si no esta ya guardado, un usuario en local
     */
    private void negotiateStoreUsers() {
        // Inicialmente no tenemos la url del avatar
        User newUser = new User(username, password, "", "ALUMNO");
        //si es la primera vez que se usa este usuario lo guardamos
        if(!isStoredUser(newUser)) {
            newUser.setUrlAvatar(avatarURL);
            localDB.addUser(newUser);
        }
    }

    /**
     * busca un usuario en db y devuelve su id si existe
      */
    private boolean isStoredUser(User newUser){

        return (localDB.getAllUsers().contains(newUser));

    }

    /**
     * Dialogo de error
     */
    private void showAlertDialog(int msg){
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setMessage(getString(msg));
        // TODO
        //alertDialog.setIcon(android.R.drawable.ic_delete);
        alertDialog.show();
    }

    /**
     * Lanza la tarea de login asincrona
     */
    private class AsyncLogin extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... urls) {
            String urlRedirect = null;
            for(String url : urls) {
                urlRedirect = doHttpPost(url);
            }
            return urlRedirect;
        }

        @Override
        protected void onPostExecute(String urlRedirect) {
            redirectLogin(urlRedirect);
        }
    }

    private class GetAvatarImageForUser extends AsyncTask<Void, Void, User> {

        @Override
        protected User doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                User user = restTemplate.getForObject(Constants.GET_AVATAR_IMAGE_SERVICE, User.class, username);
                return user;
            } catch (Exception e) {
                Log.e("LoginActivity", e.getMessage(), e);
            } catch (Throwable s) {
                Log.e("LoginActivity", s.getMessage(), s);
            }

            return null;
        }

        @Override
        protected void onPostExecute(User user) {

            // Es posible que sea un usuario sin avatar
            if (user != null) {
                avatarURL = user.getUrlAvatar();
            }
            // Tenemos el avatar, guardamos el usuario en local
            negotiateStoreUsers();

            // Damos pasa a la aplicacion
            //System.out.println("----->> webview");
            irMain(null);

        }

    }
}
