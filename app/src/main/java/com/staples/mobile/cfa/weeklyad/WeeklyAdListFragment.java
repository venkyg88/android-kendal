package com.staples.mobile.cfa.weeklyad;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyad.WeeklyAd;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */

public class WeeklyAdListFragment extends Fragment{

    private static final String STOREID = "storeid";
    private static final String CATEGORYTREEID = "categoryTreeId";
    private static final String TITLE = "title";

    MainActivity activity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    WeeklyAdListAdapter adapter;
    String storeId;
    String categoryTreeId;
    String title;
    EasyOpenApi easyOpenApi;
    List<Data> weeklyAdItems;


    public void setArguments(String storeid, String categoryTreeId, String title) {
        Bundle args = new Bundle();
        args.putString(STOREID, storeid);
        args.putString(CATEGORYTREEID, categoryTreeId);
        args.putString(TITLE, title);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.weekly_ad_list, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            storeId = args.getString(STOREID);
            categoryTreeId = args.getString(CATEGORYTREEID);
            title = args.getString(TITLE);
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.weekly_ad_list_items);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.addItemDecoration(new HorizontalDivider(getActivity()));
        getWeeklyAdListing();
        adapter = new WeeklyAdListAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD, title);
    }

    public void getWeeklyAdListing() {
        activity.showProgressIndicator();
        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdCategoryListing(storeId, categoryTreeId, new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAd, Response response) {
                activity.hideProgressIndicator();
                weeklyAdItems = weeklyAd.getContent().getCollection().getData();
                adapter.fill(weeklyAdItems);
            }

            @Override
            public void failure(RetrofitError error) {
                activity.hideProgressIndicator();
                ((MainActivity)activity).showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }
}