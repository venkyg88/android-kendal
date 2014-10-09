package com.staples.mobile.cfa.sku;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.lms.model.Lms;

import java.util.ArrayList;

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

        item.view = new TextView(activity); // TODO Hacked
        ((TextView) item.view).setText(item.identifier);
        ((TextView) item.view).setTextSize(20.0f);

        container.addView(item.view);
        return (item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        SkuItem item = array.get(position);
        container.removeView(item.view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==((SkuItem) object).view);
    }

    @Override
    public String getPageTitle(int position) {
        return (array.get(position).title);
    }

    // Retrofit EasyOpen API call

    public void fill(String identifier) {
        array.add(new SkuItem("Summary"));
        array.add(new SkuItem("Specifications"));
        array.add(new SkuItem("Reviews"));
        array.add(new SkuItem("Accessories"));
        notifyDataSetChanged();

        array.get(0).identifier = identifier;

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
//        easyOpenApi.whatever(RECOMMENDATION, STORE_ID, this);
    }

    @Override
    public void success(Lms lms, Response response) {
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
    }
}
