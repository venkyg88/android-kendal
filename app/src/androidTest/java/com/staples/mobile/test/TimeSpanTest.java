package com.staples.mobile.test;

import com.staples.mobile.cfa.store.TimeSpan;

import org.junit.Assert;
import org.junit.Test;

import java.util.TimeZone;

public class TimeSpanTest {
    private static final int ONEWEEK = 7*24*60*60*1000;
    private static final int ONEDAY = 24*60*60*1000;
    private static final int ONEHOUR = 60*60*1000;

    private static final int offset = -TimeZone.getDefault().getRawOffset();

    @Test
    public void testParse() {
        TimeSpan span;

        span = TimeSpan.parse("Thursday", "12:00AM - 1:00AM");
        Assert.assertEquals("Thursday 12:00AM should parse to 0", 0, span.getStart()-offset);
        Assert.assertEquals("Thursday 1:00AM should parse to 1 hour", ONEHOUR, span.getEnd()-offset);

        span = TimeSpan.parse("Wednesday", "12:01AM - 11:59PM");
        Assert.assertEquals("Wednesday 12:01AM should parse to 6 days", 6*ONEDAY, span.getStart()-offset);
        Assert.assertEquals("Wednesday 11:59PM should parse to 7 days", 7*ONEDAY, span.getEnd()-offset);
    }
}
