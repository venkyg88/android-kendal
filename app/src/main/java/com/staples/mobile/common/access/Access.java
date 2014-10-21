package com.staples.mobile.common.access;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.lms.api.LmsApi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;

public class Access {

    public interface OnLoginCompleteListener {
        public void onLoginComplete(boolean guestLevel);
    }

    private static final String TAG = "Access";

    private static final RestAdapter.LogLevel LOGLEVEL = RestAdapter.LogLevel.BASIC;

    private static final String USER_AGENT = "Staples Mobile App 1.0";
    private static final int TIMEOUT = 15; // Seconds

    private static Access instance;

    private OkClient okClient;
    private JacksonConverter converter;

    private EasyOpenApi easyOpenInsecureApi;
    private EasyOpenApi easyOpenSecureApi;
    private EasyOpenApi mockEasyOpenApi;
    private LmsApi lmsApi;
    private LmsApi mockLmsApi;

    private boolean guestLogin;
    private String token1;
    private String token2;
    private List<OnLoginCompleteListener> loginCompleteListeners;


    public static Access getInstance() {
        synchronized(Access.class) {
            if (instance == null) {
                instance = new Access();
            }
            return (instance);
        }
    }

    private Access() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
        okClient = new OkClient(okHttpClient);

        converter = new JacksonConverter();
        loginCompleteListeners = new ArrayList<OnLoginCompleteListener>();
    }

    // Interceptors for standard HTTP headers

    private class InsecureInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", "application/json");
            if(token1!=null) {
                request.addHeader("WCToken", token1);
            }
//            request.addHeader("Connection", "Keep-Alive");
        }
    }

    private class SecureInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", "application/json");
            if(token1!=null && token2!=null)
            {
                request.addHeader("WCToken", token1);
                request.addHeader("WCTrustedToken", token2);
            }
//            request.addHeader("Connection", "Keep-Alive");
        }
    }

    public void setTokens(String wcToken, String wcTrustedToken, boolean guestLogin) {
        token1 = wcToken;
        token2 = wcTrustedToken;
        if (token1 != null) {
            this.guestLogin = guestLogin;
            if (loginCompleteListeners != null) {
                for (OnLoginCompleteListener listener : loginCompleteListeners) {
                    listener.onLoginComplete(guestLogin);
                }
            }
        }
    }

    /** returns true if current login level is only guest */
    public boolean isGuestLogin() {
        return guestLogin;
    }

    public void setGuestLogin(boolean guestLogin) {
        this.guestLogin = guestLogin;
    }

    /** adds listener to be notified following successful login */
    public void registerLoginCompleteListener(OnLoginCompleteListener loginCompleteListener) {
        if (!loginCompleteListeners.contains(loginCompleteListener)) {
            loginCompleteListeners.add(loginCompleteListener);
        }
    }

    /** removes listener */
    public void unregisterLoginCompleteListener(OnLoginCompleteListener loginCompleteListener) {
        if (loginCompleteListeners.contains(loginCompleteListener)) {
            loginCompleteListeners.remove(loginCompleteListener);
        }
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

    public EasyOpenApi getMockEasyOpenApi(Context context) {
        if (mockEasyOpenApi!=null) return(mockEasyOpenApi);
        InvocationHandler handler = new MockApiHandler(context);
        mockEasyOpenApi = (EasyOpenApi) Proxy.newProxyInstance(EasyOpenApi.class.getClassLoader(), new Class[]{EasyOpenApi.class}, handler);
        return(mockEasyOpenApi);
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

    public LmsApi getMockLmsApi(Context context) {
        if (mockLmsApi!=null) return(mockLmsApi);
        InvocationHandler handler = new MockApiHandler(context);
        mockLmsApi = (LmsApi) Proxy.newProxyInstance(LmsApi.class.getClassLoader(), new Class[]{LmsApi.class}, handler);
        return(mockLmsApi);
    }
}
