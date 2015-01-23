package com.staples.mobile.cfa.store;

import android.text.SpannableStringBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;

/** This class represents a span of UTC time within a repeating week.
 * The basic unit is milliseconds since midnight at the start of Thursday.
 */
public class TimeSpan implements Comparable<TimeSpan> {
    public static final int ONEWEEK = 7*24*60*60*1000;
    public static final int ONEDAY = 24*60*60*1000;
    private static final int QUANTA = 5*60*1000;

    // January 1, 1970 was a Thursday
    private static final String[] DAYNAMES = {"Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday"};

    private static final SimpleDateFormat parseTimeFormat = new SimpleDateFormat("hh:mma");
    private static final SimpleDateFormat longDowFormat = new SimpleDateFormat("EEE");
    private static final SimpleDateFormat shortDowFormat = new SimpleDateFormat("EEE");
    private static final SimpleDateFormat viewTimeFormat = new SimpleDateFormat("h:mma");

    // Week starts on Monday local time
    private static final int origin = 4*ONEDAY-TimeZone.getDefault().getRawOffset();

    private int start;
    private int end;
    private int repeat;

    public TimeSpan(long start, long end) {
        this.start = (int) (start%ONEWEEK);
        this.end = (int) (end%ONEWEEK);
        if (this.end<=this.start) this.end += ONEWEEK;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
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
            start += parseTimeFormat.parse(chunks[0]).getTime();
            end += parseTimeFormat.parse(chunks[1]).getTime();
            if (end<start) end += ONEDAY;
        } catch(Exception e) {
            return(null);
        }

        // Quantize
        start = QUANTA*((start+QUANTA/2)/QUANTA);
        end = QUANTA*((end+QUANTA/2)/QUANTA);
        if (end<start) end+= ONEDAY;

        // Make TimeSpan
        TimeSpan span = new TimeSpan(start, end);
        return(span);
    }

    public int untilOutsideSpan(long now) {
        int base = (int) (now%ONEWEEK);

        if (base<start) base += ONEWEEK;
        if (base<end) return(end-base);
        return(0);
    }

    public int untilInsideSpan(long now) {
        int base = (int) (now%ONEWEEK);

        // Invert endpoints
        int a = end;
        int b = start;
        if (a>=ONEWEEK) a -= ONEWEEK;
        else b+= ONEWEEK;

        if (base<a) base += ONEWEEK;
        if (base<b) return(b-base);
        return(0);
    }

    // Normalized time and sorting

    private static int getNormalizedTime(int x) {
        x -= origin;
        if (x<0) x+= ONEWEEK;
        else if (x>=ONEWEEK) x -= ONEWEEK;
        return(x);
    }

    public int compareTo(TimeSpan another) {
        if (another==null) return(1);

        int s = Integer.signum(getNormalizedTime(start)-getNormalizedTime(another.start));
        if (s!=0) return(s);
        s = Integer.signum(getNormalizedTime(end)-getNormalizedTime(another.end));
        return(s);
    }

    // Single TimeSpan formatting

    public boolean is24Hour() {
        // Does it start at midnight?
        int x = getNormalizedTime(start);
        if ((x%ONEDAY)!=0) return(false);

        // Does it end at midnight?
        x = getNormalizedTime(end)-x;
        if (x<0) x += ONEWEEK;
        if (x!=ONEDAY) return(false);

        return(true);
    }

    private String formatDays() {
        if (repeat==0) {
            return(longDowFormat.format(start));
        } else {
            return(shortDowFormat.format(start)+ "-" +
                   shortDowFormat.format(start+repeat*ONEDAY));
        }
    }

    private String formatHours() {
        if (is24Hour()) {
            return("24 hour");
        } else {
            return(viewTimeFormat.format(start) + "-" +
                   viewTimeFormat.format(end));
        }
    }

    public String toString() {
        return(formatDays()+" "+formatHours());
    }

    // Static methods for formatting current status of store

    private static String formatStatusTime(int time, int base) {
        if (getNormalizedTime(time)/ONEDAY==getNormalizedTime(base)/ONEDAY) {
            return(viewTimeFormat.format(time));
        } else {
            return(shortDowFormat.format(time) + " " +
                   viewTimeFormat.format(time));
        }
    }

    public static String formatStatus(ArrayList<TimeSpan> spans, long now) {
        int base = (int) (now%ONEWEEK);

        // Loop for contiguous spans
        int close = base;
        for(;;) {
            // Loop for span with longest open
            int until = 0;
            for(TimeSpan span : spans) {
                int x = span.untilOutsideSpan(close);
                if (x>until) until = x;
            }

            // No span found
            if (until==0) break;

            // Extend close
            close += until;
        }

        // Close extended at least once
        if (close>base) {
            return ("Open until " + formatStatusTime(close, base));
        }

        // Loop for span with shortest time until open
        int until = ONEWEEK;
        for(TimeSpan span : spans) {
            int x = span.untilInsideSpan(base);
            if (x<until) until = x;
        }

        // Open in less than one week
        if (until<ONEWEEK) {
            return ("Open at " + formatStatusTime(base+until, base));
        }

        return("Closed");
    }

    // Static methods for formatting of store schedule

    private static boolean mergeTimeSpans(TimeSpan a, TimeSpan b) {
        // Starts are a day apart?
        int x = b.start-a.start;
        if (x<0) x += ONEWEEK;
        else if (x>=ONEWEEK) x -= ONEWEEK;
        if (x!=ONEDAY) return(false);

        // Ends are a day apart?
        x = b.end-a.end;
        if (x<0) x += ONEWEEK;
        else if (x>=ONEWEEK) x -= ONEWEEK;
        if (x!=ONEDAY) return(false);

        // Merge
        a.repeat = b.repeat+1;
        return(true);
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<TimeSpan> mergeTimeSpans(ArrayList<TimeSpan> spans) {
        if (spans==null) return(null);

        // Clone & sort
        spans = (ArrayList<TimeSpan>) spans.clone();
        Collections.sort(spans);

        // Merge
        int n = spans.size();
        for(int i=n-2;i>=0;i--) {
            TimeSpan a = spans.get(i);
            TimeSpan b = spans.get(i+1);
            if (mergeTimeSpans(a, b)) {
                spans.remove(i+1);
            }
        }
        return(spans);
    }

    public static String formatSchedule(ArrayList<TimeSpan> spans) {
        if (spans == null) return (null);

        spans = mergeTimeSpans(spans);
        
        StringBuilder sb = new StringBuilder();
        for(TimeSpan span : spans) {
            if (sb.length()>0) sb.append("\n");
            sb.append(span.formatDays());
            sb.append(" ");
            sb.append(span.formatHours());
        }
        return (sb.toString());
    }
}
