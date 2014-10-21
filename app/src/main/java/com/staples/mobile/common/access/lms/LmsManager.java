package com.staples.mobile.common.access.lms;

import android.util.Log;

import com.staples.mobile.common.access.Access;
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

        Log.v(TAG, "LmsManager:LmsManager():"
                + " this[" + this + "]"
        );

        lmsApi = Access.getInstance().getLmsApi();
    }

    public void getLms(LmsMgrCallback lmsManagerCallback,
                       boolean conditional) {

        Log.v(TAG, "LmsManager:getLms():"
                + " lms[" + lms + "]"
                + " conditional[" + conditional + "]"
                + " this[" + this + "]"
        );

        this.lmsManagerCallback = lmsManagerCallback;

        while (true) {

            if (lms == null) {

                getLmsWithLock();

                break; // while (true)
            }

            if (conditional) {

                lockRead();

                if (lmsManagerCallback != null) lmsManagerCallback.onGetLmsResult(true);

                unlockRead();

                break; // while (true)
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

        lockRead();

        if (lms != null) {
            screens = lms.getScreen();
        }
        unlockRead();

        return (screens);
    }

    @Override
    public void success(Lms lms, Response response) {

        Log.v(TAG, "LmsManager:retrofit.Callback.success():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        this.lms = lms;

        unlockWrite();

        if (lmsManagerCallback != null) lmsManagerCallback.onGetLmsResult(true);
    }

    @Override
    public void failure(RetrofitError retrofitError) {

        Log.e(TAG, "LmsManager:retrofit.Callback.failure():"
            + " retrofitError[" + retrofitError + "]"
            + " this[" + this + "]"
        );

        unlockWrite();

        if (lmsManagerCallback != null) lmsManagerCallback.onGetLmsResult(false);
    }

    private void getLmsWithLock() {

        Log.v(TAG, "LmsManager:getLmsWithLock():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        lockWrite();

        lmsApi.lms(RECOMMENDATION, STORE_ID, this);

        return;
    }

    private void lockRead() {

        Log.v(TAG, "LmsManager:lockRead():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        lmsReadLock.lock();

        return;
    }

    private void unlockRead() {

        Log.v(TAG, "LmsManager:unlockRead():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        lmsReadLock.unlock();

        return;
    }

    private void lockWrite() {

        Log.v(TAG, "LmsManager:lockWrite():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        lmsWriteLock.lock();

        return;
    }

    private void unlockWrite() {

        Log.v(TAG, "LmsManager:unlockWrite():"
                + " lms[" + lms + "]"
                + " this[" + this + "]"
        );

        lmsWriteLock.unlock();

        return;
    }
}
