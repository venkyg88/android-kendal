package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.OrderDetail;
import com.staples.mobile.common.access.easyopen.model.member.OrderHistory;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatusDetail;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OrderFragment extends Fragment {
    private static final String TAG = "Order Fragment";
    MainActivity activity;
    EasyOpenApi easyOpenApi;
    String orderNumber;
    ListView listview;
    List<OrderStatus> orders;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        listview = (ListView) view.findViewById(R.id.orderListView);
        orders = new ArrayList<OrderStatus>();

        easyOpenApi.getMemberOrderDetails(new Callback<OrderDetail>() {
            @Override
            public void success(OrderDetail orderDetail, Response response) {
                for(OrderHistory order : orderDetail.getOrderHistory()) {
                    orderNumber = order.getOrderNumber();
                    easyOpenApi.getMemberOrderStatus(orderNumber,new Callback<OrderStatusDetail>() {
                        @Override
                        public void success(OrderStatusDetail orderStatusDetail, Response response) {
                            orders.add(orderStatusDetail.getOrderStatus().get(0));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getActivity(), "Failed to fetch order status", Toast.LENGTH_LONG).show();
                            Log.i("Fail Response Order Status", error.getUrl() + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Failed to get orders associated with the account", Toast.LENGTH_LONG).show();
                Log.i("Fail Response Order History", error.getUrl() + error.getMessage());
            }
        });
        final OrderArrayAdapter adapter = new OrderArrayAdapter(activity,
                orders);
        listview.setAdapter(adapter);
        return (view);
    }
}
