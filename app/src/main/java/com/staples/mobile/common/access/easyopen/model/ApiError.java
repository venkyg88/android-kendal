/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model;

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
}
