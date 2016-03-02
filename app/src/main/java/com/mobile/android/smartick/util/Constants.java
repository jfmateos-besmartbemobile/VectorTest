package com.mobile.android.smartick.util;

/**
 * Created by gorgue on 30/01/2015.
 */
public class Constants {

    public static final boolean DEBUG_MODE = false;

    public static final String URL_CONTEXT_PROD = "https://www.smartick.es"; //PROD ENVIRONMENT
    public static final String URL_CONTEXT_PRE = "http://dev.smartick.es:88";  //DEV PRE ENVIROMENT
    public static final String URL_CONTEXT_DEV = "http://192.168.1.12";  //DEV LOCAL ENVIROMENT

    public static final String URL_SMARTICK_LOGIN = "/smartick_login";
    public static final String URL_SMARTICK_SOCIAL_LOGIN = "/acceso!validateSocial.html";
    public static final String URL_SMARTICK_LOGIN_FREEMIUM = "/loginMobileFreemium.html";
    public static final String URL_LOGOUT = "/smartick_logout";
    public static final String URL_FREEMIUM_IMAGE = "/images/freemium/";

    public static final String WELCOME_LOG_TAG ="welcome_log_tag";
    public static final String FREEMIUM_LOG_TAG ="freemium_log_tag";
    public static final String  LOGIN_LOG_TAG ="login_log_tag";
    public static final String REGISTER_LOG_TAG ="register_log_tag";
    public static final String WEBVIEW_LOG_TAG = "webView_log_tag";
    public static final String INTRO_LOG_TAG = "intro_log_tag";
    public static final String AUDIO_LOG_TAG = "audio_log_tag";
    public static final String USER_LIST_TAG ="user_list_tag";
    public static final String FILE_DL_TAG = "file_dl_tag";
    public static final String SYSINFO_LOG_TAG = "sysinfo_log_tag";
    public static final String YTPLAYER_LOG_TAG = "ytplayer_log_tag";

    public static final String SMARTICK_PREFS = "smartick_prefs";
    public static final String INSTALLATION_PREF_NAME = "install_id_pref";
    public static final String FIRST_TIME_PREF_NAME = "first_time_pref";
    public static final String FREEMIUM_AVATAR_PREF_NAME = "freemium_avatar_pref";
    public static final String FREEMIUM_AGE_PREF_NAME = "freemium_age_pref";
    public static final String SENT_GCM_TOKEN_PREF_NAME = "sent_gcm_token";
    public static final String AUDIO_CACHE_LAST_REFRESH_DATE = "audio_cache_last_refresh_date";

    public static final String GCM_REGISTRATION_COMPLETE = "registrationComplete";


    public static final int DEFAULT_FREEMIUM_AVATAR = 1;
    public static final int DEFAULT_FREEMIUM_AGE = 9;
    public static final int MIN_FREEMIUM_AGE = 4;
    public static final int MAX_FREEMIUM_AGE = 14;
    public static final int CAN_READ_MIN_AGE = 7;
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int PASSWORD_MIN_LENGTH= 4;


    private static Constants instance;
    public String url_context = URL_CONTEXT_PROD;
    public String url_smartick_login = URL_CONTEXT_PROD + URL_SMARTICK_LOGIN;
    public String url_smartick_social_login = URL_CONTEXT_PROD + URL_SMARTICK_SOCIAL_LOGIN;
    public String url_smartick_login_freemium = URL_CONTEXT_PROD + URL_SMARTICK_LOGIN_FREEMIUM;
    public String url_logout = URL_CONTEXT_PROD + URL_LOGOUT;
    public String url_freemium_image = URL_CONTEXT_PROD + URL_FREEMIUM_IMAGE;


    public static Constants instance() {
        if (instance == null) {
            instance = new Constants();
            return instance;
        }
        return instance;
    }

    public String getUrl_context() {
        return url_context;
    }

    public void setUrl_context(String url_context) {
        this.url_context = url_context;
        this.url_freemium_image = url_context + URL_FREEMIUM_IMAGE;
        this.url_logout = url_context + URL_LOGOUT;
        this.url_smartick_login = url_context + URL_SMARTICK_LOGIN;
        this.url_smartick_social_login = url_context + URL_SMARTICK_SOCIAL_LOGIN;
        this.url_smartick_login_freemium = url_context + URL_SMARTICK_LOGIN_FREEMIUM;
    }

    public String getUrl_smartick_login() {
        return url_smartick_login;
    }

    public void setUrl_smartick_login(String url_smartick_login) {
        this.url_smartick_login = url_smartick_login;
    }

    public String getUrl_smartick_social_login() {
        return url_smartick_social_login;
    }

    public void setUrl_smartick_social_login(String url_smartick_social_login) {
        this.url_smartick_social_login = url_smartick_social_login;
    }

    public String getUrl_smartick_login_freemium() {
        return url_smartick_login_freemium;
    }

    public void setUrl_smartick_login_freemium(String url_smartick_login_freemium) {
        this.url_smartick_login_freemium = url_smartick_login_freemium;
    }

    public String getUrl_logout() {
        return url_logout;
    }

    public void setUrl_logout(String url_logout) {
        this.url_logout = url_logout;
    }

    public String getUrl_freemium_image() {
        return url_freemium_image;
    }

    public void setUrl_freemium_image(String url_freemium_image) {
        this.url_freemium_image = url_freemium_image;
    }
}
