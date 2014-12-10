package com.staples.mobile.cfa.browse;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.access.easyopen.model.browse.Description;
import com.staples.mobile.common.access.easyopen.model.browse.SubCategory;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BrowseFragment extends Fragment  implements Callback<Browse>, AdapterView.OnItemClickListener {
    private static final String TAG = "BrowseFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    //    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;

    private DataWrapper wrapper;
    private ListView list;
    private BrowseAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String path = null;

        View view = inflater.inflate(R.layout.browse_frame, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            path = args.getString("path");
        }

        wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        list = (ListView) wrapper.findViewById(R.id.products);
        adapter = new BrowseAdapter(getActivity());
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        wrapper.setState(DataWrapper.State.LOADING);
        fill(null);
        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity!=null) activity.showStandardActionBar();
    }

    void fill(String identifier) {
        int i, j;

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        // Top categories
        if (identifier==null || identifier.isEmpty()) {
            easyOpenApi.topCategories(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE, null,
                    ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // Alphanumeric identifier?
        char c = identifier.charAt(0);
        if (c>='A' && c<='Z') {
            easyOpenApi.browseCategories(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                    ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // Numeric identifier
        else {
            easyOpenApi.topCategories(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE, identifier,
                    ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }
    }

    @Override
    public void success(Browse browse, Response response) {
        List<Category> categories = browse.getCategory();
        if (categories == null || categories.size() < 1) {
            adapter.notifyDataSetChanged();
            return;
        }
        processCategories(categories);
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity == null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        wrapper.setState(DataWrapper.State.EMPTY);
        Log.d(TAG, msg);
    }

    private void processCategories(List<Category> categories) {
        // Process categories
        if (categories.size() > 1) {
            for(Category category : categories) {
                List<Description> descriptions = category.getDescription1();
                if (descriptions != null && descriptions.size() > 0) {
                    // Get category title
                    Description description = descriptions.get(0);
                    String title = description.getText();
                    if (title == null) title = description.getDescription();
                    if (title == null) title = description.getName();
                    title = Html.fromHtml(title).toString();
                    BrowseItem item = new BrowseItem();
                    item.title = title;
                    item.identifier = category.getIdentifier();
                    adapter.addItem(item);
                }
            }
            wrapper.setState(DataWrapper.State.DONE);
            adapter.notifyDataSetChanged();
            return;
        }

        // Process subcategories
        Category category = categories.get(0);
        List<SubCategory> subCategories = category.getSubCategory();
        if (subCategories != null) {
            for(SubCategory subCategory : subCategories) {
                List<Description> descriptions = subCategory.getDescription();
                if (descriptions != null && descriptions.size() > 0) {
                    Description description = descriptions.get(0);
                    String title = description.getName();
                    if (title == null) title = description.getDescription();
                    BrowseItem item = new BrowseItem();
                    item.title = title;
                    item.identifier = subCategory.getIdentifier();
                    adapter.addItem(item);
                }
            }
            wrapper.setState(DataWrapper.State.DONE);
            adapter.notifyDataSetChanged();
            return;
        }
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        String identifier;
        BrowseItem item = (BrowseItem) parent.getItemAtPosition(position);
        switch(item.type) {
            case STACK:
                identifier = adapter.popStack(item);
                fill(identifier);
                wrapper.setState(DataWrapper.State.ADDING);
                adapter.notifyDataSetChanged();
                break;
            case ITEM:
                identifier = item.identifier;
                if (identifier.startsWith("CL") || identifier.startsWith("BI")) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity!=null)
                        activity.selectBundle(item.title, identifier);
                } else {
                    adapter.pushStack(item);
                    fill(item.identifier);
                    wrapper.setState(DataWrapper.State.ADDING);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
