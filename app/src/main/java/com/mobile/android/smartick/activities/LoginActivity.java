package com.mobile.android.smartick.activities;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mobile.android.smartick.R;
import com.mobile.android.smartick.UI.LanguageSelectorDialog;
import com.mobile.android.smartick.UI.LanguageSelectorInterface;
import com.mobile.android.smartick.customviews.LeftImageButton;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.network.CheckUserMobileActiveResponse;
import com.mobile.android.smartick.network.LoginStatusResponse;
import com.mobile.android.smartick.network.NetworkStatus;
import com.mobile.android.smartick.network.RegisterAppEventResponse;
import com.mobile.android.smartick.network.RememberPasswordMobileResponse;
import com.mobile.android.smartick.network.SmartickAPI;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.network.ValidateSocialResponse;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.pojos.UserType;
import com.mobile.android.smartick.util.AudioPlayer;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.util.FlipAnimation;
import com.mobile.android.smartick.util.LocaleHelper;
import com.mobile.android.smartick.widgets.adapters.UsersListAdapter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity implements LanguageSelectorInterface {

  enum TipoLogin {ALUMNO, TUTOR}

  private TipoLogin tipoLogin = TipoLogin.ALUMNO;
  private final String FONT = "fonts/DidactGothic.ttf";

  private SystemInfo sysInfo;

  private ListView listViewStudents;
  private ListView listViewTutors;

  private String username;
  private String password;
  private UserType userType;
  private String resultURL;

  private boolean loginStudentShowing = false;
  private boolean mostrarAddStudentOptions = true;

  private String usernameToDelete = null;
  private SweetAlertDialog pDialogDelete;

  private AlertDialog endpointSelectAlertDialog;
  private AlertDialog rememberPasswordAlertDialog;

  private EditText rememberPasswordStudentUsername;
  private EditText rememberPasswordTutorMail;

  private LeftImageButton loginStudentFlipButton;
  private LeftImageButton loginTutorFlipButton;
  private LeftImageButton activeTutor;
  private LeftImageButton changeTutor;
  private Button otherTutor;

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
    if (!FacebookSdk.isInitialized()) {
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

    // Inicializamos systemInfo
    sysInfo = new SystemInfo(this.getApplicationContext());


    View layoutLogin = findViewById(R.id.login_layout);
    layoutLogin.setBackground(getResources().getDrawable(R.drawable.login_bg));
    //Dev menu?
    if (Constants.DEBUG_MODE) {
      findViewById(R.id.login_button_dev_menu).setVisibility(View.VISIBLE);
    }

    //TextView font setup
    Typeface tfDidact = Typeface.createFromAsset(getAssets(), FONT);
    ((TextView) findViewById(R.id.login_label_cabecera_alumnos)).setTypeface(tfDidact);
    ((Button) findViewById(R.id.login_alumno_button)).setTypeface(tfDidact);
    otherTutor = ((Button) findViewById(R.id.other_tutor_button));
    otherTutor.setTypeface(tfDidact);

    changeTutor = (LeftImageButton) findViewById(R.id.change_tutor_button);
    activeTutor = (LeftImageButton) findViewById(R.id.tutor_active);

    //Flip Buttons setuo
    loginStudentFlipButton = (LeftImageButton) findViewById(R.id.login_student_flip_button);
    loginTutorFlipButton = (LeftImageButton) findViewById(R.id.login_tutor_flip_button);

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

    setUpOnFocusListeners();
  }

  @Override
  protected void onResume() {
    super.onResume();

    loginStudentShowing = false;
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

    if (loginStudentShowing) return;

    if (listViewStudents.getAdapter().getCount() > 0)
      showStudentLogin(false);

    flipCard();

    View layoutLogin = findViewById(R.id.login_layout);
    layoutLogin.setBackground(getResources().getDrawable(R.drawable.login_bg));
  }

  /**
   * Muestra el panel de login de tutores
   */
  public void mostrarLoginTutor(View view) {

    if (loginStudentShowing) return;

    if (listViewTutors.getAdapter().getCount() > 0)
      showTutorLogin(false);
    flipCard();

    View layoutLogin = findViewById(R.id.login_layout);
    layoutLogin.setBackground(getResources().getDrawable(R.drawable.tutor_login_bg));
  }

  private void flipCard() {
    View rootLayout = findViewById(R.id.main_activity_root);
    View cardFace = findViewById(R.id.card_login_panel_alumnos);
    View cardBack = findViewById(R.id.card_login_panel_tutores);
    View cardFaceShadow = findViewById(R.id.login_panel_alumnos_shadow);
    View cardBackShadow = findViewById(R.id.login_panel_tutores_shadow);

    FlipAnimation flipAnimation = new FlipAnimation(cardFace, cardBack, cardFaceShadow, cardBackShadow);

    if (cardFace.getVisibility() == View.GONE)
      flipAnimation.reverse();

    flipAnimation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
        loginTutorFlipButton.setEnabled(false);
        loginStudentFlipButton.setEnabled(false);
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        loginTutorFlipButton.setEnabled(true);
        loginStudentFlipButton.setEnabled(true);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });

    rootLayout.startAnimation(flipAnimation);
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

        if (loginStudentShowing) {
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

        if (loginStudentShowing) {
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


  public void addAnotherStudent(View view) {

    if (loginStudentShowing) return;

    showHideAddStudentOptions();
  }

  private void showHideAddStudentOptions() {
    AnimationSet set = new AnimationSet(true);
    Animation animation;
    Animation animationAlpha;
    if (mostrarAddStudentOptions) {
      animation = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 1.0f,
          Animation.RELATIVE_TO_SELF, 0.0f);

      animationAlpha = new AlphaAnimation(0, 1);
      findViewById(R.id.login_alumno_button_selected).setVisibility(View.VISIBLE);
      findViewById(R.id.login_alumno_button).setVisibility(View.INVISIBLE);
    } else {
      animation = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 1.0f);
      animationAlpha = new AlphaAnimation(1, 0);
      findViewById(R.id.login_alumno_button_selected).setVisibility(View.INVISIBLE);
      findViewById(R.id.login_alumno_button).setVisibility(View.VISIBLE);
    }
    animation.setDuration(500);
    animationAlpha.setDuration(500);
//    set.addAnimation(animation);
    set.addAnimation(animationAlpha);
    LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);

    LinearLayout llAnimado = (LinearLayout) findViewById(R.id.ll_add_students);
    llAnimado.setLayoutAnimation(controller);
//    llAnimado.startAnimation(animation);
    llAnimado.startAnimation(animationAlpha);

    llAnimado.setVisibility(mostrarAddStudentOptions ? View.VISIBLE : View.GONE);
    mostrarAddStudentOptions = !mostrarAddStudentOptions;
  }

  public void cambiarTutor(View view) {

    if (loginStudentShowing) return;

    listViewTutors.setVisibility(View.VISIBLE);
    changeTutor.setVisibility(View.GONE);
    otherTutor.setVisibility(View.VISIBLE);
    activeTutor.setVisibility(View.GONE);
  }

  public void showActiveTutor() {

    if (loginStudentShowing) return;

    listViewTutors.setVisibility(View.GONE);
    changeTutor.setVisibility(View.VISIBLE);
    otherTutor.setVisibility(View.GONE);
    activeTutor.setVisibility(View.VISIBLE);
  }

  public void entrarComoOtroTutor(View view) {

    if (loginStudentShowing) return;

    showTutorLogin(true);
  }

  public void showTutorLogin(boolean visible) {
    View loginTutor = findViewById(R.id.tutor_login);

    View changeTutorButton = findViewById(R.id.change_tutor_button);
    View otherTutorButton = findViewById(R.id.other_tutor_button);

    if (visible) {
      loginTutor.setVisibility(View.VISIBLE);
      activeTutor.setVisibility(View.GONE);
      changeTutorButton.setVisibility(View.GONE);
      listViewTutors.setVisibility(View.GONE);
      otherTutorButton.setVisibility(View.GONE);
    } else {
      loginTutor.setVisibility(View.GONE);
      activeTutor.setVisibility(View.VISIBLE);
      changeTutorButton.setVisibility(View.VISIBLE);
      listViewTutors.setVisibility(View.VISIBLE);
      otherTutorButton.setVisibility(View.VISIBLE);
      showActiveTutor();
    }
  }

  public void showStudentLogin(boolean visible) {
    View loginTutor = findViewById(R.id.student_login);
    View otherStudentButton = findViewById(R.id.login_alumno_button);

    if (visible) {
      loginTutor.setVisibility(View.VISIBLE);
      listViewStudents.setVisibility(View.GONE);
      otherStudentButton.setVisibility(View.GONE);
    } else {
      loginTutor.setVisibility(View.GONE);
      listViewStudents.setVisibility(View.VISIBLE);
      otherStudentButton.setVisibility(View.VISIBLE);
    }
  }


  private void setUpOnFocusListeners() {

    findViewById(R.id.student_mail_edittext).setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        findViewById(R.id.student_mail_icon).setSelected(hasFocus);
      }
    });

    findViewById(R.id.student_password_edittext).setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        findViewById(R.id.student_password_icon).setSelected(hasFocus);
      }
    });

    findViewById(R.id.tutor_mail_edittext).setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        findViewById(R.id.tutor_mail_icon).setSelected(hasFocus);
      }
    });

    findViewById(R.id.tutor_password_edittext).setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        findViewById(R.id.tutor_password_icon).setSelected(hasFocus);
      }
    });
  }


  public void showActiveTutor(View view) {
    showTutorLogin(false);
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
//                            showAlertDialog(getString(R.string.Notice),
//                                    SweetAlertDialog.ERROR_TYPE,
//                                    getString(R.string.You_must_be_connected_to_the_internet),
//                                    null, null, getString(R.string.OK), null);
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
          showStudentLogin(false);
          refreshListViewContent(listViewStudents, localDB.getUsersByType(UserType.ALUMNO), R.layout.student_login_cell);
          prepareListView(listViewStudents);
        }
        if (type.equals(UserType.TUTOR)) {
          showTutorLogin(false);
          refreshListViewContent(listViewTutors, localDB.getUsersByType(UserType.TUTOR), R.layout.tutor_login_cell);
          prepareListView(listViewTutors);
        }
        loginStudentShowing = false;

      } else {
        showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
      }
    }

  }

  public void checkLoginAlumno(View view) {
    username = ((EditText) findViewById(R.id.student_mail_edittext)).getText().toString();
    password = ((EditText) findViewById(R.id.student_password_edittext)).getText().toString();
    if (validateStudentLogin(username, password)) {
      checkLoginStatusAddNewUser(username, password, UserType.ALUMNO);
      ((EditText) findViewById(R.id.student_mail_edittext)).setText("");
      ((EditText) findViewById(R.id.student_password_edittext)).setText("");
    } else {
      showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
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
            } else {
              if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_INVALID)) {
                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
              }

              if (loginStatusResponse.getStatus().equals(SmartickAPI.LOGIN_NO_ACTIVE_SUB)) {
                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.User_does_not_have_an_active_subscription), null, null, getString(R.string.OK), null);
              }

              if (loginStatusResponse.getStatus().equals(SmartickAPI.PASSWORD_INVALID)) {
                showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.Incorrect_password), null, null, getString(R.string.OK), null);

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

  //Refreshes listView
  private void refreshListViewContent(ListView listView, List<User> userList, int layout) {
    if (listView.getId() == R.id.list_tutores) {
      showTutorLogin(userList.isEmpty());
    }
    if (listView.getId() == R.id.list_alumnos) {
      showStudentLogin(userList.isEmpty());
    }
    UsersListAdapter usersListAdapter = new UsersListAdapter(this, layout, userList);
    listView.setAdapter(usersListAdapter);

    if (!userList.isEmpty()) {
      //TODO Decidir cual es el tutor activo
      activeTutor.setLabel(userList.get(0).getUsername());
    }
  }

  public void checkLoginTutor(View view) {
    username = ((EditText) findViewById(R.id.tutor_mail_edittext)).getText().toString();
    password = ((EditText) findViewById(R.id.tutor_password_edittext)).getText().toString();

    if (validateTutorLogin(username, password)) {
      checkLoginStatusAddNewUser(username, password, UserType.TUTOR);
      ((EditText) findViewById(R.id.tutor_mail_edittext)).setText("");
      ((EditText) findViewById(R.id.tutor_password_edittext)).setText("");
    } else {
      showAlertDialog(getString(R.string.Notice), SweetAlertDialog.WARNING_TYPE, getString(R.string.username_not_valid_or_already_exists), null, null, getString(R.string.OK), null);
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

        //Send event to server
        SmartickRestClient.get().registerAppEvent("Android", sysInfo.getVersion(), sysInfo.getDevice(), sysInfo.getInstallationId(), "USER_DELETED", usernameToDelete,
            new Callback<RegisterAppEventResponse>() {
              @Override
              public void success(RegisterAppEventResponse registerAppEventResponse, Response response) {
                Log.d(Constants.LOGIN_LOG_TAG, "registerAppEvent RESPONSE: " + registerAppEventResponse.getResponse());
              }

              @Override
              public void failure(RetrofitError error) {
                Log.d(Constants.LOGIN_LOG_TAG, "registerAppEvent ERROR: " + error);
              }
            });

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
      String expression = "^[\\_a-zA-Z0-9-\\.-]+@([\\_a-zA-Z0-9-\\-]+\\.)+[A-Z]{2,4}$";
      Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(target);
      return matcher.matches();
    }
  }


  public void addNewStudent(View view) {
    showHideAddStudentOptions();
    startActivity(new Intent(this, RegistroActivity.class));
  }

  public void addExistingStudent(View view) {
    showHideAddStudentOptions();
    showStudentLogin(true);
  }

  public void addAllMyStudents(View view) {

  }

  public void goToRegister(View view) {
    startActivity(new Intent(this, RegistroActivity.class));
  }

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
        Typeface tfDidact = Typeface.createFromAsset(getAssets(), FONT);
        TextView titleWarning = (TextView) freemiumWarningView.findViewById(R.id.titleEndpoint);
        titleWarning.setTypeface(tfDidact);

        ((Button) freemiumWarningView.findViewById(R.id.debugEndpointPRODButton)).setTypeface(tfDidact);
        ((Button) freemiumWarningView.findViewById(R.id.debugEndpointPREButton)).setTypeface(tfDidact);
        ((Button) freemiumWarningView.findViewById(R.id.debugEndpointDEVButton)).setTypeface(tfDidact);
        ((Button) freemiumWarningView.findViewById(R.id.debugClearAudioCacheButton)).setTypeface(tfDidact);


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

  public void clearAudioCachePressed(View view) {
    LoginActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        endpointSelectAlertDialog.dismiss();
      }
    });

    AudioPlayer audioPlayer = new AudioPlayer();
    audioPlayer.init(this);
    audioPlayer.clearAudioCache();
  }

  //Facebook login button pressed
  public void facebookLoginButtonPressed(View v) {
    LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
  }

  //Google sign in button pressed
  public void googleSignIn(View v) {
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
      if (idToken != null) {
        doLoginGoogle(idToken);
      } else {
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
  public void mostrarRecordarPasswordAlumno(View view) {
    mostrarRecordarPasswordModal(true);
  }

  public void mostrarRecordarPasswordTutor(View view) {
    mostrarRecordarPasswordModal(false);
  }

  public void mostrarRecordarPasswordModal(final boolean withStudent) {
    LoginActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Context context = LoginActivity.this;
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rememberPasswordView = li.inflate(R.layout.remember_password_modal, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setView(rememberPasswordView);

        //sets modal content
        Typeface tfDidact = Typeface.createFromAsset(getAssets(), FONT);
        TextView titleWarning = (TextView) rememberPasswordView.findViewById(R.id.titleRemember);
        titleWarning.setTypeface(tfDidact);


        rememberPasswordStudentUsername = ((EditText) rememberPasswordView.findViewById(R.id.editTextRemenberStudentUsername));
        rememberPasswordTutorMail = ((EditText) rememberPasswordView.findViewById(R.id.editTextRemenberTutorMail));

        if (withStudent) {
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

  public void doRememberPassword(View view) {
    if (rememberPasswordStudentUsername == null || rememberPasswordTutorMail == null) return;

    String studentUsername = rememberPasswordStudentUsername.getText().toString();
    String tutorMail = rememberPasswordTutorMail.getText().toString();

    SmartickRestClient.get().rememberPassword(studentUsername, tutorMail, new Callback<RememberPasswordMobileResponse>() {
      @Override
      public void success(RememberPasswordMobileResponse rememberPasswordMobileResponse, Response response) {

        Log.d(Constants.LOGIN_LOG_TAG, rememberPasswordMobileResponse.toString());

        resetAndHidePasswordModal();

        if (rememberPasswordMobileResponse.getResponse().equals(SmartickAPI.MAIL_SENT_OK)) {
          showAlertDialog(getString(R.string.Notice),
              SweetAlertDialog.SUCCESS_TYPE,
              getString(R.string.RememberSuccess),
              null, null, getString(R.string.OK), null);
        } else if (rememberPasswordMobileResponse.getResponse().equals(SmartickAPI.UNKNOWN_TUTOR)
            || rememberPasswordMobileResponse.getResponse().equals(SmartickAPI.TUTOR_MISSING)
            || rememberPasswordMobileResponse.getResponse().equals(SmartickAPI.TUTOR_UNRELATED)
            ) {
          showAlertDialog(getString(R.string.Notice),
              SweetAlertDialog.ERROR_TYPE,
              getString(R.string.RememberFailTutor),
              null, null, getString(R.string.OK), null);
        } else {
          showAlertDialog(getString(R.string.Notice),
              SweetAlertDialog.ERROR_TYPE,
              getString(R.string.RememberFailUser),
              null, null, getString(R.string.OK), null);
        }
      }

      @Override
      public void failure(RetrofitError error) {

        Log.d(Constants.LOGIN_LOG_TAG, error.toString());

        resetAndHidePasswordModal();

        showAlertDialog(getString(R.string.Notice),
            SweetAlertDialog.ERROR_TYPE,
            getString(R.string.Something_went_wrong_try_again_later),
            null, null, getString(R.string.OK), null);
      }
    });
  }

  public void cancelRememberPassword(View view) {
    resetAndHidePasswordModal();
  }

  public void resetAndHidePasswordModal() {
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

  public void enableFlipButtons(boolean enabled) {
    loginStudentFlipButton.setEnabled(enabled);
    loginTutorFlipButton.setEnabled(enabled);
  }
}
