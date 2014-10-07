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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.MainApplication;

import com.staples.mobile.common.access.lms.api.LmsApi;
import com.staples.mobile.common.access.lms.LmsManager.LmsMgrCallback;
import com.staples.mobile.common.access.lms.LmsManager;
import com.staples.mobile.common.access.lms.model.Item;
import com.staples.mobile.common.access.lms.model.Lms;
import com.staples.mobile.common.access.lms.model.Screen;

import java.util.ArrayList;
import java.util.List;

public class LmsAdapter
    extends BaseAdapter
    implements LmsMgrCallback,
               com.squareup.picasso.Callback {

    private static final String TAG = "LmsAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private Resources resources;
    private MainActivity activity;
    private LayoutInflater layoutInflater;
    private ArrayList<LmsItem> lmsItems;
    private LmsManager lmsManager;

    private RelativeLayout lmsItemLayout;

    private LmsItemViewHolder lmsItemViewHolder;

    public LmsAdapter(MainActivity activity) {

        super();
        this.activity = activity;
        resources = MainApplication.application.getResources();
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lmsItems = new ArrayList();
        lmsManager = new LmsManager();
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
            Screen screen = lmsManager.getScreen();
            List<Item> items = screen.getItem();
            for (Item item : items) {
                lmsItems.add(new LmsItem(item.getTitle(), item.getBanner(), null));
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
                (RelativeLayout) layoutInflater.inflate(R.layout.lms_item_layout,
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
        requestCreator.into(lmsItemViewHolder.bannerImageView,
                            this); // callback
        requestCreator.fit();

        return (convertView);

    } // getView()

    // com.squareup.picasso.Callback.onSuccess()

    public void onSuccess() {
        return;
    }

    // com.squareup.picasso.Callback.onError()

    public void onError() {
        return;
    }
}
