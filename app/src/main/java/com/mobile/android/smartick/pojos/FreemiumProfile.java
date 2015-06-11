package com.mobile.android.smartick.pojos;

import android.content.Context;
import android.content.SharedPreferences;

import com.mobile.android.smartick.util.Constants;

/**
 * Created by sbarrio on 08/06/15.
 */
public class FreemiumProfile {

    private int avatar;
    private int age;
    private Context context;

    public FreemiumProfile(Context context){
        this.context = context;
        this.avatar = obtainFreemiumAvatar();
        this.age = obtainFreemiumAge();
    }

    private int obtainFreemiumAvatar(){
        SharedPreferences sysPrefs = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);
        return sysPrefs.getInt(Constants.FREEMIUM_AVATAR_PREF_NAME,Constants.DEFAULT_FREEMIUM_AVATAR);
    }

    private int obtainFreemiumAge(){
        SharedPreferences sysPrefs = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);
        return sysPrefs.getInt(Constants.FREEMIUM_AGE_PREF_NAME,Constants.DEFAULT_FREEMIUM_AGE);
    }

    public void storeFreemiumAvatar(int avatar){
        SharedPreferences sysPrefs = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sysPrefs.edit();
        editor.putInt(Constants.FREEMIUM_AVATAR_PREF_NAME, avatar);
        editor.commit();
    }

    public void storeFreemiumAge(int age){
        SharedPreferences sysPrefs = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sysPrefs.edit();
        editor.putInt(Constants.FREEMIUM_AGE_PREF_NAME, age);
        editor.commit();
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
