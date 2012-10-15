package com.smartick;

import android.app.Activity;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


public class MainActivity extends Activity {

	private ProgressBar progressBar;
	private WebView webView;
	private Menu menu;
	
	private static final String URL_CONTEXT = "http://10.0.2.2/";
	private static final String URL_SMARTICK_ACCESO = URL_CONTEXT+"acceso.html";
	private static final String URL_LOGOUT = URL_CONTEXT+"smartick_logout";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);

        webView.loadUrl(URL_SMARTICK_ACCESO);

        setWebClientOptions();
        
        overrideWebClientMethods();
        
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
        prepareProgressBar();
		
    }
    
    private void setWebClientOptions(){
        webView.setPadding(0, 0, 0, 0);
        webView.setInitialScale(ScreenUtils.getScale(getWindowManager()));
        
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
				return false;
			}	
			
			/*Ignora problemas certificado*/
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			    handler.proceed();
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.activity_main, menu);
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
            	exit();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private boolean enableMenuLogout(){
    	return webView.getUrl().contains("/alumno/");
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
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
    	webView.loadUrl(URL_LOGOUT);
    }
    
}