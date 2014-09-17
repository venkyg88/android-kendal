package com.staples.mobile;

import android.app.Activity;
import android.os.Looper;

import com.google.gson.Gson;
import com.staples.mobile.browse.object.Browse;
import com.staples.mobile.landing.object.Landing;

import retrofit.Callback;

/**
 * Created by pyhre001 on 9/17/14.
 */
public class MockEasyOpenApi implements EasyOpenApi {
    private static Gson gson = new Gson();

    private Activity activity;

    public MockEasyOpenApi(Activity activity) { // TODO Ugly hack
        this.activity = activity;
    }

    public void topCategories(
        String version,
        String storeId,
        String catalogId,
        String locale,
        String parentIdentifier,
        String zipCode,
        String client_id,
        Integer offset,
        Integer limit,
        Callback<Browse> callback
    ) {}

    public void browseCategories(
        String version,
        String storeId,
        String path,
        String catalogId,
        String locale,
        String zipCode,
        String client_id,
        Integer offset,
        Integer limit,
        Callback<Browse> callback
    ) {}

    public void landing(
         String version,
         String storeId,
         final Callback<Landing> callback
    ) {
        String json = activity.getResources().getString(R.string.landing_json);
        final Landing landing = gson.fromJson(json, Landing.class);
        Runnable runs = new Runnable() {public void run(){callback.success(landing, null);}};
        activity.runOnUiThread(runs);
    }
}
