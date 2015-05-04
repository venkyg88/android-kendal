package app.staples.mobile.cfa.skuset;

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
import app.staples.mobile.cfa.R;
import app.staples.mobile.cfa.bundle.BundleItem;
import app.staples.mobile.cfa.widget.PriceSticker;
import app.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.ArrayList;
import java.util.List;

public class SkuSetAdapter extends RecyclerView.Adapter<SkuSetAdapter.ViewHolder> {
    private static final String TAG = SkuSetAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;
        private PriceSticker priceSticker;
        private RatingStars ratingStars;

        private ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
            ratingStars = (RatingStars) view.findViewById(R.id.rating);
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<BundleItem> array;
    private View.OnClickListener listener;
    private Drawable noPhoto;

    public SkuSetAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<BundleItem>();
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
        BundleItem item = array.get(position);

        // Set tag for onClickListeners
        vh.itemView.setTag(item);

        // Set content
        if (item.imageUrl == null) vh.image.setImageDrawable(noPhoto);
        else Picasso.with(context).load(item.imageUrl).error(noPhoto).into(vh.image);
        vh.title.setText(item.title);
        vh.priceSticker.setPricing(item.finalPrice, item.wasPrice, item.unit, item.rebateIndicator);
        vh.ratingStars.setRating(item.customerRating, item.customerCount);
    }

    public int fill(List<Product> products) {
        if (products==null) return(0);
        int count = 0;
        for (Product product : products) {
            String name = Html.fromHtml(product.getProductName()).toString();
            BundleItem item = new BundleItem(products.size(), name, product.getSku());
            item.setPrice(product.getPricing());
            item.setImageUrl(product.getImage());
            array.add(item);
            count++;
        }
        notifyDataSetChanged();
        return(count);
    }
}
