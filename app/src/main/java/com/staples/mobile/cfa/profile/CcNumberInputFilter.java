package com.staples.mobile.cfa.profile;

import android.text.InputFilter;
import android.text.Spanned;

public class CcNumberInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence subseq = source.subSequence(start, end);
        StringBuilder replacementBuf = new StringBuilder(subseq);

        String textToCheck = dest.subSequence(0, dstart).toString() +
                source.subSequence(start, end) +
                dest.subSequence(dend, dest.length()).toString();

        if (dstart == 3 || dstart % 5 == 3) {
            int indexOfSpace = textToCheck.indexOf(" ", dstart);
            if (indexOfSpace == -1 && replacementBuf.length() > 0) {
                replacementBuf.insert(1, ' ');
            }
        }
        return replacementBuf.toString();
    }
}
