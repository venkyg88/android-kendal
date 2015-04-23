package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Extension of EditText that allows listener of soft keyboard BACK event
 * (code taken from http://stackoverflow.com/questions/3425932/detecting-when-user-has-dismissed-the-soft-keyboard)
 */
public class EditTextWithImeBackEvent extends EditText {

    public interface EditTextImeBackListener {
        public abstract void onImeBack(EditTextWithImeBackEvent view, String text);
    }

    private EditTextImeBackListener mOnImeBackListener;

    /** constructor */
    public EditTextWithImeBackEvent(Context context) {
        super(context);
    }

    /** constructor */
    public EditTextWithImeBackEvent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** constructor */
    public EditTextWithImeBackEvent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBackListener != null) {
                mOnImeBackListener.onImeBack(this, this.getText().toString());
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnImeBackListener(EditTextImeBackListener listener) {
        mOnImeBackListener = listener;
    }

}

