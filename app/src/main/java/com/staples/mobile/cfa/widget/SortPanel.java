package com.staples.mobile.cfa.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

public class SortPanel extends Dialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, Animation.AnimationListener {
    private static final String TAG = SortPanel.class.getSimpleName();

    private static final int[] RADIOBUTTONS = {R.id.sort_best_match, R.id.sort_title_ascending, R.id.sort_title_descending,
                                               R.id.sort_price_ascending, R.id.sort_price_descending, R.id.sort_highest_rated};

    private Activity activity;
    private View.OnClickListener listener;

    public SortPanel(Activity activity) {
        super(activity, R.style.PanelStyle);
        this.activity = activity;
        setContentView(R.layout.sort_panel);

        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        WindowManager.LayoutParams params = window.getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.5f;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        params.y = getSoftButtonBarHeight(activity);

        appendIcon(activity, (TextView) findViewById(R.id.sort_price_ascending), R.drawable.ic_sort_invert_white);
        appendIcon(activity, (TextView) findViewById(R.id.sort_price_descending), R.drawable.ic_sort_white);
    }

    @SuppressLint("NewApi")
    private int getSoftButtonBarHeight(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity!=null) {
                Display display = activity.getWindowManager().getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int usableHeight = point.y;
                display.getRealSize(point);
                int realHeight = point.y;
                if (realHeight>usableHeight)
                    return(realHeight-usableHeight);
            }
        }
        return(0);
    }

    private void appendIcon(Context context, TextView view, int icon) {
        Resources res = context.getResources();
        Drawable image = res.getDrawable(icon);
        if (image!=null) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append(view.getText());
            int i = sb.length();
            sb.append("  ");
            int size = (int) (view.getTextSize() * 1.25);
            image.setBounds(0, 0, size, size);
            sb.setSpan(new ImageSpan(image), i+1, i+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            view.setText(sb);
        }
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedRadioButton(int selected) {
        for(int id : RADIOBUTTONS) {
            RadioButton button = (RadioButton) findViewById(id);
            button.setChecked(id == selected);
        }
    }

    public void show() {
        // Set listeners
        findViewById(R.id.close_sort).setOnClickListener(this);
        for(int id : RADIOBUTTONS) {
            ((RadioButton) findViewById(id)).setOnCheckedChangeListener(this);
        }

        super.show();
        Animation slideUp = AnimationUtils.loadAnimation(activity, R.anim.bottomsheet_slide_up);
        findViewById(R.id.sort_options).startAnimation(slideUp);
    }

    @Override
    public void onAnimationStart(Animation animation) {}

    @Override
    public void onAnimationEnd(Animation animation) {
        dismiss();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {}

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.close_sort:
                Animation slideDown = AnimationUtils.loadAnimation(activity, R.anim.bottomsheet_slide_down);
                slideDown.setAnimationListener(this);
                findViewById(R.id.sort_options).startAnimation(slideDown);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        int selected = button.getId();
        for(int id : RADIOBUTTONS) {
            RadioButton ctl = (RadioButton) findViewById(id);
            ctl.setOnCheckedChangeListener(null);
            ctl.setChecked(id==selected);
            ctl.setOnCheckedChangeListener(this);
        }

        if (listener!=null) {
            listener.onClick(button);
        }


        Animation slideDown = AnimationUtils.loadAnimation(activity, R.anim.bottomsheet_delay_down);
        slideDown.setAnimationListener(this);
        findViewById(R.id.sort_options).startAnimation(slideDown);
    }
}
