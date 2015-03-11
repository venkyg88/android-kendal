package com.staples.mobile.cfa.weeklyad;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

    private String storeId = "2278338"; // TODO This needs to be implemented
    private MainActivity activity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView storeInfoVw;
    private TextView dateRangeVw;
    private WeeklyAdByCategoryAdapter adapter;
    private List<Data> weeklyAdItems;

    public WeeklyAdByCategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.weekly_ad_category, container, false);
        storeInfoVw = (TextView) view.findViewById(R.id.store_address);
        dateRangeVw = (TextView) view.findViewById(R.id.date_range);
        TextView changeStoreVw = (TextView) view.findViewById(R.id.change_store);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.weekly_ad_categories_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getWeeklyAdData();
        adapter = new WeeklyAdByCategoryAdapter(activity, storeId);
        mRecyclerView.setAdapter(adapter);

        changeStoreVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: temporary
                Toast.makeText(activity, "Not implemented", Toast.LENGTH_LONG).show();
            }
        });

        // TODO: temporary
        storeInfoVw.setText("Store address line 1\nStore address line 2");
        dateRangeVw.setText("Oct 0 - Oct 0");
        Toast.makeText(activity, "\n\nAlert!!!\n\n\n\nHard-coding store to: " + storeId + "\n\n\n\n", Toast.LENGTH_LONG).show();

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