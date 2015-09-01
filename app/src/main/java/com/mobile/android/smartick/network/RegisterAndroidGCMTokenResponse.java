package com.mobile.android.smartick.network;

/**
 * Created by sbarrio on 1/9/15.
 */
public class RegisterAndroidGCMTokenResponse {

    private String installationId;
    private String gcmToken;
    private String status;

    private RegisterAndroidGCMTokenResponse(){}

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
