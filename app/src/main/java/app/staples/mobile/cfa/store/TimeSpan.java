package app.staples.mobile.cfa.store;

import android.content.Context;

import com.crittercism.app.Crittercism;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;

import app.staples.R;

/** This class represents a span of UTC time within a repeating week.
 * The basic unit is milliseconds since midnight at the start of Thursday.
 */
public class TimeSpan implements Comparable<TimeSpan> {
    public static final int ONEWEEK = 7*24*60*60*1000;
    public static final int ONEDAY = 24*60*60*1000;
    private static final int QUANTA = 5*60*1000;

    // January 1, 1970 was a Thursday
    private static final String[] DAYNAMES = {"Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday"};

    private static final DateFormat parseTimeFormat = new SimpleDateFormat("hh:mma", Locale.US);
    private static final DateFormat longDowFormat = new SimpleDateFormat("EEE");
    private static final DateFormat shortDowFormat = new SimpleDateFormat("EEE");
    private static final DateFormat viewTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

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
            Crittercism.logHandledException(e);
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

    // Basic predicates

    public boolean isClosed() {
        return(start==end);
    }

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

    // Single TimeSpan formatting

    public String format(Context context, String divider) {
        StringBuilder sb = new StringBuilder();

        // Format day(s)
        if (repeat==0) {
            sb.append(longDowFormat.format(start));
        } else {
            sb.append(shortDowFormat.format(start));
            sb.append("-");
            sb.append(shortDowFormat.format(start+repeat*ONEDAY));
        }

        sb.append(divider);

        // Format hours
        if (isClosed()) {
            sb.append(context.getResources().getString(R.string.store_closed));
        }
        else if (is24Hour()) {
            sb.append(context.getResources().getString(R.string.store_24hours));
        } else {
            sb.append(viewTimeFormat.format(start));
            sb.append("-");
            sb.append(viewTimeFormat.format(end));
        }

        return(sb.toString());
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

    public static String formatStatus(Context context, ArrayList<TimeSpan> spans, long now) {
        if (spans==null) return(null);

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
            return(context.getResources().getString(R.string.store_open_until) + " " +
                   formatStatusTime(close, base));
        }

        // Loop for span with shortest time until open
        int until = ONEWEEK;
        for(TimeSpan span : spans) {
            int x = span.untilInsideSpan(base);
            if (x<until) until = x;
        }

        // Open in less than one week
        if (until<ONEWEEK) {
            return(context.getResources().getString(R.string.store_open_at) + " " +
                   formatStatusTime(base+until, base));
        }

        return(context.getResources().getString(R.string.store_closed));
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

    public static String formatSchedule(Context context, ArrayList<TimeSpan> spans) {
        if (spans == null) return (null);

        spans = mergeTimeSpans(spans);
        
        StringBuilder sb = new StringBuilder();
        for(TimeSpan span : spans) {
            if (sb.length()>0) sb.append("\n");
            sb.append(span.format(context, "\t"));
        }
        return (sb.toString());
    }
}
