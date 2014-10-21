package com.staples.mobile.cfa.home;

import android.app.Activity;
import android.app.Fragment;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.text.TextUtils;

import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.R;
import com.staples.mobile.common.access.lms.LmsManager;
import com.staples.mobile.common.access.lms.LmsManager.LmsMgrCallback;
import com.staples.mobile.common.access.lms.model.Area;
import com.staples.mobile.common.access.lms.model.Item;
import com.staples.mobile.common.access.lms.model.Screen;

import java.util.ArrayList;
import java.util.List;

public class LmsFragment
    extends Fragment
    implements LmsMgrCallback {

    private static final String TAG = "LmsFragment";

    private static final boolean LOGGING = true;

    private MainActivity activity;
    private Resources resources;
    private LayoutInflater layoutInflater;
    private DisplayMetrics displayMetrics;

    private View lmsFrameView;
    private LinearLayout lmsScrollLayout;

    private LmsManager lmsManager;

    private List<LmsItem> lmsItems;
    private List<LmsItem> lmsItemsA;
    private List<LmsItem> lmsItemsB;
    private List<LmsItem> lmsItemsC;
    private List<LmsItem> lmsItemsD;

    private List<Screen> screens;

    private List<Item> items;

    private Picasso picasso;
    private Drawable noPhoto;

    private int parentLayoutWidthPx;
    private int parentLayoutHeightPx;

    private View.OnClickListener itemOnClickListener;

    @Override
    public void onAttach(Activity activity) {

        if (LOGGING) Log.v(TAG, "LmsFragment:onAttach():"
                + " activity[" + activity + "]"
                + " this[" + this + "]"
        );

        super.onAttach(activity);

        this.activity = (MainActivity) activity;
        resources = activity.getResources();
        displayMetrics = resources.getDisplayMetrics();
        lmsManager = new LmsManager();

        lmsItems = new ArrayList<LmsItem>();
        lmsItemsA = new ArrayList<LmsItem>();
        lmsItemsB = new ArrayList<LmsItem>();
        lmsItemsC = new ArrayList<LmsItem>();
        lmsItemsD = new ArrayList<LmsItem>();

        return;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {

        if (LOGGING) Log.v(TAG, "LmsFragment:onCreateView():"
                + " this[" + this + "]"
        );

        picasso = Picasso.with(activity);

        noPhoto = resources.getDrawable(R.drawable.no_photo);

        this.layoutInflater = layoutInflater;
        lmsFrameView = layoutInflater.inflate(R.layout.lms_frame, container, false);

        lmsScrollLayout = (LinearLayout) lmsFrameView.findViewById(R.id.lmsScrollLayout);

        itemOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (LOGGING) Log.v(TAG, "LmsFragment:OnClickListener.onClick():"
                        + " view[" + view + "]"
                        + " this[" + this + "]"
                );
                LmsItem lmsItem = (LmsItem) view.getTag();
                String path = "/category/identifier/" + lmsItem.identifier;
                activity.selectBundle(lmsItem.title, path);
            }
        };

        lmsManager.getLms(this,  // LmsMgrCallback
                          true); // conditional

        return (lmsFrameView);
    }

    public void onGetLmsResult(boolean success) {

        if (LOGGING) Log.v(TAG, "LmsFragment:LmsManager.onGetLmsResult():"
                + " success[" + success + "]"
                + " this[" + this + "]"
        );

        if (success) {

            screens = lmsManager.getScreen();
            Screen screen = screens.get(0);
            items = screen.getItem();

            LmsItem lmsItem = null;
            List<Area> areas = null;
            String skuList = null;

            lmsItems.clear();
            lmsItemsA.clear();
            lmsItemsB.clear();
            lmsItemsC.clear();
            lmsItemsD.clear();

            for (Item item : items) {

                areas = item.getArea();
                skuList = areas.get(0).getSkuList();

                lmsItem = new LmsItem(item.getTitle(), item.getBanner(), skuList, item.getSize());
                lmsItems.add(lmsItem);

                String size = item.getSize();

                if (size.equalsIgnoreCase("A")) {
                    lmsItemsA.add(lmsItem);
                } else if (size.equalsIgnoreCase("B")) {
                    lmsItemsB.add(lmsItem);
                } else if (size.equalsIgnoreCase("C")) {
                    lmsItemsC.add(lmsItem);
                } else if (size.equalsIgnoreCase("D")) {
                    lmsItemsD.add(lmsItem);
                }
            }

            if (lmsItemsA.size() > 0) {
                doLmsItemsABD(lmsItemsA, R.drawable.banner_a_color_small, displayMetrics.widthPixels);
            }
            if (lmsItemsB.size() > 0) {
                doLmsItemsABD(lmsItemsB, R.drawable.banner_b_color_small, (displayMetrics.widthPixels / 2));
            }
            if (lmsItemsC.size() > 0) {
                doLmsItemsC(R.drawable.banner_c_color_small);
            }
            if (lmsItemsD.size() > 0) {
                doLmsItemsABD(lmsItemsD, R.drawable.banner_d_color_small, (displayMetrics.widthPixels / 4));
            }
        }
        activity.showMainScreen();
    }

    private void doLmsItemsABD(List<LmsItem> lmsItems, int bannerResourceId, int subLayoutHeight) {

        if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsABD():"
            + " lmsItems[" + lmsItems + "]"
            + " bannerResourceId[" + bannerResourceId + "]"
            + " subLayoutHeight[" + subLayoutHeight + "]"
            + " this[" + this + "]"
        );

        parentLayoutWidthPx = displayMetrics.widthPixels; // pixels
        parentLayoutHeightPx = displayMetrics.widthPixels; // pixels

        /* @@@ STUBBED
        int subLayoutHeight = displayMetrics.widthPixels;
        @@@ STUBBED */

        for (LmsItem lmsItem : lmsItems) {

            if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsABD(): lmsItem:"
                    + " lmsItem.title[" + lmsItem.title + "]"
                    + " lmsItem.bannerUrl[" + lmsItem.bannerUrl + "]"
                    + " this[" + this + "]"
            );

            LinearLayout.LayoutParams lmsItemLayoutParms = null;

            LinearLayout.LayoutParams widgetLayoutParms = null;

            LinearLayout lmsSubLayout = null;
            TextView titleTextView = null;
            ImageView categoryImageView = null;

            int marginTopPixels = 0;
            int marginBottomPixels = 0;
            int marginLeftPixels = 0;
            int marginRightPixels = 0;

            // LMS Sublayout

            lmsItemLayoutParms =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // width
                                              subLayoutHeight); // height

            lmsSubLayout = new LinearLayout(activity);
            lmsSubLayout.setLayoutParams(lmsItemLayoutParms);

            lmsSubLayout.setId(lmsSubLayout.hashCode());
            /* @@@ STUBBED
            lmsSubLayout.setBackgroundResource(R.drawable.rectangle_frame);
            @@@ STUBBED */
            lmsSubLayout.setOrientation(LinearLayout.VERTICAL);
            lmsSubLayout.setMeasureWithLargestChildEnabled(true);
            lmsSubLayout.setTag(lmsItem);
            lmsSubLayout.setOnClickListener(itemOnClickListener);

            lmsScrollLayout.addView(lmsSubLayout);

            // Title TextView

            titleTextView = new TextView(activity);
            titleTextView.setId(titleTextView.hashCode());
            titleTextView.setGravity(Gravity.LEFT);
            titleTextView.setLines(2);
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            titleTextView.setTextColor(resources.getColor(R.color.sel_text_list_item));
            titleTextView.setTextAppearance(activity, R.style.TextExtraLargeListItem);
            titleTextView.setText(lmsItem.title);

            widgetLayoutParms =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, // width
                                              LinearLayout.LayoutParams.WRAP_CONTENT); // height
            marginLeftPixels = 0;
            marginTopPixels = (int) (4 * displayMetrics.density); // margin in pixels
            marginRightPixels = 0;
            marginBottomPixels = (int) (8 * displayMetrics.density); // margin in pixels

            widgetLayoutParms.setMargins(marginLeftPixels,
                                         marginTopPixels,
                                         marginRightPixels,
                                         marginBottomPixels);

            if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsABD(): titleTextView:"
                    + " marginLeftPixels[" + marginLeftPixels + "]"
                    + " marginTopPixels[" + marginTopPixels + "]"
                    + " marginRightPixels[" + marginRightPixels + "]"
                    + " marginBottomPixels[" + marginBottomPixels + "]"
                    + " this[" + this + "]"
            );

            titleTextView.setLayoutParams(widgetLayoutParms);

            /* @@@ STUBBED
            lmsSubLayout.addView(titleTextView);
            @@@ STUBBED */

            // Category ImageView

            categoryImageView = new ImageView(activity);
            categoryImageView.setId(categoryImageView.hashCode());
            categoryImageView.setAdjustViewBounds(true);
            /* @@@ STUBBED
            categoryImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            @@@ STUBBED */
            categoryImageView.setScaleType(ImageView.ScaleType.FIT_XY);

            widgetLayoutParms =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // width
                                              LinearLayout.LayoutParams.WRAP_CONTENT); // height

            marginLeftPixels = 0;
            marginTopPixels = 0;
            marginRightPixels = 0;
            marginBottomPixels = (int) (4 * displayMetrics.density); // margin in pixels

            /* @@@ STUBBED
            widgetLayoutParms.setMargins(marginLeftPixels,
                                         marginTopPixels,
                                         marginRightPixels,
                                         marginBottomPixels);
            @@@ STUBBED */

            if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsABD(): categoryImageView:"
                    + " subLayoutHeight[" + subLayoutHeight + "]"
                    + " marginLeftPixels[" + marginLeftPixels + "]"
                    + " marginTopPixels[" + marginTopPixels + "]"
                    + " marginRightPixels[" + marginRightPixels + "]"
                    + " marginBottomPixels[" + marginBottomPixels + "]"
                    + " this[" + this + "]"
            );

            categoryImageView.setLayoutParams(widgetLayoutParms);

            lmsSubLayout.addView(categoryImageView);

            /* @@@ STUBBED
            RequestCreator requestCreator = picasso.load(lmsItem.bannerUrl);
            requestCreator.error(noPhoto);
            requestCreator.into(categoryImageView);
            requestCreator.fit();
            @@@ STUBBED */
            /* @@@ STUBBED
            RequestCreator requestCreator = picasso.load(bannerResourceId);
            requestCreator.error(noPhoto);
            requestCreator.into(categoryImageView);
            requestCreator.fit();
            @@@ STUBBED */
            categoryImageView.setImageResource(bannerResourceId);
        }

    } // doLmsItemsABD()

    private void doLmsItemsC(int bannerResourceId) {

        if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsC():"
            + " this[" + this + "]"
        );

        parentLayoutWidthPx = displayMetrics.widthPixels; // pixels
        parentLayoutHeightPx = displayMetrics.widthPixels; // pixels

        int subLayoutWidth = displayMetrics.widthPixels / 2;
        int subLayoutHeight = displayMetrics.widthPixels / 2;

        LinearLayout lmsSubLayoutContainer = null;

        boolean firstSubInContainer = true;
        int lmsItemNbr = -1;
        int lastLmsItem = (lmsItemsC.size() - 1);

        for (LmsItem lmsItem : lmsItemsC) {

            lmsItemNbr++;

            firstSubInContainer = (lmsItemNbr % 2 == 0);

            if (firstSubInContainer) {
                lmsSubLayoutContainer = new LinearLayout(activity);
                lmsSubLayoutContainer.setId(lmsSubLayoutContainer.hashCode());
                lmsSubLayoutContainer.setOrientation(LinearLayout.HORIZONTAL);
                lmsSubLayoutContainer.setMeasureWithLargestChildEnabled(true);
                lmsScrollLayout.addView(lmsSubLayoutContainer);
            }

            if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsC(): lmsItem:"
                    + " lmsItemNbr[" + lmsItemNbr + "]"
                    + " firstSubInContainer[" + firstSubInContainer + "]"
                    + " lmsItem.title[" + lmsItem.title + "]"
                    + " lmsItem.bannerUrl[" + lmsItem.bannerUrl + "]"
                    + " this[" + this + "]"
            );

            LinearLayout.LayoutParams lmsItemLayoutParms = null;

            LinearLayout.LayoutParams widgetLayoutParms = null;

            LinearLayout lmsSubLayout = null;
            TextView titleTextView = null;
            ImageView categoryImageView = null;

            int marginTopPixels = 0;
            int marginBottomPixels = 0;
            int marginLeftPixels = 0;
            int marginRightPixels = 0;

            // LMS Sublayout

            lmsItemLayoutParms =
                new LinearLayout.LayoutParams(subLayoutWidth, // width
                                              subLayoutHeight); // height

            lmsSubLayout = new LinearLayout(activity);
            lmsSubLayout.setLayoutParams(lmsItemLayoutParms);

            lmsSubLayout.setId(lmsSubLayout.hashCode());
            /* @@@ STUBBED
            lmsSubLayout.setBackgroundResource(R.drawable.rectangle_frame);
            @@@ STUBBED */
            lmsSubLayout.setOrientation(LinearLayout.VERTICAL);
            lmsSubLayout.setMeasureWithLargestChildEnabled(true);
            lmsSubLayout.setTag(lmsItem);
            lmsSubLayout.setOnClickListener(itemOnClickListener);

            lmsSubLayoutContainer.addView(lmsSubLayout);

            // Title TextView

            titleTextView = new TextView(activity);
            titleTextView.setId(titleTextView.hashCode());
            titleTextView.setGravity(Gravity.LEFT);
            titleTextView.setLines(2);
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            titleTextView.setTextColor(resources.getColor(R.color.sel_text_list_item));
            titleTextView.setTextAppearance(activity, R.style.TextExtraLargeListItem);
            titleTextView.setText(lmsItem.title);

            widgetLayoutParms =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, // width
                                              LinearLayout.LayoutParams.WRAP_CONTENT); // height
            marginLeftPixels = 0;
            marginTopPixels = (int) (4 * displayMetrics.density); // margin in pixels
            marginRightPixels = 0;
            marginBottomPixels = (int) (8 * displayMetrics.density); // margin in pixels

            widgetLayoutParms.setMargins(marginLeftPixels,
                                         marginTopPixels,
                                         marginRightPixels,
                                         marginBottomPixels);

            if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsC(): titleTextView:"
                    + " marginLeftPixels[" + marginLeftPixels + "]"
                    + " marginTopPixels[" + marginTopPixels + "]"
                    + " marginRightPixels[" + marginRightPixels + "]"
                    + " marginBottomPixels[" + marginBottomPixels + "]"
                    + " this[" + this + "]"
            );

            titleTextView.setLayoutParams(widgetLayoutParms);

            /* @@@ STUBBED
            lmsSubLayout.addView(titleTextView);
            @@@ STUBBED */

            // Category ImageView

            categoryImageView = new ImageView(activity);
            categoryImageView.setId(categoryImageView.hashCode());
            categoryImageView.setAdjustViewBounds(true);
            /* @@@ STUBBED
            categoryImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            @@@ STUBBED */
            categoryImageView.setScaleType(ImageView.ScaleType.FIT_XY);

            widgetLayoutParms =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // width
                                              LinearLayout.LayoutParams.WRAP_CONTENT); // height

            marginLeftPixels = 0;
            marginTopPixels = 0;
            marginRightPixels = 0;
            marginBottomPixels = (int) (4 * displayMetrics.density); // margin in pixels

            /* @@@ STUBBED
            widgetLayoutParms.setMargins(marginLeftPixels,
                                         marginTopPixels,
                                         marginRightPixels,
                                         marginBottomPixels);
            @@@ STUBBED */

            if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsC(): categoryImageView:"
                    + " subLayoutWidth[" + subLayoutWidth + "]"
                    + " subLayoutHeight[" + subLayoutHeight + "]"
                    + " marginLeftPixels[" + marginLeftPixels + "]"
                    + " marginTopPixels[" + marginTopPixels + "]"
                    + " marginRightPixels[" + marginRightPixels + "]"
                    + " marginBottomPixels[" + marginBottomPixels + "]"
                    + " this[" + this + "]"
            );

            categoryImageView.setLayoutParams(widgetLayoutParms);

            lmsSubLayout.addView(categoryImageView);

            /* @@@ STUBBED
            RequestCreator requestCreator = picasso.load(lmsItem.bannerUrl);
            requestCreator.error(noPhoto);
            requestCreator.into(categoryImageView);
            requestCreator.fit();
            @@@ STUBBED */
            /* @@@ STUBBED
            RequestCreator requestCreator = picasso.load(bannerResourceId);
            requestCreator.error(noPhoto);
            requestCreator.into(categoryImageView);
            requestCreator.fit();
            @@@ STUBBED */
            categoryImageView.setImageResource(bannerResourceId);
        }

    } // doLmsItemsC()
}
