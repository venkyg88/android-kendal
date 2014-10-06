package com.staples.mobile.cfa.home;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.common.access.lms.LmsManager;
import com.staples.mobile.common.access.lms.model.Item;
import com.staples.mobile.common.access.lms.model.Lms;
import com.staples.mobile.common.access.lms.api.LmsApi;
import com.staples.mobile.common.access.lms.model.Screen;

import java.util.ArrayList;
import java.util.List;

import com.staples.mobile.common.access.lms.LmsManager.LmsMgrCallback;

/* @@@ STUBBED
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
@@@ STUBBED */

public class LmsAdapter
    extends PagerAdapter
    implements LmsMgrCallback,
               AdapterView.OnItemClickListener {

    private static final String TAG = "LmsAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private MainActivity activity;
    private LayoutInflater inflater;
    private ArrayList<LmsItem> array;
    private LmsManager lmsManager;

    public LmsAdapter(MainActivity activity) {
        super();
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList();
        lmsManager = new LmsManager();
    }

    @Override
    public int getCount() {
        return (array.size());
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LmsItem item = array.get(position);
        item.list = new ListView(activity);

        // Set adapter
        ProductAdapter adapter = new ProductAdapter(activity, item);
        item.list.setAdapter(adapter);
        adapter.fill();
        item.list.setOnItemClickListener(this);

        container.addView(item.list);
        return (item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        LmsItem item = array.get(position);
        container.removeView(item.list);
        item.list = null;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==((LmsItem) object).list);
    }

    @Override
    public String getPageTitle(int position) {
        return (array.get(position).title);
    }

    // Retrofit LMS API call

    public void fill() {

        lmsManager.getLms(this,  // LmsMgrCallback
                          true); // conditional
        /* @@@ STUBBED
        MainApplication application = (MainApplication) activity.getApplication();
        LmsApi lmsApi = application.getLmsApi();
        lmsApi.lms(RECOMMENDATION, STORE_ID, this);
        @@@ STUBBED */
    }

    /* @@@ STUBBED
    @Override
    public void success(Lms lms, Response response) {
        Screen screen = lms.getScreen().get(0);
        List<Item> items = screen.getItem();
        for (Item item : items) {
            array.add(new LmsItem(item.getTitle(), item.getBanner(), null));
        }
        notifyDataSetChanged();
        activity.showMainScreen();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        activity.showMainScreen();
    }
    @@@ STUBBED */

    @Override
    public void onGetLmsResult(boolean success) {

        Log.v(TAG, "LmsAdapter:LmsManager.onGetLmsResult():"
                + " success[" + success + "]"
                + " this[" + this + "]"
        );

        if (success) {

            Screen screen = lmsManager.getScreen();
            List<Item> items = screen.getItem();
            for (Item item : items) {
                array.add(new LmsItem(item.getTitle(), item.getBanner(), null));
            }
            notifyDataSetChanged();
            activity.showMainScreen();

        } else {

            activity.showMainScreen();
        }
    }

    // LMS product item clicks

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ProductItem product = (ProductItem) parent.getItemAtPosition(position);
        activity.selectSkuItem(product.identifier);
    }
}
