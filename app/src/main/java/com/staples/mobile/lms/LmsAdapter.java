package com.staples.mobile.lms;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.staples.mobile.LmsApi;
import com.staples.mobile.MainApplication;
import com.staples.mobile.lms.object.FormFactor;
import com.staples.mobile.lms.object.Item;
import com.staples.mobile.lms.object.Lms;
import com.staples.mobile.lms.object.Page;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LmsAdapter extends PagerAdapter implements Callback<Lms> {
    private static final String TAG = "LmsAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<LmsItem> array;

    public LmsAdapter(Activity activity) {
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

        container.addView(item.list);
        return (item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        LmsItem sheet = array.get(position);
        container.removeView(sheet.list);
        sheet.list = null;
    }

    public boolean isViewFromObject(View view, Object object) {
        return (view==((LmsItem) object).list);
    }

    public String getPageTitle(int position) {
        return (array.get(position).title);
    }

    // Retrofit LMS API call

    public void fill() {
        MainApplication application = (MainApplication) activity.getApplication();
        LmsApi lmsApi = application.getMockLmsApi();
        lmsApi.lms(RECOMMENDATION, STORE_ID, this);
    }

    public void success(Lms lms, Response response) {
        Page page = lms.getPage().get(0);
        FormFactor formFactor = page.getFormFactor();
        List<Item> items = formFactor.getItem();
        for (Item item : items) {
            array.add(new LmsItem(item.getTitle(), item.getBanner(), item.getBundleId()));
        }
        notifyDataSetChanged();
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
    }
}
