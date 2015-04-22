package com.staples.mobile.cfa;

import android.app.Application;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.os.StrictMode;

import android.util.Log;

import com.crittercism.app.Crittercism;

import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.configurator.model.Configurator;

import retrofit.RetrofitError;

public class MainApplication
        extends Application
        implements AppConfigurator.AppConfiguratorCallback,
        Thread.UncaughtExceptionHandler {

    private static final String TAG = MainApplication.class.getSimpleName();

    private static final boolean LOGGING = false;

    public static MainApplication application;

    public static Resources resources;

    private static AppConfigurator appConfigurator;

    private static StaplesAppContext staplesAppContext;

    @Override
    public void onCreate() {

        if (LOGGING) Log.v(TAG, "MainApplication:onCreate(): Entry.");

        application = this;

        Thread.setDefaultUncaughtExceptionHandler((Thread.UncaughtExceptionHandler) this);

        resources = getResources();

        try {
            Crittercism.initialize(this, FlavorSpecific.CRITTERCISM_ID);
            Crittercism.leaveBreadcrumb("MainApplication:onCreate(): Crittercism initialized.");
        } catch (Exception exception) {

            if (LOGGING) Log.e(TAG, "MainApplication:onCreate(): EXCEPTION[Exception]: Crittercism.initialize()"
                            + " exception[" + exception + "]"
                            + " this[" + this + "]"
            );

            if (exception != null) exception.printStackTrace();
        }

        appConfigurator = AppConfigurator.getInstance(this, FlavorSpecific.MCS_URL);
        appConfigurator.getConfigurator(this);

        /* @@@ STUBBED
        setStrictMode();
        @@@ STUBBED */
    }

    public void uncaughtException(Thread terminatedThread, Throwable causeThrowable) {

        if (LOGGING) Log.e(TAG, "MainApplication:uncaughtException(): EXCEPTION[Throwable]:"
                        + " terminatedThread[" + terminatedThread + "]"
                        + " causeThrowable[" + causeThrowable + "]"
                        + " this[" + this + "]"
        );

        causeThrowable.printStackTrace();

        Crittercism.leaveBreadcrumb("MainApplication:uncaughtException():");
        Crittercism.logHandledException(causeThrowable);

        return;
    }

    public void onGetConfiguratorResult(Configurator configurator, boolean success, RetrofitError retrofitError) {

        if (LOGGING) Log.v(TAG, "MainApplication:AppConfigurator.onGetConfiguratorResult():"
                        + " success[" + success + "]"
                        + " retrofitError[" + retrofitError + "]"
                        + " this[" + this + "]"
        );

        if (staplesAppContext == null) {
            // This will cause StaplesAppContext to obtain a reference to the
            // newly acquired Configurator class,
            staplesAppContext = StaplesAppContext.getInstance();
        }
    }

    private void setStrictMode() {

        if (LOGGING) Log.v(TAG, "MainApplication:setStrictMode(): Entry.");

        StrictMode.enableDefaults();

        // Thread Policy

        ThreadPolicy.Builder threadPolicyBuilder = new ThreadPolicy.Builder();
        threadPolicyBuilder.detectAll();
        threadPolicyBuilder.penaltyLog();
        ThreadPolicy threadPolicy = threadPolicyBuilder.build();

        StrictMode.setThreadPolicy(threadPolicy);

        // VM Policy

        VmPolicy.Builder vmPolicyBuilder = new VmPolicy.Builder();
        vmPolicyBuilder.detectAll();
        vmPolicyBuilder.detectActivityLeaks();
        vmPolicyBuilder.detectLeakedClosableObjects();
        vmPolicyBuilder.detectLeakedSqlLiteObjects();
        vmPolicyBuilder.penaltyLog();
        VmPolicy vmPolicy = vmPolicyBuilder.build();

        StrictMode.setVmPolicy(vmPolicy);

        return;

    } // setStrictMode()

    /** returns true if network available */
    private boolean isNetworkAvailable() {
        // first check for network connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        boolean networkIsAvailable = false;
        if (networkInfo != null) {
            networkIsAvailable = networkInfo.isConnected();
        }
        if (LOGGING) Log.v(TAG, "MainApplication:isNetworkAvailable():"
                        + " networkIsAvailable[" + networkIsAvailable + "]"
                        + " networkInfo[" + networkInfo + "]"
                        + " this[" + this + "]"
        );
        return (networkIsAvailable);
    }
}
