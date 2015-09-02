package com.mobile.android.smartick.network;

/**
 * Created by sbarrio on 2/9/15.
 */
public class CheckUserMobileActiveResponse {

    private String user;
    private String installationId;
    private String response;

    private CheckUserMobileActiveResponse() {}

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
