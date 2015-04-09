package com.staples.mobile.cfa.browse;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.cfa.IdentifierType;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.FixedSizeLayoutManager;
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

public class BrowseFragment extends Fragment  implements Callback<Browse>, View.OnClickListener {
    private static final String TAG = "BrowseFragment";

    private static final int MAXFETCH = 50;

    private BrowseAdapter adapter;
    private DataWrapper.State state;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        adapter = new BrowseAdapter(getActivity());
        adapter.setOnClickListener(this);
        fill(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.browse_frame, container, false);
        RecyclerView list = (RecyclerView) view.findViewById(R.id.products);
        FixedSizeLayoutManager layoutManager = new FixedSizeLayoutManager(activity);
        layoutManager.setUnitHeight(activity.getResources().getDimensionPixelSize(R.dimen.browse_item_height));
        list.setLayoutManager(layoutManager);
        applyState(view);
        return (view);
    }

    private void applyState(View view) {
        if (view==null) view = getView();
        if (view==null) return;
        DataWrapper wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        if (adapter!=null) {
            RecyclerView list = (RecyclerView) wrapper.findViewById(R.id.products);
            list.setAdapter(adapter);
        }
        wrapper.setState(state);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.BROWSE);
    }

    void fill(String identifier) {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        switch(IdentifierType.detect(identifier)) {
            case EMPTY:
                easyOpenApi.topCategories(null, null, MAXFETCH, this);
                break;
            case TOPCATEGORY:
            case SKU: // Not really a SKU, a numeric identifier;
                easyOpenApi.topCategories(identifier, null, MAXFETCH, this);
                break;
            default:
                easyOpenApi.getCategory(identifier, null, MAXFETCH, null, null, this);
                break;
        }
        state = DataWrapper.State.ADDING;
        applyState(null);
    }

    @Override
    public void success(Browse browse, Response response) {
        Activity activity = getActivity();
        if (activity==null) return;

        int count = processCategories(browse);
        if (count==0) state = DataWrapper.State.NOMORE;
        else state = DataWrapper.State.DONE;
        applyState(null);
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity == null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        ((MainActivity)activity).showErrorDialog(msg);
        Log.d(TAG, msg);
        state = DataWrapper.State.NOMORE;
        applyState(null);
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

    private int processCategories(Browse browse) {
        if (browse==null) return(0);
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) return(0);
        int count = 0;

        for(Category category : categories) {
            List<SubCategory> subCategories = category.getSubCategory();
            if (subCategories!=null) {
                for(SubCategory subCategory : subCategories) {
                    List<Description> descriptions = subCategory.getDescription();
                    String title = getTitleFromDescriptions(descriptions);
                    if (title!=null) {
                        BrowseItem item = new BrowseItem(BrowseItem.Type.ITEM, title, subCategory.getIdentifier());
                        adapter.addItem(item);
                        count++;
                    }
                }
            } else {
                List<Description> descriptions = category.getDescription1();
                String title = getTitleFromDescriptions(descriptions);
                if (title != null) {
                    BrowseItem item = new BrowseItem(BrowseItem.Type.ITEM, title, category.getIdentifier());
                    adapter.addItem(item);
                    count++;
                }
            }
        }

        adapter.notifyDataSetChanged();
        return(count);
    }

    @Override
    public void onClick(View view) {
        String identifier;
        Object obj = view.getTag();
        if (obj instanceof BrowseItem) {
            BrowseItem item = (BrowseItem) obj;
            switch(item.type) {
                case STACK:
                    identifier = adapter.popStack(item);
                    fill(identifier);
                    Tracker.getInstance().trackActionForShopByCategory(adapter.getCategoryHierarchy()); // analytics
                    break;
                case ITEM:
                    identifier = item.identifier;
                    switch(IdentifierType.detect(identifier)) {
                        case CLASS:
                        case BUNDLE:
                            adapter.selectItem(item);
                            MainActivity activity = (MainActivity) getActivity();
                            if (activity != null) {
                                Tracker.getInstance().trackActionForShopByCategory(adapter.getCategoryHierarchy() + ":" + item.title); // analytics
                                activity.selectBundle(item.title, identifier);
                            }
                            break;
                        default:
                            adapter.pushStack(item);
                            fill(identifier);
                            Tracker.getInstance().trackActionForShopByCategory(adapter.getCategoryHierarchy()); // analytics
                            break;
                    }
                    break;
            }
        }
    }
}
