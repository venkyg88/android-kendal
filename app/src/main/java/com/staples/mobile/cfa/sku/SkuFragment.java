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
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.apptentive.ApptentiveSdk;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.feed.PersistentSizedArrayList;
import com.staples.mobile.cfa.feed.PersonalFeedSingleton;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PagerStripe;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.api.ChannelApi;
import com.staples.mobile.common.access.channel.model.review.Review;
import com.staples.mobile.common.access.channel.model.review.YotpoResponse;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.BulletDescription;
import com.staples.mobile.common.access.easyopen.model.browse.Description;
import com.staples.mobile.common.access.easyopen.model.browse.Discount;
import com.staples.mobile.common.access.easyopen.model.browse.Image;
import com.staples.mobile.common.access.easyopen.model.browse.Pricing;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;
import com.staples.mobile.common.analytics.Tracker;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuFragment extends Fragment implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, Callback, View.OnClickListener, FragmentManager.OnBackStackChangedListener {
    private static final String TAG = SkuFragment.class.getSimpleName();
    private static final String PSEUDOCLASS = "SkuDetail";

    private static final String TITLE = "title";
    private static final String IDENTIFIER = "identifier";
    private static final String SKUSET = "skuset";

    private static final String DESCRIPTION = "Description";
    private static final String SPECIFICATIONS = "Specifications";
    private static final String REVIEWS = "Reviews";

    private static final int MAXFETCH = 50;
    private static final int LOOKAHEAD = 10;

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

    private String title;
    private String identifier;
    private boolean isSkuSetOriginated;

    private DataWrapper wrapper;
    private View summary;

    // Image ViewPager
    private ViewPager imagePager;
    private SkuImageAdapter imageAdapter;
    private PagerStripe stripe;

    // Tab ViewPager
    private TabHost details;
    private ViewPager tabPager;
    private SkuTabAdapter tabAdapter;
    private boolean isShiftedTab;

    // Accessory Container
    private ViewGroup accessoryContainer;

    // Shipping Logic Container
    private ViewGroup overweightLayout;
    private ViewGroup addonLayout;

    // Reviews
    private SkuReviewAdapter reviewAdapter;
    private int reviewPage;

    public void setArguments(String title, String identifier, boolean isSkuSetRedirected) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(IDENTIFIER, identifier);
        args.putBoolean(SKUSET, isSkuSetRedirected);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        MainActivity activity = (MainActivity) getActivity();
        Resources res = activity.getResources();

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE);
            identifier = args.getString(IDENTIFIER);
            isSkuSetOriginated = args.getBoolean(SKUSET);
        }

        wrapper = (DataWrapper) inflater.inflate(R.layout.sku_summary, container, false);
        summary = wrapper.findViewById(R.id.summary);

        // Init image pager
        imagePager = (ViewPager) summary.findViewById(R.id.images);
        imageAdapter = new SkuImageAdapter(activity);
        imagePager.setAdapter(imageAdapter);
        stripe = (PagerStripe) summary.findViewById(R.id.stripe);
        imagePager.setOnPageChangeListener(stripe);

        // Init details (ViewPager)
        tabPager = (ViewPager) wrapper.findViewById(R.id.pager);
        tabAdapter = new SkuTabAdapter(activity);
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
        accessoryContainer = (ViewGroup) wrapper.findViewById(R.id.accessoryContainer);

        // Init shipping logic
        overweightLayout = (ViewGroup) wrapper.findViewById(R.id.overweight_layout);
        addonLayout = (ViewGroup) wrapper.findViewById(R.id.add_on_layout);

        // Fill details (TabHost)
        DummyFactory dummy = new DummyFactory(activity);
        addTab(dummy, res, R.string.description, DESCRIPTION);
        addTab(dummy, res, R.string.specs, SPECIFICATIONS);
        addTab(dummy, res, R.string.reviews, REVIEWS);

        // Set initial visibility
        wrapper.setState(DataWrapper.State.LOADING);
        details.setVisibility(View.GONE);

        // Set listeners
        details.setOnTabChangedListener(this);
        tabPager.setOnPageChangeListener(this);
        summary.findViewById(R.id.description_detail).setOnClickListener(this);
        summary.findViewById(R.id.specifications_detail).setOnClickListener(this);
        summary.findViewById(R.id.reviews_detail).setOnClickListener(this);
        wrapper.findViewById(R.id.add_to_cart).setOnClickListener(this);

        // Initiate API calls
        EasyOpenApi api = Access.getInstance().getEasyOpenApi(false);
        api.getSkuDetails(identifier, null, MAXFETCH, this);

        ChannelApi channelApi = Access.getInstance().getChannelApi(false);
        channelApi.getYotpoReviews(identifier, 1, 50, this);

        return (wrapper);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.SKU, title);
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
    private String formatNumbers(Resources res, Product product) {
        // Safety check
        if (product == null) return (null);
        String skuNumber = product.getSku();
        String modelNumber = product.getManufacturerPartNumber();
        if (skuNumber == null && modelNumber == null) return (null);

        // Skip redundant numbers
        if (skuNumber != null && modelNumber != null && skuNumber.equals(modelNumber))
            modelNumber = null;

        StringBuilder sb = new StringBuilder();

        if (skuNumber != null) {
            sb.append(res.getString(R.string.item));
            sb.append(":\u00a0"); // nbsp
            sb.append(skuNumber);
        }

        if (modelNumber != null) {
            if (sb.length() > 0) sb.append("   ");
            sb.append(res.getString(R.string.model));
            sb.append(":\u00a0"); // nbsp
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
//                    if ((rowCount % 2) == 0) {
//                        skuSpecRow.setBackgroundColor(parent.getContext().getResources().getColor(R.color.staples_middle_gray));
//                    }

                    // Set specification
                    ((TextView) skuSpecRow.findViewById(R.id.specName)).setText(specName);
                    ((TextView) skuSpecRow.findViewById(R.id.specValue)).setText(specValue);

                    rowCount++;
                }
            }
        }
    }

    private void addAccessory(final Product product) {
        final MainActivity activity = (MainActivity) getActivity();
        List<Product> accessories = product.getAccessory();

        LayoutInflater inflater = activity.getLayoutInflater();

        for (final Product accessory : accessories) {
            String accessoryImageUrl = accessory.getImage().get(0).getUrl();
            final String accessoryTitle = accessory.getProductName();
            final String sku = accessory.getSku();

            View skuAccessoryRow = inflater.inflate(R.layout.sku_accessory_item, null);

            // Set accessory image
            ImageView accessoryImageView = (ImageView) skuAccessoryRow.findViewById(R.id.accessory_image);
            Picasso.with(activity).load(accessoryImageUrl).error(R.drawable.no_photo).into(accessoryImageView);

            // Set listener for accessory image
            accessoryImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tracker.getInstance().trackActionForProductAccessories(sku, product, false); // analytics
                    activity.selectSkuItem(Html.fromHtml(accessoryTitle).toString(), sku, false);
                }
            });

            // Set accessory title
            TextView accessoryTitleTextView = (TextView) skuAccessoryRow.findViewById(R.id.accessory_title);
            accessoryTitleTextView.setText(Html.fromHtml(accessoryTitle).toString());

            // Set listener for accessory title
            accessoryTitleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.selectSkuItem(accessoryTitle, sku, false);
                }
            });

            // Set accessory rating
            ((RatingStars) skuAccessoryRow.findViewById(R.id.accessory_rating))
                    .setRating(accessory.getCustomerReviewRating(), accessory.getCustomerReviewCount());

            // Set accessory price
            ((PriceSticker) skuAccessoryRow.findViewById(R.id.accessory_price)).setBrowsePricing(accessory.getPricing());

            accessoryContainer.addView(skuAccessoryRow);
        }
    }

    private void saveSeenProduct(Product product){
        MainActivity activity = (MainActivity) getActivity();
        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(activity);
        HashSet<String> savedSkuSet = feedSingleton.getSavedSkus(activity);
        PersistentSizedArrayList<String> savedSeenProductList
                = feedSingleton.getSavedSeenProducts(activity);

        // Check if the product was saved before
        if(!savedSkuSet.contains(product.getSku())){
            Log.d(TAG, "Saving seen product's sku: " + product.getProductName());

            savedSeenProductList.addSeenProduct(product.getSku(), activity);
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
                    savedSeenProductList.updateSeenProductsInPhone(activity);

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
                    savedSeenProductList.updateSeenProductsInPhone(activity);

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

    @Override
    public void success(Object obj, Response response) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        if (obj instanceof SkuDetails) {
            SkuDetails details = (SkuDetails) obj;
            processSkuDetails(details);
            wrapper.setState(DataWrapper.State.DONE);
        }

        else if (obj instanceof YotpoResponse) {
            YotpoResponse yotpoResponse = (YotpoResponse) obj;
            processYotpoReview(yotpoResponse);
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        Type type = retrofitError.getSuccessType();

        if (type==SkuDetails.class) {
            wrapper.setState(DataWrapper.State.EMPTY);
            String msg = ApiError.getErrorMessage(retrofitError);
            ((MainActivity) activity).showErrorDialog(msg);
            Log.d(TAG, msg);
        }

        else if (type==YotpoResponse.class) {
            String msg = ApiError.getErrorMessage(retrofitError);
            ((MainActivity) activity).showErrorDialog(msg);
            Log.d(TAG, msg);
        }
    }

    private void processSkuDetails(SkuDetails sku) {
        final MainActivity activity = (MainActivity) getActivity();
        Resources res = activity.getResources();
        List<Product> products = sku.getProduct();
        if (products != null && products.size() > 0) {
            // Use the first product in the list
            final Product product = products.get(0);

            tabAdapter.setProduct(product);

            // Handle availability
            QuantityEditor qtyEditor = (QuantityEditor) wrapper.findViewById(R.id.quantity);
            Button addToCartButton = (Button) wrapper.findViewById(R.id.add_to_cart);
            TextView footerMsg = (TextView) wrapper.findViewById(R.id.footer_msg);
            Availability availability = Availability.getProductAvailability(product);
            TextView skuText = (TextView) wrapper.findViewById(R.id.select_sku);

            if (isSkuSetOriginated == true) {
                skuText.setVisibility(View.VISIBLE);
                skuText.setText(product.getProductName());
                skuText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = getFragmentManager();
                        if (fm != null) {
                            fm.popBackStack(); // this will take us back to one of the many places that could have opened this page
                        }
                    }
                });
            }

            // Analytics
            if (availability == Availability.SKUSET) {
                Tracker.getInstance().trackStateForSkuSet(product); // Analytics
            } else {
                Tracker.getInstance().trackStateForProduct(product); // Analytics
            }

            Apptentive.engage(activity, ApptentiveSdk.PRODUCT_DETAIL_SHOWN_EVENT);

            switch(availability) {
                case NOTHING:
                case SKUSET:
                    qtyEditor.setVisibility(View.VISIBLE);
                    addToCartButton.setVisibility(View.VISIBLE);
                    skuText.setVisibility(View.VISIBLE);
                    addToCartButton.setEnabled(false);
                    qtyEditor.setEnabled(false);
                    skuText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.selectSkuSet(product.getProductName(), product.getSku(), product.getThumbnailImage().get(0).getUrl());
                        }
                    });
                    break;
                case RETAILONLY:
                case SPECIALORDER:
                case OUTOFSTOCK:
                    // for all of the above cases, hide qty editor and ADD button, and show message
                    footerMsg.setVisibility(View.VISIBLE);
                    footerMsg.setText(availability.getTextResId());
                    break;
                case INSTOCK:
                    // show add-to-cart widgets
                    qtyEditor.setVisibility(View.VISIBLE);
                    addToCartButton.setVisibility(View.VISIBLE);
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
            String productName = Html.fromHtml(product.getProductName()).toString();
            ((TextView) summary.findViewById(R.id.title)).setText(productName);
            ((TextView) summary.findViewById(R.id.model)).setText(formatNumbers(res, product));
            ((RatingStars) summary.findViewById(R.id.rating)).setRating(product.getCustomerReviewRating(), product.getCustomerReviewCount());
            ((RatingStars) summary.findViewById(R.id.review_rating)).setRating(product.getCustomerReviewRating(), product.getCustomerReviewCount());

            // Add description
            LayoutInflater inflater = activity.getLayoutInflater();
            if (!buildDescription(inflater, (ViewGroup) summary.findViewById(R.id.description), product, 3)) {
                summary.findViewById(R.id.description).setVisibility(View.GONE);
            }

            // Check if the product has specifications
            if (product.getSpecification() != null) {
                // Add specifications
                addSpecifications(inflater, (ViewGroup) summary.findViewById(R.id.specifications), product, 3);
            } else {
                summary.findViewById(R.id.specifications_layout).setVisibility(View.GONE);
            }

            // Check if the product has accessories
            if (product.getAccessory() != null) {
                // Add accessories
                addAccessory(product);
                // Log.d(TAG, "Product has accessories.");
            } else {
                summary.findViewById(R.id.accessory_layout).setVisibility(View.GONE);
                // Log.d(TAG, "Product has no accessories.");
            }

            // check if the product is an add-on product
            if(product.getAddOnSku() != null && product.getAddOnSku().equals("Y")){
                addonLayout.setVisibility(View.VISIBLE);

                Log.d(TAG, "The product is an add-on product. sku:" + product.getSku());
            }

            // check if the product is an overweight product, example sku:650465
            if(product.getHeavyWeightSku() != null && product.getHeavyWeightSku().equals("Y")){
                if(product.getPricing() != null){
                    overweightLayout.setVisibility(View.VISIBLE);
                    float heavyWeightShipCharge = product.getPricing().get(0).getHeavyWeightShipCharge();
                    Log.d(TAG, "The product is an overweight product. sku:" + product.getSku() +
                            ", HeavyWeightShipCharge:" + heavyWeightShipCharge);
                }
            }

            if(addonLayout.getVisibility() == View.VISIBLE || overweightLayout.getVisibility() == View.VISIBLE){
                summary.findViewById(R.id.shipping_logic_layout).setVisibility(View.VISIBLE);
            }
            else{
                summary.findViewById(R.id.shipping_logic_layout).setVisibility(View.GONE);
            }

            // check if the product has discount
            Pricing pricing = product.getPricing().get(0);
            List<Discount> discounts = pricing.getDiscount();
            // Add pricing with rebate
            if (discounts != null && discounts.size() > 0) {
                for (Discount discount : discounts) {
                    if (discount.getName().equals("rebate") && discount.getAmount() > 0) {
                        summary.findViewById(R.id.rebate_layout).setVisibility(View.VISIBLE);

                        Button rebateButton = (Button) summary.findViewById(R.id.rebate_button);
                        float rebate = discount.getAmount();
                        String rebateString = String.format("%.2f", rebate);
                        rebateButton.setText(String.valueOf("$" + rebateString + " " + res.getString(R.string.rebate)));

                        float finalPrice = pricing.getFinalPrice();
                        float wasPrice = pricing.getListPrice();
                        String unit = pricing.getUnitOfMeasure();
                        PriceSticker priceSticker = (PriceSticker) summary.findViewById(R.id.pricing);
                        priceSticker.setPricing(finalPrice + rebate, wasPrice, unit, "");

                        Log.d(TAG, "The product has rebate. sku:" + product.getSku() + ", rebate:" + rebate);
                        break;
                    }
                }
            }
            // Add pricing without rebate
            else {
                PriceSticker priceSticker = (PriceSticker) summary.findViewById(R.id.pricing);
                priceSticker.setPricing(pricing);

                summary.findViewById(R.id.rebate_layout).setVisibility(View.GONE);
            }

            // Save seen products detail for personal feed
            saveSeenProduct(product);
        }
    }

    private void processYotpoReview(YotpoResponse yotpoResponse) {
        if (yotpoResponse==null) return;
        com.staples.mobile.common.access.channel.model.review.Response response = yotpoResponse.getResponse();
        if (response==null) return;
        List<Review> reviews = response.getReviews();
        if (reviews==null || reviews.size()==0) return;

        if (reviewAdapter==null) {
            reviewAdapter = new SkuReviewAdapter(getActivity());
            tabAdapter.setReviewAdapter(reviewAdapter);
        }
        reviewAdapter.fill(reviews);
        reviewAdapter.notifyDataSetChanged();

        ViewGroup parent = (ViewGroup) summary.findViewById(R.id.reviews);
        SkuReviewAdapter.ViewHolder vh = reviewAdapter.onCreateViewHolder(parent, 0);
        reviewAdapter.onBindViewHolder(vh, 0);
        parent.addView(vh.itemView);
    }

    // TabHost notifications
    public void onTabChanged(String tag) {
        int index;

        // Get index
        if (tag.equals(DESCRIPTION)) index = 0;
        else if (tag.equals(SPECIFICATIONS)) index = 1;
        else if (tag.equals(REVIEWS)) index = 2;
        else throw (new RuntimeException("Unknown tag from TabHost"));

        // analytics
        Tracker.getInstance().trackActionForProductTabs(tabAdapter.getPageTitle(index), tabAdapter.getProduct());

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
        if (isShiftedTab) return;
        wrapper.setState(DataWrapper.State.GONE);
        details.setVisibility(View.VISIBLE);
        isShiftedTab = true;
        tabPager.setCurrentItem(position);

        // Add pseudo fragment to back stack
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.addToBackStack(PSEUDOCLASS);
        transaction.commit();
        manager.addOnBackStackChangedListener(this);
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager manager = getFragmentManager();
        int fragmentEntryCount = manager.getBackStackEntryCount();
        if(fragmentEntryCount < 1) return;

        String name = manager.getBackStackEntryAt(fragmentEntryCount - 1).getName();
        if (PSEUDOCLASS.equals(name)) {
            isShiftedTab = false;
            return;
        }

        manager.removeOnBackStackChangedListener(this);
        wrapper.setState(DataWrapper.State.DONE);
        details.setVisibility(View.GONE);
        isShiftedTab = false;
    }

    private class AddToCart implements CartApiManager.CartRefreshCallback {
        private int quantity;

        private AddToCart(String identifier, int quantity) {
            this.quantity = quantity;
            MainActivity activity = (MainActivity) getActivity();
            activity.showProgressIndicator();
            CartApiManager.addItemToCart(identifier, quantity, this);
        }

        @Override
        public void onCartRefreshComplete(String errMsg) {
            Activity activity = getActivity();
            if (!(activity instanceof MainActivity)) return;

            ((MainActivity) activity).hideProgressIndicator();

            // if success
            if (errMsg == null) {
                ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
                ((Button) wrapper.findViewById(R.id.add_to_cart)).setText(R.string.add_another);
                Tracker.getInstance().trackActionForAddToCartFromProductDetails(CartApiManager.getCartProduct(identifier), quantity);
            } else {
                // if non-grammatical out-of-stock message from api, provide a nicer message
                if (errMsg.contains("items is out of stock")) {
                    errMsg = activity.getResources().getString(R.string.avail_outofstock);
                }
                ((MainActivity) activity) .showErrorDialog(errMsg);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.description_detail:
                shiftToDetail(0);
                break;
            case R.id.specifications_detail:
                shiftToDetail(1);
                break;
            case R.id.reviews_detail:
                shiftToDetail(2);
                break;
            case R.id.add_to_cart:
                QuantityEditor edit = (QuantityEditor) wrapper.findViewById(R.id.quantity);
                int quantity = edit.getQuantity();
                new AddToCart(identifier, quantity);
                break;
        }
    }
}
