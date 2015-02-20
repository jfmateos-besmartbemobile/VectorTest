package com.mobile.android.smartick.pojos;

import android.content.Context;
import android.content.SharedPreferences;

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
        SharedPreferences install = context.getSharedPreferences(Constants.INSTALLATION_ID_FILE, Context.MODE_PRIVATE);
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
        return System.getProperty("os.version");
    }

    private String obtainDevice(){
        return android.os.Build.DEVICE;
    }

    private String obtainVersion(){
        return System.getProperty("versionName");
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
