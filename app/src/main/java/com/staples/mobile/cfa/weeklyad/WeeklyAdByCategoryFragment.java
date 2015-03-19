package com.staples.mobile.cfa.weeklyad;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.location.LocationFinder;
import com.staples.mobile.common.access.channel.model.store.Obj;
import com.staples.mobile.common.access.channel.model.store.StoreData;
import com.staples.mobile.common.access.channel.model.store.StoreQuery;
import com.staples.mobile.common.access.easyopen.model.weeklyadpromo.WeeklyAdPromo;
import com.staples.mobile.common.access.easyopen.model.weeklyadstore.WeeklyAdStore;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.weeklyadbycategory.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyadbycategory.WeeklyAdCategories;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */

public class WeeklyAdByCategoryFragment extends Fragment {
    private static final String TAG = WeeklyAdByCategoryFragment.class.getSimpleName();

    private static final String ARG_STORENO = "storeNo";

    private static final String DEFAULT_STORE_NO = "0349";

    private String storeId; // special store id required for weekly ad service
    private String storeNo; // storeNo available via store finder
    private String city;
    private String address;
    private MainActivity activity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView storeInfoVw;
    private TextView dateRangeVw;
    private WeeklyAdByCategoryAdapter adapter;
    private List<Data> weeklyAdItems;


    public void setArguments(String storeNo /*, String city, String address*/) {
        Bundle args = new Bundle();
        args.putString(ARG_STORENO, storeNo);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity)getActivity();

        Bundle args = getArguments();
        if (args != null) {  // note that there will likely be a title arg even when no storeNo, so storeNo may still be null when args is not
            storeNo = args.getString(ARG_STORENO);
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

        // if store info avail
        if (!TextUtils.isEmpty(storeNo)) {
            getWeeklyAdStoreAndData();
        } else {
            // otherwise get store info from postal code
            LocationFinder finder = LocationFinder.getInstance(activity);
            String postalCode = finder.getPostalCode();
            if (!TextUtils.isEmpty(postalCode)) {
                activity.showProgressIndicator();
                Access.getInstance().getChannelApi(false).storeLocations(postalCode, new StoreInfoCallback());
            } else {
                storeNo = DEFAULT_STORE_NO;
                getWeeklyAdStoreAndData();
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD);
        Tracker.getInstance().trackStateForWeeklyAdClass(); // Analytics
    }


    private void getWeeklyAdStoreAndData() {
        activity.showProgressIndicator();
        final EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdStore(Integer.parseInt(storeNo), new Callback<WeeklyAdStore>() {
            @Override
            public void success(WeeklyAdStore weeklyAdStore, Response response) {
                if (weeklyAdStore.getContent().getCollection() != null) {
                    com.staples.mobile.common.access.easyopen.model.weeklyadstore.Data data =
                            weeklyAdStore.getContent().getCollection().getData();
                    storeId = String.valueOf(data.getStoreid());
                    address = data.getAddress1();
                    city = data.getCity();
                    storeInfoVw.setText(address + "\n" + city);
                    getWeeklyAdData();
                    getWeeklyAdDates();
                } else {
                    activity.hideProgressIndicator();
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

    private void getWeeklyAdDates() {
        // don't bother with progress indicator for this less important call made in parallel,
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdPromotions(storeId, new Callback<WeeklyAdPromo>() {
            @Override
            public void success(WeeklyAdPromo weeklyAdPromo, Response response) {
                if (weeklyAdPromo.getContent().getCollection() != null) {
                    com.staples.mobile.common.access.easyopen.model.weeklyadpromo.Data data =
                            weeklyAdPromo.getContent().getCollection().getData();
                    // example: "3/21/2015 12:00:00 AM",
                    SimpleDateFormat parserFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.ENGLISH);
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d", Locale.ENGLISH);
                    try {
                        Date startDate = parserFormat.parse(data.getSalestartdate());
                        Date endDate = parserFormat.parse(data.getSaleenddate());
                        dateRangeVw.setText(displayFormat.format(startDate) + " - " + displayFormat.format(endDate));
                    } catch (ParseException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                // don't display error message, just don't display dates
                Log.d(TAG, ApiError.getErrorMessage(error));
            }
        });

    }

    private void getWeeklyAdData(){
        activity.showProgressIndicator();
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdByCategories(storeId, new Callback<WeeklyAdCategories>() {
            @Override
            public void success(WeeklyAdCategories weeklyAdCategories, Response response) {
                activity.hideProgressIndicator();
                if (weeklyAdCategories.getContent().getCollection() != null) {
                    weeklyAdItems = weeklyAdCategories.getContent().getCollection().getData();
                    adapter.fill(weeklyAdItems, storeId);
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


    private class StoreInfoCallback implements Callback<StoreQuery> {
        @Override
        public void success(StoreQuery storeQuery, Response response) {
            List<StoreData> storeData = storeQuery.getStoreData();
            // if there are any nearby stores
            if (storeData != null && !storeData.isEmpty()) {

                // Get store location
                Obj storeObj = storeData.get(0).getObj();
                storeNo = storeObj.getStoreNumber();
            }
            // use default if no store result
            else {
                storeNo = DEFAULT_STORE_NO;
            }

            getWeeklyAdStoreAndData();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            activity.hideProgressIndicator();
            activity.showErrorDialog(ApiError.getErrorMessage(retrofitError));
        }
    }

}
