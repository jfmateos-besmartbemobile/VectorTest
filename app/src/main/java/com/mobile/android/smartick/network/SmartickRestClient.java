package com.mobile.android.smartick.network;

import com.mobile.android.smartick.util.Constants;

import retrofit.RestAdapter;

/**
 * Created by sbarrio on 09/04/15.
 */
public class SmartickRestClient {

    private static SmartickAPI REST_CLIENT;
    private static String ROOT = Constants.URL_CONTEXT;

    static {
        setupRestClient();
    }

    private SmartickRestClient() {}

    public static SmartickAPI get() {
        return REST_CLIENT;
    }

    private static void setupRestClient() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.URL_CONTEXT)
                .build();

        REST_CLIENT = restAdapter.create(SmartickAPI.class);
    }
}
