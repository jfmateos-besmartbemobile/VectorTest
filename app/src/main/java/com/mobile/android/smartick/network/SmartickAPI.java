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

    public final String LOGIN_VALID = "login_valid";
    public final String LOGIN_NO_ACTIVE_SUB = "no_active_subscription";
    public final String LOGIN_INVALID = "login_invalid";
    public final String PASSWORD_INVALID = "password_invalid";
    public final String REGISTER_OK = "OK";
    public final String REGISTER_KO = "KO";
    public final String REGISTER_GCM_OK = "OK";
    public final String REGISTER_GCM_KO = "KO";
    public final int MIN_USERNAME_LENGTH = 3;
    public final int MIN_PASSWORD_LENGTH = 4;

    //ASYNC

    @GET("/getAvatarImageForUser.html")
    public void getAvatarImageForUser(@Query("username") String user,
                                      Callback<GetAvatarImageForUserResponse> callback);

    @GET("/loginMobile.html")
    public void getLoginStatus(@Query("user") String user,
                              @Query("password")String password,
                              @Query("installationId") String installationID,
                              @Query("device") String device,
                              @Query("version") String version,
                              @Query("osVersion") String osVersion,
                              Callback<LoginStatusResponse> callback);

    @GET("/registerAlumnoMobile.html")
    public void registerAlumnoMobile(
            @Query("username") String username,
            @Query("password") String password,
            @Query("nombre") String nombre,
            @Query("apellidos") String apellidos,
            @Query("diaNacimiento") String diaNacimiento,
            @Query("mesNacimiento") String mesNacimiento,
            @Query("anioNacimiento") String anioNacimiento,
            @Query("genero") String genero,
            @Query("tutorUsername") String tutorUsername,
            @Query("alumnoLocale") String alumnoLocale,
            @Query("installationId") String installationID,
            @Query("device") String device,
            @Query("version") String version,
            @Query("osVersion") String osVersion,
            Callback<RegisterAlumnoResponse> callback);

    @GET("/registerTutorMobile.html")
    public void registerTutorMobile(
            @Query("tutorMail") String tutorMail,
            @Query("password") String password,
            @Query("nombre") String nombre,
            @Query("apellidos") String apellidos,
            @Query("telefono") String telefono,
            @Query("tutorLocale") String tutorLocale,
            @Query("installationId") String installationID,
            @Query("device") String device,
            @Query("version") String version,
            @Query("osVersion") String osVersion,
            Callback<RegisterTutorResponse> callback);

    @GET("/getFreemiumSessionStatus.html")
    public void getFreemiumSessionStatus(
            @Query("installationId") String installationId,
            Callback<GetFreemiumSessionStatusResponse> callback);

    @GET("/clearFreemiumSession.html")
    public void clearFreemoumSessionStatus(
            @Query("installationId") String installationId,
            Callback<ClearFreemiumSessionResponse> callback);

    @GET("/registerAndroidGCMToken.html")
    public void registerAndroidGCMToken(
            @Query("installationId") String installationId,
            @Query("gcmToken") String gcmToken,
            Callback<RegisterAndroidGCMTokenResponse> callback);

    //SYNC

    @GET("/getAvatarImageForUser.html")
    public GetAvatarImageForUserResponse getAvatarImageForUser(@Query("username") String user);


    @GET("/loginMobile.html")
    public LoginStatusResponse getLoginStatus(@Query("user") String user,
                                              @Query("password")String password,
                                              @Query("installationID") String installationID,
                                              @Query("device") String device,
                                              @Query("version") String version,
                                              @Query("osVersion") String osVersion);

    @GET("/registerAlumnoMobile.html")
    public RegisterAlumnoResponse registerAlumnoMobile(
            @Query("username") String username,
            @Query("password") String password,
            @Query("nombre") String nombre,
            @Query("apellidos") String apellidos,
            @Query("diaNacimiento") String diaNacimiento,
            @Query("mesNacimiento") String mesNacimiento,
            @Query("anioNacimiento") String anioNacimiento,
            @Query("genero") String genero,
            @Query("tutorUsername") String tutorUsername,
            @Query("alumnoLocale") String alumnoLocale,
            @Query("installationId") String installationID,
            @Query("device") String device,
            @Query("version") String version,
            @Query("osVersion") String osVersion);

    @GET("/registerTutorMobile.html")
    public RegisterTutorResponse registerAlumnoMobile(
            @Query("tutorMail") String tutorMail,
            @Query("password") String password,
            @Query("nombre") String nombre,
            @Query("apellidos") String apellidos,
            @Query("telefono") String telefono,
            @Query("tutorLocale") String tutorLocale,
            @Query("installationId") String installationID,
            @Query("device") String device,
            @Query("version") String version,
            @Query("osVersion") String osVersion);


    @GET("/getFreemiumSessionStatus.html")
    public GetFreemiumSessionStatusResponse getFreemiumSessionStatus(
            @Query("installationId") String installationId);

    @GET("/clearFreemiumSession.html")
    public ClearFreemiumSessionResponse clearFreemoumSessionStatus(
            @Query("installationId") String installationId);

    @GET("/registerAndroidGCMToken.html")
    public RegisterAndroidGCMTokenResponse registerAndroidGCMToken(
            @Query("installationId") String installationId,
            @Query("gcmToken") String gcmToken);

}
