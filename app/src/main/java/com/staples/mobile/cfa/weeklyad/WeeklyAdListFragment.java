package com.staples.mobile.cfa.weeklyad;


import android.os.Bundle;
import android.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyad.WeeklyAd;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.widget.AdapterView.OnItemClickListener;

public class WeeklyAdListFragment extends Fragment implements View.OnClickListener {

    MainActivity activity;
    ListView listView;
    WeeklyAdListAdapter adapter;
    String storeId;
    String categoryTreeId;

    public WeeklyAdListFragment() {
        // Required empty public constructor
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    public void setCategoryTreeId(String categoryTreeId) {
        this.categoryTreeId = categoryTreeId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.weekly_ad_by_category, container, false);
        listView = (ListView) view.findViewById(R.id.weekly_ad_categories_list);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(activity,
                        "Position :" + position, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        adapter = new WeeklyAdListAdapter(activity);
        listView.setAdapter(adapter);

        activity.showProgressIndicator();

        final EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdCategoryListing(storeId, categoryTreeId, new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAd, Response response) {
                activity.hideProgressIndicator();
                List<Data> data = weeklyAd.getContent().getCollection().getData();
                if (data == null) {
                    //TODO:display error
                } else {
                    adapter.addAll(data);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                activity.hideProgressIndicator();
                Toast.makeText(activity, "Error getting data", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD);
    }

    @Override
    public void onClick(View v) {

        Toast toast = Toast.makeText(activity.getApplicationContext(), "Still getting data", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}