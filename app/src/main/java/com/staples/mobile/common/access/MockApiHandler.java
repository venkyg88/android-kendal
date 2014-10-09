package com.staples.mobile.common.access;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.staples.mobile.common.access.lms.model.Lms;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import retrofit.Callback;

public class MockApiHandler implements InvocationHandler {
    private static final String TAG = "MockApiHandler";

    private Context context;
    private ObjectMapper mapper;
    private Handler handler;

    public MockApiHandler(Context context) {
        this.context = context;
        mapper = new ObjectMapper();
//        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        handler = new Handler(Looper.getMainLooper());
    }

    private <T> T loadJsonObjects(String filename, Class<T> responseClass, final Callback<T> callback) {
        try {
            InputStream stream = context.getResources().getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            final T objects = mapper.readValue(reader, responseClass);
            reader.close();

            Runnable runs = new Runnable() {public void run() {callback.success(objects, null);}};
            handler.post(runs);
            return(objects);
        } catch(JsonParseException e) {
            Log.e(TAG, "Could not parse " + filename + " (" + e + ")");
        } catch(Exception e) {
            Log.e(TAG, "Could not load " + filename + " (" + e + ")");
        }
        Runnable runs = new Runnable() {public void run() {callback.failure(null);}};
        handler.post(runs);
        return(null);
    }

    private Callback<?> getCallback(Object[] args) {
        if (args==null) return(null);
        int argn = args.length;
        if (argn<1) return(null);
        Object callback = args[argn-1];
        if (!(callback instanceof Callback<?>)) return(null);
        return((Callback<?>) callback);
    }

    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) {
        Log.d(TAG, "Invoke " + method.toString());

        // Get method name
        String name = method.getName();

        // Get callback
        Callback<?> callback =  getCallback(args);
        if (callback==null) throw(new RuntimeException("Invoke can not find callback"));

        // Handle main API requests
        if (name.equals("lms")) {
            Object objects = loadJsonObjects("lms_v6.json", Lms.class, (Callback<Lms>) callback);
            return (objects);
        }

        // Unhandled
        throw(new RuntimeException("Unhandled mock API call"));
    }
}