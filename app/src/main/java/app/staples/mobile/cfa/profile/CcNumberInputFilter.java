package app.staples.mobile.cfa.profile;

import android.text.InputFilter;
import android.text.Spanned;

public class CcNumberInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        StringBuilder replacementBuf = new StringBuilder("");
        CharSequence subseq = source.subSequence(start, end);
        boolean isNumeric = subseq.toString().matches("^[0-9]*$");

        if (isNumeric) {

            replacementBuf = new StringBuilder(subseq);

            String textToCheck = dest.subSequence(0, dstart).toString() +
                    source.subSequence(start, end) +
                    dest.subSequence(dend, dest.length()).toString();

            int indexOfSpace = -1;
            if (dstart % 5 == 3) {
                indexOfSpace = textToCheck.indexOf(" ", dstart);
                if (textToCheck.length() < 19) {
                    if (indexOfSpace == -1 && replacementBuf.length() > 0) {
                        replacementBuf.insert(1, ' ');
                    }
                }
            } else {
                int textToCheckLen = textToCheck.length();
                if (textToCheckLen % 5 == 0) {
                    if (textToCheckLen < 19) {
                        indexOfSpace = textToCheck.indexOf(" ", dstart);
                        if (indexOfSpace == -1 && replacementBuf.length() > 0) {
                            replacementBuf.insert(0, ' ');
                        }
                    }
                }
            }
        } // if ( ! isNumeric)

        return replacementBuf.toString();
    }
}
