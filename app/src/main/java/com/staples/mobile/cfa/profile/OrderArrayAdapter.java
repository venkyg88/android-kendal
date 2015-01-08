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
import com.staples.mobile.common.access.easyopen.model.member.OrderStatusDetail;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Avinash Dodda.
 */
public class OrderArrayAdapter extends ArrayAdapter<Shipment>{
    private LayoutInflater inflater;

    public OrderArrayAdapter(Context context) {
        super(context, R.layout.order_listview_row);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.order_listview_row, parent, false);

        Shipment shipment = getItem(position);
        TextView dateTv = (TextView)rowView.findViewById(R.id.dateTV);
        TextView numItemsTV = (TextView)rowView.findViewById(R.id.numItemsTV);
        TextView orderTotalTV = (TextView)rowView.findViewById(R.id.orderTotalTV);
        TextView orderStatusTV = (TextView)rowView.findViewById(R.id.orderStatusTV);

        double itemsOrdered = 0.0;
        double total = 0.0;
        for(ShipmentSKU sku : shipment.getShipmentSku()) {
            itemsOrdered += Double.parseDouble(sku.getQtyOrdered());
            total += Double.parseDouble(sku.getLineTotal());
        }
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                    Locale.ENGLISH);
            Date parsedDate = sdf.parse(shipment.getOrderDate());
            SimpleDateFormat formatter = new SimpleDateFormat("MMM. d, yyyy");
            dateTv.setText(formatter.format(parsedDate));
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
        orderTotalTV.setText("$"+total);
        orderStatusTV.setText(shipment.getShipmentStatusDescription());
        numItemsTV.setText(""+ (int)itemsOrdered + " Items");
        return rowView;
    }
}
