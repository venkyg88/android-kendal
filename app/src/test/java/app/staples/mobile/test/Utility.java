package app.staples.mobile.test;

import com.staples.mobile.common.access.config.AppConfigurator;

import org.junit.Assert;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

import java.util.Map;

import app.staples.mobile.cfa.MainActivity;

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

        controller.resume();
    }

    public static void waitForMcs() {
        AppConfigurator instance = AppConfigurator.getInstance();
        int i = 5;
        for(;i>=0;i--) {
            if (instance.getConfigurator() != null) break;
            try {
                Thread.sleep(1000);
            } catch(Exception e) {
            }
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
        }
        if (i<0) Assert.fail("Timeout waiting for MCS");

        // TODO Ugly, wait for login. OMG, we haven't made login a singleton yet?
        try {
            Thread.sleep(5000);
        } catch(Exception e) {}
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
    }

    public static void tearDown() {
        controller.destroy();
    }
}
