package com.staples.mobile.cfa.sku;

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
	}

	public AnimatedBarScrollView(Context context, AttributeSet attributeSet){
		super(context, attributeSet);
	}

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        // hide action bar title at first
        if(isFirstLoad) {
            MainActivity.actionBar.getBackground().setAlpha(0);
            MainActivity.titleVw.setTextColor(MainActivity.titleVw.getTextColors().withAlpha(0));
        }
        else{
            MainActivity.setActionBarTitle(SkuFragment.productName);
            MainActivity.actionBar.getBackground().setAlpha(AnimatedBarScrollView.currentAlpha);
            MainActivity.titleVw.setTextColor(
                    MainActivity.titleVw.getTextColors().withAlpha(AnimatedBarScrollView.currentAlpha));
        }

        isFirstLoad = false;

        MainActivity.titleVw.setSingleLine(true);
        MainActivity.titleVw.setLines(1);
        MainActivity.titleVw.setPadding(0, 0, 0, 0);
        MainActivity.titleVw.setTextSize(18f);
        MainActivity.titleVw.setEllipsize(TextUtils.TruncateAt.END);
    }

	@Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        //MainActivity.actionBar.getHeight() + MainActivity.splashHeight

        MainActivity.actionBar.setBackgroundColor(getResources().getColor(R.color.staples_light));

        MainActivity.setActionBarTitle(SkuFragment.productName);

        Float screenHeightDp = convertPixelsToDp(MainActivity.screenHeight, getContext());
        Float currentPositionDp = convertPixelsToDp(getScrollY(), getContext());
        //MainActivity.setActionBarTitle("DP:" + currentPositionDp + " Screen:" + screenHeightDp);

        Float scrollThreshold = screenHeightDp / 4;

		if(currentPositionDp <= scrollThreshold){
            currentAlpha = Math.round(currentPositionDp * MAX_ALPHA / scrollThreshold);
            MainActivity.actionBar.getBackground().setAlpha(currentAlpha);
            MainActivity.titleVw.setTextColor(
                    MainActivity.titleVw.getTextColors().withAlpha(currentAlpha));
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
