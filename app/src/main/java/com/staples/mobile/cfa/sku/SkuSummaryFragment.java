package com.staples.mobile.cfa.sku;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.model.sku.*;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuSummaryFragment extends Fragment implements Callback<Sku> {
    private static final String TAG = "SkuSummaryFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private SkuImageAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String identifier = null;

        Log.d(TAG, "onCreateView()");

        Bundle args = getArguments();
        if (args!=null) {
            identifier = args.getString("identifier");
        }

        View view = inflater.inflate(R.layout.sku_summary, container, false);
        ViewPager pager = (ViewPager) view.findViewById(R.id.images);
        adapter = new SkuImageAdapter(getActivity());
        pager.setAdapter(adapter);

        Access.getInstance().getEasyOpenApi(false).getSkuInfo(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                                                              ZIPCODE, CLIENT_ID, null, MAXFETCH, this);

        return (view);
    }

    @Override
    public void success(Sku sku, Response response) {
        Product[] products = sku.getProduct();
        if (products!=null) {
            Product product = products[0];
            View view = getView();

            Image[] images = product.getImage();
            if (images!=null && images.length>0) {
                for(Image image : images) {
                    String url = image.getUrl();
                    if (url!=null) adapter.add(url);
                }
            }
            adapter.notifyDataSetChanged();

            ((TextView) view.findViewById(R.id.title)).setText(product.getProductName());
            ((RatingStars) view.findViewById(R.id.rating)).setRating(product.getCustomerReviewRating(), product.getCustomerReviewCount());

            Pricing[] pricings = product.getPricing();
            if (pricings!=null) {
                for(Pricing pricing : pricings) {
                    float finalPrice = pricing.getFinalPrice();
                    if (finalPrice>0.0f) {
                        ((PriceSticker) view.findViewById(R.id.pricing)).setPricing(finalPrice, pricing.getUnitOfMeasure());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
    }
}
