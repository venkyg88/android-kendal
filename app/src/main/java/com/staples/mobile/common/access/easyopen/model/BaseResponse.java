/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model;


import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by sutdi001 on 11/7/14.
 */
public class BaseResponse {

    // include this so that ((BaseResponse)retrofitError.getBody()).getErrors()
    // can be examined in 400 Bad Request and 500 Internal Server Error failure responses
    private List<ApiError> errors;

    public List<ApiError> getErrors() {
        return errors;
    }

    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }

}

