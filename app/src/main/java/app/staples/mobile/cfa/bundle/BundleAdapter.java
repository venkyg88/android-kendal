package app.staples.mobile.cfa.bundle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.easyopen.model.browse.Pricing;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.IdentifierType;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.widget.DataWrapper;
import app.staples.mobile.cfa.widget.PriceSticker;
import app.staples.mobile.cfa.widget.RatingStars;

public class BundleAdapter extends RecyclerView.Adapter<BundleAdapter.ViewHolder> implements DataWrapper.Layoutable {
    private static final String TAG = BundleAdapter.class.getSimpleName();

    private static final String SCENE7SIGNATURE = "/s7/is/";

    public interface OnFetchMoreData {
        void onFetchMoreData();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;
        private RatingStars ratingStars;
        private PriceSticker priceSticker;
        private TextView overweight;
        private TextView addon;
        private TextView rebateNote;
        private TextView rebateText;
        private ImageView action;
        private View whirlie;

        private ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            ratingStars = (RatingStars) view.findViewById(R.id.rating);
            priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
            overweight = (TextView) view.findViewById(R.id.overweight);
            addon = (TextView) view.findViewById(R.id.addon);
            rebateNote = (TextView) view.findViewById(R.id.rebate_note);
            rebateText = (TextView)view.findViewById(R.id.rebate_text);
            action = (ImageView) view.findViewById(R.id.bundle_action);
            whirlie = view.findViewById(R.id.bundle_whirlie);
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<BundleItem> array;
    private OnFetchMoreData fetcher;
    private int threshold;
    private View.OnClickListener listener;
    private int layout;
    private Picasso picasso;
    private int imageWidth;
    private int imageHeight;
    private Drawable noPhoto;
    private NumberFormat format;

    public BundleAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<BundleItem>();
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
        if (mode==DataWrapper.LayoutMode.TALL) layout = R.layout.bundle_item_tall;
        else layout = R.layout.bundle_item_wide;
    }

    public void setOnFetchMoreData(OnFetchMoreData fetcher, int threshold) {
        this.fetcher = fetcher;
        this.threshold = threshold;
    }

    /** needed for analytics */
    public int getItemPosition(BundleItem item) {
        return array.indexOf(item);
    }

    @Override
    public int getItemCount() {
        return(array.size());
    }

    @Override
    public int getItemViewType(int position) {
        return(layout);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(layout, parent, false);
        ViewHolder vh = new ViewHolder(view);

        // Set onClickListeners
        vh.itemView.setOnClickListener(listener);
        vh.action.setOnClickListener(listener);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        BundleItem item = array.get(position);

        // Set tag for onClickListeners
        vh.itemView.setTag(item);
        vh.action.setTag(item);

        // Load image
        String imageUrl = item.imageUrl;
        if (imageUrl==null) {
            vh.image.setImageDrawable(noPhoto);
        } else {
            if (imageUrl.contains(SCENE7SIGNATURE) &&
                !imageUrl.contains("?")) {
                imageUrl = imageUrl + "?$std$";
            }
            picasso.load(imageUrl).error(noPhoto).resize(imageWidth, imageHeight).centerInside().into(vh.image);
        }

        // Set content
        vh.title.setText(item.title);
        vh.ratingStars.setRating(item.customerRating, item.customerCount);
        vh.priceSticker.setPricing(item.finalPrice, item.wasPrice, item.unit, item.rebateIndicator);
        if(item.rebateIndicator!=null) {
            vh.rebateNote.setVisibility(View.VISIBLE);
            vh.rebateText.setVisibility(View.VISIBLE);
            vh.rebateText.setText(format.format(item.rebatePrice) + " Rebate");
        } else {
            vh.rebateNote.setVisibility(View.GONE);
            vh.rebateText.setVisibility(View.GONE);
        }
        vh.addon.setVisibility((item.isAddOnItem) ? View.VISIBLE : View.GONE);
        vh.overweight.setVisibility((item.isHeavyWeight) ? View.VISIBLE : View.GONE);
        if (item.busy) {
            vh.action.setVisibility(View.GONE);
            vh.whirlie.setVisibility(View.VISIBLE);
        } else {
            vh.action.setVisibility(View.VISIBLE);
            vh.whirlie.setVisibility(View.GONE);
        }

        if (item.type== IdentifierType.SKUSET) vh.action.setImageResource(R.drawable.ic_more_vert_black);
        else vh.action.setImageResource(R.drawable.ic_add_black);

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

    public void fill(List<Product> products) {
        if (products==null) return;
        for (Product product : products) {
            String name = MiscUtils.cleanupHtml(product.getProductName());
            BundleItem item = new BundleItem(array.size(), name, product.getSku());
            item.setImageUrl(product.getImage());
            item.customerRating = product.getCustomerReviewRating();
            item.customerCount = product.getCustomerReviewCount();
            item.isAddOnItem = MiscUtils.parseBoolean(product.getAddOnSku(), false);
            item.isHeavyWeight = MiscUtils.parseBoolean(product.getHeavyWeightSku(), false);

            List<Pricing> pricings = product.getPricing();
            if (pricings!=null && pricings.size()>0) {
                item.setPrice(pricings);
                Pricing pricing = pricings.get(0);
                if (pricing != null) {
                    item.setRebatePrice(pricing.getDiscount());
                    item.heavyWeightShippingCharge = pricing.getHeavyWeightShipCharge();
                }
            }
            array.add(item);
        }
    }

    public void sort(Comparator<BundleItem> comparator) {
        Collections.sort(array, comparator);
    }
}
