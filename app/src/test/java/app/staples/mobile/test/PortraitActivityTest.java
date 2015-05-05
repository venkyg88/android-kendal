package app.staples.mobile.test;

import android.widget.ListView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import app.staples.BuildConfig;
import app.staples.R;
import app.staples.mobile.cfa.DrawerAdapter;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21, qualifiers = "port")
public class PortraitActivityTest {
    private static final String TAG = PortraitActivityTest.class.getSimpleName();

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
        Assert.assertTrue("Left drawer should have a bunch of items", count>5 && count<20);
    }
}
