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

    private LmsPersistentState lmsPersistentState;

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

    private int aItemWidth;
    private int aItemHeight;

    private int bItemWidth;
    private int bItemHeight;

    private int cItemWidth;
    private int cItemHeight;

    private int dItemWidth;
    private int dItemHeight;

    private View.OnClickListener itemOnClickListener;

    @Override
    public void onAttach(Activity activity) {

        if (LOGGING) Log.v(TAG, "LmsFragment:onAttach():"
                        + " activity[" + activity + "]"
                        + " this[" + this + "]"
        );

        super.onAttach(activity);

        lmsPersistentState = LmsPersistentState.getInstance();

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

        boolean conditionalLmsRefresh = true;
        long currentTimeMs = System.currentTimeMillis();
        long lastTimeLmsRefreshed = lmsPersistentState.getLastTimeLmsRefreshed();
        long timeToRefreshMs = lastTimeLmsRefreshed + LMS_REFRESH_TIME_MILLIS;

        if (currentTimeMs > timeToRefreshMs) {
            conditionalLmsRefresh = false; // force Lms refresh
            lmsPersistentState.setLastTimeLmsRefreshed(currentTimeMs);
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

                String size = lmsItem.size;

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

            boolean isPortrait = deviceInfo.isCurrentOrientationPortrait(resources);

            if (isPortrait) {
                doPortrait();
            } else {
                doLandscape();
            }
        }
        activity.showMainScreen();
    }

    private void doPortrait() {

        if (LOGGING) Log.v(TAG, "LmsFragment:doPortrait():"
                        + " this[" + this + "]"
        );

        // A items are square.
        aItemWidth = aItemHeight = deviceInfo.getSmallestAbsWidthPixels();

        bItemWidth = aItemWidth;        // same width as an A item.
        bItemHeight = aItemHeight / 2;  // half the height of an A item.

        cItemWidth = bItemWidth / 2;    // half the width of a A item.
        cItemHeight = bItemHeight;      // same height as a B item.

        dItemWidth = aItemWidth;        // same width as an A item.
        dItemHeight = aItemHeight / 4;  // quarter the height of an A item.

        if (lmsItemsA.size() > 0) {
            doLmsItemsABDPort(lmsItemsA);
        }
        if (lmsItemsB.size() > 0) {
            doLmsItemsABDPort(lmsItemsB);
        }
        if (lmsItemsC.size() > 0) {
            doLmsItemsCPort();
        }
        if (lmsItemsD.size() > 0) {
            doLmsItemsABDPort(lmsItemsD);
        }

    } // doPortrait()

    private void doLmsItemsABDPort(List<LmsItem> lmsItems) {

        if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsABDPort():"
                        + " lmsItems[" + lmsItems + "]"
                        + " this[" + this + "]"
        );

        for (LmsItem lmsItem : lmsItems) {

            String size = lmsItem.size;
            int subLayoutHeight = 0;

            if (size.equalsIgnoreCase("A")) {

                subLayoutHeight = aItemHeight;

            } else if (size.equalsIgnoreCase("B")) {

                subLayoutHeight = bItemHeight;

            } else if (size.equalsIgnoreCase("D")) {

                subLayoutHeight = dItemHeight;
            }

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(aItemWidth, subLayoutHeight, categoryImageView);
            widgetLayout.setTag(lmsItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            lmsScrollLayout.addView(widgetLayout);
        }

    } // doLmsItemsABDPort()

    private void doLmsItemsCPort() {

        if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsCPort():"
                        + " this[" + this + "]"
        );

        LinearLayout subLayoutContainer = null;

        boolean firstSubInContainer = true;
        int lmsItemNbr = -1;

        for (LmsItem lmsItem : lmsItemsC) {

            lmsItemNbr++;

            firstSubInContainer = (lmsItemNbr % 2 == 0);

            if (firstSubInContainer) {

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);
                lmsScrollLayout.addView(subLayoutContainer);
            }

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(cItemWidth, cItemHeight, categoryImageView);
            widgetLayout.setTag(lmsItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);
        }

    } // doLmsItemsCPort()

    private void doLandscape() {

        if (LOGGING) Log.v(TAG, "LmsFragment:doLandscape():"
                        + " this[" + this + "]"
        );

        // A items are square.
        aItemWidth = (deviceInfo.getLargestAbsWidthPixels() / 2);
        aItemHeight = aItemWidth;

        bItemWidth = aItemWidth;        // same width as an A item.
        bItemHeight = aItemHeight / 2;  // half the height of an A item.

        cItemWidth = aItemWidth / 2;    // half the width of a A item.
        cItemHeight = bItemHeight;      // same height as a B item.

        dItemWidth = aItemWidth;        // same width as an A item.
        dItemHeight = aItemHeight / 4;  // quarter the height of an A item.

        if (LOGGING) Log.v(TAG, "LmsFragment:doLandscape():"
                        + " aItemWidth[" + aItemWidth + "]"
                        + " aItemHeight[" + aItemHeight + "]"
                        + " bItemWidth[" + bItemWidth + "]"
                        + " bItemHeight[" + bItemHeight + "]"
                        + " cItemWidth[" + cItemWidth + "]"
                        + " cItemHeight[" + cItemHeight + "]"
                        + " dItemWidth[" + dItemWidth + "]"
                        + " dItemHeight[" + dItemHeight + "]"
                        + " this[" + this + "]"
        );

        if (lmsItemsA.size() > 0) {
            doLmsItemsALand();
        } else {
            doLmsItemsLand();
        }

    } // doLandscape()

    private void doLmsItemsALand() {

        if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsALand():"
                        + " this[" + this + "]"
        );

        // Handle the A item.

        LmsItem lmsItemA = lmsItemsA.get(0);

        // LMS Sublayout

        // Horizontal. Contains A item and lmsBCDLayout.
        LinearLayout subLayout = getSubLayout(aItemWidth * 2,
                aItemHeight,
                LinearLayout.HORIZONTAL);

        lmsScrollLayout.addView(subLayout);

        ImageView categoryImageView = getImageView();
        setImage(categoryImageView, lmsItemA.bannerUrl);

        // Vertical. Contains selectable content. Used to create a rectangular
        // frame around the content.
        LinearLayout widgetLayout = getWidgetLayout(aItemWidth, aItemHeight, categoryImageView);
        widgetLayout.setTag(lmsItemA);
        widgetLayout.setOnClickListener(itemOnClickListener);
        widgetLayout.addView(categoryImageView);

        subLayout.addView(widgetLayout);


        // Vertical. Contains one or more B, C, and/or D items.
        LinearLayout lmsBCDLayout = getBCDLayout();

        subLayout.addView(lmsBCDLayout);

        boolean aFilled = false;

        while (true) {

            if (lmsItemsB.size() >= 2) {

                aFilled = fillAWithB(lmsBCDLayout, 2);
                break; // while (true)
            }
            if (lmsItemsB.size() > 0) {

                fillAWithB(lmsBCDLayout, 1);

                if (lmsItemsC.size() > 0) {

                    fillAWithC(lmsBCDLayout, 1);

                } else if (lmsItemsD.size() >= 2) {

                    fillAWithD(lmsBCDLayout, 1);
                }
                break; // while (true)

            } else if (lmsItemsC.size() >= 4) {

                fillAWithC(lmsBCDLayout, 2);

            } else if (lmsItemsC.size() > 0) {

                fillAWithC(lmsBCDLayout, 1);
                fillAWithD(lmsBCDLayout, 2);

            } else if (lmsItemsD.size() > 0) {

                fillAWithD(lmsBCDLayout, 4);
            }
            break; // while (true)

        } // while (true)

        doLmsItemsLand();

    } // doLmsItemsALand()

    private boolean fillAWithB(LinearLayout lmsBCDLayout, int maxItems) {

        if (LOGGING) Log.v(TAG, "LmsFragment:fillAWithB():"
                        + " this[" + this + "]"
        );

        int nbrListItems = Math.min(lmsItemsB.size(), maxItems);

        int lmsItemNdx = 0;
        LmsItem lmsItem = null;

        for (lmsItemNdx = 0; lmsItemNdx < nbrListItems; lmsItemNdx++) {

            lmsItem = lmsItemsB.get(0);

            lmsItemsB.remove(0);

            if (LOGGING) Log.v(TAG, "LmsFragment:fillAWithB(): lmsItem:"
                            + " lmsItem.title[" + lmsItem.title + "]"
                            + " lmsItem.bannerUrl[" + lmsItem.bannerUrl + "]"
                            + " this[" + this + "]"
            );

            // Category ImageView

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(bItemWidth, bItemHeight, categoryImageView);
            widgetLayout.setTag(lmsItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            lmsBCDLayout.addView(widgetLayout);
        }

        boolean aFilled = (lmsItemNdx == maxItems) ? true : false;

        return (aFilled);

    } // fillAWithB()

    private boolean fillAWithC(LinearLayout lmsBCDLayout, int maxItems) {

        if (LOGGING) Log.v(TAG, "LmsFragment:fillAWithC():"
                        + " this[" + this + "]"
        );

        LinearLayout subLayoutContainer = null;

        // maxItems is the maximum number of SubLayout containers allowed. Each
        // SubLayout container can contain 2 list items.
        int nbrListItems = Math.min(lmsItemsC.size(), (maxItems * 2));

        boolean firstSubInContainer = true;

        int nbrSubLayoutContainers = 0;

        int lmsItemNdx = 0;
        LmsItem lmsItem = null;

        for (lmsItemNdx = 0; lmsItemNdx < nbrListItems; lmsItemNdx++) {

            lmsItem = lmsItemsC.get(0);

            lmsItemsC.remove(0);

            firstSubInContainer = (lmsItemNdx % 2 == 0);

            if (firstSubInContainer) {

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);
                lmsBCDLayout.addView(subLayoutContainer);

                nbrSubLayoutContainers++;
            }

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(cItemWidth, cItemHeight, categoryImageView);
            widgetLayout.setTag(lmsItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);
        }

        boolean aFilled = (nbrSubLayoutContainers == maxItems) ? true : false;

        return (aFilled);

    } // fillAWithC()

    private boolean fillAWithD(LinearLayout lmsBCDLayout, int maxItems) {

        if (LOGGING) Log.v(TAG, "LmsFragment:fillAWithD():"
                        + " this[" + this + "]"
        );

        int nbrListItems = Math.min(lmsItemsD.size(), maxItems);

        int lmsItemNdx = 0;
        LmsItem lmsItem = null;

        for (lmsItemNdx = 0; lmsItemNdx < nbrListItems; lmsItemNdx++) {

            lmsItem = lmsItemsD.get(0);

            lmsItemsD.remove(0);

            if (LOGGING) Log.v(TAG, "LmsFragment:fillAWithD(): lmsItem:"
                            + " lmsItem.title[" + lmsItem.title + "]"
                            + " lmsItem.bannerUrl[" + lmsItem.bannerUrl + "]"
                            + " this[" + this + "]"
            );

            // Category ImageView

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(dItemWidth, dItemHeight, categoryImageView);
            widgetLayout.setTag(lmsItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            lmsBCDLayout.addView(widgetLayout);
        }

        boolean aFilled = (lmsItemNdx == maxItems) ? true : false;

        return (aFilled);

    } // fillAWithD()

    private LinearLayout getWidgetLayout(int layoutWidth, int layoutHeight, View childView) {

        if (LOGGING) Log.v(TAG, "LmsFragment:getWidgetLayout():"
                        + " layoutWidth[" + layoutWidth + "]"
                        + " layoutHeight[" + layoutHeight + "]"
                        + " childView[" + childView + "]"
                        + " this[" + this + "]"
        );

        // Vertical. Contains selectable content. Used to create a rectangular
        // frame around the content.
        LinearLayout widgetLayout = null;

        widgetLayout = new LinearLayout(activity);
        widgetLayout.setBackgroundResource(R.drawable.rectangle_frame);

        widgetLayout.setId(widgetLayout.hashCode());
        widgetLayout.setOrientation(LinearLayout.VERTICAL);
        widgetLayout.setMeasureWithLargestChildEnabled(true);

        int padding = 0;
        widgetLayout.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams widgetLayoutParms =
                new LinearLayout.LayoutParams(layoutWidth, // width
                        layoutHeight); // height

        int margin = 0;
        widgetLayoutParms.setMargins(margin, margin, margin, margin); // left, top, right, bottom

        childView.setLayoutParams(widgetLayoutParms);

        return (widgetLayout);

    } // getWidgetLayout()

    private LinearLayout getBCDLayout() {

        if (LOGGING) Log.v(TAG, "LmsFragment:getBCDLayout():"
                        + " this[" + this + "]"
        );

        LinearLayout.LayoutParams lmsBCDLayoutParms =

                new LinearLayout.LayoutParams(aItemWidth, // width
                        aItemHeight); // height

        LinearLayout lmsBCDLayout = new LinearLayout(activity);
        lmsBCDLayout.setLayoutParams(lmsBCDLayoutParms);

        lmsBCDLayout.setId(lmsBCDLayout.hashCode());
        lmsBCDLayout.setOrientation(LinearLayout.VERTICAL);
        lmsBCDLayout.setMeasureWithLargestChildEnabled(true);

        int padding = 0;
        lmsBCDLayout.setPadding(padding, padding, padding, padding);

        return (lmsBCDLayout);

    } // getBCDLayout()

    private LinearLayout getSubLayout(int layoutWidth, int layoutHeight, int orientation) {

        if (LOGGING) Log.v(TAG, "LmsFragment:getSubLayout():"
                        + " layoutWidth[" + layoutWidth + "]"
                        + " layoutHeight[" + layoutHeight + "]"
                        + " this[" + this + "]"
        );

        LinearLayout.LayoutParams lmsSubLayoutParms =
                new LinearLayout.LayoutParams(layoutWidth, // width
                        layoutHeight); // height

        LinearLayout subLayout = new LinearLayout(activity);

        subLayout.setLayoutParams(lmsSubLayoutParms);
        subLayout.setBackgroundResource(R.drawable.rectangle_frame);

        subLayout.setId(subLayout.hashCode());
        subLayout.setOrientation(orientation);
        subLayout.setMeasureWithLargestChildEnabled(true);

        int padding = 0;
        subLayout.setPadding(padding, padding, padding, padding);

        return (subLayout);

    } // getSubLayout()

    private LinearLayout getSubLayoutContainer(int orientation) {

        if (LOGGING) Log.v(TAG, "LmsFragment:getSubLayoutContainer():"
                        + " orientation[" + orientation + "]"
                        + " this[" + this + "]"
        );

        LinearLayout subLayoutContainer = new LinearLayout(activity);
        subLayoutContainer.setId(subLayoutContainer.hashCode());
        subLayoutContainer.setOrientation(orientation);
        subLayoutContainer.setMeasureWithLargestChildEnabled(true);

        int padding = 0;
        subLayoutContainer.setPadding(padding, padding, padding, padding);

        return (subLayoutContainer);

    } // getSubLayoutContainer()

    private ImageView getImageView() {

        if (LOGGING) Log.v(TAG, "LmsFragment:getImageView():"
                        + " this[" + this + "]"
        );

        ImageView imageView = new ImageView(activity);
        imageView.setId(imageView.hashCode());
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int padding = 24;
        imageView.setPadding(padding, padding, padding, padding);

        return (imageView);

    } // getImageView()

    private void setImage(ImageView imageView, String imageUrl) {

        if (LOGGING) Log.v(TAG, "LmsFragment:setImage():"
                        + " imageUrl[" + imageUrl + "]"
                        + " imageView[" + imageView + "]"
                        + " this[" + this + "]"
        );

        RequestCreator requestCreator = picasso.load(imageUrl);
        requestCreator.error(noPhoto);
        requestCreator.into(imageView);
        requestCreator.fit();

    } // setImage()

    private void doLmsItemsLand() {

        if (LOGGING) Log.v(TAG, "LmsFragment:doLmsItemsLand():"
                        + " this[" + this + "]"
        );

        if (lmsItemsB.size() > 0) {
            fillWithBLand();
        }
        if (lmsItemsC.size() > 0) {
            fillWithCLand();
        }
        if (lmsItemsD.size() > 0) {
            fillWithDLand();
        }

    } // doLmsItemsLand()

    private void fillWithBLand() {

        if (LOGGING) Log.v(TAG, "LmsFragment:fillWithBLand():"
                        + " this[" + this + "]"
        );

        final int NBR_ITEMS_IN_CONTAINER = 2;

        int nbrListItems = lmsItemsB.size();

        boolean firstSubInContainer = true;

        int lmsItemNdx = 0;
        LmsItem lmsItem = null;

        LinearLayout subLayoutContainer = null;

        for (lmsItemNdx = 0; lmsItemNdx < nbrListItems; lmsItemNdx++) {

            lmsItem = lmsItemsB.get(0);

            lmsItemsB.remove(0);

            firstSubInContainer = (lmsItemNdx % NBR_ITEMS_IN_CONTAINER == 0);

            if (firstSubInContainer) {

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);
                lmsScrollLayout.addView(subLayoutContainer);
            }

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(bItemWidth, bItemHeight, categoryImageView);
            widgetLayout.setTag(lmsItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);
        }

    } // fillWithBLand()

    private void fillWithCLand() {

        if (LOGGING) Log.v(TAG, "LmsFragment:fillWithCLand():"
                        + " this[" + this + "]"
        );

        final int NBR_ITEMS_IN_CONTAINER = 4;

        int nbrListItems = lmsItemsC.size();

        boolean firstSubInContainer = true;

        int nbrCItemsInContainer = 0;

        int lmsItemNdx = 0;
        LmsItem lmsItem = null;

        LinearLayout subLayoutContainer = null;

        for (lmsItemNdx = 0; lmsItemNdx < nbrListItems; lmsItemNdx++) {

            lmsItem = lmsItemsC.get(0);

            lmsItemsC.remove(0);

            firstSubInContainer = (lmsItemNdx % NBR_ITEMS_IN_CONTAINER == 0);

            if (firstSubInContainer) {

                nbrCItemsInContainer = 0;

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);

                lmsScrollLayout.addView(subLayoutContainer);
            }

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(cItemWidth, cItemHeight, categoryImageView);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.setTag(lmsItem);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);

            nbrCItemsInContainer++;
        }

        if (nbrCItemsInContainer < 4) {

            padWithDLand(subLayoutContainer, 2);
        }

    } // fillWithCLand()

    private void padWithDLand(LinearLayout subLayoutContainer, int nbrListItems) {

        if (LOGGING) Log.v(TAG, "LmsFragment:padWithDLand():"
                        + " nbrListItems[" + nbrListItems + "]"
                        + " this[" + this + "]"
        );

        int lmsItemNdx = 0;
        LmsItem lmsItem = null;

        LinearLayout dItemContainer = getSubLayoutContainer(LinearLayout.VERTICAL);

        subLayoutContainer.addView(dItemContainer);

        for (lmsItemNdx = 0; lmsItemNdx < nbrListItems; lmsItemNdx++) {

            lmsItem = lmsItemsD.get(0);

            lmsItemsD.remove(0);

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(dItemWidth, dItemHeight, categoryImageView);
            widgetLayout.setTag(lmsItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            dItemContainer.addView(widgetLayout);
        }

    } // padWithDLand()

    private void fillWithDLand() {

        if (LOGGING) Log.v(TAG, "LmsFragment:fillWithDLand():"
                        + " this[" + this + "]"
        );

        final int NBR_ITEMS_IN_CONTAINER = 2;

        int nbrListItems = lmsItemsD.size();

        boolean firstSubInContainer = true;

        int lmsItemNdx = 0;
        LmsItem lmsItem = null;

        LinearLayout subLayoutContainer = null;

        for (lmsItemNdx = 0; lmsItemNdx < nbrListItems; lmsItemNdx++) {

            lmsItem = lmsItemsD.get(0);

            lmsItemsD.remove(0);

            firstSubInContainer = (lmsItemNdx % NBR_ITEMS_IN_CONTAINER == 0);

            if (firstSubInContainer) {

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);
                lmsScrollLayout.addView(subLayoutContainer);
            }

            ImageView categoryImageView = getImageView();
            setImage(categoryImageView, lmsItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(dItemWidth, dItemHeight, categoryImageView);
            widgetLayout.setTag(lmsItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);
        }

    } // fillWithDLand()
}
