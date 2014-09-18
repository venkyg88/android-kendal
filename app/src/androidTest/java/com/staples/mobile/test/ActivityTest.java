package com.staples.mobile.test;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.staples.mobile.DrawerAdapter;
import com.staples.mobile.DrawerItem;
import com.staples.mobile.EasyOpenApi;
import com.staples.mobile.MainActivity;
import com.staples.mobile.MainApplication;
import com.staples.mobile.R;
import com.staples.mobile.lms.object.Lms;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ActivityTest implements Callback<Lms> {
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

    private boolean success;
    private boolean failure;

    @Test
    public void testMockLandingApi() throws InterruptedException{
        success = false;
        failure = false;
        EasyOpenApi easyOpenApi = application.getMockEasyOpenApi();
        easyOpenApi.lms("Ignore", "Ignore", this);
        Thread.sleep(1000);
        Assert.assertTrue("MockLandingApi should have succeeded", success);
        Assert.assertFalse("MockLandingApi should not have failed", failure);
    }

    public void success(Lms lms, Response response) {
        success = true;
    }

    public void failure(RetrofitError retrofitError) {
        failure = true;
    }
}
