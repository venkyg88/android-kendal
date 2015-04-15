package com.staples.mobile.test;

import com.staples.mobile.cfa.BuildConfig;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.inventory.StoreInventory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21, qualifiers = "port")
public class InventoryModelTest {
    private ActivityController controller;
    private MainActivity activity;
    private EasyOpenApi easyOpenApi;
    private EasyOpenApi mockEasyOpenApi;

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

    @Test
    public void inventoryModelIsCreatedWithLiveCall() throws InterruptedException{
        if (!Utility.doLiveCalls) return;

        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        success = false;
        failure = false;
        //http://sapi.staples.com/v1/10001/stores/inventory?locale=en_US&zipCode=05251&catalogId=10051&partNumber=513096&distance=100&client_id=N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS
        easyOpenApi.getStoreInventory("513096", "25", 0, 50,
                new Callback<StoreInventory>() {
                    @Override
                    public void success(StoreInventory storeInventory, Response response) {
                        success = true;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        failure = true;
                    }
                }
        );

        Thread.sleep(10000);
        Robolectric.flushBackgroundScheduler();
        Robolectric.flushForegroundScheduler();

        Assert.assertFalse("Model creation should not have failed", failure);
        Assert.assertTrue("Model creation should have succeeded", success);
    }
    @Test
    public void inventoryModelIsCreatedWithMockCall() throws InterruptedException{

        mockEasyOpenApi = Access.getInstance().getMockEasyOpenApi(activity);
        success = false;
        failure = false;

        //Parameters to the mock API don't matter since it reads from a json file anyway
        mockEasyOpenApi.getStoreInventory(null, null, null, null, new Callback<StoreInventory>() {
            @Override
            public void success(StoreInventory storeInventory, Response response) {
                success = true;
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                failure = true;
            }
        });

        Thread.sleep(5000);
        Robolectric.flushBackgroundScheduler();
        Robolectric.flushForegroundScheduler();

        Assert.assertFalse("Model creation should not have failed", failure);
        Assert.assertTrue("Model creation should have succeeded", success);
    }


}
