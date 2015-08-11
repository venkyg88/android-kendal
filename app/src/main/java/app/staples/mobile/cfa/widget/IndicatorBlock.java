package app.staples.mobile.cfa.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;

import app.staples.R;
import app.staples.mobile.cfa.util.MiscUtils;

/**
 * <b>XML attributes</b>
 * android:textStyle
 * android:textSize
 * android:textColor
 * cornerRadius
 * spacingGap
 *
 * String templates use {0} for integer prices and {1} for fractional prices
 */
public class IndicatorBlock extends View implements View.OnClickListener {
    private static final String TAG = IndicatorBlock.class.getSimpleName();

    private class Indicator {
        private float price;
        private String text;
        private int color;
        private Bitmap icon;
        private int explainId;
        private String message;
    }

    private ArrayList<Indicator> array;
    private RectF rect;
    private Paint cellPaint;
    private Paint textPaint;
    private float cornerRadius;
    private int spacingGap;
    private Dialog popup;

    public IndicatorBlock(Context context) {
        this(context, null, 0);
    }

    public IndicatorBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        array = new ArrayList<Indicator>();
        rect = new RectF();

        // Set default values
        int textStyle = Typeface.NORMAL;
        int textSize = 10;
        int textColor = 0xff000000;
        cornerRadius = 0f;
        spacingGap = 0;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndicatorBlock);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.IndicatorBlock_android_textStyle:
                    textStyle = a.getInt(index, textStyle);
                    break;
                case R.styleable.IndicatorBlock_android_textSize:
                    textSize = a.getDimensionPixelSize(index, textSize);
                    break;
                case R.styleable.IndicatorBlock_android_textColor:
                    textColor = a.getInt(index, textColor);
                    break;
                case R.styleable.IndicatorBlock_cornerRadius:
                    cornerRadius = a.getDimension(index, cornerRadius);
                    break;
                case R.styleable.IndicatorBlock_spacingGap:
                    spacingGap = a.getDimensionPixelSize(index, spacingGap);
                    break;
            }
        }
        a.recycle();

        // Make Paints
        cellPaint = new Paint();
        cellPaint.setAntiAlias(true);
        cellPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, textStyle));
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
    }

    public boolean isInfoAvailable() {
        for(Indicator item : array) {
            if (item.explainId > 0) {
                return (true);
            }
        }
        return(false);
    }

    public void reset() {
        array.clear();
        setVisibility(GONE);
        setOnClickListener(null);
    }

    public void addIndicator(float price, int textId, int colorId, int explainId) {
        Indicator item = new Indicator();
        Resources res = getResources();
        item.text = res.getString(textId);
        item.color = res.getInteger(colorId);
        item.explainId = explainId;
        item.message = getResources().getString(R.string.explain_oversized);
        array.add(item);
        setVisibility(VISIBLE);
        requestLayout();
    }

    public void addPricedIndicator(float price, int textId, int colorId, int explainId) {
        Indicator item = new Indicator();
        item.price = price;
        Resources res = getResources();
        String text = res.getString(textId);
        DecimalFormat format = MiscUtils.getIntegerCurrencyFormat();
        item.text = MessageFormat.format(text, format.format(price));
        item.color = res.getInteger(colorId);
        item.explainId = explainId;
        item.message = getResources().getString(R.string.explain_minimum);
        array.add(item);
        setVisibility(VISIBLE);
        requestLayout();
    }

    public void addIcon(int iconId) {
        Resources res = getResources();
        Indicator item = new Indicator();
        item.icon = BitmapFactory.decodeResource(res, iconId);
        array.add(item);
        setVisibility(VISIBLE);
        requestLayout();
    }

    public void enableExplainDialog() {
        setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = 0;
        for(Indicator item : array) {
            if (width>0) width += spacingGap;
            if (item.icon!=null) {
                width += (4*item.icon.getWidth()+4)/5; // Allow for visual size
            } else {
                width += Math.ceil(textPaint.measureText(item.text))+getPaddingLeft()+getPaddingRight();
            }
        }
        int height = (int) textPaint.getTextSize() + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthSpec), resolveSize(height, heightSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (array.size()==0) return;
        rect.top = 0;
        rect.bottom = getHeight();
        rect.left = 0;
        for(Indicator item : array) {
            if (item.icon!=null) {
                // Draw icon
                int width = item.icon.getWidth();
                int height = item.icon.getHeight();
                float y = (getHeight()-getPaddingTop()-getPaddingBottom()-height)/2.0f+getPaddingTop();
                canvas.drawBitmap(item.icon, rect.left-0.125f*width, y, null);
                rect.left += 0.75*width+spacingGap;
            } else {
                // Draw background
                rect.right = rect.left+textPaint.measureText(item.text)+getPaddingLeft()+getPaddingRight();
                cellPaint.setColor(item.color);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellPaint);

                // Draw text
                float y = -0.9f*textPaint.ascent()+getPaddingTop();
                canvas.drawText(item.text, rect.left+getPaddingLeft(), y, textPaint);
                rect.left = rect.right+spacingGap;
            }
        }
    }

    private void rewriteMessages(ViewGroup group, String arg0, String arg1) {
        int n = group.getChildCount();
        for(int i=0;i<n;i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                rewriteMessages((ViewGroup) child, arg0, arg1);
            } else if (child instanceof TextView) {
                String text = ((TextView) child).getText().toString();
                text = MessageFormat.format(text, arg0, arg1);
                ((TextView) child).setText(text);
            }
        }
    }

    private void showExplainDialog() {

        // Making the Dialog more consistent with the rest of the App
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        for(Indicator item : array) {
            if (item.explainId>0) {
                builder.setTitle(item.text);
                builder.setMessage(item.message);
            }
        }

        builder.setPositiveButton(R.string.okay_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();


//        // Create dialog and set window options
//        popup = new Dialog(getContext());
//        Window window = popup.getWindow();
//        window.requestFeature(Window.FEATURE_NO_TITLE);
//        window.setBackgroundDrawableResource(R.drawable.dialog_frame);
//
//        // Configure frame
//        popup.setContentView(R.layout.explain_dialog);
//        popup.findViewById(R.id.dismiss).setOnClickListener(this);
//        popup.setCanceledOnTouchOutside(true);
//
//        // Add individual items
//        DecimalFormat format0 = MiscUtils.getIntegerCurrencyFormat();
//        DecimalFormat format1 = MiscUtils.getCurrencyFormat();
//        ViewGroup frame = (ViewGroup) popup.findViewById(R.id.frame);
//        LayoutInflater inflater = (LayoutInflater) frame.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        for(Indicator item : array) {
//            if (item.explainId>0) {
//                ViewGroup child = (ViewGroup) inflater.inflate(item.explainId, frame, false);
//                String arg0 = format0.format(item.price);
//                String arg1 = format1.format(item.price);
//                rewriteMessages(child, arg0, arg1);
//                frame.addView(child);
//            }
//        }
//
//        popup.show();
    }

    @Override
    public void onClick(View view) {
        if (view==this) {
            showExplainDialog();
            return;
        }

//        switch(view.getId()) {
//            case R.id.dismiss:
//                if (popup!=null) {
//                    popup.dismiss();
//                }
//                break;
//        }
    }
}
