package com.staples.mobile.cfa.profile;

import android.text.InputFilter;
import android.text.Spanned;

public class ExpiryDateInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence subseq = source.subSequence(start, end);
        StringBuilder replacementBuf = new StringBuilder(subseq);

        String textToCheck = dest.subSequence(0, dstart).toString() +
                source.subSequence(start, end) +
                dest.subSequence(dend, dest.length()).toString();

        int indexOfSlash = textToCheck.indexOf("/");
        if (indexOfSlash > 2 || textToCheck.length() > 5) {
            replacementBuf.setLength(0); // don't allow more than max chars, or more then 2 chars before slash
        } else if (indexOfSlash == -1) {
            if (textToCheck.length() > 1 && dstart < 3 && (dstart + end-start) > 1) {
                replacementBuf.insert(2 - dstart, '/');
            }
        }
        return replacementBuf.toString();
    }
}
