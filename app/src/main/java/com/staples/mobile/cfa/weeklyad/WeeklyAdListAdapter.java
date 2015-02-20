package com.staples.mobile.cfa.weeklyad;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.util.WeeklyAdImageUrlHelper;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;

import java.util.ArrayList;
import java.util.List;

public class WeeklyAdListAdapter extends RecyclerView.Adapter<WeeklyAdListAdapter.ViewHolder> {

    private ArrayList<Data> array;
    private Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView dealTitleTV;
        public TextView dealsCountTV;
        ImageView dealIV;

        public ViewHolder(View v) {
            super(v);
            dealTitleTV = (TextView)v.findViewById(R.id.weekly_ad_list_title_text);
            dealsCountTV = (TextView)v.findViewById(R.id.weekly_ad_list_deal_text);
            dealIV = (ImageView)v.findViewById(R.id.weekly_ad_list_image);
        }
    }

    public WeeklyAdListAdapter(Activity activity) {
        this.activity = activity;
        this.array = new ArrayList<Data>();
    }

    @Override
    public WeeklyAdListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weekly_ad_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = array.get(position);

        holder.dealTitleTV.setText(data.getTitle());
        holder.dealsCountTV.setText(data.getPrice());
        Picasso.with(activity)
                .load(WeeklyAdImageUrlHelper.getUrl(60,100, data.getImage()))
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
