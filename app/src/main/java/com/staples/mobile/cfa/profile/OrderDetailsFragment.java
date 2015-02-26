package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Avinash Dodda.
 */
public class OrderDetailsFragment extends Fragment {
    private static final String TAG = "OrderDetailsFragment";
    MainActivity activity;
    Shipment shipment;
    TextView orderNumber;
    TextView orderTotal;
    TextView orderDate;
    TextView orderQty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        activity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.order_detail_fragment, container, false);
        orderNumber = (TextView)view.findViewById(R.id.orderNumber);
        orderDate = (TextView)view.findViewById(R.id.orderDate);
        orderTotal = (TextView)view.findViewById(R.id.orderTotal);
        orderQty = (TextView)view.findViewById(R.id.orderQtyLbl);

        Bundle args = getArguments();
        if(args != null) {
            shipment = (Shipment)args.getSerializable("orderData");
            if(shipment != null) {
                double itemsOrdered = 0.0;
                double total = 0.0;
                for(ShipmentSKU sku : shipment.getShipmentSku()) {
                    itemsOrdered += Double.parseDouble(sku.getQtyOrdered());
                    total += Double.parseDouble(sku.getLineTotal());
                }
                orderNumber.setText(shipment.getOrderNumber());
                orderTotal.setText("$"+total);
                orderQty.setText("("+itemsOrdered+" Items)");
                try{
                    SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                            Locale.ENGLISH);
                    Date parsedDate = sdf.parse(shipment.getOrderDate());
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM. d, yyyy");
                    orderDate.setText(formatter.format(parsedDate));
                }catch (ParseException e)
                {
                    e.printStackTrace();
                    Crittercism.logHandledException(e);
                }
            }
        }
        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ORDER);
    }
}
