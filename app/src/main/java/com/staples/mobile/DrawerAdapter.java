package com.staples.mobile;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.staples.mobile.browse.BrowseApi;
import com.staples.mobile.browse.CategoryFragment;
import com.staples.mobile.browse.CategoryItem;
import com.staples.mobile.object.Browse;
import com.staples.mobile.object.Category;
import com.staples.mobile.object.Description;
import com.staples.mobile.object.FilterGroup;
import com.staples.mobile.object.SubCategory;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pyhre001 on 8/14/14.
 */
public class DrawerAdapter extends BaseAdapter implements Callback<Browse> {
    private static final String TAG = "DrawerAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<DrawerItem> array;

    public DrawerAdapter(Context context) {
        super();
        activity = (Activity) context;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<DrawerItem>(16);
    }

    /* Array items */

    @Override
    public boolean isEmpty() {
        return(array.isEmpty());
    }

    @Override
    public int getCount() {
        return(array.size());
    }

    public void add(DrawerItem item) {
       array.add(item);
    }

    @Override
    public DrawerItem getItem(int position) {
        return(array.get(position));
    }

    @Override
    public long getItemId(int position) {
        return(0);
    }

    /* Views */

    @Override
    public int getViewTypeCount() {
        return (DrawerItem.NTYPES);
    }

    @Override
    public int getItemViewType(int position) {
        DrawerItem item = array.get(position);
        return(item.type.viewType);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DrawerItem item = array.get(position);

        // Get a new or recycled view of the right type
        if (view==null)
            view = inflater.inflate(item.type.layoutId, null);

        // Set enable
        view.setEnabled(item.isEnabled());

        // Set icon TODO

        // Set title
        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) {
            if (item.childCount > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(item.title);
                sb.append(" (");
                sb.append(item.childCount);
                sb.append(")");
                title.setText(sb.toString());
            } else title.setText(item.title);
        }
        return(view);
    }

    // Enables

    @Override
    public boolean areAllItemsEnabled () {
        return(false);
    }

    @Override
    public boolean isEnabled(int position) {
        DrawerItem item = array.get(position);
        return(item.isEnabled());
    }

    public void fill() {
        // Fill adapter with fragment titles
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.home_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.feed_title, ToBeDoneFragment.class));

        add(new DrawerItem(DrawerItem.Type.HEADER, activity, 0, R.string.shop_header));
        add(new DrawerItem(DrawerItem.Type.CATEGORY, activity, 0, R.string.products_title, CategoryFragment.class));
        add(new DrawerItem(DrawerItem.Type.CATEGORY, activity, 0, R.string.technology_title, CategoryFragment.class));

        add(new DrawerItem(DrawerItem.Type.HEADER, activity, 0, R.string.account_header));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.order_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.reward_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.store_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.profile_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.list_title, ToBeDoneFragment.class));

        add(new DrawerItem(DrawerItem.Type.HEADER, activity, 0, R.string.account_header));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.ink_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.settings_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.help_title, ToBeDoneFragment.class));


        RestAdapter easyOpenApi = ((MainApplication) activity.getApplication()).getEasyOpenApi();
        BrowseApi browseApi = easyOpenApi.create(BrowseApi.class);
        String path = "category/top";
        browseApi.browse(RECOMMENDATION, STORE_ID, path, CATALOG_ID, LOCALE, ZIPCODE, CLIENT_ID, this);
    }

    public void success(Browse browse, Response response) {
        Category[] categories = browse.getCategory();
        if (categories!=null) {

            // Process categories
            int count = categories.length;
            for (int i = 0; i < count; i++) {
                Category category = categories[i];
                Description[] descriptions = category.getDescription();
                if (descriptions != null && descriptions.length > 0) {
                    Description description = descriptions[0];
                    String title = description.getText();
                    if (title == null) title = description.getDescription();
                    if (title == null)  title = description.getName();
                        DrawerItem item = findItemByTitle(title);
                    if (item != null) {
                        item.childCount = category.getChildCount();
                        item.path = category.getCategoryUrl();
                    }
                }
            }
            Log.d(TAG, "Got " + count + " categories");
        }
        notifyDataSetChanged();
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Sorry, failure " + retrofitError);
        notifyDataSetChanged();
    }

    DrawerItem findItemByTitle(String title) {
        int n = array.size();
        for(int i=0;i<n;i++) {
            DrawerItem item = array.get(i);
            if (item.title!=null && item.title.equals(title))
                return(item);
        }
        return(null);
    }

    // update must be run on the UI thread

    public void update() {
        Runnable runs = new Runnable() {public void run() {DrawerAdapter.this.notifyDataSetChanged();}};
        activity.runOnUiThread(runs);
    }
}
