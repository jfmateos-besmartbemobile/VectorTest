package com.mobile.android.smartick.network;

/**
 * Created by sbarrio on 22/04/15.
 */
public class ClearFreemiumSessionResponse {

    private String installationId;
    private Boolean deleted = null;

    private ClearFreemiumSessionResponse(){}

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
