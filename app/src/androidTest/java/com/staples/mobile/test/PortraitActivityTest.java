package com.staples.mobile.test;

import android.widget.ListView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.DrawerAdapter;
import com.staples.mobile.cfa.MainActivity;

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
@Config(emulateSdk = 18, qualifiers = "port")
public class PortraitActivityTest {
    private static final String TAG = "PortraitActivityTest";

    private ActivityController controller;
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
    public void testLeftDrawer() {
        ListView menu = (ListView) activity.findViewById(R.id.left_menu);
        Assert.assertNotNull("Left drawer should contain a list", menu);

        DrawerAdapter adapter = (DrawerAdapter) menu.getAdapter();
        Assert.assertNotNull("Left drawer should have adapter", adapter);

        int count = adapter.getCount();
        Assert.assertEquals("Left drawer should have 10 items", 10, count);
    }
}
