package com.mobile.android.smartick.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.http.SslError;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.mobile.android.smartick.R;
import com.mobile.android.smartick.YouTubeAPI.DeveloperKey;
import com.mobile.android.smartick.network.FileDownloader;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.UserType;
import com.mobile.android.smartick.util.AudioPlayer;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.network.NetworkStatus;
import com.mobile.android.smartick.util.LocaleHelper;

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
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


import javax.net.ssl.HttpsURLConnection;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends FragmentActivity {

	private XWalkView webView;
    private XWalkCookieManager cookieManager = null;
    private AudioPlayer audioPlayer;
    private String audioCallback = null;
    private Context ctx;
    private SweetAlertDialog pDialog;
    private SweetAlertDialog pAlertLogoutDialog;

	private String url;
    private String urlResult;
	private String username;
    private String password;
    private UserType userType;

    //Social
    private String token;
    private String socialType;

    private RelativeLayout tutorNameHolder;
    private SystemInfo sysInfo;

    //youtube player
    private YouTubePlayer ytPlayer;
    private View youTubePlayerHolder;
    private String youTubeVideoTitle = "";
    private String onFinishYoutubeCallback = "";
    private String onCloseDialogCallback = "";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //Set up selected language
        LocaleHelper.onCreate(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Network availability check
		if (!NetworkStatus.isConnected(this)) {
			toLogin();
		} else {

            ctx = this.getApplicationContext();

            //retrieves parameters from intent
			Bundle b = getIntent().getExtras();
			url = b.getString("url");

            //retreives username and password from bundle
			username = b.getString("username");
            password = b.getString("password");
            userType = UserType.valueOf(b.getString("userType"));

            //retreives social login info (if any)
            socialType = b.getString("socialType");
            token = b.getString("token");

            //initializaes systemInfo
            sysInfo = new SystemInfo(ctx);

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
            audioPlayer.setPlayerCallbacks(audioPlayer.player, onCompletionListener);

            //youtube init
            initializeNativeYoutubePlayer();

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
            logoutButton.setVisibility(View.GONE);

            //Go to virtual world button (DEBUG)
            if (Constants.DEBUG_MODE){
                Button goToVirtualWorldButton = (Button) findViewById(R.id.go_to_vw_button);
                goToVirtualWorldButton.setVisibility(View.VISIBLE);
                goToVirtualWorldButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToVirtualWorldButtonPressed();
                    }
                });
            }

            if (socialType != null && token != null){
                //performs social login
                new AsyncSocialLogin().execute(Constants.instance().getUrl_smartick_social_login(),username,token,socialType,sysInfo.getInstallationId());
            }else{
                //performs normal user login
                new AsyncLogin().execute(Constants.instance().getUrl_smartick_login(),username,password,sysInfo.getInstallationId());
            }

            //sets progress bar
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.BlueColor));
            pDialog.getProgressHelper().setRimColor(getResources().getColor(R.color.LightBlueColor));
            pDialog.setTitleText(getString(R.string.Loading));
            pDialog.setCancelable(false);
            pDialog.show();

            //if user is tutor, sets tutor name holder on top
            tutorNameHolder = (RelativeLayout) findViewById(R.id.tutor_name_holder);
            if (userType.equals(UserType.TUTOR)){
                TextView tutorName = (TextView) findViewById(R.id.tutor_name_text);
                tutorName.setText(username);
                tutorNameHolder.setVisibility(View.VISIBLE);
            }else{
                tutorNameHolder.setVisibility(View.GONE);
            }
		}
	}

//Buttons
    private void backButtonPressed(){
        String urlWebView = webView.getUrl();
        if ( urlWebView.contains("alumno/tutorialSesion")
                || urlWebView.contains("student/tutorialSession")
                || urlWebView.contains("alumno/home")
                || urlWebView.contains("student/home")
                || urlWebView.contains("alumno/fin")
                || urlWebView.contains("student/end")
                || urlWebView.contains("student/exercise-tutorial!endTutorial")
                || urlWebView.contains("tutor/")){
            showAlertLogout();
        }else if (urlWebView.contains("presentacionProblema")
                || urlWebView.contains("exercise")
                || urlWebView.contains("student/sessionTutorial")
                || urlWebView.contains("student/interactive-tutorial")
                || urlWebView.contains("initial-feedback")) {
//            webView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
            webView.load("/home.html",null);
        }else{
            webView.evaluateJavascript("volverButtonPressedAndroidApp();",null);
        }

        //hides youtubePlayer on page change
        if (youTubePlayerHolder != null && youTubePlayerHolder.getVisibility() == View.VISIBLE){
            hideYTPlayerHolder();
        }
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
        logoutButton.setVisibility(View.GONE);
    }

    private void goToVirtualWorldButtonPressed(){
        webView.evaluateJavascript("location.href='/student/virtualWorld.html'",null);
    }

//Web location
    private boolean isOnStudentWeb(){
        return webView.getUrl().contains("/alumno") || webView.getUrl().contains("/student");
    }

    private boolean isOnTutorWeb(){
        return webView.getUrl().contains("/tutor");
    }

//Acivity switching

    //to login
    private void toLogin() {
        finish();
    }


//WebView setttings and control

	private void setWebClientOptions() {
        webView.addJavascriptInterface(new JsInterface(), "SmartickAudioInterface");
        webView.addJavascriptInterface(new JsScrollInterface(), "SmartickJsScrollInterface");
        webView.addJavascriptInterface(new JsYouTubeInterface(), "SmartickYouTubeInterface");
        webView.clearCache(true);
	}

//Javascript Interface
    public class JsInterface {
        public JsInterface() {
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

    public class JsScrollInterface {
        public JsScrollInterface(){
        }

        @JavascriptInterface
        public synchronized void scrolled(){
            Log.d(Constants.WEBVIEW_LOG_TAG,"WebView scrolled");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (userType.equals(UserType.TUTOR) && tutorNameHolder.getVisibility() != View.GONE){
                        tutorNameHolder.setVisibility(View.GONE);
                    }
                }
            });
        }

        @JavascriptInterface
        public synchronized void reachedTop(){
            Log.d(Constants.WEBVIEW_LOG_TAG,"WebView scroll is at the top");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (userType.equals(UserType.TUTOR) && tutorNameHolder.getVisibility() != View.VISIBLE){
                        tutorNameHolder.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public class JsYouTubeInterface {
        public JsYouTubeInterface(){
        }

        @JavascriptInterface
        public synchronized void setVideoTitle(String title){
            Log.d(Constants.YTPLAYER_LOG_TAG,"SmartickYouTubeInterface - setVideoTitle - " + title);
            youTubeVideoTitle = title;
        }

        @JavascriptInterface
        public synchronized void enableControls(){
            Log.d(Constants.YTPLAYER_LOG_TAG, "SmartickYouTubeInterface - enableControls - ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ytPlayer != null) {
                        YouTubePlayer.PlayerStyle style = YouTubePlayer.PlayerStyle.DEFAULT;
                        ytPlayer.setPlayerStyle(style);
                    }
                }
            });
        }

        @JavascriptInterface
        public synchronized void disableControls(){
            Log.d(Constants.YTPLAYER_LOG_TAG, "SmartickYouTubeInterface - enableControls - ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ytPlayer != null) {
                        YouTubePlayer.PlayerStyle style = YouTubePlayer.PlayerStyle.MINIMAL;
                        ytPlayer.setPlayerStyle(style);
                    }
                }
            });
        }

        @JavascriptInterface
        public synchronized void setOnFinishYouTube(String callback){
            Log.d(Constants.YTPLAYER_LOG_TAG, "SmartickYouTubeInterface - setOnFinishYoutubeCallback - " + callback);
            onFinishYoutubeCallback = callback;
        }

        @JavascriptInterface
        public synchronized void setOnCloseDialog(String callback){
            Log.d(Constants.YTPLAYER_LOG_TAG, "SmartickYouTubeInterface - setOnCloseDialogCallback - " + callback);
            onCloseDialogCallback = callback;
        }

        @JavascriptInterface
        public synchronized boolean isPlayerReady(){
            Log.d(Constants.YTPLAYER_LOG_TAG,"SmartickYouTubeInterface - isPlayerReady ");
            if (ytPlayer != null){
                return true;
            }
            return false;
        }

        @JavascriptInterface
        public synchronized void playVideo(final String videoId){
            Log.d(Constants.YTPLAYER_LOG_TAG,"SmartickYouTubeInterface - Play Video " + videoId);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ytPlayer != null) {
                        showYTPlayerHolder();
                        ytPlayer.cueVideo(videoId);
                        ytPlayer.play();
                    }
                }
            });
        }

        @JavascriptInterface
        public synchronized void closeVideo(){
            Log.d(Constants.YTPLAYER_LOG_TAG,"SmartickYouTubeInterface - Close video");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ytPlayer != null) {
                        hideYTPlayerHolder();
                    }
                    if (onCloseDialogCallback != null) {
                        webView.evaluateJavascript(onCloseDialogCallback + "();", null);
                    }

                    onFinishYoutubeCallback = null;
                    onCloseDialogCallback = null;
                }
            });
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

            //shows/hides logout button
            if (isOnStudentWeb()){
                tutorNameHolder.setVisibility(View.GONE);
                String urlWebView = webView.getUrl();
                if (urlWebView.contains("presentacionProblema")
                        || urlWebView.contains("exercise")
                        || urlWebView.contains("alumno/home")
                        || urlWebView.contains("student/home")
                        || urlWebView.contains("alumno/fin")
                        || urlWebView.contains("student/end")
                        || urlWebView.contains("tutorialAyudaSesion")
                        || urlWebView.contains("sessionTutorial")
                        || urlWebView.contains("/initial-feedback")
                        || urlWebView.contains("/final-feedback")){
                    hideLogoutButton();
                }else{
                    showLogoutButton();
                }
            }

            //sets javascript scroll listeners
            webView.evaluateJavascript("var $win = $(window);$win.scroll(function () {if ($win.scrollTop() == 0){ SmartickJsScrollInterface.reachedTop();}else{SmartickJsScrollInterface.scrolled();}});", null);
        }

        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view, progressInPercent);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Loading Progress:" + progressInPercent);
        }

        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            super.shouldOverrideUrlLoading(view, url);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Should override loading: " + url);
            if (url.contains("bajarDiploma")
                    || url.contains("diploma.html?idDiploma")
                    || url.contains("truco.html?idTruco")
                    || url.contains("trucoPdf.html")){
                new AsyncDownloadFile(url).execute();
                Log.d(Constants.WEBVIEW_LOG_TAG, "Should override loading - Download pdf");
                return true;
            }
            if (url.contains("acceso") || url.contains("login")){
                toLogin();
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
                toLogin();
            }else{
                super.onReceivedLoadError(view, errorCode, description, failingUrl);
            }
        }

        public void onReceivedSslError(XWalkView view, ValueCallback<java.lang.Boolean> callback, SslError error) {
            Log.d(Constants.WEBVIEW_LOG_TAG, "Received SSL Error: " + error.toString());
            super.onReceivedSslError(view, callback, error);
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
            return doHttpPost(params[0], params[1], params[2], params[3]);
        }

        @Override
        protected void onPostExecute(String urlRedirect) {
            redirectLogin(urlRedirect);
        }
    }

    private String doHttpPost(String url,String username, String password, String installationId){

        DefaultHttpClient httpClient = new DefaultHttpClient();
        MyRedirectHandler handler = new MyRedirectHandler();
        httpClient.setRedirectHandler(handler);

        HttpPost post = new HttpPost(url);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("j_username", username));
        nvps.add(new BasicNameValuePair("j_password", password));

        // id de dispositivo para la visualizacion adaptada a tablets
        post.addHeader("android-app", installationId);

        //User agent para app Android
        post.addHeader("User-Agent","Smartick_Android/" + sysInfo.getVersion() + " (Android: " + sysInfo.getOsVersion() + " " + sysInfo.getDevice() +")");

        //Cookie almacenada?
        String c = cookieManager.getCookie(url);
        if (c != null){
            post.addHeader("Cookie", c);
        }

        HttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
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


//    private String doHttpPostUrlConnection(String url,String username, String password, String installationId){
//
//        URL urlConn = null;
//        String response = "";
//        try {
//
//            urlConn = new URL(url);
//            HttpURLConnection urlConnection = (HttpsURLConnection) urlConn.openConnection();
//            urlConnection.setReadTimeout(60000);
//            urlConnection.setConnectTimeout(60000);
//            urlConnection.setRequestMethod("POST");
//            urlConnection.setDoInput(true);
//            urlConnection.setDoOutput(true);
//            urlConnection.setInstanceFollowRedirects(true);
//
//            String param="j_username=" + URLEncoder.encode(username,"UTF-8") +
//                         "&j_password="+URLEncoder.encode(password,"UTF-8");
//
//            urlConnection.setRequestProperty("android-app", sysInfo.getInstallationId());
//            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            urlConnection.setRequestProperty("User-Agent","Smartick_Android/" + sysInfo.getVersion() + " (Android: " + sysInfo.getOsVersion() + " " + sysInfo.getDevice() +")");
//            urlConnection.setRequestProperty("j_username",username);
//            urlConnection.setRequestProperty("j_password",password);
//
//            //Cookie almacenada?
//            String c = cookieManager.getCookie(url);
//            if (c != null){
//                urlConnection.setRequestProperty("Cookie", c);
//            }
//
//
//            urlConnection.connect();
//
//            OutputStream os = urlConnection.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//
//            writer.write(param);
//
//            writer.flush();
//            writer.close();
//            os.close();
//            int responseCode=urlConnection.getResponseCode();
//
//            if (responseCode == HttpsURLConnection.HTTP_OK) {
//                String line;
//                BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//                while ((line=br.readLine()) != null) {
//                    response+=line;
//                }
//            }
//            else {
//                response="";
//            }
//
//
//
//
//            // get redirect url from "location" header field
//            String newUrl = urlConnection.getHeaderField("Location");
//            Map<String, List<String>> headers = urlConnection.getHeaderFields();
//
//            // get the cookie if need, for login
//            String cookies = urlConnection.getHeaderField("Set-Cookie");
//
//
//
//
//            return newUrl;
//
//            lastRedirectedUri = super.getLocationURI(response, context);
//            if(urlResult == null){
//                urlResult = lastRedirectedUri.toString();
//
//                //retreives cookies from response
//                Header [] headers = response.getAllHeaders();
//                for (Header h: headers){
//                    if (h.getName().equalsIgnoreCase("set-cookie"))
//                    {
//                        cookieManager.setCookie(urlResult,h.getValue());
//                    }
//                }
//            }
//            return lastRedirectedUri;
//
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }


    // Social login request
    private class AsyncSocialLogin extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            return doSocialHttpPost(params[0], params[1], params[2], params[3],params[4]);
        }

        @Override
        protected void onPostExecute(String urlRedirect) {
            redirectLogin(urlRedirect);
        }
    }

    private String doSocialHttpPost(String url,String username, String token, String socialType, String installationId){

        DefaultHttpClient httpClient = new DefaultHttpClient();
        MyRedirectHandler handler = new MyRedirectHandler();
        httpClient.setRedirectHandler(handler);

        HttpPost post = new HttpPost(url);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("type", socialType));
        nvps.add(new BasicNameValuePair("token", token));

        // id de dispositivo para la visualizacion adaptada a tablets
        post.addHeader("android-app", installationId);

        //User agent para app Android
        post.addHeader("User-Agent","Smartick_Android/" + sysInfo.getVersion() + " (Android: " + sysInfo.getOsVersion() + " " + sysInfo.getDevice() +")");

        //Cookie almacenada?
        String c = cookieManager.getCookie(url);
        if (c != null){
            post.addHeader("Cookie", c);
        }

        HttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
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

    private void doLogout(){
        Log.d(Constants.WEBVIEW_LOG_TAG, "doLogout");

        if (pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }

        //Logs out facebook user
        if (socialType != null && token != null && socialType.equals("Facebook")){
            LoginManager.getInstance().logOut();
        }

        audioPlayer.stop();
        webView.load(Constants.instance().getUrl_logout(), null);
        finish();
    }

    private File downloadPDFFromUrl(String url){
        Log.d(Constants.WEBVIEW_LOG_TAG, "Downloading pdf from: " + url);

        File folder = new File(ctx.getExternalCacheDir(), "/smk_pdf/");
        if (!folder.exists()){
            folder.mkdirs();
        }

        String c = cookieManager.getCookie(Constants.instance().getUrl_context());

        File fileDL = new File(folder, "diploma.pdf");
        FileDownloader.download(url, fileDL, c);

        File file = new File(folder, "diploma.pdf");
        return file;
    }

    private void showPDF(File file){
        if (file!= null && file.exists()){
            PackageManager packageManager = getPackageManager();
            Intent testIntent = new Intent(Intent.ACTION_VIEW);
            testIntent.setType("application/pdf");
            List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/pdf");
            startActivity(intent);
        }else{
            Log.d(Constants.WEBVIEW_LOG_TAG, "ShowPDF - File does not exist!");
        }
    }

    //Alert dialog
    private SweetAlertDialog showAlertLogout(){
        SweetAlertDialog alertDialog = new SweetAlertDialog(this,SweetAlertDialog.NORMAL_TYPE);
        alertDialog.setTitleText(getString(R.string.Warning));
        alertDialog.setContentText(getString(R.string.Go_back_to_login_int));
        alertDialog.setConfirmText(getString(R.string.Yes));
        alertDialog.setCancelText(getString(R.string.No));

        alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                if (pAlertLogoutDialog != null && pAlertLogoutDialog.isShowing()) {
                    pAlertLogoutDialog.dismiss();
                }
                doLogout();
            }
        });

        alertDialog.show();

        return alertDialog;
    }

    // download file async task
    private class AsyncDownloadFile extends AsyncTask<String, Integer, File> {

        private String url;

        public AsyncDownloadFile(String url){
            this.url = url;
        }

        @Override
        protected File doInBackground(String... params) {
            return downloadPDFFromUrl(this.url);
        }

        @Override
        protected void onPostExecute(File file) {
            showPDF(file);
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
            toLogin();
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

                //retreives cookies from response
                Header [] headers = response.getAllHeaders();
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

    //Native Youtbe Player
    private void initializeNativeYoutubePlayer(){
        youTubePlayerHolder = findViewById(R.id.youtube_player_holder);
        youTubePlayerHolder.setVisibility(View.GONE);
        YouTubePlayerFragment youTubePlayerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                ytPlayer = null;
                if (!b) {
                    ytPlayer = youTubePlayer;

                    YouTubePlayer.PlayerStyle style = YouTubePlayer.PlayerStyle.MINIMAL;
                    ytPlayer.setPlayerStyle(style);

                    //sets up event listeners
                    ytPlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                        @Override
                        public void onLoading() {
                            ytPlayerLoading();
                        }

                        @Override
                        public void onLoaded(String s) {
                            ytPlayerLoaded(s);
                        }

                        @Override
                        public void onAdStarted() {
                            ytAdStarted();
                        }

                        @Override
                        public void onVideoStarted() {
                            ytVideoStarted();
                        }

                        @Override
                        public void onVideoEnded() {
                            ytVideoEnded();
                        }

                        @Override
                        public void onError(YouTubePlayer.ErrorReason errorReason) {
                            ytVideoError(errorReason);
                        }
                    });
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d(Constants.YTPLAYER_LOG_TAG, "onInitializationFailure - set ytPlayer = null");
                ytPlayer = null;
            }
        });
    }

    private void ytPlayerLoading(){
        Log.d(Constants.YTPLAYER_LOG_TAG, "onLoading");
    }

    private void ytPlayerLoaded(String s){
        Log.d(Constants.YTPLAYER_LOG_TAG, "onLoaded " + s);
    }

    private void ytAdStarted(){
        Log.d(Constants.YTPLAYER_LOG_TAG,"onAdStarted");
    }

    private void ytVideoStarted(){
        Log.d(Constants.YTPLAYER_LOG_TAG, "onVideoStarted");
    }

    private void ytVideoEnded(){
        Log.d(Constants.YTPLAYER_LOG_TAG, "onVideoEnded");
        if (onFinishYoutubeCallback != null){
            webView.evaluateJavascript(onFinishYoutubeCallback + "();", null);
        }
    }

    private void ytVideoError(YouTubePlayer.ErrorReason errorReason){
        Log.d(Constants.YTPLAYER_LOG_TAG, "onError " + errorReason);
    }

    private void  showYTPlayerHolder(){
        Log.d(Constants.YTPLAYER_LOG_TAG,"show YT player");
        youTubePlayerHolder.setVisibility(View.VISIBLE);
        TextView title = (TextView) findViewById(R.id.yt_video_title);
        title.setText(youTubeVideoTitle);
        webView.setVisibility(View.GONE);
    }

    private void hideYTPlayerHolder(){
        Log.d(Constants.YTPLAYER_LOG_TAG, "hide YT player");
        if (ytPlayer != null){
            ytPlayer.pause();
            youTubeVideoTitle = "";
            youTubePlayerHolder.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }
    }

    public void closeYTPlayerHolder(View view){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideYTPlayerHolder();
            }
        });
    }

    public void toggleYTPlayerFullscreen(View view){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ytPlayer != null){
                    ytPlayer.setFullscreen(true);
                }
            }
        });
    }

}