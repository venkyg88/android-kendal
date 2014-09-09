package com.staples.mobile.test;

import android.app.Activity;
import android.util.Log;

import com.staples.mobile.JSONResponse;
import com.staples.mobile.MainActivity;
import com.staples.mobile.TopCategoryFiller;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

    @RunWith(RobolectricTestRunner.class)
public class InstrumentTest extends TestCase {
    public static final String TAG = "InstrumentTest";

    public InstrumentTest() {

    }

    @Test
    public void testSimple() {
        Assert.assertEquals(27, 27);
    }

    @Test(timeout=1000)
    public void notFoundAmI() {
        Assert.assertEquals(10, 10);
    }

    // TopCategoryResponse tests

    @Test
    public void testInvalidURL() {
        TopCategoryFiller.TopCategoryResponse response = (TopCategoryFiller.TopCategoryResponse) JSONResponse.getResponse("cow", TopCategoryFiller.TopCategoryResponse.class);
        Assert.assertEquals(992, response.httpStatusCode);
    }

    @Test
    public void testURLDoesNotExist() {
        TopCategoryFiller.TopCategoryResponse response = (TopCategoryFiller.TopCategoryResponse) JSONResponse.getResponse("http://abcde/", TopCategoryFiller.TopCategoryResponse.class);
        Assert.assertEquals(995, response.httpStatusCode);
    }

    // Activity test

    @Test
    public void testActivityFound() {
        Activity activity = Robolectric.buildActivity(MainActivity.class).create().get();
        Assert.assertNotNull(activity);
        Log.d(TAG, "Activity: " + activity);
    }
}
