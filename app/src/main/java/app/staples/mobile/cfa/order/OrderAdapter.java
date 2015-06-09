package app.staples.mobile.cfa.order;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.staples.R;
import app.staples.mobile.cfa.util.MiscUtils;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public enum TrackType {
        SHIPPED("Shipped"),
        DELIVERED("Delivered");
        private String name;

        TrackType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    private ArrayList<OrderShipmentListItem> array;
    private Activity activity;
    View.OnClickListener onClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView orderTotalTV;
        private TextView numItemsTV;
        private TextView orderStatusTV;
        private TextView orderDateTV;
        private TextView orderNumTV;
        private TextView orderShipmentTV;
        private View trackShipmentBtn;
        private View viewRecieptBtn;

        public ViewHolder(View view) {
            super(view);
            orderDateTV = (TextView) view.findViewById(R.id.order_date);
            orderStatusTV = (TextView) view.findViewById(R.id.order_status);
            numItemsTV = (TextView) view.findViewById(R.id.order_item_count);
            orderTotalTV = (TextView) view.findViewById(R.id.order_total);
            orderNumTV = (TextView) view.findViewById(R.id.order_number);
            orderShipmentTV = (TextView) view.findViewById(R.id.order_shipment);
            trackShipmentBtn = view.findViewById(R.id.track_shipment_btn);
            viewRecieptBtn = view.findViewById(R.id.order_reciept_btn);
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
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
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

        if (orderStatus == null) {
            vh.orderDateTV.setText(null);
            vh.orderTotalTV.setText(null);
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM. dd, yyyy", Locale.US);
            String orderDate = formatter.format(OrderShipmentListItem.parseDate(orderStatus.getOrderDate()));
            vh.orderDateTV.setText(orderDate);
            if (orderStatus.getGrandTotal() == null) {
                vh.orderTotalTV.setText(null);
            } else {
                float shipmentTotal = 0.0f;
                for(ShipmentSKU shipmentSku : shipment.getShipmentSku()) {
                    if(shipmentSku != null) {
                        shipmentTotal += Float.parseFloat(shipmentSku.getLineTotal());
                    }
                }
                vh.orderTotalTV.setText(MiscUtils.getCurrencyFormat().format(shipmentTotal));
            }
        }

        vh.orderShipmentTV.setText("Shipment " + (order.getShipmentIndex()+1) + " of " + order.getShipments().size());
        vh.orderNumTV.setText("#" + orderStatus.getOrderNumber());
        vh.orderStatusTV.setText(shipment.getShipmentStatusDescription());
        vh.numItemsTV.setText(r.getQuantityString(R.plurals.cart_qty, totalItemQtyOfShipment, totalItemQtyOfShipment));

        vh.trackShipmentBtn.setVisibility(View.INVISIBLE);
        if (shipment.getShipmentStatusDescription().equals(TrackType.SHIPPED.toString())) {
            vh.trackShipmentBtn.setVisibility(View.VISIBLE);
        } else if (shipment.getShipmentStatusDescription().equals(TrackType.DELIVERED.toString())) {
            vh.trackShipmentBtn.setVisibility(View.VISIBLE);
        }

        vh.trackShipmentBtn.setTag(order);
        vh.viewRecieptBtn.setTag(order);

        vh.trackShipmentBtn.setOnClickListener(onClickListener);
        vh.viewRecieptBtn.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public OrderShipmentListItem getItem(int position) {
        return array.get(position);
    }

    public void clear() {
        array.clear();
    }

    public void fill(List<OrderShipmentListItem> items) {
        array.addAll(items);
        notifyDataSetChanged();
    }
}
