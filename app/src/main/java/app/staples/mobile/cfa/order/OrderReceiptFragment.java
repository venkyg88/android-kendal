package app.staples.mobile.cfa.order;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.Image;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.OrderStatusDetail;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;
import com.staples.mobile.common.access.easyopen.model.member.ShipmentSKU;
import com.staples.mobile.common.analytics.Tracker;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.profile.CreditCard;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.widget.ActionBar;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OrderReceiptFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = OrderReceiptFragment.class.getSimpleName();
    private static final String ORDER_NUMBER = "OrderNumber";
    MainActivity activity;
    EasyOpenApi easyOpenApi;
    OrderShipmentListItem order;
    OrderStatus orderStatus;

    TextView orderNumber;
    TextView orderDate;
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
    TextView orderQty;
    TextView orderTotal;

    public void setArguments(String orderNumber) {
        Bundle args = new Bundle();
        args.putString(ORDER_NUMBER, orderNumber);
        setArguments(args);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("OrderReceiptFragment:onCreateView(): Displaying the Order Receipt screen.");
        activity = (MainActivity) getActivity();
        orderStatus = null;
        View view = inflater.inflate(R.layout.order_detail_fragment, container, false);

        Bundle args = getArguments();
        if(args != null) {
            if(args.getString(ORDER_NUMBER) != null) {
                String orderNumber = args.getString(ORDER_NUMBER);
                easyOpenApi = Access.getInstance().getEasyOpenApi(true);
                easyOpenApi.getMemberOrderStatus(orderNumber, new Callback<OrderStatusDetail>() {
                    @Override
                    public void success(OrderStatusDetail orderStatusDetail, Response response) {
                        orderStatus = orderStatusDetail.getOrderStatus().get(0);
                        if(getView() != null) {
                            displayOrderReceipt();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        ((MainActivity)getActivity()).showErrorDialog("Cannot fetch order at this time");
                    }
                });
            } else {
                order = (OrderShipmentListItem)args.getSerializable("orderData");
                if (order != null) {
                    orderStatus = order.getOrderStatus();
                }
            }
        }
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(orderStatus != null) {
            displayOrderReceipt();
        }
    }


    public void displayOrderReceipt () {
        Resources r = getResources();
        View view = getView();

        if (view != null) {
            orderNumber = (TextView)view.findViewById(R.id.orderNumber);
            orderDate = (TextView)view.findViewById(R.id.orderDate);
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
            orderQty = (TextView)view.findViewById(R.id.order_item_count);
            orderTotal = (TextView)view.findViewById(R.id.order_total);

            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

            final LinearLayout shipmentItem = (LinearLayout)view.findViewById(R.id.sku_item);

            DecimalFormat currencyFormat = MiscUtils.getCurrencyFormat();
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
                    View v = getActivity().getLayoutInflater().inflate(R.layout.shipment_listitem, shipmentItem, false);
                    TextView skuTitle = (TextView) v.findViewById(R.id.shipmentTitle);
                    TextView skuPrice = (TextView) v.findViewById(R.id.shipmentPrice);
                    TextView skuQuantity = (TextView) v.findViewById(R.id.shipmentQty);
                    TextView shipmentNum = (TextView) v.findViewById(R.id.shipmentLbl);
                    TextView deliveryDate = (TextView) v.findViewById(R.id.deliveryDateTV);
                    final ImageView skuImage = (ImageView) v.findViewById(R.id.skuImage);
                    skuImage.setTag(sku);
                    skuImage.setOnClickListener(this);

                    // if first item in shipment, then include the shipment label
                    if (j == 0) {
                        shipmentNum.setText("Shipment " + (i + 1));
                        deliveryDate.setText(deliveryDateText);
                    } else {
                        shipmentNum.setVisibility(View.GONE);
                        deliveryDate.setVisibility(View.GONE);
                    }
                    skuTitle.setText(sku.getSkuDescription());
                    skuPrice.setText(currencyFormat.format(Float.parseFloat(sku.getLineTotal())));
                    int skuQty = (int)Float.parseFloat(sku.getQtyOrdered()); // API value has decimal point (e.g. "1.0")
                    skuQuantity.setText(String.valueOf(skuQty));
//                        totalItemsInOrder += skuQty;

                    shipmentItem.addView(v);

                    // fill in images asynchronously
                    easyOpenApi = Access.getInstance().getEasyOpenApi(false);
                    easyOpenApi.getSkuDetails(sku.getSkuNumber(), new Callback<SkuDetails>() {
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

            orderNumber.setText(r.getString(R.string.order) + " " + orderStatus.getOrderNumber());
            orderDate.setText(r.getString(R.string.order_date) + ": " + formatter.format(OrderShipmentListItem.parseDate(orderStatus.getOrderDate())));

            // determine item qty of shipment
            int totalItemQtyOfShipment = 0;
            List<Shipment> shipments = orderStatus.getShipment();
            if (shipments!=null && shipments.size()>0) {
                Shipment shipment = shipments.get(0);
                if (shipment!=null) {
                    List<ShipmentSKU> shipmentSkus = shipment.getShipmentSku();
                    if (shipmentSkus!=null) {
                        for(ShipmentSKU shipmentSku : shipmentSkus) {
                            int qtyOrdered = (int) Double.parseDouble(shipmentSku.getQtyOrdered()); // using parseDouble since quantity string is "1.0"
                            totalItemQtyOfShipment += qtyOrdered;
                        }
                    }
                }
            }

            orderQty.setText(r.getQuantityString(R.plurals.cart_qty, totalItemQtyOfShipment, totalItemQtyOfShipment));
            orderTotal.setText("$"+orderStatus.getGrandTotal());

            cardImage.setImageResource(CreditCard.Type.matchOnApiName(orderStatus.getPayment().get(0)
                    .getPaymentMethodCode()).getImageResource());
            cardInfo.setText(r.getString(R.string.card_ending_in) + " " + orderStatus.getPayment().get(0).getCcLast4Digits());
            orderName.setText(orderStatus.getShiptoFirstName() + " " + orderStatus.getShiptoLastName());
            String zipCode = orderStatus.getShiptoZip();
            if(zipCode.length() > 5) {
                zipCode = zipCode.substring(0,5) + "-" + zipCode.substring(5);
            }
            billingAddress.setText(orderStatus.getShiptoAddress1() + ((orderStatus.getShiptoAddress2()!=null) ? " " +
                    orderStatus.getShiptoAddress2() + " " : " ") + orderStatus.getShiptoCity() + ", " +
                    orderStatus.getShiptoState() + " "+ zipCode);
            orderSubTotal.setText("$"+orderStatus.getShipmentSkuSubtotal());
            orderCoupons.setText("-$"+orderStatus.getCouponTotal());
            String formattedShipping = orderStatus.getShippingAndHandlingTotal();
            if (TextUtils.isDigitsOnly(formattedShipping)) {
                formattedShipping = currencyFormat.format(Float.parseFloat(formattedShipping));
            }
            orderShipping.setText(formattedShipping);
            orderTax.setText("$"+orderStatus.getSalesTaxTotal());
            orderGrandTotal.setText("$"+orderStatus.getGrandTotal());
        } else {
            Crittercism.leaveBreadcrumb("OrderReceiptFragment:displayOrderReceipt(): Failure to display order receipt");
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ORDER);
        Tracker.getInstance().trackStateForOrderDetails(); // Analytics
    }

    @Override
    public void onClick(View view) {
        Object tag;
        switch(view.getId()) {
            case R.id.skuImage:
                tag = view.getTag();
                if (tag instanceof ShipmentSKU) {
                    ShipmentSKU shipmentSku = (ShipmentSKU) tag;
                    ((MainActivity)getActivity()).selectSkuItem(shipmentSku.getSkuDescription(), shipmentSku.getSkuNumber(), false);
                }
                break;
        }
    }
}
