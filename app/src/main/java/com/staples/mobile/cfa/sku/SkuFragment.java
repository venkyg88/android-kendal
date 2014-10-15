package com.staples.mobile.cfa.sku;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PagerStripe;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.model.sku.BulletDescription;
import com.staples.mobile.common.access.easyopen.model.sku.Image;
import com.staples.mobile.common.access.easyopen.model.sku.Pricing;
import com.staples.mobile.common.access.easyopen.model.sku.Product;
import com.staples.mobile.common.access.easyopen.model.sku.Sku;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuFragment extends Fragment implements Callback<Sku>, View.OnClickListener {
    private static final String TAG = "SkuFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private DataWrapper wrapper;
    private SkuImageAdapter imageAdapter;
    private PagerStripe stripe;
    private ViewPager details;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String identifier = null;

        Log.d(TAG, "onCreateView()");

        Bundle args = getArguments();
        if (args!=null) {
            identifier = args.getString("identifier");
        }

        View frame = inflater.inflate(R.layout.sku_frame, container, false);
        wrapper = (DataWrapper) frame.findViewById(R.id.wrapper);

        // Init image pager
        ViewPager imagePager = (ViewPager) frame.findViewById(R.id.images);
        imageAdapter = new SkuImageAdapter(getActivity());
        imagePager.setAdapter(imageAdapter);
        stripe = (PagerStripe) frame.findViewById(R.id.stripe);
        imagePager.setOnPageChangeListener(stripe);

        // Init detail pager
        details = (ViewPager) frame.findViewById(R.id.details);
        SkuPageAdapter pageAdapter = new SkuPageAdapter(getActivity());
        details.setAdapter(pageAdapter);

        pageAdapter.add("Description");
        pageAdapter.add("Specification");
        pageAdapter.add("Reviews");
        pageAdapter.notifyDataSetChanged();

        // Set initial visibility
        wrapper.setState(DataWrapper.State.LOADING);
        details.setVisibility(View.GONE);

        frame.findViewById(R.id.pricing).setOnClickListener(this); // TODO Hacked for test

        Access.getInstance().getEasyOpenApi(false).getSkuInfo(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                                                              ZIPCODE, CLIENT_ID, null, MAXFETCH, this);

        return (frame);
    }

    @Override
    public void success(Sku sku, Response response) {
        Product[] products = sku.getProduct();
        if (products!=null) {
            Product product = products[0];
            View frame = getView();

            // Add images
            Image[] images = product.getImage();
            if (images!=null && images.length>0) {
                for(Image image : images) {
                    String url = image.getUrl();
                    if (url!=null) imageAdapter.add(url);
                }
            }
            stripe.setCount(imageAdapter.getCount());
            imageAdapter.notifyDataSetChanged();

            ((TextView) frame.findViewById(R.id.title)).setText(product.getProductName());
            ((RatingStars) frame.findViewById(R.id.rating)).setRating(product.getCustomerReviewRating(), product.getCustomerReviewCount());

            // Add pricing
            Pricing[] pricings = product.getPricing();
            if (pricings!=null) {
                for(Pricing pricing : pricings) {
                    float finalPrice = pricing.getFinalPrice();
                    if (finalPrice>0.0f) {
                        ((PriceSticker) frame.findViewById(R.id.pricing)).setPricing(finalPrice, pricing.getUnitOfMeasure());
                        break;
                    }
                }
            }

            // Add bullets
            BulletDescription[] bullets = product.getBulletDescription();
            if (bullets!=null) {
                Activity activity = getActivity();
                LayoutInflater inflater = activity.getLayoutInflater();
                ViewGroup block = (ViewGroup) frame.findViewById(R.id.description);
                for(BulletDescription bullet : bullets) {
                    String text = bullet.getText();
                    if (text!=null) {
                        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.bullet_item, block, false);
                        ((TextView) group.findViewById(R.id.bullet)).setText(text);
                        block.addView(group);
                    }
                }
            }

            // Ready to display
            wrapper.setState(DataWrapper.State.DONE);
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        wrapper.setState(DataWrapper.State.EMPTY);
    }

    @Override
    public void onClick(View view) {
        wrapper.setState(DataWrapper.State.GONE);
        details.setVisibility(View.VISIBLE);
    }
}
