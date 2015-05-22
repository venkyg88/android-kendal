package app.staples.mobile.cfa.sku;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.crittercism.app.Crittercism;
import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.api.ChannelApi;
import com.staples.mobile.common.access.channel.model.review.Pagination;
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

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.apptentive.ApptentiveSdk;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.feed.PersistentSizedArrayList;
import app.staples.mobile.cfa.feed.PersonalFeedSingleton;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.DataWrapper;
import app.staples.mobile.cfa.widget.PagerStripe;
import app.staples.mobile.cfa.widget.PriceSticker;
import app.staples.mobile.cfa.widget.QuantityEditor;
import app.staples.mobile.cfa.widget.RatingStars;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SkuFragment extends Fragment implements ViewPager.OnPageChangeListener, Callback, View.OnClickListener, FragmentManager.OnBackStackChangedListener, SkuReviewAdapter.OnFetchMoreData {
    private static final String TAG = SkuFragment.class.getSimpleName();
    private static final String PSEUDOCLASS = "SkuDetail";

    private static final String TITLE = "title";
    private static final String IDENTIFIER = "identifier";
    private static final String SKUSET = "skuset";

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

        Availability(int resid) {
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

    private Picasso picasso;

    private DataWrapper wrapper;
    private DataWrapper.State state;
    private View summary;

    // Image ViewPager
    private ViewPager imagePager;
    private SkuImageAdapter imageAdapter;
    private PagerStripe stripe;

    // Tab ViewPager
    private View details;
    private ViewPager tabPager;
    private SkuTabAdapter tabAdapter;
    private boolean isShiftedTab;

    // Reviews
    private SkuReviewAdapter reviewAdapter;
    private int reviewPage;

    // Cached values
    private Product cached;

    public void setArguments(String title, String identifier, boolean isSkuSetRedirected) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(IDENTIFIER, identifier);
        args.putBoolean(SKUSET, isSkuSetRedirected);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE);
            identifier = args.getString(IDENTIFIER);
            isSkuSetOriginated = args.getBoolean(SKUSET);
        }

        picasso = Picasso.with(getActivity());

        // Get product details & reviews
        EasyOpenApi api = Access.getInstance().getEasyOpenApi(false);
        api.getSkuDetails(identifier, null, MAXFETCH, this);

        reviewPage = 1;
        queryReviews();
        state = DataWrapper.State.LOADING;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("SkuFragment:onCreateView(): Displaying the SKU screen.");

        wrapper = (DataWrapper) inflater.inflate(R.layout.sku_summary, container, false);
        summary = wrapper.findViewById(R.id.summary);

        imagePager = (ViewPager) summary.findViewById(R.id.images);
        stripe = (PagerStripe) summary.findViewById(R.id.stripe);
        imagePager.setOnPageChangeListener(stripe);

        details = wrapper.findViewById(R.id.details);
        tabPager = (ViewPager) details.findViewById(R.id.pager);
        details.setVisibility(View.GONE);

        // Set listeners
        tabPager.setOnPageChangeListener(this);
        summary.findViewById(R.id.description_detail).setOnClickListener(this);
        summary.findViewById(R.id.specification_detail).setOnClickListener(this);
        summary.findViewById(R.id.review_detail).setOnClickListener(this);
        details.findViewById(R.id.description_tab).setOnClickListener(this);
        details.findViewById(R.id.specification_tab).setOnClickListener(this);
        details.findViewById(R.id.review_tab).setOnClickListener(this);
        wrapper.findViewById(R.id.add_to_cart).setOnClickListener(this);

        applyState(wrapper);
        return (wrapper);
    }

    private void createAdapters() {
        Activity activity = getActivity();
        if (activity==null) return;
        Resources res = activity.getResources();

        if (imageAdapter==null) {
            imageAdapter = new SkuImageAdapter(activity);
        }

        if (tabAdapter==null) {
            tabAdapter = new SkuTabAdapter(activity);
            tabAdapter.add(res.getString(R.string.description));
            tabAdapter.add(res.getString(R.string.specs));
            tabAdapter.add(res.getString(R.string.reviews));
            tabAdapter.notifyDataSetChanged();
        }

        if (reviewAdapter==null) {
            reviewAdapter = new SkuReviewAdapter(activity);
        }

        tabAdapter.setReviewAdapter(reviewAdapter);
    }

    private void applyState(View view) {
        if (view==null) view = getView();
        if (view==null) return;

        if (imagePager!=null && imagePager.getAdapter()==null && imageAdapter!=null) {
            imagePager.setAdapter(imageAdapter);
        }

        if (tabPager!=null && tabPager.getAdapter()==null && tabAdapter!=null) {
            tabPager.setAdapter(tabAdapter);
            details = wrapper.findViewById(R.id.details);
        }

        if (reviewAdapter!=null && reviewAdapter.getItemCount()>0) {
            ViewGroup reviews = (ViewGroup) summary.findViewById(R.id.reviews);
            if (reviews.getChildCount()==0) {
                SkuReviewAdapter.ViewHolder vh = reviewAdapter.onCreateViewHolder(reviews, 0);
                reviewAdapter.onBindViewHolder(vh, 0);
                vh.limitComments(3);
                reviews.addView(vh.itemView);
                summary.findViewById(R.id.review_layout).setVisibility(View.VISIBLE);
                summary.findViewById(R.id.rating).setOnClickListener(this);
            }
        }

        if (cached!=null) {
           applyProduct(cached);
        }

        if (view instanceof DataWrapper) {
            ((DataWrapper) view).setState(state);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.SKU, title);
    }

    @Override
    public void onDestroy() {
        FragmentManager manager = getFragmentManager();
        manager.removeOnBackStackChangedListener(this);
        super.onDestroy();
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
        if (parent.getChildCount()>0) return(true);

        boolean described = false;
        int count = 0;

        // Add descriptions
        List<Description> paragraphs = product.getParagraph();
        if (paragraphs != null) {
            for (Description paragraph : paragraphs) {
                String text = Html.fromHtml(paragraph.getText()).toString();
                if (!text.isEmpty()) {
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
                if (!text.isEmpty()) {
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
        if (parent.getChildCount()>0) return; // Don't double fill!

        List<Description> specs = product.getSpecification();
        if (specs==null) return;

        int count = 0;
        for (Description spec : specs) {
            if (count >= limit) {
                break;
            }

            String specName = Html.fromHtml(spec.getName()).toString();
            String specValue = Html.fromHtml(spec.getText()).toString();

            if (!specName.isEmpty() && !specValue.isEmpty()) {
                View row = inflater.inflate(R.layout.sku_spec_item, parent, false);
                if (count==0) {
                    row.setBackgroundColor(0x00000000);
                }
                parent.addView(row);
                ((TextView) row.findViewById(R.id.specName)).setText(specName);
                ((TextView) row.findViewById(R.id.specValue)).setText(specValue);
                count++;
            }
        }
    }

    private void addAccessory(final Product product) {
        MainActivity activity = (MainActivity) getActivity();
        List<Product> accessories = product.getAccessory();

        LayoutInflater inflater = activity.getLayoutInflater();
        ViewGroup parent = (ViewGroup) summary.findViewById(R.id.accessory_container);
        if (parent.getChildCount()>0) return;

        for (final Product accessory : accessories) {
            String accessoryTitle = accessory.getProductName();
            String sku = accessory.getSku();

            View row = inflater.inflate(R.layout.sku_accessory_item, parent, false);
            row.setOnClickListener(this);
            row.setTag(accessory);

            // Set accessory image
            String accessoryImageUrl = null;
            List<Image> images = accessory.getImage();
            if (images!=null && images.size()>0) {
                Image image = images.get(0);
                if (image!=null) {
                    accessoryImageUrl = image.getUrl();
                }
            }
            ImageView accessoryImageView = (ImageView) row.findViewById(R.id.accessory_image);
            if (accessoryImageUrl!=null) {
                picasso.load(accessoryImageUrl).error(R.drawable.no_photo).fit().into(accessoryImageView);
            } else {
                picasso.load(R.drawable.no_photo).into(accessoryImageView);
            }

            // Set accessory title
            TextView accessoryTitleTextView = (TextView) row.findViewById(R.id.accessory_title);
            accessoryTitleTextView.setText(Html.fromHtml(accessoryTitle).toString());

            // Set accessory price
            ((PriceSticker) row.findViewById(R.id.accessory_price)).setBrowsePricing(accessory.getPricing());

            // Set accessory rating
            ((RatingStars) row.findViewById(R.id.accessory_rating))
                    .setRating(accessory.getCustomerReviewRating(), accessory.getCustomerReviewCount());

            parent.addView(row);
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
            state = DataWrapper.State.DONE;
            applyState(null);
        }

        else if (obj instanceof YotpoResponse) {
            YotpoResponse yotpoResponse = (YotpoResponse) obj;
            processReviews(yotpoResponse);
            applyState(null);
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

    // This method handles bad JSON boolean fields
    private boolean isJsonTrue(String text) {
        if (text==null) return(false);
        if (text.equalsIgnoreCase("Y") ||
            text.equalsIgnoreCase("true")) return(true);
        return(false);
    }

    private void processSkuDetails(SkuDetails sku) {
        final MainActivity activity = (MainActivity) getActivity();
        Resources res = activity.getResources();
        List<Product> products = sku.getProduct();
        if (products != null && products.size() > 0) {
            // Use the first product in the list
            createAdapters();
            cached = products.get(0);
            tabAdapter.setProduct(cached);
        }
    }

    private float findRebate(Pricing pricing) {
        if (pricing==null) return(0.0f);
        List<Discount> discounts = pricing.getDiscount();
        if (discounts==null) return(0.0f);

        float rebate = 0.0f;
        for(Discount discount : discounts) {
            String name = discount.getName();
            if ("rebate".equals(name)) {
                float amount = discount.getAmount();
                if (amount > rebate) rebate = amount;
            }
        }
        return(rebate);
    }

    private void applyProduct(final Product product) {
        final MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;
        Resources res = activity.getResources();

        // Handle availability
        QuantityEditor qtyEditor = (QuantityEditor) wrapper.findViewById(R.id.quantity);
        Button addToCartButton = (Button) wrapper.findViewById(R.id.add_to_cart);
        TextView footerMsg = (TextView) wrapper.findViewById(R.id.footer_msg);
        Availability availability = Availability.getProductAvailability(product);
        TextView skuText = (TextView) wrapper.findViewById(R.id.select_sku);

        if (isSkuSetOriginated) {
            skuText.setVisibility(View.VISIBLE);
            skuText.setText(R.string.skuset_change);
            skuText.setOnClickListener(this);
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
                skuText.setOnClickListener(this);
                skuText.setTag(product);
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
        }

        // check if the product is an add-on product
        if (isJsonTrue(product.getAddOnSku())) {
            summary.findViewById(R.id.add_on_layout).setVisibility(View.VISIBLE);
        }

        // check if the product is an overweight product, example sku:650465
        if (isJsonTrue(product.getHeavyWeightSku())) {
            if (product.getPricing()!=null){
                summary.findViewById(R.id.overweight_warning).setVisibility(View.VISIBLE);
                float heavyWeightShipCharge = product.getPricing().get(0).getHeavyWeightShipCharge();
            }
        }

        // check if the product has discount
        List<Pricing> pricings = product.getPricing();
        if (pricings!=null && pricings.size()>0) {
            Pricing pricing = pricings.get(0);
            PriceSticker priceSticker = (PriceSticker) summary.findViewById(R.id.pricing);
            float rebate = findRebate(pricing);
            if (rebate>0.0f) {
                summary.findViewById(R.id.rebate_layout).setVisibility(View.VISIBLE);
                TextView rebateText = (TextView) summary.findViewById(R.id.rebate_text);
                String text = String.format("$%.2f %s", rebate, res.getString(R.string.rebate));
                rebateText.setText(text);

                float finalPrice = pricing.getFinalPrice();
                float wasPrice = pricing.getListPrice();
                String unit = pricing.getUnitOfMeasure();
                priceSticker.setPricing(finalPrice + rebate, wasPrice, unit, "*");
            } else {
                summary.findViewById(R.id.rebate_layout).setVisibility(View.GONE);
                priceSticker.setPricing(pricing);
            }
        }

        // Save seen products detail for personal feed
        saveSeenProduct(product);
    }

    private void queryReviews() {
        ChannelApi channelApi = Access.getInstance().getChannelApi(false);
        channelApi.getYotpoReviews(identifier, reviewPage, MAXFETCH, this);
    }

    @Override
    public void onFetchMoreData() {
        reviewPage++;
        queryReviews();
    }

    private int processReviews(YotpoResponse yotpoResponse) {
        if (yotpoResponse==null) return(0);
        com.staples.mobile.common.access.channel.model.review.Response response = yotpoResponse.getResponse();
        if (response==null) return(0);
        List<Review> reviews = response.getReviews();
        if (reviews==null || reviews.size()==0) return(0);

        boolean complete = false;
        Pagination pagination = response.getPagination();
        if (pagination!=null) {
            complete = (pagination.getTotal()<=reviewPage*MAXFETCH);
        }

        createAdapters();
        reviewAdapter.fill(reviews);
        reviewAdapter.notifyDataSetChanged();

        int count = reviewAdapter.getItemCount();
        if (!complete) {
            reviewAdapter.setOnFetchMoreData(this, count-LOOKAHEAD);
        }
        return(count);
    }

    // ViewPager notifications

    public void onPageScrollStateChanged(int state) {
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
       selectDetailTab(position);
    }

    private void selectDetailTab(int position) {
        details.findViewById(R.id.description_tab).setSelected(position==0);
        details.findViewById(R.id.specification_tab).setSelected(position==1);
        details.findViewById(R.id.review_tab).setSelected(position==2);
    }

    // Detail and add-to-cart clicks
    private void shiftToDetail(int position) {
        if (!isShiftedTab) {
            wrapper.setState(DataWrapper.State.GONE);
            details.setVisibility(View.VISIBLE);

            // Add pseudo fragment to back stack
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.addToBackStack(PSEUDOCLASS);
            transaction.commit();
            manager.addOnBackStackChangedListener(this);
            isShiftedTab = true;
        }

        tabPager.setCurrentItem(position);
        selectDetailTab(position);

        Tracker.getInstance().trackActionForProductTabs(tabAdapter.getPageTitle(position), tabAdapter.getProduct());
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager manager = getFragmentManager();
        int index = manager.getBackStackEntryCount()-1;
        if (index<0) return;

        String name = manager.getBackStackEntryAt(index).getName();
        if (PSEUDOCLASS.equals(name)) {
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
            QuantityEditor edit = (QuantityEditor) wrapper.findViewById(R.id.quantity);
            edit.setQuantity(1);

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
                ((MainActivity) activity).showErrorDialog(errMsg);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.description_tab:
            case R.id.description_detail:
                shiftToDetail(0);
                break;
            case R.id.specification_tab:
            case R.id.specification_detail:
                shiftToDetail(1);
                break;
            case R.id.review_tab:
            case R.id.review_detail:
            case R.id.rating:
                shiftToDetail(2);
                break;
            case R.id.add_to_cart:
                QuantityEditor edit = (QuantityEditor) wrapper.findViewById(R.id.quantity);
                int quantity = edit.getQuantity();
                new AddToCart(identifier, quantity);
                break;
            case R.id.select_sku:
                Object skuSetTag = view.getTag();
                if(skuSetTag instanceof Product) {
                    Product product = (Product) skuSetTag;
                    ((MainActivity)getActivity()).selectSkuSet(product.getProductName(), product.getSku(), product.getThumbnailImage().get(0).getUrl());
                }
                if(isSkuSetOriginated) {
                    ((MainActivity) getActivity()).popBackStack();
                }
                break;
            case R.id.accessory_layout:
                Object accessoryTag = view.getTag();
                if(accessoryTag instanceof Product) {
                    Product product = (Product)accessoryTag;
                    ((MainActivity)getActivity()).selectSkuItem(product.getProductName(), product.getSku(), false);
                }
                break;
        }
    }
}
