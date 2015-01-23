package com.staples.mobile.test;

import com.staples.mobile.cfa.store.TimeSpan;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.TimeZone;

public class TimeSpanTest {
    private static final int ONEWEEK = 7*24*60*60*1000;
    private static final int ONEDAY = 24*60*60*1000;
    private static final int ONEHOUR = 60*60*1000;

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

    public static final String response = "Mon-Wed 9:00AM-5:00PM\n"+
                                          "Thu 24 hour\n"+
                                          "Fri 9:00AM-5:00PM\n"+
                                          "Sat-Sun 9:00AM-2:00PM";

    @Test
    public void testFormatSchedule() {
        ArrayList<TimeSpan> array = new ArrayList<TimeSpan>();
        array.add(TimeSpan.parse("Thursday", "12:01AM - 11:59PM"));
        array.add(TimeSpan.parse("Tuesday", "09:00AM - 05:00PM"));
        array.add(TimeSpan.parse("Sunday", "09:00AM - 02:00PM"));
        array.add(TimeSpan.parse("Wednesday", "09:00AM - 05:00PM"));
        array.add(TimeSpan.parse("Friday", "09:00AM - 05:00PM"));
        array.add(TimeSpan.parse("Saturday", "09:00AM - 02:00PM"));
        array.add(TimeSpan.parse("Monday", "09:00AM - 05:00PM"));
        String result = TimeSpan.formatSchedule(array);
        Assert.assertEquals("Schedule should have formatted correctly", response, result);
    }
}
