package com.staples.mobile.cfa.analytics;

import android.content.Context;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.staples.mobile.cfa.home.ConfigItem;
import com.staples.mobile.common.access.easyopen.model.browse.Analytic;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.HashMap;

/**
 * Created by burcoral on 2/5/15.
 */


//See why this type of singleton--> http://howtodoinjava.com/2012/10/22/singleton-design-pattern-in-java/

public class Tracker {

    public enum AppType { AFA, CFA }
    public enum UserType { GUEST, REGISTERED }



    private static volatile Tracker instance = null;

    public static HashMap<String, Object> globalContextData;

    // private constructor
    private Tracker() {
    }

    /** get singleton instance of Tracker */
    public static Tracker getInstance() {
        if (instance == null) {
            synchronized (Tracker.class) {
                // Double check
                if (instance == null) {
                    instance = new Tracker();
                }
            }
        }
        return instance;
    }

    //////////////////////////////////////////////////////////
    ////////////// initialization calls //////////////////////
    //////////////////////////////////////////////////////////

    /** initialize analytics */
    public void initialize(AppType appType, Context context, boolean enableDebugLogging) {
        switch(appType) {
            case AFA:
                setAFAGlobalDefinitions();
            case CFA:
                setCFAGlobalDefinitions();
        }
        Config.setContext(context);
        Config.setDebugLogging(enableDebugLogging);
        //Analytics
        Config.collectLifecycleData();
    }

    private boolean isInitialized() {
        return (globalContextData != null);
    }

    /** enable or disable tracking of data */
    public void enableTracking(boolean enable) {
        if (isInitialized()) {
            if (enable) {
                Config.collectLifecycleData();
            } else {
                Config.pauseCollectingLifecycleData();
            }
        }
    }

    /** CFA should call this as user type changes between guest and registered */
    public void setUserType(UserType userType) {
        if (isInitialized()) {
            switch (userType) {
                case GUEST:
                    globalContextData.put("s.evar10", "Guest");
                    globalContextData.remove("s.evar32"); // remove email id
                    globalContextData.remove("s.evar11"); // remove rewards number
                    break;
                case REGISTERED:
                    globalContextData.put("s.evar10", "Registered");
                    break;
            }
        }
    }

    /** CFA should call this whenever profile is loaded */
    public void setProfileInfo(String emailId, String rewardsNumber) {
        if (isInitialized()) {
            globalContextData.put("s.evar32", emailId);
            globalContextData.put("s.evar11", rewardsNumber);
        }
    }

    /**  */
    public void setPreviousPageName(String previousPageName) {
        if (isInitialized()) {
            globalContextData.put("s.prop26", previousPageName);
        }
    }

    /**  */
    public void setProductRating(String productRating) {
        if (isInitialized()) {
            globalContextData.put("s.evar27", productRating);
        }
    }

    //////////////////////////////////////////////////////////
    ////////////// trackState calls //////////////////////////
    //////////////////////////////////////////////////////////


    public void trackStateForHome() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        contextData.put("s.pageName", "Homepage");
        contextData.put("Channel", "Home");
        contextData.put("s.events", "event4");
        contextData.put("s.prop3", "Home");
        contextData.put("s.prop4", "Homepage");
        contextData.put("s.prop5", "Homepage");
        contextData.put("s.prop6", "Homepage");
        Analytics.trackState("s.pageName", contextData);
    }


    /** search tab used by afa */
    public void trackStateForSearchTab() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        contextData.put("s.pageName", "Search Tab");
        contextData.put("s.evar3", "Search");
        contextData.put("s.evar17", "Search: Basic Search");
        contextData.put("s.prop3", "Search");
        contextData.put("s.prop38", "Search");
        Analytics.trackState("s.pageName", contextData);
    }

    /** search bar used by cfa */
    public void trackStateForSearchBar() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        contextData.put("s.pageName", "Search Bar");
        contextData.put("s.prop3", "Search Bar");
        contextData.put("s.prop4", "Search Bar");
        contextData.put("s.prop5", "Search Bar");
        contextData.put("s.prop6", "Search Bar");
        Analytics.trackState("s.pageName", contextData);
    }


    public void trackStateForSearchResults(String term, int count) {
        HashMap<String, Object> contextData = createContextWithGlobal();
        contextData.put("s.pageName", "Search Results");
        contextData.put("Channel", "Search Results");
        contextData.put("s.evar1", term);
        contextData.put("s.prop1", term);
        contextData.put("s.prop2", count);
        contextData.put("s.prop3", "Search Results");
        contextData.put("s.prop4", "Search Results");
        contextData.put("s.prop5", "Search Results");
        contextData.put("s.prop6", "Search Results");
        contextData.put("s.prop54", "Best Match");
        contextData.put("s.prop53", "List");
        Analytics.trackState("s.pageName", contextData);
    }



    public void trackStateForProduct(Product product) {
        if (product != null) {
            HashMap<String, Object> contextData = createContextWithGlobal();
            contextData.put("s.pageName", "Product Screen");
            contextData.put("s.products", product.getDisplayName());
            contextData.put("s.prop3", product.getDisplayName());
            contextData.put("s.evar27", product.getCustomerReviewRating());
            if (product.getAnalytic() != null && product.getAnalytic().size() > 0) {
                Analytic an = product.getAnalytic().get(0);
                if (an != null) {
                    contextData.put("Channel", an.getSuperCategoryName());
                    contextData.put("s.prop4", an.getCategoryName());
                    contextData.put("s.prop5", an.getDepartmentName());
                    contextData.put("s.prop6", an.getClassName());
                }
            }
            Analytics.trackState("s.pageName", contextData);
        }
    }

    //////////////////////////////////////////////////////////
    ////////////// trackAction calls //////////////////////////
    //////////////////////////////////////////////////////////

    public void trackActionForNavigationDrawer(String drawerItemText, String currentPageName) {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("Item Click", "Nav Drawer");
        contextData.put("Click", 1);
        contextData.put("s.prop27", drawerItemText + "|" + currentPageName);
        Analytics.trackAction("Item Click", contextData);
    }


    public void trackActionForPersonalizedMessaging(String personalizedMsg) {
        // TODO: are really supposed to track a count???
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("Item Click", personalizedMsg);
        contextData.put("Click", 1);
        Analytics.trackAction("Item Click", contextData);
    }

    public void trackActionForHomePage(ConfigItem configItem) {
        if (configItem != null) {
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("Item Click", "Homepage Banner");
            contextData.put("Click", 1);
            contextData.put("s.evar4", configItem.title);
            contextData.put("s.evar3", "Internal Campaigns");
            contextData.put("s.evar17", "Internal Campaigns:Homepage");
            contextData.put("s.prop38", "Internal Campaigns");
            Analytics.trackAction("Item Click", contextData);
        }
    }

    //////////////////////////////////////////////////////////
    ////////////// private calls //////////////////////////
    //////////////////////////////////////////////////////////



    private HashMap<String, Object> createContextWithGlobal() {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.putAll(globalContextData);
        return contextData;
    }

    private void setAFAGlobalDefinitions() {
        setCommonGlobalDefinitions();
        globalContextData.put("s.evar73", "Associate App");
        globalContextData.put("s.evar18", getAssociateId());
        globalContextData.put("s.evar75", getStoreId());
    }
    private void setCFAGlobalDefinitions() {
        setCommonGlobalDefinitions();
        globalContextData.put("s.prop71", "Android App");
        globalContextData.put("s.evar73", "Android App");
        setUserType(UserType.GUEST); // setting to Guest initially, app must update
        globalContextData.put("s.evar35", "en-US");
        globalContextData.put("s.prop41", "en-US");

        // Note that the app must dynamically update rewards no as profile is loaded and reloaded.
        // Also, app must dynamically update the user type to Guest or Registered as the user signs in and out.
    }
    private static void setCommonGlobalDefinitions() {
        globalContextData = new HashMap<String, Object>();
        globalContextData.put("s.evar51", getZipCode());
    }

    //@TODO
    private static String getZipCode() {
        return "02139";
    }
    //@TODO
    private static String getStoreId() {
        return "01154";
    }
    //@TODO
    private static String getAssociateId() {
        return "1656013";
    }

}