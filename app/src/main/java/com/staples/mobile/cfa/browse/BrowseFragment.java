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

    private static final int MAXFETCH = 50;

    private static Bundle adapterState; // TODO Using global state

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

        wrapper.setState(DataWrapper.State.ADDING);
        adapter.restoreState(adapterState);
        fill(adapter.getActiveIdentifier());
        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity!=null) activity.showActionBar(R.string.staples, R.drawable.ic_search_white, null);
    }

    void fill(String identifier) {
        int i, j;

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        // Top categories
        if (identifier==null || identifier.isEmpty()) {
            easyOpenApi.topCategories(null, null, MAXFETCH, this);
            return;
        }

        // Alphanumeric identifier?
        char c = identifier.charAt(0);
        if (c>='A' && c<='Z') {
            easyOpenApi.browseCategories(identifier, null, MAXFETCH, this);
            return;
        }

        // Numeric identifier
        else {
            easyOpenApi.topCategories(identifier, null, MAXFETCH, this);
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

    private String getTitleFromDescriptions(List<Description> descriptions) {
        if (descriptions==null) return(null);
        for(Description description : descriptions) {
            String title = description.getName();
            if (title==null) title = description.getText();
            if (title==null) title = description.getDescription();
            if (title!=null) {
                title = Html.fromHtml(title).toString();
                return(title);
            }
        }
        return(null);
    }

    private void processCategories(List<Category> categories) {
        // Process categories
        if (categories.size() > 1) {
            for(Category category : categories) {
                List<Description> descriptions = category.getDescription1();
                String title = getTitleFromDescriptions(descriptions);
                if (title!=null) {
                    BrowseItem item = new BrowseItem(BrowseItem.Type.ITEM, title, category.getIdentifier());
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
                String title = getTitleFromDescriptions(descriptions);
                if (title!=null) {
                    BrowseItem item = new BrowseItem(BrowseItem.Type.ITEM, title, subCategory.getIdentifier());
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
                adapterState = adapter.saveState(adapterState);
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
                    adapterState = adapter.saveState(adapterState);
                    wrapper.setState(DataWrapper.State.ADDING);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
