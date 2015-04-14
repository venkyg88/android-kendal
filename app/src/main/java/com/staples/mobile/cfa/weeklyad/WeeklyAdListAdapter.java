package com.staples.mobile.cfa.weeklyad;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;

import java.util.ArrayList;
import java.util.List;

public class WeeklyAdListAdapter extends RecyclerView.Adapter<WeeklyAdListAdapter.ViewHolder> {
    private static String TAG = WeeklyAdListAdapter.class.getSimpleName();

    public class Item {
        public String title;
        public String identifier;
        public String imageUrl;
        public String description;
        public boolean inStoreOnly;
        public float finalPrice;
        public String unit;
        public boolean busy;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public PriceSticker priceSticker;
        public TextView availability;
        ImageView image;
        View action;
        View whirlie;

        public ViewHolder(View view) {
            super(view);
            title = (TextView)view.findViewById(R.id.title);
            priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
            availability = (TextView)view.findViewById(R.id.availability);
            image = (ImageView)view.findViewById(R.id.image);
            action = view.findViewById(R.id.action);
            whirlie = view.findViewById(R.id.whirlie);
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Item> array;
    View.OnClickListener listener;

    public WeeklyAdListAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<Item>();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.weekly_ad_list_item, parent, false);
        ViewHolder vh = new ViewHolder(view);

        view.setOnClickListener(listener);
        vh.action.setOnClickListener(listener);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Item item = array.get(position);

        // Set tag for onClickListeners
        vh.itemView.setTag(item);
        vh.action.setTag(item);

        vh.title.setText(item.title);
        vh.priceSticker.setPricing(item.finalPrice, 0.0f, item.unit, null);

        if (item.inStoreOnly || item.identifier==null) {
            vh.availability.setVisibility(View.VISIBLE);
            vh.action.setVisibility(View.GONE);
            vh.whirlie.setVisibility(View.GONE);
        } else if (item.busy) {
            vh.availability.setVisibility(View.GONE);
            vh.action.setVisibility(View.GONE);
            vh.whirlie.setVisibility(View.VISIBLE);
        } else {
            vh.availability.setVisibility(View.GONE);
            vh.action.setVisibility(View.VISIBLE);
            vh.whirlie.setVisibility(View.GONE);
        }

        Picasso.with(context).load(item.imageUrl).into(vh.image);
    }

    public void fill(List<Data> datas) {
        array.clear();
        for(Data data : datas) {
            Item item = new Item();
            item.title = data.getTitle();
            item.identifier = data.getRetailerproductcode();
            String text = data.getPrice();
            if (text!=null) {
                if (text.startsWith("$"))
                    text = text.substring(1);
                try {
                    item.finalPrice = Float.parseFloat(text);
                } catch(NumberFormatException e) {
                    item.finalPrice = 6.66f;
                }
            }
Log.d(TAG, "Qual#"+data.getPricequalifier()+"#");
            item.unit = data.getPricequalifier();
            item.inStoreOnly = data.getFineprint().contains("In store only");
            item.imageUrl = data.getImage();
            array.add(item);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        array.clear();
        notifyDataSetChanged();
    }
}
