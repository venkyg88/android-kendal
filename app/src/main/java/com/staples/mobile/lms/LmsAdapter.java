package com.staples.mobile.lms;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.EasyOpenApi;
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

/**
 * Created by pyhre001 on 9/16/14.
 */
public class LmsAdapter extends PagerAdapter  implements Callback<Lms> {
    private static final String TAG = "LmsAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private Activity activity;
    private ArrayList<String> array;

    public LmsAdapter(Activity activity) {
        super();
        this.activity = activity;
        array = new ArrayList();
    }

    @Override
    public int getCount() {
        return(array.size());
    }

    public void add(String title) {
        array.add(title);
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return(null);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    public boolean	 isViewFromObject(View view, Object object) {
        return(true);
    }

    public String getPageTitle(int position) {
        return(array.get(position));
    }

    public void fill() {
        MainApplication application = (MainApplication) activity.getApplication();
        EasyOpenApi easyOpenApi = application.getMockEasyOpenApi();
        easyOpenApi.lms(RECOMMENDATION, STORE_ID, this);
    }

    public void success(Lms lms, Response response) {
        Page page = lms.getPage().get(0);
        FormFactor formFactor = page.getFormFactor();
        List<Item> items = formFactor.getItem();
        for(Item item : items) {
            add(item.getTitle());
        }
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
    }

}
