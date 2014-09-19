package com.staples.mobile.lms;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.EasyOpenApi;
import com.staples.mobile.MainApplication;
import com.staples.mobile.R;
import com.staples.mobile.lms.object.FormFactor;
import com.staples.mobile.lms.object.Item;
import com.staples.mobile.lms.object.Lms;
import com.staples.mobile.lms.object.Page;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class LmsFragment extends Fragment implements Callback<Lms> {
    private static final String TAG = "LmsFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private ViewPager pager;
    private LmsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.landing, container, false);

        adapter = new LmsAdapter();
        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        MainApplication application = (MainApplication) getActivity().getApplication();
        EasyOpenApi easyOpenApi = application.getMockEasyOpenApi();
        easyOpenApi.lms(RECOMMENDATION, STORE_ID, this);

        return(view);
    }

    public void success(Lms lms, Response response) {
        Page page = lms.getPage().get(0);
        FormFactor formFactor = page.getFormFactor();
        List<Item> items = formFactor.getItem();
        for(Item item : items) {
            adapter.add(item.getTitle());
        }
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback "+ retrofitError);
    }
}
