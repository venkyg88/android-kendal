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
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private ArrayList<OrderShipmentListItem> array;
    private Activity activity;
    View.OnClickListener onClickListener;

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

    public OrderAdapter(Activity activity, View.OnClickListener onClickListener) {
        this.activity = activity;
        this.onClickListener = onClickListener;
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
        OrderShipmentListItem order = array.get(position);
        OrderStatus orderStatus = order.getOrderStatus();
        Shipment shipment = order.getShipment();

        Resources r = activity.getResources();

        String orderNumberText = "Order# "+ orderStatus.getOrderNumber();
        if (order.getShipments().size() > 1) {
            orderNumberText += " - Shipment " + (order.getShipmentIndex() + 1);
        }
        holder.orderNumTV.setText(orderNumberText);

        // determine item qty of shipment
        int totalItemQtyOfShipment = 0;
        for (ShipmentSKU shipmentSku : shipment.getShipmentSku()) {
            int qtyOrdered = (int)Double.parseDouble(shipmentSku.getQtyOrdered()); // using parseDouble since quantity string is "1.0"
            totalItemQtyOfShipment += qtyOrdered;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        boolean delivered = "DLV".equals(shipment.getShipmentStatusCode());
        String deliveryDate = r.getString(delivered? R.string.delivered_date : R.string.estimated_delivery);
        deliveryDate += " - " + formatter.format(OrderShipmentListItem.parseDate(delivered?
                shipment.getActualShipDate() : shipment.getScheduledDeliveryDate()));
        holder.expectedDelivery.setText(deliveryDate);

        holder.orderStatusTV.setText(shipment.getShipmentStatusDescription());
        holder.numItemsTV.setText(r.getQuantityString(R.plurals.cart_qty, totalItemQtyOfShipment, totalItemQtyOfShipment));

        holder.trackShipmentBtn.setTag(position);
        holder.viewRecieptBtn.setTag(position);

        holder.trackShipmentBtn.setOnClickListener(onClickListener);
        holder.viewRecieptBtn.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public OrderShipmentListItem getItem(int position) {
        return array.get(position);
    }

    public void fill(List<OrderShipmentListItem> items) {
        array.addAll(items);
        notifyDataSetChanged();
    }

}
