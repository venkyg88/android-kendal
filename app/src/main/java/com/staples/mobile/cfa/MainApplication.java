package com.staples.mobile.cfa;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;
import com.staples.mobile.cfa.feed.FeedAdapter;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.lms.api.LmsApi;
import com.staples.mobile.common.access.login.model.TokenObject;

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

    private OkClient okClient;
    private JacksonConverter converter;
    private EasyOpenApi easyOpenApi;
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

    // Interceptor for standard HTTP headers

    private static class StandardInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", "application/json");
//            request.addHeader("WCToken", wcToken);
//            request.addHeader("WCTrustedToken", wcTrustedToken);
//            request.addHeader("Connection", "Keep-Alive");
        }
    }

    private static class StandardInterceptorUpdated implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {

            FeedAdapter feedAdapter = new FeedAdapter();
            TokenObject tokenObject = feedAdapter.getUserTokens();
            request.addHeader("Content-Type", "application/json");
            request.addHeader("WCToken", tokenObject.getWCToken());
            request.addHeader("WCTrustedToken", tokenObject.getWCTrustedToken());
        }
    }

    // EasyOpen API

    public EasyOpenApi getEasyOpenApi() {
        if (easyOpenApi!=null) return(easyOpenApi);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);
        builder.setEndpoint(EasyOpenApi.SERVICE_ENDPOINT);
        builder.setRequestInterceptor(new StandardInterceptor());
        builder.setConverter(converter);
        builder.setLogLevel(LOGLEVEL);
        builder.setLog(new AndroidLog(TAG));
        RestAdapter adapter = builder.build();

        easyOpenApi = adapter.create(EasyOpenApi.class);
        return(easyOpenApi);
    }

    // LMS API

    public LmsApi getLmsApi() {
        if (lmsApi!=null) return(lmsApi);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);
        builder.setEndpoint(LmsApi.SERVICE_ENDPOINT);
        builder.setRequestInterceptor(new StandardInterceptor());
        builder.setConverter(converter);
        builder.setLogLevel(LOGLEVEL);
        builder.setLog(new AndroidLog(TAG));
        RestAdapter adapter = builder.build();

        lmsApi = adapter.create(LmsApi.class);
        return(lmsApi);
    }

    public EasyOpenApi getEasyOpenApiSecure()
    {
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);
        builder.setEndpoint(EasyOpenApi.SERVICE_ENDPOINT_SECURE);
        builder.setRequestInterceptor(new StandardInterceptorUpdated());
        builder.setConverter(converter);
        builder.setLogLevel(LOGLEVEL);
        builder.setLog(new AndroidLog(TAG));
        RestAdapter adapter = builder.build();

        easyOpenApi = adapter.create(EasyOpenApi.class);
        return(easyOpenApi);
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
