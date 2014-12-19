package com.staples.mobile.cfa.bundle;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BundleFragment extends Fragment implements Callback<Browse>, View.OnClickListener {
    private static final String TAG = "BundleFragment";

    private static final String TITLE = "title";
    private static final String IDENTIFIER = "identifier";

    private static final int MAXFETCH = 50;

    private DataWrapper wrapper;
    private BundleAdapter adapter;
    private String title;

    public void setArguments(String title, String identifier) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(IDENTIFIER, identifier);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String identifier = null;

        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            title = args.getString(TITLE);
            identifier = args.getString(IDENTIFIER);
        }

        wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        RecyclerView products = (RecyclerView) view.findViewById(R.id.products);
        adapter = new BundleAdapter(getActivity());
        products.setAdapter(adapter);
        products.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        products.addItemDecoration(new HorizontalDivider(getActivity()));
        adapter.setOnClickListener(this);

        wrapper.setState(DataWrapper.State.LOADING);
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.browseCategories(identifier, null, MAXFETCH, this);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity!=null) activity.showActionBar(title, R.drawable.ic_search_white, null);
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
        wrapper.setState(DataWrapper.State.EMPTY);
        Log.d(TAG, msg);
    }

    private int processBrowse(Browse browse) {
        if (browse==null) return(0);
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) return(0);
        Category category = categories.get(0);
        if (category==null) return(0);

        // Add straight products
        int count = adapter.fill(category.getProduct());

        // Add promos (in bundle)
        List<Category> promos = category.getPromoCategory();
        if (promos!=null) {
            for(Category promo : promos)
                count += adapter.fill(promo.getProduct());
        }
        return(count);
    }

    @Override
    public void onClick(View view) {
        Object tag;
        switch(view.getId()) {
            case R.id.bundle_item:
                tag = view.getTag();
                if (tag instanceof BundleItem) {
                    String identifier = ((BundleItem) tag).identifier;
                    ((MainActivity) getActivity()).selectSkuItem(identifier);
                }
                break;
            case R.id.action:
                tag = view.getTag();
                if (tag instanceof BundleItem) {
                    String title = ((BundleItem) tag).title;
                    Toast.makeText(getActivity(), "Clicked on "+title, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
