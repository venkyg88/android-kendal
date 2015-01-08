package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.search.SearchBarView;

public class ActionBar extends LinearLayout {
    private static final String TAG = "ActionBar";

    public ActionBar(Context context) {
        this(context, null, 0);
    }

    public ActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Generic ActionBar item

    public View addItem(View view, int childId, int gravity, OnClickListener listener) {
        view.setId(childId);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = gravity|Gravity.CENTER_VERTICAL;
        view.setLayoutParams(params);
        view.setOnClickListener(listener);
        addView(view);
        return(view);
    }

    // Specific type ActionBar items

    public TextView addText(int childId, int gravity, OnClickListener listener) {
        TextView view = new TextView(getContext(), null, R.attr.actionBarTextStyle);
        addItem(view, childId, gravity, listener);
        return(view);
    }

    public ImageView addIcon(int childId, int resId, int gravity, OnClickListener listener) {
        ImageView view = new ImageView(getContext(), null, R.attr.actionBarIconStyle);
        if (resId!=0) view.setImageDrawable(getResources().getDrawable(resId));
        addItem(view, childId, gravity, listener);
        return(view);
    }

    public SearchBarView addSearchBar(int childId, int gravity, OnClickListener listener) {
        SearchBarView view = new SearchBarView(getContext(), null, R.attr.actionBarSearchStyle);
        addItem(view, childId, gravity, listener);
        return(view);
    }

    public Button addButton(int childId, int resId, int gravity, OnClickListener listener) {
        Button view = new Button(getContext(), null, R.attr.actionBarButtonStyle);
        if (resId!=0) view.setText(getResources().getString(resId));
        addItem(view, childId, gravity, listener);
        return(view);
    }

    public BadgeImageView addBadge(int childId, int gravity, OnClickListener listener) {
        BadgeImageView view = new BadgeImageView(getContext(), null, R.attr.actionBarBadgeStyle);
        addItem(view, childId, gravity, listener);
        return(view);
    }

    // Measurement & layout

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Get dimensions
        int layoutWidth = MeasureSpec.getSize(widthSpec);
        int slackWidth = layoutWidth-getPaddingLeft()-getPaddingRight();
        int usedHeight = 0;

        // Iterate over children
        int n = getChildCount();
        for(int i=0;i<n;i++) {
            View child = getChildAt(i);
            if (child.getVisibility()!=View.GONE) {
                // Measure child
                child.measure(MeasureSpec.makeMeasureSpec(slackWidth, MeasureSpec.AT_MOST),
                              MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                // Calculate usage
                slackWidth -= childWidth;
                usedHeight = Math.max(usedHeight, childHeight);
            }
        }

        // Adjust for padding and return
        usedHeight += getPaddingTop()+getPaddingBottom();
        setMeasuredDimension(layoutWidth, resolveSize(usedHeight, heightSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Get dimensions
        right -= left;
        bottom -= top;

        // Adjust for padding
        left = getPaddingLeft();
        top = getPaddingTop();
        right -= getPaddingRight();
        bottom -= getPaddingBottom();

        // Iterate over children
        int n = getChildCount();
        for(int i=0;i<n;i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int x, y;
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                LayoutParams params = (LayoutParams) child.getLayoutParams();

                // Horizontal positioning
                switch(params.gravity&Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.RIGHT:
                        x = right-width;
                        right = x;
                        break;
                    case Gravity.CENTER_HORIZONTAL: // Only a last child can be centered
                        x = (left+right-width)/2;
                        break;
                    default:
                        x = left;
                        left += width;
                        break;
                }

                // Vertical centering
                switch(params.gravity&Gravity.VERTICAL_GRAVITY_MASK) {
                    case Gravity.BOTTOM:
                        y = bottom;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        y = (bottom-top-height)/2;
                        break;
                    default:
                        y = top;
                }

                child.layout(x, y, x+width, y+height);
            }
        }
    }
}
