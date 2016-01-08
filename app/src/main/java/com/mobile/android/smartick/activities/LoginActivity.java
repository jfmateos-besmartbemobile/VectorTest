package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.network.CheckUserMobileActiveResponse;
import com.mobile.android.smartick.network.LoginStatusResponse;
import com.mobile.android.smartick.network.SmartickAPI;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.pojos.UserType;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.network.NetworkStatus;
import com.mobile.android.smartick.widgets.adapters.UsersListAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

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
    private String resultURL;

    private String usernameToDelete = null;
    private SweetAlertDialog pDialogDelete;

    private AlertDialog endpointSelectAlertDialog;

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

        //Dev menu?
        if (Constants.DEBUG_MODE){
            findViewById(R.id.login_button_dev_menu).setVisibility(View.VISIBLE);
        }

        //login form validation
        EditText studentUsernameEditText = (EditText) findViewById(R.id.login_alias);
        studentUsernameEditText.addTextChangedListener(this);

        EditText studentPasswordEditText = (EditText) findViewById(R.id.login_password);
        studentPasswordEditText.addTextChangedListener(this);

        EditText tutorUsernameEditText = (EditText) findViewById(R.id.login_alias2);
        tutorUsernameEditText.addTextChangedListener(this);

        EditText tutorPasswordEditText = (EditText) findViewById(R.id.login_password2);
        tutorPasswordEditText.addTextChangedListener(this);

        //TextView font setup
        Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");
        ((TextView) findViewById(R.id.login_label_cabecera_tutores)).setTypeface(tfDidact);
        ((Button) findViewById(R.id.login_tutor_button)).setTypeface(tfDidact);
        ((TextView) findViewById(R.id.login_label_cabecera_alumnos)).setTypeface(tfDidact);
        ((Button) findViewById(R.id.login_alumno_button)).setTypeface(tfDidact);

        //loads users into their respective listView
        //STUDENTS
        listViewStudents = (ListView) findViewById(R.id.list_alumnos);
        refreshListViewContent(listViewStudents, localDB.getUsersByType(UserType.ALUMNO), R.layout.student_login_cell);
        prepareListView(listViewStudents);

        //TUTORS
        listViewTutors = (ListView) findViewById(R.id.list_tutores);
        refreshListViewContent(listViewTutors, localDB.getUsersByType(UserType.TUTOR), R.layout.tutor_login_cell);
        prepareListView(listViewTutors);

        //checks for inactive students
        checkStudentsActive();
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
        intent.putExtra("userType", userType.toString());
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

        //login on touch
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //retreive user
                User user = (User) adapter.getItem(position);
                UserType userType = UserType.valueOf(user.getPerfil());
                doLogin(user.getUsername(), user.getPassword(), userType);
            }
        });

        //on long click user is asked for user deletion
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) adapter.getItem(position);
                usernameToDelete = user.getUsername();
                showUserDeleteDialog(user.getUsername());
                return false;
            }
        });
    }

    /** Muestra panel de login */
    public void entrarComoOtroAlumno(View view) {
        setAddAlumnoPanelVisible(true);
    }

    /** Muestra panel de login */
    public void entrarComoOtroTutor(View view) {
        setAddTutorPanelVisible(true);
    }

    public void setAddAlumnoPanelVisible(boolean visible){
        View view = findViewById(R.id.panel_login_alumno);
        if (visible){
            view.setVisibility(View.VISIBLE);
        }else{
            view.setVisibility(View.GONE);
        }
    }

    public void setAddTutorPanelVisible (boolean visible){
        View view = findViewById(R.id.panel_login_tutor);
        if (visible){
            view.setVisibility(View.VISIBLE);
        }else{
            view.setVisibility(View.GONE);
        }
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
            // network error
            showAlertDialog(getString(R.string.Notice),
                    SweetAlertDialog.ERROR_TYPE,
                    getString(R.string.You_must_be_connected_to_the_internet),
                    null, null, getString(R.string.OK), null);
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
                        if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_VALID)) {
                            irMain();
                        } else {
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_INVALID))
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_NO_ACTIVE_SUB))
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.User_does_not_have_an_active_subscription), null, null, getString(R.string.OK), null);
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.PASSWORD_INVALID))
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.Incorrect_password), null, null, getString(R.string.OK), null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        // something went wrong
                        showAlertDialog(getString(R.string.Notice),
                                SweetAlertDialog.ERROR_TYPE,
                                getString(R.string.You_must_be_connected_to_the_internet),
                                null, null, getString(R.string.OK), null);
                    }
                });
    }

    public void checkStudentsActive(){
        //are there any inactive students for this installation id?
        List<User> students = localDB.getUsersByType(UserType.ALUMNO);
        String installationId = sysInfo.getInstallationId();
        for (User u : students){
            SmartickRestClient.get().checkUserMobileActive(
                    u.getUsername(),
                    installationId,
                    new Callback<CheckUserMobileActiveResponse>() {
                        @Override
                        public void success(CheckUserMobileActiveResponse checkUserMobileActiveResponse, Response response) {
                            if (checkUserMobileActiveResponse.getResponse().equals(SmartickAPI.USER_INACTIVE)){
                                //delete user from this installation
                                String user = checkUserMobileActiveResponse.getUser();
                                User userToDelete = localDB.getUser(user);
                                localDB.deleteUser(userToDelete);

                                //refresh student list
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshListViewContent(listViewStudents, localDB.getUsersByType(UserType.ALUMNO), R.layout.student_login_cell);
                                        prepareListView(listViewStudents);
                                    }
                                });
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            // something went wrong
                            showAlertDialog(getString(R.string.Notice),
                                    SweetAlertDialog.ERROR_TYPE,
                                    getString(R.string.You_must_be_connected_to_the_internet),
                                    null, null, getString(R.string.OK), null);
                        }
                    });
        }
    }

    //Add new users
    public void addUser(String username, String password,UserType type){
        Log.d(Constants.LOGIN_LOG_TAG,"adding" + type.toString() +": " + username + " " + password);
        if ((username != null) && (password != null)){
            if (negotiateStoreUsers(username,password,type)){
                Log.d(Constants.LOGIN_LOG_TAG,"new " + type.toString() + " added");

                //refresh listview?
                if (type.equals(UserType.ALUMNO)){
                    refreshListViewContent(listViewStudents, localDB.getUsersByType(UserType.ALUMNO), R.layout.student_login_cell);
                    prepareListView(listViewStudents);
                }
                if (type.equals(UserType.TUTOR)){
                    refreshListViewContent(listViewTutors, localDB.getUsersByType(UserType.TUTOR), R.layout.tutor_login_cell);
                    prepareListView(listViewTutors);
                }

                setAddAlumnoPanelVisible(false);
                setAddTutorPanelVisible(false);
                resetLoginAlumnoPanel();
                resetLoginTutorPanel();


            }else{
                showAlertDialog(getString(R.string.Notice),SweetAlertDialog.WARNING_TYPE,getString(R.string.username_not_valid_or_already_exists),null,null,getString(R.string.OK),null);
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
            showAlertDialog(getString(R.string.Notice),SweetAlertDialog.WARNING_TYPE,getString(R.string.username_not_valid_or_already_exists),null,null,getString(R.string.OK),null);
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
                        if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_VALID)) {
                            addUser(username, password, type);
                            hideSoftKeyboard();
                        } else {
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_INVALID))
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_NO_ACTIVE_SUB))
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.User_does_not_have_an_active_subscription), null, null, getString(R.string.OK), null);
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.PASSWORD_INVALID))
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.Incorrect_password), null, null, getString(R.string.OK), null);
                            resetLoginAlumnoPanel();
                            resetLoginTutorPanel();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        // network error
                        showAlertDialog(getString(R.string.Notice),
                                SweetAlertDialog.ERROR_TYPE,
                                getString(R.string.You_must_be_connected_to_the_internet),
                                null, null, getString(R.string.OK), null);
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

    //Refreshes listView
    private void refreshListViewContent(ListView listView, List<User> userList,int layout){
        UsersListAdapter usersListAdapter = new UsersListAdapter(this, layout, userList);
        listView.setAdapter(usersListAdapter);
    }

    /**
     * Oculta el panel de nuevo alumnos
     * @param view
     */
    public void cancelar(View view) {
        setLoginAlumnoPanelVisible(false);
        setLoginTutorPanelVisible(false);
        hideSoftKeyboard();
    }

    public void checkLoginTutor(View view){
        username = ((EditText)findViewById(R.id.login_alias2)).getText().toString();
        password = ((EditText)findViewById(R.id.login_password2)).getText().toString();
        if (validateTutorLogin(username, password)){
            checkLoginStatusAddNewUser(username, password, UserType.TUTOR);
        }else{
            showAlertDialog(getString(R.string.Notice),SweetAlertDialog.WARNING_TYPE,getString(R.string.username_not_valid_or_already_exists),null,null,getString(R.string.OK),null);
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
    private SweetAlertDialog showAlertDialog(String titleText,int type,String contentText,String cancelButtonText, SweetAlertDialog.OnSweetClickListener cancelListener, String confirmButtonText,SweetAlertDialog.OnSweetClickListener confirmListener){
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

        return alertDialog;
    }

    //Delete Dialog
    private void showUserDeleteDialog(String username){
        String messageContent = String.format(getString(R.string.someon_data_will_be_deleted_from_this_device), username);
        pDialogDelete = showAlertDialog(getString(R.string.Warning), SweetAlertDialog.WARNING_TYPE, messageContent, getString(R.string.Cancel), new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                cancelDeleteUser();
            }
        }, getString(R.string.OK), new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                deleteUser();
            }
        });
    }

    private void deleteUser(){
        if (usernameToDelete != null){
            User user = localDB.getUser(usernameToDelete);
            if (user != null){
                UserType type = UserType.valueOf(user.getPerfil());
                localDB.deleteUser(user);

                //refreshes listview
                if (type.equals(UserType.ALUMNO)){
                    refreshListViewContent(listViewStudents, localDB.getUsersByType(UserType.ALUMNO), R.layout.student_login_cell);
                    prepareListView(listViewStudents);
                }
                if (type.equals(UserType.TUTOR)){
                    refreshListViewContent(listViewTutors, localDB.getUsersByType(UserType.TUTOR), R.layout.tutor_login_cell);
                    prepareListView(listViewTutors);
                }
            }
            usernameToDelete = null;
        }

        if (pDialogDelete != null && pDialogDelete.isShowing()){
            pDialogDelete.dismiss();
        }
    }

    private  void cancelDeleteUser(){
        usernameToDelete = null;
        if (pDialogDelete != null && pDialogDelete.isShowing()){
            pDialogDelete.dismiss();
        }
    }

    //Login validation
    private boolean validateStudentLogin(String username, String password){
        if (username!= null && username.length() >= SmartickAPI.MIN_USERNAME_LENGTH && password!=null && password.length() >= SmartickAPI.MIN_PASSWORD_LENGTH){
            return true;
        }
        return false;
    }

    private boolean validateTutorLogin(String username, String password){
        if (username!= null && username.length() >= SmartickAPI.MIN_USERNAME_LENGTH && isValidEmail(username) && password!=null && password.length() >= SmartickAPI.MIN_PASSWORD_LENGTH){
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

    //Soft keyboard
    private void hideSoftKeyboard(){
        EditText e1 = ((EditText) findViewById(R.id.login_alias));
        EditText e2 = ((EditText) findViewById(R.id.login_password));
        EditText e3 = ((EditText) findViewById(R.id.login_alias2));
        EditText e4 = ((EditText) findViewById(R.id.login_password2));
        InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(e1.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(e2.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(e3.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(e4.getWindowToken(), 0);
    }

    //DEBUG MENU

    /** Volver a la pantalla de inicio */
    public void mostrarDevMenu(View view) {
        showModalDebug();
    }

    public void showModalDebug(){

        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = LoginActivity.this;
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View freemiumWarningView = li.inflate(R.layout.debug_endpoint_select_dialog, null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setView(freemiumWarningView);

                //sets modal content
                Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");
                TextView titleWarning = (TextView) freemiumWarningView.findViewById(R.id.titleEndpoint);
                titleWarning.setTypeface(tfDidact);

                ((Button) freemiumWarningView.findViewById(R.id.debugEndpointPRODButton)).setTypeface(tfDidact);
                ((Button) freemiumWarningView.findViewById(R.id.debugEndpointPREButton)).setTypeface(tfDidact);
                ((Button) freemiumWarningView.findViewById(R.id.debugEndpointDEVButton)).setTypeface(tfDidact);


                endpointSelectAlertDialog = alertBuilder.create();
                endpointSelectAlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                endpointSelectAlertDialog.setCanceledOnTouchOutside(false);

                //shows dialog
                endpointSelectAlertDialog.show();
            }
        });
    }

    public void setEndpointPRODPressed(View view){
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                endpointSelectAlertDialog.dismiss();
                Constants.instance().setUrl_context(Constants.URL_CONTEXT_PROD);
            }
        });
    }

    public void setEndpointPREPressed(View view){
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                endpointSelectAlertDialog.dismiss();
                Constants.instance().setUrl_context(Constants.URL_CONTEXT_PRE);
            }
        });
    }

    public void setEndpointDEVPressed(View view){
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                endpointSelectAlertDialog.dismiss();
                Constants.instance().setUrl_context(Constants.URL_CONTEXT_DEV);
                Log.d(Constants.LOGIN_LOG_TAG,Constants.instance().getUrl_context());
            }
        });
    }

}
