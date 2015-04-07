/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.kount;

import android.app.Activity;
import android.util.Log;

import com.devicecollector.DeviceCollector;

/**
 * Created by sutdi001 on 4/6/15.
 */
public class KountManager implements DeviceCollector.StatusListener {
    public static final String TAG = KountManager.class.getSimpleName();

    private static final String KOUNT_MERCHANT_URL = "https://ssl.kaptcha.com";
    private static final String KOUNT_MERCHANT_ID = "101100"; //

    static KountManager instance;

    private DeviceCollector deviceCollector;
    String sessionId;

    public static KountManager getInstance() {
        return instance;
    }

    /** get singleton instance of Tracker */
    public static KountManager getInstance(Activity activity) {
        if (instance == null) {
            synchronized (KountManager.class) {
                // Double check
                if (instance == null) {
                    instance = new KountManager(activity);
                }
            }
        }
        return instance;
    }

    private KountManager(Activity activity) {
        // initialize Kount
        try {
            deviceCollector = new DeviceCollector(activity);
            deviceCollector.setMerchantId(KOUNT_MERCHANT_ID);
            deviceCollector.setCollectorUrl(KOUNT_MERCHANT_URL);
            deviceCollector.setStatusListener(this);
        } catch (Exception e) {
            Log.e(TAG, "Kount: " + e.getMessage());
        }
    }


    /**
     * @param sessionId use order id
     */
    public void collect(String sessionId) {
        // only call collect once per sessionId
        if (sessionId != null && !sessionId.equals(this.sessionId)) {
            try {
                deviceCollector.collect(sessionId);
                this.sessionId = sessionId;
            } catch (Exception e) {
                Log.e(TAG, "Kount: " + e.getMessage());
            }
        }
    }


    /* methods implementing Kount's DeviceCollector.StatusListener interface */

    @Override
    public void onCollectorStart() {
        Log.i(TAG, "Kount: onCollectorStart");
    }

    @Override
    public void onCollectorSuccess() {
        Log.i(TAG, "Kount: onCollectorSuccess");
    }

    @Override
    public void onCollectorError(DeviceCollector.ErrorCode errorCode, Exception e) {
        Log.e(TAG, "Kount: " + errorCode + (e != null? (" " + e.getMessage()) : ""));
    }
}
