package com.staples.mobile.lms;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.staples.mobile.EasyOpenApi;
import com.staples.mobile.MainApplication;
import com.staples.mobile.R;
import com.staples.mobile.lms.object.FormFactor;
import com.staples.mobile.lms.object.Item;
import com.staples.mobile.lms.object.Lms;
import com.staples.mobile.lms.object.Page;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class LmsAdapter extends PagerAdapter
                        implements retrofit.Callback<Lms>, com.squareup.picasso.Callback {
    private static final String TAG = "LmsAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private class Sheet {
        String title;
        String bannerUrl;
        String identifier;
        View view;

        Sheet(String title, String bannerUrl, String identifier) {
            this.title = title;
            this.bannerUrl = bannerUrl;
            this.identifier = identifier;
        }
    }

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<Sheet> array;
    private Picasso picasso;

    public LmsAdapter(Activity activity) {
        super();
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList();
        picasso = Picasso.with(activity);
    }

    @Override
    public int getCount() {
        return (array.size());
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Sheet sheet = array.get(position);
        sheet.view = inflater.inflate(R.layout.lms_page, container, false);

        // Load banner image
        ImageView banner = (ImageView) sheet.view.findViewById(R.id.banner);
        RequestCreator requestCreator = picasso.load(sheet.bannerUrl);
        requestCreator.into(banner, this);
        requestCreator.fit();

        // Set adapter
        ProductAdapter adapter = new ProductAdapter(activity);
        ListView list = (ListView) sheet.view.findViewById(R.id.products);
        list.setAdapter(adapter);
        adapter.fill(sheet.identifier);

        container.addView(sheet.view);
        return (sheet);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Sheet sheet = array.get(position);
        container.removeView(sheet.view);
        sheet.view = null;
    }

    public boolean isViewFromObject(View view, Object object) {
        return (view==((Sheet) object).view);
    }

    public String getPageTitle(int position) {
        return (array.get(position).title);
    }

    // Retrofit

    public void fill() {
        MainApplication application = (MainApplication) activity.getApplication();
        EasyOpenApi easyOpenApi = application.getMockEasyOpenApi();
        easyOpenApi.lms(RECOMMENDATION, STORE_ID, this);
    }

    public void success(Lms lms, Response response) {
        Page page = lms.getPage().get(0);
        FormFactor formFactor = page.getFormFactor();
        List<Item> items = formFactor.getItem();
        for (Item item : items) {
            array.add(new Sheet(item.getTitle(), item.getBanner(), item.getBundleId()));
        }
        notifyDataSetChanged();
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
    }

    // Picasso

    public void onSuccess() {
    }

    public void onError() {
    }
}
