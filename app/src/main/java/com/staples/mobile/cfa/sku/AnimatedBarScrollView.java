package com.staples.mobile.cfa.sku;

/**
 * Author: Yongnan Zhou
 */

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ScrollView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;

public class AnimatedBarScrollView extends ScrollView
{
    private final String TAG = "AnimatedBarScrollView";
    private final int MAX_ALPHA = 255;
    public static int currentAlpha;
    public static boolean isFirstLoad = true;

    public AnimatedBarScrollView(Context context){
        super(context);
        MainActivity mainActivity = (MainActivity) getContext();
    }

    public AnimatedBarScrollView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        MainActivity mainActivity = (MainActivity) getContext();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.getContainFrame().setPadding(0, 0, 0, 0);

        // hide action bar title at first
        if(isFirstLoad) {
            mainActivity.getBar().getBackground().setAlpha(0);
            mainActivity.getTitleView().setTextColor(mainActivity.getTitleView().getTextColors().withAlpha(0));
        }
        else{
            mainActivity.getTitleView().setText(SkuFragment.productName);
            mainActivity.getBar().getBackground().setAlpha(AnimatedBarScrollView.currentAlpha);
            mainActivity.getTitleView().setTextColor(
                    mainActivity.getTitleView().getTextColors().withAlpha(AnimatedBarScrollView.currentAlpha));
        }

        isFirstLoad = false;

        // set action bar title format
        mainActivity.getTitleView().setSingleLine(true);
        mainActivity.getTitleView().setLines(1);
        mainActivity.getTitleView().setPadding(0, 0, 0, 0);
        mainActivity.getTitleView().setTextSize(18f);
        mainActivity.getTitleView().setEllipsize(TextUtils.TruncateAt.END);

        mainActivity.getLeftDrawer().setPadding(0, (int) convertDpToPixel((float) 56, getContext()), 0, 0);
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.getBar().setBackgroundColor(getResources().getColor(R.color.staples_light));

        mainActivity.getTitleView().setText(SkuFragment.productName);

        Float screenHeightDp = convertPixelsToDp(mainActivity.getScreenHeight(), getContext());
        Float currentPositionDp = convertPixelsToDp(getScrollY(), getContext());
        //MainActivity.setActionBarTitle("DP:" + currentPositionDp + " Screen:" + screenHeightDp);

        Float scrollThreshold = screenHeightDp / 4;

        if(currentPositionDp <= scrollThreshold){
            currentAlpha = Math.round(currentPositionDp * MAX_ALPHA / scrollThreshold);
            mainActivity.getTitleView().setTextColor(
                    mainActivity.getTitleView().getTextColors().withAlpha(currentAlpha));
            mainActivity.getBar().getBackground().setAlpha(currentAlpha);
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }

    private float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    private float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }
}
