/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.test;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.common.device.DeviceInfo;

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
@Config(emulateSdk = 18, qualifiers = "port" )
public class ActivityDeviceInfoTest {
    public static final String TAG = "ActivityDeviceInfoTest";

    ActivityController controller;
    private MainActivity activity;

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
    }

    @After
    public void tearDown() {
        controller.destroy();
    }

    @Test
    public void testToString() throws InterruptedException {
        DeviceInfo deviceInfo = new DeviceInfo(activity);
        System.out.println(deviceInfo.toString());
    }

    @Test
    public void testValueExistence() throws InterruptedException {
        DeviceInfo deviceInfo = new DeviceInfo(activity);

        // Build methods
        Assert.assertNotNull("Brand should exist", deviceInfo.getBrand());
        Assert.assertNotNull("Device should exist", deviceInfo.getDevice());
        Assert.assertNotNull("Display should exist", deviceInfo.getDisplay());
        Assert.assertNotNull("Manufacturer should exist", deviceInfo.getManufacturer());
        Assert.assertNotNull("Model should exist", deviceInfo.getModel());
        Assert.assertNotNull("Product should exist", deviceInfo.getProduct());
        Assert.assertNotNull("SerialNumber should exist", deviceInfo.getSerialNumber());
        Assert.assertNotNull("VersionCodeName should exist", deviceInfo.getVersionCodeName());
        Assert.assertNotNull("VersionIncrementalBuild should exist", deviceInfo.getVersionIncrementalBuild());
        Assert.assertNotNull("VersionReleaseName should exist", deviceInfo.getVersionReleaseName());
        Assert.assertNotEquals("VersionSdkLevel should exist", 0, deviceInfo.getVersionSdkLevel());

        // Configuration methods
        Assert.assertNotNull("Locale should exist", deviceInfo.getLocale());
        Assert.assertNotNull("FontScale should exist", deviceInfo.getFontScale());
        Assert.assertTrue("Layout size should at least be small", deviceInfo.isLayoutSizeAtLeastSmall());
        Assert.assertNotNull("ScreenLayoutSize should exist", deviceInfo.getScreenLayoutSize());
// robolectric emulation returns 0 for UiModeType, UiModeNight
//        Assert.assertNotEquals("UiModeType should exist", 0.0, deviceInfo.getUiModeType());
//        Assert.assertNotEquals("UiModeNight should exist", 0.0, deviceInfo.getUiModeNight());

        // DisplayMetrics methods
        Assert.assertNotEquals("SmallestAbsWidthPixels should exist", 0, deviceInfo.getSmallestAbsWidthPixels());
        Assert.assertNotEquals("LargestAbsWidthPixels should exist", 0, deviceInfo.getLargestAbsWidthPixels());
        Assert.assertNotEquals("SmallestAbsWidthDp should exist", 0, deviceInfo.getSmallestAbsWidthDp());
        Assert.assertNotEquals("LargestAbsWidthDp should exist", 0, deviceInfo.getLargestAbsWidthDp());
        Assert.assertNotEquals("DensityDpi should exist", 0, deviceInfo.getDensityDpi());
        Assert.assertNotEquals("LogicalDensity should exist", 0, deviceInfo.getLogicalDensity());
        Assert.assertNotEquals("ScaledDensityForFonts should exist", 0, deviceInfo.getScaledDensityForFonts());
        Assert.assertNotEquals("ExactXDpi should exist", 0, deviceInfo.getExactXDpi());
        Assert.assertNotEquals("ExactYDpi should exist", 0, deviceInfo.getExactYDpi());
    }

    @Test
    public void testWidths() throws InterruptedException {
        DeviceInfo d = new DeviceInfo(activity);

        Assert.assertTrue(d.getSmallestAbsWidthDp() <= d.getLargestAbsWidthDp());
        Assert.assertTrue(d.getSmallestAbsWidthPixels() <= d.getLargestAbsWidthPixels());
    }

    @Test
    public void testUnitsConversion() throws InterruptedException {
        DeviceInfo d = new DeviceInfo(activity);

        // convert from pixels to db and back to pixels and confirm the same within rounding error
        Assert.assertEquals(d.getSmallestAbsWidthPixels(),
                d.convertDpToPixels(d.convertPixelsToDp(d.getSmallestAbsWidthPixels())), .5);

        // if logical density > 1, then # of pixels > # of DPs
        if (d.getLogicalDensity() > 1.0) {
            // confirm that # of pixels > # of DPs
            Assert.assertTrue(d.getSmallestAbsWidthPixels() > d.convertPixelsToDp(d.getSmallestAbsWidthPixels()));
        } else if (d.getLogicalDensity() < 1.0) {
            // confirm that # of pixels < # of DPs
            Assert.assertTrue(d.getSmallestAbsWidthPixels() < d.convertPixelsToDp(d.getSmallestAbsWidthPixels()));
        } else {
            // confirm the same
            Assert.assertTrue(d.getSmallestAbsWidthPixels() == d.convertPixelsToDp(d.getSmallestAbsWidthPixels()));
        }
    }

}
