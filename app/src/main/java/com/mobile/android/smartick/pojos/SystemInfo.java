package com.mobile.android.smartick.pojos;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.mobile.android.smartick.util.Constants;

/**
 * Created by sbarrio on 19/02/15.
 */
public class SystemInfo {

    private String installationId;
    private String device;
    private String osVersion;
    private String version;
    private Context context;

    public SystemInfo(Context context){
        this.context = context;
        this.installationId = obtainInstallationId();
        this.osVersion = obtainOsVersion();
        this.version = obtainVersion();
        this.device = obtainDevice();
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
}
