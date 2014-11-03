package com.staples.mobile.cfa.home;

import android.util.Log;

public class LmsPersistentState {

    private static final String TAG = "LmsPersistentState";

    private static final boolean LOGGING = true;

    private static LmsPersistentState instance;

    private long lastTimeLmsRefreshed;

    public static LmsPersistentState getInstance() {

        if (LOGGING) Log.v(TAG, "LmsPersistentState:getInstance():"
                        + " instance[" + instance + "]"
        );

        synchronized (LmsPersistentState.class) {

            if (instance == null) {
                instance = new LmsPersistentState();
            }
            return (instance);
        }
    }

    private LmsPersistentState() {
    }

    public long getLastTimeLmsRefreshed() {
        return (lastTimeLmsRefreshed);
    }

    public void setLastTimeLmsRefreshed(long lastTimeLmsRefreshed) {
        this.lastTimeLmsRefreshed = lastTimeLmsRefreshed;
    }
}
