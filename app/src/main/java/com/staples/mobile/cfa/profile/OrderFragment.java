package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OrderFragment extends Fragment {
    private static final String TAG = "OrderFragment";
    MainActivity activity;
    EasyOpenApi easyOpenApi;
    String orderNumber;
    ListView listview;
    OrderArrayAdapter adapter;
    TextView orderTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        listview = (ListView) view.findViewById(R.id.orderListView);
        adapter = new OrderArrayAdapter(activity);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(adapter != null) {
                    Fragment orderDetailsFragment = Fragment.instantiate(activity, OrderDetailsFragment.class.getName());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("orderData", adapter.getItem(position));
                    orderDetailsFragment.setArguments(bundle);
                    activity.navigateToFragment(orderDetailsFragment);
                }

            }
        });
        fill();
        return (view);
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
                                shipment.setOrderNumber(orderStatusDetail.getOrderStatus().get(0).getOrderNumber());
                                shipment.setOrderDate(orderStatusDetail.getOrderStatus().get(0).getOrderDate());
                                adapter.add(shipment);
                            }
                            activity.hideProgressIndicator();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            activity.hideProgressIndicator();
                            activity.showErrorDialog(ApiError.getErrorMessage(error));
                            Log.i("Fail Response Order Status", error.getUrl() + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {
                orderTV = (TextView)getView().findViewById(R.id.orderTV);
                orderTV.setVisibility(View.VISIBLE);
                orderTV.setText("No Orders Found");
                activity.hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
                Log.i("Fail Response Order History", error.getUrl() + error.getMessage());
            }
        });
    }
}
