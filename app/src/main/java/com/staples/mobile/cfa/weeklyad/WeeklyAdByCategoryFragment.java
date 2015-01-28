package com.staples.mobile.cfa.weeklyad;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.weeklyadbycategory.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyadbycategory.WeeklyAdCategories;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WeeklyAdByCategoryFragment extends Fragment {

    String storeId = "2278338";
    MainActivity activity;
    ListView listView;
    WeeklyAdByCategoryAdapter adapter;
    public WeeklyAdByCategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.weekly_ad_by_category, container, false);
        listView = (ListView) view.findViewById(R.id.weekly_ad_categories_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Data data = adapter.getItem(position);
                WeeklyAdListFragment weeklyAdListFragment = new WeeklyAdListFragment();
                weeklyAdListFragment.setCategoryTreeId(data.getCategorytreeid());
                weeklyAdListFragment.setStoreId(storeId);
                activity.selectFragment(weeklyAdListFragment, MainActivity.Transition.FADE, true);
            }
        });
        adapter = new WeeklyAdByCategoryAdapter(activity);
        listView.setAdapter(adapter);

        getWeeklyAdData();
        return view;
    }

    private void getWeeklyAdData(){
        activity.showProgressIndicator();
        final EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdByCategories(storeId, new Callback<WeeklyAdCategories>() {
            @Override
            public void success(WeeklyAdCategories weeklyAdCategories, Response response) {

                activity.hideProgressIndicator();

                List<Data> data = weeklyAdCategories.getContent().getCollection().getData();
                if (data == null) {
                    //TODO: Display error
                } else {
                    adapter.addAll(data);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                activity.hideProgressIndicator();
            }
        });
    }

    public void onResume() {
        super.onResume();
        if (activity!=null) {
//            activity.showActionBar(R.string.weekly_ad_title, 0, null);
        }
    }
}
