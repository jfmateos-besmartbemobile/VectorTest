package com.smartick;

import android.app.Activity;
import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

	private ProgressBar progressBar;
	private WebView webView;
	
	private static final String URL_SMARTICK_ACCESO = "http://10.0.2.2/acceso.html";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);
        
        setWebClientOptions();
        
        overrideWebClientMethods();

        webView.loadUrl(URL_SMARTICK_ACCESO);
        
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
        prepareProgressBar();
		
    }
    
    private void setWebClientOptions(){
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
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
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
