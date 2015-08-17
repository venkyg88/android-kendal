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
@Config(constants = BuildConfig.class, emulateSdk = 21, qualifiers = "port")
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
        int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());

        span = TimeSpan.parse("Thursday", "4:00AM - 5:00AM");
        Assert.assertEquals("Thursday 4:00AM should parse to 4 hours", 4*ONEHOUR, span.getStart()+offset);
        Assert.assertEquals("Thursday 5:00AM should parse to 5 hour", 5*ONEHOUR, span.getEnd()+offset);

        span = TimeSpan.parse("Wednesday", "12:01AM - 11:59PM");
        Assert.assertEquals("Wednesday 12:01AM should parse to 6 days", 6*ONEDAY, span.getStart()+offset);
        Assert.assertEquals("Wednesday 11:59PM should parse to 7 days", 7*ONEDAY, span.getEnd()+offset);
    }

    private ArrayList<TimeSpan> getTestSchedule() {
        ArrayList<TimeSpan> spans = new ArrayList<TimeSpan>();
        spans.add(TimeSpan.parse("Thursday", "12:01AM - 11:59PM"));
        spans.add(TimeSpan.parse("Tuesday", "09:00AM - 05:00PM"));
        spans.add(TimeSpan.parse("Sunday", "09:00AM - 02:00PM"));
        spans.add(TimeSpan.parse("Wednesday", "09:00AM - 05:00PM"));
        spans.add(TimeSpan.parse("Friday", "09:00AM - 05:00PM"));
        spans.add(TimeSpan.parse("Saturday", "09:00AM - 02:00PM"));
        spans.add(TimeSpan.parse("Monday", "09:00AM - 05:00PM"));
        return(spans);
    }

    @Test
    public void testFormatStatus() {
        String result;
        long now;

        ArrayList<TimeSpan> spans = getTestSchedule();
        int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());

        now = 1234l*ONEWEEK+ONEDAY+9*ONEHOUR-offset;
        result = TimeSpan.formatStatus(spans, now);
        Assert.assertEquals("Store status incorrect", "Open until 5:00 PM", result);

        now--;
        result = TimeSpan.formatStatus(spans, now);
        Assert.assertEquals("Store status incorrect", "Open at 9:00 AM", result);

        now = 5678l*ONEWEEK-offset;
        result = TimeSpan.formatStatus(spans, now);
        Assert.assertEquals("Store status incorrect", "Open until Fri 12:00 AM", result);

        now--;
        result = TimeSpan.formatStatus(spans, now);
        Assert.assertEquals("Store status incorrect", "Open at Thu 12:00 AM", result);

        result = TimeSpan.formatStatus(new ArrayList<TimeSpan>(), now);
        Assert.assertEquals("Store status incorrect", "Closed", result);
    }

    public static final String response = "Mon-Wed\t9:00 AM-5:00 PM\n"+
            "Thu\t24 hours\n"+
            "Fri\t9:00 AM-5:00 PM\n"+
            "Sat-Sun\t9:00 AM-2:00 PM";

    @Test
    public void testFormatSchedule() {
        ArrayList<TimeSpan> spans = getTestSchedule();
        String result = TimeSpan.formatSchedule(spans);
        Assert.assertEquals("Schedule incorrect", response, result);
    }
}
