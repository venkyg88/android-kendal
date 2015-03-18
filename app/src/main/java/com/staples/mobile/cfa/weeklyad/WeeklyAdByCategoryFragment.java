package com.staples.mobile.cfa.weeklyad;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.location.LocationFinder;
import com.staples.mobile.common.access.channel.model.store.Obj;
import com.staples.mobile.common.access.channel.model.store.StoreAddress;
import com.staples.mobile.common.access.channel.model.store.StoreData;
import com.staples.mobile.common.access.channel.model.store.StoreQuery;
import com.staples.mobile.common.analytics.Tracker;
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

    private static final String STORENO = "storeNo";
    private static final String CITY = "city";
    private static final String ADDRESS = "address";

    private static final String DEFAULT_STORE_NO = "0349";
    private static final String DEFAULT_STORE_CITY = "Framingham";

    private String storeNo;
    private String city;
    private String address;
    private MainActivity activity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView storeInfoVw;
    private TextView dateRangeVw;
    private WeeklyAdByCategoryAdapter adapter;
    private List<Data> weeklyAdItems;


    public void setArguments(String storeNo, String city, String address) {
        Bundle args = new Bundle();
        args.putString(STORENO, storeNo);
        args.putString(CITY, city);
        args.putString(ADDRESS, address);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity)getActivity();

        Bundle args = getArguments();
        if (args != null) {
            storeNo = args.getString(STORENO);
            city = args.getString(CITY);
            address = args.getString(ADDRESS);
        }

        View view = inflater.inflate(R.layout.weekly_ad_category, container, false);
        storeInfoVw = (TextView) view.findViewById(R.id.store_address);
        dateRangeVw = (TextView) view.findViewById(R.id.date_range);
        TextView changeStoreVw = (TextView) view.findViewById(R.id.change_store);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.weekly_ad_categories_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new WeeklyAdByCategoryAdapter(activity);
        mRecyclerView.setAdapter(adapter);

        changeStoreVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.selectStoreFinder();
            }
        });





        //TODO
        dateRangeVw.setText("Oct 0 - Oct 0");






        // if store info avail
        if (!TextUtils.isEmpty(storeNo)) {
            storeInfoVw.setText(address + "\n" + city);
            getWeeklyAdData();
        } else {
            // otherwise get store info from postal code
            LocationFinder finder = LocationFinder.getInstance(activity);
            String postalCode = finder.getPostalCode();
            if (!TextUtils.isEmpty(postalCode)) {
                activity.showProgressIndicator();
                Access.getInstance().getChannelApi(false).storeLocations(postalCode, new StoreInfoCallback());
            } else {
                useDefaultStore();
                getWeeklyAdData();
            }
        }

        return view;
    }

    private void getWeeklyAdData(){
        activity.showProgressIndicator();
        final EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdByCategories(storeNo, new Callback<WeeklyAdCategories>() {
            @Override
            public void success(WeeklyAdCategories weeklyAdCategories, Response response) {
                activity.hideProgressIndicator();
                if (weeklyAdCategories.getContent().getCollection() != null) {
                    weeklyAdItems = weeklyAdCategories.getContent().getCollection().getData();
                    adapter.fill(weeklyAdItems, storeNo);
                } else {
                    activity.showErrorDialog(R.string.empty);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                activity.hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }

    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD);
        Tracker.getInstance().trackStateForWeeklyAdClass(); // Analytics
    }


    private void useDefaultStore() {
        storeNo = DEFAULT_STORE_NO;
        city = DEFAULT_STORE_CITY;
        storeInfoVw.setText(address + "\n" + city);
    }

    private class StoreInfoCallback implements Callback<StoreQuery> {
        @Override
        public void success(StoreQuery storeQuery, Response response) {
            List<StoreData> storeData = storeQuery.getStoreData();
            // if there are any nearby stores
            if (storeData != null && !storeData.isEmpty()) {

                // Get store location
                Obj storeObj = storeData.get(0).getObj();
                StoreAddress storeAddress = storeObj.getStoreAddress();
                storeNo = storeObj.getStoreNumber();
                city = storeAddress.getCity();
                address = storeAddress.getAddressLine1();
                storeInfoVw.setText(address + "\n" + city);
            }
            // use default if no store result
            else {
                useDefaultStore();
            }

            getWeeklyAdData();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            activity.hideProgressIndicator();
            activity.showErrorDialog(ApiError.getErrorMessage(retrofitError));
        }
    }

}
