package com.staples.mobile.cfa;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.lms.api.LmsApi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    private static final RestAdapter.LogLevel LOGLEVEL = RestAdapter.LogLevel.BASIC;

    private static final String USER_AGENT = "Staples Mobile App 1.0";
    private static final int TIMEOUT = 15; // Seconds

    private static String token1;
    private static String token2;

    private OkClient okClient;
    private JacksonConverter converter;

    private EasyOpenApi easyOpenInsecureApi;
    private EasyOpenApi easyOpenSecureApi;
    private LmsApi lmsApi;
    private LmsApi mockLmsApi;

    @Override
    public void onCreate() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
        okClient = new OkClient(okHttpClient);

        converter = new JacksonConverter();
    }

    // Interceptors for standard HTTP headers

    private static class InsecureInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", "application/json");
//            request.addHeader("Connection", "Keep-Alive");
        }
    }

    private static class SecureInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", "application/json");
            request.addHeader("WCToken", token1);
            request.addHeader("WCTrustedToken", token2);
//            request.addHeader("Connection", "Keep-Alive");
        }
    }

    public static void setTokens(String wcToken, String wcTrustedToken) {
        token1 = wcToken;
        token2 = wcTrustedToken;
    }

    // EasyOpen API

    public EasyOpenApi getEasyOpenApi(boolean secure) {
        // Check for existing
        if (!secure) {
            if (easyOpenInsecureApi!=null) return(easyOpenInsecureApi);
        } else {
            if (easyOpenSecureApi!=null) return(easyOpenSecureApi);
        }

        // Build API
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);
        if (!secure) {
            builder.setEndpoint(EasyOpenApi.INSECURE_ENDPOINT);
            builder.setRequestInterceptor(new InsecureInterceptor());
        } else {
            builder.setEndpoint(EasyOpenApi.SECURE_ENDPOINT);
            builder.setRequestInterceptor(new SecureInterceptor());
        }
        builder.setConverter(converter);
        builder.setLogLevel(LOGLEVEL);
        builder.setLog(new AndroidLog(TAG));
        RestAdapter adapter = builder.build();
        EasyOpenApi api = adapter.create(EasyOpenApi.class);

        // Save for later use
        if (!secure) easyOpenInsecureApi = api;
        else easyOpenSecureApi = api;
        return(api);
    }

    // LMS API

    public LmsApi getLmsApi() {
        if (lmsApi!=null) return(lmsApi);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);
        builder.setEndpoint(LmsApi.INSECURE_ENDPOINT);
        builder.setRequestInterceptor(new InsecureInterceptor());
        builder.setConverter(converter);
        builder.setLogLevel(LOGLEVEL);
        builder.setLog(new AndroidLog(TAG));
        RestAdapter adapter = builder.build();

        lmsApi = adapter.create(LmsApi.class);
        return(lmsApi);
    }

    public LmsApi getMockLmsApi() {
        if (mockLmsApi!=null) return(mockLmsApi);
        InvocationHandler handler = new MockApiHandler(this);
        mockLmsApi = (LmsApi) Proxy.newProxyInstance(LmsApi.class.getClassLoader(),
                                                     new Class[] {LmsApi.class},
                                                     handler);
        return(mockLmsApi);
    }
}
