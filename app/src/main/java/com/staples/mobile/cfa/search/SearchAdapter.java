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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;
import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.search.Product;
import com.staples.mobile.common.access.easyopen.model.search.Search;
import com.staples.mobile.common.access.easyopen.model.search.SearchResult;
import com.staples.mobile.common.access.easyopen.model.search.ThumbnailImage;

import org.apache.http.HttpStatus;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class SearchAdapter extends ArrayAdapter<SearchResultRowItem> implements Callback<SearchResult>{
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
    private SearchWrapperLayout wrapper;

    private static Search mySearch;
    private int search_counter = 0;
    private LayoutInflater inflater;
    private Activity activity;
    private String keyword;
    private int layout;

    private class SearchService {
        private final String API_URL = "http://qapi.staples.com";
        private final String TAG = "SearchService";
        private final OkHttpClient okHttpClient = new OkHttpClient();

        //@TODO Retrofit logging level
        public EasyOpenApi getSearchApi() {

            RestAdapter restAdapter = new RestAdapter
                    .Builder()
                    .setClient(new OkClient(okHttpClient))
                            //.setErrorHandler(new ErrorRetrofitHandlerException())
                    .setEndpoint(API_URL)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setLog(new AndroidLog(TAG))
                    .build();
            EasyOpenApi searchApi = restAdapter.create(EasyOpenApi.class);
            return searchApi;
        }
    }

    public SearchAdapter(Activity activity, SearchWrapperLayout wrapper) {
        super(activity, 0);
        this.activity = activity;
        this.wrapper = wrapper;
        inflater = activity.getLayoutInflater();
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public void getSearchResults(final String keyword)
    {
        wrapper.setState(SearchWrapperLayout.State.LOADING);

        this.keyword = keyword;
        if (easyOpenApi == null) {
            SearchService ss = new SearchService();
            easyOpenApi = ss.getSearchApi();
        }

        //easyOpenApi.searchApi(v, storeId, catalogId, locale, zipCode, term, page,
        //					  limit, sort, client_id, filterId, callback);
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
                        Log.d(TAG, "Retro successed: product name[0]:" + mySearch.getProduct()[0].getProductName());
                        setSearchResult();
                        wrapper.setState(SearchWrapperLayout.State.DONE);
                    }
                    else{
                        wrapper.setState(SearchWrapperLayout.State.EMPTY);
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
        wrapper.setState(SearchWrapperLayout.State.EMPTY);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchResultRowItem rowItem = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.search_result_row_wide, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);
        if (title!=null) title.setText(rowItem.getProduceName());

        RatingStars ratingStars = (RatingStars) convertView.findViewById(R.id.rating);
        ratingStars.setRating(Float.parseFloat(rowItem.getRating()), Integer.parseInt(rowItem.getReviewAmount()));

        PriceSticker priceSticker = (PriceSticker) convertView.findViewById(R.id.pricing);
        priceSticker.setPricing(Float.parseFloat(rowItem.getCurrentPrice()), rowItem.getUnitOfMeasure());

        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        if (rowItem.getImageUrl() == null) {
            image.setImageDrawable(noPhoto);
        }
        else {
            Picasso.with(activity).load(rowItem.getImageUrl()).error(noPhoto).into(image);
        }

        return convertView;
    }

    private void setSearchResult (){
        Search searchObj = mySearch;

        for (int i = 0; i < searchObj.getProduct().length; i++) {
            Product product = searchObj.getProduct()[i];
            String productName = product.getProductName();
            String currentPrice = String.valueOf(product.getPricing()[0].getFinalPrice());
            String previousPrice = String.valueOf(product.getPricing()[0].getPrice());
            String rating = String.valueOf(product.getCustomerReviewRating());
            String reviewAmount = String.valueOf(product.getCustomerReviewCount());
            String sku = String.valueOf(product.getSku());
            String unitOfMeasure = String.valueOf(product.getPricing()[0].getUnitOfMeasure());

            ThumbnailImage thumbNailImage = product.getThumbnailImage()[0];
            String thumbnailImageURL = thumbNailImage.getUrl();
            StringBuilder sb = new StringBuilder();
            sb.append(thumbnailImageURL);
            sb.append("&hei=100&wid=100&resMode=sharp");
            String imageUrl = sb.toString();

            SearchResultRowItem item = new SearchResultRowItem(i, productName, previousPrice,
                    currentPrice, reviewAmount, rating, sku, unitOfMeasure, imageUrl);
            add(item);
        }
    }
}