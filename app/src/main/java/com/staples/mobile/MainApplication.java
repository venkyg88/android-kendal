package com.staples.mobile;

import android.app.Application;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    private static final String SERVER = "http://sapi.staples.com";
    private static RestAdapter.LogLevel LOGLEVEL = RestAdapter.LogLevel.BASIC;

    private OkClient okClient;
    private EasyOpenApi easyOpenApi;

    @Override
    public void onCreate() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okClient = new OkClient(okHttpClient);

        createEasyOpenApi();
    }

    // EasyOpen API

    private void createEasyOpenApi() {
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);
        builder.setEndpoint(SERVER);
        builder.setRequestInterceptor(new EasyOpenInterceptor());
        builder.setLogLevel(LOGLEVEL);
        builder.setLog(new AndroidLog(TAG));
        RestAdapter adapter = builder.build();

        easyOpenApi = adapter.create(EasyOpenApi.class);
    }

    private static class EasyOpenInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("Accept", "application/json");
//            request.addHeader("Accept-encoding", "gzip");
            request.addHeader("Connection", "Keep-Alive");
        }
    }

    public EasyOpenApi getEasyOpenApi() {
        return(easyOpenApi);
    }
}
