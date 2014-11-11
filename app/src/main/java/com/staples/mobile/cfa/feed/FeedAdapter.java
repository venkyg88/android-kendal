package com.staples.mobile.cfa.feed;

import android.app.Activity;

import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;

public class FeedAdapter {

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
//    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private Activity activity;

    private EasyOpenApi easyOpenApi;

    public FeedAdapter(Activity activity) {
        super();
        this.activity = activity;
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
    }

    public FeedAdapter()
    {
    }

    public void fill() {

    }


}