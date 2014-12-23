package com.staples.mobile.cfa.profile;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Avinash Dodda.
 */
public class OrderArrayAdapter extends ArrayAdapter<OrderStatus>{
    private LayoutInflater inflater;

    public OrderArrayAdapter(Context context) {
        super(context, R.layout.order_listview_row);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.order_listview_row, parent, false);

        OrderStatus orderStatus = getItem(position);
        TextView dateTv = (TextView)rowView.findViewById(R.id.dateTV);
        TextView numItemsTV = (TextView)rowView.findViewById(R.id.numItemsTV);
        TextView orderTotalTV = (TextView)rowView.findViewById(R.id.orderTotalTV);
        TextView orderStatusTV = (TextView)rowView.findViewById(R.id.orderStatusTV);

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                    Locale.ENGLISH);
            Date parsedDate = sdf.parse(orderStatus.getOrderDate());
            SimpleDateFormat formatter = new SimpleDateFormat("MMM. d, yyyy");
            dateTv.setText(formatter.format(parsedDate));
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
        orderTotalTV.setText("$"+orderStatus.getTotal());
        orderStatusTV.setText(orderStatus.getShipment().get(0).getShipmentStatusDescription());

        double itemsOrdered = 0.0;
        for(ShipmentSKU shipment : orderStatus.getShipment().get(0).getShipmentSku() ) {
            itemsOrdered += Double.parseDouble(shipment.getQtyOrdered());
        }
        numItemsTV.setText(""+ itemsOrdered + " Items");
        return rowView;
    }
}
