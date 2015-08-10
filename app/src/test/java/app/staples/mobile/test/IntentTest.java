package app.staples.mobile.test;

import android.content.UriMatcher;
import android.net.Uri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import app.staples.BuildConfig;
import app.staples.mobile.cfa.MainActivity;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, qualifiers = "port")
public class IntentTest {
    private static final String TAG = IntentTest.class.getSimpleName();

    private class Example {
        private String input;
        private String result;

        private Example(String input, String result) {
            this.input = input;
            this.result = result;
        }
    }

    private Example[] examples = {
        // CFA scheme
        new Example("http://staples.com",                                             null),
        new Example("http://staples.com/",                                            null),
        new Example("http://staples.com/cfa",                                         null),
        new Example("http://staples.com/cfa/",                                        null),
        new Example("http://staples.com/cfa/home",                                    "MATCH_CFA_HOME"),
        new Example("http://staples.com/cfa/home/",                                   "MATCH_CFA_HOME"),
        new Example("http://staples.com/cfa/home/cow",                                null),
        new Example("http://staples.com/cfa/sku",                                     null),
        new Example("http://staples.com/cfa/sku/",                                    null),
        new Example("http://staples.com/cfa/sku/123455",                              "MATCH_CFA_SKU"),
        new Example("http://staples.com/cfa/sku/123455/",                             "MATCH_CFA_SKU"),
        new Example("http://staples.com/cfa/sku/123455/cow",                          null),
        new Example("http://staples.com/cfa/category/BI739472",                       "MATCH_CFA_CATEGORY"),
        new Example("http://staples.com/cfa/category/BI739472/",                      "MATCH_CFA_CATEGORY"),
        new Example("http://staples.com/cfa/category/BI739472/cow",                   null),
        new Example("http://staples.com/cfa/category/CL90000",                        "MATCH_CFA_CATEGORY"),
        new Example("http://staples.com/cfa/category/CL90000/",                       "MATCH_CFA_CATEGORY"),
        new Example("http://staples.com/cfa/category/CL90000/cow",                    null),
        new Example("http://staples.com/cfa/search/cat",                              "MATCH_CFA_SEARCH"),
        new Example("http://staples.com/cfa/search/cat/",                             "MATCH_CFA_SEARCH"),
        new Example("http://staples.com/cfa/search/cat/cow",                          null),
        // Mobile web
        new Example("http://m.staples.com",                                           "MATCH_MWEB_HOME"),
        new Example("http://m.staples.com/",                                          "MATCH_MWEB_HOME"),
        new Example("http://m.staples.com/moo",                                       null),
        new Example("http://m.staples.com/moo/cow",                                   "MATCH_MWEB_LINK"),
        new Example("http://m.staples.com/moo/brown/cow",                             null),
        // Weekly ad
        new Example("http://weeklyad.staples.com/StaplesSD/WeeklyAd?storeid=2478536", "MATCH_WEEKLYAD"),
        // Errors
        new Example("foo://bogus",                                                    null)
    };

    @Before
    public void setUp() {
        Utility.setUp();
    }

    @After
    public void tearDown() {
        Utility.tearDown();
    }

    private UriMatcher getUriMatcher() {
        Object obj = null;
        try {
            Field field = MainActivity.class.getDeclaredField("uriMatcher");
            field.setAccessible(true);
            obj = field.get(null);
        } catch(Exception e) {}
        if (!(obj instanceof UriMatcher)) {
            Assert.fail("Can't access uriMatcher");
        }
        return((UriMatcher) obj);
    }

    private int getConstant(String name) {
        if (name==null) return(UriMatcher.NO_MATCH);

        int value = 0;
        try {
            Field field = MainActivity.class.getDeclaredField(name);
            field.setAccessible(true);
            value = field.getInt(null);
        } catch(Exception e) {
            Assert.fail("Can't access constant "+name);
        }
        return(value);
    }

    @Test
    public void testIntents() {
        UriMatcher uriMatcher = getUriMatcher();
        for (Example example : examples) {
            Uri uri = Uri.parse(example.input);
            int expected = getConstant(example.result);
            int actual = uriMatcher.match(uri);
            Assert.assertEquals(example.input+" did not match", expected, actual);
        }
    }
}
