package app.staples.mobile.cfa.util;

import android.text.Html;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MiscUtils {
    private static DecimalFormat currencyFormat;

    public static DecimalFormat getCurrencyFormat() {
        if (currencyFormat == null) {
            // set up currency format to use minus sign for negative amounts (needed for coupons)
            currencyFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            String symbol = currencyFormat.getCurrency().getSymbol();
            currencyFormat.setNegativePrefix("-"+symbol);
            currencyFormat.setNegativeSuffix("");
        }
        return currencyFormat;
    }

    public static String cleanupHtml(String text) {
        if (text==null) return(null);
        return(Html.fromHtml(text).toString());
    }

    // Conversion of "divided|strings" to {"divided", "strings"}

    public static List<String> multiStringToList(String multi) {
        if (multi==null) return(null);
        int n = multi.length();
        if (n==0) return(null);

        ArrayList<String> list = new ArrayList<String>();
        int j;
        for(int i=0;i<=n;i=j+1) {
            for(j=i;j<n;j++) {
                if (multi.charAt(j)=='|') break;
            }
            if (j>i) list.add(multi.substring(i, j));
            else list.add(null);
        }
        return(list);
    }

    public static String listToMultiString(List<String> list) {
        if (list==null) return(null);
        int n = list.size();
        if (n==0) return(null);

        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for(String item : list) {
            if (flag) sb.append("|");
            if (item!=null) {
                item = item.trim();
                if (!item.isEmpty()) {
                    sb.append(item);
                }
            }
            flag = true;
        }
        return(sb.toString());
    }

    // Utility for dealing with bad JSON with "N" & "Y" for boolean values

    public static boolean parseBoolean(String string, boolean defaultValue) {
        if (string==null || string.isEmpty()) return(defaultValue);
        if (string.equalsIgnoreCase("N")) return(false);
        if (string.equalsIgnoreCase("Y")) return(true);
        if (string.equalsIgnoreCase("false")) return(false);
        if (string.equalsIgnoreCase("true")) return(true);
        return(defaultValue);
    }
}
