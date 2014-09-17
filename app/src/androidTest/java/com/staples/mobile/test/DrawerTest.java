package com.staples.mobile.test;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.staples.mobile.DrawerAdapter;
import com.staples.mobile.DrawerItem;
import com.staples.mobile.MainActivity;
import com.staples.mobile.MainApplication;
import com.staples.mobile.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DrawerTest {
    public static final String TAG = "DrawerTest";

    private MainActivity activity;
    private MainApplication application;

    @Before
    public void startActivity() {
        // Redirect logcat to stdout logfile
        ShadowLog.stream = System.out;

        // Create activity controller
        ActivityController controller = Robolectric.buildActivity(MainActivity.class);

        // Create activity
        controller.create();
        activity = (MainActivity) controller.get();

        // Check for success
        Assert.assertNotNull("Activity should exist", activity);
        application = (MainApplication) activity.getApplication();
        Assert.assertNotNull("Application should exist", application);
    }

    @Test(timeout=2000)
    public void testLeftDrawer() throws InterruptedException {
        System.out.println("testLeftDrawer");
        ListView list = (ListView) activity.findViewById(R.id.left_drawer);
        Assert.assertNotNull("Left drawer should exist", list);
        DrawerAdapter adapter = (DrawerAdapter) list.getAdapter();
        Assert.assertNotNull("Left drawer should have adapter", adapter);
        int count = adapter.getCount();
        Assert.assertEquals("Left drawer should have 18 items", 18, count);

        // Check that Products DrawerItem is ok
        DrawerItem item = adapter.findItemByTitle("Products");
        Assert.assertNotNull("Products DrawerItem should exist", item);

// TODO thread deadlock in Retrofit stalls callbacks
//        for(;;) {
//            if (item.path != null && item.childCount > 0) break;
//            Thread.sleep(100);
//        }
//        Assert.assertNotNull("Products DrawerItem should have path", item.path);
//        Assert.assertTrue("Products DrawerItem should have children", item.childCount>0);
    }

    @Test
    public void testRightDrawer() {
        System.out.println("testRightDrawer");
        ListView list = (ListView) activity.findViewById(R.id.cart_list);
        Assert.assertNotNull("Right drawer should exist", list);
        ArrayAdapter adapter = (ArrayAdapter) list.getAdapter();
        Assert.assertNotNull("Right drawer should have adapter", adapter);
        int count = adapter.getCount();
        Assert.assertEquals("Right drawer should have 3 items", 3, count);
    }
}
