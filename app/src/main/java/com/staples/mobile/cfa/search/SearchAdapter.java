package com.staples.mobile.cfa.search;

/**
 * Author: Yongnan Zhou
 */

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;
import com.staples.mobile.cfa.bundle.BundleItem;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.*;

import org.apache.http.HttpStatus;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchAdapter extends ArrayAdapter<BundleItem> implements DataWrapper.Layoutable, Callback<SearchResult>{
    private static final String TAG = "SearchAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";
    private static final String ZIPCODE = "01702";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String SEARCH_RESULTS_AMOUNT = "20";
    private static final String SORT_BY_BEST_MATCH = "0";

    private EasyOpenApi easyOpenApi;
    private Drawable noPhoto;
    private DataWrapper wrapper;

    private static Search mySearch;
    private int search_counter = 0;
    private LayoutInflater inflater;
    private Activity activity;
    private String keyword;
    private int layout;

    public SearchAdapter(Activity activity, DataWrapper wrapper) {
        super(activity, 0);
        this.activity = activity;
        this.wrapper = wrapper;
        inflater = activity.getLayoutInflater();
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
    }

    public void setLayout(DataWrapper.Layout layout) {
        if (layout==DataWrapper.Layout.TALL) this.layout = R.layout.bundle_item_tall;
        else  this.layout = R.layout.bundle_item_wide;
    }

    public void getSearchResults(final String keyword)
    {
        wrapper.setState(DataWrapper.State.LOADING);

        this.keyword = keyword;

        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.searchResult(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE, ZIPCODE, keyword, "1",
                SEARCH_RESULTS_AMOUNT, SORT_BY_BEST_MATCH, CLIENT_ID, "", this);
    }

    @Override
    public void success(SearchResult searchResult, Response response) {
        if(response.getStatus() == HttpStatus.SC_OK){
            if (searchResult == null) {
                search_counter++;
                Log.d(TAG, "Retro: searchResult is null");
                Log.d(TAG + "Error JSON searchResult", "No value for 'Search' in JSON response. "
                        + "Resend the request for " + search_counter + " times.");
                if(search_counter <= 5){
                    getSearchResults(keyword);
                }
            }
            else{
                if(searchResult.getSearch() == null || searchResult.getSearch().length < 1){
                    Log.d(TAG, "Retro: searchResult.getSearch() is null");
                    notifyDataSetChanged();
                    return;
                }
                else{
                    mySearch = searchResult.getSearch()[0];
                    if(mySearch.getItemCount() > 0){
                        setSearchResult();
                        wrapper.setState(DataWrapper.State.DONE);
                    }
                    else{
                        wrapper.setState(DataWrapper.State.EMPTY);
                    }
                    notifyDataSetChanged();
                }
            }
        }
        else{
            //wrapper.setState(SearchWrapperLayout.State.EMPTY);
            Log.d(TAG, "Retro: Incorrect HTTP status code: " + response.getStatus());
        }
        notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Retro failed!");
        Log.d(TAG, "Fail to get search results: " + retrofitError.getMessage());
        Log.d(TAG, "URl used to get search details: " + retrofitError.getUrl());
        wrapper.setState(DataWrapper.State.EMPTY);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        BundleItem item = getItem(position);

        if (view==null)
            view = inflater.inflate(layout, parent, false);

        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) title.setText(item.title);

        RatingStars ratingStars = (RatingStars) view.findViewById(R.id.rating);
        ratingStars.setRating(item.customerRating, item.customerCount);

        PriceSticker priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
        priceSticker.setPricing(item.price, item.unit);

        ImageView image = (ImageView) view.findViewById(R.id.image);
        if (item.imageUrl==null) image.setImageDrawable(noPhoto);
        else Picasso.with(activity).load(item.imageUrl).error(noPhoto).into(image);

        return(view);
    }

    private void setSearchResult (){
        List<Product> products = mySearch.getProduct();
        if (products!=null) {

            for (Product product : products) {
                BundleItem item = new BundleItem(product.getProductName(), product.getSku());
                item.setImageUrl(product.getImage());
                item.setPrice(product.getPricing());
                item.customerRating = product.getCustomerReviewRating();
                item.customerCount = product.getCustomerReviewCount();
                add(item);
            }
        }
    }
}