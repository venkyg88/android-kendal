package com.staples.mobile.test;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.staples.mobile.cfa.DrawerAdapter;
import com.staples.mobile.common.access.lms.api.LmsApi;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.R;
import com.staples.mobile.common.access.lms.model.Lms;

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

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, qualifiers = "port")
public class ActivityTest implements Callback<Lms> {
    public static final String TAG = "DrawerTest";

    private MainActivity activity;
    private MainApplication application;

    @Before
    public void setup() {
        // Redirect logcat to stdout logfile
        ShadowLog.stream = System.out;

        // Create activity controller
        ActivityController controller = Robolectric.buildActivity(MainActivity.class);
        Assert.assertNotNull("Robolectric controller should not be null", controller);

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
        Assert.assertEquals("Left drawer should have 8 items", 8, count);
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
    public void testMockLmsApi() throws InterruptedException{
        success = false;
        failure = false;
        LmsApi lmsApi = application.getMockLmsApi();
        lmsApi.lms("Ignore", "Ignore", this);
        Thread.sleep(1000);
        Assert.assertTrue("MockLmsApi should have succeeded", success);
        Assert.assertFalse("MockLmsApi should not have failed", failure);
    }

    public void success(Lms lms, Response response) {
        success = true;
    }

    public void failure(RetrofitError retrofitError) {
        failure = true;
    }
}
