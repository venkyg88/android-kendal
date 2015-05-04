package app.staples.mobile.cfa.weeklyad;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import app.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;

import java.util.ArrayList;
import java.util.List;

public class  WeeklyAdListAdapter extends RecyclerView.Adapter<WeeklyAdListAdapter.ViewHolder> {
    private static String TAG = WeeklyAdListAdapter.class.getSimpleName();

    public static class Item {
        public String title;
        public String identifier;
        public String imageUrl;
        public String description;
        public String buyNow;
        public boolean inStoreOnly;
        public float finalPrice;
        public String unit;
        public String literal;
        public boolean busy;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView priceSticker;
        public TextView availability;
        public TextView pricingUnit;
        ImageView image;
        View action;
        View whirlie;

        public ViewHolder(View view) {
            super(view);
            title = (TextView)view.findViewById(R.id.title);
            priceSticker = (TextView) view.findViewById(R.id.pricing);
            pricingUnit = (TextView) view.findViewById(R.id.pricing_unit);
            availability = (TextView)view.findViewById(R.id.availability);
            image = (ImageView)view.findViewById(R.id.image);
            action = view.findViewById(R.id.action);
            whirlie = view.findViewById(R.id.whirlie);
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Item> array;
    private View.OnClickListener listener;
    private String each;

    public WeeklyAdListAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<Item>();
        each = context.getResources().getString(R.string.each);
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
        if (item.literal!=null) {
            vh.priceSticker.setText(item.literal);
        } else {
            vh.priceSticker.setText("$" + item.finalPrice);
            vh.pricingUnit.setText(item.unit);

        }

        if (item.inStoreOnly) {
            vh.availability.setVisibility(View.VISIBLE);
            vh.action.setVisibility(View.GONE);
            vh.whirlie.setVisibility(View.GONE);
        }
        else if( item.buyNow == ""){
            vh.availability.setVisibility(View.GONE);
            vh.action.setVisibility(View.GONE);
            vh.whirlie.setVisibility(View.GONE);
        }  else if (item.identifier==null) {
            vh.availability.setVisibility(View.GONE);
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
            item.description = data.getDescription();
            item.buyNow = data.getBuynow();
            if (item.description==null || item.description.isEmpty()) {
                item.description = item.title;
            }
            String sku = data.getRetailerproductcode();
            if (sku!=null && !sku.isEmpty()) {
                item.identifier = sku;
            }
            String text = data.getPrice();
            if (text!=null) {
                if (text.startsWith("$")) {
                    try {
                        item.finalPrice = Float.parseFloat(text.substring(1));
                        item.unit = each;
                    } catch(NumberFormatException e) {
                        item.literal = text;
                    }
                } else {
                    item.literal = text;
                }
            }
            item.imageUrl = data.getImage();
            item.inStoreOnly = data.getFineprint().contains("In store only");
            array.add(item);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        array.clear();
        notifyDataSetChanged();
    }
}
