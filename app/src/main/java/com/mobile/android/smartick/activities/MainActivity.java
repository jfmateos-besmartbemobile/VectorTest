package com.mobile.android.smartick.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.data.UsersDBHandler;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.pojos.User;
import com.mobile.android.smartick.util.Constants;
import com.mobile.android.smartick.util.Network;
import com.mobile.android.smartick.util.RedirectHandler;
import com.mobile.android.smartick.util.ScreenUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	private ProgressBar progressBar;
	private WebView webView;
	private Menu menu;

	private String url;
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
            //retreives parameters from intent
			Bundle b = getIntent().getExtras();
			url = b.getString("url");

            //retreives username and password from bundle
			username = b.getString("username");
            password = b.getString("password");

            //initializaes systemInfo
            sysInfo = new SystemInfo(this.getApplicationContext());

			setContentView(R.layout.activity_main);

			webView = (WebView) findViewById(R.id.webview);

            //preapres webView settings and overrides event listener methods
            setWebClientOptions();
            overrideWebClientMethods();

            new AsyncLogin().execute(Constants.URL_SMARTICK_LOGIN,username,password,sysInfo.getInstallationId());

            //sets progress bar
			progressBar = (ProgressBar) findViewById(R.id.progressbar);
			prepareProgressBar();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menu_logout).setEnabled(enableMenuLogout());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_logout:
                logout();
                return true;
            case R.id.menu_exit:
                toExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (menu != null) {
                    menu.findItem(R.id.menu_logout).setEnabled(enableMenuLogout());
                }
                return super.onKeyDown(keycode, e);
        }
        return super.onKeyDown(keycode, e);
    }

    //WebView setttings and control
	private void setWebClientOptions() {
		webView.setPadding(0, 0, 0, 0);
		webView.setInitialScale(ScreenUtils.getScale(getWindowManager(),
				webView.getUrl()));

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setUseWideViewPort(false);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setSupportZoom(false);
		webSettings.setBuiltInZoomControls(false);

		webView.addJavascriptInterface(new SmartickJavaScriptInterface(), "smartick");
	}

    private void overrideWebClientMethods() {
        final Activity activity = this;
        webView.setWebViewClient(new WebViewClient() {
            /* Para que no se abra en un navegador */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(Constants.WEBVIEW_LOG_TAG,"shouldIverrideUrlLoading - url: " + url);
                if (url.equals(Constants.URL_LOGOUT)) {
                    toLogin();
                }
                return false;
            }

            /* Ignora problemas certificado */
            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                Log.d(Constants.WEBVIEW_LOG_TAG,"onReceivedSSLError: " + error);
                handler.proceed();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                Log.d(Constants.WEBVIEW_LOG_TAG,"onReceivedError url: " + failingUrl + " description: " + description);
            }

            /* Captura cuando empieza la página */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(Constants.WEBVIEW_LOG_TAG,"onPageStarted url: " + url);
                view.setInitialScale(ScreenUtils.getScale(getWindowManager(),
                        url));
                super.onPageStarted(view, url, favicon);
            }

            /*	Obtiene el valor src de la imagen del avatar desde home */
            @Override
            public void onPageFinished(WebView view, String url){
                Log.d(Constants.WEBVIEW_LOG_TAG,"onPageFinished url: " + url);
            }
        });
    }

    private boolean enableMenuLogout() {
        return webView.getUrl().contains(Constants.PATH_ALUMNO);
    }

    private void logout() {
        webView.loadUrl(Constants.URL_LOGOUT);
    }

    //WebView javascript interview
    public class SmartickJavaScriptInterface{
        // This annotation is required in Jelly Bean and later:
        //@JavascriptInterface
        public void receiveValueFromJs(String str) {
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
        RedirectHandler handler = new RedirectHandler();
        httpClient.setRedirectHandler(handler);

        HttpPost post = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("j_username", username));
        nvps.add(new BasicNameValuePair("j_password", password));

        // id de dispositivo para la visualizacion adaptada a tablets
        post.addHeader("android-app", installationId);

        //User agent para app Android
        post.addHeader("User-Agent","Smartick_Android");

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
            Log.d(Constants.WEBVIEW_LOG_TAG,"Login valid");
            webView.loadUrl(urlRedirect);
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

    private void toLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void toOffline() {
        startActivity(new Intent(this, OfflineActivity.class));
    }


    //Progress Bar
    private void prepareProgressBar() {
        webView.setWebChromeClient(new WebChromeClient() {
            /* Barra de carga */
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
                MainActivity.this.setProgress(progress * 1000);

                progressBar.incrementProgressBy(progress);

                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Context getMainContext(){
        return this;
    }

}