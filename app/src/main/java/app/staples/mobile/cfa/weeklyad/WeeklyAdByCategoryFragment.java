package app.staples.mobile.cfa.weeklyad;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.model.store.Obj;
import com.staples.mobile.common.access.channel.model.store.StoreData;
import com.staples.mobile.common.access.channel.model.store.StoreQuery;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Collection;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Content;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyad.WeeklyAd;
import com.staples.mobile.common.analytics.Tracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.location.LocationFinder;
import app.staples.mobile.cfa.widget.ActionBar;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WeeklyAdByCategoryFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = WeeklyAdByCategoryFragment.class.getSimpleName();

    private static final String ARG_STORENO = "storeNo";
    private static final String ARG_STOREID = "storeId";

    private static final String DEFAULT_STORE_NO = "0349";

    private String storeId; // special store id required for weekly ad service
    private String storeNo; // storeNo available via store finder
    private String city;
    private String address;

    private TextView storeInfoVw;
    private TextView dateRangeVw;
    private WeeklyAdByCategoryAdapter adapter;
    private List<Data> weeklyAdItems;

    public void setArguments(String storeNo, String storeId /*, String city, String address*/) {
        Bundle args = new Bundle();
        args.putString(ARG_STORENO, storeNo);
        args.putString(ARG_STOREID, storeId);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Crittercism.leaveBreadcrumb("WeeklyAdByCategoryFragment:onCreateView(): Displaying the Weekly Ad by Category screen.");
        MainActivity activity = (MainActivity)getActivity();

        Bundle args = getArguments();
        if (args != null) {  // note that there will likely be a title arg even when no storeNo, so storeNo may still be null when args is not
            storeNo = args.getString(ARG_STORENO);
            storeId = args.getString(ARG_STOREID);
        }

        View view = inflater.inflate(R.layout.weekly_ad_category, container, false);
        view.setTag(this);

        storeInfoVw = (TextView) view.findViewById(R.id.store_address);
        dateRangeVw = (TextView) view.findViewById(R.id.date_range);
        RecyclerView list = (RecyclerView) view.findViewById(R.id.weekly_ad_categories_list);
        list.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(activity);
        list.setLayoutManager(manager);
        adapter = new WeeklyAdByCategoryAdapter(activity);
        list.setAdapter(adapter);

        view.findViewById(R.id.change_store).setOnClickListener(this);

        if(!TextUtils.isEmpty(storeId)){
            getWeeklyAdData();
        } else{
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
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;

        activity.showProgressIndicator();
        final EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdStore(Integer.parseInt(storeNo), new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAdStore, Response response) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity==null) return;

                if (weeklyAdStore!=null) {
                    Content content = weeklyAdStore.getContent();
                    if (content != null) {
                        Collection collection = content.getCollection();
                        if (collection!=null) {
                            List<Data> datas = collection.getData();
                            if (datas != null && datas.size() > 0) {
                                Data data = datas.get(0);
                                storeId = String.valueOf(data.getStoreid());
                                address = data.getAddress1();
                                city = data.getCity();
                                storeInfoVw.setText(address + "\n" + city);
                                getWeeklyAdData();
                                getWeeklyAdDates();
                                return;
                            }
                        }
                    }
                }

                activity.hideProgressIndicator();
                activity.showErrorDialog(R.string.empty);
            }

            @Override
            public void failure(RetrofitError error) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity==null) return;

                activity.hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }

    private void getWeeklyAdDates() {
        // don't bother with progress indicator for this less important call made in parallel,
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdPromotions(storeId, new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAdPromo, Response response) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity==null) return;

                if (weeklyAdPromo!=null) {
                    Content content = weeklyAdPromo.getContent();
                    if (content != null) {
                        Collection collection = content.getCollection();
                        List<Data> datas = collection.getData();
                        if (datas != null && datas.size() > 0) {
                            Data data = datas.get(0);
                            // example: "3/21/2015 12:00:00 AM",
                            SimpleDateFormat parserFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.ENGLISH);
                            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d", Locale.ENGLISH);
                            try {
                                Date startDate = parserFormat.parse(data.getSalestartdate());
                                Date endDate = parserFormat.parse(data.getSaleenddate());
                                dateRangeVw.setText(displayFormat.format(startDate) + " - " + displayFormat.format(endDate));
                            } catch(ParseException e) {
                                Log.d(TAG, e.getMessage());
                                Crittercism.logHandledException(e);
                            }
                        }
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
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;

        activity.showProgressIndicator();
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdByCategories(storeId, new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAdCategories, Response response) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity==null) return;

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
                MainActivity activity = (MainActivity) getActivity();
                if (activity==null) return;

                activity.hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }

    private class StoreInfoCallback implements Callback<StoreQuery> {
        @Override
        public void success(StoreQuery storeQuery, Response response) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity==null) return;

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
            MainActivity activity = (MainActivity) getActivity();
            if (activity==null) return;

            activity.hideProgressIndicator();
            activity.showErrorDialog(ApiError.getErrorMessage(retrofitError));
        }
    }

    @Override
    public void onClick(View view) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;

        switch(view.getId()) {
            case R.id.change_store:
                activity.selectStoreFragment();
                break;
        }
    }
}
