package com.mobile.android.smartick.network;

/**
 * Created by imac on 09/04/15.
 */
public class GetAvatarImageForUserResponse {

    private String username;
    private String urlAvatar;

    private GetAvatarImageForUserResponse(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrlAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        this.urlAvatar = urlAvatar;
    }
}
