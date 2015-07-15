package app.staples.mobile.cfa.skuset;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.easyopen.model.browse.Pricing;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.bundle.BundleItem;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.widget.IndicatorBlock;
import app.staples.mobile.cfa.widget.PriceSticker;
import app.staples.mobile.cfa.widget.RatingStars;

public class SkuSetAdapter extends RecyclerView.Adapter<SkuSetAdapter.ViewHolder> {
    private static final String TAG = SkuSetAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;
        private PriceSticker priceSticker;
        private RatingStars ratingStars;
        private IndicatorBlock indicators;

        private ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
            ratingStars = (RatingStars) view.findViewById(R.id.rating);
            indicators = (IndicatorBlock) view.findViewById(R.id.indicators);
        }
    }

    private LayoutInflater inflater;
    private ArrayList<BundleItem> array;
    private Picasso picasso;
    private Drawable noPhoto;
    private View.OnClickListener listener;

    public SkuSetAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<BundleItem>();
        picasso = Picasso.with(context);
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
        else picasso.load(item.imageUrl).error(noPhoto).into(vh.image);
        vh.title.setText(item.title);
        if (item.rebatePrice>0.0f) {
            vh.priceSticker.setPricing(item.finalPrice+item.rebatePrice, item.wasPrice, item.unit, "*");
        } else {
            vh.priceSticker.setPricing(item.finalPrice, item.wasPrice, item.unit, null);
        }
        vh.ratingStars.setRating(item.customerRating, item.customerCount);

        // Set indicators
        vh.indicators.reset();
        if (item.rebatePrice>0.0f) {
            vh.indicators.addPricedIndicator(item.rebatePrice, R.string.indicator_rebate, R.color.staples_red, 0);
        }
        if (item.addOnBasketPrice>0.0f) {
            vh.indicators.addPricedIndicator(item.addOnBasketPrice, R.string.indicator_minimum, R.color.staples_blue, R.layout.explain_minimum);
        }
        if (item.extraShippingCharge>0.0f) {
            vh.indicators.addPricedIndicator(item.extraShippingCharge, R.string.indicator_oversized, R.color.staples_blue, R.layout.explain_oversized);
        }
        if (vh.indicators.isInfoAvailable()) {
            vh.indicators.addIcon(R.drawable.ic_info_outline_blue_18dp);
            vh.indicators.enableExplainDialog();
        }
    }

    public int fill(List<Product> products) {
        if (products==null) return(0);
        int count = 0;
        for (Product product : products) {
            String name = MiscUtils.cleanupHtml(product.getProductName());
            BundleItem item = new BundleItem(count, name, product.getSku());
            item.setImageUrl(product.getImage());

            List<Pricing> pricings = product.getPricing();
            if (pricings!=null && pricings.size()>0) {
                item.processPricing(pricings.get(0));
            }

            array.add(item);
            count++;
        }
        notifyDataSetChanged();
        return(count);
    }
}
