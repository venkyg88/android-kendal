package com.staples.mobile.cfa.util;

import android.util.Log;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    private static final String TAG = "DateUtils";

    private static final String TWO_DIGIT_MONTH_YEAR = "MM/yy";

    private static final boolean LOGGING = true;

    public static boolean validateCreditCardExpDate(TextView expDateView) {

        boolean dateValid = false;

        String expDateStr = null;
        String expDateFormatted = null;

        if (expDateView != null) {

            Date expDate = null;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TWO_DIGIT_MONTH_YEAR);

            expDateStr = expDateView.getText().toString().trim();

            try {

                // A MM/yy date like 16/99 will be parsed and not cause a
                // ParseException. The resulting Date object will reflect a date
                // of "04/00/2100". This is the result of subtracting 12 months
                // from 16 and incrementing the year by 1. This is a known
                // issue.
                expDate = simpleDateFormat.parse(expDateStr);

                // Single digit months and years will be parsed successfully
                // with two-digit month and year patterns. The format() method
                // will left pad both with '0'.
                expDateFormatted = simpleDateFormat.format(expDate);
                expDateView.setText(expDateFormatted);

            } catch (ParseException parseException ) {
                expDate = null; // insurance
            }

            dateValid = (expDate != null);
        }

        Log.v(TAG, "DateUtils:validateCreditCardExpDate():"
            + " dateValid[" + dateValid + "]"
            + " expDateStr[" + expDateStr + "]"
            + " expDateFormatted[" + expDateFormatted + "]"
        );

        return (dateValid);
    }
}
