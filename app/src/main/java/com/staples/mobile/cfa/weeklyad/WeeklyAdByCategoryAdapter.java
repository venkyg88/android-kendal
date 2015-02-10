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
import com.staples.mobile.common.access.easyopen.util.WeeklyAdImageUrlHelper;
import com.staples.mobile.common.access.easyopen.model.weeklyadbycategory.Data;

public class WeeklyAdByCategoryAdapter extends ArrayAdapter<Data> {

    private LayoutInflater inflater;
    private Context context;

    public WeeklyAdByCategoryAdapter(Context context) {
        super(context, R.layout.weekly_ad_by_categories_item);
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.weekly_ad_by_categories_item, parent, false);
        }

        Data data = getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.weekly_ad_category_image);

        Picasso.with(context)
                .load(WeeklyAdImageUrlHelper.getUrl(60,100, data.getImagelocation()))
                .into(imageView);

        TextView titleTextView = (TextView) convertView.findViewById(R.id.weekly_ad_category_title_text);
        titleTextView.setText(data.getName());

        TextView dealsCountTextView = (TextView) convertView.findViewById(R.id.weekly_ad_category_deals_count_text);
        dealsCountTextView.setText(data.getCount() + " deals");

        return convertView;
    }
}
