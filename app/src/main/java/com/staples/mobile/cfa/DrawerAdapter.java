package com.staples.mobile.cfa;

import android.app.Activity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.feed.PersonalFeedFragment;
import com.staples.mobile.cfa.home.LmsFragment;
import com.staples.mobile.cfa.login.LoginFragment;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.store.StoreFragment;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.access.easyopen.model.browse.Description;
import com.staples.mobile.common.access.easyopen.model.browse.SubCategory;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DrawerAdapter extends BaseAdapter implements Callback<Browse>{
    private static final String TAG = "DrawerAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
//    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;


    private static final int MAXFETCH = 50;

    private Activity activity;
    private DataWrapper wrapper;
    private LayoutInflater inflater;

    private ArrayList<DrawerItem> menuList;
    private ArrayList<DrawerItem> stackList;
    private ArrayList<DrawerItem> browseList;

    private boolean browseMode;
    private Button signInBtn;

    public DrawerAdapter(Activity activity, DataWrapper wrapper) {
        super();
        this.activity = activity;
        this.wrapper = wrapper;
        inflater = activity.getLayoutInflater();
        menuList = new ArrayList<DrawerItem>();
        stackList = new ArrayList<DrawerItem>();
        browseList = new ArrayList<DrawerItem>();
    }

    public void setBrowseMode(boolean mode) {
        if (mode==browseMode) return;
        browseMode = mode;
        notifyDataSetChanged();
    }

    // Items

    @Override
    public int getCount() {
        if (!browseMode) return(menuList.size());
        else return(stackList.size()+browseList.size());
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public DrawerItem getItem(int position) {
        int size;

        if (!browseMode) {
            size = menuList.size();
            if (position<size) return(menuList.get(position));
        }
        else {
            size = stackList.size();
            if (position < size) return (stackList.get(position));
            position -= size;
            size = browseList.size();
            if (position < size) return (browseList.get(position));
        }
        return(null);
    }

    /* Views */

    @Override
    public int getViewTypeCount() {
        return (DrawerItem.NTYPES);
    }

    @Override
    public int getItemViewType(int position) {
        DrawerItem item = getItem(position);
        return(item.type.viewType);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DrawerItem item = getItem(position);

        // Get a new or recycled view of the right type
        if (view==null)
            view = inflater.inflate(item.type.layoutId, parent, false);

        // Set icon
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        if (icon!=null) icon.setImageDrawable(item.icon);

        // Set title
        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) title.setText(item.title);

        return(view);
    }

    public void fill() {
        wrapper.setState(DataWrapper.State.ADDING);

        // Fill menu list TODO icons nulled out
        menuList.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.home_title, LmsFragment.class));
        menuList.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.personal_feed_title, PersonalFeedFragment.class));
        menuList.add(new DrawerItem(DrawerItem.Type.BROWSE, activity, 0, R.string.category_title));
        menuList.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.store_locator_title, StoreFragment.class));
        menuList.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.weekly_ad_title, ToBeDoneFragment.class));
        menuList.add(new DrawerItem(DrawerItem.Type.ACCOUNT, activity, 0, R.string.account_title, LoginFragment.class));
        menuList.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.rewards_title, ToBeDoneFragment.class));
        menuList.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.order_title, ToBeDoneFragment.class));
        menuList.add(new DrawerItem(DrawerItem.Type.PROFILE, activity, 0, R.string.profile_title, ProfileFragment.class));
        menuList.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.about_title, AboutFragment.class));

        // Fill stack list
        stackList.add(new DrawerItem(DrawerItem.Type.BACKTOTOP, activity, 0, R.string.backtotop_title));

        // Fill browse list
        fill(null);
    }

    public void pushStack(DrawerItem item) {
        // Push new stack item
        item.type = DrawerItem.Type.STACK;
        stackList.add(item);

        // Clear browse list
        browseList.clear();

        // Update and queue query
        notifyDataSetChanged();
        fill(item.path);
    }

    public void popStack(DrawerItem item) {
        // Safety check
        int index = stackList.indexOf(item);
        if (index<0) return;

        // Get path
        String path = null;
        if (index>0) path = stackList.get(index-1).path;

        // Pop stack
        int size = stackList.size();
        for(;index<size;) {
            size--;
            DrawerItem dead = (DrawerItem) stackList.remove(size);
        }

        // Clear browse list
        browseList.clear();

        // Update and queue query
        notifyDataSetChanged();
        fill(path);
    }

    void fill(String path) {
        int i, j;

        wrapper.setState(DataWrapper.State.ADDING);

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        // Get top categories
        if (path==null) {
            easyOpenApi.topCategories(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE, null,
                                      ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // Decode category alphanumeric identifiers
        i = path.indexOf("/category/identifier/");
        if (i >= 0) {
            i += "/category/identifier/".length();
            j = path.indexOf('?', i);
            if (j <= 0) j = path.length();
            String identifier = path.substring(i, j);
            easyOpenApi.browseCategories(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                                         ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // Decode category numeric identifiers
        i = path.indexOf("parentIdentifier=");
        if (i >= 0) {
            i += "parentIdentifier=".length();
            j = path.indexOf('&', i);
            if (j <= 0) j = path.length();
            String parentIdentifier = path.substring(i, j);
            easyOpenApi.topCategories(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE, parentIdentifier,
                                      ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // No idea what the path is
        Log.d(TAG, "Unknown path: " + path);
        wrapper.setState(DataWrapper.State.NOMORE);
        notifyDataSetChanged();
    }

    @Override
    public void success(Browse browse, Response response) {
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) {
            notifyDataSetChanged();
            return;
        }

        // Process categories
        if (categories.size()>1) {
            for(Category category : categories){
                List<Description> descriptions = category.getDescription1();
                if (descriptions != null && descriptions.size() > 0) {
                    // Get category title
                    Description description = descriptions.get(0);
                    String title = description.getText();
                    if (title == null) title = description.getDescription();
                    if (title == null) title = description.getName();
                    title = Html.fromHtml(title).toString();
                    DrawerItem item = new DrawerItem(DrawerItem.Type.CATEGORY, activity, 0, 0);
                    item.title = title;
                    item.path = category.getCategoryUrl();
                    browseList.add(item);
                }
            }
            wrapper.setState(DataWrapper.State.DONE);
            notifyDataSetChanged();
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
                    DrawerItem item = new DrawerItem(DrawerItem.Type.CATEGORY, activity, 0, 0);
                    item.title = title;
                    item.path = subCategory.getCategoryUrl();
                    browseList.add(item);
                }
            }
            wrapper.setState(DataWrapper.State.DONE);
            notifyDataSetChanged();
            return;
        }
        notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        wrapper.setState(DataWrapper.State.NOMORE);
        notifyDataSetChanged();
    }
}
