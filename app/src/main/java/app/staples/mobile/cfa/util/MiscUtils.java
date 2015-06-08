package app.staples.mobile.cfa.util;

import android.text.Html;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
}
