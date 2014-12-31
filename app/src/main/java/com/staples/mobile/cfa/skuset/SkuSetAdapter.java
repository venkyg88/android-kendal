package com.staples.mobile.cfa.skuset;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.browse.Image;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.ArrayList;
import java.util.List;

public class SkuSetAdapter extends RecyclerView.Adapter<SkuSetAdapter.ViewHolder> {
    private static final String TAG = "SkuSetAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;

        private ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
        }
    }

    public static class Item
    {
        public String title;
        public String identifier;
        public String imageUrl;

        private Item(String title, String identifier) {
            this.title = title;
            this.identifier = identifier;
        }
        public String setImageUrl(List<Image> images) {
            if (images==null) return(null);
            for(Image image : images) {
                String url = image.getUrl();
                if (url!=null) {
                    imageUrl = url;
                    return(imageUrl);
                }
            }
            return(null);
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Item> array;
    private View.OnClickListener listener;
    private Drawable noPhoto;

    public SkuSetAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<Item>();
        noPhoto = context.getResources().getDrawable(R.drawable.no_photo);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return(array.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(R.layout.skuset_item, parent, false);
        ViewHolder vh = new ViewHolder(view);

        // Set onClickListeners
        vh.itemView.setOnClickListener(listener);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Item item = array.get(position);

        // Set tag for onClickListeners
        vh.itemView.setTag(item);

        // Set content
        if (item.imageUrl == null) vh.image.setImageDrawable(noPhoto);
        else Picasso.with(context).load(item.imageUrl).error(noPhoto).into(vh.image);
        vh.title.setText(item.title);
    }

    public int fill(List<Product> products) {
        if (products==null) return(0);
        int count = 0;
        for (Product product : products) {
            String name = Html.fromHtml(product.getProductName()).toString();
            Item item = new Item(name, product.getSku());
            item.setImageUrl(product.getImage());
            array.add(item);
            count++;
        }
        notifyDataSetChanged();
        return(count);
    }
}
