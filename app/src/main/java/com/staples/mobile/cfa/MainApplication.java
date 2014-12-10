package com.staples.mobile.cfa;

import android.app.Application;

import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.os.StrictMode;

import android.util.Log;

import com.staples.mobile.common.access.config.AppConfigurator;

public class MainApplication
        extends Application
        implements AppConfigurator.AppConfiguratorCallback,
        Thread.UncaughtExceptionHandler {

    private static final String TAG = "MainApplication";

    private static final boolean LOGGING = true;

    public static MainApplication application = null;

    private AppConfigurator appConfigurator;

    @Override
    public void onCreate() {

        if (LOGGING) Log.v(TAG, "MainApplication:onCreate(): Entry.");

        application = this;

        Thread.setDefaultUncaughtExceptionHandler((Thread.UncaughtExceptionHandler) this);

        appConfigurator = new AppConfigurator(this);
        appConfigurator.getConfigurator(this);

        /* @@@ STUBBED
        setStrictMode();
        @@@ STUBBED */
    }

    public void uncaughtException(Thread terminatedThread, Throwable causeThrowable) {
        if (LOGGING) Log.v(TAG, "MainApplication:onCreate(): Entry."
                        + " terminatedThread[" + terminatedThread + "]"
                        + " causeThrowable[" + causeThrowable + "]"
                        + " this[" + this + "]"
        );

        causeThrowable.printStackTrace();

        return;
    }

    public void onGetConfiguratorResult(boolean success) {

        if (LOGGING) Log.v(TAG, "MainApplication:AppConfigurator.onGetConfiguratorResult():"
                        + " success[" + success + "]"
                        + " this[" + this + "]"
        );
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
}
