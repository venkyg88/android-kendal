package com.staples.mobile.cfa.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

public class HackEditor extends EditText implements View.OnClickListener, TextView.OnEditorActionListener {
    private static final String TAG = "HackEditor";

    public interface OnQtyChangeListener {
        public void onQtyChange(View view, int value);
    }

    private Context context;
    private Dialog popup;
    private OnQtyChangeListener listener;

    private int minQuantity;
    private int maxQuantity;
    private int popupWidth;

    private int quantity;
    private boolean inSpecialNeedOfKeyboard = false;

    // Constructors

    public HackEditor(Context context) {
        this(context, null, 0);
    }

    public HackEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HackEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

        // Preset default attributes
        minQuantity = 1;
        maxQuantity = 10;
        popupWidth = 100;
        quantity = 1;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HackEditor);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.HackEditor_minQuantity:
                    minQuantity = a.getInt(index, minQuantity);
                    break;
                case R.styleable.HackEditor_maxQuantity:
                    maxQuantity = a.getInt(index, maxQuantity);
                    break;
                case R.styleable.HackEditor_popupWidth:
                    popupWidth = a.getDimensionPixelSize(index, popupWidth);
                    break;
            }
        }
        a.recycle();

        setHint(Integer.toString(minQuantity));
        setOnClickListener(this);
        setOnEditorActionListener(this);
    }

    // Public methods

    public void setOnQtyChangeListener(OnQtyChangeListener listener) {
        this.listener = listener;
    }

    public int getQuantity() {
        return(quantity);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        setText(Integer.toString(quantity));
        setSelection(0);
        inSpecialNeedOfKeyboard = false;
    }

    @Override
    public void onClick(View view) {
        // EditText clicks
        if (view == this) {
            if (quantity<maxQuantity && !inSpecialNeedOfKeyboard) showPopup();
            else showKeyboard();
            return;
        }

        // otherwise, the click was on the dialog popup

        // Dialog quantity select
        int id = view.getId();
        if (id>=0 && id<maxQuantity) {
            if (popup!=null) {
                popup.dismiss();
                popup = null;
            }
            quantity = id;
            setText(Integer.toString(quantity));
            setSelection(0);
            inSpecialNeedOfKeyboard = false;
            if (listener!=null)
                listener.onQtyChange(this, id);
            return;
        }

        // Dialog more select
        if (id==maxQuantity) {
            if (popup!=null) {
                popup.dismiss();
                popup = null;
            }
            // Sometimes the attempt to automatically show the keyboard doesn't work (e.g. landscape
            // mode on a small phone). If we don't do something like the following, then the next
            // click will just open the popup again and the user will get nowhere.
            inSpecialNeedOfKeyboard = true;

            postDelayed(new ShowKeyboard(), 100);
            return;
        }
    }

    // Runnables to complete actions

    private class ShowKeyboard implements Runnable {
        @Override
        public void run() {
            showKeyboard();
        }
    }

    private class AfterKeyboard implements Runnable {
        @Override
        public void run() {
            setText(Integer.toString(quantity));
            setSelection(0);
            setFocusable(false);
            setFocusableInTouchMode(false);
            inSpecialNeedOfKeyboard = false;
        }
    }

    // Soft keyboard actions, search & back

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId==EditorInfo.IME_ACTION_DONE) {
            quantity = minQuantity;
            String text = getText().toString();
            if (text != null) {
                try {
                    quantity = Integer.parseInt(text);
                } catch(Exception e) { }
            }
            if (listener != null)
                listener.onQtyChange(this, quantity);
            post(new AfterKeyboard());
        }
        return(false);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_UP) {
            post(new AfterKeyboard());
        }
        return(false);
    }

    // Basic mechanisms

    private void showPopup() {
        popup = new Dialog(context);
        Window window = popup.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);

        popup.setContentView(R.layout.hack_layout);
        ViewGroup strip = (ViewGroup) popup.findViewById(R.id.strip);

        LayoutInflater inflater = popup.getLayoutInflater();
        for(int i=minQuantity;i<=maxQuantity;i++) {
            String text;
            TextView digit = (TextView) inflater.inflate(R.layout.hack_item, strip, false);
            digit.setId(i);
            if (i==0) text = context.getResources().getString(R.string.remove);
            else text = Integer.toString(i);
            if (i==maxQuantity) text += "+";
            digit.setText(text);
            digit.setOnClickListener(this);
            strip.addView(digit);
        }

        popup.show();
        window.setLayout(popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void showKeyboard() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(HackEditor.this, InputMethodManager.SHOW_IMPLICIT);
        selectAll();
    }
}
