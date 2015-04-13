package com.mobile.android.smartick.network;

import com.mobile.android.smartick.pojos.SystemInfo;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Streaming;

/**
 * Created by sbarrio on 09/04/15.
 */

public interface SmartickAPI {

    //ASYNC

    @GET("/loginMobile.html")
    public void getLoginStatus(@Query("user") String user, @Query("password") String password, Callback<LoginStatusResponse> callback);

    @GET("/getAvatarImageForUser.html")
    public void getAvatarImageForUser(@Query("username") String user, Callback<GetAvatarImageForUserResponse> callback);

    @GET("/diploma.html")
    @Streaming
    void getDiploma(@Query("idDiploma") String id,@Path("filename") String filename,Callback<Response> callback);

    //SYNC

    @GET("/loginMobile.html")
    public LoginStatusResponse getLoginStatus(@Query("user") String user, @Query("password") String password);

    @GET("/getAvatarImageForUser.html")
    public GetAvatarImageForUserResponse getAvatarImageForUser(@Query("username") String user);
}
