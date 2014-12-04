package com.staples.mobile.cfa.feed;

/**
 * Author: Yongnan Zhou
 */

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;

public class PersonalFeedFragment extends BaseFragment {
    private static final String TAG = "PersonalFeedFragment";
    private ListView seenProductsListView;
    private SeenProductsAdapter seenProductsListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LinearLayout personalFeedLayout = (LinearLayout) inflater.inflate(R.layout.personal_feed, container, false);
        seenProductsListView = (ListView) personalFeedLayout.findViewById(R.id.product_list);

        setAdapter();
        setLinstener();

        return (personalFeedLayout);
    }

    private void setAdapter(){
        seenProductsListAdapter = new SeenProductsAdapter(getActivity());

        PersistentSizedArrayList<SeenProductsRowItem> saveSeenProducts =
                PersonalFeedSingleton.getInstance(getActivity()).getSavedSeenProducts(getActivity());

        for(SeenProductsRowItem savedSeenProduct : saveSeenProducts){
                seenProductsListAdapter.add(savedSeenProduct);
                Log.d(TAG, "Saved seen products: " + savedSeenProduct.getProduceName());
        }

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