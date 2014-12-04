package com.staples.mobile.cfa;

import android.app.Application;

import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.os.StrictMode;

import android.util.Log;

import com.staples.mobile.common.access.lms.LmsManager;
import com.staples.mobile.common.access.lms.LmsManager.LmsMgrCallback;

public class MainApplication
        extends Application
        implements LmsMgrCallback,
        Thread.UncaughtExceptionHandler {

    private static final String TAG = "MainApplication";

    private static final boolean LOGGING = false;

    public static MainApplication application = null;

    private LmsManager lmsManager;

    @Override
    public void onCreate() {

        if (LOGGING) Log.v(TAG, "MainApplication:onCreate(): Entry.");

        application = this;

        Thread.setDefaultUncaughtExceptionHandler((Thread.UncaughtExceptionHandler) this);

        lmsManager = new LmsManager(this);
        lmsManager.getLms(this);

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

    public void onGetLmsResult(boolean success) {

        if (LOGGING) Log.v(TAG, "MainApplication:LmsManager.onGetLmsResult():"
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