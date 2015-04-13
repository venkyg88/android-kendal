package com.staples.mobile.test;

import android.widget.ListView;

import com.staples.mobile.cfa.DrawerAdapter;
import com.staples.mobile.cfa.R;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 21, qualifiers = "land")
public class LandscapeActivityTest {
    private static final String TAG = LandscapeActivityTest.class.getSimpleName();

    @Before
    public void setUp() {
        Utility.setUp();
    }

    @After
    public void tearDown() {
        Utility.tearDown();
    }

    @Test
    public void testLeftDrawer() {
        ListView menu = (ListView) Utility.activity.findViewById(R.id.left_menu);
        Assert.assertNotNull("Left drawer should contain a list", menu);

        DrawerAdapter adapter = (DrawerAdapter) menu.getAdapter();
        Assert.assertNotNull("Left drawer should have adapter", adapter);

        int count = adapter.getCount();
        Assert.assertEquals("Left drawer should have 11 items", 11, count);
    }
}
