package com.smartick.activities;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

import com.smartick.pojos.ListUser;
import com.smartick.utils.Constants;
import com.smartick.utils.NetworkUtils;
import com.smartick.utils.ScreenUtils;
import com.smartick.utils.usersDBHandler;

public class MainActivity extends Activity {

	private ProgressBar progressBar;
	private WebView webView;
	private Menu menu;

	private String url;
	private String username;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// comprueba estado de red usando objeto NetworkUtils
		if (!NetworkUtils
				.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
			NetworkUtils.toOfflineActivity(this);
		} else {
			Bundle b = getIntent().getExtras();
			url = b.getString("url");
			username = b.getString("username");
			setContentView(R.layout.activity_main);
			webView = (WebView) findViewById(R.id.webview);
			new SmartickViewTask().execute();
			progressBar = (ProgressBar) findViewById(R.id.progressbar);
			prepareProgressBar();
		}
	}

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
	
	
   public class SmartickJavaScriptInterface{
        // This annotation is required in Jelly Bean and later:
        //@JavascriptInterface
        public void receiveValueFromJs(String str) {
        	if (str!= null && str.length()>0){
        		guardarUrlAvatarAlumno(str);
        	}
       }
    }
   

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

	private void overrideWebClientMethods() {
		final Activity activity = this;
		webView.setWebViewClient(new WebViewClient() {
			/* Para que no se abra en un navegador */
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.equals(Constants.URL_LOGOUT)) {
					NetworkUtils.toLoginActivity(activity);
				}
				return false;
			}

			/* Ignora problemas certificado */
			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

			/* Captura cuando empieza la página */
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				view.setInitialScale(ScreenUtils.getScale(getWindowManager(),
						url));
				super.onPageStarted(view, url, favicon);
			}
			
			/*	Obtiene el valor src de la imagen del avatar desde home */
			@Override
			  public void onPageFinished(WebView view, String url){
				view.loadUrl("javascript:androidGetAvatarSrc()");
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
			NetworkUtils.exit(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean enableMenuLogout() {
		return webView.getUrl().contains(Constants.PATH_ALUMNO);
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

	private void logout() {
		webView.loadUrl(Constants.URL_LOGOUT);
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
	
	private void guardarUrlAvatarAlumno(String url){
		//obtener nombre archivo de la url
		File f = new File(url);
		String imgName = f.getName();
		//buscamos user en db, si no hay avatar lo insertamos
		usersDBHandler db = new usersDBHandler(getMainContext());
	    ListUser user = db.getUser(username);
	    if (user.getAvatarUrl()==null || user.getAvatarUrl().isEmpty()){
		    user.setAvatarUrl(imgName);
		    db.updateUser(user);
	    }
	    db.close();
	}
	

	/* Clase para implementar los métodos del webview */
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
			// Workaround porque hay un problema al pasar una cookie al webview
			// :
			// http://walletapp.net/es/cookbook/android-passing-cookie-to-webview
			SystemClock.sleep(1000);
			return false;
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Boolean result) {
			setWebClientOptions();
			overrideWebClientMethods();
			if (url == null || url.toString().isEmpty()) {
				webView.loadUrl(Constants.URL_SIGNIN);
			} else {
				cookieManager.setCookie(Constants.URL_CONTEXT, "JSESSIONID"
						+ url.substring(url.lastIndexOf("=")));
				CookieSyncManager.getInstance().sync();
				webView.loadUrl(url);
			}
		}
	}
	

}