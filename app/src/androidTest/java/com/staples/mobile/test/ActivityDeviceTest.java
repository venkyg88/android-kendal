/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.test;

import android.content.res.Configuration;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.common.device.DeviceStats;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 15, qualifiers = "port")
public class ActivityDeviceTest {
    public static final String TAG = "ActivityDeviceTest";

    ActivityController controller;
    private MainActivity activity;
    private MainApplication application;

    @Before
    public void setUp() {
        // Redirect logcat to stdout logfile
        ShadowLog.stream = System.out;

        // Create activity controller
        controller = Robolectric.buildActivity(MainActivity.class);
        Assert.assertNotNull("Robolectric controller should not be null", controller);

        // Create activity
        controller.create();
        controller.start();
        controller.visible();
        activity = (MainActivity) controller.get();

        // Check for success
        Assert.assertNotNull("Activity should exist", activity);
        application = (MainApplication) activity.getApplication();
        Assert.assertNotNull("Application should exist", application);
    }

    @After
    public void tearDown() {
        controller.destroy();
    }

    @Test
    public void testToString() throws InterruptedException {
        DeviceStats deviceStats = new DeviceStats(activity);
        System.out.println(deviceStats.toString());
    }

    @Test
    public void testValueExistence() throws InterruptedException {
        DeviceStats deviceStats = new DeviceStats(activity);

        // Configuration methods
        Assert.assertTrue("Orientation should exist",
                deviceStats.getOrientation() == Configuration.ORIENTATION_LANDSCAPE ||
                deviceStats.getOrientation() == Configuration.ORIENTATION_PORTRAIT);
        Assert.assertNotNull("Locale should exist", deviceStats.getLocale());
        Assert.assertNotNull("FontScale should exist", deviceStats.getFontScale());
        Assert.assertNotEquals("ScreenWidthDp should exist", 0, deviceStats.getScreenWidthDp());
        Assert.assertNotEquals("ScreenHeightDp should exist", 0, deviceStats.getScreenHeightDp());
        Assert.assertNotEquals("SmallestScreenWidthDp should exist", 0, deviceStats.getSmallestScreenWidthDp());
        Assert.assertTrue("Layout size should at least be small",
                deviceStats.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_SMALL));
        Assert.assertNotNull("ScreenLayoutSize should exist", deviceStats.getScreenLayoutSize());
        Assert.assertNotEquals("UiModeType should exist", 0.0, deviceStats.getUiModeType());
        Assert.assertNotEquals("UiModeNight should exist", 0.0, deviceStats.getUiModeNight());

        // DisplayMetrics methods
        Assert.assertNotEquals("AbsoluteHeightPixels should exist", 0, deviceStats.getAbsoluteHeightPixels());
        Assert.assertNotEquals("AbsoluteWidthPixels should exist", 0, deviceStats.getAbsoluteWidthPixels());
        Assert.assertNotEquals("DensityDpi should exist", 0, deviceStats.getDensityDpi());
        Assert.assertNotEquals("LogicalDensity should exist", 0, deviceStats.getLogicalDensity());
        Assert.assertNotEquals("ScaledDensityForFonts should exist", 0, deviceStats.getScaledDensityForFonts());
        Assert.assertNotEquals("ExactXDpi should exist", 0, deviceStats.getExactXDpi());
        Assert.assertNotEquals("ExactYDpi should exist", 0, deviceStats.getExactYDpi());
        Assert.assertNotEquals("convertDpToPixels(14) should exist", 0, deviceStats.convertDpToPixels(14));
        Assert.assertNotEquals("convertPixelsToDp(14)", 0, deviceStats.convertPixelsToDp(14));
        Assert.assertNotEquals("convertPixelsToIntegerDp(14) should exist", 0, deviceStats.convertPixelsToIntegerDp(14));
    }
}
