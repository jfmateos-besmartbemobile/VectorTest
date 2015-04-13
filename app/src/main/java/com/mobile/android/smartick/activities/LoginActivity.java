package com.mobile.android.smartick.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.mobile.android.smartick.network.LoginStatusResponse;
import com.mobile.android.smartick.network.SmartickAPI;
import com.mobile.android.smartick.network.SmartickRestClient;
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

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends ListActivity implements TextWatcher{

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

        //login form validation
        EditText studentUsernameEditText = (EditText) findViewById(R.id.login_alias);
        studentUsernameEditText.addTextChangedListener(this);

        EditText studentPasswordEditText = (EditText) findViewById(R.id.login_password);
        studentPasswordEditText.addTextChangedListener(this);

        EditText tutorUsernameEditText = (EditText) findViewById(R.id.login_alias2);
        tutorUsernameEditText.addTextChangedListener(this);

        EditText tutorPasswordEditText = (EditText) findViewById(R.id.login_password2);
        tutorPasswordEditText.addTextChangedListener(this);

        // TODO: temporal para pruebas!!
//        localDB.deleteAll();

        prepareListView(localDB.getAllUsers());
    }


    /** Volver a la pantalla de inicio */
    public void irWelcome(View view) {
        // Cambiamos a la pantalla de inicio
        finish();
    }

    /** Vamos a la pantalla de webview con la aplicacion */
    public void irMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("url", resultURL);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);
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
                doLoginAlumno();
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
        doLoginAlumno();
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
    private void doLoginAlumno() {

        // TODO: revisar si tiene sentido aqui la comprobacion de conectividad
        if (Network.isConnected(this)) {
            irMain(null);
        } else {
            // error conexion
            showAlertDialog(R.string.error_conexion);
        }
    }

    private void checkLoginStatus(){
        SmartickRestClient.get().getLoginStatus(username, password, new Callback<LoginStatusResponse>() {
            @Override
            public void success(LoginStatusResponse loginStatusResponse, Response response) {
                loginStatusResponse.getStatus();
            }

            @Override
            public void failure(RetrofitError error) {
                // something went wrong
            }
        });
    }

    public void addAlumno(View view){
        username = ((EditText)findViewById(R.id.login_alias)).getText().toString();
        password = ((EditText)findViewById(R.id.login_password)).getText().toString();
        negotiateStoreUsers();
        findViewById(R.id.panel_login_alumno).setVisibility(View.GONE);
        ((EditText)findViewById(R.id.login_alias)).setText("");
        ((EditText)findViewById(R.id.login_password)).setText("");
    }

    /**
     * Oculta el panel de nuevo alumnos
     * @param view
     */
    public void cancelar(View view) {
        findViewById(R.id.panel_login_alumno).setVisibility(View.GONE);
    }

    /**
     * Guarda, si no esta ya guardado, un usuario en local
     */
    private void negotiateStoreUsers() {
        User newUser = new User(username, password, "ALUMNO");
        //si es la primera vez que se usa este usuario lo guardamos
        if(!isStoredUser(newUser)) {
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

    //Login validation
    private void validateStudentLogin(){
        String username = ((EditText)findViewById(R.id.login_alias)).getText().toString();
        String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
        if (username!= null && username.length() > 0 && password!=null && password.length() > 0){
            //TO DO sends request to validate login
        }
    }

    //TextWatcher methods
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        validateStudentLogin();
    }
}
