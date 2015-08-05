package app.staples.mobile.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.TimeZone;

import app.staples.BuildConfig;
import app.staples.mobile.cfa.store.TimeSpan;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, qualifiers = "port")
public class TimeSpanTest {
    private static final int ONEWEEK = 7*24*60*60*1000;
    private static final int ONEDAY = 24*60*60*1000;
    private static final int ONEHOUR = 60*60*1000;

    @Before
    public void setUp() {
        Utility.setUp();
    }

    @After
    public void tearDown() {
        Utility.tearDown();
    }

    @Test
    public void testParse() {
        TimeSpan span;
        int offset = -TimeZone.getDefault().getRawOffset();

        span = TimeSpan.parse("Thursday", "12:00AM - 1:00AM");
        Assert.assertEquals("Thursday 12:00AM should parse to 0", 0, span.getStart()-offset);
        Assert.assertEquals("Thursday 1:00AM should parse to 1 hour", ONEHOUR, span.getEnd()-offset);

        span = TimeSpan.parse("Wednesday", "12:01AM - 11:59PM");
        Assert.assertEquals("Wednesday 12:01AM should parse to 6 days", 6*ONEDAY, span.getStart()-offset);
        Assert.assertEquals("Wednesday 11:59PM should parse to 7 days", 7*ONEDAY, span.getEnd()-offset);
    }

    public static final String response = "Mon-Wed\t9:00 AM-5:00 PM\n"+
                                          "Thu\t24 hours\n"+
                                          "Fri\t9:00 AM-5:00 PM\n"+
                                          "Sat-Sun\t9:00 AM-2:00 PM";

    @Test
    public void testFormatSchedule() {
        ArrayList<TimeSpan> spans = new ArrayList<TimeSpan>();
        spans.add(TimeSpan.parse("Thursday", "12:01AM - 11:59PM"));
        spans.add(TimeSpan.parse("Tuesday", "09:00AM - 05:00PM"));
        spans.add(TimeSpan.parse("Sunday", "09:00AM - 02:00PM"));
        spans.add(TimeSpan.parse("Wednesday", "09:00AM - 05:00PM"));
        spans.add(TimeSpan.parse("Friday", "09:00AM - 05:00PM"));
        spans.add(TimeSpan.parse("Saturday", "09:00AM - 02:00PM"));
        spans.add(TimeSpan.parse("Monday", "09:00AM - 05:00PM"));
        String result = TimeSpan.formatSchedule(Utility.activity, spans);
        Assert.assertEquals("Schedule should have formatted correctly", response, result);
    }
}
