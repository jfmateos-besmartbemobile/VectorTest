package com.mobile.android.smartick.network;

import com.mobile.android.smartick.pojos.SystemInfo;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
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

    public final String LOGIN_VALID = "login_valid";
    public final String LOGIN_NO_ACTIVE_SUB = "no_active_subscription";
    public final String LOGIN_INVALID = "login_invalid";
    public final String PASSWORD_INVALID = "password_invalid";
    public final String REGISTER_OK = "OK";
    public final String REGISTER_KO = "KO";
    public final String REGISTER_GCM_OK = "OK";
    public final String REGISTER_GCM_KO = "KO";
    public final String USER_ACTIVE = "ACTIVE";
    public final String USER_INACTIVE = "INACTIVE";
    public final String UNKNOWN_USER = "UNKNOWN_USER";
    public final int MIN_USERNAME_LENGTH = 3;
    public final int MIN_PASSWORD_LENGTH = 4;

    //ASYNC

    @FormUrlEncoded
    @POST("/getAvatarImageForUser.html")
    public void getAvatarImageForUser(@Field("username") String user,
                                      Callback<GetAvatarImageForUserResponse> callback);

    @FormUrlEncoded
    @POST("/loginMobile.html")
    public void getLoginStatus(@Field("user") String user,
                              @Field("password")String password,
                              @Field("installationId") String installationID,
                              @Field("device") String device,
                              @Field("version") String version,
                              @Field("osVersion") String osVersion,
                              Callback<LoginStatusResponse> callback);

    @FormUrlEncoded
    @POST("/checkUserMobileActive.html")
    public void checkUserMobileActive(@Field("user") String user,
                                      @Field("installationId") String installationId,
                                      Callback<CheckUserMobileActiveResponse> callback);

    @FormUrlEncoded
    @POST("/registerAlumnoMobile.html")
    public void registerAlumnoMobile(
            @Field("username") String username,
            @Field("password") String password,
            @Field("nombre") String nombre,
            @Field("apellidos") String apellidos,
            @Field("diaNacimiento") String diaNacimiento,
            @Field("mesNacimiento") String mesNacimiento,
            @Field("anioNacimiento") String anioNacimiento,
            @Field("genero") String genero,
            @Field("tutorUsername") String tutorUsername,
            @Field("alumnoLocale") String alumnoLocale,
            @Field("installationId") String installationID,
            @Field("device") String device,
            @Field("version") String version,
            @Field("osVersion") String osVersion,
            Callback<RegisterAlumnoResponse> callback);

    @FormUrlEncoded
    @POST("/registerTutorMobile.html")
    public void registerTutorMobile(
            @Field("tutorMail") String tutorMail,
            @Field("password") String password,
            @Field("nombre") String nombre,
            @Field("apellidos") String apellidos,
            @Field("telefono") String telefono,
            @Field("tutorLocale") String tutorLocale,
            @Field("installationId") String installationID,
            @Field("device") String device,
            @Field("version") String version,
            @Field("osVersion") String osVersion,
            Callback<RegisterTutorResponse> callback);

    @FormUrlEncoded
    @POST("/getFreemiumSessionStatus.html")
    public void getFreemiumSessionStatus(
            @Field("installationId") String installationId,
            Callback<GetFreemiumSessionStatusResponse> callback);

    @FormUrlEncoded
    @POST("/clearFreemiumSession.html")
    public void clearFreemoumSessionStatus(
            @Field("installationId") String installationId,
            Callback<ClearFreemiumSessionResponse> callback);

    @FormUrlEncoded
    @POST("/registerAndroidGCMToken.html")
    public void registerAndroidGCMToken(
            @Field("installationId") String installationId,
            @Field("gcmToken") String gcmToken,
            Callback<RegisterAndroidGCMTokenResponse> callback);

    //SYNC

    @FormUrlEncoded
    @POST("/getAvatarImageForUser.html")
    public GetAvatarImageForUserResponse getAvatarImageForUser(@Field("username") String user);

    @FormUrlEncoded
    @POST("/loginMobile.html")
    public LoginStatusResponse getLoginStatus(@Field("user") String user,
                                              @Field("password")String password,
                                              @Field("installationID") String installationID,
                                              @Field("device") String device,
                                              @Field("version") String version,
                                              @Field("osVersion") String osVersion);

    @FormUrlEncoded
    @POST("/checkUserMobileActive.html")
    public CheckUserMobileActiveResponse checkUserMobileActive(@Field("user") String user,
                                      @Field("installationId") String installationId);

    @FormUrlEncoded
    @POST("/registerAlumnoMobile.html")
    public RegisterAlumnoResponse registerAlumnoMobile(
            @Field("username") String username,
            @Field("password") String password,
            @Field("nombre") String nombre,
            @Field("apellidos") String apellidos,
            @Field("diaNacimiento") String diaNacimiento,
            @Field("mesNacimiento") String mesNacimiento,
            @Field("anioNacimiento") String anioNacimiento,
            @Field("genero") String genero,
            @Field("tutorUsername") String tutorUsername,
            @Field("alumnoLocale") String alumnoLocale,
            @Field("installationId") String installationID,
            @Field("device") String device,
            @Field("version") String version,
            @Field("osVersion") String osVersion);

    @FormUrlEncoded
    @POST("/registerTutorMobile.html")
    public RegisterTutorResponse registerTutorMobile(
            @Field("tutorMail") String tutorMail,
            @Field("password") String password,
            @Field("nombre") String nombre,
            @Field("apellidos") String apellidos,
            @Field("telefono") String telefono,
            @Field("tutorLocale") String tutorLocale,
            @Field("installationId") String installationID,
            @Field("device") String device,
            @Field("version") String version,
            @Field("osVersion") String osVersion);


    @FormUrlEncoded
    @POST("/getFreemiumSessionStatus.html")
    public GetFreemiumSessionStatusResponse getFreemiumSessionStatus(
            @Field("installationId") String installationId);

    @FormUrlEncoded
    @POST("/clearFreemiumSession.html")
    public ClearFreemiumSessionResponse clearFreemoumSessionStatus(
            @Field("installationId") String installationId);

    @FormUrlEncoded
    @POST("/registerAndroidGCMToken.html")
    public RegisterAndroidGCMTokenResponse registerAndroidGCMToken(
            @Field("installationId") String installationId,
            @Field("gcmToken") String gcmToken);
}
