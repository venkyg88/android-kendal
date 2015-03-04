package com.staples.mobile.cfa.order;

import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.CartonWithSku;
import com.staples.mobile.common.access.easyopen.model.member.OrderDetail;
import com.staples.mobile.common.access.easyopen.model.member.OrderHistory;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatusDetail;
import com.staples.mobile.common.access.easyopen.model.member.ScanData;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */

public class OrderFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "OrderFragment";
    MainActivity activity;
    EasyOpenApi easyOpenApi;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    OrderAdapter adapter;
    TextView orderErrorTV;
    LinearLayout trackingLayout;
    Animation bottomSheetSlideUpAnimation;
    Animation bottomSheetSlideDownAnimation;
    ArrayList<OrderShipmentListItem> shipmentListItems;
    OrderStatusDetailCallback orderStatusDetailCallback;
    int numOrdersToRetrieve;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        activity = (MainActivity) getActivity();
        shipmentListItems = new ArrayList<OrderShipmentListItem>();
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        trackingLayout = (LinearLayout)view.findViewById(R.id.tracking_layout);
        bottomSheetSlideUpAnimation = AnimationUtils.loadAnimation(activity, R.anim.bottomsheet_slide_up);
        bottomSheetSlideDownAnimation = AnimationUtils.loadAnimation(activity, R.anim.bottomsheet_slide_down);
        bottomSheetSlideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {
                trackingLayout.setVisibility(View.VISIBLE);
            }
            @Override public void onAnimationEnd(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }
        });
        bottomSheetSlideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                trackingLayout.setVisibility(View.INVISIBLE);
            }
            @Override public void onAnimationRepeat(Animation animation) { }
        });

        orderErrorTV = (TextView)view.findViewById(R.id.orderErrorTV);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.orders_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new OrderAdapter(getActivity(), this);
        mRecyclerView.setAdapter(adapter);
        fill();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ORDER);
    }

    @Override
    public void onClick(View view) {
        OrderShipmentListItem shipment;
        switch(view.getId()) {
            case R.id.trackShipmentBtn:
                shipment = adapter.getItem((int)view.getTag());
                showTrackingInfo(shipment);
                break;
            case R.id.orderReceiptBtn:
                shipment = adapter.getItem((int)view.getTag());
                Fragment orderDetailsFragment = Fragment.instantiate(activity, OrderReceiptFragment.class.getName());
                Bundle bundle = new Bundle();
                bundle.putSerializable("orderData", shipment);
                orderDetailsFragment.setArguments(bundle);
                ((MainActivity) activity).navigateToFragment(orderDetailsFragment);
                break;
            default:
                break;
        }
    }

    private void showTrackingInfo(final OrderShipmentListItem shipment) {
        activity.showProgressIndicator();
        easyOpenApi.getMemberOrderTrackingShipment(shipment.getOrderNumber(),
                shipment.getShipmentNumber(), "000", new Callback<OrderStatusDetail>() {

                    @Override
                    public void success(OrderStatusDetail orderStatusDetail, Response response) {
                        activity.hideProgressIndicator();
                        Resources r = activity.getResources();
                        TextView shipmentLabelVw = (TextView)trackingLayout.findViewById(R.id.shipment_label);
                        TextView trackingNumberVw = (TextView)trackingLayout.findViewById(R.id.tracking_number);
                        TextView carrierVw = (TextView)trackingLayout.findViewById(R.id.carrier);
                        TextView deliveryScansVw = (TextView)trackingLayout.findViewById(R.id.delivery_scans);
                        View closeButton = trackingLayout.findViewById(R.id.close_button);

                        String shipmentLabel = "Order# "+ shipment.getOrderNumber();
                        if (shipment.getShipmentIndex() != null) {
                            shipmentLabel = "Shipment " + shipment.getShipmentIndex() + " of " + shipmentLabel;
                        }
                        shipmentLabelVw.setText(shipmentLabel);

                        CartonWithSku cartonWithSku = orderStatusDetail.getOrderStatus().get(0).getShipment().get(0).getCartonWithSku().get(0);
                        carrierVw.setText(r.getString(R.string.carrier) + ": " + cartonWithSku.getCarrierCode());
                        trackingNumberVw.setText(r.getString(R.string.tracking_number) + ": " + cartonWithSku.getTrackingNumber());
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
                        StringBuilder scanBuf = new StringBuilder();
                        if (cartonWithSku.getScanData() != null) {
                            for (ScanData scanData : cartonWithSku.getScanData()) {
                                Date date = parseDate(scanData.getScanDateAndTime());
                                if (scanBuf.length() > 0) {
                                    scanBuf.append("\n");
                                }
                                scanBuf.append(dateFormatter.format(date));
                                if (!TextUtils.isEmpty(scanData.getScanDescription())) {
                                    scanBuf.append("  ").append(scanData.getScanDescription());
                                }
                            }
                        }
                        if (scanBuf.length() == 0) {
                            scanBuf.append(r.getString(R.string.no_detailed_tracking));
                        }
                        deliveryScansVw.setText(scanBuf.toString());

                        closeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                trackingLayout.startAnimation(bottomSheetSlideDownAnimation);
                            }
                        });

                        // make visible with animation
                        trackingLayout.startAnimation(bottomSheetSlideUpAnimation);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        activity.hideProgressIndicator();
                        activity.showErrorDialog(ApiError.getErrorMessage(error));
                    }
                });
    }

    private void fill(){
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
                orderErrorTV.setVisibility(View.VISIBLE);
                activity.hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
                Log.i(TAG, "Fail Response Order History " + error.getUrl() + " " + error.getMessage());
            }
        });
    }


    private SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
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


    /**
     *
     * order detail callback
     *
     * */
    private class OrderStatusDetailCallback implements Callback<OrderStatusDetail> {

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
            if (numOrdersToRetrieve == 0) {
                if (shipmentListItems.size() > 0) {
                    // sort order shipments by descending order date and order number, then by shipment info
                    sortShipments(shipmentListItems);
                    adapter.fill(shipmentListItems);
                } else {
                    orderErrorTV.setVisibility(View.VISIBLE);
                }
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