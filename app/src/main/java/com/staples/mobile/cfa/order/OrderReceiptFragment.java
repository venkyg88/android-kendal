package com.staples.mobile.cfa.order;

import android.app.Fragment;
import android.content.res.Resources;
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
import com.staples.mobile.cfa.util.CurrencyFormat;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.Image;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;

import java.text.SimpleDateFormat;

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
    OrderShipmentListItem order;

    TextView orderNumber;
//    TextView orderTotal;
    TextView orderDate;
//    TextView orderQty;
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

        Resources r = getResources();

        View view = inflater.inflate(R.layout.order_detail_fragment, container, false);

        orderNumber = (TextView)view.findViewById(R.id.orderNumber);
        orderDate = (TextView)view.findViewById(R.id.orderDate);
//        orderTotal = (TextView)view.findViewById(R.id.orderTotal);
//        orderQty = (TextView)view.findViewById(R.id.orderQtyLbl);
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

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

        final LinearLayout shipmentItem = (LinearLayout)view.findViewById(R.id.sku_item);

        Bundle args = getArguments();
        if(args != null) {
            order = (OrderShipmentListItem)args.getSerializable("orderData");
            if (order != null) {
                OrderStatus orderStatus = order.getOrderStatus();
//                int totalItemsInOrder = 0;

                // for each shipment of the order
                for (int i = 0;  i < orderStatus.getShipment().size(); i++) {
                    Shipment shipment = orderStatus.getShipment().get(i);

                    boolean delivered = "DLV".equals(shipment.getShipmentStatusCode());
                    String deliveryDateText = r.getString(delivered? R.string.delivered_date : R.string.estimated_delivery);
                    deliveryDateText += " - " + formatter.format(OrderShipmentListItem.parseDate(delivered?
                            shipment.getActualShipDate() : shipment.getScheduledDeliveryDate()));

                    // for each item within the shipment
                    for (int j = 0; j < shipment.getShipmentSku().size(); j++) {
                        ShipmentSKU sku = shipment.getShipmentSku().get(j);

                        View v = inflater.inflate(R.layout.shipment_listitem, shipmentItem, false);
                        TextView skuTitle = (TextView) v.findViewById(R.id.shipmentTitle);
                        TextView skuPrice = (TextView) v.findViewById(R.id.shipmentPrice);
                        TextView skuQuantity = (TextView) v.findViewById(R.id.shipmentQty);
                        TextView shipmentNum = (TextView) v.findViewById(R.id.shipmentLbl);
                        TextView deliveryDate = (TextView) v.findViewById(R.id.deliveryDateTV);
                        View horizRule = v.findViewById(R.id.horizontal_rule);
                        final ImageView skuImage = (ImageView) v.findViewById(R.id.skuImage);

                        // if first item in shipment, then include the shipment label
                        if (j == 0) {
                            shipmentNum.setText("Shipment " + (i + 1));
                            deliveryDate.setText(deliveryDateText);
                        } else {
                            shipmentNum.setVisibility(View.GONE);
                            deliveryDate.setVisibility(View.GONE);
                            horizRule.setVisibility(View.GONE);
                        }
                        skuTitle.setText(sku.getSkuDescription());
                        skuPrice.setText(CurrencyFormat.getFormatter().format(Float.parseFloat(sku.getLineTotal())));
                        int skuQty = (int)Float.parseFloat(sku.getQtyOrdered()); // API value has decimal point (e.g. "1.0")
                        skuQuantity.setText(String.valueOf(skuQty));
//                        totalItemsInOrder += skuQty;

                        shipmentItem.addView(v);

                        // fill in images asynchronously
                        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
                        easyOpenApi.getSkuDetails(sku.getSkuNumber(), 1, 50, new Callback<SkuDetails>() {
                            @Override public void success(SkuDetails skuDetails, Response response) {
                                String imageUrl = null;
                                if (skuDetails.getProduct() != null && skuDetails.getProduct().size() > 0) {
                                    Product product = skuDetails.getProduct().get(0);
                                    if (product.getImage() != null && product.getImage().size() > 0) {
                                        Image image = product.getImage().get(0);
                                        imageUrl = image.getUrl();
                                    }
                                }
                                if (imageUrl != null) {
                                    Picasso.with(activity).load(imageUrl).error(R.drawable.no_photo).into(skuImage);
                                } else {
                                    skuImage.setImageResource(R.drawable.no_photo);
                                }
                            }
                            @Override public void failure(RetrofitError error) {
                                skuImage.setImageResource(R.drawable.no_photo);
                            }
                        });

                    }
                }

                orderNumber.setText(orderStatus.getOrderNumber());
//                orderTotal.setText("$"+orderStatus.getGrandTotal());
//                orderQty.setText("("+ r.getQuantityString(R.plurals.cart_qty, totalItemsInOrder, totalItemsInOrder) + ")");

                orderDate.setText(r.getString(R.string.order_date) + ": " + formatter.format(OrderShipmentListItem.parseDate(orderStatus.getOrderDate())));
                cardImage.setImageResource(CreditCard.Type.matchOnApiName(orderStatus.getPayment().get(0)
                        .getPaymentMethodCode()).getImageResource());
                cardInfo.setText(r.getString(R.string.card_ending_in) + " " + orderStatus.getPayment().get(0).getCcLast4Digits());
                orderName.setText(orderStatus.getShiptoFirstName() + " " + orderStatus.getShiptoLastName());
                billingAddress.setText(orderStatus.getShiptoAddress1() + ((orderStatus.getShiptoAddress2()!=null) ? " " +
                        orderStatus.getShiptoAddress2() + " " : " ") + orderStatus.getShiptoCity() + ", " +
                        orderStatus.getShiptoState() + " "+ orderStatus.getShiptoZip());
                orderSubTotal.setText("$"+orderStatus.getShipmentSkuSubtotal());
                orderCoupons.setText(orderStatus.getCouponTotal());
                orderShipping.setText(orderStatus.getShippingAndHandlingTotal());
                orderTax.setText("$"+orderStatus.getSalesTaxTotal());
                orderGrandTotal.setText("$"+orderStatus.getGrandTotal());
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ORDER);
    }

}
