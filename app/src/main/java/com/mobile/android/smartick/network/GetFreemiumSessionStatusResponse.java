package com.mobile.android.smartick.network;

/**
 * Created by sbarrio on 22/04/15.
 */
public class GetFreemiumSessionStatusResponse {

    private String installationId;
    private String lastSessionDate;
    private Boolean sessionFinished;

    private GetFreemiumSessionStatusResponse(){}

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public String getLastSessionDate() {
        return lastSessionDate;
    }

    public void setLastSessionDate(String lastSessionDate) {
        this.lastSessionDate = lastSessionDate;
    }

    public Boolean getSessionFinished() {
        return sessionFinished;
    }

    public void setSessionFinished(Boolean sessionFinished) {
        this.sessionFinished = sessionFinished;
    }
}
