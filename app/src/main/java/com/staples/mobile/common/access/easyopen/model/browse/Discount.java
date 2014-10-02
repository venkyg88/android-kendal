package com.staples.mobile.common.access.easyopen.model.browse;

public class Discount {
    private float amount;
    private String currency;
    private boolean deductFromListPrice;
    private String name;

    public float getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isDeductFromListPrice() {
        return deductFromListPrice;
    }

    public String getName() {
        return name;
    }
}
