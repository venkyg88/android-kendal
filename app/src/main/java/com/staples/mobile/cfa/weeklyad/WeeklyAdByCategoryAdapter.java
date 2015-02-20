package com.staples.mobile.cfa.weeklyad;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.util.WeeklyAdImageUrlHelper;
import com.staples.mobile.common.access.easyopen.model.weeklyadbycategory.Data;

import java.util.ArrayList;
import java.util.List;

public class WeeklyAdByCategoryAdapter extends RecyclerView.Adapter<WeeklyAdByCategoryAdapter.ViewHolder>{
    private ArrayList<Data> array;
    private Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView dealTitleTV;
        public TextView dealsCountTV;
        ImageView dealIV;
        private ClickListener clickListener;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            dealTitleTV = (TextView)v.findViewById(R.id.weekly_ad_category_title_text);
            dealsCountTV = (TextView)v.findViewById(R.id.weekly_ad_category_deals_count_text);
            dealIV = (ImageView)v.findViewById(R.id.weekly_ad_category_image);
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
    }

    @Override
    public WeeklyAdByCategoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weekly_ad_categories_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position) {
                Data data = array.get(position);
                WeeklyAdListFragment weeklyAdFragment = new WeeklyAdListFragment();
                weeklyAdFragment.setArguments("2278338",data.getCategorytreeid(), data.getName());
                ((MainActivity)activity).navigateToFragment(weeklyAdFragment);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = array.get(position);

        holder.dealTitleTV.setText(data.getName());
        holder.dealsCountTV.setText(data.getCount() + " deals");
        Picasso.with(activity)
                .load(WeeklyAdImageUrlHelper.getUrl(60,100, data.getImagelocation()))
                .into(holder.dealIV);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void fill(List<Data> items) {
        array.addAll(items);
        notifyDataSetChanged();
    }
}