package com.staples.mobile.cfa.bundle;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BundleFragment extends Fragment implements Callback<Browse>, AdapterView.OnItemClickListener {
    private static final String TAG = "BundleFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";
    private static final String ZIPCODE = "01010";
//    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final int MAXFETCH = 50;

    private DataWrapper wrapper;
    private GridView products;
    private BundleAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String path = null;

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            path = args.getString("path");
        }

        wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        products = (GridView) view.findViewById(R.id.products);
        adapter = new BundleAdapter(getActivity());
        products.setAdapter(adapter);
        products.setOnItemClickListener(this);

        fill(path);

        return (view);
    }

    private void fill(String path) {
        int i, j;

        wrapper.setState(DataWrapper.State.LOADING);
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        // Decode category alphanumeric identifiers
        i = path.indexOf("/category/identifier/");
        if (i >= 0) {
            i += "/category/identifier/".length();
            j = path.indexOf('?', i);
            if (j <= 0) j = path.length();
            String identifier = path.substring(i, j);
            easyOpenApi.browseCategories(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                                         ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // No idea what the path is
        Log.d(TAG, "Unknown path: " + path);
        wrapper.setState(DataWrapper.State.EMPTY);
    }

    @Override
    public void success(Browse browse, Response response) {
        Activity activity = getActivity();
        if (activity==null) return;

        int count = processBrowse(browse);
        if (count==0) wrapper.setState(DataWrapper.State.EMPTY);
        else wrapper.setState(DataWrapper.State.DONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity==null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        Log.d(TAG, msg);
    }

    private int processBrowse(Browse browse) {
        if (browse==null) return(0);
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) return(0);
        Category category = categories.get(0);
        if (category==null) return(0);
        int count = adapter.fill(category.getProduct());
        return(count);
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        BundleItem item = (BundleItem) parent.getItemAtPosition(position);
        if (item==null || item.identifier==null) {
            return;
        }
        ((MainActivity) getActivity()).selectSkuItem(item.identifier);
    }
}