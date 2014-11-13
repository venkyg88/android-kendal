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
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.DataWrapper;

public class PersonalFeedFragment extends Fragment {
    private static final String TAG = "PersonalFeedFragment";

    private ListView seenProductsListView;
    private SeenProductsAdapter seenProductsListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        LinearLayout personalFeedLayout = (LinearLayout) inflater.inflate(R.layout.personal_feed, container, false);
        seenProductsListView = (ListView) personalFeedLayout.findViewById(R.id.product_list);

        setAdapter();
        setLinstener();

        return (personalFeedLayout);
    }

    private void setAdapter(){
        seenProductsListAdapter = new SeenProductsAdapter(getActivity());
//        SeenProductsRowItem(String productName,
//                            String currentPrice,
//                            String reviewAmount,
//                            String rating,
//                            String sku,
//                            String unitOfMeasure,
//                            String imageUrl)
        SeenProductsRowItem item = new SeenProductsRowItem("Product", "99", "100", "2", "223456",
                "Each", "http://www.staples-3p.com/s7/is/image/Staples/s0795727_sc7");
        SeenProductsRowItem item2 = new SeenProductsRowItem("Product Product Product Product Product Product", "99", "100", "2", "122374",
                "Each", "http://www.staples-3p.com/s7/is/image/Staples/s0795727_sc7");
        SeenProductsRowItem item3 = new SeenProductsRowItem("Product Product Product asdf asdf Product Product Product Product Product fdasdf Product Product Product Product Product", "99", "100", "2", "123456",
                "Each", "http://www.staples-3p.com/s7/is/image/Staples/s0795727_sc7");
        seenProductsListAdapter.add(item);
        seenProductsListAdapter.add(item2);
        seenProductsListAdapter.add(item3);

        seenProductsListView.setAdapter(seenProductsListAdapter);
    }

    private void setLinstener(){
        // Set listener on seen product list item
        seenProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SeenProductsRowItem item = (SeenProductsRowItem) parent.getItemAtPosition(position);
                String sku = item.getSku();
                ((MainActivity) getActivity()).selectSkuItem(sku);
            }
        });
    }
}