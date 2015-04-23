package com.staples.mobile.cfa.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CurrencyFormat {
    private static DecimalFormat currencyFormat;

    public static DecimalFormat getFormatter() {
        if (currencyFormat == null) {
            // set up currency format to use minus sign for negative amounts (needed for coupons)
            currencyFormat = (DecimalFormat) NumberFormat.getCurrencyInstance();
            String symbol = currencyFormat.getCurrency().getSymbol();
            currencyFormat.setNegativePrefix("-"+symbol);
            currencyFormat.setNegativeSuffix("");
        }
        return currencyFormat;
    }
}
