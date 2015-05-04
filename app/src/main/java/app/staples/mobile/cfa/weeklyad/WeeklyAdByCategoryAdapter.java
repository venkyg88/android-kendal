package app.staples.mobile.cfa.weeklyad;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;

import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;

public class WeeklyAdByCategoryAdapter extends RecyclerView.Adapter<WeeklyAdByCategoryAdapter.ViewHolder>{
    private ArrayList<Data> array;
    private Activity activity;
    String storeId;
    ArrayList<String> categoryTreeIds;
    ArrayList<String> titles;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView weeklyAdDealTileTV;
        public TextView weeklyAdDealCountTV;
        private ImageView weeklyAdCategoryIV;
        private ClickListener clickListener;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            weeklyAdDealTileTV = (TextView)v.findViewById(R.id.weekly_ad_category_title_text);
            weeklyAdDealCountTV = (TextView)v.findViewById(R.id.weekly_ad_category_deals_count_text);
            weeklyAdCategoryIV = (ImageView)v.findViewById(R.id.weekly_ad_category_image);
        }

        public interface ClickListener {

            /**
             * Called when the view is clicked.
             *
             * @param v view that is clicked
             * @param position of the clicked item
             */
            public void onClick(View v, int position);

        }

        /* Setter for listener. */
        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getPosition());
        }
    }

    public WeeklyAdByCategoryAdapter(Activity activity) {
        this.activity = activity;
        this.array = new ArrayList<Data>();
        this.categoryTreeIds = new ArrayList<>();
        this.titles = new ArrayList<>();
    }

    @Override
    public WeeklyAdByCategoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekly_ad_categories_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position) {
//                Data data = array.get(position);
                WeeklyAdListFragment weeklyAdFragment = new WeeklyAdListFragment();
                weeklyAdFragment.setArguments(storeId, position, categoryTreeIds, titles);
                ((MainActivity)activity).selectFragment(DrawerItem.WEEKLYDETAIL, weeklyAdFragment, MainActivity.Transition.RIGHT, true);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = array.get(position);

        holder.weeklyAdDealTileTV.setText(data.getName());
        holder.weeklyAdDealCountTV.setText(activity.getResources().getQuantityString(R.plurals.deals,
                                           data.getCount(), data.getCount()));
        Picasso.with(activity)
                .load(data.getImagelocation()) // note that urls returned from category api don't include dimensions
                .error(R.drawable.no_photo)
                .into(holder.weeklyAdCategoryIV);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void fill(List<Data> items, String storeId) {
        this.storeId = storeId;
        array.addAll(items);
        for (Data item : items) {
            categoryTreeIds.add(item.getCategorytreeid());
            titles.add(item.getName());
        }
        notifyDataSetChanged();
    }
}
