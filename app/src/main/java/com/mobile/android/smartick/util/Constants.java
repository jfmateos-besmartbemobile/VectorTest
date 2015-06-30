package com.mobile.android.smartick.util;

/**
 * Created by gorgue on 30/01/2015.
 */
public class Constants {




//    public static final String URL_CONTEXT = "http://www.smartick.es";
    public static final String URL_CONTEXT = "http://10.0.0.12";
    public static final String URL_SMARTICK_LOGIN = URL_CONTEXT + "/smartick_login";
    public static final String URL_SMARTICK_LOGIN_FREEMIUM = URL_CONTEXT + "/loginMobileFreemium.html";
    public static final String LOGIN_MOBILE_SERVICE = URL_CONTEXT + "/loginMobile.html?user={username}&password={password}";
    public static final String URL_LOGOUT = URL_CONTEXT + "/smartick_logout";
    public static final String URL_FREEMIUM_IMAGE = URL_CONTEXT + "/images/freemium/";

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

    public static final String DEVICE_INFO_FILE = "device_info_file";
    public static final String SMARTICK_PREFS = "smartick_prefs";
    public static final String INSTALLATION_PREF_NAME = "install_id_pref";
    public static final String FREEMIUM_AVATAR_PREF_NAME = "freemium_avatar_pref";
    public static final String FREEMIUM_AGE_PREF_NAME = "freemium_age_pref";
    public static final String AUDIO_CACHE_LAST_REFRESH_DATE = "audio_cache_last_refresh_date";

    public static final int DEFAULT_FREEMIUM_AVATAR = 1;
    public static final int DEFAULT_FREEMIUM_AGE = 9;
    public static final int MIN_FREEMIUM_AGE = 4;
    public static final int MAX_FREEMIUM_AGE = 14;

}
