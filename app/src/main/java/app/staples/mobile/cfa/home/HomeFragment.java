package app.staples.mobile.cfa.home;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.model.store.Obj;
import com.staples.mobile.common.access.channel.model.store.StoreAddress;
import com.staples.mobile.common.access.channel.model.store.StoreData;
import com.staples.mobile.common.access.channel.model.store.StoreHours;
import com.staples.mobile.common.access.channel.model.store.StoreQuery;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.configurator.model.Area;
import com.staples.mobile.common.access.configurator.model.Configurator;
import com.staples.mobile.common.access.configurator.model.Item;
import com.staples.mobile.common.access.configurator.model.Screen;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.common.device.DeviceInfo;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.location.LocationFinder;
import app.staples.mobile.cfa.profile.ProfileDetails;
import app.staples.mobile.cfa.store.StoreFragment;
import app.staples.mobile.cfa.store.TimeSpan;
import app.staples.mobile.cfa.util.CurrencyFormat;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.DataWrapper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeFragment extends Fragment implements LocationFinder.PostalCodeCallback, AppConfigurator.AppConfiguratorCallback {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private static final boolean LOGGING = false;

    private static final int PADDING_ZERO = 0;

    private static final int MARGIN_ZERO = 0;
    private static final int MARGIN_LEFT_DP = 0;
    private static final int MARGIN_TOP_DP = 0;
    private static final int MARGIN_RIGHT_DP = 0;
    private static final int MARGIN_BOTTOM_DP = 24;

    private MainActivity activity;
    private Resources resources;

    private DeviceInfo deviceInfo;
    private String postalCode;

    private int lastOrientation = Configuration.ORIENTATION_UNDEFINED;
    private View configFrameView;
    private LinearLayout configScrollLayout;
    private LinearLayout.LayoutParams subLayoutContainerLayoutParms;
    private LinearLayout.LayoutParams widgetLayoutParms;

    private AppConfigurator appConfigurator;
    private Configurator configurator;
    private StaplesAppContext staplesAppContext;

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

    // Personalized Message Bar UI Elements
    private TextView loginText;
    private LinearLayout login_layout;
    private LinearLayout reward_layout;
    private LinearLayout store_wrapper;
    private TextView rewardTextView;
    private TextView storeNameTextView;
    private TextView storeStatusTextView;
    private DataWrapper storeWrapper;

    @Override
    public void onAttach(Activity activity) {

        if (LOGGING) Log.v(TAG, "HomeFragment:onAttach():"
                        + " activity[" + activity + "]"
                        + " this[" + this + "]"
        );

        super.onAttach(activity);

        this.activity = (MainActivity) activity;

        resources = activity.getResources();
        appConfigurator = AppConfigurator.getInstance();

        configItems = new ArrayList<ConfigItem>();
        configItemsA = new ArrayList<ConfigItem>();
        configItemsB = new ArrayList<ConfigItem>();
        configItemsC = new ArrayList<ConfigItem>();
        configItemsD = new ArrayList<ConfigItem>();

        return;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {

        Crittercism.leaveBreadcrumb("HomeFragment:onCreateView(): Displaying the home/landing screen.");

        Configuration configuration = resources.getConfiguration();
        lastOrientation = configuration.orientation;

        picasso = Picasso.with(activity);

        noPhoto = ResourcesCompat.getDrawable(resources, R.drawable.no_photo, null);

        configFrameView = layoutInflater.inflate(R.layout.home_fragment, container, false);

        configScrollLayout = (LinearLayout) configFrameView.findViewById(R.id.configScrollLayout);

        storeWrapper = (DataWrapper) configFrameView.findViewById(R.id.store_wrapper);

        subLayoutContainerLayoutParms =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, // width
                        ViewGroup.LayoutParams.MATCH_PARENT); // height

        subLayoutContainerLayoutParms.setMargins(MARGIN_LEFT_DP, MARGIN_TOP_DP, MARGIN_RIGHT_DP, MARGIN_BOTTOM_DP); // left, top, right, bottom

        itemOnClickListener = new OnClickListener() {

            @Override
            public void onClick(View view) {
                

                ConfigItem configItem = (ConfigItem) view.getTag();
                if (configItem != null) {
                    Crittercism.leaveBreadcrumb("HomeFragment:OnClickListener.onClick(): User has selected an item with the following title: configItem.title[" + configItem.title + "]");
                    Tracker.getInstance().trackActionForHomePage(configItem.title); // Analytics
                }
                activity.selectBundle(configItem.title, configItem.identifier);
            }
        };

        // initiate personalized message bar
        findMessageBarViews();
        updateMessageBar();

        // checking for configurator just to be sure, but MainActivity should not allow this
        // fragment to be loaded if no configurator.
        if (appConfigurator != null) {

            configurator = appConfigurator.getConfigurator();

            if (configurator != null) {
                initFromConfiguratorResult();
            }
        }

        return (configFrameView);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

        if (LOGGING) Log.v(TAG, "HomeFragment:onConfigurationChanged():"
                        + " lastOrientation[" + lastOrientation + "]"
                        + " configuration.orientation[" + configuration.orientation + "]"
                        + " configurator[" + configurator + "]"
                        + " this[" + this + "]"
        );

        if (configuration.orientation != lastOrientation) {

            lastOrientation = configuration.orientation;

            FrameLayout messageLayout = (FrameLayout) configScrollLayout.getChildAt(0);
            configScrollLayout.removeAllViews();
            configScrollLayout.addView(messageLayout);

            initFromConfiguratorResult();
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        appConfigurator = AppConfigurator.getInstance();
        appConfigurator.getConfigurator(this); // AppConfiguratorCallback

        ActionBar.getInstance().setConfig(ActionBar.Config.DEFAULT);

        Tracker.getInstance().trackStateForHome(); // Analytics

        // call store api and get store info
        LocationFinder finder = LocationFinder.getInstance(getActivity());
        finder.registerPostalCodeListener(this);
        String postalCode = finder.getPostalCode();
        if (TextUtils.isEmpty(postalCode)) {
            finder.getLocation();
        } else {
            onGetPostalCodeSuccess(postalCode);
        }
    }

    private void initFromConfiguratorResult() {

        if (LOGGING) Log.v(TAG, "HomeFragment:initFromConfiguratorResult():"
                        + " this[" + this + "]"
        );

        while (true) {

            deviceInfo = new DeviceInfo(resources);

            staplesAppContext = StaplesAppContext.getInstance();

            screens = staplesAppContext.getScreen();

            if (LOGGING) Log.v(TAG, "HomeFragment:initFromConfiguratorResult():"
                            + " screens[" + screens + "]"
                            + " this[" + this + "]"
            );

            if (screens == null) break; // while (true)

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

            if (LOGGING) Log.v(TAG, "HomeFragment:initFromConfiguratorResult():"
                            + " items[" + items + "]"
                            + " this[" + this + "]"
            );

            if (items == null) break; // while (true)

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

            break; // while (true)

        } // while (true)
    }

    @Override
    public void onPause() {
        LocationFinder.getInstance(getActivity()).unRegisterPostalCodeListener(this);
        super.onPause();
    }

    private void doPortrait() {

        if (LOGGING) Log.v(TAG, "HomeFragment:doPortrait():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:doConfigItemsABDPort():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:doConfigItemsCPort():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:doLandscape():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:doLandscape():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:doConfigItemsALand():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:fillAWithB():"
                        + " this[" + this + "]"
        );

        int nbrListItems = Math.min(configItemsB.size(), maxItems);

        int configItemNdx = 0;
        ConfigItem configItem = null;

        for (configItemNdx = 0; configItemNdx < nbrListItems; configItemNdx++) {

            configItem = configItemsB.get(0);

            configItemsB.remove(0);

            if (LOGGING) Log.v(TAG, "HomeFragment:fillAWithB(): configItem:"
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
            LinearLayout widgetLayout = getWidgetLayout(bItemWidth, bItemHeight, marginBottom, categoryImageView);
            widgetLayout.setTag(configItem);
            widgetLayout.setOnClickListener(itemOnClickListener);
            widgetLayout.addView(categoryImageView);

            configBCDLayout.addView(widgetLayout);
        }

    } // fillAWithB()

    private void fillAWithC(LinearLayout configBCDLayout, int maxItems) {

        if (LOGGING) Log.v(TAG, "HomeFragment:fillAWithC():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:fillAWithD():"
                        + " this[" + this + "]"
        );

        int nbrListItems = Math.min(configItemsD.size(), maxItems);

        int configItemNdx = 0;
        ConfigItem configItem = null;

        for (configItemNdx = 0; configItemNdx < nbrListItems; configItemNdx++) {

            configItem = configItemsD.get(0);

            configItemsD.remove(0);

            if (LOGGING) Log.v(TAG, "HomeFragment:fillAWithD(): configItem:"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:getWidgetLayout():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:getBCDLayout():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:getSubLayout():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:getSubLayoutContainer():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:getImageView():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:setImage():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:doConfigItemsLand():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:fillWithBLand():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:fillWithCLand():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:padWithDLand():"
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

        if (LOGGING) Log.v(TAG, "HomeFragment:fillWithDLand():"
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

    @Override
    public void onGetPostalCodeSuccess(String postalCode) {
        if (TextUtils.isEmpty(postalCode)) {
            // display "no store nearby"
            if (storeWrapper != null) {
                storeWrapper.setState(DataWrapper.State.EMPTY);
            }
            Log.d(TAG, (String) getResources().getText(R.string.error_no_location_service));
        } else {
            Log.d(TAG, "Store zipcode:" + postalCode);
            Access.getInstance().getChannelApi(false).storeLocations(postalCode, new StoreInfoCallback());
        }
    }

    @Override
    public void onGetPostalCodeFailure() {
        if (storeWrapper != null) {
            storeWrapper.setState(DataWrapper.State.EMPTY);
        }
    }

    @Override
    public void onGetConfiguratorResult(Configurator configurator, boolean success, RetrofitError retrofitError) {
        if(success) {
            Log.d(TAG, "Successfully retrieved MCS data");
        } else {
            Log.d(TAG, retrofitError.getMessage());
        }
    }

    private class StoreInfoCallback implements Callback<StoreQuery> {
        @Override
        public void success(StoreQuery storeQuery, Response response) {
            Activity activity = getActivity();
            if (!(activity instanceof MainActivity)) return;

            List<StoreData> storeData = storeQuery.getStoreData();
            // if there are any nearby stores
            if(storeData != null) {
                if (!storeData.isEmpty()) {
                    Obj storeObj = storeData.get(0).getObj();

                    // Get store location
                    StoreAddress storeAddress = storeObj.getStoreAddress();
                    String storeCity = storeAddress.getCity();
                    String storeState = storeAddress.getState();
                    storeNameTextView.setText(storeCity + "," + storeState);

                    // Get store office hours
                    List<StoreHours> storeHourList = storeObj.getStoreHours();
                    ArrayList<TimeSpan> spans = new ArrayList<TimeSpan>();
                    for (StoreHours hours : storeHourList) {
                        TimeSpan span = TimeSpan.parse(hours.getDayName(), hours.getHours());
                        if (span != null) spans.add(span);
                    }
                    String status = TimeSpan.formatStatus(activity, spans, System.currentTimeMillis());
                    int i = status.indexOf(' ');
                    if (i > 0) status = status.substring(0, i);
                    storeStatusTextView.setText(status);

                    if (storeCity == null) {
                        storeWrapper.setState(DataWrapper.State.EMPTY);
                    } else {
                        storeWrapper.setState(DataWrapper.State.DONE);
                    }
                }
                // display "find nearby store" if no store result
                else {
                    storeWrapper.setState(DataWrapper.State.EMPTY);
                    Log.d(TAG, "No nearby stores found from the StoreInfoCallback.");
                }
            }
            // display "find nearby store" if storeData is null
            else {
                storeWrapper.setState(DataWrapper.State.EMPTY);
                Log.d(TAG, "storeData is null from the StoreInfoCallback.");
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            String message = ApiError.getErrorMessage(retrofitError);
            Log.d(TAG, message);

            storeWrapper.setState(DataWrapper.State.EMPTY);
        }
    }

    private void findMessageBarViews(){
        login_layout = (LinearLayout) configFrameView.findViewById(R.id.login_layout);
        loginText = (TextView) configFrameView.findViewById(R.id.login_message);
        reward_layout = (LinearLayout) configFrameView.findViewById(R.id.reward_layout);
        store_wrapper = (LinearLayout) configFrameView.findViewById(R.id.store_wrapper);
        rewardTextView = (TextView) configFrameView.findViewById(R.id.reward);
        storeNameTextView = (TextView) configFrameView.findViewById(R.id.store_name);
        storeStatusTextView = (TextView) configFrameView.findViewById(R.id.store_status);
    }

    private void updateMessageBar(){
        setMessageListeners();

        Access access = Access.getInstance();
        // Logged In
        // Note that member can be null if failure to retrieve profile following successful login
        if(access.isLoggedIn() && !access.isGuestLogin() && ProfileDetails.getMember() != null){
            float rewards = 0;
            Member member = ProfileDetails.getMember();
            if (member.getRewardsNumber() != null && member.getRewardDetails() != null) {
                rewards = ProfileDetails.getRewardsTotal();
            }

            if (rewards != 0) {
                login_layout.setVisibility(View.GONE);
                reward_layout.setVisibility(View.VISIBLE);
                rewardTextView.setText(CurrencyFormat.getFormatter().getCurrency().toString() + (int) rewards);
                Log.d(TAG, "Rewards from message bar: " + rewards);
            }
            else {
                String loginMessage = MessageFormat.format(getString(R.string.welcome_format), member.getUserName());
                loginText.setText(loginMessage);

                login_layout.setVisibility(View.VISIBLE);
                reward_layout.setVisibility(View.GONE);

                login_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tracker.getInstance().trackActionForPersonalizedMessaging("Profile"); // Analytics
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.selectProfileFragment();
                    }
                });
            }
        }
        // Not Logged In
        else{
            loginText.setText(R.string.login_greeting);
            login_layout.setVisibility(View.VISIBLE);
            reward_layout.setVisibility(View.GONE);
        }
    }

    private void setMessageListeners(){
        login_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.getInstance().trackActionForPersonalizedMessaging("Login"); // Analytics
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.selectLoginFragment();
            }
        });

        reward_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.getInstance().trackActionForPersonalizedMessaging("Reward"); // Analytics
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.selectRewardsFragment();
            }
        });

        store_wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.getInstance().trackActionForPersonalizedMessaging("Store"); // Analytics
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.selectFragment(DrawerItem.STORE, new StoreFragment(), MainActivity.Transition.NONE, true);
            }
        });
    }
    // End of Personalized Message Bar Methods
    //////////////////////////////////////////////////////////////////////////////
}
