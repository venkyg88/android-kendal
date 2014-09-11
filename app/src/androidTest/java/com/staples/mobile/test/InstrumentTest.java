package com.staples.mobile.test;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.staples.mobile.DrawerAdapter;
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

import java_cup.Main;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class InstrumentTest {
    public static final String TAG = "ActivityTest";

    private MainActivity activity;
    private MainApplication application;

    @Before
    public void startActivity() {
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        Assert.assertNotNull(activity);
        application = (MainApplication) activity.getApplication();
        Assert.assertNotNull(application);
    }

//    @Test
//    public void intentionalFail() {
//        Assert.assertTrue(false);
//    }

    @Test
    public void testLeftDrawer() {
        ListView list = (ListView) activity.findViewById(R.id.left_drawer);
        Assert.assertNotNull(list);
        DrawerAdapter adapter = (DrawerAdapter) list.getAdapter();
        Assert.assertNotNull(adapter);
        int count = adapter.getCount();
        Assert.assertEquals(15, count);
    }

    @Test
    public void testRightDrawer() {
        ListView list = (ListView) activity.findViewById(R.id.cart_list);
        Assert.assertNotNull(list);
        ArrayAdapter adapter = (ArrayAdapter) list.getAdapter();
        Assert.assertNotNull(adapter);
        int count = adapter.getCount();
        Assert.assertEquals(3, count);
    }
}
