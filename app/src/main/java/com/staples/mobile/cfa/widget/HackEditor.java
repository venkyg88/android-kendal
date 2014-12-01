package com.staples.mobile.cfa.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

public class HackEditor extends EditText implements View.OnClickListener, TextView.OnEditorActionListener {
    private static final String TAG = "HackEditor";

    private Context context;
    private Dialog popup;
    private Handler handler;
    private int minQuantity;
    private int maxQuantity;
    private int popupWidth;

    public HackEditor(Context context) {
        this(context, null, 0);
    }

    public HackEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HackEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        handler = new Handler();

        // Preset default attributes
        minQuantity = 1;
        maxQuantity = 10;
        popupWidth = 100;

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

    public int getQuantity() {
        String text = getText().toString();
        if (text==null || text.isEmpty())
            return(minQuantity);
        try {
            int qty = Integer.parseInt(text);
            return(qty);
        } catch(Exception e) {
            return (minQuantity);
        }
    }

    public void setQuantity(int quantity) {
        setText(Integer.toString(quantity));
    }

    @Override
    public void onClick(View view) {
        // EditText clicks
        if (view == this) {
            showPopup();
            return;
        }

        // Dialog quantity select
        int id = view.getId();
        if (id>=0 && id<maxQuantity) {
            if (popup!=null) {
                popup.dismiss();
                popup = null;
            }
            setText(Integer.toString(id));
            return;
        }

        // Dialog more select
        if (id==maxQuantity) {
            if (popup!=null) {
                popup.dismiss();
                popup = null;
            }
            handler.postDelayed(new ShowKeyboard(), 100);
            return;
        }
    }

    private class ShowKeyboard implements Runnable {
        @Override
        public void run() {
            setFocusable(true);
            setFocusableInTouchMode(true);
            selectAll();
            requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(HackEditor.this, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private class DisableFocus implements Runnable {
        @Override
        public void run() {
            setFocusable(false);
            setFocusableInTouchMode(false);
        }
    }

    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        handler.post(new DisableFocus());
        return(false);
    }

    private void showPopup()
    {
        popup = new Dialog(context);
        Window window = popup.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = popup.getLayoutInflater();
        View frame = inflater.inflate(R.layout.hack_layout, null, false);
        popup.setContentView(frame);
        ViewGroup strip = (ViewGroup) frame.findViewById(R.id.strip);

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
}
