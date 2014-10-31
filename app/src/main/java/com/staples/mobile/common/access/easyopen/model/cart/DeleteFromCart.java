package com.staples.mobile.common.access.easyopen.model.cart;


import com.staples.mobile.common.access.easyopen.model.ApiError;

import java.util.List;

//placeholder class for delete from cart responses. Since a successful deletion is
//supposed to give an empty response, this has been left blank.
public class DeleteFromCart {

    // include this so that ((CartContents)retrofitError.getBody()).getErrors()
    // can be examined in 400 Bad Request failure response (e.g. in debugger)
    private List<ApiError> errors;
    public List<ApiError> getErrors() { return errors; }
    public void setErrors(List<ApiError> errors) { this.errors = errors; }

}
