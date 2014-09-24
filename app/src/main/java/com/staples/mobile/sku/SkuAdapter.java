package com.staples.mobile.sku;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.staples.mobile.MainApplication;
import com.staples.mobile.easyopen.EasyOpenApi;
import com.staples.mobile.lms.FormFactor;
import com.staples.mobile.lms.Item;
import com.staples.mobile.lms.Lms;
import com.staples.mobile.lms.LmsApi;
import com.staples.mobile.lms.Page;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuAdapter extends PagerAdapter implements Callback<Lms> {
    private static final String TAG = "SkuAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<SkuItem> array;

    public SkuAdapter(Activity activity) {
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
        SkuItem item = array.get(position);

//        container.addView(item.whatever);
        return (item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        SkuItem item = array.get(position);
        container.removeView(item.view);

    }

    public boolean isViewFromObject(View view, Object object) {
        return (view==((SkuItem) object).view);
    }

    public String getPageTitle(int position) {
        return (array.get(position).title);
    }

    // Retrofit EasyOpen API call

    public void fill() {
        array.add(new SkuItem("Summary"));
        array.add(new SkuItem("Specifications"));
        array.add(new SkuItem("Reviews"));
        notifyDataSetChanged();

        MainApplication application = (MainApplication) activity.getApplication();
        EasyOpenApi easyOpenApi = application.getEasyOpenApi();
//        easyOpenApi.whatever(RECOMMENDATION, STORE_ID, this);
    }

    public void success(Lms lms, Response response) {
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
    }
}
