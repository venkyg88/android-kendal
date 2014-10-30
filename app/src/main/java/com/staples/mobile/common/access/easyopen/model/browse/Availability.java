package com.staples.mobile.common.access.easyopen.model.browse;

import com.staples.mobile.R;

public enum Availability {
    NOTHING      (R.string.avail_nothing),
    SKUSET       (R.string.avail_skuset),
    RETAILONLY   (R.string.avail_retailonly),
    SPECIALORDER (R.string.avail_specialorder),
    INSTOCK      (R.string.avail_instock),
    OUTOFSTOCK   (R.string.avail_outofstock);

    private int resid;

    private Availability(int resid) {
        this.resid = resid;
    }

    public static Availability getProductAvailability(Product product) {
        if (product==null) return(NOTHING);
        if (product.getProduct()!=null) return(SKUSET);
        if (product.isRetailOnly()) return(RETAILONLY);
        if (product.isRetailOnlySpecialOrder()) return(SPECIALORDER);
        if (product.isInStock()) return(INSTOCK);
        return(OUTOFSTOCK);
    }

    public int getTextResId() {
        return(resid);
    }
}
