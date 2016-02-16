package com.mobile.android.smartick.network;

import com.facebook.internal.Validate;

/**
 * Created by sbarrio on 3/2/16.
 */
public class ValidateSocialResponse {

    private String token;
    private String type;
    private String result;
    private String email;

    private ValidateSocialResponse() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
