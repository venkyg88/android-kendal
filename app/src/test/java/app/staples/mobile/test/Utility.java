package com.staples.mobile.test;

import app.staples.mobile.cfa.MainActivity;

import org.junit.Assert;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

import java.util.Map;

public class Utility {

    public static ActivityController controller;
    public static MainActivity activity;

    public static boolean doLiveCalls = true;

    static {
        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> entry : env.entrySet()) {
            String name = entry.getKey();
            if (name.equals("NOLIVECALLS")) {
                String value = entry.getValue().trim().toLowerCase();
                if (value.equals("1") || value.equals("true")) {
                    System.out.println("*** Disabling live tests ***");
                    doLiveCalls = false;
                }
            }
        }
    }

    public static void setUp() {
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

    public static void tearDown() {
        controller.destroy();
    }
}
