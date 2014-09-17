package com.staples.mobile.landing;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.EasyOpenApi;
import com.staples.mobile.MainApplication;
import com.staples.mobile.PagerAdapter;
import com.staples.mobile.R;
import com.staples.mobile.landing.object.Landing;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class LandingFragment extends Fragment implements Callback<Landing> {
    private static final String TAG = "LandingFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private ViewPager pager;
    private PagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.landing, container, false);

        FragmentManager manager = getFragmentManager();
//        FragmentManager manager = getChildFragmentManager(); TODO requires API 17
        adapter = new PagerAdapter(manager);
        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        EasyOpenApi easyOpenApi = ((MainApplication) getActivity().getApplication()).getMockEasyOpenApi(getActivity());
        easyOpenApi.landing(RECOMMENDATION, STORE_ID, this);

        return(view);
    }

    public void success(Landing landing, Response response) {
        Log.d(TAG, "Success callback "+landing.getColumn()[0].getName());
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback");
    }
}
