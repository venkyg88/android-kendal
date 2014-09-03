package com.staples.drawertest.test;

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;

/**
 * A test Runner to see if Android Studio is building correctly
 */
public class Runner extends Instrumentation {
    private static final String TAG = "Runner";

    @Override
    public void onCreate(Bundle bundle) {
        Log.d(TAG, "onCreate");
        start();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        Bundle bundle = new Bundle();
        finish(27, bundle);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }
}
