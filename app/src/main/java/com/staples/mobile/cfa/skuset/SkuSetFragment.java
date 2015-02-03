package com.staples.mobile.cfa.skuset;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuSetFragment extends Fragment  implements Callback<SkuDetails>, View.OnClickListener {
    private static final String TAG = "SkuSetFragment";

    private static final String TITLE = "title";
    private static final String IDENTIFIER = "identifier";
    private static final String IMAGEURL = "imageUrl";

    private static final int MAXFETCH = 50;

    private DataWrapper wrapper;
    private SkuSetAdapter adapter;
    private String title;

    public void setArguments(String title, String identifier, String imageUrl) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(IDENTIFIER, identifier);
        args.putString(IMAGEURL, imageUrl);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String identifier = null;
        String imageUrl = null;

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE);
            identifier = args.getString(IDENTIFIER);
            imageUrl = args.getString(IMAGEURL);
        }

        // Initialize content
        View frame = inflater.inflate(R.layout.skuset_frame, container, false);
        wrapper = (DataWrapper) frame.findViewById(R.id.wrapper);
        RecyclerView list = (RecyclerView) wrapper.findViewById(R.id.list);
        adapter = new SkuSetAdapter(getActivity());
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter.setOnClickListener(this);

//        // Set main image
//        ImageView image = (ImageView) frame.findViewById(R.id.image);
//        Drawable noPhoto = getActivity().getResources().getDrawable(R.drawable.no_photo);
//        if (imageUrl == null) image.setImageDrawable(noPhoto);
//        else Picasso.with(getActivity()).load(imageUrl).error(noPhoto).into(image);

        // Start item query
        wrapper.setState(DataWrapper.State.LOADING);
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getSkuSummary(identifier, null, MAXFETCH, this);

        return(frame);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.SKUSET);
    }

    @Override
    public void success(SkuDetails details, Response response) {
        Activity activity = getActivity();
        if (activity==null) return;

        int count = processDetails(details);
        if (count==0) wrapper.setState(DataWrapper.State.EMPTY);
        else wrapper.setState(DataWrapper.State.DONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity==null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        ((MainActivity) activity).showErrorDialog(msg);
        wrapper.setState(DataWrapper.State.EMPTY);
        Log.d(TAG, msg);
    }

    private int processDetails(SkuDetails details) {
        if (details==null) return(0);
        List<Product> products = details.getProduct();
        if (products==null || products.size()<1) return(0);
        int count = adapter.fill(products.get(0).getProduct());
        return(count);
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag instanceof SkuSetAdapter.Item) {
            SkuSetAdapter.Item item = (SkuSetAdapter.Item) tag;
            ((MainActivity) getActivity()).selectSkuItem(item.title, item.identifier);
        }
    }
}
