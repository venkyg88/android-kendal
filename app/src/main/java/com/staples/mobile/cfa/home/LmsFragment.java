package com.staples.mobile.cfa.home;

import android.app.Activity;
import android.app.Fragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.text.TextUtils;

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
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.lms.LmsManager;
import com.staples.mobile.common.access.lms.LmsManager.LmsMgrCallback;
import com.staples.mobile.common.access.configurator.model.Area;
import com.staples.mobile.common.access.configurator.model.Item;
import com.staples.mobile.common.access.configurator.model.Screen;
import com.staples.mobile.common.device.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class LmsFragment
        extends Fragment
        implements LmsMgrCallback {

    private static final String TAG = "LmsFragment";

    private static final boolean LOGGING = false;

    private static final long LMS_REFRESH_TIME_MILLIS = (5 * 60 * 1000);

    private MainActivity activity;
    private Resources resources;
    private LayoutInflater layoutInflater;

    private DeviceInfo deviceInfo;

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


        // @@@ TODO Need to make refresh interval settable.
        Access access = Access.getInstance();

        boolean conditionalLmsRefresh = true;
        long currentTimeMs = System.currentTimeMillis();
        long lastTimeLmsRefreshed = access.getLastTimeLmsRefreshed();
        long timeToRefreshMs = lastTimeLmsRefreshed + LMS_REFRESH_TIME_MILLIS;

        if (currentTimeMs > timeToRefreshMs) {
            conditionalLmsRefresh = false; // force Lms refresh
            access.setLastTimeLmsRefreshed(currentTimeMs);
        }

        lmsManager.getLms(this,  // LmsMgrCallback
                conditionalLmsRefresh); // conditional

        return (lmsFrameView);
    }

    public void onGetLmsResult(boolean success) {

        if (LOGGING) Log.v(TAG, "LmsFragment:LmsManager.onGetLmsResult():"
                        + " success[" + success + "]"
                        + " this[" + this + "]"
        );

        deviceInfo = new DeviceInfo(resources);

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
                doLmsItemsABD(lmsItemsA, R.drawable.banner_a_color_small, deviceInfo.getSmallestAbsWidthPixels());
            }
            if (lmsItemsB.size() > 0) {
                doLmsItemsABD(lmsItemsB, R.drawable.banner_b_color_small, (deviceInfo.getSmallestAbsWidthPixels() / 2));
            }
            if (lmsItemsC.size() > 0) {
                doLmsItemsC(R.drawable.banner_c_color_small);
            }
            if (lmsItemsD.size() > 0) {
                doLmsItemsABD(lmsItemsD, R.drawable.banner_d_color_small, (deviceInfo.getSmallestAbsWidthPixels() / 4));
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

        parentLayoutWidthPx = deviceInfo.getWidthPixels(); // pixels
        parentLayoutHeightPx = deviceInfo.getHeightPixels(); // pixels

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

            // LMS Sublayout

            lmsItemLayoutParms =
                    new LinearLayout.LayoutParams(deviceInfo.getSmallestAbsWidthPixels(), // width
                            subLayoutHeight); // height

            lmsSubLayout = new LinearLayout(activity);
            lmsSubLayout.setLayoutParams(lmsItemLayoutParms);

            lmsSubLayout.setId(lmsSubLayout.hashCode());
            lmsSubLayout.setOrientation(LinearLayout.VERTICAL);
            lmsSubLayout.setMeasureWithLargestChildEnabled(true);
            lmsSubLayout.setTag(lmsItem);
            lmsSubLayout.setOnClickListener(itemOnClickListener);

            lmsScrollLayout.addView(lmsSubLayout);

            // Category ImageView

            categoryImageView = new ImageView(activity);
            categoryImageView.setId(categoryImageView.hashCode());
            categoryImageView.setAdjustViewBounds(true);
            categoryImageView.setScaleType(ImageView.ScaleType.FIT_XY);

            widgetLayoutParms =
                    new LinearLayout.LayoutParams(deviceInfo.getSmallestAbsWidthPixels(), // width
                            LinearLayout.LayoutParams.WRAP_CONTENT); // height

            categoryImageView.setLayoutParams(widgetLayoutParms);

            lmsSubLayout.addView(categoryImageView);

            Bitmap bannerBitMap = BitmapFactory.decodeResource(resources, bannerResourceId);
            categoryImageView.setImageBitmap(bannerBitMap);
        }

    } // doLmsItemsABD()

    private void doLmsItemsC(int bannerResourceId) {

        if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsC():"
                        + " this[" + this + "]"
        );

        parentLayoutWidthPx = deviceInfo.getWidthPixels(); // pixels
        parentLayoutHeightPx = deviceInfo.getHeightPixels(); // pixels

        int subLayoutWidth = deviceInfo.getSmallestAbsWidthPixels() / 2;
        int subLayoutHeight = deviceInfo.getSmallestAbsWidthPixels() / 2;

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

            LinearLayout.LayoutParams lmsItemLayoutParms = null;

            LinearLayout.LayoutParams widgetLayoutParms = null;

            LinearLayout lmsSubLayout = null;
            TextView titleTextView = null;
            ImageView categoryImageView = null;

            // LMS Sublayout

            lmsItemLayoutParms =
                    new LinearLayout.LayoutParams(subLayoutWidth, // width
                            subLayoutHeight); // height

            lmsSubLayout = new LinearLayout(activity);
            lmsSubLayout.setLayoutParams(lmsItemLayoutParms);

            lmsSubLayout.setId(lmsSubLayout.hashCode());
            lmsSubLayout.setOrientation(LinearLayout.VERTICAL);
            lmsSubLayout.setMeasureWithLargestChildEnabled(true);
            lmsSubLayout.setTag(lmsItem);
            lmsSubLayout.setOnClickListener(itemOnClickListener);

            lmsSubLayoutContainer.addView(lmsSubLayout);

            // Category ImageView

            categoryImageView = new ImageView(activity);
            categoryImageView.setId(categoryImageView.hashCode());
            categoryImageView.setAdjustViewBounds(true);
            categoryImageView.setScaleType(ImageView.ScaleType.FIT_XY);

            widgetLayoutParms =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // width
                            LinearLayout.LayoutParams.WRAP_CONTENT); // height

            categoryImageView.setLayoutParams(widgetLayoutParms);

            lmsSubLayout.addView(categoryImageView);

            Bitmap bannerBitMap = BitmapFactory.decodeResource(resources, bannerResourceId);
            categoryImageView.setImageBitmap(bannerBitMap);
        }

    } // doLmsItemsC()
}
