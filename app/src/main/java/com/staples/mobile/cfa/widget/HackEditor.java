package com.staples.mobile.cfa.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

public class HackEditor extends EditText implements View.OnClickListener {
    private static final String TAG = "HackEditor";

    private int MINQUANTITY = 1;
    private int MAXQUANTITY = 10;

    private Context context;
    private Dialog popup;

    public HackEditor(Context context) {
        this(context, null, 0);
    }

    public HackEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HackEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        setHint(Integer.toString(MINQUANTITY));
        setOnClickListener(this);
    }

    public int getQuantity() {
        String text = getText().toString();
        if (text==null || text.isEmpty())
            return(MINQUANTITY);
        try {
            int qty = Integer.parseInt(text);
            return(qty);
        } catch(Exception e) {
            return (MINQUANTITY);
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
        if (id>=0 && id<MAXQUANTITY) {
            if (popup!=null) {
                popup.dismiss();
                popup = null;
            }
            setText(Integer.toString(id));
            return;
        }

        // Dialog more select
        if (id==MAXQUANTITY) {
            if (popup!=null) {
                popup.dismiss();
                popup = null;
            }
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
            selectAll();
            return;
        }
    }

    private void showPopup()
    {
        popup = new Dialog(context);
        Window window = popup.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = popup.getLayoutInflater();
        ViewGroup frame = (ViewGroup) inflater.inflate(R.layout.hack_layout, null, false);
        popup.setContentView(frame);

        for(int i=MINQUANTITY;i<=MAXQUANTITY;i++) {
            TextView digit = (TextView) inflater.inflate(R.layout.hack_item, frame, false);
            digit.setId(i);
            String text = Integer.toString(i);
            if (i==MAXQUANTITY) {
                text += "+";
                digit.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            digit.setText(text);
            digit.setOnClickListener(this);
            frame.addView(digit);
        }

        popup.show();
        frame.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int width = frame.getMeasuredWidth();
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
