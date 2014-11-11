/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by sutdi001 on 10/30/14.
 *
 * Extend BaseResponse to allow retrieval of error messages from within the retrofit failure callback
 * method. BaseResponse includes the following to allow a place for error info within the api response
 * object.
 *
 *     private List<ApiError> errors;
 *     public List<ApiError> getErrors() { return errors; }
 *     public void setErrors(List<ApiError> errors) { this.errors = errors; }
 *
 * Call ApiError.getApiError(retrofitError) or ApiError.getErrorMessage(retrofitError) to retrieve
 * error info from the retrofitError parameter of the failure callback.
 *
 * The EasyOpenApi often includes helful error messages when returning a 400 Bad Request or
 * 500 Internal Server Error. Drawing these from the retrofitError object saves the step of having
 * to reproduce the call with all its parameters and headers in an external tool such as Postman.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiError {
    String errorCode;
    String errorKey;
    String errorMessage;
    String errorParameters;

    // need to include JsonIgnoreProperties flag above because insufficient-stock error includes an
    // undocumented "items" property in the error response. for example:
    //        "items": [
    //            {
    //                "substituteItem": {
    //                    "partNumber": "803389",
    //                    "quantity": "0.0"
    //                }
    //            }
    //        ]

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


    /** convenience method for retrieving API error msg from response object when success() callback called */
    public static String getApiSuccessError(BaseResponse responseObject) {
        List<ApiError> errors = responseObject.getErrors();
        if (errors != null && errors.size() > 0) {
            ApiError apiError = errors.get(0);
            return (apiError.getErrorMessage() != null)? apiError.getErrorMessage() : apiError.getErrorKey();
        }
        return null;
    }

    /** convenience method for retrieving API error from RetrofitError error
     * (have response object extend BaseResponse) */
    public static ApiError getApiError(RetrofitError error) {
        ApiError apiError = null;
        if (error.getResponse() != null) {
            int httpErrorCode = error.getResponse().getStatus();
            if (httpErrorCode == 400 || httpErrorCode == 500) {
                if (error.getBody() instanceof BaseResponse) {
                    List<ApiError> errors = ((BaseResponse) error.getBody()).getErrors();
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
     * (have response object extend BaseResponse) */
    public static String getErrorMessage(RetrofitError error) {
        ApiError apiError = getApiError(error);
        return (apiError.getErrorMessage() != null)? apiError.getErrorMessage() : apiError.getErrorKey();
    }
}
