package com.staples.mobile.cfa.weeklyad;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.easyopen.util.WeeklyAdImageUrlHelper;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avinash Dodda.
 */

public class WeeklyAdListAdapter extends RecyclerView.Adapter<WeeklyAdListAdapter.ViewHolder>{

    private ArrayList<Data> array;
    private Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView weeklyAdListDealTV;
        public TextView weeklyAdDealPriceInfoTV;
        public TextView weeklyAdDealAvailabilityTV;
        public TextView priceExtension;
        ImageView weeklyAdListIV;
        ImageView weeklyAdAction;

        public ViewHolder(View v) {
            super(v);
            weeklyAdListDealTV = (TextView)v.findViewById(R.id.weekly_ad_list_title_text);
            weeklyAdDealPriceInfoTV = (TextView)v.findViewById(R.id.weekly_ad_list_price);
            weeklyAdDealAvailabilityTV = (TextView)v.findViewById(R.id.inStoreTV);
            priceExtension = (TextView)v.findViewById(R.id.weeklyad_price_extension);
            weeklyAdAction = (ImageView)v.findViewById(R.id.weeklyad_sku_action);
            weeklyAdListIV = (ImageView)v.findViewById(R.id.weekly_ad_list_image);
        }
    }

    public WeeklyAdListAdapter(Activity activity) {
        this.activity = activity;
        this.array = new ArrayList<Data>();
    }

    @Override
    public WeeklyAdListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weekly_ad_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Data data = array.get(position);

        if(data.getFineprint().contains("In store only") || data.getRetailerproductcode() == "") {
          holder.weeklyAdDealAvailabilityTV.setVisibility(View.VISIBLE);
          holder.weeklyAdDealAvailabilityTV.setText("Available in store only");
        }
        else {
            holder.weeklyAdAction.setVisibility(View.VISIBLE);
            holder.weeklyAdAction.setImageDrawable(holder.weeklyAdAction.getResources().getDrawable(R.drawable.add_to_cart));
            holder.weeklyAdAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final ImageView buttonVw = (ImageView)view;
                    ((MainActivity)activity).showProgressIndicator();
                    buttonVw.setImageDrawable(buttonVw.getResources().getDrawable(R.drawable.ic_android));
                    CartApiManager.addItemToCart(data.getRetailerproductcode(), 1, new CartApiManager.CartRefreshCallback() {
                        @Override
                        public void onCartRefreshComplete(String errMsg) {
                            ((MainActivity)activity).hideProgressIndicator();
                            ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
                            // if success
                            if (errMsg == null) {
                                buttonVw.setImageDrawable(buttonVw.getResources().getDrawable(R.drawable.added_to_cart));
                                ((MainActivity)activity).showNotificationBanner(R.string.cart_updated_msg);
                            } else {
                                buttonVw.setImageDrawable(buttonVw.getResources().getDrawable(R.drawable.add_to_cart));
                                // if non-grammatical out-of-stock message from api, provide a nicer message
                                if (errMsg.contains("items is out of stock")) {
                                    errMsg = activity.getResources().getString(R.string.avail_outofstock);
                                }
                                ((MainActivity)activity).showErrorDialog(errMsg);
                            }
                        }
                    });
                }
            });
        }

        holder.weeklyAdListDealTV.setText(data.getTitle());
        holder.weeklyAdDealPriceInfoTV.setText(data.getPrice());

        if(data.getPrice().contains("$")) {
            holder.priceExtension.setVisibility(View.VISIBLE);
            holder.priceExtension.setText("each");
        }

        Picasso.with(activity)
                .load(WeeklyAdImageUrlHelper.getUrl(60,100, data.getImage()))
                .into(holder.weeklyAdListIV);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void fill(List<Data> items) {
        array.clear();
        array.addAll(items);
        notifyDataSetChanged();
    }
}
