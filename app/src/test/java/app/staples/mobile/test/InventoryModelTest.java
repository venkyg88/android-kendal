package app.staples.mobile.test;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.inventory.StoreInventory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import app.staples.BuildConfig;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21, qualifiers = "port")
public class InventoryModelTest {
    private EasyOpenApi easyOpenApi;
    private boolean success;
    private boolean failure;

    @Before
    public void setUp() {
        Utility.setUp();
        Utility.waitForMcs();
    }

    @After
    public void tearDown() {
        Utility.tearDown();
    }

    @Test
    public void inventoryModelIsCreatedWithLiveCall() throws InterruptedException {
        if (!Utility.doLiveCalls) return;

        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        Assert.assertNotNull("Should have gotten easyOpenApi", easyOpenApi);
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
    public void inventoryModelIsCreatedWithMockCall() throws InterruptedException {
        easyOpenApi = Access.getInstance().getMockEasyOpenApi(Utility.activity);
        Assert.assertNotNull("Should have gotten mockEasyOpenApi", easyOpenApi);
        success = false;
        failure = false;

        //Parameters to the mock API don't matter since it reads from a json file anyway
        easyOpenApi.getStoreInventory(null, null, null, null, new Callback<StoreInventory>() {
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
