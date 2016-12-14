package com.mobile.android.smartick.pojos;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.emma.android.eMMa;
import com.emma.android.eMMaUserInfoInterface;
import com.mobile.android.smartick.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by sbarrio on 19/02/15.
 */
public class SystemInfo {

    private String installationId;
    private String device;
    private String osVersion;
    private String version;
    private String locale;
    private String extra;
    private boolean firstTimeRunning;
    private Context context;

    public SystemInfo(Context context){
        this.context = context;
        this.installationId = obtainInstallationId();
        this.osVersion = obtainOsVersion();
        this.version = obtainVersion();
        this.device = obtainDevice();
        this.locale = obtainLocale();
        this.extra = obtainExtra();
        this.firstTimeRunning = obtainFirstTimeRunning();
    }

    private String obtainInstallationId(){
        if (context == null){
            return null;
        }
        SharedPreferences install = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);
        installationId = install.getString(Constants.INSTALLATION_PREF_NAME, null);
        if (installationId == null){
            installationId = Installation.id(context);
            SharedPreferences.Editor editor = install.edit();
            editor.putString(Constants.INSTALLATION_PREF_NAME, installationId);
            editor.commit();
        }
        return installationId;
    }

    private String obtainOsVersion(){
        return android.os.Build.VERSION.RELEASE;
    }

    private String obtainDevice(){
        return android.os.Build.DEVICE;
    }

    private String obtainVersion(){
        int versionCode = 0;
        String versionName = null;
        if (context == null){
            return null;
        }
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(Constants.SYSINFO_LOG_TAG, e.toString());
        }
        return versionName + "b" + versionCode;
    }

    private String obtainLocale(){
        return Locale.getDefault().toString();
    }

    private boolean obtainFirstTimeRunning(){
        if (context == null){
            return true;
        }
        SharedPreferences prefs = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);
        firstTimeRunning = prefs.getBoolean(Constants.FIRST_TIME_PREF_NAME, true);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.FIRST_TIME_PREF_NAME, false);
        editor.commit();

        return firstTimeRunning;
    }

    private String obtainExtra(){

        if (context == null){
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(Constants.EXTRA_KEY,null);
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isFirstTimeRunning() {
        return firstTimeRunning;
    }

    public void setFirstTimeRunning(boolean firstTimeRunning) {
        this.firstTimeRunning = firstTimeRunning;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {

        SharedPreferences extraEdit = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = extraEdit.edit();
        editor.putString(Constants.EXTRA_KEY, extra);
        editor.commit();

        this.extra = extra;
    }
}
