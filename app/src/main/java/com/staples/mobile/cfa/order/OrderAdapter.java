package com.staples.mobile.cfa.order;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private ArrayList<OrderShipmentListItem> array;
    private Activity activity;
    View.OnClickListener onClickListener;
    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderTotalTV;
        TextView numItemsTV;
        TextView orderStatusTV;
        TextView orderDateTV;
        Button trackShipmentBtn;
        Button viewRecieptBtn;

        public ViewHolder(View v) {
            super(v);
            orderDateTV = (TextView) v.findViewById(R.id.order_date);
            orderStatusTV = (TextView) v.findViewById(R.id.order_status);
            numItemsTV = (TextView) v.findViewById(R.id.order_item_count);
            orderTotalTV = (TextView) v.findViewById(R.id.order_total);
            trackShipmentBtn = (Button) v.findViewById(R.id.track_shipment_btn);
            viewRecieptBtn = (Button) v.findViewById(R.id.order_reciept_btn);
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

        // determine item qty of shipment
        int totalItemQtyOfShipment = 0;
        for (ShipmentSKU shipmentSku : shipment.getShipmentSku()) {
            int qtyOrdered = (int)Double.parseDouble(shipmentSku.getQtyOrdered()); // using parseDouble since quantity string is "1.0"
            totalItemQtyOfShipment += qtyOrdered;
        }

        if (orderStatus != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM. dd, yyyy", Locale.US);
            String orderDate = formatter.format(OrderShipmentListItem.parseDate(orderStatus.getOrderDate()));
            holder.orderDateTV.setText(orderDate);
            if (orderStatus.getGrandTotal() != null) {
                holder.orderTotalTV.setText(currencyFormatter.format(Float.parseFloat(orderStatus.getGrandTotal())));
            }
        }

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
