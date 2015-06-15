package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.network.LoginStatusResponse;
import com.mobile.android.smartick.network.SmartickAPI;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.pojos.UserType;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.network.NetworkStatus;
import com.mobile.android.smartick.widgets.adapters.UsersListAdapter;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends Activity implements TextWatcher{

    enum TipoLogin {ALUMNO, TUTOR};

    private TipoLogin tipoLogin = TipoLogin.ALUMNO;

    private SystemInfo sysInfo;

    private ListView listViewStudents;
    private ListView listViewTutors;

    private String username;
    private String password;
    private UserType userType;
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

        //loads users into their respective listView
        //STUDENTS
        listViewStudents = (ListView) findViewById(R.id.list_alumnos);
        UsersListAdapter adapterStudents = new UsersListAdapter(this, R.layout.users_list, localDB.getUsersByType(UserType.ALUMNO));
        listViewStudents.setAdapter(adapterStudents);
        prepareListView(listViewStudents);

        //TUTORS
        listViewTutors = (ListView) findViewById(R.id.list_tutores);
        UsersListAdapter adapterTutors = new UsersListAdapter(this,R.layout.users_list,localDB.getUsersByType(UserType.TUTOR));
        listViewTutors.setAdapter(adapterStudents);
        prepareListView(listViewTutors);
    }


    /** Volver a la pantalla de inicio */
    public void irWelcome(View view) {
        // Cambiamos a la pantalla de inicio
        finish();
    }

    /** Vamos a la pantalla de webview con la aplicacion */
    public void irMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("url", resultURL);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("userType",userType.toString());
        startActivity(intent);
    }

    /** Muestra el panel de login de alumnos */
    public void mostrarLoginAlumno(View view) {

        View layoutLogin = findViewById(R.id.login_layout);
        layoutLogin.setBackground(getResources().getDrawable(R.drawable.login_bg));

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
        layoutLogin.setBackground(getResources().getDrawable(R.drawable.tutor_login_bg));

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
    private void prepareListView(ListView listView){

        final UsersListAdapter adapter = (UsersListAdapter) listView.getAdapter();

        listView.setTextFilterEnabled(true);

        //al tocar en un usuario de la lista se envia el formulario con sus datos de acceso recordados
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos el usuario seleccionado y cargamos sus datos
                User user = (User) adapter.getItem(position);
                UserType userType = UserType.valueOf(user.getPerfil());
                doLogin(user.getUsername(), user.getPassword(), userType);
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
     */
    public void loginAlumno(String username, String password) {
        doLogin(username, password, UserType.ALUMNO);
    }

    /**
     * Comprueba las credenciales
     */
    private void doLogin(String username, String password,UserType userType) {

        if (NetworkStatus.isConnected(this)) {
            checkLoginStatusForLogin(username,password,userType);
        } else {
            // error conexion
            showAlertDialog(getString(R.string.You_must_be_connected_to_the_internet),
                    SweetAlertDialog.ERROR_TYPE,
                    null,
                    null,null,getString(R.string.OK),null);
        }
    }

    private void checkLoginStatusForLogin(String username, String password, UserType userType){
        this.username = username;
        this.password = password;
        this.userType = userType;
        SmartickRestClient.get().getLoginStatus(username,
                password,
                sysInfo.getInstallationId(),
                sysInfo.getDevice(),
                sysInfo.getVersion(),
                sysInfo.getOsVersion(),
                new Callback<LoginStatusResponse>() {
                    @Override
                    public void success(LoginStatusResponse loginStatusResponse, Response response) {
                        Log.d(Constants.LOGIN_LOG_TAG, "check login status - RESPONSE: " + loginStatusResponse.getStatus());
                        if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_VALID)){
                            irMain();
                        }else {
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_INVALID))
                                showAlertDialog(getString(R.string.username_not_valid_or_already_exists), SweetAlertDialog.WARNING_TYPE, null, null, null, getString(R.string.OK), null);
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_NO_ACTIVE_SUB))
                                showAlertDialog(getString(R.string.User_does_not_have_an_active_subscription), SweetAlertDialog.WARNING_TYPE, null, null, null, getString(R.string.OK), null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        // something went wrong
                    }
                });
    }

    //Add new users

    public void addUser(String username, String password,UserType type){
        Log.d(Constants.LOGIN_LOG_TAG,"adding" + type.toString() +": " + username + " " + password);
        if ((username != null) && (password != null)){
            if (negotiateStoreUsers(username,password,type)){
                Log.d(Constants.LOGIN_LOG_TAG,"new " + type.toString() + " added");

                //refresh listview?
                if (type.equals(UserType.ALUMNO)){
                    prepareListView(listViewStudents);
                }
                if (type.equals(UserType.TUTOR)){
                    prepareListView(listViewTutors);
                }

            }else{
                showAlertDialog(getString(R.string.username_not_valid_or_already_exists),SweetAlertDialog.WARNING_TYPE,null,null,null,getString(R.string.OK),null);
                resetLoginAlumnoPanel();
                resetLoginTutorPanel();
            }
        }
    }
    public void checkLoginAlumno(View view){
        username = ((EditText)findViewById(R.id.login_alias)).getText().toString();
        password = ((EditText)findViewById(R.id.login_password)).getText().toString();
        if (validateStudentLogin(username, password)){
            checkLoginStatusAddNewUser(username, password, UserType.ALUMNO);
        }else{
            showAlertDialog(getString(R.string.username_not_valid_or_already_exists),SweetAlertDialog.WARNING_TYPE,null,null,null,getString(R.string.OK),null);
            resetLoginAlumnoPanel();
        }
    }

    private void checkLoginStatusAddNewUser(final String username, final String password,final UserType type){
        SmartickRestClient.get().getLoginStatus(username,
                                                password,
                                                sysInfo.getInstallationId(),
                                                sysInfo.getDevice(),
                                                sysInfo.getVersion(),
                                                sysInfo.getOsVersion(),
                                                new Callback<LoginStatusResponse>() {
            @Override
            public void success(LoginStatusResponse loginStatusResponse, Response response) {
                Log.d(Constants.LOGIN_LOG_TAG, "check login status - RESPONSE: " + loginStatusResponse.getStatus());
                if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_VALID)){
                    addUser(username, password, type);
                }else{
                    if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_INVALID))
                        showAlertDialog(getString(R.string.username_not_valid_or_already_exists), SweetAlertDialog.WARNING_TYPE, null, null, null, getString(R.string.OK), null);
                    if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_NO_ACTIVE_SUB))
                        showAlertDialog(getString(R.string.User_does_not_have_an_active_subscription), SweetAlertDialog.WARNING_TYPE, null, null, null, getString(R.string.OK), null);
                    resetLoginAlumnoPanel();
                    resetLoginTutorPanel();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                // something went wrong
            }
        });
    }

    private void resetLoginAlumnoPanel(){
        ((EditText)findViewById(R.id.login_alias)).setText("");
        ((EditText)findViewById(R.id.login_password)).setText("");
    }

    private void setLoginAlumnoPanelVisible(boolean visible){
        if (visible){
            findViewById(R.id.panel_login_alumno).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.panel_login_alumno).setVisibility(View.GONE);
        }
    }

    /**
     * Oculta el panel de nuevo alumnos
     * @param view
     */
    public void cancelar(View view) {
        setLoginAlumnoPanelVisible(false);
        setLoginTutorPanelVisible(false);
    }

    public void checkLoginTutor(View view){
        username = ((EditText)findViewById(R.id.login_alias2)).getText().toString();
        password = ((EditText)findViewById(R.id.login_password2)).getText().toString();
        if (validateTutorLogin(username, password)){
            checkLoginStatusAddNewUser(username, password, UserType.TUTOR);
        }else{
            showAlertDialog(getString(R.string.username_not_valid_or_already_exists),SweetAlertDialog.WARNING_TYPE,null,null,null,getString(R.string.OK),null);
            resetLoginTutorPanel();
        }
    }

    private void resetLoginTutorPanel(){
        ((EditText)findViewById(R.id.login_alias2)).setText("");
        ((EditText)findViewById(R.id.login_password2)).setText("");
    }

    private void setLoginTutorPanelVisible(boolean visible){
        if (visible){
            findViewById(R.id.panel_login_tutor).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.panel_login_tutor).setVisibility(View.GONE);
        }
    }

    /**
     * Guarda, si no esta ya guardado, un usuario en local
     */
    private boolean negotiateStoreUsers(String username, String password,UserType type) {
        User newUser = new User(username, password, type.toString());
        //si es la primera vez que se usa este usuario lo guardamos
        if(!isStoredUser(newUser)) {
            localDB.addUser(newUser);
            return true;
        }
        return false;
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
    private void showAlertDialog(String titleText,int type,String contentText,String cancelButtonText, SweetAlertDialog.OnSweetClickListener cancelListener, String confirmButtonText,SweetAlertDialog.OnSweetClickListener confirmListener){
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, type);
        if (cancelButtonText != null){
            alertDialog.setCancelText(cancelButtonText);
        }
        if (cancelListener != null){
            alertDialog.setCancelClickListener(cancelListener);
        }
        if (confirmButtonText != null){
            alertDialog.setConfirmText(confirmButtonText);
        }
        if (contentText != null){
            alertDialog.setContentText(contentText);
        }
        if (confirmListener != null){
            alertDialog.setConfirmClickListener(confirmListener);
        }

        alertDialog.setTitleText(titleText);
        alertDialog.show();
    }

    //Login validation
    private boolean validateStudentLogin(String username, String password){
        if (username!= null && username.length() > SmartickAPI.MIN_USERNAME_LENGTH && password!=null && password.length() > SmartickAPI.MIN_PASSWORD_LENGTH){
            return true;
        }
        return false;
    }

    private boolean validateTutorLogin(String username, String password){
        if (username!= null && username.length() > SmartickAPI.MIN_USERNAME_LENGTH && isValidEmail(username) && password!=null && password.length() > SmartickAPI.MIN_PASSWORD_LENGTH){
            return true;
        }
        return false;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
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

    }
}
