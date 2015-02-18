package com.staples.mobile.cfa.analytics;

import java.util.HashMap;

/**
 * Created by burcoral on 2/5/15.
 */


//See why this type of singleton--> http://howtodoinjava.com/2012/10/22/singleton-design-pattern-in-java/

public class Tracker {
    private static volatile Tracker instance = null;

    public static HashMap<String, Object> globalContextData;

    // private constructor
    private Tracker() {
    }


    public static Tracker getInstance(AppType appType) {
        if (instance == null) {
            synchronized (Tracker.class) {
                // Double check
                if (instance == null) {
                    instance = new Tracker();
                    switch(appType) {
                        case AFA:
                            setAFAGlobalDefinitions();
                        case CFA:
                            setCFAGlobalDefinitions();
                    }
                }
            }
        }
        return instance;
    }

    private HashMap<String, Object>  initialize() {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.putAll(globalContextData);
        return contextData;
    }
    public HashMap<String, Object>  getContext4Search() {
        HashMap<String, Object> contextData = initialize();
        contextData.put("s.pageName", "Search on the Action Bar");
        contextData.put("s.evar3", "Search");
        contextData.put("s.evar17", "Search: Basic Search");
        contextData.put("s.prop3", "Search");
        contextData.put("s.prop38", "Search");
        return contextData;
    }
    public HashMap<String, Object>  getContext4SearchResults(String term, int count) {
        HashMap<String, Object> contextData = initialize();
        contextData.put("s.pageName", "Search Results");
        contextData.put("Channel", "Search Results");
        contextData.put("s.evar1", term);
        contextData.put("s.prop1", term);
        contextData.put("s.prop2", count);
        contextData.put("s.prop3", "Search ");
        contextData.put("s.prop4", "Search Results");
        contextData.put("s.prop5", "Search Results");
        contextData.put("s.prop6", "Search Results");
        return contextData;
    }
    public HashMap<String, Object>  getContext4Product(String sku, String detail, String superCategory, String category, String department, String productClass, float rating) {
        HashMap<String, Object> contextData = initialize();
        contextData.put("s.pageName", "Product Screen");
        contextData.put("Channel", superCategory);
        contextData.put("s.products", sku);
        contextData.put("s.prop3", detail);
        contextData.put("s.prop4", category);
        contextData.put("s.prop5", department);
        contextData.put("s.prop6", productClass);
        contextData.put("s.evar27", rating);
        return contextData;
    }

    private static void setAFAGlobalDefinitions() {
        globalContextData = new HashMap<String, Object>();
        globalContextData.put("s.evar73", "Associate App");
        globalContextData.put("s.evar18", getAssociateId());
        globalContextData.put("s.evar75", getStoreId());
        globalContextData.put("s.evar51", getZipCode());
    }
    private static void setCFAGlobalDefinitions() {
       globalContextData = new HashMap<String, Object>();
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