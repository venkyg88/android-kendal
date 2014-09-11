package com.staples.mobile.browse.object;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class Browse {
    @JsonProperty("Category")
    private Category[] Category;

    public Category[] getCategory() {
        return Category;
    }
}
