package com.mobile.android.smartick.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.mobile.android.smartick.R;
import com.mobile.android.smartick.network.RegisterAndroidGCMTokenResponse;
import com.mobile.android.smartick.network.SmartickAPI;
import com.mobile.android.smartick.network.SmartickRestClient;
import com.mobile.android.smartick.pojos.SystemInfo;
import com.mobile.android.smartick.util.Constants;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SmartickRegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private SystemInfo sysInfo;

    public SmartickRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(Constants.SENT_GCM_TOKEN_PREF_NAME, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Constants.GCM_REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {

        //register received token with server
        sysInfo = new SystemInfo(this.getApplicationContext());
        final Context ctx = this.getApplicationContext();

        SmartickRestClient.get().registerAndroidGCMToken(
                sysInfo.getInstallationId(),
                token,
                new Callback<RegisterAndroidGCMTokenResponse>() {
                    @Override
                    public void success(RegisterAndroidGCMTokenResponse registerAndroidGCMTokenResponse, Response response) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
                        if (registerAndroidGCMTokenResponse.getStatus().equals(SmartickAPI.REGISTER_GCM_OK)) {
                            sharedPreferences.edit().putBoolean(Constants.SENT_GCM_TOKEN_PREF_NAME, true).apply();
                        }else{
                            sharedPreferences.edit().putBoolean(Constants.SENT_GCM_TOKEN_PREF_NAME, false).apply();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
                        sharedPreferences.edit().putBoolean(Constants.SENT_GCM_TOKEN_PREF_NAME, false).apply();
                    }
                }
        );
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

}