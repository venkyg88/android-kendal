package com.staples.mobile.cfa.sku;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.feed.PersistentSizedArrayList;
import com.staples.mobile.cfa.feed.PersonalFeedSingleton;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PagerStripe;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.BulletDescription;
import com.staples.mobile.common.access.easyopen.model.browse.Description;
import com.staples.mobile.common.access.easyopen.model.browse.Image;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;
import com.staples.mobile.common.access.easyopen.model.reviews.Data;
import com.staples.mobile.common.access.easyopen.model.reviews.ReviewSet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuFragment extends Fragment implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener,
        View.OnClickListener, FragmentManager.OnBackStackChangedListener {
    private static final String TAG = "SkuFragment";

    private static final String IDENTIFIER = "identifier";

    private static final String DESCRIPTION = " Description";
    private static final String SPECIFICATIONS = " Specifications";
    private static final String REVIEWS = "Reviews";

    private static final int MAXFETCH = 50;

    private static SimpleDateFormat iso8601;

    public enum Availability {
        NOTHING      (R.string.avail_nothing),
        SKUSET       (R.string.avail_skuset),
        RETAILONLY   (R.string.avail_retailonly),
        SPECIALORDER (R.string.avail_specialorder),
        INSTOCK      (R.string.avail_instock),
        OUTOFSTOCK   (R.string.avail_outofstock);

        private int resid;

        private Availability(int resid) {
            this.resid = resid;
        }

        public static Availability getProductAvailability(Product product) {
            if (product==null) return(NOTHING);
            if (product.getProduct()!=null) return(SKUSET);
            if (product.isRetailOnly()) return(RETAILONLY);
            if (product.isRetailOnlySpecialOrder()) return(SPECIALORDER);
            if (product.isInStock()) return(INSTOCK);
            return(OUTOFSTOCK);
        }

        public int getTextResId() {
            return(resid);
        }
    }

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

    // Accessory Container
    private LinearLayout accessoryContainer;

    // Spec Table Container
    private TableLayout specContainer;

    private boolean shifted;

    public void setArguments(String identifier) {
        Bundle args = new Bundle();
        args.putString(IDENTIFIER, identifier);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        Bundle args = getArguments();
        if (args != null) {
            identifier = args.getString(IDENTIFIER);
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

        // Init accessory
        accessoryContainer = (LinearLayout) wrapper.findViewById(R.id.accessoryContainer);

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

        // Initiate API calls
        EasyOpenApi api = Access.getInstance().getEasyOpenApi(false);
        api.getSkuDetails(identifier, null, MAXFETCH, new SkuDetailsCallback());
        api.getReviews(identifier, new ReviewSetCallback());

        return (wrapper);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).showActionBar(R.string.staples, R.drawable.ic_search_white, null);
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
            return (view);
        }
    }

    private void addTab(TabHost.TabContentFactory dummy, Resources res, int resid, String tag) {
        TabHost.TabSpec tab = details.newTabSpec(tag);
        tab.setIndicator(res.getString(resid));
        tab.setContent(dummy);
        details.addTab(tab);
    }

    // Formatters and builders

    private String formatNumbers(Product product) {
        // Safety check
        if (product == null) return (null);
        String skuNumber = product.getSku();
        String modelNumber = product.getManufacturerPartNumber();
        if (skuNumber == null && modelNumber == null) return (null);

        // Skip redundant numbers
        if (skuNumber != null && modelNumber != null && skuNumber.equals(modelNumber))
            modelNumber = null;

        Resources res = getActivity().getResources();
        StringBuilder sb = new StringBuilder();

        if (skuNumber != null) {
            sb.append(res.getString(R.string.item));
            sb.append(":\u00a0");
            sb.append(skuNumber);
        }

        if (modelNumber != null) {
            if (sb.length() > 0) sb.append("   ");
            sb.append(res.getString(R.string.model));
            sb.append(":\u00a0");
            sb.append(modelNumber);
        }
        return (sb.toString());
    }

    public static boolean buildDescription(LayoutInflater inflater, ViewGroup parent, Product product, int limit) {
        boolean described = false;
        int count = 0;

        // Add descriptions
        List<Description> paragraphs = product.getParagraph();
        if (paragraphs != null) {
            for (Description paragraph : paragraphs) {
                String text = Html.fromHtml(paragraph.getText()).toString();
                if (text != null) {
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
        if (bullets != null) {
            for (BulletDescription bullet : bullets) {
                if (count >= limit) break;
                String text = Html.fromHtml(bullet.getText()).toString();
                if (text != null) {
                    View item = inflater.inflate(R.layout.sku_bullet_item, parent, false);
                    parent.addView(item);
                    ((TextView) item.findViewById(R.id.bullet)).setText(text);
                    count++;
                }
            }
        }

        return (described || count > 0);
    }

    protected static void addSpecifications(LayoutInflater inflater, ViewGroup parent, Product product, int limit) {
        int rowCount = 0;

        // Add specification
        List<Description> specs = product.getSpecification();
        if (specs != null) {
            TableLayout table = (TableLayout) inflater.inflate(R.layout.sku_spec_table, parent, false);
            parent.addView(table);

            for (Description spec : specs) {
                if (rowCount >= limit) {
                    break;
                }

                String specName = Html.fromHtml(spec.getName()).toString();
                String specValue = Html.fromHtml(spec.getText()).toString();

                if (specName != null && specValue != null) {
                    TableRow skuSpecRow = (TableRow) inflater.inflate(R.layout.sku_spec_item, table, false);
                    table.addView(skuSpecRow);

                    // set dark color for even table rows
                    if ((rowCount % 2) == 0) {
                        skuSpecRow.setBackgroundColor(0xffdddddd);
                    }

                    // Set specification
                    ((TextView) skuSpecRow.findViewById(R.id.specName)).setText(specName);
                    ((TextView) skuSpecRow.findViewById(R.id.specValue)).setText(specValue);

                    rowCount++;
                }
            }
        }
    }

    private void addAccessory(Product product) {
        List<Product> accessories = product.getAccessory();

        LayoutInflater inflater = getActivity().getLayoutInflater();

        for (final Product accessory : accessories) {
            String accessoryImageUrl = accessory.getImage().get(0).getUrl();
            String accessoryTitle = accessory.getProductName();
            final String sku = accessory.getSku();

            View skuAccessoryRow = inflater.inflate(R.layout.sku_accessory_item, null);

            // Set accessory image
            ImageView accessoryImageView = (ImageView) skuAccessoryRow.findViewById(R.id.accessory_image);
            Picasso.with(getActivity()).load(accessoryImageUrl).error(R.drawable.no_photo).into(accessoryImageView);

            // Set listener for accessory image
            accessoryImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).selectSkuItem(sku);
                }
            });

            // Set accessory title
            TextView accessoryTitleTextView = (TextView) skuAccessoryRow.findViewById(R.id.accessory_title);
            accessoryTitleTextView.setText(accessoryTitle);

            // Set listener for accessory title
            accessoryTitleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).selectSkuItem(sku);
                }
            });

            // Set accessory rating
            ((RatingStars) skuAccessoryRow.findViewById(R.id.accessory_rating))
                    .setRating(accessory.getCustomerReviewRating(), accessory.getCustomerReviewCount());

            // Set accessory price
            ((PriceSticker) skuAccessoryRow.findViewById(R.id.accessory_price)).setPricing(accessory.getPricing());

            accessoryContainer.addView(skuAccessoryRow);
        }
    }

    private void saveSeenProduct(Product product){
        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(getActivity());
        HashSet<String> savedSkuSet = feedSingleton.getSavedSkus(getActivity());
        PersistentSizedArrayList<String> savedSeenProductList
                = feedSingleton.getSavedSeenProducts(getActivity());

        // Check if the product was saved before
        if(!savedSkuSet.contains(product.getSku())){
            Log.d(TAG, "Saving seen product's sku: " + product.getProductName());

            savedSeenProductList.addSeenProduct(product.getSku(), getActivity());
        }

        // Update last seen products list's order keeping the last seen item at the end of the list
        else{
            if(savedSeenProductList.size() == 2){
                if(product.getSku().equals(savedSeenProductList.get(0))){
                    Log.d(TAG, "Shift sku list left to keep last seen sku at the end of the list. Size:2");

                    String skuString = "";
                    for(String sku : savedSeenProductList){
                        skuString = sku + "," + skuString;
                    }
                    Log.d(TAG, "Before shift sku list:" + skuString);

                    // Shift savedSeenProductList left from index 1 to 0
                    savedSeenProductList.set(0, savedSeenProductList.get(1));

                    // Set last seen product at the end of the list
                    savedSeenProductList.set(1, product.getSku());

                    // Update the seen products list in singleton
                    feedSingleton.setSavedSeenProducts(savedSeenProductList);

                    // Update updated seen products list in the phone
                    savedSeenProductList.updateSeenProductsInPhone(getActivity());

                    skuString = "";
                    for(String sku : savedSeenProductList){
                        skuString = sku + "," + skuString;
                    }
                    Log.d(TAG, "After shift sku list:" + skuString);
                }
            }
            else if(savedSeenProductList.size() == 3){
                // If current sku equals to the sku in 1st or 2nd index in the sized ArrayList
                if(!product.getSku().equals(savedSeenProductList.get(2))){
                    Log.d(TAG, "Shift sku list left to keep last seen sku at the end of the list. Size:3");

                    String skuString = "";
                    for(String sku : savedSeenProductList){
                        skuString = sku + "," + skuString;
                    }
                    Log.d(TAG, "Before shift sku list:" + skuString);

                    if(product.getSku().equals(savedSeenProductList.get(0))) {
                        // Shift savedSeenProductList left from index 1 to 0
                        savedSeenProductList.set(0, savedSeenProductList.get(1));
                    }

                    // Shift savedSeenProductList left from index 2 to 1
                    savedSeenProductList.set(1, savedSeenProductList.get(2));

                    // Set last seen product at the end of the list
                    savedSeenProductList.set(2, product.getSku());

                    // Update the seen products sized ArrayList in singleton
                    feedSingleton.setSavedSeenProducts(savedSeenProductList);

                    // Update updated seen products sized ArrayList in the phone
                    savedSeenProductList.updateSeenProductsInPhone(getActivity());

                    skuString = "";
                    for(String sku : savedSeenProductList){
                        skuString = sku + "," + skuString;
                    }
                    Log.d(TAG, "After shift sku list:" + skuString);
                }
            }
        }
    }

    // Retrofit callbacks

    private class SkuDetailsCallback implements Callback<SkuDetails> {
        @Override
        public void success(SkuDetails details, Response response) {
            Activity activity = getActivity();
            if (activity==null) return;

            processSkuDetails(details);
            wrapper.setState(DataWrapper.State.DONE);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Activity activity = getActivity();
            if (activity==null) return;

            wrapper.setState(DataWrapper.State.EMPTY);
            String msg = ApiError.getErrorMessage(retrofitError);
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            Log.d(TAG, msg);
        }
    }

    private class ReviewSetCallback implements Callback<ReviewSet> {
        @Override
        public void success(ReviewSet reviews, Response response) {
            Activity activity = getActivity();
            if (activity==null) return;

            processReviewSet(reviews);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Activity activity = getActivity();
            if (activity==null) return;

            String msg = ApiError.getErrorMessage(retrofitError);
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            Log.d(TAG, msg);
        }
    }

    private void processSkuDetails(SkuDetails sku) {
        List<Product> products = sku.getProduct();
        if (products != null && products.size() > 0) {
            // Use the first product in the list
            Product product = products.get(0);
            tabAdapter.setProduct(product);

            // Handle availability
            QuantityEditor edit = (QuantityEditor) wrapper.findViewById(R.id.quantity);
            Button button = (Button) wrapper.findViewById(R.id.add_to_cart);
            Availability availability = Availability.getProductAvailability(product);
            switch (availability) {
                case NOTHING:
                case SKUSET:
                case RETAILONLY:
                case SPECIALORDER:
                case OUTOFSTOCK:
                    edit.setEnabled(false);
                    button.setText(availability.getTextResId());
                    button.setEnabled(false);
                    break;
                case INSTOCK:
                    edit.setEnabled(true);
                    button.setText(R.string.add_to_cart);
                    button.setEnabled(true);
                    break;
            }

            // Add images
            List<Image> images = product.getImage();
            if (images != null && images.size() > 0) {
                for (Image image : images) {
                    String url = image.getUrl();
                    if (url != null) imageAdapter.add(url);
                }
            }

            // Handle 0, 1, many images
            int n = imageAdapter.getCount();
            if (n == 0) imagePager.setVisibility(View.GONE);
            else {
                if (n > 1) stripe.setCount(imageAdapter.getCount());
                else stripe.setVisibility(View.GONE);
                imageAdapter.notifyDataSetChanged();
            }

            // Add info
            String name = Html.fromHtml(product.getProductName()).toString();
            ((TextView) summary.findViewById(R.id.title)).setText(name);
            ((TextView) summary.findViewById(R.id.numbers)).setText(formatNumbers(product));
            ((RatingStars) summary.findViewById(R.id.rating)).setRating(product.getCustomerReviewRating(), product.getCustomerReviewCount());

            // Add pricing
            ((PriceSticker) summary.findViewById(R.id.pricing)).setPricing(product.getPricing());

            // Add description
            LayoutInflater inflater = getActivity().getLayoutInflater();
            if (!buildDescription(inflater, (ViewGroup) summary.findViewById(R.id.description), product, 3))
                summary.findViewById(R.id.description_detail).setVisibility(View.GONE);

            // Check if the product has specifications
            if (product.getSpecification() != null) {
                // Add specifications
                addSpecifications(inflater, (ViewGroup) summary.findViewById(R.id.specifications), product, 3);
                Log.d(TAG, "Product has specification.");
            } else {
                summary.findViewById(R.id.specifications).setVisibility(View.GONE);
                summary.findViewById(R.id.specification_detail).setVisibility(View.GONE);
                Log.d(TAG, "Product has no specification.");
            }

            // Check if the product has accessories
            if (product.getAccessory() != null) {
                // Add accessories
                addAccessory(product);
                Log.d(TAG, "Product has accessories.");
            } else {
                summary.findViewById(R.id.accessory_title).setVisibility(View.GONE);
                Log.d(TAG, "Product has no accessories.");
            }

            // Save seen products detail for personal feed
            saveSeenProduct(product);
        }
    }

    public static String formatTimestamp(String raw) {
        if (raw == null) return (null);

        if (iso8601 == null)
            iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try {
            Date date = iso8601.parse(raw);
            String text = DateFormat.getDateInstance().format(date);
            return (text);
        } catch (ParseException e) {
            return (null);
        }
    }

    private void processReviewSet(ReviewSet reviews) {
        if (reviews == null) return;
        List<Data> datas = reviews.getData();
        if (datas == null || datas.size() < 1) return;
        tabAdapter.setReviews(datas);

        Data item = datas.get(0);
        if (item != null) {
            // Inflate review block
            LayoutInflater inflater = getActivity().getLayoutInflater();
            ViewGroup parent = (ViewGroup) summary.findViewById(R.id.reviews);
            View view = inflater.inflate(R.layout.sku_review_brief_item, parent, false);
            parent.addView(view);

            // Set items
            String created = formatTimestamp(item.getCreatedDatetime());
            if (created != null)
                ((TextView) view.findViewById(R.id.sku_review_date)).setText(created);
            else ((TextView) view.findViewById(R.id.sku_review_date)).setVisibility(View.GONE);

            ((RatingStars) view.findViewById(R.id.sku_review_rating)).setRating(item.getRating(), null);

            String comments = item.getComments();
            if (comments != null)
                ((TextView) view.findViewById(R.id.sku_review_comments)).setText(comments);
            else ((TextView) view.findViewById(R.id.sku_review_comments)).setVisibility(View.GONE);
        }

// TODO Review tags
//            List<HashMap<String, List<String>>> tags = data.getReview_tags();
//            if (tags!=null) {
//                for(HashMap<String, List<String>> map : tags) {
//                    Set<Map.Entry<String, List<String>>> set = map.entrySet();
//                    for(Map.Entry entry : set) {
//                        Log.d(TAG, entry.getKey() + " " + entry.getValue());
//                    }
//                }
//            }
    }

    // TabHost notifications

    public void onTabChanged(String tag) {
        int index;

        // Get index
        if (tag.equals(DESCRIPTION)) index = 0;
        else if (tag.equals(SPECIFICATIONS)) index = 1;
        else if (tag.equals(REVIEWS)) index = 2;
        else throw (new RuntimeException("Unknown tag from TabHost"));

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
    public void onBackStackChanged() {
        FragmentManager manager = getFragmentManager();
        int n = manager.getBackStackEntryCount();
        if (n < 1) return;
        String name = manager.getBackStackEntryAt(n - 1).getName();
        if (name != null && name.equals("SkuDetail")) return;
        manager.removeOnBackStackChangedListener(this);
        wrapper.setState(DataWrapper.State.DONE);
        details.setVisibility(View.GONE);
        shifted = false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
                int qty = edit.getQuantity();
                final MainActivity activity = (MainActivity) getActivity();
                activity.showProgressIndicator();
                CartApiManager.addItemToCart(identifier, qty, new CartApiManager.CartRefreshCallback() {
                    @Override
                    public void onCartRefreshComplete(String errMsg) {
                        activity.hideProgressIndicator();
                        activity.updateCartIcon(CartApiManager.getCartTotalItems());
                    }
                });
                break;
        }
    }
}
