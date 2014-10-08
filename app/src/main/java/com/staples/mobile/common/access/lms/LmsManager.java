/**
 * Created by DowSt001 on 10/06/14.
 */

package com.staples.mobile.common.access.lms;

import android.util.Log;

import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.common.access.lms.api.LmsApi;
import com.staples.mobile.common.access.lms.model.Lms;
import com.staples.mobile.common.access.lms.model.Screen;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.RetrofitError;

public class LmsManager implements Callback<Lms> {

    // Class Variables

    private static final String TAG = "LmsManager";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final ReentrantReadWriteLock lmsReadWriteLock =
        new ReentrantReadWriteLock();

    private static final Lock lmsReadLock = lmsReadWriteLock.readLock();
    private static final Lock lmsWriteLock = lmsReadWriteLock.writeLock();

    private static Lms lms;

    // Interface Definitions

    public interface LmsMgrCallback {

        public void onGetLmsResult(boolean success);
    }

    // Instance Variables

    private LmsApi lmsApi;

    private LmsMgrCallback lmsManagerCallback;

    // Constructors and Methods

    public LmsManager() {

        lmsApi = MainApplication.application.getLmsApi();
    }

    public void getLms(LmsMgrCallback lmsManagerCallback,
                       boolean conditional) {

        this.lmsManagerCallback = lmsManagerCallback;

        while (true) {

            if (conditional) {

                lmsReadLock.lock();

                if (lms != null) {

                    lmsReadLock.unlock();

                    if (lmsManagerCallback != null) lmsManagerCallback.onGetLmsResult(true);
                    break; // while (true)
                }
                lmsReadLock.unlock();
            }

            getLmsWithLock();

            break; // while (true)

        } // while (true)
    }

    public List<Screen> getScreen() {

        Log.v(TAG, "LmsManager:getScreen():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        List<Screen> screens = null;

        lmsReadLock.lock();

        if (lms != null) {
            screens = lms.getScreen();
        }
        lmsReadLock.unlock();

        return (screens);
    }

    @Override
    public void success(Lms lms, Response response) {

        Log.v(TAG, "LmsManager:retrofit.Callback.success():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        this.lms = lms;

        lmsWriteLock.unlock();

        if (lmsManagerCallback != null) lmsManagerCallback.onGetLmsResult(true);
    }

    @Override
    public void failure(RetrofitError retrofitError) {

        Log.e(TAG, "LmsManager:retrofit.Callback.failure():"
            + " retrofitError[" + retrofitError + "]"
            + " this[" + this + "]"
        );

        lmsWriteLock.unlock();

        if (lmsManagerCallback != null) lmsManagerCallback.onGetLmsResult(false);
    }

    private void getLmsWithLock() {

        Log.v(TAG, "LmsManager:getLmsWithLock():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        lmsWriteLock.lock();

        lmsApi.lms(RECOMMENDATION, STORE_ID, this);

        return;
    }
}
