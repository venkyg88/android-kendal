package com.staples.mobile.cfa.order;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.LinearLayoutWithOverlay;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */

public class OrderFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = OrderFragment.class.getSimpleName();
    MainActivity activity;
    EasyOpenApi easyOpenApi;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    LinearLayoutWithOverlay overlayableLayout;
    OrderAdapter adapter;
    TextView orderErrorTV;
    LinearLayout trackingLayout;
    Animation bottomSheetSlideUpAnimation;
    Animation bottomSheetSlideDownAnimation;
    ArrayList<OrderShipmentListItem> orderShipmentListItems;
    OrderStatusDetailCallback orderStatusDetailCallback;
    int numOrdersToRetrieve;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        activity = (MainActivity) getActivity();
        orderShipmentListItems = new ArrayList<OrderShipmentListItem>();
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        // setup overlay
        overlayableLayout = (LinearLayoutWithOverlay)view.findViewById(R.id.overlayable_layout);
        overlayableLayout.setOverlayView(view.findViewById(R.id.overlay));
        overlayableLayout.setOnSwallowedClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissTrackingInfo();
            }
        });

        // setup tracking bottom sheet
        trackingLayout = (LinearLayout)view.findViewById(R.id.tracking_layout);
        bottomSheetSlideUpAnimation = AnimationUtils.loadAnimation(activity, R.anim.bottomsheet_slide_up);
        bottomSheetSlideDownAnimation = AnimationUtils.loadAnimation(activity, R.anim.bottomsheet_slide_down);
        bottomSheetSlideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {
                overlayableLayout.showOverlay(true);
                trackingLayout.setVisibility(View.VISIBLE);
            }
            @Override public void onAnimationEnd(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }
        });
        bottomSheetSlideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                trackingLayout.setVisibility(View.INVISIBLE);
                overlayableLayout.showOverlay(false);
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
        Tracker.getInstance().trackStateForOrders(); // Analytics
    }

    @Override
    public void onClick(View view) {
        OrderShipmentListItem order;
        switch(view.getId()) {
            case R.id.trackShipmentBtn:
                order = adapter.getItem((int)view.getTag());
                showTrackingInfo(order);
                break;
            case R.id.orderReceiptBtn:
                order = adapter.getItem((int)view.getTag());
                Fragment orderDetailsFragment = Fragment.instantiate(activity, OrderReceiptFragment.class.getName());
                Bundle bundle = new Bundle();
                bundle.putSerializable("orderData", order);
                orderDetailsFragment.setArguments(bundle);
                ((MainActivity) activity).navigateToFragment(orderDetailsFragment);
                break;
            default:
                break;
        }
    }

    private void showTrackingInfo(final OrderShipmentListItem order) {
        activity.showProgressIndicator();
        final OrderStatus orderStatus = order.getOrderStatus();
        final Shipment shipment = order.getShipment();
        easyOpenApi.getMemberOrderTrackingShipment(order.getOrderStatus().getOrderNumber(),
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

                        String shipmentLabel = "Order# "+ orderStatus.getOrderNumber();
                        if (order.getShipments().size() > 1) {
                            shipmentLabel = "Shipment " + (order.getShipmentIndex()+1) + " of " + shipmentLabel;
                        }
                        shipmentLabelVw.setText(shipmentLabel);

                        StringBuilder scanBuf = new StringBuilder();
                        Shipment shipment = orderStatusDetail.getOrderStatus().get(0).getShipment().get(0);
                        if (shipment.getCartonWithSku() != null && shipment.getCartonWithSku().size() > 0) {
                            CartonWithSku cartonWithSku = shipment.getCartonWithSku().get(0);
                            carrierVw.setText(r.getString(R.string.carrier) + ": " + cartonWithSku.getCarrierCode());
                            trackingNumberVw.setText(r.getString(R.string.tracking_number) + " " + cartonWithSku.getTrackingNumber());
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("M/d/yy h:mm aa");
                            if (cartonWithSku.getScanData() != null) {
                                for (ScanData scanData : cartonWithSku.getScanData()) {
                                    Date date = OrderShipmentListItem.parseDate(scanData.getScanDateAndTime());
                                    if (scanBuf.length() > 0) {
                                        scanBuf.append("\n");
                                    }
                                    scanBuf.append(dateFormatter.format(date));
                                    if (!TextUtils.isEmpty(scanData.getScanDescription())) {
                                        scanBuf.append("  ").append(scanData.getScanDescription());
                                    }
                                }
                            }
                        } else {
                            carrierVw.setText(null);
                            trackingNumberVw.setText(null);
                        }
                        if (scanBuf.length() == 0) {
                            scanBuf.append(r.getString(R.string.no_detailed_tracking));
                        }
                        deliveryScansVw.setText(scanBuf.toString());

                        closeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dismissTrackingInfo();
                            }
                        });

                        // make visible with animation
                        overlayableLayout.showOverlay(true); // do this before animation to avoid flash between progress overlay and this one
                        trackingLayout.startAnimation(bottomSheetSlideUpAnimation);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        activity.hideProgressIndicator();
                        activity.showErrorDialog(ApiError.getErrorMessage(error));
                    }
                });
    }

    private void dismissTrackingInfo() {
        trackingLayout.startAnimation(bottomSheetSlideDownAnimation);
    }

    private void fill(){
        activity.showProgressIndicator();
        // get orders in descending date order up to some maximum
        easyOpenApi.getMemberOrderHistory("orderDate", "DESC", 1, 30, new Callback<OrderDetail>() {
            @Override
            public void success(OrderDetail orderDetail, Response response) {
                orderStatusDetailCallback = new OrderStatusDetailCallback(); // only need to create one instance of the item detail callback
                numOrdersToRetrieve = 0;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)-2); // 2 years ago
                long ageLimit = calendar.getTimeInMillis();
                for (OrderHistory order : orderDetail.getOrderHistory()) {
                    Date orderDate = OrderShipmentListItem.parseDate(order.getOrderDate());
                    // when orders are beyond a certain age limit, quit retrieving
                    if (orderDate.getTime() < ageLimit) {
                        break;
                    }
                    easyOpenApi.getMemberOrderStatus(order.getOrderNumber(), orderStatusDetailCallback);
                    numOrdersToRetrieve++;
                }
                if (numOrdersToRetrieve == 0) {
                    orderErrorTV.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    activity.hideProgressIndicator();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                orderErrorTV.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                activity.hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
                Log.i(TAG, "Fail Response Order History " + error.getUrl() + " " + error.getMessage());
            }
        });
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
            OrderStatus orderStatus =  orderStatusDetail.getOrderStatus().get(0);
            List<Shipment> shipments = orderStatus.getShipment();
            int numShipments = shipments.size();
            for (int i = 0; i < numShipments; i++) {
                // create an order object for each shipment with an index into the shipment of interest
                orderShipmentListItems.add(new OrderShipmentListItem(i, orderStatus));
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
                if (orderShipmentListItems.size() > 0) {
                    // sort order shipments by descending order date and order number, then by shipment info
                    Collections.sort(orderShipmentListItems);
                    adapter.fill(orderShipmentListItems);
                } else {
                    orderErrorTV.setVisibility(View.VISIBLE);
                }
                activity.hideProgressIndicator();
            }
        }
    };
}
