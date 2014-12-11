package com.staples.mobile.cfa.home;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.configurator.model.Area;
import com.staples.mobile.common.access.configurator.model.Item;
import com.staples.mobile.common.access.configurator.model.Screen;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.device.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class ConfiguratorFragment
        extends Fragment
        implements AppConfigurator.AppConfiguratorCallback {
    private static final String TAG = "ConfiguratorFragment";

    private static final boolean LOGGING = true;

    private static final long CONFIGURATOR_REFRESH_TIME_MILLIS = (5 * 60 * 1000);

    private static final int PADDING_ZERO = 0;

    private static final int MARGIN_ZERO = 0;
    private static final int MARGIN_LEFT_DP = 0;
    private static final int MARGIN_TOP_DP = 0;
    private static final int MARGIN_RIGHT_DP = 0;
    private static final int MARGIN_BOTTOM_DP = 20;

    private MainActivity activity;
    private Resources resources;

    private DeviceInfo deviceInfo;

    private View configFrameView;
    private LinearLayout configScrollLayout;
    private LinearLayout.LayoutParams subLayoutContainerLayoutParms;
    private LinearLayout.LayoutParams widgetLayoutParms;

    private AppConfigurator appConfigurator;

    private List<ConfigItem> configItems;
    private List<ConfigItem> configItemsA;
    private List<ConfigItem> configItemsB;
    private List<ConfigItem> configItemsC;
    private List<ConfigItem> configItemsD;

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

    private boolean retryGetConfig = true;

    @Override
    public void onAttach(Activity activity) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:onAttach():"
                        + " activity[" + activity + "]"
                        + " this[" + this + "]"
        );

        super.onAttach(activity);

        this.activity = (MainActivity) activity;
        resources = activity.getResources();
        appConfigurator = new AppConfigurator(MainApplication.application);

        configItems = new ArrayList<ConfigItem>();
        configItemsA = new ArrayList<ConfigItem>();
        configItemsB = new ArrayList<ConfigItem>();
        configItemsC = new ArrayList<ConfigItem>();
        configItemsD = new ArrayList<ConfigItem>();

        return;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:onCreateView():"
                        + " this[" + this + "]"
        );

        picasso = Picasso.with(activity);

        noPhoto = resources.getDrawable(R.drawable.no_photo);

        configFrameView = layoutInflater.inflate(R.layout.config_frame, container, false);

        configScrollLayout = (LinearLayout) configFrameView.findViewById(R.id.configScrollLayout);

        subLayoutContainerLayoutParms =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, // width
                        ViewGroup.LayoutParams.MATCH_PARENT); // height

        subLayoutContainerLayoutParms.setMargins(MARGIN_LEFT_DP, MARGIN_TOP_DP, MARGIN_RIGHT_DP, MARGIN_BOTTOM_DP); // left, top, right, bottom

        itemOnClickListener = new OnClickListener() {

            @Override
            public void onClick(View view) {

                if (LOGGING) Log.v(TAG, "ConfiguratorFragment:OnClickListener.onClick():"
                                + " view[" + view + "]"
                                + " this[" + this + "]"
                );

                ConfigItem configItem = (ConfigItem) view.getTag();
                activity.selectBundle(configItem.title, configItem.identifier);
            }
        };

        appConfigurator.getConfigurator(this); // AppConfiguratorCallback

        return (configFrameView);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity!=null) activity.showActionBar(R.string.staples, R.drawable.ic_search_white, null);
    }

    public void onGetConfiguratorResult(boolean success) {
        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:AppConfigurator.onGetConfiguratorResult():"
                        + " success[" + success + "]"
                        + " this[" + this + "]"
        );

        if (success) {

            deviceInfo = new DeviceInfo(resources);

            screens = appConfigurator.getScreen();
            Screen screen = screens.get(0);
            items = screen.getItem();

            ConfigItem configItem = null;
            List<Area> areas = null;
            String skuList = null;

            configItems.clear();
            configItemsA.clear();
            configItemsB.clear();
            configItemsC.clear();
            configItemsD.clear();

            for (Item item : items) {

                areas = item.getArea();
                skuList = areas.get(0).getSkuList();

                configItem = new ConfigItem(item.getTitle(), item.getBanner(), skuList, item.getSize());
                configItems.add(configItem);

                String size = configItem.size;

                if (size.equalsIgnoreCase("A")) {

                    configItemsA.add(configItem);

                } else if (size.equalsIgnoreCase("B")) {

                    configItemsB.add(configItem);

                } else if (size.equalsIgnoreCase("C")) {

                    configItemsC.add(configItem);

                } else if (size.equalsIgnoreCase("D")) {

                    configItemsD.add(configItem);
                }
            }

            boolean isPortrait = deviceInfo.isCurrentOrientationPortrait(resources);

            if (isPortrait) {
                doPortrait();
            } else {
                doLandscape();
            }
        } else {

            if (retryGetConfig) appConfigurator.getConfigurator(this); // AppConfiguratorCallback
            retryGetConfig = false;
        }

        activity.showMainScreen();
    }

    private void doPortrait() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:doPortrait():"
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

        if (configItemsA.size() > 0) {
            doConfigItemsABDPort(configItemsA);
        }
        if (configItemsB.size() > 0) {
            doConfigItemsABDPort(configItemsB);
        }
        if (configItemsC.size() > 0) {
            doConfigItemsCPort();
        }
        if (configItemsD.size() > 0) {
            doConfigItemsABDPort(configItemsD);
        }

    } // doPortrait()

    private void doConfigItemsABDPort(List<ConfigItem> configItems) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:doConfigItemsABDPort():"
                        + " configItems[" + configItems + "]"
                        + " this[" + this + "]"
        );

        for (ConfigItem configItem : configItems) {

            String size = configItem.size;
            int subLayoutHeight = 0;
            ImageView categoryImageView = null;

            if (size.equalsIgnoreCase("A")) {

                subLayoutHeight = aItemHeight;
                categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);

            } else if (size.equalsIgnoreCase("B")) {

                subLayoutHeight = bItemHeight;
                categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);

            } else if (size.equalsIgnoreCase("D")) {

                subLayoutHeight = dItemHeight;
                categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            }

            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(aItemWidth, subLayoutHeight, MARGIN_BOTTOM_DP, categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            configScrollLayout.addView(widgetLayout);
        }

    } // doConfigItemsABDPort()

    private void doConfigItemsCPort() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:doConfigItemsCPort():"
                        + " this[" + this + "]"
        );

        LinearLayout subLayoutContainer = null;

        boolean firstSubInContainer = true;
        int configItemNbr = -1;

        for (ConfigItem configItem : configItemsC) {

            configItemNbr++;

            firstSubInContainer = (configItemNbr % 2 == 0);

            if (firstSubInContainer) {

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);
                subLayoutContainerLayoutParms.setMargins(MARGIN_ZERO, MARGIN_ZERO, MARGIN_ZERO, MARGIN_BOTTOM_DP); // left, top, right, bottom
                configScrollLayout.addView(subLayoutContainer, subLayoutContainerLayoutParms);
            }

            ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(cItemWidth, cItemHeight, MARGIN_ZERO, categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);
        }

    } // doConfigItemsCPort()

    private void doLandscape() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:doLandscape():"
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

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:doLandscape():"
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

        if (configItemsA.size() > 0) {
            doConfigItemsALand();
        } else {
            doConfigItemsLand();
        }

    } // doLandscape()

    private void doConfigItemsALand() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:doConfigItemsALand():"
                        + " this[" + this + "]"
        );

        // Handle the A item.

        ConfigItem configItemA = configItemsA.get(0);

        // Configurator Sublayout

        // Horizontal. Contains A item and configBCDLayout.
        LinearLayout subLayout = getSubLayout(aItemWidth * 2,
                aItemHeight,
                LinearLayout.HORIZONTAL);

        subLayoutContainerLayoutParms.setMargins(MARGIN_LEFT_DP, MARGIN_TOP_DP, MARGIN_RIGHT_DP, MARGIN_BOTTOM_DP); // left, top, right, bottom
        configScrollLayout.addView(subLayout, subLayoutContainerLayoutParms);

        ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
        setImage(categoryImageView, configItemA.bannerUrl);

        // Vertical. Contains selectable content. Used to create a rectangular
        // frame around the content.
        LinearLayout widgetLayout = getWidgetLayout(aItemWidth, aItemHeight, MARGIN_ZERO, categoryImageView);
        widgetLayout.setTag(configItemA);
        widgetLayout.setOnClickListener(itemOnClickListener);
        widgetLayout.addView(categoryImageView);

        subLayout.addView(widgetLayout);

        // Vertical. Contains one or more B, C, and/or D items.
        LinearLayout configBCDLayout = getBCDLayout();

        subLayout.addView(configBCDLayout);

        while (true) {

            if (configItemsB.size() >= 2) {

                fillAWithB(configBCDLayout, 2);

                break; // while (true)
            }
            if (configItemsB.size() > 0) {

                fillAWithB(configBCDLayout, 1);

                if (configItemsC.size() > 0) {

                    fillAWithC(configBCDLayout, 1);

                } else if (configItemsD.size() >= 2) {

                    fillAWithD(configBCDLayout, 1);
                }
                break; // while (true)

            } else if (configItemsC.size() >= 4) {

                fillAWithC(configBCDLayout, 2);

            } else if (configItemsC.size() > 0) {

                fillAWithC(configBCDLayout, 1);
                fillAWithD(configBCDLayout, 2);

            } else if (configItemsD.size() > 0) {

                fillAWithD(configBCDLayout, 4);
            }
            break; // while (true)

        } // while (true)

        doConfigItemsLand();

    } // doConfigItemsALand()

    private void fillAWithB(LinearLayout configBCDLayout, int maxItems) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:fillAWithB():"
                        + " this[" + this + "]"
        );

        int nbrListItems = Math.min(configItemsB.size(), maxItems);

        int configItemNdx = 0;
        ConfigItem configItem = null;

        for (configItemNdx = 0; configItemNdx < nbrListItems; configItemNdx++) {

            configItem = configItemsB.get(0);

            configItemsB.remove(0);

            if (LOGGING) Log.v(TAG, "ConfiguratorFragment:fillAWithB(): configItem:"
                            + " configItem.title[" + configItem.title + "]"
                            + " configItem.bannerUrl[" + configItem.bannerUrl + "]"
                            + " this[" + this + "]"
            );

            // Category ImageView

            ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            int marginBottom = (configItemNdx == (nbrListItems - 1)) ? MARGIN_ZERO : MARGIN_BOTTOM_DP;
            LinearLayout widgetLayout = getWidgetLayout(bItemWidth, bItemHeight, marginBottom categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            configBCDLayout.addView(widgetLayout);
        }

    } // fillAWithB()

    private void fillAWithC(LinearLayout configBCDLayout, int maxItems) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:fillAWithC():"
                        + " this[" + this + "]"
        );

        LinearLayout subLayoutContainer = null;

        // maxItems is the maximum number of SubLayout containers allowed. Each
        // SubLayout container can contain 2 list items.
        int nbrListItems = Math.min(configItemsC.size(), (maxItems * 2));

        boolean firstSubInContainer = true;

        int nbrSubLayoutContainers = 0;

        int configItemNdx = 0;
        ConfigItem configItem = null;

        for (configItemNdx = 0; configItemNdx < nbrListItems; configItemNdx++) {

            configItem = configItemsC.get(0);

            configItemsC.remove(0);

            firstSubInContainer = (configItemNdx % 2 == 0);

            if (firstSubInContainer) {

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);
                configBCDLayout.addView(subLayoutContainer, subLayoutContainerLayoutParms);

                nbrSubLayoutContainers++;
            }

            ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            int marginBottom = (configItemNdx == (nbrListItems - 1)) ? MARGIN_ZERO : MARGIN_BOTTOM_DP;
            LinearLayout widgetLayout = getWidgetLayout(cItemWidth, cItemHeight, marginBottom, categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);
        }

    } // fillAWithC()

    private void fillAWithD(LinearLayout configBCDLayout, int maxItems) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:fillAWithD():"
                        + " this[" + this + "]"
        );

        int nbrListItems = Math.min(configItemsD.size(), maxItems);

        int configItemNdx = 0;
        ConfigItem configItem = null;

        for (configItemNdx = 0; configItemNdx < nbrListItems; configItemNdx++) {

            configItem = configItemsD.get(0);

            configItemsD.remove(0);

            if (LOGGING) Log.v(TAG, "ConfiguratorFragment:fillAWithD(): configItem:"
                            + " configItem.title[" + configItem.title + "]"
                            + " configItem.bannerUrl[" + configItem.bannerUrl + "]"
                            + " this[" + this + "]"
            );

            // Category ImageView

            ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            int marginBottom = (configItemNdx == (nbrListItems - 1)) ? MARGIN_ZERO : MARGIN_BOTTOM_DP;
            LinearLayout widgetLayout = getWidgetLayout(dItemWidth, dItemHeight, marginBottom, categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            configBCDLayout.addView(widgetLayout);
        }

    } // fillAWithD()

    private LinearLayout getWidgetLayout(int layoutWidth, int layoutHeight, int marginBottom, View childView) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:getWidgetLayout():"
                        + " layoutWidth[" + layoutWidth + "]"
                        + " layoutHeight[" + layoutHeight + "]"
                        + " childView[" + childView + "]"
                        + " this[" + this + "]"
        );

        // Vertical. Contains selectable content. Used to create a rectangular
        // frame around the content.
        LinearLayout widgetLayout = null;

        widgetLayout = new LinearLayout(activity);
        /* @@@ STUBBED
        widgetLayout.setBackgroundResource(R.drawable.rectangle_frame);
        @@@ STUBBED */

        widgetLayout.setId(widgetLayout.hashCode());
        widgetLayout.setOrientation(LinearLayout.VERTICAL);
        widgetLayout.setMeasureWithLargestChildEnabled(true);

        widgetLayout.setPadding(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);

        widgetLayoutParms =
                new LinearLayout.LayoutParams(layoutWidth, // width
                        layoutHeight); // height

        int margin = 0;
        widgetLayoutParms.setMargins(margin, margin, margin, marginBottom); // left, top, right, bottom

        childView.setLayoutParams(widgetLayoutParms);

        return (widgetLayout);

    } // getWidgetLayout()

    private LinearLayout getBCDLayout() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:getBCDLayout():"
                        + " this[" + this + "]"
        );

        LinearLayout.LayoutParams configBCDLayoutParms =

                new LinearLayout.LayoutParams(aItemWidth, // width
                        aItemHeight); // height

        LinearLayout configBCDLayout = new LinearLayout(activity);
        configBCDLayout.setLayoutParams(configBCDLayoutParms);

        configBCDLayout.setId(configBCDLayout.hashCode());
        configBCDLayout.setOrientation(LinearLayout.VERTICAL);
        configBCDLayout.setMeasureWithLargestChildEnabled(true);

        configBCDLayout.setPadding(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);

        return (configBCDLayout);

    } // getBCDLayout()

    private LinearLayout getSubLayout(int layoutWidth, int layoutHeight, int orientation) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:getSubLayout():"
                        + " layoutWidth[" + layoutWidth + "]"
                        + " layoutHeight[" + layoutHeight + "]"
                        + " this[" + this + "]"
        );

        LinearLayout.LayoutParams configSubLayoutParms =
                new LinearLayout.LayoutParams(layoutWidth, // width
                        layoutHeight); // height

        LinearLayout subLayout = new LinearLayout(activity);

        subLayout.setLayoutParams(configSubLayoutParms);
        /* @@@ STUBBED
        subLayout.setBackgroundResource(R.drawable.rectangle_frame);
        @@@ STUBBED */

        subLayout.setId(subLayout.hashCode());
        subLayout.setOrientation(orientation);
        subLayout.setMeasureWithLargestChildEnabled(true);

        subLayout.setPadding(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);

        return (subLayout);

    } // getSubLayout()

    private LinearLayout getSubLayoutContainer(int orientation) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:getSubLayoutContainer():"
                        + " orientation[" + orientation + "]"
                        + " this[" + this + "]"
        );

        LinearLayout subLayoutContainer = new LinearLayout(activity);
        subLayoutContainer.setId(subLayoutContainer.hashCode());
        subLayoutContainer.setOrientation(orientation);
        subLayoutContainer.setMeasureWithLargestChildEnabled(true);

        subLayoutContainer.setPadding(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);

        return (subLayoutContainer);

    } // getSubLayoutContainer()

    private ImageView getImageView(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:getImageView():"
                        + " this[" + this + "]"
        );

        ImageView imageView = new ImageView(activity);
        imageView.setId(imageView.hashCode());
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        imageView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        return (imageView);

    } // getImageView()

    private void setImage(ImageView imageView, String imageUrl) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:setImage():"
                        + " imageUrl[" + imageUrl + "]"
                        + " imageView[" + imageView + "]"
                        + " this[" + this + "]"
        );

        RequestCreator requestCreator = picasso.load(imageUrl);
        requestCreator.error(noPhoto);
        requestCreator.into(imageView);
        requestCreator.fit();

    } // setImage()

    private void doConfigItemsLand() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:doConfigItemsLand():"
                        + " this[" + this + "]"
        );

        if (configItemsB.size() > 0) {
            fillWithBLand();
        }
        if (configItemsC.size() > 0) {
            fillWithCLand();
        }
        if (configItemsD.size() > 0) {
            fillWithDLand();
        }

    } // doConfigItemsLand()

    private void fillWithBLand() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:fillWithBLand():"
                        + " this[" + this + "]"
        );

        final int NBR_ITEMS_IN_CONTAINER = 2;

        int nbrListItems = configItemsB.size();

        boolean firstSubInContainer = true;

        int configItemNdx = 0;
        ConfigItem configItem = null;

        LinearLayout subLayoutContainer = null;

        for (configItemNdx = 0; configItemNdx < nbrListItems; configItemNdx++) {

            configItem = configItemsB.get(0);

            configItemsB.remove(0);

            firstSubInContainer = (configItemNdx % NBR_ITEMS_IN_CONTAINER == 0);

            if (firstSubInContainer) {

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);
                subLayoutContainerLayoutParms.setMargins(MARGIN_ZERO, MARGIN_ZERO, MARGIN_ZERO, MARGIN_ZERO); // left, top, right, bottom
                configScrollLayout.addView(subLayoutContainer, subLayoutContainerLayoutParms);
                subLayoutContainerLayoutParms.setMargins(MARGIN_LEFT_DP, MARGIN_TOP_DP, MARGIN_RIGHT_DP, MARGIN_BOTTOM_DP); // left, top, right, bottom
            }

            ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(bItemWidth, bItemHeight, MARGIN_BOTTOM_DP, categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);
        }

    } // fillWithBLand()

    private void fillWithCLand() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:fillWithCLand():"
                        + " this[" + this + "]"
        );

        final int NBR_ITEMS_IN_CONTAINER = 4;

        int nbrListItems = configItemsC.size();

        boolean firstSubInContainer = true;

        int nbrCItemsInContainer = 0;

        int configItemNdx = 0;
        ConfigItem configItem = null;

        LinearLayout subLayoutContainer = null;

        for (configItemNdx = 0; configItemNdx < nbrListItems; configItemNdx++) {

            configItem = configItemsC.get(0);

            configItemsC.remove(0);

            firstSubInContainer = (configItemNdx % NBR_ITEMS_IN_CONTAINER == 0);

            if (firstSubInContainer) {

                nbrCItemsInContainer = 0;

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);

                subLayoutContainerLayoutParms.setMargins(MARGIN_ZERO, MARGIN_ZERO, MARGIN_ZERO, MARGIN_BOTTOM_DP); // left, top, right, bottom
                configScrollLayout.addView(subLayoutContainer, subLayoutContainerLayoutParms);
                subLayoutContainerLayoutParms.setMargins(MARGIN_LEFT_DP, MARGIN_TOP_DP, MARGIN_RIGHT_DP, MARGIN_BOTTOM_DP); // left, top, right, bottom
            }

            ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(cItemWidth, cItemHeight, MARGIN_ZERO, categoryImageView);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.setTag(configItem);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);

            nbrCItemsInContainer++;
        }

        if (nbrCItemsInContainer < 4) {

            padWithDLand(subLayoutContainer, 2);
        }

    } // fillWithCLand()

    private void padWithDLand(LinearLayout subLayoutContainer, int nbrListItems) {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:padWithDLand():"
                        + " nbrListItems[" + nbrListItems + "]"
                        + " this[" + this + "]"
        );

        int configItemsSize = configItemsD.size();
        int lastListItem = (nbrListItems <= configItemsSize) ? nbrListItems : configItemsSize;
        int configItemNdx = 0;
        ConfigItem configItem = null;

        LinearLayout dItemContainer = getSubLayoutContainer(LinearLayout.VERTICAL);
        subLayoutContainer.addView(dItemContainer);

        for (configItemNdx = 0; configItemNdx < lastListItem; configItemNdx++) {

            configItem = configItemsD.get(0);

            configItemsD.remove(0);

            ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(dItemWidth, dItemHeight, MARGIN_ZERO, categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            dItemContainer.addView(widgetLayout, widgetLayoutParms);
        }

    } // padWithDLand()

    private void fillWithDLand() {

        if (LOGGING) Log.v(TAG, "ConfiguratorFragment:fillWithDLand():"
                        + " this[" + this + "]"
        );

        final int NBR_ITEMS_IN_CONTAINER = 2;

        int nbrListItems = configItemsD.size();

        boolean firstSubInContainer = true;

        int configItemNdx = 0;
        ConfigItem configItem = null;

        LinearLayout subLayoutContainer = null;

        for (configItemNdx = 0; configItemNdx < nbrListItems; configItemNdx++) {

            configItem = configItemsD.get(0);

            configItemsD.remove(0);

            firstSubInContainer = (configItemNdx % NBR_ITEMS_IN_CONTAINER == 0);

            if (firstSubInContainer) {

                subLayoutContainer = getSubLayoutContainer(LinearLayout.HORIZONTAL);
                subLayoutContainerLayoutParms.setMargins(MARGIN_ZERO, MARGIN_ZERO, MARGIN_ZERO, MARGIN_BOTTOM_DP); // left, top, right, bottom
                configScrollLayout.addView(subLayoutContainer, subLayoutContainerLayoutParms);
                subLayoutContainerLayoutParms.setMargins(MARGIN_LEFT_DP, MARGIN_TOP_DP, MARGIN_RIGHT_DP, MARGIN_BOTTOM_DP); // left, top, right, bottom
            }

            ImageView categoryImageView = getImageView(PADDING_ZERO, PADDING_ZERO, PADDING_ZERO, PADDING_ZERO);
            setImage(categoryImageView, configItem.bannerUrl);

            // Vertical. Contains selectable content. Used to create a
            // rectangular frame around the content.
            LinearLayout widgetLayout = getWidgetLayout(dItemWidth, dItemHeight, MARGIN_ZERO, categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            subLayoutContainer.addView(widgetLayout);
        }

    } // fillWithDLand()
}
