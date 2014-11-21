package com.staples.mobile.cfa.store;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** This class represents a span of local time within a repeating week.
 * The basic unit is milliseconds since midnight at the start of Sunday.
 */
public class TimeSpan {
    public static final int ONEWEEK = 604800000;
    public static final int ONEDAY =   86400000;

    // January 1, 1970 was a Thursday
    private static final String[] DAYNAMES = {"Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday"};
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma");

    private int start;
    private int end;

    public TimeSpan(long start, long end) {
        this.start = (int) (start%ONEWEEK);
        this.end = (int) (end%ONEWEEK);
        if (this.end<=this.start) this.end += ONEWEEK;
    }

    public static TimeSpan parse(String dayName, String hours) {
        if (dayName==null || hours==null) return(null);

        // Parse dayName
        dayName = dayName.trim();
        int i;
        for(i=0;i<7;i++)
            if (dayName.equalsIgnoreCase(DAYNAMES[i])) break;
        if (i>=7) return(null);
        long start = i*ONEDAY;
        long end = start;

        // Parse hours
        hours = hours.trim();
        String[] chunks = hours.split("-");
        if (chunks.length!=2) return(null);
        try {
            start += timeFormat.parse(chunks[0]).getTime();
            end += timeFormat.parse(chunks[1]).getTime();
            if (end < start) end += ONEDAY;
        } catch(Exception e) {
            return(null);
        }

        // Make TimeSpan
        TimeSpan span = new TimeSpan(start, end);
        return(span);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int untilOutsideSpan(long when) {
        int x = (int) (when%ONEWEEK);

        if (x<start) x += ONEWEEK;
        if (x<end) return(end-x);
        return(0);
    }

    public int untilInsideSpan(long when) {
        int x = (int) (when%ONEWEEK);

        int a = end;
        int b = start;
        if (a>=ONEWEEK) a -= ONEWEEK;
        else b+= ONEWEEK;

        if (x<a) x += ONEWEEK;
        if (x<b) return(b-x);
        return(0);
    }

    public String format(int dateStyle, DateFormat timeFormat) {
        Calendar calendar = timeFormat.getCalendar();
        calendar.setTimeInMillis(start);
        String dow = calendar.getDisplayName(Calendar.DAY_OF_WEEK, dateStyle, Locale.getDefault());
        String open = timeFormat.format(start);
        String close = timeFormat.format(end);
        return(dow+" "+open+"-"+close);
    }
}
