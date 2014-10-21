package com.staples.mobile.test;

import android.util.Log;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;

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
public class ObjectModelTest implements Callback<CartContents> {

    private ActivityController controller;
    private MainActivity activity;

    private boolean success;
    private boolean failure;

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

    @Test(timeout = 10000)
    public void testCartCanBeViewed() throws InterruptedException {
        success = false;
        failure = false;
        EasyOpenApi easyOpenApi = Access.getInstance().getMockEasyOpenApi(activity);
        Log.d("TIME",""+System.currentTimeMillis());
        easyOpenApi.viewCart("v1",
                "10001",
                "en_US",
                "01010",
                "10051",
                "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS",
                this);
        Log.d("TIME:", "" + System.currentTimeMillis());
        Thread.sleep(1000);
        Log.d("TIME:",""+System.currentTimeMillis());
        Assert.assertTrue("Api call should have succeeded", success);
        Assert.assertFalse("Api call should not have failed", failure);
    }



    public void success(CartContents cart, Response response) {
        success = true;

    }

    public void failure(RetrofitError retrofitError) {
        failure = true;
//        System.err.println("Network error: "+retrofitError.isNetworkError());
//        System.err.println(retrofitError.getMessage());
//        retrofitError.printStackTrace();
    }
}
