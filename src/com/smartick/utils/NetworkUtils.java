package com.smartick.utils;

import com.smartick.activities.LoginActivity;
import com.smartick.activities.OfflineActivity;

import android.app.Activity;
import android.content.Intent;
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
    
    public static void toOfflineActivity(Activity activity){
    	Intent intent = new Intent(activity, OfflineActivity.class);
    	activity.startActivity(intent);
    }
    
    public static void toLoginActivity(Activity activity){
    	Intent intent = new Intent(activity, LoginActivity.class);
    	activity.startActivity(intent);
    }

    public static void exit(Activity activity){
    	Intent intent = new Intent(Intent.ACTION_MAIN);
    	intent.addCategory(Intent.CATEGORY_HOME);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	activity.startActivity(intent);
    }
}