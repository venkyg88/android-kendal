package app.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import app.staples.R;

public class DualHintEdit extends EditText implements TextWatcher {
    private static final String TAG = DualHintEdit.class.getSimpleName();

    public interface OnUpdatedTextListener {
        void onUpdatedText(DualHintEdit view);
    }

    private TextPaint dualPaint;
    private int dualSize;
    private int dualGap;
    private String dualHint;
    private int dualX;
    private int dualY;
    private OnUpdatedTextListener listener;

    // Constructors

    public DualHintEdit(Context context) {
        super(context);
        init(context, null);
    }

    public DualHintEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DualHintEdit(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Preset default attributes
        CharSequence hint = getHint();
        if (hint!=null) {
            dualHint = getHint().toString();
        }
        dualSize = (int) (0.75f*getTextSize());
        dualGap = (dualSize+2)/4;
        Typeface dualTypeface = getTypeface();
        int dualColor = context.getResources().getColor(R.color.staples_dark_gray);

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DualHintEdit);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.DualHintEdit_dualHint:
                    dualHint = a.getString(index);
                    break;
                case R.styleable.DualHintEdit_dualSize:
                    dualSize = a.getDimensionPixelSize(index, dualSize);
                    break;
                case R.styleable.DualHintEdit_dualGap:
                    dualGap = a.getDimensionPixelSize(index, dualGap);
                    break;
                case R.styleable.DualHintEdit_dualColor:
                    dualColor = a.getInt(index, dualColor);
                    break;
            }
        }
        a.recycle();

        // Cleanup text
        if (dualHint!=null && dualHint.isEmpty()) {
            dualHint = null;
        }

        // Set paint
        dualPaint = new TextPaint();
        dualPaint.setAntiAlias(true);
        dualPaint.setTypeface(dualTypeface);
        dualPaint.setTextSize(dualSize);
        dualPaint.setColor(dualColor);

        // Set metrics
        dualX = getPaddingLeft();
        dualY = getPaddingTop()-(int) dualPaint.ascent();
    }

    public void setOnUpdatedTextListener(OnUpdatedTextListener listener) {
        this.listener = listener;
        addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void
    afterTextChanged(Editable s) {
        if (listener!=null) {
            listener.onUpdatedText(this);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        height += dualSize+dualGap;
        setMeasuredDimension(width, resolveSize(height, heightSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        CharSequence text = getText();
        if (text!=null && text.length()>0) {
            if (dualHint!=null) {
                canvas.drawText(dualHint, 0, dualHint.length(), dualX, dualY, dualPaint);
            }
        }
    }
}
