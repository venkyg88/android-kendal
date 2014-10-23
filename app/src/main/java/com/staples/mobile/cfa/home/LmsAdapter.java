package com.staples.mobile.cfa.home;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
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
import com.staples.mobile.cfa.MainApplication;
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
    DisplayMetrics displayMetrics;
    private MainActivity activity;
    private LayoutInflater layoutInflater;
    private ArrayList<LmsItem> lmsItems;

    private ListView bundlesListView;

    private List<Screen> screens;
    private List<Item> items;

    Picasso picasso;
    private Drawable noPhoto;

    private LmsManager lmsManager;

    public LmsAdapter(MainActivity activity, View view) {

        super();

        Log.v(TAG, "LmsAdapter:LmsAdapter():"
            + " activity[" + activity + "]"
            + " view[" + view + "]"
            + " this[" + this + "]"
        );

        this.activity = activity;
        resources = activity.getResources();
        displayMetrics = resources.getDisplayMetrics();

        lmsManager = new LmsManager();

        lmsItems = new ArrayList();

        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);

        picasso = Picasso.with(activity);
        /* @@@ STUBBED
        // Do NOT enable logging unless absolutely necessary. Picasso logging
        // consumes a lot of memory and cpu cycles. scd 10/13/2014
         picasso.setLoggingEnabled(true);
        @@@ STUBBED */

        // bundlesListView = (ListView) view.findViewById(R.id.bundlesListView);
        bundlesListView.setAdapter(this);
        bundlesListView.setOnItemClickListener(this);
    }

    public void fill() {

        lmsManager.getLms(this,  // LmsMgrCallback
                          true); // conditional
    }

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

                String size = item.getSize();
                if (size.equalsIgnoreCase("C")) continue;
                if (size.equalsIgnoreCase("D")) continue;

                lmsItem = new LmsItem(item.getTitle(), item.getBanner(), skuList, item.getSize());
                lmsItems.add(lmsItem);
            }
            notifyDataSetChanged();
        }
        activity.showMainScreen();
    }

    @Override
    public int getCount() {

        int itemCnt = lmsItems.size();

        Log.v(TAG, "LmsAdapter:getCount():"
                        + " itemCnt[" + itemCnt + "]"
                        + " this[" + this + "]"
        );
        return (itemCnt);
    }

    @Override
    public Object getItem(int position) {
        LmsItem lmsItem = lmsItems.get(position);

        Log.v(TAG, "LmsAdapter:getItem():"
            + " position[" + position + "]"
            + " lmsItem.hashCode()[" + lmsItem.hashCode() + "]"
            + " this[" + this + "]"
        );
        return (lmsItem);
    }

    @Override
    public long getItemId(int position) {
        LmsItem lmsItem = lmsItems.get(position);
        long uniqueId = lmsItem.hashCode();

        Log.v(TAG, "LmsAdapter:getItemId():"
            + " position[" + position + "]"
            + " lmsItem.hashCode()[" + lmsItem.hashCode() + "]"
            + " uniqueId[" + uniqueId + "]"
            + " this[" + this + "]"
        );
        return (uniqueId);
    }

    @Override
    public View getView(int       position,
                        View      convertView,
                        ViewGroup parentViewGroup) {

        Log.v(TAG, "LmsAdapter:getView():"
            + " position[" + position + "]"
            + " convertView[" + convertView + "]"
            + " parentViewGroup[" + parentViewGroup + "]"
            + " this[" + this + "]"
        );

        LmsItem lmsItemSelected = lmsItems.get(position);

        LmsItemViewHolder lmsItemViewHolder = null;

        if (convertView == null) {

            LinearLayout lmsItemLayout =
                (LinearLayout) layoutInflater.inflate(R.layout.lms_item,
                                                        parentViewGroup,
                                                        false);
            convertView = lmsItemLayout;

            Log.v(TAG, "LmsAdapter:getView():"
                + " position[" + position + "]"
                + " convertView[" + convertView + "]"
                + " lmsItemLayout.hashCode()[" + lmsItemLayout.hashCode() + "]"
                + " lmsItemLayout[" + lmsItemLayout + "]"
                + " this[" + this + "]"
            );

            lmsItemViewHolder = createViewHolder(lmsItemSelected, convertView, parentViewGroup);

        } else {

            lmsItemViewHolder = (LmsItemViewHolder) convertView.getTag();

            Log.v(TAG, "LmsAdapter:getView():"
                    + " lmsItemViewHolder.hashCode()[" + lmsItemViewHolder.hashCode() + "]"
                    + " convertView.hashCode()[" + convertView.hashCode() + "]"
                    + " lmsItemViewHolder[" + lmsItemViewHolder + "]"
                    + " convertView[" + convertView + "]"
                    + " this[" + this + "]"
            );
        }

        populateListItem(lmsItemSelected, lmsItemViewHolder);

        return (convertView);

    } // getView()

    private void populateListItem(LmsItem lmsItemSelected, LmsItemViewHolder lmsItemViewHolder) {

        lmsItemViewHolder.titleTextView1.setText(lmsItemSelected.title);

        if (lmsItemViewHolder.titleTextView2 != null) {
            lmsItemViewHolder.titleTextView2.setText(lmsItemSelected.title);
        }
        // The into() method MUST run on the main thread. If not, you will get
        // an IllegalStateException. scd 10/13/2014

        RequestCreator requestCreator = picasso.load(lmsItemSelected.bannerUrl);
        requestCreator.error(noPhoto);
        requestCreator.into(lmsItemViewHolder.categoryImageView1);
        requestCreator.fit();

        if (lmsItemViewHolder.categoryImageView2 != null) {
            RequestCreator requestCreator2 = picasso.load(lmsItemSelected.bannerUrl);
            requestCreator2.error(noPhoto);
            requestCreator2.into(lmsItemViewHolder.categoryImageView2);
            requestCreator2.fit();
        }
    }

    private LmsItemViewHolder createViewHolder(LmsItem lmsItemSelected, View convertView, ViewGroup parentViewGroup) {

        LinearLayout lmsItemLayout = (LinearLayout) convertView;

        Log.v(TAG, "LmsAdapter:createViewHolder():"
                + " lmsItemLayout.hashCode()[" + lmsItemLayout.hashCode() + "]"
                + " convertView.hashCode()[" + convertView.hashCode() + "]"
                + " lmsItemLayout[" + lmsItemLayout + "]"
                + " convertView[" + convertView + "]"
                + " parentViewGroup[" + parentViewGroup + "]"
                + " this[" + this + "]"
        );

        LmsItemViewHolder lmsItemViewHolder = new LmsItemViewHolder();

        Log.v(TAG, "LmsAdapter:createViewHolder():"
                + " lmsItemViewHolder.hashCode()[" + lmsItemViewHolder.hashCode() + "]"
                + " lmsItemViewHolder[" + lmsItemViewHolder + "]"
                + " this[" + this + "]"
        );

        Log.v(TAG, "LmsAdapter:createViewHolder():"
                + " lmsItemSelected.size[" + lmsItemSelected.size + "]"
                + " lmsItemSelected.title[" + lmsItemSelected.title + "]"
                + " lmsItemSelected.bannerUrl[" + lmsItemSelected.bannerUrl + "]"
                + " lmsItemSelected.identifier[" + lmsItemSelected.identifier + "]"
                + " this[" + this + "]"
        );

        String size = lmsItemSelected.size;

        int subLayoutWidth = 0;
        int subLayoutHeight = 0;
        int nbrOfSubLayouts = 0;

        int parentLayoutWidth = parentViewGroup.getWidth(); // pixels
        int parentLayoutHeight = parentViewGroup.getHeight(); // pixels

        if (size.equalsIgnoreCase("A")) {

            nbrOfSubLayouts = 1;
            subLayoutWidth = parentLayoutWidth;
            subLayoutHeight = parentLayoutWidth;

        } else if (size.equalsIgnoreCase("B")) {

            nbrOfSubLayouts = 1;
            subLayoutWidth = parentLayoutWidth;
            subLayoutHeight = parentLayoutWidth / 2;

        } else if (size.equalsIgnoreCase("C")) {

            nbrOfSubLayouts = 1;
            subLayoutWidth = parentLayoutWidth;
            subLayoutHeight = parentLayoutWidth / 4;

        } else if (size.equalsIgnoreCase("D")) {

            nbrOfSubLayouts = 2;
            subLayoutWidth = parentLayoutWidth / 2;
            subLayoutHeight = parentLayoutWidth / 2;
        }

        Log.v(TAG, "LmsAdapter:createViewHolder():"
                + " subLayoutWidth[" + subLayoutWidth + "]"
                + " subLayoutHeight[" + subLayoutHeight + "]"
                + " parentLayoutWidth[" + parentLayoutWidth + "]"
                + " parentLayoutHeight[" + parentLayoutHeight + "]"
                + " this[" + this + "]"
        );

        LinearLayout.LayoutParams lmsItemLayoutParms = null;

        LinearLayout.LayoutParams widgetLayoutParms = null;

        LinearLayout lmsSubLayout1 = null;
        TextView titleTextView1 = null;
        ImageView categoryImageView1 = null;

        LinearLayout lmsSubLayout2 = null;
        TextView titleTextView2 = null;
        ImageView categoryImageView2 = null;

        int marginTopPixels = 0;
        int marginBottomPixels = 0;
        int marginLeftPixels = 0;
        int marginRightPixels = 0;

        int widthPixels = 0;
        int heightPixels = 0;

        // LMS Sublayout 1

        lmsItemLayoutParms =
            new LinearLayout.LayoutParams(subLayoutWidth, // width
                                          subLayoutHeight); // height

        lmsSubLayout1 = new LinearLayout(activity);
        lmsSubLayout1.setLayoutParams(lmsItemLayoutParms);

        lmsSubLayout1.setId(lmsSubLayout1.hashCode());
        lmsSubLayout1.setBackgroundResource(R.drawable.rectangle_frame);
        lmsSubLayout1.setOrientation(LinearLayout.VERTICAL);
        lmsSubLayout1.setMeasureWithLargestChildEnabled(false);

        lmsItemLayout.addView(lmsSubLayout1);

        // Title TextView 1

        titleTextView1 = new TextView(activity);
        titleTextView1.setId(titleTextView1.hashCode());
        titleTextView1.setGravity(Gravity.LEFT);
        titleTextView1.setLines(2);
        titleTextView1.setEllipsize(TextUtils.TruncateAt.END);
        titleTextView1.setTextColor(resources.getColor(R.color.sel_text_list_item));
        titleTextView1.setTextAppearance(activity, R.style.TextExtraLargeListItem);

        widthPixels = resources.getDimensionPixelSize(R.dimen.min_column_width); // width in pixels

        widgetLayoutParms =
            new LinearLayout.LayoutParams(widthPixels, // width
                                          ViewGroup.LayoutParams.WRAP_CONTENT); // height
        marginLeftPixels = 0;
        marginTopPixels = (int) (4 * displayMetrics.density); // margin in pixels
        marginRightPixels = 0;
        marginBottomPixels = (int) (8 * displayMetrics.density); // margin in pixels

        widgetLayoutParms.setMargins(marginLeftPixels,
                                     marginTopPixels,
                                     marginRightPixels,
                                     marginBottomPixels);

        Log.v(TAG, "LmsAdapter:createViewHolder(): titleTextView1:"
                + " widthPixels[" + widthPixels + "]"
                + " marginLeftPixels[" + marginLeftPixels + "]"
                + " marginTopPixels[" + marginTopPixels + "]"
                + " marginRightPixels[" + marginRightPixels + "]"
                + " marginBottomPixels[" + marginBottomPixels + "]"
                + " this[" + this + "]"
        );

        titleTextView1.setLayoutParams(widgetLayoutParms);

        lmsSubLayout1.addView(titleTextView1);

        // Category ImageView 1

        categoryImageView1 = new ImageView(activity);
        categoryImageView1.setId(categoryImageView1.hashCode());
        categoryImageView1.setAdjustViewBounds(true);

        /* @@@ STUBBED
        widthPixels = subLayoutHeight * 3 / 4; // width in pixels
        @@@ STUBBED */
        widthPixels = subLayoutHeight * 2 / 4; // width in pixels
        heightPixels = widthPixels; // height in pixels

        widgetLayoutParms =
            new LinearLayout.LayoutParams(widthPixels, // width
                                          heightPixels); // height

        marginLeftPixels = (int) (24 * displayMetrics.density); // margin in pixels
        marginTopPixels = 0;
        marginRightPixels = (int) (24 * displayMetrics.density); // margin in pixels
        marginBottomPixels = (int) (8 * displayMetrics.density); // margin in pixels

        widgetLayoutParms.setMargins(marginLeftPixels,
                                     marginTopPixels,
                                     marginRightPixels,
                                     marginBottomPixels);

        Log.v(TAG, "LmsAdapter:createViewHolder(): categoryImageView1:"
                + " subLayoutWidth[" + subLayoutWidth + "]"
                + " subLayoutHeight[" + subLayoutHeight + "]"
                + " widthPixels[" + widthPixels + "]"
                + " heightPixels[" + heightPixels + "]"
                + " marginLeftPixels[" + marginLeftPixels + "]"
                + " marginTopPixels[" + marginTopPixels + "]"
                + " marginRightPixels[" + marginRightPixels + "]"
                + " marginBottomPixels[" + marginBottomPixels + "]"
                + " this[" + this + "]"
        );

        categoryImageView1.setLayoutParams(widgetLayoutParms);

        lmsSubLayout1.addView(categoryImageView1);

        if (size.equalsIgnoreCase("A")
        ||  size.equalsIgnoreCase("B")
        ||  size.equalsIgnoreCase("C")) {

            lmsItemViewHolder.lmsSubLayout1 = lmsSubLayout1;
            lmsItemViewHolder.titleTextView1 = titleTextView1;
            lmsItemViewHolder.categoryImageView1 = categoryImageView1;

            lmsItemViewHolder.lmsSubLayout2 = null;
            lmsItemViewHolder.titleTextView2 = null;
            lmsItemViewHolder.categoryImageView2 = null;

            lmsItemLayout.setTag(lmsItemViewHolder);

            return (lmsItemViewHolder);
        }

        // LMS Sublayout 2

        lmsItemLayoutParms =
            new LinearLayout.LayoutParams(subLayoutWidth, // width
                                          subLayoutHeight); // height

        lmsSubLayout2 = new LinearLayout(activity);
        lmsSubLayout2.setLayoutParams(lmsItemLayoutParms);

        lmsSubLayout2.setId(lmsSubLayout2.hashCode());
        lmsSubLayout2.setBackgroundResource(R.drawable.rectangle_frame);
        lmsSubLayout2.setOrientation(LinearLayout.VERTICAL);
        lmsSubLayout2.setMeasureWithLargestChildEnabled(false);

        /* @@@ STUBBED
        lmsItemLayoutParms.addRule(LinearLayout.RIGHT_OF,
                                   lmsSubLayout1.getId());
        @@@ STUBBED */

        lmsItemLayout.addView(lmsSubLayout2);

        // Title TextView 2

        titleTextView2 = new TextView(activity);
        titleTextView2.setId(titleTextView2.hashCode());
        titleTextView2.setGravity(Gravity.LEFT);
        titleTextView2.setLines(2);
        titleTextView2.setEllipsize(TextUtils.TruncateAt.END);
        titleTextView2.setTextColor(resources.getColor(R.color.sel_text_list_item));
        titleTextView2.setTextAppearance(activity, R.style.TextExtraLargeListItem);

        widthPixels = resources.getDimensionPixelSize(R.dimen.min_column_width); // width in pixels

        widgetLayoutParms =
            new LinearLayout.LayoutParams(widthPixels, // width
                                          ViewGroup.LayoutParams.WRAP_CONTENT); // height
        marginLeftPixels = 0;
        marginTopPixels = (int) (4 * displayMetrics.density); // margin in pixels
        marginRightPixels = 0;
        marginBottomPixels = (int) (8 * displayMetrics.density); // margin in pixels

        widgetLayoutParms.setMargins(marginLeftPixels,
                                     marginTopPixels,
                                     marginRightPixels,
                                     marginBottomPixels);

        titleTextView2.setLayoutParams(widgetLayoutParms);

        lmsSubLayout2.addView(titleTextView2);

        // Category ImageView 2

        categoryImageView2 = new ImageView(activity);
        categoryImageView2.setId(categoryImageView2.hashCode());
        categoryImageView2.setAdjustViewBounds(true);

        widthPixels = resources.getDimensionPixelSize(R.dimen.image_square_size); // width in pixels
        heightPixels = resources.getDimensionPixelSize(R.dimen.image_square_size); // height in pixels

        widgetLayoutParms =
            new LinearLayout.LayoutParams(widthPixels, // width
                                          heightPixels); // height

        marginLeftPixels = (int) (24 * displayMetrics.density); // margin in pixels
        marginTopPixels = 0;
        marginRightPixels = (int) (24 * displayMetrics.density); // margin in pixels
        marginBottomPixels = (int) (8 * displayMetrics.density); // margin in pixels

        widgetLayoutParms.setMargins(marginLeftPixels,
                                     marginTopPixels,
                                     marginRightPixels,
                                     marginBottomPixels);

        categoryImageView2.setLayoutParams(widgetLayoutParms);

        lmsSubLayout2.addView(categoryImageView2);

        // Set View Holder

        lmsItemViewHolder.lmsSubLayout1 = lmsSubLayout1;
        lmsItemViewHolder.titleTextView1 = titleTextView1;
        lmsItemViewHolder.categoryImageView1 = categoryImageView1;

        lmsItemViewHolder.lmsSubLayout2 = lmsSubLayout2;
        lmsItemViewHolder.titleTextView2 = titleTextView2;
        lmsItemViewHolder.categoryImageView2 = categoryImageView2;

        lmsItemLayout.setTag(lmsItemViewHolder);

        return (lmsItemViewHolder);
    }

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
