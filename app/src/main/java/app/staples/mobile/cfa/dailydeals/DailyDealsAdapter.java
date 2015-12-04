package app.staples.mobile.cfa.dailydeals;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.easyopen.model.dailydeals.Details;
import com.staples.mobile.common.access.easyopen.model.dailydeals.Products;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.widget.DataWrapper;
import app.staples.mobile.cfa.widget.PriceSticker;
import app.staples.mobile.cfa.widget.RatingStars;

public class DailyDealsAdapter extends RecyclerView.Adapter<DailyDealsAdapter.ViewHolder> implements DataWrapper.Layoutable {

    private static final String TAG = DailyDealsAdapter.class.getSimpleName();

    public interface OnFetchMoreData {
        void onFetchMoreData();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;
        private TextView promoMessage;
        private RatingStars ratingStars;
        private PriceSticker priceSticker;
        private ImageView action;
        private View whirlie;

        private ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            promoMessage = (TextView) view.findViewById(R.id.deal_promo_message);
            ratingStars = (RatingStars) view.findViewById(R.id.rating);
            priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
            action = (ImageView) view.findViewById(R.id.bundle_action);
            whirlie = view.findViewById(R.id.bundle_whirlie);
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<DailyDealsItem> array;
    private OnFetchMoreData fetcher;
    private int threshold;
    private View.OnClickListener listener;
    private int layout;
    private Picasso picasso;
    private int imageWidth;
    private int imageHeight;
    private Drawable noPhoto;
    private NumberFormat format;

    public DailyDealsAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<DailyDealsItem>();
        picasso = Picasso.with(context);
        Resources res = context.getResources();
        imageWidth = res.getDimensionPixelSize(R.dimen.image_square_size);
        imageHeight = res.getDimensionPixelSize(R.dimen.image_square_size);
        noPhoto = res.getDrawable(R.drawable.no_photo);
        format = MiscUtils.getCurrencyFormat();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setLayout(DataWrapper.LayoutMode mode) {
        if (mode==DataWrapper.LayoutMode.TALL)
            layout = R.layout.daily_deal_item_tall;
        else
            layout = R.layout.daily_deal_item_wide;
    }

    public void setOnFetchMoreData(OnFetchMoreData fetcher, int threshold) {
        this.fetcher = fetcher;
        this.threshold = threshold;
    }

    // Needed for analytics.
    public int getItemPosition(DailyDealsItem item) {
        return array.indexOf(item);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    @Override
    public int getItemViewType(int position) {
        return(layout);
    }

    @Override
    public DailyDealsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(layout, parent, false);
        ViewHolder vh = new ViewHolder(view);

        // Set onClickListeners
        vh.itemView.setOnClickListener(listener);
        vh.action.setOnClickListener(listener);
        return vh;
    }

    @Override
    public void onBindViewHolder(DailyDealsAdapter.ViewHolder holder, int position) {
        DailyDealsItem item = array.get(position);

        // Set tag for onClickListeners
        holder.itemView.setTag(item);
        holder.action.setTag(item);

        // Load image
        String imageUrl= item.details.getProductImage();
        if (imageUrl==null) {
            holder.image.setImageDrawable(noPhoto);
        } else {
            picasso.load(imageUrl).error(noPhoto).resize(imageWidth, imageHeight).centerInside().into(holder.image);
        }
        holder.title.setText(item.name);
        holder.ratingStars.setRating((float)item.details.getRating()/10, item.details.getNumberOfReviews());

        if(null != item.details.getDealPromoMessage()) {
            holder.priceSticker.setVisibility(View.GONE);
            holder.promoMessage.setVisibility(View.VISIBLE);
            holder.promoMessage.setText(item.details.getDealPromoMessage());
        }
        else {
            holder.priceSticker.setVisibility(View.VISIBLE);
            holder.promoMessage.setVisibility(View.GONE);
            holder.priceSticker.setPricing(item.details.getFinalPrice(), item.details.getListPrice(), item.details.getUnitOfMeasure(), null);
        }

        if (item.busy) {
            holder.action.setVisibility(View.GONE);
            holder.whirlie.setVisibility(View.VISIBLE);
        } else {
            holder.action.setVisibility(View.VISIBLE);
            holder.whirlie.setVisibility(View.GONE);
        }

        if (null != item.details.getSkuSetType())
            holder.action.setImageResource(R.drawable.ic_more_vert_black);
        else
            holder.action.setImageResource(R.drawable.ic_add_black);

        // Need to get more data?
        if (fetcher!=null && position>threshold) {
            fetcher.onFetchMoreData();
            fetcher = null;
            threshold = 0;
        }
    }

    public void clear() {
        array.clear();
    }

    public int fill(List<Products> products,String identifier) {
        if (products==null) return(0);
        int count = 0;
        for (Products product : products) {
            String name = MiscUtils.cleanupHtml(product.getName());
            int soldCount = product.getSoldCount();
            String endDate = product.getEndDate();
            Details details = product.getDetails();
            DailyDealsItem item = new DailyDealsItem(name,soldCount,endDate,details,identifier);
            array.add(item);
            count++;
        }
        return(count);
    }
}