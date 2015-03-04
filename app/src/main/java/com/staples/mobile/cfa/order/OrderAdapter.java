package com.staples.mobile.cfa.order;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Avinash Dodda.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> implements View.OnClickListener{

    private ArrayList<OrderShipmentListItem> array;
    private Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumTV;
        TextView numItemsTV;
        TextView orderStatusTV;
        TextView expectedDelivery;
        Button trackShipmentBtn;
        Button viewRecieptBtn;

        public ViewHolder(View v) {
            super(v);
            orderNumTV = (TextView) v.findViewById(R.id.orderNumTv);
            numItemsTV = (TextView) v.findViewById(R.id.numItemsTV);
            orderStatusTV = (TextView) v.findViewById(R.id.orderStatusTV);
            expectedDelivery = (TextView) v.findViewById(R.id.orderDeliveryTV);
            trackShipmentBtn = (Button) v.findViewById(R.id.trackShipmentBtn);
            viewRecieptBtn = (Button) v.findViewById(R.id.orderReceiptBtn);
        }
    }

    public OrderAdapter(Activity activity) {
        this.activity = activity;
        this.array = new ArrayList<OrderShipmentListItem>();
    }

    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final OrderShipmentListItem shipment = array.get(position);
        Resources r = activity.getResources();

        String orderNumberText = "Order# "+ shipment.getOrderStatus().getOrderNumber();
        if (shipment.getShipmentIndex() != null) {
            orderNumberText += " - Shipment " + shipment.getShipmentIndex();
        }
        holder.orderNumTV.setText(orderNumberText);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        holder.expectedDelivery.setText("Estimated Delivery - "+ formatter.format(shipment.getScheduledDeliveryDate()));

        holder.orderStatusTV.setText(shipment.getShipmentStatusDescription());
        holder.numItemsTV.setText(r.getQuantityString(R.plurals.cart_qty, shipment.getQuantity(), shipment.getQuantity()));

        holder.trackShipmentBtn.setOnClickListener(this);
        holder.viewRecieptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment orderDetailsFragment = Fragment.instantiate(activity, OrderReceiptFragment.class.getName());
                Bundle bundle = new Bundle();
                bundle.putSerializable("orderData", shipment);
                orderDetailsFragment.setArguments(bundle);
                ((MainActivity) activity).navigateToFragment(orderDetailsFragment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void fill(List<OrderShipmentListItem> items) {
        array.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.trackShipmentBtn:
                ((MainActivity)activity).showNotificationBanner("In Progress");
                break;
            default:
                break;
        }
    }
}
