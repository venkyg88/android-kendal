package com.staples.mobile.cfa.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.OrderHistory;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatusDetail;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class OrderArrayAdapter extends ArrayAdapter<OrderStatus>{
    private final Context context;
    private final List<OrderStatus> values;

    public OrderArrayAdapter(Context context, List<OrderStatus> values) {
        super(context, R.layout.order_listview_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.order_listview_row, parent, false);

        OrderStatus orderStatus = values.get(position);
        TextView dateTv = (TextView)rowView.findViewById(R.id.dateTV);
        TextView numItemsTV = (TextView)rowView.findViewById(R.id.numItemsTV);
        TextView orderTotalTV = (TextView)rowView.findViewById(R.id.orderTotalTV);
        TextView orderStatusTV = (TextView)rowView.findViewById(R.id.orderStatusTV);

        dateTv.setText(orderStatus.getOrderDate());
        orderTotalTV.setText(orderStatus.getTotal());
        orderStatusTV.setText(orderStatus.getShipment().get(0).getShipmentStatusDescription());

        return rowView;
    }

}
