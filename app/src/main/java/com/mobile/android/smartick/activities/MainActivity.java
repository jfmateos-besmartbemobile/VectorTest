package com.mobile.android.smartick.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.network.FileDownloader;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.util.AudioPlayer;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.util.Network;
import com.mobile.android.smartick.util.RedirectHandler;
import com.mobile.android.smartick.util.ScreenUtils;

import org.apache.http.Header;
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
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkClient;
import org.xwalk.core.internal.XWalkCookieManager;
import org.xwalk.core.internal.XWalkSettings;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class MainActivity extends Activity {

	private ProgressBar progressBar;
	private XWalkView webView;
    private XWalkCookieManager cookieManager = null;
    private AudioPlayer audioPlayer;
    private String audioCallback = null;
    private Context ctx;

	private String url;
    private String urlResult;
	private String username;
    private String password;
    private SystemInfo sysInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Network availability check
		if (!Network.isConnected(this)) {
			toOffline();
		} else {

            ctx = this.getApplicationContext();

            //retreives parameters from intent
			Bundle b = getIntent().getExtras();
			url = b.getString("url");

            //retreives username and password from bundle
			username = b.getString("username");
            password = b.getString("password");

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

            //Enables remote debugging
            XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

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
            new AsyncLogin().execute(Constants.URL_SMARTICK_LOGIN,username,password,sysInfo.getInstallationId());

            //sets progress bar
			progressBar = (ProgressBar) findViewById(R.id.progressbar);
		}
	}


//Buttons
    private void backButtonPressed(){
        String urlWebView = webView.getUrl();
        if (urlWebView.contains("presentacionProblema")
                || urlWebView.contains("alumno/tutorialSesion")
                || urlWebView.contains("alumno/home")
                || urlWebView.contains("initial-feedback")
                || urlWebView.contains("alumno/fin")
                || urlWebView.contains("tutor/")){
           doLogout();
           toLogin();
        }else{
            webView.evaluateJavascript("volverButtonPressedAndroidApp();",null);
        }
    }

    private void logoutButtonPressed(){
        doLogout();
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
        return webView.getUrl().contains("/alumno");
    }

    private boolean isOnTutorWeb(){
        return webView.getUrl().contains("/tutor");
    }

//Acivity switching

    //to login
    private void toLogin() {
        finish();
    }
    //no network
    private void toOffline() {
        startActivity(new Intent(this, OfflineActivity.class));
    }



//WebView setttings and control

	private void setWebClientOptions() {
        webView.addJavascriptInterface(new JsInterface(), "SmartickAudioInterface");
        webView.clearCache(true);
	}

//Javascript Interface3
    public class JsInterface {
        public JsInterface() {
        }

        @JavascriptInterface
        public void playUrl(String path) {
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - Play audio file: " + path);
            audioPlayer.playURL(Constants.URL_CONTEXT + path);
        }

        @JavascriptInterface
        public void stop(){
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - Stop Audio");
            audioPlayer.stop();
        }

        @JavascriptInterface
        public void bind(String callback){
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - bind audio callback to " + callback);
            audioCallback = callback;
        }


        @JavascriptInterface
        public void unbind(){
            Log.d(Constants.WEBVIEW_LOG_TAG,"SmartickAudioInterface - unbind audio callback");
            audioCallback = null;
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
            Log.d(Constants.WEBVIEW_LOG_TAG, "Load Finished:" + url);

            //disables touch highlighting
            webView.evaluateJavascript("document.documentElement.style.webkitUserSelect='none';" +
                                       "document.documentElement.style.webkitTouchCallout='none';" +
                                       "document.documentElement.style.webkitTapHighlightColor='rgba(0,0,0,0)';",null);

            //shows/hides logout button
            if (isOnStudentWeb()){
                String urlWebView = webView.getUrl();
                if (urlWebView.contains("presentacionProblema")
                        || urlWebView.contains("alumno/home")
                        || urlWebView.contains("alumno/fin")
                        || urlWebView.contains("tutorialAyudaSesion")
                        || urlWebView.contains("/initial-feedback")
                        || urlWebView.contains("/final-feedback")){
                    hideLogoutButton();
                }else{
                    showLogoutButton();
                }
            }
        }

        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view, progressInPercent);
            Log.d(Constants.WEBVIEW_LOG_TAG, "Loading Progress:" + progressInPercent);

            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);

            progressBar.incrementProgressBy(progressInPercent);

            if (progressInPercent == 100) {
                progressBar.setVisibility(View.GONE);
            }
        }

        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {

            if (url.contains("bajarDiploma") || url.contains("diploma.html?idDiploma") || url.contains("truco.html?idTruco")){
                new AsyncDownloadFile(url).execute();
                return true;
            }
            if (url.contains("accceso")){
                doLogout();
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
            Log.d(Constants.WEBVIEW_LOG_TAG, "Load Failed:" + description);
            super.onReceivedLoadError(view, errorCode, description, failingUrl);
        }

        public void onReceivedSslError(XWalkView view, ValueCallback<java.lang.Boolean> callback, SslError error) {
            Log.d(Constants.WEBVIEW_LOG_TAG, "Received SSL Error: " + error.toString());
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
            Log.d(Constants.WEBVIEW_LOG_TAG, "Scale changed.");
        }
    }


    // login request
    private class AsyncLogin extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            return doHttpPost(params[0],params[1],params[2],params[3]);
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
        post.addHeader("User-Agent","Smartick_Android");
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
        Log.d(Constants.WEBVIEW_LOG_TAG,"LAST_REDIRECT: " + last.toString());
        return last.toString();
    }

    private void doLogout(){
        audioPlayer.stop();
        webView.load(Constants.URL_LOGOUT,null);
    }

    private File downloadPDFFromUrl(String url){
        Log.d(Constants.WEBVIEW_LOG_TAG,"Downloading pdf from: " + url);

        File folder = new File(ctx.getExternalCacheDir(), "/smk_pdf/");
        if (!folder.exists()){
            folder.mkdirs();
        }

        String c = cookieManager.getCookie(Constants.URL_CONTEXT);

        File fileDL = new File(folder, "diploma.pdf");
        FileDownloader.download(url,fileDL,c);

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
            Log.d(Constants.WEBVIEW_LOG_TAG,"ShowPDF - File does not exist!");
        }
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
        if(!urlRedirect.contains("acceso")) {
            Log.d(Constants.WEBVIEW_LOG_TAG,"Login valid");
            webView.load(urlRedirect,null);
        } else {
            Log.d(Constants.WEBVIEW_LOG_TAG,"Login failed");
        }
    }

    //Switching activities
    private void toExit(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
}