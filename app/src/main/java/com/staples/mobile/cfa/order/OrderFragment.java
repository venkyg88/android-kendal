package com.staples.mobile.cfa.order;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.OrderDetail;
import com.staples.mobile.common.access.easyopen.model.member.OrderHistory;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatusDetail;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */

public class OrderFragment extends Fragment {
    private static final String TAG = "OrderFragment";
    MainActivity activity;
    EasyOpenApi easyOpenApi;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    OrderAdapter adapter;
    TextView orderErrorTV;
    ArrayList<OrderShipmentListItem> shipmentListItems;
    OrderStatusDetailCallback orderStatusDetailCallback;
    int numOrdersToRetrieve;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        activity = (MainActivity) getActivity();
        shipmentListItems = new ArrayList<OrderShipmentListItem>();
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.orders_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new OrderAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);
        fill();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ORDER);
    }

    public void fill(){
        activity.showProgressIndicator();
        easyOpenApi.getMemberOrderHistory("orderDate", "DESC", 1, 10, new Callback<OrderDetail>() {
            @Override
            public void success(OrderDetail orderDetail, Response response) {
                orderStatusDetailCallback = new OrderStatusDetailCallback(); // only need to create one instance of the item detail callback
                numOrdersToRetrieve = orderDetail.getOrderHistory().size();
                for (OrderHistory order : orderDetail.getOrderHistory()) {
                    easyOpenApi.getMemberOrderStatus(order.getOrderNumber(), orderStatusDetailCallback);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                orderErrorTV = (TextView)getView().findViewById(R.id.orderErrorTV);
                orderErrorTV.setVisibility(View.VISIBLE);
                orderErrorTV.setText("No Orders Found");
                activity.hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
                Log.i(TAG, "Fail Response Order History " + error.getUrl() + " " + error.getMessage());
            }
        });
    }


    private class OrderStatusDetailCallback implements Callback<OrderStatusDetail> {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

        private Date parseDate(String date) {
            try {
                return dateFormat.parse(date);
            } catch (ParseException e) {
                // return oldest possible date
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(0);
                return cal.getTime();
            }
        }

        @Override
        public void success(OrderStatusDetail orderStatusDetail, Response response) {
            numOrdersToRetrieve--;
            List<Shipment> shipments = orderStatusDetail.getOrderStatus().get(0).getShipment();
            int numShipments = shipments.size();
            for (int i = 0; i < numShipments; i++) {
                Shipment shipment = shipments.get(i);
                shipment.setOrderStatus(orderStatusDetail.getOrderStatus().get(0));
                int totalItemQtyOfShipment = 0;
                List<OrderShipmentListItem.ShipmentSku> skus = new ArrayList<OrderShipmentListItem.ShipmentSku>();
                for (ShipmentSKU shipmentSku : shipment.getShipmentSku()) {
                    int qtyOrdered = (int)Double.parseDouble(shipmentSku.getQtyOrdered()); // using parseDouble since quantity string is "1.0"
                    totalItemQtyOfShipment += qtyOrdered;
                    skus.add(new OrderShipmentListItem.ShipmentSku(shipmentSku.getSkuNumber(),
                            shipmentSku.getSkuDescription(), qtyOrdered,
                            Float.parseFloat(shipmentSku.getLineTotal())));
                }
                OrderStatus orderStatus = orderStatusDetail.getOrderStatus().get(0);
                shipmentListItems.add(new OrderShipmentListItem((numShipments > 1)? i+1 : null,
                        shipment.getShipmentNumber(),
                        parseDate(shipment.getScheduledDeliveryDate()),
                        shipment.getShipmentStatusDescription(),
                        totalItemQtyOfShipment,
                        parseDate(orderStatus.getOrderDate()),
                        orderStatus.getOrderNumber(),
                        orderStatus, skus));
            }
            finishOrderDetail();
        }

        @Override
        public void failure(RetrofitError error) {
            numOrdersToRetrieve--;
            finishOrderDetail(); // since some calls may have succeeded
            activity.hideProgressIndicator();
            // DLS: do not display error message for order details calls that fail, the order simply won't show up in the list
            Log.i(TAG, "Fail Response Order Status Detail: " + error.getUrl() + " " + error.getMessage());
        }

        private void finishOrderDetail() {

            // DLS: need to wait until all items retrieved (otherwise the same items get added multiple times)
            if (numOrdersToRetrieve == 0 && shipmentListItems.size() > 0) {

                // sort order shipments by descending order date and order number, then by shipment info
                sortShipments(shipmentListItems);

                adapter.fill(shipmentListItems);
                activity.hideProgressIndicator();
            }
        }

        private void sortShipments(List<OrderShipmentListItem> listItems) {
            // sort orders by descending order date, then by shipment
            Collections.sort(listItems, new Comparator<OrderShipmentListItem>() {
                @Override
                public int compare(OrderShipmentListItem left, OrderShipmentListItem right) {
                    int result = 0;
                    // first sort by descending order date to make sure most recent are shown at the top
                    Date leftParsedDate = parseDate(left.getOrderStatus().getOrderDate());
                    Date rightParsedDate = parseDate(right.getOrderStatus().getOrderDate());
                    result = rightParsedDate.compareTo(leftParsedDate);
                    if (result == 0) {
                        // next sort by descending order number to make sure shipments of an order are grouped together
                        result = right.getOrderStatus().getOrderNumber().compareTo(left.getOrderStatus().getOrderNumber());
                        if (result == 0) {
                            // next sort by delivery date
                            result = right.getScheduledDeliveryDate().compareTo(left.getScheduledDeliveryDate());
                            if (result == 0) {
                                // next sort by shipment index
                                result = left.getShipmentIndex() - right.getShipmentIndex();
                            }
                        }
                    }
                    return result;
                }
            });
        }
    };
}