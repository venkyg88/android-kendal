package com.staples.mobile;

import android.app.Activity;
import android.app.Application;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    private static final RestAdapter.LogLevel LOGLEVEL = RestAdapter.LogLevel.BASIC;

    private static final String USER_AGENT = "Staples Mobile App 1.0";

    private static final int TIMEOUT = 15; // Seconds

    private OkClient okClient;
    private EasyOpenApi easyOpenApi;
    private EasyOpenApi mockEasyOpenApi;

    @Override
    public void onCreate() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
        okClient = new OkClient(okHttpClient);
    }

    // EasyOpen API

    public EasyOpenApi getEasyOpenApi() {
        if (easyOpenApi!=null) return(easyOpenApi);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);
        builder.setEndpoint(EasyOpenApi.SERVICE_ENDPOINT);
        builder.setRequestInterceptor(new EasyOpenInterceptor());
        builder.setLogLevel(LOGLEVEL);
        builder.setLog(new AndroidLog(TAG));
        RestAdapter adapter = builder.build();

        easyOpenApi = adapter.create(EasyOpenApi.class);
        return(easyOpenApi);
    }

    private static class EasyOpenInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", "application/json");
//            request.addHeader("Connection", "Keep-Alive");
        }
    }

    public EasyOpenApi getMockEasyOpenApi() {
        if (mockEasyOpenApi!=null) return(mockEasyOpenApi);
        mockEasyOpenApi = new MockEasyOpenApi(getApplicationContext());
        return(mockEasyOpenApi);
    }
}