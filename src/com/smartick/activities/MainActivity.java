package com.smartick.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.smartick.utils.Constants;
import com.smartick.utils.NetworkUtils;
import com.smartick.utils.ScreenUtils;


public class MainActivity extends Activity {

	private ProgressBar progressBar;
	private WebView webView;
	private Menu menu;
	
	private String url;
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if(!NetworkUtils.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))){
        	toOfflineActivity();
        }else{
            Bundle b = getIntent().getExtras();
            url = b.getString("url");
	        setContentView(R.layout.activity_main);
	        webView = (WebView) findViewById(R.id.webview);
	        new SmartickViewTask().execute();
			progressBar = (ProgressBar) findViewById(R.id.progressbar);
	        prepareProgressBar();
        }
    }
    
    private void setWebClientOptions(){
        webView.setPadding(0, 0, 0, 0);
        webView.setInitialScale(ScreenUtils.getScale(getWindowManager(), webView.getUrl()));
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
    }

    private void prepareProgressBar(){
		webView.setWebChromeClient(new WebChromeClient(){
			/*Barra de carga*/
			@Override
		    public void onProgressChanged(WebView view, int progress){				
			    progressBar.setProgress(0);
			    progressBar.setVisibility(View.VISIBLE);
			    MainActivity.this.setProgress(progress * 1000);

		        progressBar.incrementProgressBy(progress);

		        if(progress == 100){
		        	progressBar.setVisibility(View.GONE);
		        }
		    }
		});
    }
    
    private void overrideWebClientMethods(){
        webView.setWebViewClient(new WebViewClient(){
        	/*Para que no se abra en un navegador*/
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				if(url.equals(Constants.URL_LOGOUT)){
					toLoginActivity();
				}
				return false;
			}	
			
			/*Ignora problemas certificado*/
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			    handler.proceed();
			}
			/*Captura cuando empieza la página*/
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				view.setInitialScale(ScreenUtils.getScale(getWindowManager(), url));
				super.onPageStarted(view, url, favicon);
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        this.menu = menu;
        getMenuInflater().inflate(R.menu.activity_main, menu);
        menu.findItem(R.id.menu_logout).setEnabled(enableMenuLogout());
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_logout:
                logout();
                return true;
            case R.id.menu_exit:
            	exit();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private boolean enableMenuLogout(){
    	return webView.getUrl().contains(Constants.PATH_ALUMNO);
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e){
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
            	if(menu != null){
            		menu.findItem(R.id.menu_logout).setEnabled(enableMenuLogout());
            	}
                return super.onKeyDown(keycode, e);
        }
        return super.onKeyDown(keycode, e);
    }

    private void exit(){
    	Intent intent = new Intent(Intent.ACTION_MAIN);
    	intent.addCategory(Intent.CATEGORY_HOME);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(intent);
    }
    
    private void logout(){
    	webView.loadUrl(Constants.URL_LOGOUT);
    }
    
    private void toOfflineActivity(){
    	Intent intent = new Intent(this, OfflineActivity.class);
    	startActivity(intent);
    }
    
    private void toLoginActivity(){
    	Intent intent = new Intent(this, LoginActivity.class);
    	startActivity(intent);
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	/*Clase para implementar los métodos del webview*/
	private class SmartickViewTask extends AsyncTask<Void, Void, Boolean> {  
        CookieManager cookieManager;  
  
        @Override  
        protected void onPreExecute() {  
            CookieSyncManager.createInstance(MainActivity.this);  
            cookieManager = CookieManager.getInstance();  
            cookieManager.removeSessionCookie();   
            super.onPreExecute();  
        }  
        
        protected Boolean doInBackground(Void... param) {  
            //Workaround porque hay un problema al pasar una cookie al webview : http://walletapp.net/es/cookbook/android-passing-cookie-to-webview
            SystemClock.sleep(1000);  
            return false;  
        }  
        
        @SuppressLint("NewApi")
		@Override  
        protected void onPostExecute(Boolean result) {
	        setWebClientOptions();
	        overrideWebClientMethods();
        	if(url == null || url.toString().isEmpty()){
    	        webView.loadUrl(Constants.URL_SIGNIN);
        	}else{
                cookieManager.setCookie(Constants.URL_CONTEXT,"JSESSIONID"+url.substring(url.lastIndexOf("=")));
                CookieSyncManager.getInstance().sync();  
    	        webView.loadUrl(url);
        	}
        } 
	}

	
}