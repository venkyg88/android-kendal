
package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.staples.mobile.common.access.easyopen.model.ApiError;

import java.util.ArrayList;
import java.util.List;

//wrapper class for add and update api responses

public class CartUpdate {

    // include this so that ((CartContents)retrofitError.getBody()).getErrors()
    // can be examined in 400 Bad Request failure response (e.g. in debugger)
    private List<ApiError> errors;
    public List<ApiError> getErrors() { return errors; }
    public void setErrors(List<ApiError> errors) { this.errors = errors; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message; // this field is not documented, but is included in some cases (e.g. out of stock items)

    private List<ItemsAdded> itemsAdded = new ArrayList<ItemsAdded>();

    public List<ItemsAdded> getItemsAdded() {
        return itemsAdded;
    }

    public void setItemsAdded(List<ItemsAdded> itemsAdded) {
        this.itemsAdded = itemsAdded;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
