/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model;

import java.util.List;

/**
 * Created by sutdi001 on 10/31/14.
 */
public interface SupportsApiErrors {
    public List<ApiError> getErrors();
}
