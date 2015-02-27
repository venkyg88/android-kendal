package com.staples.mobile.cfa.weeklyad;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.weeklyadbycategory.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyadbycategory.WeeklyAdCategories;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */

public class WeeklyAdByCategoryFragment extends Fragment {

    String storeId = "2278338"; // TODO This needs to be implemented
    MainActivity activity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    WeeklyAdByCategoryAdapter adapter;
    List<Data> weeklyAdItems;

    public WeeklyAdByCategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.weekly_ad_category, container, false);
        ImageView weeklyAdImage = (ImageView) view.findViewById(R.id.weeklyad_image);

        Picasso.with(activity)
                .load(R.drawable.weekly_ad_image)
                .fit()
                .into(weeklyAdImage);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.weekly_ad_categories_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        getWeeklyAdData();
        adapter = new WeeklyAdByCategoryAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);
        return view;
    }

    private void getWeeklyAdData(){
        activity.showProgressIndicator();
        final EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdByCategories(storeId, new Callback<WeeklyAdCategories>() {
            @Override
            public void success(WeeklyAdCategories weeklyAdCategories, Response response) {
                activity.hideProgressIndicator();
                weeklyAdItems = weeklyAdCategories.getContent().getCollection().getData();
                adapter.fill(weeklyAdItems);
            }

            @Override
            public void failure(RetrofitError error) {
                activity.showErrorDialog(ApiError.getErrorMessage(error));
                activity.hideProgressIndicator();
            }
        });
    }

    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD);
    }
}