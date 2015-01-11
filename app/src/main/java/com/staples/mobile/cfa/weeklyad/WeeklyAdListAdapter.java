package com.staples.mobile.cfa.weeklyad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.WeeklyAdImageUrlHelper;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;

public class WeeklyAdListAdapter extends ArrayAdapter<Data> {

    private LayoutInflater inflater;
    private Context context;

    public WeeklyAdListAdapter(Context context) {
        super(context, R.layout.weekly_ad_by_categories_item);
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.weekly_ad_list_item, parent, false);
        }

        Data data = getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.weekly_ad_list_item_image);

        Picasso.with(context)
                .load(WeeklyAdImageUrlHelper.getUrl(60, 100, data.getImage()))
                .into(imageView);

        TextView titleTextView = (TextView) convertView.findViewById(R.id.weekly_ad_list_item_title_text);
        titleTextView.setText(data.getTitle());

        TextView priceTextView = (TextView) convertView.findViewById(R.id.weekly_ad_list_item_price_text);
        priceTextView.setText(data.getPrice());

        return convertView;
    }
}
