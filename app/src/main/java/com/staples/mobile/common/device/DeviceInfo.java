/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.device;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.Locale;

/**
 * Created by Diana Sutlief on 10/8/14.
 *
 * Provides stats on physical device gathered from DisplayMetrics and Configuration
 */
public class DeviceInfo {

    Resources resources;
    DisplayMetrics displayMetrics;
    Configuration configuration;


    /** constructor */
    public DeviceInfo(Resources r) {
        this.resources = r;
        this.displayMetrics = resources.getDisplayMetrics();
        this.configuration = resources.getConfiguration();
    }

    /** constructor */
    public DeviceInfo(Context c) {
        this(c.getResources());
    }


    // --------------------------------------------------------------- //
    // ----------------------- using Build --------------------------- //
    // --------------------------------------------------------------- //

    /** returns consumer-visible brand name */
    public String getBrand() {
        return Build.BRAND;
    }

    /** returns device's industrial design name */
    public String getDevice() {
        return Build.DEVICE;
    }

    /** returns consumer-visible build ID */
    public String getDisplay() {
        return Build.DISPLAY;
    }

    /** returns manufacturer of the product/hardware */
    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /** returns consumer-visible model name */
    public String getModel() {
        return Build.MODEL;
    }

    /** returns consumer-visible product name */
    public String getProduct() {
        return Build.PRODUCT;
    }

    /** returns hardware serial number, if available */
    public String getSerialNumber() {
        return Build.SERIAL;
    }

    /** returns version's development code name */
    public String getVersionCodeName() {
        return Build.VERSION.CODENAME;
    }

    /** returns version's incremental build number */
    public String getVersionIncrementalBuild() {
        return Build.VERSION.INCREMENTAL;
    }

    /** returns version's release name */
    public String getVersionReleaseName() {
        return Build.VERSION.RELEASE;
    }

    /** returns version's SDK level */
    public int getVersionSdkLevel() {
        return Build.VERSION.SDK_INT;
    }

    // --------------------------------------------------------------- //
    // ----------------------- using Resources ----------------------- //
    // --------------------------------------------------------------- //

    /** returns dimension in DP of a dimension resource */
    public float getDimension(int dimensionResourceId) {
        return resources.getDimension(dimensionResourceId);
    }


    // --------------------------------------------------------------- //
    // ------------- using Resources.getConfiguration() --------------- //
    // --------------------------------------------------------------- //


    /** returns user preference for the scaling factor for fonts, relative to the base density scaling */
    public float getFontScale() {
        return configuration.fontScale;
    }

    /** gets current user preference for the locale, corresponding to locale resource qualifier */
    public Locale getLocale() {
        return configuration.locale;
    }

    /** returns current width of the available screen space in dp units */
    public int getScreenWidthDp() {
        return configuration.screenWidthDp;
    }

    /** returns current height of the available screen space in dp units */
    public int getScreenHeightDp() {
        return configuration.screenHeightDp;
    }

    /** returns the smallest of both screenWidthDp and screenHeightDp in both portrait and landscape */
    public float getSmallestScreenWidthDp() {
        return configuration.smallestScreenWidthDp;
    }

    /** returns true if current screenLayout is at least size (e.g. Configuration.SCREENLAYOUT_SIZE_SMALL) */
    public boolean isLayoutSizeAtLeast (int size) {
        return configuration.isLayoutSizeAtLeast(size);
    }

    /** returns screenLayout size (e.g. Configuration.SCREENLAYOUT_SIZE_SMALL) */
    public float getScreenLayoutSize() {
        return configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
    }

    /** returns true if screen is wider/taller than normal */
    public boolean isScreenLayoutLong() {
        return (configuration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) ==
                Configuration.SCREENLAYOUT_LONG_YES;
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
        return configuration.uiMode & Configuration.UI_MODE_TYPE_MASK;
    }

    /** returns UI mode night setting, may be one of:
     *
     * Configuration.UI_MODE_NIGHT_UNDEFINED
     * Configuration.UI_MODE_NIGHT_NO
     * Configuration.UI_MODE_NIGHT_YES
     */
    public float getUiModeNight() {
        return configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }


    // --------------------------------------------------------------- //
    // ------------- using Resources.getDisplayMetrics() -------------- //
    // --------------------------------------------------------------- //


    /** returns the absolute height of the display in pixels */
    public int getAbsoluteHeightPixels() {
        return displayMetrics.heightPixels;
    }

    /** returns the absolute width of the display in pixels */
    public int getAbsoluteWidthPixels() {
        return displayMetrics.widthPixels;
    }

    /** returns screen density in pixels (dots) per inch */
    public int getDensityDpi() {
        return displayMetrics.densityDpi;
    }

    /** returns logical density of screen which is used for scaling
     * E.g. 1 on 160dpi screen where 1 dip ~= 1 pixel
     *      .75 on 120dpi screen where 1 dip ~= .75 pixel
     *      2 on 320dpi screen where 1 dip ~= 2 pixel
    */
    public float getLogicalDensity() {
        return displayMetrics.density;
    }

    /** returns scaling factor for fonts, similar to logical density except may be adjusted
     * in smaller increments at runtime based on a user preference for the font size
    */
    public float getScaledDensityForFonts() {
        return displayMetrics.scaledDensity;
    }

    /** returns exact dpi in x direction */
    public float getExactXDpi() {
        return displayMetrics.xdpi;
    }

    /** returns exact dpi in y direction */
    public float getExactYDpi() {
        return displayMetrics.ydpi;
    }

    /** converts value in DP to number of pixels */
    public float convertDpToPixels(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    /** converts number of pixels to DP units */
    public float convertPixelsToDp(int pixels) {
        return pixels / displayMetrics.density;
    }

    /** converts number of pixels to DP units */
    public int convertPixelsToIntegerDp(int pixels) {
        return Math.round(convertPixelsToDp(pixels));
    }


    /** returns collection of device info */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("-------- Build --------")
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
                .append("\n\n-------- Configuration --------")
                .append("\ngetLocale: ").append(getLocale())
                .append("\ngetFontScale: ").append(getFontScale())
                .append("\ngetScreenWidthDp: ").append(getScreenWidthDp())
                .append("\ngetScreenHeightDp: ").append(getScreenHeightDp())
                .append("\ngetSmallestScreenWidthDp: ").append(getSmallestScreenWidthDp())
                .append("\nisLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_SMALL): ")
                .append(isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_SMALL))
                .append("\nisLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL): ")
                .append(isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL))
                .append("\nisLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE): ")
                .append(isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE))
                .append("\nisLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE): ")
                .append(isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE))
                .append("\ngetScreenLayoutSize: ").append(getScreenLayoutSize())
                .append("\nisScreenLayoutLong: ").append(isScreenLayoutLong())
                .append("\ngetUiModeType: ").append(getUiModeType())
                .append("\ngetUiModeNight: ").append(getUiModeNight())
                .append("\n\n-------- DisplayMetrics --------")
                .append("\ngetDensityDpi: ").append(getDensityDpi())
                .append("\ngetLogicalDensity: ").append(getLogicalDensity())
                .append("\ngetScaledDensityForFonts: ").append(getScaledDensityForFonts())
                .append("\ngetExactXDpi: ").append(getExactXDpi())
                .append("\ngetExactYDpi: ").append(getExactYDpi())
                .append("\ngetAbsoluteWidthPixels: ").append(getAbsoluteWidthPixels())
                .append("\ngetAbsoluteHeightPixels: ").append(getAbsoluteHeightPixels())
                .append("\nconvertPixelsToDp(getAbsoluteWidthPixels()): ").append(convertPixelsToDp(getAbsoluteWidthPixels()))
                .append("\nconvertPixelsToDp(getAbsoluteHeightPixels()): ").append(convertPixelsToDp(getAbsoluteHeightPixels()))
                .append("\nconvertPixelsToIntegerDp(getAbsoluteWidthPixels()): ").append(convertPixelsToIntegerDp(getAbsoluteWidthPixels()))
                .append("\nconvertPixelsToIntegerDp(getAbsoluteHeightPixels()): ").append(convertPixelsToIntegerDp(getAbsoluteHeightPixels()))
                .append("\nconvertDpToPixels(getScreenWidthDp()): ").append(convertDpToPixels(getScreenWidthDp()))
                .append("\nconvertDpToPixels(getScreenHeightDp()): ").append(convertDpToPixels(getScreenHeightDp()))
                .append("\n");
        return stringBuilder.toString();
    }

}


