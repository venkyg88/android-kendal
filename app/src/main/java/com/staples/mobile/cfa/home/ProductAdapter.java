package com.staples.mobile.cfa.home;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProductAdapter
    extends ArrayAdapter<ProductItem>
    implements Callback<Browse> {

    private static final String TAG = ProductAdapter.class.getSimpleName();

    private static final int VIEW_BANNER = 0;
    private static final int VIEW_PRODUCT = 1;




    private static final int MAXFETCH = 50;

    private Activity activity;
    private ConfigItem configItem;
    private LayoutInflater inflater;

    public ProductAdapter(Activity activity, ConfigItem configItem) {
        super(activity, 0);
        this.activity = activity;
        this.configItem = configItem;
        inflater = activity.getLayoutInflater();
    }

    /* Views */

    @Override
    public int getViewTypeCount() {
        return (2);
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0) return (VIEW_BANNER);
        else return (VIEW_PRODUCT);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return(false);
    }

    @Override
    public boolean isEnabled(int position) {
        return(position>0);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ProductItem item = getItem(position);

        int type = getItemViewType(position);
        if (view==null) {
            switch(type) {
                case VIEW_BANNER:
                    view = inflater.inflate(R.layout.banner_item, parent, false);
                    ImageView banner = (ImageView) view.findViewById(R.id.image);
                    Picasso.with(activity).load(item.imageUrl).into(banner);
                    break;

                case VIEW_PRODUCT:
                    view = inflater.inflate(R.layout.product_item, parent, false);
                    break;
            }
        }

        if (type==VIEW_PRODUCT) {
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(item.title);
        }
        return(view);
    }

    void fill() {
        add(new ProductItem(null, configItem.bannerUrl, null));
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getCategory(configItem.identifier, null, MAXFETCH, null, null, this);
    }

    // Retrofit EasyOpen API call

    @Override
    public void success(Browse browse, Response response) {
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) {
            notifyDataSetChanged();
            return;
        }

        List<Product> products = categories.get(0).getProduct();
        if (products != null) {
            for(Product product : products) {
                ProductItem item = new ProductItem(product.getProductName(), null, product.getSku());
                add(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        notifyDataSetChanged();
    }
}
