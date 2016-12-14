package com.mobile.android.smartick.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.network.GetFreemiumSessionStatusResponse;
import com.mobile.android.smartick.network.LoginStatusResponse;
import com.mobile.android.smartick.network.NetworkStatus;
import com.mobile.android.smartick.network.SmartickAPI;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.FreemiumProfile;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.UserType;
import com.mobile.android.smartick.util.AudioPlayer;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.util.LocaleHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by sbarrio on 03/06/15.
 */
public class FreemiumMainActivity extends Activity {

    private static final String HABITACION = "habitacion";
    private static final String CLUB_SOCIAL = "clubSocial";
    private static final String COLEGIO_MSG = "colegio";
    private static final String TIENDA_MSG =  "tienda";
    private static final String CASTILLO_MSG = "castillo";
    private static final String AVATAR_MSG = "avatar";

    private XWalkView webView;
    private XWalkCookieManager cookieManager = null;
    private AudioPlayer audioPlayer;
    private String audioCallback = null;
    private Context ctx;
    private SweetAlertDialog pDialog;
    private SweetAlertDialog pAlertResetDialog;
    private SweetAlertDialog pAlertLogoutDialog;
    private ImageLoader imageLoader;

    private String url;
    private String urlResult;
    private int selectedAvatar;
    private int selectedAge;
    private RelativeLayout tutorNameHolder;
    private SystemInfo sysInfo;
    private FreemiumProfile freemiumProfile;

    //Modal dialogs
    private AlertDialog benefitAlertDialog;
    private AlertDialog registerAlertDialog;

    private boolean registerModalShowing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up selected language
        LocaleHelper.onCreate(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Network availability check
        if (!NetworkStatus.isConnected(this)) {
            toWelcome();
            return;
        }

        ctx = this.getApplicationContext();

        //retreives parameters from intent
        Bundle b = getIntent().getExtras();
        url = b.getString("url");

        //retreives username and password from bundle
        selectedAvatar = b.getInt("selectedAvatar");
        selectedAge = b.getInt("selectedAge");

        //initializaes systemInfo
        sysInfo = new SystemInfo(ctx);

        //inits freemium profile and updates it
        freemiumProfile = new FreemiumProfile(ctx);
        freemiumProfile.storeFreemiumAge(selectedAge);
        freemiumProfile.storeFreemiumAvatar(selectedAvatar);

        setContentView(R.layout.activity_main);

        webView=(XWalkView)findViewById(R.id.webview);

        //sets cookie manager
        cookieManager = new XWalkCookieManager();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptFileSchemeCookies(true);

        //sets clients
        webView.setUIClient(new UIClient(webView));
        webView.setResourceClient(new ResourceClient(webView));

        //Sets window scale configuration
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        webView.getSettings().setInitialPageScale(1.0f);

        if (height <= 600){
            webView.setInitialScale(75);
        }else{
            webView.setInitialScale(100);
        }

        webView.getSettings().setUseWideViewPort(true);

        //webView settings
        setWebClientOptions();

        //Inits Audio Player
        audioPlayer = new AudioPlayer();
        audioPlayer.init(ctx);

        //sets player callbacks
        MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                audioPlayer.finishedPlayback();
                executeAudioCallback();
            }
        };
        audioPlayer.setPlayerCallbacks(audioPlayer.player,onCompletionListener);

        //sets up imageLoader
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
        imageLoader = imageLoader.getInstance();


        //Enables remote debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, Constants.DEBUG_MODE);

        //Button listeners
        Button backButton = (Button) findViewById(R.id.back_button_main);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButtonPressed();
            }
        });

        Button logoutButton = (Button) findViewById(R.id.logout_button_main);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutButtonPressed();
            }
        });

        //logout button is not visible by default
        logoutButton.setVisibility(View.INVISIBLE);

        //performs login
        new AsyncLogin().execute(Constants.instance().getUrl_smartick_login_freemium());

        //sets progress bar
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.BlueColor));
        pDialog.getProgressHelper().setRimColor(getResources().getColor(R.color.LightBlueColor));
        pDialog.setTitleText(getString(R.string.Loading));
        pDialog.setCancelable(false);
        pDialog.show();

        //if user is tutor, sets tutor name holder on top
        tutorNameHolder = (RelativeLayout) findViewById(R.id.tutor_name_holder);
        tutorNameHolder.setVisibility(View.GONE);
    }


    //Buttons
    private void backButtonPressed(){

        //check freemium session status
        SmartickRestClient.get().getFreemiumSessionStatus(sysInfo.getInstallationId(),
                new Callback<GetFreemiumSessionStatusResponse>() {
                    @Override
                    public void success(GetFreemiumSessionStatusResponse freemiumSessionStatusResponse, Response response){
                        Log.d(Constants.FREEMIUM_LOG_TAG, "getFreemiumSessionSatus RESPONSE: last Freemium session on - : " + freemiumSessionStatusResponse.getLastSessionDate());
                        //session not finished -> Alert Modal prior to session reset
                        if (!freemiumSessionStatusResponse.getSessionFinished()){
                            pAlertResetDialog = showAlertSessionResetModal();
                        }else{
                            //session finished, we let the user go back
                            String urlWebView = webView.getUrl();
                            if (urlWebView != null && urlWebView.contains("end")){
                                doLogout();
                            }else{
                                webView.evaluateJavascript("volverButtonPressedAndroidApp();",null);
                            }
                        }
                    }
                    @Override
                public void failure(RetrofitError error) {
                        Log.d(Constants.FREEMIUM_LOG_TAG, "getFreemiumSessionSatus ERROR: " + error);
                        finish();
                    }
                });
    }

    private void logoutButtonPressed(){
        showAlertLogout();
    }

    private void showLogoutButton(){
        Button logoutButton = (Button) findViewById(R.id.logout_button_main);
        logoutButton.setVisibility(View.VISIBLE);
    }

    private void hideLogoutButton(){
        Button logoutButton = (Button) findViewById(R.id.logout_button_main);
        logoutButton.setVisibility(View.INVISIBLE);
    }

    //Web location
    private boolean isOnStudentWeb(){
        return webView.getUrl().contains("/alumno") || webView.getUrl().contains("/student");
    }

    private boolean isOnTutorWeb(){
        return webView.getUrl().contains("/tutor");
    }

    private void toWelcome(){
        finish();
    }

    private void toRegister(){
        finish();
        startActivity(new Intent(this, RegistroActivity.class));
    }

    //WebView setttings and control
    private void setWebClientOptions() {
        webView.addJavascriptInterface(new JsAudioInterface(), "SmartickAudioInterface");
        webView.addJavascriptInterface(new JsFreemiumInterface(), "SmartickFreemiumInterface");
        webView.clearCache(true);
    }

    //Javascript Interfaces
    public class JsAudioInterface {
        public JsAudioInterface() {
        }

        @JavascriptInterface
        public synchronized void playUrl(String path) {
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - Play audio file: " + path);

            //if url is missing http we add our URL_CONTEXT
            if (path != null && path.startsWith("http")){
                audioPlayer.playURL(path);
            }else if (path.startsWith("//")){
                audioPlayer.playURL("https:" + path);
            }else{
                audioPlayer.playURL(Constants.instance().getUrl_context() + path);
            }
        }

        @JavascriptInterface
        public synchronized void stop(){
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - Stop Audio");
            audioPlayer.stop();
        }

        @JavascriptInterface
        public synchronized void bind(String callback){
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - bind audio callback to " + callback);
            audioCallback = callback;
        }


        @JavascriptInterface
        public synchronized void unbind(){
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - unbind audio callback");
            audioCallback = null;
        }
    }

    public class JsFreemiumInterface {
        public JsFreemiumInterface() {
        }

        @JavascriptInterface
        public synchronized void showRegisterMessage(String type) {
            Log.d(Constants.WEBVIEW_LOG_TAG, "SmartickFreemiumInterface - show register message for: " + type);
            showRegisterModalForType(type);
        }

        @JavascriptInterface
        public synchronized void showRegisterMessageGame(String name) {
            Log.d(Constants.WEBVIEW_LOG_TAG, "SmartickFreemiumInterface - show register message game for: " + name);
            if (!registerModalShowing){
                registerModalShowing = true;
                showRegisterModal(getString(R.string.Smartick_Games),getString(R.string.vw_games),"registerGames",R.layout.freemium_register_games_modal);
            }
        }
    }

    private void executeAudioCallback(){
        if (audioCallback != null){
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - execute callback");
            webView.evaluateJavascript(audioCallback + "()",null);
        }
    }


    class ResourceClient extends XWalkResourceClient {

        public ResourceClient(XWalkView xwalkView) {
            super(xwalkView);
        }

        public void onLoadStarted(XWalkView view, String url) {
            super.onLoadStarted(view, url);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Load Started:" + url);
        }

        public void onLoadFinished(XWalkView view, String url) {
            super.onLoadFinished(view, url);

            if (pDialog.isShowing()){
                pDialog.dismiss();
            }

            Log.d(Constants.WEBVIEW_LOG_TAG, "Load Finished:" + url);

            //disables touch highlighting
            webView.evaluateJavascript("document.documentElement.style.webkitUserSelect='none';" +
                    "document.documentElement.style.webkitTouchCallout='none';" +
                    "document.documentElement.style.webkitTapHighlightColor='rgba(0,0,0,0)';",null);

            String urlWebView = webView.getUrl();
            if (urlWebView.contains("presentacionProblema")
                    || urlWebView.contains("exercise")
                    || urlWebView.contains("end")
                    || urlWebView.contains("fin")){
                hideLogoutButton();
            }else{
                showLogoutButton();
            }

            //benefits modal appears when user gets to mundoVirtual
            if (urlWebView.contains("mundoVirtualFreemium.html")
                    || urlWebView.contains("virtualWorldFreemium.html")){
                showBenefitsModal();
            }
        }

        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view, progressInPercent);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Loading Progress:" + progressInPercent);
        }

        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            super.shouldOverrideUrlLoading(view,url);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Should override loading: " + url);

            if (url.contains("acceso") || url.contains("login") || url.contains("accessDenied")){
                doLogout();
                return true;
            }

            return false;
        }

        public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
            Log.d(Constants.WEBVIEW_LOG_TAG, "Intercept load request");

            return super.shouldInterceptLoadRequest(view, url);
        }

        public void onReceivedLoadError(XWalkView view, int errorCode, String description,
                                        String failingUrl) {
            Log.d(Constants.WEBVIEW_LOG_TAG, "Load Failed - Error: " + errorCode + " - " + description);
            if (errorCode == ERROR_CONNECT
                    || errorCode == ERROR_HOST_LOOKUP
                    || errorCode == ERROR_TIMEOUT
                    || errorCode == ERROR_REDIRECT_LOOP
                    || errorCode == ERROR_TOO_MANY_REQUESTS){
                toWelcome();
            }else{
                super.onReceivedLoadError(view, errorCode, description, failingUrl);
            }
        }

        public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
            Log.d(Constants.WEBVIEW_LOG_TAG, "Received SSL Error: " + error.toString());
            super.onReceivedSslError(view,callback,error);
        }
    }

    class UIClient extends XWalkUIClient {

        public UIClient(XWalkView xwalkView) {
            super(xwalkView);
        }

        public void onJavascriptCloseWindow(XWalkView view) {
            super.onJavascriptCloseWindow(view);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Window closed.");
        }

        public boolean onJavascriptModalDialog(XWalkView view, JavascriptMessageType type,
                                               String url,
                                               String message, String defaultValue, XWalkJavascriptResult result) {
            Log.d(Constants.WEBVIEW_LOG_TAG, "Show JS dialog.");
            return super.onJavascriptModalDialog(view, type, url, message, defaultValue, result);
        }

        public void onFullscreenToggled(XWalkView view, boolean enterFullscreen) {
            super.onFullscreenToggled(view, enterFullscreen);
            if (enterFullscreen) {
                Log.d(Constants.WEBVIEW_LOG_TAG, "Entered fullscreen.");
            } else {
                Log.d(Constants.WEBVIEW_LOG_TAG, "Exited fullscreen.");
            }
        }

        public void openFileChooser(XWalkView view, ValueCallback<Uri> uploadFile,
                                    String acceptType, String capture) {
            super.openFileChooser(view, uploadFile, acceptType, capture);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Opened file chooser.");
        }

        public void onScaleChanged(XWalkView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Scale changed from " + oldScale + " to " + newScale);
        }
    }


    // login request
    private class AsyncLogin extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            return doHttpPost(params[0]);
        }

        @Override
        protected void onPostExecute(String urlRedirect) {
            redirectLogin(urlRedirect);
        }
    }

    /**
     * Si el login es correcto se pasa al webview
     */
    private void redirectLogin(String urlRedirect){
        if(urlRedirect != null && !urlRedirect.contains("acceso") && !urlRedirect.contains("login")) {
            Log.d(Constants.WEBVIEW_LOG_TAG,"Login valid");
            webView.load(urlRedirect,null);
        } else {
            Log.d(Constants.WEBVIEW_LOG_TAG,"Login failed");
            toWelcome();
        }
    }

    private String doHttpPost(String url){

        DefaultHttpClient httpClient = new DefaultHttpClient();
        MyRedirectHandler handler = new MyRedirectHandler();
        httpClient.setRedirectHandler(handler);

        //device info on post body
        HttpPost post = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("device", sysInfo.getDevice()));
        nvps.add(new BasicNameValuePair("version", sysInfo.getVersion()));
        nvps.add(new BasicNameValuePair("osVersion", sysInfo.getOsVersion()));

        // id de dispositivo para la visualizacion adaptada a tablets
        post.addHeader("android-app", sysInfo.getInstallationId());
        post.addHeader("freemium-installid", sysInfo.getInstallationId());
        post.addHeader("freemium-age",String.valueOf(selectedAge));
        post.addHeader("freemium-avatar",String.valueOf(selectedAvatar));
        post.addHeader("loc",sysInfo.getLocale());

        //User agent para app Android
        post.addHeader("User-Agent","Smartick_Android/" + sysInfo.getVersion() + " (Android: " + sysInfo.getOsVersion() + " " + sysInfo.getDevice() +")");
        HttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
            response = httpClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        URI last = handler.lastRedirectedUri;
        if (last!= null){
            Log.d(Constants.WEBVIEW_LOG_TAG,"LAST_REDIRECT: " + last.toString());
            return last.toString();
        }
        return null;
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

                //retreives cookies from response
                Header[] headers = response.getAllHeaders();
                for (Header h: headers){
                    if (h.getName().equalsIgnoreCase("set-cookie"))
                    {
                        cookieManager.setCookie(urlResult,h.getValue());
                    }
                }
            }
            return lastRedirectedUri;
        }
    }

    private void doLogout(){
        Log.d(Constants.WEBVIEW_LOG_TAG, "doLogout");

        if (pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }
        audioPlayer.stop();
        webView.load(Constants.instance().getUrl_logout(), null);
        finish();
    }


    private SweetAlertDialog showAlertSessionResetModal(){
        SweetAlertDialog alertDialog = new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE);
        alertDialog.setTitleText(getString(R.string.Notice));
        alertDialog.setContentText(getString(R.string.lose_progress));
        alertDialog.setConfirmText(getString(R.string.Exit));
        alertDialog.setCancelText(getString(R.string.Continue));

        alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                if (pAlertResetDialog != null && pAlertResetDialog.isShowing()) {
                    pAlertResetDialog.dismiss();
                }
                doLogout();
            }
        });

        alertDialog.show();

        return alertDialog;
    }

    private SweetAlertDialog showAlertLogout(){
        SweetAlertDialog alertDialog = new SweetAlertDialog(this,SweetAlertDialog.NORMAL_TYPE);
        alertDialog.setTitleText(getString(R.string.Warning));
        alertDialog.setContentText(getString(R.string.Go_back_to_login_int));
        alertDialog.setConfirmText(getString(R.string.Yes));
        alertDialog.setCancelText(getString(R.string.No));

        alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                if (pAlertLogoutDialog!= null && pAlertLogoutDialog.isShowing()){
                    pAlertLogoutDialog.dismiss();
                }
                pAlertLogoutDialog = null;
                doLogout();
            }
        });

        alertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                if (pAlertLogoutDialog != null && pAlertLogoutDialog.isShowing()) {
                    pAlertLogoutDialog.dismiss();
                }
                pAlertLogoutDialog = null;
            }
        });

        alertDialog.show();

        return alertDialog;
    }

    private void showRegisterModalForType(String type){
        if (registerModalShowing){
            return;
        }
        registerModalShowing = true;
        switch(type){
            case HABITACION:
                //inflate modal de habitacion
                showRegisterModal(getString(R.string.Personal_room), getString(R.string.vw_room), "registerHabitacion", R.layout.freemium_register_room_modal);
                break;
            case COLEGIO_MSG:
                showRegisterModal(getString(R.string.School),getString(R.string.vw_school),"registerColegio",R.layout.freemium_register_modal);
                break;
            case CLUB_SOCIAL:
                showRegisterModal(getString(R.string.Social_Club),getString(R.string.vw_social),"registerClubSocial",R.layout.freemium_register_modal);
                break;
            case TIENDA_MSG:
                showRegisterModal(getString(R.string.Shop),getString(R.string.vw_shop),"registerTienda",R.layout.freemium_register_modal);
                break;
            case CASTILLO_MSG:
                showRegisterModal(getString(R.string.Mathgicians),getString(R.string.vw_mathgicians),"registerCastillos",R.layout.freemium_register_modal);
                break;
            case AVATAR_MSG:
                showRegisterModal(getString(R.string.Your_Avatar),getString(R.string.vw_avatar),"registerAvatar",R.layout.freemium_register_modal);
                break;
            default: break;
        }
    }

    public void goBackRegisterPressed(View view){
        hideRegisterModal();
        FreemiumMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("volverACentro();", null);
            }
        });
    }

    public void laterGameButtonPressed(View view){
        hideRegisterModal();
        FreemiumMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("volverACentro();", null);
            }
        });
    }

    public void registerButtonPressed(View view){
        //go to register
        toRegister();
    }

    public void enterRoomButtonPressed(View view){
        hideRegisterModal();
        FreemiumMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("entrarEnHabitacion();", null);
            }
        });
    }

    public void laterButtonPressed(View view){
        hideBenefitsModal();
    }

    //Benefits modal
    private void showBenefitsModal(){
        Context context = FreemiumMainActivity.this;
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View benefitView = li.inflate(R.layout.freemium_benefit_modal,null);

        Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");
        ((TextView) benefitView.findViewById(R.id.titleBenefits)).setTypeface(tfDidact);
        ((TextView) benefitView.findViewById(R.id.benefit1)).setTypeface(tfDidact);
        ((TextView) benefitView.findViewById(R.id.benefit2)).setTypeface(tfDidact);
        ((TextView) benefitView.findViewById(R.id.benefit3)).setTypeface(tfDidact);
        ((TextView) benefitView.findViewById(R.id.benefit4)).setTypeface(tfDidact);
        ((TextView) benefitView.findViewById(R.id.benefit5)).setTypeface(tfDidact);
        ((Button) benefitView.findViewById(R.id.registerBenefitButton)).setTypeface(tfDidact);
        ((Button) benefitView.findViewById(R.id.laterBenefitButton)).setTypeface(tfDidact);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setView(benefitView);

        benefitAlertDialog = alertBuilder.create();
        benefitAlertDialog.show();

    }

    private void hideBenefitsModal(){
        benefitAlertDialog.dismiss();
    }

    private void showRegisterModal(final String title,final String text, final String image, final int layout){

        FreemiumMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = FreemiumMainActivity.this;
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View registerView = li.inflate(layout, null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setView(registerView);

                //sets modal content
                Typeface tfDidact = Typeface.createFromAsset(getAssets(), "fonts/DidactGothic.ttf");
                TextView titleRegister = (TextView) registerView.findViewById(R.id.titleRegister);
                titleRegister.setTypeface(tfDidact);
                titleRegister.setText(title);

                TextView textRegister = (TextView) registerView.findViewById(R.id.textRegisterModal);
                textRegister.setTypeface(tfDidact);
                textRegister.setText(text);

                ImageView imageRegisterModal = (ImageView) registerView.findViewById(R.id.imageViewRegister);
                getImageRegister(image, imageRegisterModal);

                Button b = (Button) registerView.findViewById(R.id.registerModalButton);
                if (b != null){
                    b.setTypeface(tfDidact);
                }

                b = (Button) registerView.findViewById(R.id.goBackRegisterButton);
                if (b != null){
                    b.setTypeface(tfDidact);
                }

                b = (Button) registerView.findViewById(R.id.enterRoomRegisterButton);
                if (b != null){
                    b.setTypeface(tfDidact);
                }

                b = (Button) registerView.findViewById(R.id.laterGameRegisterButton);
                if (b != null){
                    b.setTypeface(tfDidact);
                }

                registerAlertDialog = alertBuilder.create();
                registerAlertDialog.setCanceledOnTouchOutside(false);

                //shows dialog
                registerAlertDialog.show();
            }
        });
    }

    private void hideRegisterModal(){
        registerAlertDialog.dismiss();
        registerModalShowing = false;
    }

    private void getImageRegister(String imageName, final ImageView targetImageView) {
        final String imageUri = Constants.instance().getUrl_freemium_image() + sysInfo.getLocale() + "/" + imageName + ".png";
        Log.d(Constants.WEBVIEW_LOG_TAG, "requesting image: " + imageUri);
        FreemiumMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageLoader.displayImage(imageUri, targetImageView);
            }
        });
    }
}
