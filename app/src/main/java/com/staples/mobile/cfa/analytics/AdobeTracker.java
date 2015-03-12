/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.analytics;

import android.content.Context;
import android.location.Location;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.staples.mobile.common.analytics.Tracker;

import java.util.Map;

/**
 * Created by sutdi001 on 3/12/15.
 */
public class AdobeTracker implements Tracker.AnalyticsService {

    /** initialize analytics */
    public AdobeTracker(Context context, boolean enableDebugLogging) {
        Config.setContext(context);
        Config.setDebugLogging(enableDebugLogging);
        Config.collectLifecycleData();
        Tracker.getInstance().initialize(Tracker.AppType.CFA, this);
    }

    /** enable or disable tracking of data, after initialization via constructor */
    public static void enableTracking(boolean enable) {
        if (Tracker.getInstance().isInitialized()) {
            if (enable) {
                Config.collectLifecycleData();
            } else {
                Config.pauseCollectingLifecycleData();
            }
        }
    }

    @Override
    public void trackState(String state, Map<String, Object> contextData) {
        Analytics.trackState(state, contextData);
    }

    @Override
    public void trackAction(String action, Map<String, Object> contextData) {
        Analytics.trackAction(action, contextData);
    }

    @Override
    public void trackLocation(Location location, Map<String, Object> contextData) {
        Analytics.trackLocation(location, contextData);
    }
}
