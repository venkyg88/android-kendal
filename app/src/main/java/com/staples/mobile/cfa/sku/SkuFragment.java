package com.staples.mobile.cfa.sku;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PagerStripe;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.model.sku.BulletDescription;
import com.staples.mobile.common.access.easyopen.model.sku.Description;
import com.staples.mobile.common.access.easyopen.model.sku.Image;
import com.staples.mobile.common.access.easyopen.model.sku.Pricing;
import com.staples.mobile.common.access.easyopen.model.sku.Product;
import com.staples.mobile.common.access.easyopen.model.sku.SkuDetails;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuFragment extends Fragment implements Callback<SkuDetails>, TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener ,View.OnClickListener {
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

    private View frame;
    private DataWrapper wrapper;
    private String identifier;

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

        frame = inflater.inflate(R.layout.sku_summary, container, false);
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
        tabAdapter = new SkuTabAdapter(getActivity());
        tabPager.setAdapter(tabAdapter);

        // Fill detail (View Pager)
        tabAdapter.add(res.getString(R.string.description));
        tabAdapter.add(res.getString(R.string.specs));
        tabAdapter.add(res.getString(R.string.reviews));
        tabAdapter.notifyDataSetChanged();

        // Init details (TabHost)
        details = (TabHost) frame.findViewById(R.id.details);
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
        frame.findViewById(R.id.quantity).setEnabled(false);
        frame.findViewById(R.id.add_to_cart).setEnabled(false);

        // Set listeners
        details.setOnTabChangedListener(this);
        tabPager.setOnPageChangeListener(this);
        frame.findViewById(R.id.description_detail).setOnClickListener(this);
        frame.findViewById(R.id.specification_detail).setOnClickListener(this);
        frame.findViewById(R.id.review_detail).setOnClickListener(this);
        frame.findViewById(R.id.add_to_cart).setOnClickListener(this);

        Access.getInstance().getEasyOpenApi(false).getSkuDetails(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
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

    public static boolean buildDescription(LayoutInflater inflater, ViewGroup parent, Product product, int limit) {
        // Add descriptions
        List<Description> paragraphs = product.getParagraph();
        if (paragraphs!=null) {
            for(Description paragraph : paragraphs) {
                 String text = paragraph.getText();
                 if (text!=null) {
                     TextView item = (TextView) inflater.inflate(R.layout.sku_paragraph_item, parent, false);
                     item.setText(text);
                     item.setMaxLines(limit);
                     parent.addView(item);
                 }
            }
        }

        // Add bullets
        List<BulletDescription> bullets = product.getBulletDescription();
        if (bullets!=null) {
            int count = 0;
            for(BulletDescription bullet : bullets) {
                if (count>=limit) break;
                String text = bullet.getText();
                if (text != null) {
                    View item = inflater.inflate(R.layout.sku_bullet_item, parent, false);
                    ((TextView) item.findViewById(R.id.bullet)).setText(text);
                    parent.addView(item);
                    count++;
                }
            }
        }

        return(parent.getChildCount()>1);
    }

    public static boolean buildSpecifications(LayoutInflater inflater, ViewGroup parent, Product product, int limit) {
        ViewGroup table = null;

        // Add specification pairs
        List<Description> specs = product.getSpecification();
        if (specs!=null) {
            int count = 0;
            for(Description spec : specs) {
                if (count>=limit) break;
                String name = spec.getName();
                String text = spec.getText();
                if (name!=null && text!=null) {
                    if (table==null) {
                        table = (ViewGroup) inflater.inflate(R.layout.sku_spec_table, parent);
                    }
                    View item = inflater.inflate(R.layout.sku_spec_item, table, false);
                    if ((count&1)!=0) item.setBackgroundColor(0xffcccccc);
                    ((TextView) item.findViewById(R.id.name)).setText(name);
                    ((TextView) item.findViewById(R.id.text)).setText(text);
                    table.addView(item);
                    count++;
                }
            }
        }
        return(parent.getChildCount()>1);
    }

    // Retrofit callbacks

    @Override
    public void success(SkuDetails sku, Response response) {
        List<Product> products = sku.getProduct();
        if (products!=null && products.size()>0) {
            // Use the first product in the list
            Product product = products.get(0);
            tabAdapter.setProduct(product);

            // Enable add-to-cart if not a Sku set
            List<Product> skuset = product.getProduct();
            if (skuset==null) {
                frame.findViewById(R.id.quantity).setEnabled(true);
                frame.findViewById(R.id.add_to_cart).setEnabled(true);
            } else if (!product.isInStock()) {
                Button button = (Button) frame.findViewById(R.id.add_to_cart);
                button.setText("Out of stock");
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

            // Add description & specifications
            LayoutInflater inflater = getActivity().getLayoutInflater();
            buildDescription(inflater, (ViewGroup) frame.findViewById(R.id.description), product, 3);
            buildSpecifications(inflater, (ViewGroup) frame.findViewById(R.id.specifications), product, 3);

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

        Log.d(TAG, "onTabChanged "+index);
        tabPager.setCurrentItem(index);
    }

    // ViewPager notifications

    public void onPageScrollStateChanged(int state) {
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected "+position);
        details.setCurrentTab(position);
    }

    // Detail and add-to-cart clicks

    private void closeSoftKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                EditText edit = (EditText) frame.findViewById(R.id.quantity);
                closeSoftKeyboard(edit);
                String text = edit.getText().toString();
                int qty = 1;
                if (text.length()>0) {
                    try {
                        qty = Integer.parseInt(text);
                    } catch(NumberFormatException e) {}
                }
                MainActivity activity = (MainActivity) getActivity();
                activity.addItemToCart(identifier, qty);
                break;
        }
    }
}
