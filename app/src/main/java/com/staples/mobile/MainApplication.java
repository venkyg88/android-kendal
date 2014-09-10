package com.staples.mobile;

import android.app.Application;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    private static final String SERVER = "http://sapi.staples.com";

    private RestAdapter easyOpenApi;

    @Override
    public void onCreate() {
        OkHttpClient okHttpClient = new OkHttpClient();
        OkClient okClient = new OkClient(okHttpClient);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);
        builder.setEndpoint(SERVER);
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        builder.setLog(new AndroidLog(TAG));
        easyOpenApi = builder.build();
    }

    public RestAdapter getEasyOpenApi() {
        return(easyOpenApi);
    }
}
