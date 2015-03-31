package com.staples.mobile.test;

import com.staples.mobile.common.device.DeviceInfo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, qualifiers = "port" )
public class ActivityDeviceInfoTest {
    private static final String TAG = "ActivityDeviceInfoTest";

    @Before
    public void setUp() {
        Utility.setUp();
    }

    @After
    public void tearDown() {
        Utility.tearDown();
    }

    @Test
    public void testToString() throws InterruptedException {
        DeviceInfo deviceInfo = new DeviceInfo(Utility.activity);
        System.out.println(deviceInfo.toString());
    }

    @Test
    public void testValueExistence() throws InterruptedException {
        DeviceInfo deviceInfo = new DeviceInfo(Utility.activity);

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
        DeviceInfo d = new DeviceInfo(Utility.activity);

        Assert.assertTrue(d.getSmallestAbsWidthDp() <= d.getLargestAbsWidthDp());
        Assert.assertTrue(d.getSmallestAbsWidthPixels() <= d.getLargestAbsWidthPixels());
    }

    @Test
    public void testUnitsConversion() throws InterruptedException {
        DeviceInfo d = new DeviceInfo(Utility.activity);

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
