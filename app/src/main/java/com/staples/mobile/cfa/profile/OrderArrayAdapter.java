package com.staples.mobile.cfa.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Avinash Dodda.
 */
public class OrderArrayAdapter extends ArrayAdapter<Shipment> implements View.OnClickListener{
    private LayoutInflater inflater;
    TextView orderNumTV;
    TextView numItemsTV;
    TextView orderStatusTV;
    TextView expectedDelivery;
    Button trackShipmentBtn;
    Button viewRecieptBtn;

    public OrderArrayAdapter(Context context) {
        super(context, R.layout.order_listview_row);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.order_listview_row, parent, false);
        orderNumTV = (TextView)rowView.findViewById(R.id.orderNumTv);
        numItemsTV = (TextView)rowView.findViewById(R.id.numItemsTV);
        orderStatusTV = (TextView)rowView.findViewById(R.id.orderStatusTV);
        expectedDelivery = (TextView)rowView.findViewById(R.id.orderDeliveryTV);
        trackShipmentBtn = (Button)rowView.findViewById(R.id.trackShipmentBtn);
        viewRecieptBtn = (Button)rowView.findViewById(R.id.orderReceiptBtn);
        trackShipmentBtn.setOnClickListener(this);
        viewRecieptBtn.setOnClickListener(this);

        Shipment shipment = getItem(position);

        orderNumTV.setText("Order# "+ shipment.getOrderNumber());
        double itemsOrdered = 0.0;
        for(ShipmentSKU sku : shipment.getShipmentSku()) {
            itemsOrdered += Double.parseDouble(sku.getQtyOrdered());
        }


        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                    Locale.ENGLISH);
            Date parsedDate = sdf.parse(shipment.getScheduledDeliveryDate());
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
            expectedDelivery.setText("Estimated Delivery - "+ formatter.format(parsedDate));
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
        orderStatusTV.setText(shipment.getShipmentStatusDescription());
        numItemsTV.setText(""+ (int)itemsOrdered + " Items");
        return rowView;
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.trackShipmentBtn:
                ((MainActivity)getContext()).showNotificationBanner("hi");
                break;
            case  R.id.orderReceiptBtn:
                ((MainActivity)getContext()).showNotificationBanner("hello");
                break;
            default:
                break;
        }
    }
}
