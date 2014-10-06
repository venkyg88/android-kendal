package com.staples.mobile.common.access.feed.model;

/**
 * Created by Avinash Dodda
 */

public class ErrorDetail {

    private String errorCode;

    private String errorKey;

    private String errorMessage;

    private String errorParameters;

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
