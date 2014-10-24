/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.device;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Created by Diana Sutlief on 10/8/14.
 *
 * Provides stats on physical device gathered from DisplayMetrics and Configuration
 */
public class DeviceInfo {

    // fields populated from android.os.Build
    private String brand;
    private String device;
    private String display;
    private String manufacturer;
    private String model;
    private String product;
    private String serialNumber;
    private String versionCodeName;
    private String versionIncrementalBuild;
    private String versionReleaseName;
    private int versionSdkLevel;

    // fields populated from android.content.res.Configuration
    private float fontScale;
    private Locale locale;
    private boolean layoutSizeAtLeastSmall;
    private boolean layoutSizeAtLeastNormal;
    private boolean layoutSizeAtLeastLarge;
    private boolean layoutSizeAtLeastXLarge;
    private float screenLayoutSize;
    private boolean screenLayoutLong;
    private float uiModeType;
    private float uiModeNight;

    // fields populated from android.util.DisplayMetrics
    private int smallestAbsWidthPixels;
    private int largestAbsWidthPixels;
    private int widthPixels;
    private int heightPixels;
    private float smallestAbsWidthDp;
    private float largestAbsWidthDp;
    private int densityDpi;
    private float logicalDensity;
    private float scaledDensityForFonts;
    private float exactXDpi;
    private float exactYDpi;


    /** constructor */
    public DeviceInfo(Context c) {
        this(c.getResources());
    }

    /** constructor */
    public DeviceInfo(Resources r) {
        DisplayMetrics displayMetrics = r.getDisplayMetrics();
        Configuration configuration = r.getConfiguration();

        // values populated from android.os.Build
        brand = Build.BRAND;
        device = Build.DEVICE;
        display = Build.DISPLAY;
        manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;
        product = Build.PRODUCT;
        serialNumber = Build.SERIAL;
        versionCodeName = Build.VERSION.CODENAME;
        versionIncrementalBuild = Build.VERSION.INCREMENTAL;
        versionReleaseName = Build.VERSION.RELEASE;
        versionSdkLevel = Build.VERSION.SDK_INT;

        // fields populated from android.content.res.Configuration
        fontScale = configuration.fontScale;
        locale = configuration.locale;
        layoutSizeAtLeastSmall = configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_SMALL);
        layoutSizeAtLeastNormal = configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL);
        layoutSizeAtLeastLarge = configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
        layoutSizeAtLeastXLarge = configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE);
        screenLayoutSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        screenLayoutLong = (configuration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) ==
                Configuration.SCREENLAYOUT_LONG_YES;
        uiModeType = configuration.uiMode & Configuration.UI_MODE_TYPE_MASK;
        uiModeNight = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        // values populated from android.util.DisplayMetrics
        densityDpi = displayMetrics.densityDpi; // IMPORTANT: set density first for use in conversion
        logicalDensity = displayMetrics.density;
        widthPixels = displayMetrics.widthPixels;
        heightPixels = displayMetrics.heightPixels;
        smallestAbsWidthPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        largestAbsWidthPixels = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        smallestAbsWidthDp = convertPixelsToDp(smallestAbsWidthPixels);
        largestAbsWidthDp = convertPixelsToDp(largestAbsWidthPixels);
        scaledDensityForFonts = displayMetrics.scaledDensity;
        exactXDpi = displayMetrics.xdpi;
        exactYDpi = displayMetrics.ydpi;

    }



    // --------------------------------------------------------------- //
    // ------------------ using android.os.Build --------------------- //
    // --------------------------------------------------------------- //

    /** returns consumer-visible brand name */
    public String getBrand() {
        return brand;
    }

    /** returns device's industrial design name */
    public String getDevice() {
        return device;
    }

    /** returns consumer-visible build ID */
    public String getDisplay() {
        return display;
    }

    /** returns manufacturer of the product/hardware */
    public String getManufacturer() {
        return manufacturer;
    }

    /** returns consumer-visible model name */
    public String getModel() {
        return model;
    }

    /** returns consumer-visible product name */
    public String getProduct() {
        return product;
    }

    /** returns hardware serial number, if available */
    public String getSerialNumber() {
        return serialNumber;
    }

    /** returns version's development code name */
    public String getVersionCodeName() {
        return versionCodeName;
    }

    /** returns version's incremental build number */
    public String getVersionIncrementalBuild() {
        return versionIncrementalBuild;
    }

    /** returns version's release name */
    public String getVersionReleaseName() {
        return versionReleaseName;
    }

    /** returns version's SDK level */
    public int getVersionSdkLevel() {
        return versionSdkLevel;
    }


    // --------------------------------------------------------------- //
    // ---------- using android.content.res.Configuration ------------ //
    // --------------------------------------------------------------- //


    /** returns user preference for the scaling factor for fonts, relative to the base density scaling */
    public float getFontScale() {
        return fontScale;
    }

    /** gets current user preference for the locale, corresponding to locale resource qualifier */
    public Locale getLocale() {
        return locale;
    }

    /** returns true if current screenLayout is at least small */
    public boolean isLayoutSizeAtLeastSmall() {
        return layoutSizeAtLeastSmall;
    }

    /** returns true if current screenLayout is at least normal */
    public boolean isLayoutSizeAtLeastNormal() {
        return layoutSizeAtLeastNormal;
    }

    /** returns true if current screenLayout is at least large */
    public boolean isLayoutSizeAtLeastLarge() {
        return layoutSizeAtLeastLarge;
    }

    /** returns true if current screenLayout is at least extra large */
    public boolean isLayoutSizeAtLeastXLarge() {
        return layoutSizeAtLeastXLarge;
    }

    /** returns screenLayout size (e.g. Configuration.SCREENLAYOUT_SIZE_SMALL) */
    public float getScreenLayoutSize() {
        return screenLayoutSize;
    }

    /** returns true if screen is wider/taller than normal */
    public boolean isScreenLayoutLong() {
        return screenLayoutLong;
    }


    /** returns UI mode type, may be one of:
     *
     * Configuration.UI_MODE_TYPE_UNDEFINED
     * Configuration.UI_MODE_TYPE_NORMAL
     * Configuration.UI_MODE_TYPE_DESK
     * Configuration.UI_MODE_TYPE_CAR
     * Configuration.UI_MODE_TYPE_TELEVISION
     * Configuration.UI_MODE_TYPE_APPLIANCE
     * Configuration.UI_MODE_TYPE_WATCH
     */
    public float getUiModeType() {
        return uiModeType;
    }

    /** returns UI mode night setting, may be one of:
     *
     * Configuration.UI_MODE_NIGHT_UNDEFINED
     * Configuration.UI_MODE_NIGHT_NO
     * Configuration.UI_MODE_NIGHT_YES
     */
    public float getUiModeNight() {
        return uiModeNight;
    }


    // --------------------------------------------------------------- //
    // ------------- using android.util.DisplayMetrics --------------- //
    // --------------------------------------------------------------- //


    /** returns the absolute width of the display in pixels */
    public int getWidthPixels() {
        return widthPixels;
    }

    /** returns the absolute height of the display in pixels */
    public int getHeightPixels() {
        return heightPixels;
    }

    /** returns the absolute width of the longest dimension of the display in pixels */
    public int getLargestAbsWidthPixels() {
        return largestAbsWidthPixels;
    }

    /** returns the absolute width of the shortest dimension of the display in pixels */
    public int getSmallestAbsWidthPixels() {
        return smallestAbsWidthPixels;
    }

    /** returns the absolute width of the longest dimension of the display in dp */
    public float getLargestAbsWidthDp() {
        return largestAbsWidthDp;
    }

    /** returns the absolute width of the shortest dimension of the display in dp */
    public float getSmallestAbsWidthDp() {
        return smallestAbsWidthDp;
    }


    /** returns screen density in pixels (dots) per inch */
    public int getDensityDpi() {
        return densityDpi;
    }

    /** returns logical density of screen which is used for scaling (ratio of pixels/DIP)
     * E.g. 1 on 160dpi screen where 1 dip ~= 1 pixel
     *      .75 on 120dpi screen where 1 dip ~= .75 pixel
     *      2 on 320dpi screen where 1 dip ~= 2 pixel
    */
    public float getLogicalDensity() {
        return logicalDensity;
    }

    /** returns scaling factor for fonts, similar to logical density except may be adjusted
     * in smaller increments at runtime based on a user preference for the font size
    */
    public float getScaledDensityForFonts() {
        return scaledDensityForFonts;
    }

    /** returns exact dpi in x direction */
    public float getExactXDpi() {
        return exactXDpi;
    }

    /** returns exact dpi in y direction */
    public float getExactYDpi() {
        return exactYDpi;
    }



    // --------------------------------------------------------------- //
    // --------------------- conversion methods ---------------------- //
    // --------------------------------------------------------------- //

    /** converts value in DP to number of pixels */
    public float convertDpToPixels(float dp) {
        return dp * logicalDensity;
    }

    /** converts number of pixels to DP units */
    public float convertPixelsToDp(float pixels) {
        return pixels / logicalDensity;
    }

    /** converts number of pixels to DP units */
    public int convertPixelsToIntegerDp(float pixels) {
        return Math.round(convertPixelsToDp(pixels));
    }

    /** converts Dp to number of pixels */
    public int convertDpToIntegerPixels(float dp) {
        return Math.round(convertDpToPixels(dp));
    }


    // --------------------------------------------------------------- //
    // ------------------ varies by configuration -------------------- //
    // --------------------------------------------------------------- //

    /** returns true if current orientation of the screen is landscape */
    public boolean isCurrentOrientationLandscape(Resources r) {
        return Configuration.ORIENTATION_LANDSCAPE == r.getConfiguration().orientation;
    }

    /** returns true if current orientation of the screen is portrait */
    public boolean isCurrentOrientationPortrait(Resources r) {
        return Configuration.ORIENTATION_PORTRAIT == r.getConfiguration().orientation;
    }

    /** returns current width of the available screen space in dp units */
    public int getCurrentAvailScreenWidthDp(Resources r) {
        return r.getConfiguration().screenWidthDp;
    }

    /** returns current height of the available screen space in dp units */
    public int getCurrentAvailScreenHeightDp(Resources r) {
        return r.getConfiguration().screenHeightDp;
    }

    /** returns the smallest of both screenWidthDp and screenHeightDp in both portrait and landscape */
    public float getCurrentAvailSmallestScreenWidthDp(Resources r) {
        return r.getConfiguration().smallestScreenWidthDp;
    }



    // --------------------------------------------------------------- //
    // ------------------------- toString() -------------------------- //
    // --------------------------------------------------------------- //

    /** returns collection of device info */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("\nDevice Info:")
                .append("\n\n-------- from android.os.Build --------")
                .append("\ngetBrand: ").append(getBrand())
                .append("\ngetDevice: ").append(getDevice())
                .append("\ngetDisplay: ").append(getDisplay())
                .append("\ngetManufacturer: ").append(getManufacturer())
                .append("\ngetModel: ").append(getModel())
                .append("\ngetProduct: ").append(getProduct())
                .append("\ngetSerialNumber: ").append(getSerialNumber())
                .append("\ngetVersionCodeName: ").append(getVersionCodeName())
                .append("\ngetVersionIncrementalBuild: ").append(getVersionIncrementalBuild())
                .append("\ngetVersionReleaseName: ").append(getVersionReleaseName())
                .append("\ngetVersionSdkLevel: ").append(getVersionSdkLevel())
                .append("\n\n-------- from android.content.res.Configuration --------")
                .append("\ngetLocale: ").append(getLocale())
                .append("\ngetFontScale: ").append(getFontScale())
                .append("\nisLayoutSizeAtLeastSmall: ").append(isLayoutSizeAtLeastSmall())
                .append("\nisLayoutSizeAtLeastNormal: ").append(isLayoutSizeAtLeastNormal())
                .append("\nisLayoutSizeAtLeastLarge: ").append(isLayoutSizeAtLeastLarge())
                .append("\nisLayoutSizeAtLeastXLarge: ").append(isLayoutSizeAtLeastXLarge())
                .append("\ngetScreenLayoutSize: ").append(getScreenLayoutSize())
                .append("\nisScreenLayoutLong: ").append(isScreenLayoutLong())
                .append("\ngetUiModeType: ").append(getUiModeType())
                .append("\ngetUiModeNight: ").append(getUiModeNight())
                .append("\n\n-------- from android.util.DisplayMetrics --------")
                .append("\ngetDensityDpi: ").append(getDensityDpi())
                .append("\ngetLogicalDensity: ").append(getLogicalDensity())
                .append("\ngetScaledDensityForFonts: ").append(getScaledDensityForFonts())
                .append("\ngetExactXDpi: ").append(getExactXDpi())
                .append("\ngetExactYDpi: ").append(getExactYDpi())
                .append("\ngetSmallestAbsWidthDp: ").append(getSmallestAbsWidthDp())
                .append("\ngetLargestAbsWidthDp: ").append(getLargestAbsWidthDp())
                .append("\ngetSmallestAbsWidthPixels: ").append(getSmallestAbsWidthPixels())
                .append("\ngetLargestAbsWidthPixels: ").append(getLargestAbsWidthPixels())
                .append("\n");
        return stringBuilder.toString();
    }

}


