package com.staples.mobile.common.access.easyopen.model.login;

import com.staples.mobile.common.access.easyopen.model.ApiError;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class EmptyResponse {

    // include this so that we can capture the errors
    private List<ApiError> errors;
    public List<ApiError> getErrors() { return errors; }
    public void setErrors(List<ApiError> errors) { this.errors = errors; }
}
