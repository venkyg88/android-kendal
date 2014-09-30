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
import com.staples.mobile.common.access.lms.model.FormFactor;
import com.staples.mobile.common.access.lms.model.Item;
import com.staples.mobile.common.access.lms.model.Lms;
import com.staples.mobile.common.access.lms.api.LmsApi;
import com.staples.mobile.common.access.lms.model.Page;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LmsAdapter extends PagerAdapter implements Callback<Lms>, AdapterView.OnItemClickListener {
    private static final String TAG = "LmsAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private MainActivity activity;
    private LayoutInflater inflater;
    private ArrayList<LmsItem> array;

    public LmsAdapter(MainActivity activity) {
        super();
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList();
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
        MainApplication application = (MainApplication) activity.getApplication();
        LmsApi lmsApi = application.getLmsApi();
        lmsApi.lms(RECOMMENDATION, STORE_ID, this);
    }

    @Override
    public void success(Lms lms, Response response) {
        Page page = lms.getPage().get(0);
        FormFactor formFactor = page.getFormFactor();
        List<Item> items = formFactor.getItem();
        for (Item item : items) {
            array.add(new LmsItem(item.getTitle(), item.getBanner(), item.getBundleId()));
        }
        notifyDataSetChanged();
        activity.showMainScreen();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        activity.showMainScreen();
    }

    // LMS product item clicks

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ProductItem product = (ProductItem) parent.getItemAtPosition(position);
        activity.selectSkuItem(product.identifier);
    }
}
