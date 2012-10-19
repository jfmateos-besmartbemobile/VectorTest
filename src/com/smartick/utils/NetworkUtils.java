package com.smartick.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
	
    public static boolean isOnline(ConnectivityManager connectivityManager){
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

}