package com.staples.mobile.test;

import android.util.Log;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdate;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.cart.TypedJsonString;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


//These are tested against LIVE sapi calls
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, qualifiers = "port")
public class CartModelTest implements Callback<CartContents> {

    private EasyOpenApi easyOpenApi;
    private boolean success;
    private boolean failure;

    @Test
    public void testCartCanBeViewed() throws InterruptedException {

        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        success = false;
        failure = false;

        Log.d("TIME",""+System.currentTimeMillis());
        easyOpenApi.viewCart("v1",
                "10001",
                "en_US",
                "01010",
                "10051",
                "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS",
                0,
                50,
                this);

        Thread.sleep(5000);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        Assert.assertFalse("Api call should not have failed", failure);
        Assert.assertTrue("Api call should have succeeded", success);
    }

    @Test
    public void testCartItemsCanBeAdded() throws InterruptedException{
        success = false;
        failure = false;
        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        TypedJsonString body = new TypedJsonString("{" +
                "\"orderItem\": [" +
                "{\"partNumber_0\":123455,\"quantity_0\": \"3\"}," +
                "{\"partNumber_1\":487908,\"quantity_1\": \"3\"}" +
                "]}");
        easyOpenApi.addToCart(body,
                "v1",
                "10001",
                "en_US",
                "01010",
                "10051",
                "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS",
                new Callback<CartUpdate>() {
                    @Override
                    public void success(CartUpdate cartUpdate, Response response) {
                        success = true;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        failure = true;
                    }
                });
        Thread.sleep(5000);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        Assert.assertFalse("Api call should not have failed", failure);
        Assert.assertTrue("Api call should have succeeded", success);
    }

    public void success(CartContents cartContents, Response response) {
        success = true;

        List<Cart> cartItems = cartContents.getCart();
        if(cartItems.size()==0){
            System.err.println("Empty Cart");
            return;
        }

        List<Product> products = cartItems.get(0).getProduct();
        for(Product item: products){
            System.out.println("Product:" + item.getProductName()+", qty: "+item.getQuantity());
        }
    }

    public void failure(RetrofitError retrofitError) {
        failure = true;
        retrofitError.printStackTrace();
    }
}
