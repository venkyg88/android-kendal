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
import com.staples.mobile.common.access.easyopen.model.member.OrderStatusDetail;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;

import java.util.ArrayList;

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
    String orderNumber;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    OrderAdapter adapter;
    TextView orderErrorTV;
    ArrayList<Shipment> modifiedShipment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        activity = (MainActivity) getActivity();
        modifiedShipment = new ArrayList<Shipment>();
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.orders_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        fill();
        adapter = new OrderAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ORDER);
    }

    public void fill(){
        activity.showProgressIndicator();

        easyOpenApi.getMemberOrderDetails(new Callback<OrderDetail>() {
            @Override
            public void success(OrderDetail orderDetail, Response response) {
                for(OrderHistory order : orderDetail.getOrderHistory()) {
                    orderNumber = order.getOrderNumber();
                    easyOpenApi.getMemberOrderStatus(orderNumber,new Callback<OrderStatusDetail>() {
                        @Override
                        public void success(OrderStatusDetail orderStatusDetail, Response response) {
                            for(Shipment shipment: orderStatusDetail.getOrderStatus().get(0).getShipment()){
                                shipment.setOrderStatus(orderStatusDetail.getOrderStatus().get(0));
                                modifiedShipment.add(shipment);
                            }
                            adapter.fill(modifiedShipment);
                            activity.hideProgressIndicator();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            activity.hideProgressIndicator();
                            activity.showErrorDialog(ApiError.getErrorMessage(error));
                            Log.i(TAG, "Fail Response Order Status " + error.getUrl() + " " + error.getMessage());
                        }
                    });
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
}