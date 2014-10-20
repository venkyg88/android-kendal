package com.staples.mobile.cfa.sku;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
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

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuFragment extends Fragment implements Callback<Sku>, TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener ,View.OnClickListener {
    private static final String TAG = "SkuFragment";

    private static final String DESCRIPTION =" Description";
    private static final String SPECIFICATIONS =" Specifications";
    private static final String REVIEWS = "Reviews";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private DataWrapper wrapper;
    private String identifier;

    // Image ViewPager
    private ViewPager imagePager;
    private SkuImageAdapter imageAdapter;
    private PagerStripe stripe;

    // Tab ViewPager
    private TabHost details;
    private ViewPager tabPager;

    private boolean shifted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        Bundle args = getArguments();
        if (args!=null) {
            identifier = args.getString("identifier");
        }

        View frame = inflater.inflate(R.layout.sku_frame, container, false);
        wrapper = (DataWrapper) frame.findViewById(R.id.wrapper);
        Resources res = getActivity().getResources();

        // Init image pager
        imagePager = (ViewPager) frame.findViewById(R.id.images);
        imageAdapter = new SkuImageAdapter(getActivity());
        imagePager.setAdapter(imageAdapter);
        stripe = (PagerStripe) frame.findViewById(R.id.stripe);
        imagePager.setOnPageChangeListener(stripe);

        // Init details (ViewPager)
        tabPager = (ViewPager) frame.findViewById(R.id.pager);
        SkuTabAdapter tabAdapter = new SkuTabAdapter(getActivity());
        tabPager.setAdapter(tabAdapter);

        // Fill detail (View Pager)
        tabAdapter.add(res.getString(R.string.description));
        tabAdapter.add(res.getString(R.string.specs));
        tabAdapter.add(res.getString(R.string.reviews));
        tabAdapter.notifyDataSetChanged();

        tabPager.setOnPageChangeListener(this);

        // Init details (TabHost)
        details = (TabHost) frame.findViewById(R.id.details);
        details.setup();

        // Fill details (TabHost)
        DummyFactory dummy = new DummyFactory(getActivity());
        addTab(dummy, res, R.string.description, DESCRIPTION);
        addTab(dummy, res, R.string.specs, SPECIFICATIONS);
        addTab(dummy, res, R.string.reviews, REVIEWS);

        details.setOnTabChangedListener(this);

        // Set initial visibility
        wrapper.setState(DataWrapper.State.LOADING);
        details.setVisibility(View.GONE);

        frame.findViewById(R.id.description_detail).setOnClickListener(this);
        frame.findViewById(R.id.specification_detail).setOnClickListener(this);
        frame.findViewById(R.id.review_detail).setOnClickListener(this);
        frame.findViewById(R.id.add_to_cart).setOnClickListener(this);

        Access.getInstance().getEasyOpenApi(false).getSkuInfo(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                                                              ZIPCODE, CLIENT_ID, null, MAXFETCH, this);

        return (frame);
    }

    public static class DummyFactory implements TabHost.TabContentFactory {
        private View view;

        public DummyFactory(Context context) {
            view = new View(context);
        }

        public View createTabContent(String tag) {
            return(view);
        }
    }

    private void addTab(TabHost.TabContentFactory dummy, Resources res, int resid, String tag) {
        TabHost.TabSpec tab = details.newTabSpec(tag);
        tab.setIndicator(res.getString(resid));
        tab.setContent(dummy);
        details.addTab(tab);
    }

    private String formatNumbers(Product product) {
        // Safety check
        if (product==null) return(null);
        String skuNumber = product.getSku();
        String modelNumber = product.getManufacturerPartNumber();
        if (skuNumber==null && modelNumber==null) return(null);

        // Skip redundant numbers
        if (skuNumber!=null && modelNumber!=null && skuNumber.equals(modelNumber))
            modelNumber = null;

        Resources res = getActivity().getResources();
        StringBuilder sb = new StringBuilder();

        if (skuNumber!=null) {
            sb.append(res.getString(R.string.item));
            sb.append(":\u00a0");
            sb.append(skuNumber);
        }

        if (modelNumber!=null) {
            if (sb.length()>0) sb.append("   ");
            sb.append(res.getString(R.string.model));
            sb.append(":\u00a0");
            sb.append(modelNumber);
        }
        return(sb.toString());
    }

    public void addBullets(LayoutInflater inflater, ViewGroup parent, Product product, int limit) {
        List<BulletDescription> bullets = product.getBulletDescription();
        if (bullets==null) return;
        for(BulletDescription bullet : bullets) {
            if (limit<=0) return;
            String text = bullet.getText();
            if (text != null) {
                View item = inflater.inflate(R.layout.bullet_item, parent, false);
                ((TextView) item.findViewById(R.id.bullet)).setText(text);
                parent.addView(item);
                limit --;
            }
        }
    }

    @Override
    public void success(Sku sku, Response response) {
        Product[] products = sku.getProduct();
        if (products!=null) {
            Product product = products[0];
            View frame = getView();

            // Add images
            List<Image> images = product.getImage();
            if (images!=null && images.size()>0) {
                for(Image image : images) {
                    String url = image.getUrl();
                    if (url!=null) imageAdapter.add(url);
                }
            }

            // Handle 0, 1, many images
            int n = imageAdapter.getCount();
            if (n==0) imagePager.setVisibility(View.GONE);
            else {
                if (n>1) stripe.setCount(imageAdapter.getCount());
                else stripe.setVisibility(View.GONE);
                imageAdapter.notifyDataSetChanged();
            }

            // Add info
            ((TextView) frame.findViewById(R.id.title)).setText(product.getProductName());
            ((TextView) frame.findViewById(R.id.numbers)).setText(formatNumbers(product));
            ((RatingStars) frame.findViewById(R.id.rating)).setRating(product.getCustomerReviewRating(), product.getCustomerReviewCount());

            // Add pricing
            List<Pricing> pricings = product.getPricing();
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
            LayoutInflater inflater = getActivity().getLayoutInflater();
            addBullets(inflater, (ViewGroup) frame.findViewById(R.id.description), product, 3);

            // Ready to display
            wrapper.setState(DataWrapper.State.DONE);
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        wrapper.setState(DataWrapper.State.EMPTY);
    }

    public void onTabChanged(String tag) {
        int index;

        // Get index
        if (tag.equals(DESCRIPTION)) index = 0;
        else if (tag.equals(SPECIFICATIONS)) index = 1;
        else if (tag.equals(REVIEWS)) index = 2;
        else throw(new RuntimeException("Unknown tag from TabHost"));

        Log.d(TAG, "onTabChanged "+index);
        tabPager.setCurrentItem(index);
    }

    public void onPageScrollStateChanged(int state) {
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected "+position);
        details.setCurrentTab(position);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.description_detail:
                wrapper.setState(DataWrapper.State.GONE);
                details.setVisibility(View.VISIBLE);
                tabPager.setCurrentItem(0);
                break;
            case R.id.specification_detail:
                wrapper.setState(DataWrapper.State.GONE);
                details.setVisibility(View.VISIBLE);
                tabPager.setCurrentItem(1);
                break;
            case R.id.review_detail:
                wrapper.setState(DataWrapper.State.GONE);
                details.setVisibility(View.VISIBLE);
                tabPager.setCurrentItem(2);
                break;
            case R.id.add_to_cart:
                MainActivity activity = (MainActivity) getActivity();
                activity.addItemToCart(identifier);
                break;
        }
    }
}
