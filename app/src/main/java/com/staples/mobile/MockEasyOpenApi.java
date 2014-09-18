package com.staples.mobile;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.staples.mobile.browse.object.Browse;
import com.staples.mobile.landing.object.Landing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import retrofit.Callback;

/**
 * Created by pyhre001 on 9/17/14.
 */
public class MockEasyOpenApi implements EasyOpenApi {
    private static final String TAG = "MockEasyOpenApi";

    private static Gson gson = new Gson();

    private Context context;
    private Handler handler;

    public MockEasyOpenApi(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
    }

    private <T> T loadObjects(String filename, Class<T> responseClass, final Callback<T> callback) {
        try {
            InputStream stream = context.getResources().getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            final T objects = gson.fromJson(reader, responseClass);
            Runnable runs = new Runnable() {public void run(){callback.success(objects, null);}};
            handler.post(runs);
            return(objects);
        } catch(JsonSyntaxException e) {
            Log.e(TAG, "Could not parse " + filename + " (" + e + ")");
        } catch(Exception e) {
            Log.e(TAG, "Could not load " + filename + " (" + e + ")");
        }
        Runnable runs = new Runnable() {public void run(){callback.failure(null);}};
        handler.post(runs);
        return(null);
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
    ) {
        throw new RuntimeException("Mock API call not implemented");
    }

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
    ) {
        throw new RuntimeException("Mock API call not implemented");
    }

    public void landing(
         String version,
         String storeId,
         final Callback<Landing> callback
    ) {
        loadObjects("landing.json", Landing.class, callback);
    }
}
