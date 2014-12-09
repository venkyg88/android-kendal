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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalFeedFragment extends BaseFragment
        implements ProductCollection.ProductCollectionCallBack{
    private static final String TAG = "PersonalFeedFragment";

    private ListView seenProductsListView;
    private SeenProductsAdapter seenProductsListAdapter;

    public static final String DAILY_DEAL_IDENTIFIER = "BI739472";
    public static final String CLEARANCE_IDENTIFIER = "BI642994";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LinearLayout personalFeedLayout = (LinearLayout) inflater.inflate(R.layout.personal_feed, container, false);
        seenProductsListView = (ListView) personalFeedLayout.findViewById(R.id.product_list);

        setAdapter();
        setLinstener();

        Map collectionMap = new HashMap<String, String>();

        String url = "category/identifier/";
        ProductCollection.getProducts(url + DAILY_DEAL_IDENTIFIER,
                "50", // limit (may be null)
                "1", // offset (may be null)
                collectionMap, // currently not used
                this); // ProductCollection CallBack

        ProductCollection.getProducts(url + CLEARANCE_IDENTIFIER,
                "50", // limit (may be null)
                "1", // offset (may be null)
                collectionMap, // currently not used
                this); // ProductCollection CallBack


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

    @Override
    public void onProductCollectionResult(ProductCollection.ProductContainer productContainer,
                                          List<ProductCollection.ProductContainer.ERROR_CODES> errorCodes) {
        String identifier = productContainer.getIdentifier();
        Log.v(TAG, "identifier: " + identifier);

    }
}