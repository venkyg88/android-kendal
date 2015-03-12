package com.staples.mobile.cfa.analytics;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.staples.mobile.cfa.home.ConfigItem;
import com.staples.mobile.common.access.easyopen.model.browse.Analytic;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;

import java.util.HashMap;
import java.util.List;

/**
 * initially created by burcoral on 2/5/15. further developed by diana sutlief
 */


//See why this type of singleton--> http://howtodoinjava.com/2012/10/22/singleton-design-pattern-in-java/

public class Tracker {

    public enum AppType { AFA, CFA }
    public enum UserType { GUEST, REGISTERED }
    public enum SearchType { BASIC_SEARCH, AUTOCOMPLETE, RECENT_SEARCH }
    public enum ViewType { LIST("List"), GRID("Grid");
        private String name;
        ViewType(String name) { this.name = name; }
        public String getName() { return name; }
    }
    public enum PageType {
        PAGE_LOGIN("Login"),
        PAGE_HOME("Homepage"),
        PAGE_INTERNAL_CAMPAIGNS("Internal Campaigns"),
        PAGE_PERSONAL_FEED("Personal Feed"),
        PAGE_CART("Shopping Cart"),
        PAGE_CART_COUPONS("Shopping Cart Coupons"),
        PAGE_CHECKOUT("Checkout"),
        PAGE_CHECKOUT_LOGIN("Checkout Login"),
        PAGE_CHECKOUT_REVIEW_AND_PAY("Review & Pay"),
        PAGE_CHECKOUT_EDIT_SHIPPING("Checkout Edit Shipping"),
        PAGE_CHECKOUT_EDIT_BILLING("Checkout Edit Billing"),
        PAGE_CHECKOUT_EDIT_PAYMENT("Checkout Edit Payment"),
        PAGE_PRODUCT_DETAIL("Product Detail"),
        PAGE_SKU_SET("SKU Set"),
        PAGE_CLASS("Class"),
        PAGE_ACCOUNT("My Account"),
        PAGE_STORE("Store"),
        PAGE_WEEKLY_AD("WeeklyAd"),
        PAGE_SHOP_BY_CATEGORY("Shop by Category"),
        PAGE_SEARCH("Search"),
        PAGE_SEARCH_BAR("Search Bar"),
        PAGE_SEARCH_TAB("Search Tab"),
        PAGE_SEARCH_RESULTS("Search Results");

        private String name;
        PageType(String name) { this.name = name; }
        public String getName() { return name; }
    }

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
        String pageTypeName = PageType.PAGE_HOME.getName();
        addNameProperties(contextData, pageTypeName);
        contextData.put("PageViews", 1); // event4
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForLogin() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_LOGIN.getName();
        addNameProperties(contextData, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForResetPassword() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_LOGIN.getName();
        String longName = pageTypeName + ": Reset Password";
        addNameProperties(contextData, pageTypeName, pageTypeName, longName, longName, longName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForRegister() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_ACCOUNT.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Create New Account";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForProfile() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_ACCOUNT.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Profile";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForRewards() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_ACCOUNT.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Rewards";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForOrders() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_ACCOUNT.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Order";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForOrderDetails() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_ACCOUNT.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Order Details";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForStoreFinder() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_STORE.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Store Finder";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForStoreResults() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_STORE.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Store Results";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForStoreDetail() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_STORE.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Store Detail";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }


    public void trackStateForWeeklyAdClass() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_WEEKLY_AD.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Class";
        addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForWeeklyAd() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_WEEKLY_AD.getName();
        addNameProperties(contextData, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForShopByCategory() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_SHOP_BY_CATEGORY.getName();
        addNameProperties(contextData, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForPersonalFeed() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_PERSONAL_FEED.getName();
        addNameProperties(contextData, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }


    /** search tab used by afa */
    public void trackStateForSearchTab() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_SEARCH_TAB.getName();
        addNameProperties(contextData, pageTypeName);
        contextData.put("s.evar3", pageTypeName);
        contextData.put("s.evar17", pageTypeName + ": Basic Search");
        contextData.put("s.prop38", pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    /** search bar used by cfa */
    public void trackStateForSearchBar() {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_SEARCH_BAR.getName();
        addNameProperties(contextData, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }


    public void trackStateForSearchResults(String term, int count, ViewType viewType) {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_SEARCH_RESULTS.getName();
        contextData.put("Searches", 1); // event1
        contextData.put("PageViews", 1); // event4
        if (count == 0) {
            contextData.put("NullSearches", 1); // event2
            term = "null:" + term;
            String shortName = pageTypeName;
            pageTypeName += ": No Search Results";
            addNameProperties(contextData, shortName, shortName, pageTypeName, pageTypeName, pageTypeName);
        } else {
            addNameProperties(contextData, pageTypeName);
        }
        contextData.put("s.evar1", term);
        contextData.put("s.prop1", term);
        contextData.put("s.prop2", count);
        contextData.put("sort", "Best Match"); // s.prop54
        contextData.put("s.prop53", viewType.getName());
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForClass(int count, Browse browse, ViewType viewType) {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_CLASS.getName();
        contextData.put("PageViews", 1); // event4
        contextData.put("s.prop2", count);
        contextData.put("s.prop3", pageTypeName);
        contextData.put("s.prop53", viewType.getName());
        String pageName = pageTypeName;
        if (browse != null && browse.getCategory() != null && browse.getCategory().size() > 0) {
            List<Analytic> analytics = browse.getCategory().get(0).getCategoryAnalytic();
            if (analytics != null && analytics.size() > 0) {
                Analytic analytic = analytics.get(0);
                if (analytic != null) {
                    StringBuilder categoryBuf = new StringBuilder();
                    if (!TextUtils.isEmpty(analytic.getSuperCategoryName())) {
                        categoryBuf.append(analytic.getSuperCategoryName());
                        contextData.put("Channel", categoryBuf.toString());
                    }
                    if (!TextUtils.isEmpty(analytic.getCategoryName())) {
                        categoryBuf.append(":").append(analytic.getCategoryName());
                        contextData.put("s.prop4", categoryBuf.toString());
                    }
                    if (!TextUtils.isEmpty(analytic.getDepartmentName())) {
                        categoryBuf.append(":").append(analytic.getDepartmentName());
                        contextData.put("s.prop5", categoryBuf.toString());
                    }
                    if (!TextUtils.isEmpty(analytic.getClassName())) {
                        categoryBuf.append(":").append(analytic.getClassName());
                        contextData.put("s.prop6", categoryBuf.toString());
                    }
                    addCategoryCodeHierarchies(contextData, analytic);
                    pageName = categoryBuf.toString(); // overwrite pagename if hierarchy available
                }
            }
        }
        Analytics.trackState(pageName, contextData);
    }

    public void trackStateForProduct(Product product) {
        if (product != null) {
            String pageTypeName = PageType.PAGE_PRODUCT_DETAIL.getName();
            HashMap<String, Object> contextData = createContextWithGlobal();
            contextData.put("ProductView", 1); // prodView event
            contextData.put("PageViews", 1); // event4
            if (!product.isInStock()) {
                contextData.put("OutofStock", 1); // event78
            }
            contextData.put("s.products", product.getSku());
            String longName = pageTypeName + ": Details";
            addNameProperties(contextData, pageTypeName, pageTypeName, longName, longName, longName);
            contextData.put("s.evar27", product.getCustomerReviewRating());
            if (product.getAnalytic() != null && product.getAnalytic().size() > 0) {
                Analytic analytic = product.getAnalytic().get(0);
                if (analytic != null) {
                    if (!TextUtils.isEmpty(analytic.getSuperCategoryName())) {
                        pageTypeName += ": " + analytic.getSuperCategoryName();
                    }
                    addCategoryCodeHierarchies(contextData, analytic);
                }
            }
            Analytics.trackState(pageTypeName, contextData);
        }
    }

    public void trackStateForSkuSet(Product product) {
        if (product != null) {
            String pageTypeName = PageType.PAGE_SKU_SET.getName();
            HashMap<String, Object> contextData = createContextWithGlobal();
            contextData.put("PageViews", 1); // event4
            contextData.put("skuset", 1); // event16
            String shortName = PageType.PAGE_PRODUCT_DETAIL.getName();
            String longName = PageType.PAGE_PRODUCT_DETAIL.getName() + ": " + pageTypeName;
            addNameProperties(contextData, shortName, shortName, longName, longName, longName);
            if (product.getAnalytic() != null && product.getAnalytic().size() > 0) {
                Analytic analytic = product.getAnalytic().get(0);
                if (analytic != null) {
                    if (!TextUtils.isEmpty(analytic.getSuperCategoryName())) {
                        pageTypeName += ": " + analytic.getSuperCategoryName();
                    }
                    addCategoryCodeHierarchies(contextData, analytic);
                }
            }
            Analytics.trackState(pageTypeName, contextData);
        }
    }

    public void trackStateForCart(Cart cart) {
        if (cart != null && cart.getProduct() != null) {
            StringBuilder skus = new StringBuilder();
            for (com.staples.mobile.common.access.easyopen.model.cart.Product product : cart.getProduct()) {
                skus.append(";").append(product.getSku());
            }
            String pageTypeName = PageType.PAGE_CART.getName();
            HashMap<String, Object> contextData = createContextWithGlobal();
            contextData.put("CartViews", 1); // scView event
            contextData.put("s.products", skus.toString());
            addNameProperties(contextData, pageTypeName);
            Analytics.trackState(pageTypeName, contextData);
        }
    }

    public void trackStateForCartCoupons() {
        String pageTypeName = PageType.PAGE_CART.getName();
        HashMap<String, Object> contextData = createContextWithGlobal();
        String shortName = pageTypeName;
        pageTypeName += ": Coupons";
        addNameProperties(contextData, shortName, "Shopping Cart Coupons", pageTypeName,
                pageTypeName, pageTypeName);
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForCheckoutReviewAndPay(boolean shippingAddrPrefilled, boolean paymentPrefilled) {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_CHECKOUT.getName();
        String shortName = pageTypeName;
        pageTypeName += ": " + PageType.PAGE_CHECKOUT_REVIEW_AND_PAY.getName();
        addNameProperties(contextData, shortName, PageType.PAGE_CHECKOUT_REVIEW_AND_PAY.getName(),
                          pageTypeName, pageTypeName, pageTypeName);
        contextData.put("PageViews", 1); // event4
        if (shippingAddrPrefilled) {
            contextData.put("enteraddresses", 1); // event6
        }
        if (paymentPrefilled) {
            contextData.put("paymentmethod", 1); // event7
        }
        Analytics.trackState(pageTypeName, contextData);
    }

    public void trackStateForOrderConfirmation(String orderNumber) {
        HashMap<String, Object> contextData = createContextWithGlobal();
        String pageTypeName = PageType.PAGE_CHECKOUT.getName();
        String shortName = pageTypeName;
        pageTypeName += ": Confirmation";
        addNameProperties(contextData, shortName, "Checkout Confirmation",
                pageTypeName, pageTypeName, pageTypeName);
        contextData.put("PageViews", 1); // event4
        contextData.put("Purchase", 1); // purchase event
        contextData.put("purchaseID", orderNumber);
        Analytics.trackState(pageTypeName, contextData);
    }


    ///////////////////////////////////////////////////////////
    ////////////// trackAction calls //////////////////////////
    ///////////////////////////////////////////////////////////


    public void trackActionForNavigationDrawer(String drawerItemText, String currentPageName) {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("Item Click", "Nav Drawer");
        contextData.put("Click", 1);
        contextData.put("s.prop27", drawerItemText + "|" + currentPageName);
        Analytics.trackAction("Nav Drawer", contextData);
    }

    // e.g. ShopCategory:<SC>:<CG>:<DP>:<CL>
    public void trackActionForShopByCategory(String categoryHierarchy) {
        if (!TextUtils.isEmpty(categoryHierarchy)) {
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("s.prop27", "ShopCategory:" + categoryHierarchy);
            Analytics.trackAction("ShopCategory Drilldown", contextData);
        }
    }


    public void trackActionForPersonalizedMessaging(String personalizedMsg) {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("Item Click", personalizedMsg);
        contextData.put("Click", 1);
        Analytics.trackAction("Homepage Personalized", contextData);
    }

    public void trackActionForHomePage(String bannerName) {
        String pageTypeName = PageType.PAGE_INTERNAL_CAMPAIGNS.getName();
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("Item Click", "Homepage Banner");
        contextData.put("Click", 1);
        contextData.put("s.evar4", bannerName);
        contextData.put("s.evar3", pageTypeName);
        contextData.put("s.evar17", pageTypeName + ":" + PageType.PAGE_HOME.getName());
        contextData.put("s.prop38", pageTypeName);
        Analytics.trackAction("Homepage Banner", contextData);
    }

    public void trackActionForSearch(SearchType searchType) {
        String pageTypeName = PageType.PAGE_SEARCH.getName();
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("s.evar3", pageTypeName);
        contextData.put("s.prop38", pageTypeName);
        String searchTypeString = null;
        switch (searchType) {
            case BASIC_SEARCH: searchTypeString = "Basic Search"; break;
            case AUTOCOMPLETE: searchTypeString = "Autocomplete"; break;
            case RECENT_SEARCH: searchTypeString = "Recent Searches"; break;
        }
        contextData.put("s.evar17", pageTypeName + ":" + searchTypeString);
        Analytics.trackAction("Search Initiated", contextData);
    }


    public void trackActionForSearchItemSelection(int itemPosition, int pageNo) {
        trackActionForItemSelection(PageType.PAGE_SEARCH_RESULTS, itemPosition, pageNo);
    }
    public void trackActionForClassItemSelection(int itemPosition, int pageNo) {
        trackActionForItemSelection(PageType.PAGE_CLASS, itemPosition, pageNo);
    }
    private void trackActionForItemSelection(PageType pageType, int itemPosition, int pageNo) {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("s.pageName", pageType.getName());
        contextData.put("s.prop3", pageType.getName());
        contextData.put("s.evar19", (itemPosition+1) + ":" + pageNo+1);
        Analytics.trackAction("(Slot Location", contextData);
    }

    public void trackActionForProductTabs(String tabName, Product product) {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("s.pageName", "Product Detail:"); //  start with at least this much in case analytic empty
        contextData.put("s.prop11", tabName);
        contextData.put("s.prop48", product.getSku());
        if (product.getAnalytic() != null && product.getAnalytic().size() > 0) {
            Analytic analytic = product.getAnalytic().get(0);
            if (analytic != null) {
                if (!TextUtils.isEmpty(analytic.getSuperCategoryName())) {
                    contextData.put("s.pageName", "Product Detail: " + analytic.getSuperCategoryName());
                }
            }
        }
        Analytics.trackAction("Product Details Tabs", contextData);
    }

    public void trackActionForAddToCartFromProductDetails(String sku, float price, int quantity) {
        trackActionForAddToCart(PageType.PAGE_PRODUCT_DETAIL, sku, price, quantity);
    }
    public void trackActionForAddToCartFromSearchResults(String sku, float price, int quantity) {
        trackActionForAddToCart(PageType.PAGE_SEARCH_RESULTS, sku, price, quantity);
    }
    public void trackActionForAddToCartFromClass(String sku, float price, int quantity) {
        trackActionForAddToCart(PageType.PAGE_CLASS, sku, price, quantity);
    }
    public void trackActionForAddToCart(PageType pageType, String sku, float price, int quantity) {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        float totalPrice = price;
        if (quantity != 1) { // only doing the extra math operations if quantity not 1
            totalPrice = Math.round(price * quantity * 100f) / 100f;
        }
        contextData.put("s.pageName", pageType.getName());
        contextData.put("cartadds", 1); // scAdd event
        contextData.put("cartopens", 1); // scOpen event
        // format for "s.products" is ";<sku no>;;;event35=<Total Price>|event36=<Total No of Units>"
        contextData.put("s.products", ";"+sku+";;;event35="+totalPrice+"|event36="+quantity);
        contextData.put("s.evar12", pageType.getName());
        Analytics.trackAction("Cart Addition", contextData);
    }

    public void trackActionForUpdateQtyFromCart(String sku, int qty) {
        String pageTypeName = PageType.PAGE_CART.getName();
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("s.pageName", pageTypeName);
        contextData.put("s.products", ";"+sku);
        contextData.put("Item Click", qty);
        contextData.put("Click", 1);
        Analytics.trackAction("Cart Update", contextData);
    }

    public void trackActionForRemoveFromCart(String sku) {
        String pageTypeName = PageType.PAGE_CART.getName();
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("s.pageName", pageTypeName);
        contextData.put("cartremoves", 1); // scRemove event
        contextData.put("s.products", ";"+sku);
        Analytics.trackAction("Cart Removal", contextData);
    }

    public void trackActionForCheckoutEnterAddress() {
        String pageTypeName = PageType.PAGE_CHECKOUT.getName() + ": " + PageType.PAGE_CHECKOUT_REVIEW_AND_PAY.getName();
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("s.pageName", pageTypeName);
        contextData.put("enteraddresses", 1); // event6
        Analytics.trackAction("Checkout:Enter Address", contextData);
    }

    public void trackActionForCheckoutEnterPayment() {
        String pageTypeName = PageType.PAGE_CHECKOUT.getName() + ": " + PageType.PAGE_CHECKOUT_REVIEW_AND_PAY.getName();
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("s.pageName", pageTypeName);
        contextData.put("paymentmethod", 1); // event7
        Analytics.trackAction("Checkout:Payment Details", contextData);
    }

    public void trackActionForCheckoutFormErrors(String errMsg) {
        String pageTypeName = PageType.PAGE_CHECKOUT.getName() + ": " + PageType.PAGE_CHECKOUT_REVIEW_AND_PAY.getName();
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("s.pageName", pageTypeName);
        contextData.put("s.prop10", errMsg);
        Analytics.trackAction("Checkout Form Error", contextData);
    }


    ///////////////////////////////////////////////////////////
    ////////////// trackLocation call /////////////////////////
    ///////////////////////////////////////////////////////////

    public void trackLocation(Location location) {
        Analytics.trackLocation(location, null);
    }



    //////////////////////////////////////////////////////////
    ////////////// private calls /////////////////////////////
    //////////////////////////////////////////////////////////

    private HashMap<String, Object> createContextWithGlobal() {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.putAll(globalContextData);
        return contextData;
    }

    // short cut version for cases where name is the same across the board
    private void addNameProperties(HashMap<String, Object> contextData, String name) {
        addNameProperties(contextData, name, name, name, name, name);
    }

    private void addNameProperties(HashMap<String, Object> contextData, String channel,
                                   String prop3, String prop4, String prop5, String prop6) {
        contextData.put("Channel", channel);
        contextData.put("s.prop3", prop3);
        contextData.put("s.prop4", prop4);
        contextData.put("s.prop5", prop5);
        contextData.put("s.prop6", prop6);
    }

    private void addCategoryCodeHierarchies(HashMap<String, Object> contextData, Analytic analytic) {
        if (analytic != null) {
            String categoryCodeHierarchy = buildCategoryCodeHierarchy(analytic);
            if (!TextUtils.isEmpty(categoryCodeHierarchy)) {
                contextData.put("s.prop31", categoryCodeHierarchy);
                contextData.put("s.evar38", categoryCodeHierarchy);
            }
        }
    }

//    private String buildCategoryHierarchy(Analytic analytic) {
//        StringBuilder buf = new StringBuilder();
//        if (analytic != null) {
//            if (analytic.getSuperCategoryName() != null) {
//                buf.append(analytic.getSuperCategoryName());
//            }
//            if (analytic.getCategoryName() != null) {
//                buf.append(":").append(analytic.getCategoryName());
//            }
//            if (analytic.getDepartmentName() != null) {
//                buf.append(":").append(analytic.getDepartmentName());
//            }
//            if (analytic.getClassName() != null) {
//                buf.append(":").append(analytic.getClassName());
//            }
//        }
//        return buf.toString();
//    }

    private String buildCategoryCodeHierarchy(Analytic analytic) {
        StringBuilder buf = new StringBuilder();
        if (analytic != null) {
            if (analytic.getSuperCategoryCode() != null) {
                buf.append(analytic.getSuperCategoryCode());
            }
            if (analytic.getCategoryCode() != null) {
                buf.append(":").append(analytic.getCategoryCode());
            }
            if (analytic.getDepartmentCode() != null) {
                buf.append(":").append(analytic.getDepartmentCode());
            }
            if (analytic.getClassCode() != null) {
                buf.append(":").append(analytic.getClassCode());
            }
        }
        return buf.toString();
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