/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by sutdi001 on 10/30/14.
 *
 * Note: by including the following in the definition of the api response object, then within
 * a 400 Bad Request failure response, retrofitError.getBody() can be cast to the response object
 * type and errors examined. This saves the step of having to reproduce the call with all its params
 * and headers in an external tool such as Postman.
 *
 *     private List<ApiError> errors;
 *     public List<ApiError> getErrors() { return errors; }
 *     public void setErrors(List<ApiError> errors) { this.errors = errors; }
 *
 * Also include "implements SupportsApiErrors" to make use of the ApiError.getErrorMessage convenience method
 */
public class ApiError {
    String errorCode;
    String errorKey;
    String errorMessage;
    String errorParameters;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorParameters() {
        return errorParameters;
    }

    public void setErrorParameters(String errorParameters) {
        this.errorParameters = errorParameters;
    }



    /** convenience method for retrieving API error from RetrofitError error
     * (have response object implement SupportsApiErrors) */
    public static ApiError getApiError(RetrofitError error) {
        ApiError apiError = null;
        if (error.getResponse() != null) {
            int httpErrorCode = error.getResponse().getStatus();
            if (httpErrorCode == 400 || httpErrorCode == 500) {
                if (error.getBody() instanceof SupportsApiErrors) {
                    List<ApiError> errors = ((SupportsApiErrors) error.getBody()).getErrors();
                    if (errors != null && errors.size() > 0) {
                        apiError = errors.get(0);
                    }
                }
            }
        }
        if (apiError == null) {
            apiError = new ApiError();
            apiError.setErrorMessage(error.getMessage());
        }
        return apiError;
    }

    /** convenience method for retrieving error message from RetrofitError error
     * (have response object implement SupportsApiErrors) */
    public static String getErrorMessage(RetrofitError error) {
        ApiError apiError = getApiError(error);
        if (apiError.getErrorMessage() != null) {
            return apiError.getErrorMessage();
        } else {
            return apiError.getErrorKey();
        }
    }
}
