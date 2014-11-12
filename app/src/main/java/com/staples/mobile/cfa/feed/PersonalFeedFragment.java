package com.staples.mobile.cfa.feed;

/**
 * Author: Yongnan Zhou
 */

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.DataWrapper;

public class PersonalFeedFragment extends Fragment {
    private static final String TAG = "PersonalFeedFragment";

    private SeenProductsAdapter seenProductsAdapter;
    private DataWrapper wrapper;
    private View view;
    ListView seenProductsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        LinearLayout personalFeedLayout = (LinearLayout) inflater.inflate(R.layout.personal_feed, container, false);
        seenProductsListView = (ListView) personalFeedLayout.findViewById(R.id.product_list);
        setAdapter();

        return (personalFeedLayout);
    }

    private void setAdapter(){
        seenProductsAdapter = new SeenProductsAdapter(getActivity(), wrapper);

//        SeenProductsRowItem(String productName,
//                            String currentPrice,
//                            String reviewAmount,
//                            String rating,
//                            String sku,
//                            String unitOfMeasure,
//                            String imageUrl)
        SeenProductsRowItem item = new SeenProductsRowItem("Product", "99", "100", "2", "123456",
                "Each", "http://www.staples-3p.com/s7/is/image/Staples/s0795727_sc7");
        SeenProductsRowItem item2 = new SeenProductsRowItem("Product Product Product Product Product Product", "99", "100", "2", "123456",
                "Each", "http://www.staples-3p.com/s7/is/image/Staples/s0795727_sc7");
        SeenProductsRowItem item3 = new SeenProductsRowItem("Product Product Product Product Product Product", "99", "100", "2", "123456",
                "Each", "http://www.staples-3p.com/s7/is/image/Staples/s0795727_sc7");
        SeenProductsRowItem item4 = new SeenProductsRowItem("Product Product Product Product Product Product", "99", "100", "2", "123456",
                "Each", "http://www.staples-3p.com/s7/is/image/Staples/s0795727_sc7");
        seenProductsAdapter.add(item);
        seenProductsAdapter.add(item2);
        seenProductsAdapter.add(item3);
        seenProductsAdapter.add(item4);
        seenProductsListView.setAdapter(seenProductsAdapter);
//        seenProductsListView.setOnItemClickListener(this);
    }
}