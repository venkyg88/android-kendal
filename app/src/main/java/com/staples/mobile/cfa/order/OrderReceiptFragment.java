package com.staples.mobile.cfa.order;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.CreditCard;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */
public class OrderReceiptFragment extends Fragment {
    private static final String TAG = "OrderDetailsFragment";
    MainActivity activity;
    EasyOpenApi easyOpenApi;
    Shipment shipment;

    TextView orderNumber;
    TextView orderTotal;
    TextView orderDate;
    TextView orderQty;
    TextView deliveryDate;
    TextView cardInfo;
    TextView orderName;
    TextView billingAddress;
    TextView orderSubTotal;
    TextView orderCoupons;
    TextView orderShipping;
    TextView orderTax;
    TextView orderGrandTotal;
    ImageView cardImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        activity = (MainActivity) getActivity();
        activity.showProgressIndicator();
        View view = inflater.inflate(R.layout.order_detail_fragment, container, false);

        orderNumber = (TextView)view.findViewById(R.id.orderNumber);
        orderDate = (TextView)view.findViewById(R.id.orderDate);
        orderTotal = (TextView)view.findViewById(R.id.orderTotal);
        orderQty = (TextView)view.findViewById(R.id.orderQtyLbl);
        deliveryDate = (TextView)view.findViewById(R.id.deliveryDateTV);
        cardImage = (ImageView)view.findViewById(R.id.creditCardImage);
        cardInfo = (TextView)view.findViewById(R.id.cardInfoTV);
        orderName = (TextView)view.findViewById(R.id.billingNameTV);
        billingAddress = (TextView)view.findViewById(R.id.addressTV);
        orderSubTotal = (TextView)view.findViewById(R.id.orderSubTotalTV);
        orderCoupons = (TextView)view.findViewById(R.id.couponsTV);
        orderShipping = (TextView)view.findViewById(R.id.shippingTV);
        orderTax = (TextView)view.findViewById(R.id.orderTaxTV);
        orderGrandTotal = (TextView)view.findViewById(R.id.orderGrandTotalTV);

        final LinearLayout shipmentItem = (LinearLayout)view.findViewById(R.id.sku_item);

        Bundle args = getArguments();
        if(args != null) {
            shipment = (Shipment)args.getSerializable("orderData");
            if(shipment != null) {
                float itemsOrdered = 0;
                float total = 0;
                int i = 1;
                for(final ShipmentSKU sku : shipment.getShipmentSku()) {

                    View v = inflater.inflate(R.layout.shipment_listitem, shipmentItem, false);
                    final TextView skuTitle = (TextView)v.findViewById(R.id.shipmentTitle);
                    final TextView skuPrice = (TextView)v.findViewById(R.id.shipmentPrice);
                    final TextView skuQuantity = (TextView)v.findViewById(R.id.shipmentQty);
                    final TextView shipmentNum = (TextView)v.findViewById(R.id.shipmentLbl);
                    final ImageView skuImage = (ImageView)v.findViewById(R.id.skuImage);
                    shipmentNum.setText("Shipment " + i);

                    easyOpenApi = Access.getInstance().getEasyOpenApi(false);
                    easyOpenApi.getSkuDetails(sku.getSkuNumber(), null, 50, new Callback<SkuDetails>() {
                        @Override
                        public void success(SkuDetails skuDetails, Response response) {
                            Picasso.with(activity).load(skuDetails.getProduct().get(0).getImage().get(0).getUrl()).error(R.drawable.no_photo).into(skuImage);
                            skuTitle.setText(skuDetails.getProduct().get(0).getProductName());
                            skuPrice.setText("$" + sku.getLineTotal());
                            float qty = Float.parseFloat(sku.getQtyOrdered());
                            skuQuantity.setText(Integer.toString((int) qty));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            ((MainActivity)activity).showErrorDialog(ApiError.getErrorMessage(error));
                        }
                    });

                    shipmentItem.addView(v);

                    itemsOrdered += Float.parseFloat(sku.getQtyOrdered());
                    total += Float.parseFloat(sku.getLineTotal());
                    i = i+1;
                }
                orderNumber.setText(shipment.getOrderStatus().getOrderNumber());
                orderTotal.setText("$"+shipment.getOrderStatus().getGrandTotal());
                orderQty.setText("("+ Integer.toString((int) itemsOrdered)+" Items)");
                deliveryDate.setText(dateFormatter(shipment.getScheduledDeliveryDate()));
                orderDate.setText(dateFormatter(shipment.getOrderStatus().getOrderDate()));
                cardImage.setImageResource(CreditCard.Type.matchOnApiName(shipment.getOrderStatus().getPayment().get(0)
                        .getPaymentMethodCode()).getImageResource());
                cardInfo.setText("Card ending in " + shipment.getOrderStatus().getPayment().get(0).getCcLast4Digits());
                orderName.setText(shipment.getOrderStatus().getShiptoFirstName() + " " + shipment.getOrderStatus().getShiptoLastName());
                billingAddress.setText(shipment.getOrderStatus().getShiptoAddress1() + ((shipment.getOrderStatus().getShiptoAddress2()!=null) ? " " +
                        shipment.getOrderStatus().getShiptoAddress2() + " " : " ") + shipment.getOrderStatus().getShiptoCity() + ", " +
                        shipment.getOrderStatus().getShiptoState() + " "+ shipment.getOrderStatus().getShiptoZip());
                orderSubTotal.setText(shipment.getOrderStatus().getShipmentSkuSubtotal());
                orderCoupons.setText(shipment.getOrderStatus().getCouponTotal());
                orderShipping.setText(shipment.getOrderStatus().getShippingAndHandlingTotal());
                orderTax.setText(shipment.getOrderStatus().getSalesTaxTotal());
                orderGrandTotal.setText(shipment.getOrderStatus().getGrandTotal());
                activity.hideProgressIndicator();
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ORDER);
    }

    public String dateFormatter(String dateToBeFormatted){
        String dateFormatted = null;
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                    Locale.ENGLISH);
            Date parsedDate = sdf.parse(dateToBeFormatted);
            SimpleDateFormat formatter = new SimpleDateFormat("MMM. d, yyyy");
            dateFormatted =  formatter.format(parsedDate);
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
        return dateFormatted;
    }
}
