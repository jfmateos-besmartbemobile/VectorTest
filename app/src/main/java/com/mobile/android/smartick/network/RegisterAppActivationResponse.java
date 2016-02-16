package com.mobile.android.smartick.network;

/**
 * Created by sbarrio on 25/1/16.
 */
public class RegisterAppActivationResponse {
    private String installationId;
    private String app;
    private String device;
    private String appVersion;
    private String reponse;

    private RegisterAppActivationResponse(){}

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }
}
