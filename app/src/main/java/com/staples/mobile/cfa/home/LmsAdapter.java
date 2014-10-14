package com.staples.mobile.cfa.home;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.common.access.lms.LmsManager;
import com.staples.mobile.common.access.lms.LmsManager.LmsMgrCallback;
import com.staples.mobile.common.access.lms.model.Area;
import com.staples.mobile.common.access.lms.model.Item;
import com.staples.mobile.common.access.lms.model.Screen;

import java.util.ArrayList;
import java.util.List;

public class LmsAdapter
    extends BaseAdapter
    implements LmsMgrCallback,
               AdapterView.OnItemClickListener {

    private static final String TAG = "LmsAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private Resources resources;
    private MainActivity activity;
    private LayoutInflater layoutInflater;
    private ArrayList<LmsItem> lmsItems;
    private LmsManager lmsManager;

    private LinearLayout lmsItemLayout;

    private ListView bundlesListView;
    private LmsItemViewHolder lmsItemViewHolder;

    private List<Screen> screens;
    private List<Item> items;

    public LmsAdapter(MainActivity activity, View view) {

        super();

        this.activity = activity;
        resources = activity.getResources();

        lmsItems = new ArrayList();
        lmsManager = new LmsManager();

        layoutInflater = activity.getLayoutInflater();

        bundlesListView = (ListView) view.findViewById(R.id.lsvBundles);
        bundlesListView.setAdapter(this);
        bundlesListView.setOnItemClickListener(this);
    }

    public void fill() {

        lmsManager.getLms(this,  // LmsMgrCallback
                true); // conditional
    }

    @Override
    public void onGetLmsResult(boolean success) {

        Log.v(TAG, "LmsAdapter:LmsManager.onGetLmsResult():"
                        + " success[" + success + "]"
                        + " this[" + this + "]"
        );

        if (success) {

            screens = lmsManager.getScreen();
            Screen screen = screens.get(0);
            items = screen.getItem();
            LmsItem lmsItem = null;
            List<Area> areas = null;
            String skuList = null;

            for (Item item : items) {
                areas = item.getArea();
                skuList = areas.get(0).getSkuList();
                lmsItem = new LmsItem(item.getTitle(), item.getBanner(), skuList);
                lmsItems.add(lmsItem);
            }
            notifyDataSetChanged();
        }
        activity.showMainScreen();
    }

    public int getCount() {
        int itemCnt = lmsItems.size();
        return (itemCnt);
    }

    public Object getItem(int position) {
        LmsItem lmsItem = lmsItems.get(position);
        return (lmsItem);
    }

    public long getItemId(int position) {
        LmsItem lmsItem = lmsItems.get(position);
        long uniqueId = lmsItem.hashCode();
        return (uniqueId);
    }

    public View getView(int       position,
                        View      convertView,
                        ViewGroup parentViewGroup) {

        if (convertView == null) {

            lmsItemLayout =
                (LinearLayout) layoutInflater.inflate(R.layout.lms_item,
                                                        parentViewGroup,
                                                        false);
            convertView = lmsItemLayout;

            lmsItemViewHolder = new LmsItemViewHolder();
            lmsItemViewHolder.titleTextView = (TextView) lmsItemLayout.findViewById(R.id.titleTextView);
            lmsItemViewHolder.bannerImageView = (ImageView) lmsItemLayout.findViewById(R.id.categoryImageView);

            convertView.setTag(lmsItemViewHolder);

        } else { // if (wvConvertView == null)

            // Get the LmsItemViewHolder back to get fast access to the view items.

            lmsItemViewHolder = (LmsItemViewHolder) convertView.getTag();

        } // else { // if (wvConvertView == null)

        // Populate LmsItemViewHolder

        LmsItem lmsItemSelected = lmsItems.get(position);
        String title = lmsItemSelected.title;
        lmsItemViewHolder.titleTextView.setText(title);

        String bannerUrl = lmsItemSelected.bannerUrl;
        Picasso picasso = Picasso.with(activity);
        RequestCreator requestCreator = picasso.load(bannerUrl);
        requestCreator.into(lmsItemViewHolder.bannerImageView); // callback
        requestCreator.fit();

        return (convertView);

    } // getView()

    @Override
    public void onItemClick(AdapterView<?> parent,
                            View           clickedView,
                            int            position,
                            long           rowId)
    {
        Log.v(TAG, "LmsAdapter:onItemClick():"
                + " position[" + position + "]"
                + " this[" + this + "]"
        );

        LmsItem lmsItem = lmsItems.get(position);

        String path = "/category/identifier/" + lmsItem.identifier;

        activity.selectBundle(lmsItem.title, path);

        return;

    } // onItemClick()
}
