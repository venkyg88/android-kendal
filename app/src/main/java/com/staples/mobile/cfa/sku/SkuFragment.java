package com.staples.mobile.cfa.sku;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.LoginHelper;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PagerStripe;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.model.browse.Availability;
import com.staples.mobile.common.access.easyopen.model.browse.BulletDescription;
import com.staples.mobile.common.access.easyopen.model.browse.Description;
import com.staples.mobile.common.access.easyopen.model.browse.Image;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuFragment extends Fragment implements Callback<SkuDetails>, TabHost.OnTabChangeListener,
                                                     ViewPager.OnPageChangeListener, View.OnClickListener, FragmentManager.OnBackStackChangedListener {
    private static final String TAG = "SkuFragment";

    private static final String DESCRIPTION =" Description";
    private static final String SPECIFICATIONS =" Specifications";
    private static final String REVIEWS = "Reviews";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
//    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;

    private String identifier;

    private DataWrapper wrapper;
    private ViewGroup summary;

    // Image ViewPager
    private ViewPager imagePager;
    private SkuImageAdapter imageAdapter;
    private PagerStripe stripe;

    // Tab ViewPager
    private TabHost details;
    private ViewPager tabPager;
    private SkuTabAdapter tabAdapter;

    private boolean shifted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        Bundle args = getArguments();
        if (args!=null) {
            identifier = args.getString("identifier");
        }

        wrapper = (DataWrapper) inflater.inflate(R.layout.sku_summary, container, false);
        summary = (ViewGroup) wrapper.findViewById(R.id.summary);
        Resources res = getActivity().getResources();

        // Init image pager
        imagePager = (ViewPager) summary.findViewById(R.id.images);
        imageAdapter = new SkuImageAdapter(getActivity());
        imagePager.setAdapter(imageAdapter);
        stripe = (PagerStripe) summary.findViewById(R.id.stripe);
        imagePager.setOnPageChangeListener(stripe);

        // Init details (ViewPager)
        tabPager = (ViewPager) wrapper.findViewById(R.id.pager);
        tabAdapter = new SkuTabAdapter(getActivity());
        tabPager.setAdapter(tabAdapter);

        // Fill detail (View Pager)
        tabAdapter.add(res.getString(R.string.description));
        tabAdapter.add(res.getString(R.string.specs));
        tabAdapter.add(res.getString(R.string.reviews));
        tabAdapter.notifyDataSetChanged();

        // Init details (TabHost)
        details = (TabHost) wrapper.findViewById(R.id.details);
        details.setup();

        // Fill details (TabHost)
        DummyFactory dummy = new DummyFactory(getActivity());
        addTab(dummy, res, R.string.description, DESCRIPTION);
        addTab(dummy, res, R.string.specs, SPECIFICATIONS);
        addTab(dummy, res, R.string.reviews, REVIEWS);

        // Set initial visibility
        wrapper.setState(DataWrapper.State.LOADING);
        details.setVisibility(View.GONE);

        // Disable add-to-cart
        wrapper.findViewById(R.id.quantity).setEnabled(false);
        wrapper.findViewById(R.id.add_to_cart).setEnabled(false);

        // Set listeners
        details.setOnTabChangedListener(this);
        tabPager.setOnPageChangeListener(this);
        summary.findViewById(R.id.description_detail).setOnClickListener(this);
        summary.findViewById(R.id.specification_detail).setOnClickListener(this);
        summary.findViewById(R.id.review_detail).setOnClickListener(this);
        wrapper.findViewById(R.id.add_to_cart).setOnClickListener(this);

        Access.getInstance().getEasyOpenApi(false).getSkuDetails(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                                                              ZIPCODE, CLIENT_ID, null, MAXFETCH, this);

        return(wrapper);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentManager manager = getFragmentManager();
        manager.removeOnBackStackChangedListener(this);
    }

    public static class DummyFactory implements TabHost.TabContentFactory {
        private View view;

        public DummyFactory(Context context) {
            view = new View(context);
        }

        @Override
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

    public static boolean buildDescription(LayoutInflater inflater, ViewGroup parent, Product product, int limit) {
        boolean described = false;
        int count = 0;

        // Add descriptions
        List<Description> paragraphs = product.getParagraph();
        if (paragraphs!=null) {
            for(Description paragraph : paragraphs) {
                 String text = paragraph.getText();
                 if (text!=null) {
                     TextView item = (TextView) inflater.inflate(R.layout.sku_paragraph_item, parent, false);
                     parent.addView(item);
                     item.setText(text);
                     item.setMaxLines(limit);
                     described = true;
                 }
            }
        }

        // Add bullets
        List<BulletDescription> bullets = product.getBulletDescription();
        if (bullets!=null) {
            for(BulletDescription bullet : bullets) {
                if (count>=limit) break;
                String text = bullet.getText();
                if (text != null) {
                    View item = inflater.inflate(R.layout.sku_bullet_item, parent, false);
                    parent.addView(item);
                    ((TextView) item.findViewById(R.id.bullet)).setText(text);
                    count++;
                }
            }
        }

        return(described || count>0);
    }

    public static boolean buildSpecifications(LayoutInflater inflater, ViewGroup parent, Product product, int limit) {
        ViewGroup table = null;
        int count = 0;

        // Add specification pairs
        List<Description> specs = product.getSpecification();
        if (specs!=null) {
            for(Description spec : specs) {
                if (count>=limit) break;
                String name = spec.getName();
                String text = spec.getText();
                if (name!=null && text!=null) {
                    if (table==null) {
                        table = (ViewGroup) inflater.inflate(R.layout.sku_spec_table, parent, false);
                        parent.addView(table);
                    }
                    View item = inflater.inflate(R.layout.sku_spec_item, table, false);
                    table.addView(item);
                    if ((count&1)!=0) item.setBackgroundColor(0xffdddddd);
                    ((TextView) item.findViewById(R.id.name)).setText(name);
                    ((TextView) item.findViewById(R.id.value)).setText(text);
                    count++;
                }
            }
        }
        return(count>0);
    }

    // Retrofit callbacks

    @Override
    public void success(SkuDetails sku, Response response) {
        List<Product> products = sku.getProduct();
        if (products!=null && products.size()>0) {
            // Use the first product in the list
            Product product = products.get(0);
            tabAdapter.setProduct(product);

            // Handle availability
            QuantityEditor edit = (QuantityEditor) wrapper.findViewById(R.id.quantity);
            Button button = (Button) wrapper.findViewById(R.id.add_to_cart);
            Availability availability = Availability.getProductAvailability(product);
            switch(availability) {
                case NOTHING:
                case SKUSET:
                case RETAILONLY:
                case SPECIALORDER:
                case OUTOFSTOCK:
                    edit.setVisibility(View.GONE);
                    button.setText(availability.getTextResId());
                    button.setEnabled(false);
                    break;
                case INSTOCK:
                    edit.setVisibility(View.VISIBLE);
                    edit.setQtyValue(1);
                    button.setText(R.string.add_to_cart);
                    button.setEnabled(true);
                    break;
            }

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
            ((TextView) summary.findViewById(R.id.title)).setText(product.getProductName());
            ((TextView) summary.findViewById(R.id.numbers)).setText(formatNumbers(product));
            ((RatingStars) summary.findViewById(R.id.rating)).setRating(product.getCustomerReviewRating(), product.getCustomerReviewCount());

            // Add pricing
            ((PriceSticker) summary.findViewById(R.id.pricing)).setPricing(product.getPricing());

            // Add description
            LayoutInflater inflater = getActivity().getLayoutInflater();
            if (!buildDescription(inflater, (ViewGroup) summary.findViewById(R.id.description), product, 3))
                summary.findViewById(R.id.description_detail).setVisibility(View.GONE);

            // Add specifications
            if (!buildSpecifications(inflater, (ViewGroup) summary.findViewById(R.id.specifications), product, 3))
                summary.findViewById(R.id.specification_detail).setVisibility(View.GONE);

            // Add reviews
//            summary.findViewById(R.id.review_detail).setVisibility(View.GONE);

            // Ready to display
            wrapper.setState(DataWrapper.State.DONE);
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        wrapper.setState(DataWrapper.State.EMPTY);
    }

   // TabHost notifications

    public void onTabChanged(String tag) {
        int index;

        // Get index
        if (tag.equals(DESCRIPTION)) index = 0;
        else if (tag.equals(SPECIFICATIONS)) index = 1;
        else if (tag.equals(REVIEWS)) index = 2;
        else throw(new RuntimeException("Unknown tag from TabHost"));

        tabPager.setCurrentItem(index);
    }

    // ViewPager notifications

    public void onPageScrollStateChanged(int state) {
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        details.setCurrentTab(position);
    }

    // Detail and add-to-cart clicks

    private void shiftToDetail(int position) {
        if (shifted) return;
        wrapper.setState(DataWrapper.State.GONE);
        details.setVisibility(View.VISIBLE);
        shifted = true;
        tabPager.setCurrentItem(position);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.addToBackStack("SkuDetail");
        transaction.commit();
        manager.addOnBackStackChangedListener(this);
    }

    @Override
    public void	onBackStackChanged() {
        FragmentManager manager = getFragmentManager();
        int n = manager.getBackStackEntryCount();
        if (n<1) return;
        String name = manager.getBackStackEntryAt(n-1).getName();
        if (name!=null && name.equals("SkuDetail")) return;
        manager.removeOnBackStackChangedListener(this);
        wrapper.setState(DataWrapper.State.DONE);
        details.setVisibility(View.GONE);
        shifted = false;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.description_detail:
                shiftToDetail(0);
                break;
            case R.id.specification_detail:
                shiftToDetail(1);
                break;
            case R.id.review_detail:
                shiftToDetail(2);
                break;
            case R.id.add_to_cart:
                QuantityEditor edit = (QuantityEditor) wrapper.findViewById(R.id.quantity);
                int qty = edit.getQtyValue(1);
                MainActivity activity = (MainActivity) getActivity();
                activity.addItemToCart(identifier, qty);
                break;
        }
    }
}
