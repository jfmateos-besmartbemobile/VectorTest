package com.mobile.android.smartick.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
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

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.LanguageSelectorDialog;
import com.mobile.android.smartick.UI.LanguageSelectorInterface;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.network.CheckUserMobileActiveResponse;
import com.mobile.android.smartick.network.LoginStatusResponse;
import com.mobile.android.smartick.network.RememberPasswordMobileResponse;
import com.mobile.android.smartick.network.SmartickAPI;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.network.ValidateSocialResponse;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.pojos.UserType;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.network.NetworkStatus;
import com.mobile.android.smartick.util.LocaleHelper;
import com.mobile.android.smartick.widgets.adapters.UsersListAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.squareup.okhttp.Call;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends Activity implements TextWatcher,LanguageSelectorInterface {


    enum TipoLogin {ALUMNO, TUTOR};

    private TipoLogin tipoLogin = TipoLogin.ALUMNO;

    private SystemInfo sysInfo;

    private ListView listViewStudents;
    private ListView listViewTutors;

    private String username;
    private String password;
    private UserType userType;
    private String resultURL;

    private boolean loginStudentShowing = false;
    private boolean loginTutorShowing = false;

    private String usernameToDelete = null;
    private SweetAlertDialog pDialogDelete;

    private AlertDialog endpointSelectAlertDialog;
    private AlertDialog rememberPasswordAlertDialog;

    private EditText rememberPasswordStudentUsername;
    private EditText rememberPasswordTutorMail;

    private Button loginStudentFlipButton;
    private Button loginTutorFlipButton;

    private UsersDBHandler localDB = new UsersDBHandler(this);

    //FB login
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;

    //Google Sign in
    private GoogleApiClient mGoogleApiClient;
    private int RC_SIGN_IN = 600613; //GOOGLE

    //Language selector
    LanguageSelectorDialog selectorDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up selected language
        LocaleHelper.onCreate(this);

        //prepares facebook login button
        //Inits Facebook SDK
        if (!FacebookSdk.isInitialized()){
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        callbackManager = CallbackManager.Factory.create();



        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken == null) {
                    LoginManager.getInstance().logOut();
                }
            }
        };

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d(Constants.LOGIN_LOG_TAG, "success");

                        //query server ofr email info to store it
                        AccessToken at = loginResult.getAccessToken();
                        doLoginFacebook();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d(Constants.LOGIN_LOG_TAG, "cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d(Constants.LOGIN_LOG_TAG, "errror");
                        showAlertDialog(getString(R.string.Notice),
                                SweetAlertDialog.ERROR_TYPE,
                                getString(R.string.Something_went_wrong_try_again_later),
                                null, null, getString(R.string.OK), null);
                    }
                });

        //sets activity layout
        setContentView(R.layout.activity_login);

        //FB Login
        Button loginButton = (Button) findViewById(R.id.fb_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookLoginButtonPressed();
            }
        });

        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        //logs out user if it was already logged
        if (accessToken != null) {
            LoginManager.getInstance().logOut();
        }

        //Google login
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Button signInButton = (Button) findViewById(R.id.google_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        // Inicializamos systemInfo
        sysInfo = new SystemInfo(this.getApplicationContext());

        // Inicialmente mostramos el login para alumnos
        mostrarLoginAlumno(null);

        // Inicialmente ocultamos el panel de login
        findViewById(R.id.panel_login_alumno).setVisibility(View.GONE);
        findViewById(R.id.panel_login_tutor).setVisibility(View.GONE);

        //Dev menu?
        if (Constants.DEBUG_MODE) {
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

        //Flip Buttons setuo
        loginStudentFlipButton = (Button) findViewById(R.id.login_student_flip_button);
        loginTutorFlipButton = (Button) findViewById(R.id.login_tutor_flip_button);

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

    @Override
    protected void onResume() {
        super.onResume();

        loginStudentShowing = false;
        loginTutorShowing = false;
        enableFlipButtons(true);

        // Facebook logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Volver a la pantalla de inicio
     */
    public void irWelcome(View view) {
        // Cambiamos a la pantalla de inicio
        finish();
    }

    /**
     * Vamos a la pantalla de webview con la aplicacion
     */
    public void irMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("url", resultURL);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("userType", userType.toString());
        startActivity(intent);
    }

    /**
     * Vamos a la pantalla de webView con datos sociales de login
     */
    public void irMainSocial(String username, String token, String type) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("userType", UserType.TUTOR.toString());
        intent.putExtra("token", token);
        intent.putExtra("socialType", type);
        startActivity(intent);
    }


    /**
     * Muestra el panel de login de alumnos
     */
    public void mostrarLoginAlumno(View view) {

        if (loginStudentShowing || loginTutorShowing){
            return;
        }

        View layoutLogin = findViewById(R.id.login_layout);
        layoutLogin.setBackground(getResources().getDrawable(R.drawable.login_bg));

        View panelLoginTutor = findViewById(R.id.login_panel_tutores);
        panelLoginTutor.setVisibility(LinearLayout.GONE);
        View panelLoginAlumno = findViewById(R.id.login_panel_alumnos);
        panelLoginAlumno.setVisibility(LinearLayout.VISIBLE);

        Button botonFlipAlumno = (Button) findViewById(R.id.login_student_flip_button);
        botonFlipAlumno.setVisibility(Button.VISIBLE);
        Button botonFlipTutor = (Button) findViewById(R.id.login_tutor_flip_button);
        botonFlipTutor.setVisibility(Button.GONE);
    }

    /**
     * Muestra el panel de login de tutores
     */
    public void mostrarLoginTutor(View view) {

        if (loginStudentShowing || loginTutorShowing){
            return;
        }

        View layoutLogin = findViewById(R.id.login_layout);
        layoutLogin.setBackground(getResources().getDrawable(R.drawable.tutor_login_bg));

        View panelLoginTutor = findViewById(R.id.login_panel_tutores);
        panelLoginTutor.setVisibility(LinearLayout.VISIBLE);
        View panelLoginAlumno = findViewById(R.id.login_panel_alumnos);
        panelLoginAlumno.setVisibility(LinearLayout.GONE);

        Button botonFlipAlumno = (Button) findViewById(R.id.login_student_flip_button);
        botonFlipAlumno.setVisibility(Button.GONE);
        Button botonFlipTutor = (Button) findViewById(R.id.login_tutor_flip_button);
        botonFlipTutor.setVisibility(Button.VISIBLE);
    }

    /**
     * Prepara la listView de usuarios
     */
    private void prepareListView(ListView listView) {

        final UsersListAdapter adapter = (UsersListAdapter) listView.getAdapter();

        listView.setTextFilterEnabled(true);

        //login on touch
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (loginStudentShowing || loginTutorShowing){
                    return;
                }

                //retreive user
                User user = (User) adapter.getItem(position);
                UserType userType = UserType.valueOf(user.getPerfil());
                if (user.getPassword() != null) {
                    doLogin(user.getUsername(), user.getPassword(), userType);
                }
            }
        });

        //on long click user is asked for user deletion
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (loginStudentShowing || loginTutorShowing){
                    return false;
                }

                User user = (User) adapter.getItem(position);
                usernameToDelete = user.getUsername();
                showUserDeleteDialog(user.getUsername());
                return true;
            }
        });
    }

    /**
     * Muestra panel de login
     */
    public void entrarComoOtroAlumno(View view) {

        if (loginStudentShowing || loginTutorShowing){
            return;
        }

        loginStudentShowing = true;
        enableFlipButtons(false);

        setAddAlumnoPanelVisible(true);
    }

    /**
     * Muestra panel de login
     */
    public void entrarComoOtroTutor(View view) {

        if (loginStudentShowing || loginTutorShowing){
            return;
        }

        loginTutorShowing = true;
        enableFlipButtons(false);

        setAddTutorPanelVisible(true);
    }

    public void setAddAlumnoPanelVisible(boolean visible) {
        View view = findViewById(R.id.panel_login_alumno);
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    public void setAddTutorPanelVisible(boolean visible) {
        View view = findViewById(R.id.panel_login_tutor);
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
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
    private void doLogin(String username, String password, UserType userType) {

        if (NetworkStatus.isConnected(this)) {
            checkLoginStatusForLogin(username, password, userType);
        } else {
            // network error
            showAlertDialog(getString(R.string.Notice),
                    SweetAlertDialog.ERROR_TYPE,
                    getString(R.string.You_must_be_connected_to_the_internet),
                    null, null, getString(R.string.OK), null);
        }
    }

    private void doLoginFacebook() {

        final String token = AccessToken.getCurrentAccessToken().getToken();

        Log.d(Constants.LOGIN_LOG_TAG, "Do login facebook for token" + token);
        SmartickRestClient.get().validateSocial(token, "Facebook", new Callback<ValidateSocialResponse>() {
            @Override
            public void success(ValidateSocialResponse validateSocialResponse, Response response) {
                Log.d(Constants.LOGIN_LOG_TAG, "success");
                String email = validateSocialResponse.getEmail();
                if (email != null) {
                    irMainSocial(email, token, "Facebook");
                } else {
                    //No email -> Error
                    showAlertDialog(getString(R.string.Notice),
                            SweetAlertDialog.ERROR_TYPE,
                            getString(R.string.Social_need_email),
                            null, null, getString(R.string.OK), null);

                    LoginManager.getInstance().logOut();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.LOGIN_LOG_TAG, "failure");

                showAlertDialog(getString(R.string.Notice),
                        SweetAlertDialog.ERROR_TYPE,
                        getString(R.string.Something_went_wrong_try_again_later),
                        null, null, getString(R.string.OK), null);

                LoginManager.getInstance().logOut();
            }
        });
    }

    private void doLoginGoogle(String idToken) {

        final String token = idToken;

        Log.d(Constants.LOGIN_LOG_TAG, "Do login google for token" + token);
        SmartickRestClient.get().validateSocial(token, "Google", new Callback<ValidateSocialResponse>() {
            @Override
            public void success(ValidateSocialResponse validateSocialResponse, Response response) {
                Log.d(Constants.LOGIN_LOG_TAG, "success");
                String email = validateSocialResponse.getEmail();
                if (email != null) {
                    irMainSocial(email, token, "Google");
                } else {
                    //No email -> Error
                    showAlertDialog(getString(R.string.Notice),
                            SweetAlertDialog.ERROR_TYPE,
                            getString(R.string.Social_need_email),
                            null, null, getString(R.string.OK), null);

                    LoginManager.getInstance().logOut();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.LOGIN_LOG_TAG, "failure");

                showAlertDialog(getString(R.string.Notice),
                        SweetAlertDialog.ERROR_TYPE,
                        getString(R.string.Something_went_wrong_try_again_later),
                        null, null, getString(R.string.OK), null);

                LoginManager.getInstance().logOut();
            }
        });
    }

    private void checkLoginStatusForLogin(String username, String password, UserType userType) {
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

    public void checkStudentsActive() {
        //are there any inactive students for this installation id?
        List<User> students = localDB.getUsersByType(UserType.ALUMNO);
        String installationId = sysInfo.getInstallationId();
        for (User u : students) {
            SmartickRestClient.get().checkUserMobileActive(
                    u.getUsername(),
                    installationId,
                    new Callback<CheckUserMobileActiveResponse>() {
                        @Override
                        public void success(CheckUserMobileActiveResponse checkUserMobileActiveResponse, Response response) {
                            if (checkUserMobileActiveResponse.getResponse().equals(SmartickAPI.USER_INACTIVE)) {
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
    public void addUser(String username, String password, UserType type) {
        Log.d(Constants.LOGIN_LOG_TAG, "adding" + type.toString() + ": " + username + " " + password);
        if ((username != null) && (password != null)) {
            if (negotiateStoreUsers(username, password, type)) {
                Log.d(Constants.LOGIN_LOG_TAG, "new " + type.toString() + " added");

                //refresh listview?
                if (type.equals(UserType.ALUMNO)) {
                    refreshListViewContent(listViewStudents, localDB.getUsersByType(UserType.ALUMNO), R.layout.student_login_cell);
                    prepareListView(listViewStudents);
                }
                if (type.equals(UserType.TUTOR)) {
                    refreshListViewContent(listViewTutors, localDB.getUsersByType(UserType.TUTOR), R.layout.tutor_login_cell);
                    prepareListView(listViewTutors);
                }

                setAddAlumnoPanelVisible(false);
                setAddTutorPanelVisible(false);
                resetLoginAlumnoPanel();
                resetLoginTutorPanel();


            } else {
                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
                resetLoginAlumnoPanel();
                resetLoginTutorPanel();
            }
        }

    }

    public void checkLoginAlumno(View view) {
        username = ((EditText) findViewById(R.id.login_alias)).getText().toString();
        password = ((EditText) findViewById(R.id.login_password)).getText().toString();
        if (validateStudentLogin(username, password)) {
            checkLoginStatusAddNewUser(username, password, UserType.ALUMNO);
        } else {
            showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
            resetLoginAlumnoPanel();
        }
    }

    private void checkLoginStatusAddNewUser(final String username, final String password, final UserType type) {
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
                            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_INVALID)){
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
                                resetLoginAlumnoPanel();
                                resetLoginTutorPanel();
                            }

                            if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_NO_ACTIVE_SUB)){
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.User_does_not_have_an_active_subscription), null, null, getString(R.string.OK), null);
                                resetLoginAlumnoPanel();
                                resetLoginTutorPanel();
                            }

                            if (loginStatusResponse.getStatus().equals(SmartickAPI.PASSWORD_INVALID)){
                                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.Incorrect_password), null, null, getString(R.string.OK), null);

                                //just resets the password
                                ((EditText) findViewById(R.id.login_password)).setText("");
                                ((EditText) findViewById(R.id.login_password2)).setText("");
                            }
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

    private void resetLoginAlumnoPanel() {
        ((EditText) findViewById(R.id.login_alias)).setText("");
        ((EditText) findViewById(R.id.login_password)).setText("");
    }

    private void setLoginAlumnoPanelVisible(boolean visible) {
        if (visible) {
            loginStudentShowing = true;
            enableFlipButtons(false);
            findViewById(R.id.panel_login_alumno).setVisibility(View.VISIBLE);
        } else {
            loginStudentShowing = false;
            enableFlipButtons(true);
            findViewById(R.id.panel_login_alumno).setVisibility(View.GONE);
        }
    }

    //Refreshes listView
    private void refreshListViewContent(ListView listView, List<User> userList, int layout) {
        UsersListAdapter usersListAdapter = new UsersListAdapter(this, layout, userList);
        listView.setAdapter(usersListAdapter);
    }

    /**
     * Oculta el panel de nuevo alumnos
     *
     * @param view
     */
    public void cancelar(View view) {
        setLoginAlumnoPanelVisible(false);
        setLoginTutorPanelVisible(false);
        loginTutorShowing = false;
        loginStudentShowing = false;
        enableFlipButtons(true);
        hideSoftKeyboard();
    }

    public void checkLoginTutor(View view) {
        username = ((EditText) findViewById(R.id.login_alias2)).getText().toString();
        password = ((EditText) findViewById(R.id.login_password2)).getText().toString();
        if (validateTutorLogin(username, password)) {
            checkLoginStatusAddNewUser(username, password, UserType.TUTOR);
        } else {
            showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
            resetLoginTutorPanel();
        }
    }

    private void resetLoginTutorPanel() {
        ((EditText) findViewById(R.id.login_alias2)).setText("");
        ((EditText) findViewById(R.id.login_password2)).setText("");
    }

    private void setLoginTutorPanelVisible(boolean visible) {
        if (visible) {
            findViewById(R.id.panel_login_tutor).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.panel_login_tutor).setVisibility(View.GONE);
        }
    }

    /**
     * Guarda, si no esta ya guardado, un usuario en local
     */
    private boolean negotiateStoreUsers(String username, String password, UserType type) {
        User newUser = new User(username, password, type.toString());
        //si es la primera vez que se usa este usuario lo guardamos
        if (!isStoredUser(newUser)) {
            localDB.addUser(newUser);
            return true;
        }
        return false;
    }

    /**
     * busca un usuario en db y devuelve su id si existe
     */
    private boolean isStoredUser(User newUser) {
        return (localDB.getAllUsers().contains(newUser));
    }

    /**
     * Dialogo de error
     */
    private SweetAlertDialog showAlertDialog(String titleText, int type, String contentText, String cancelButtonText, SweetAlertDialog.OnSweetClickListener cancelListener, String confirmButtonText, SweetAlertDialog.OnSweetClickListener confirmListener) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, type);
        if (cancelButtonText != null) {
            alertDialog.setCancelText(cancelButtonText);
        }
        if (cancelListener != null) {
            alertDialog.setCancelClickListener(cancelListener);
        }
        if (confirmButtonText != null) {
            alertDialog.setConfirmText(confirmButtonText);
        }
        if (contentText != null) {
            alertDialog.setContentText(contentText);
        }
        if (confirmListener != null) {
            alertDialog.setConfirmClickListener(confirmListener);
        }

        alertDialog.setTitleText(titleText);
        alertDialog.show();

        return alertDialog;
    }

    //Delete Dialog
    private void showUserDeleteDialog(String username) {
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

    private void deleteUser() {
        if (usernameToDelete != null) {
            User user = localDB.getUser(usernameToDelete);
            if (user != null) {
                UserType type = UserType.valueOf(user.getPerfil());
                localDB.deleteUser(user);

                //refreshes listview
                if (type.equals(UserType.ALUMNO)) {
                    refreshListViewContent(listViewStudents, localDB.getUsersByType(UserType.ALUMNO), R.layout.student_login_cell);
                    prepareListView(listViewStudents);
                }
                if (type.equals(UserType.TUTOR)) {
                    refreshListViewContent(listViewTutors, localDB.getUsersByType(UserType.TUTOR), R.layout.tutor_login_cell);
                    prepareListView(listViewTutors);
                }
            }
            usernameToDelete = null;
        }

        if (pDialogDelete != null && pDialogDelete.isShowing()) {
            pDialogDelete.dismiss();
        }
    }

    private void cancelDeleteUser() {
        usernameToDelete = null;
        if (pDialogDelete != null && pDialogDelete.isShowing()) {
            pDialogDelete.dismiss();
        }
    }

    //Login validation
    private boolean validateStudentLogin(String username, String password) {
        if (username != null && username.length() >= SmartickAPI.MIN_USERNAME_LENGTH && password != null && password.length() >= SmartickAPI.MIN_PASSWORD_LENGTH) {
            return true;
        }
        return false;
    }

    private boolean validateTutorLogin(String username, String password) {
        if (username != null && username.length() >= SmartickAPI.MIN_USERNAME_LENGTH && isValidEmail(username) && password != null && password.length() >= SmartickAPI.MIN_PASSWORD_LENGTH) {
            return true;
        }
        return false;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
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
    private void hideSoftKeyboard() {
        EditText e1 = ((EditText) findViewById(R.id.login_alias));
        EditText e2 = ((EditText) findViewById(R.id.login_password));
        EditText e3 = ((EditText) findViewById(R.id.login_alias2));
        EditText e4 = ((EditText) findViewById(R.id.login_password2));
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(e1.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(e2.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(e3.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(e4.getWindowToken(), 0);
    }

    //DEBUG MENU

    /**
     * Volver a la pantalla de inicio
     */
    public void mostrarDevMenu(View view) {
        showModalDebug();
    }

    public void showModalDebug() {

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

    public void setEndpointPRODPressed(View view) {
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                endpointSelectAlertDialog.dismiss();
                Constants.instance().setUrl_context(Constants.URL_CONTEXT_PROD);
                SmartickRestClient.reset();
            }
        });
    }

    public void setEndpointPREPressed(View view) {
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                endpointSelectAlertDialog.dismiss();
                Constants.instance().setUrl_context(Constants.URL_CONTEXT_PRE);
                SmartickRestClient.reset();
            }
        });
    }

    public void setEndpointDEVPressed(View view) {
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                endpointSelectAlertDialog.dismiss();
                Constants.instance().setUrl_context(Constants.URL_CONTEXT_DEV);
                SmartickRestClient.reset();
            }
        });
    }

    //Facebook login button pressed
    public void facebookLoginButtonPressed(){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    //Google sign in button pressed
    public void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(Constants.LOGIN_LOG_TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String idToken = acct.getIdToken();
            Log.d(Constants.LOGIN_LOG_TAG, "handleSignInResult: sign in with idToken= " + acct.getIdToken());
            if (idToken != null){
                doLoginGoogle(idToken);
            }else{
                showAlertDialog(getString(R.string.Notice),
                        SweetAlertDialog.ERROR_TYPE,
                        getString(R.string.Something_went_wrong_try_again_later),
                        null, null, getString(R.string.OK), null);
            }

        } else {
            // Signed out, show unauthenticated UI.
            Log.d(Constants.LOGIN_LOG_TAG, "handleSignInResult: sign out");
        }
    }

    //Language selector
    public void showLanguageSelector(View view) {
        selectorDialog = new LanguageSelectorDialog();
        FragmentManager fm = getFragmentManager();
        selectorDialog.show(fm, "Language selector");
    }

    @Override
    public void languageChanged() {
        selectorDialog.dismiss();
        this.recreate();
    }



    //Remember password
    public void mostrarRecordarPasswordAlumno(View view){
        mostrarRecordarPasswordModal(true);
    }

    public void mostrarRecordarPasswordTutor(View view){
        mostrarRecordarPasswordModal(false);
    }

    public void mostrarRecordarPasswordModal(final boolean withStudent){
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = LoginActivity.this;
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rememberPasswordView = li.inflate(R.layout.remember_password_modal, null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setView(rememberPasswordView);

                //sets modal content
                Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");
                TextView titleWarning = (TextView) rememberPasswordView.findViewById(R.id.titleRemember);
                titleWarning.setTypeface(tfDidact);


                rememberPasswordStudentUsername = ((EditText) rememberPasswordView.findViewById(R.id.editTextRemenberStudentUsername));
                rememberPasswordTutorMail = ((EditText) rememberPasswordView.findViewById(R.id.editTextRemenberTutorMail));

                if (withStudent){
                    rememberPasswordStudentUsername.setVisibility(View.VISIBLE);
                }

                rememberPasswordAlertDialog = alertBuilder.create();
                rememberPasswordAlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rememberPasswordAlertDialog.setCanceledOnTouchOutside(true);

                //shows dialog
                rememberPasswordAlertDialog.show();
            }
        });
    }

    public void doRememberPassword(View view){
        if (rememberPasswordStudentUsername == null || rememberPasswordTutorMail == null){
            return;
        }

        String studentUsername = rememberPasswordStudentUsername.getText().toString();
        String tutorMail = rememberPasswordTutorMail.getText().toString();

        SmartickRestClient.get().rememberPassword(studentUsername, tutorMail, new Callback<RememberPasswordMobileResponse>() {
            @Override
            public void success(RememberPasswordMobileResponse rememberPasswordMobileResponse, Response response) {

                Log.d(Constants.LOGIN_LOG_TAG,rememberPasswordMobileResponse.toString());

                resetAndHidePasswordModal();

                if (rememberPasswordMobileResponse.getResponse().equals(SmartickAPI.MAIL_SENT_OK)){
                    showAlertDialog(getString(R.string.Notice),
                            SweetAlertDialog.SUCCESS_TYPE,
                            getString(R.string.RememberSuccess),
                            null, null, getString(R.string.OK), null);
                }else if (rememberPasswordMobileResponse.getResponse().equals(SmartickAPI.UNKNOWN_TUTOR)
                        || rememberPasswordMobileResponse.getResponse().equals(SmartickAPI.TUTOR_MISSING)
                        || rememberPasswordMobileResponse.getResponse().equals(SmartickAPI.TUTOR_UNRELATED)
                        ){
                    showAlertDialog(getString(R.string.Notice),
                            SweetAlertDialog.ERROR_TYPE,
                            getString(R.string.RememberFailTutor),
                            null, null, getString(R.string.OK), null);
                }else{
                    showAlertDialog(getString(R.string.Notice),
                            SweetAlertDialog.ERROR_TYPE,
                            getString(R.string.RememberFailUser),
                            null, null, getString(R.string.OK), null);
                }
            }

            @Override
            public void failure(RetrofitError error) {

                Log.d(Constants.LOGIN_LOG_TAG,error.toString());

                resetAndHidePasswordModal();

                showAlertDialog(getString(R.string.Notice),
                        SweetAlertDialog.ERROR_TYPE,
                        getString(R.string.Something_went_wrong_try_again_later),
                        null, null, getString(R.string.OK), null);
            }
        });
    }

    public void cancelRememberPassword(View view){
        resetAndHidePasswordModal();
    }

    public void resetAndHidePasswordModal(){
        rememberPasswordAlertDialog.dismiss();
        rememberPasswordStudentUsername = null;
        rememberPasswordTutorMail = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Facebook trackers
        accessTokenTracker.stopTracking();
    }

    public void enableFlipButtons(boolean enabled){
        loginStudentFlipButton.setEnabled(enabled);
        loginTutorFlipButton.setEnabled(enabled);
    }
}
