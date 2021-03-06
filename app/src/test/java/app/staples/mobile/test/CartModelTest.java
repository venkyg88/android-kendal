package app.staples.mobile.test;

import android.util.Log;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdate;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.cart.TypedJsonString;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import app.staples.BuildConfig;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

//These are tested against LIVE sapi calls
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, qualifiers = "port")
public class CartModelTest {
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
    public void testCartCanBeViewed() throws InterruptedException {
        if (!Utility.doLiveCalls) return;

        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        Assert.assertNotNull("Should have gotten EasyOpenApi", easyOpenApi);
        success = false;
        failure = false;

        easyOpenApi.viewCart(0, 50,
                new Callback<CartContents>() {
                    @Override
                    public void success(CartContents cartContents, Response response) {
                        success = true;

                        List<Cart> cartItems = cartContents.getCart();
                        if (cartItems==null || cartItems.size()==0) {
                            System.err.println("Empty Cart");
                            return;
                        }

                        List<Product> products = cartItems.get(0).getProduct();
                        for(Product item : products) {
                            System.out.println("Product:" + item.getProductName() + ", qty: " + item.getQuantity());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        failure = true;
                        error.printStackTrace();
                    }
                });

        Thread.sleep(10000);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertFalse("Api call should not have failed", failure);
        Assert.assertTrue("Api call should have succeeded", success);
    }

    @Test
    public void testCartItemsCanBeAdded() throws InterruptedException{
        if (!Utility.doLiveCalls) return;

        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        Assert.assertNotNull("Should have gotten EasyOpenApi", easyOpenApi);
        success = false;
        failure = false;

        TypedJsonString body = new TypedJsonString("{" +
                "\"orderItem\": [" +
                "{\"partNumber_0\":733333,\"quantity_0\": \"3\"}," +
                "{\"partNumber_1\":487908,\"quantity_1\": \"3\"}" +
                "]}");
        easyOpenApi.addToCart(body,
                new Callback<CartUpdate>() {
                    @Override
                    public void success(CartUpdate cartUpdate, Response response) {
                        success = true;
                        Log.d("testCartItemsCanBeAdded", cartUpdate.getMessage());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        failure = true;
                        error.printStackTrace();
                    }
                });

        Thread.sleep(10000);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertFalse("Api call should not have failed", failure);
        Assert.assertTrue("Api call should have succeeded", success);
    }
}
