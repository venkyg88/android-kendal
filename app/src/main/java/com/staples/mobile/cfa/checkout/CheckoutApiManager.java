/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethodResponse;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;
import com.staples.mobile.common.access.easyopen.model.checkout.SubmitOrderRequest;
import com.staples.mobile.common.access.easyopen.model.checkout.SubmitOrderResponse;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCardPOW;
import com.staples.mobile.common.access.easyopen.model.member.POWResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by sutdi001 on 12/31/14.
 */
public class CheckoutApiManager {

    public interface ApplyAddressCallback {
        public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg);
    }

    public interface ApplyPaymentMethodCallback {
        public void onApplyPaymentMethodComplete(String paymentMethodId, String authorized, String errMsg);
    }

    public interface PrecheckoutCallback {
        public void onPrecheckoutComplete(String shippingCharge, Float tax, String errMsg, String infoMsg);
    }

    public interface OrderSubmissionCallback {
        public void onOrderSubmissionComplete(String orderId, String orderNumber, String errMsg);
    }


    /** applies shipping address to cart  */
    public static void applyShippingAddress(ShippingAddress shippingAddress, final ApplyAddressCallback applyAddressCallback) {
        final EasyOpenApi secureApi = Access.getInstance().getEasyOpenApi(true);
        secureApi.addShippingAddressToCart(shippingAddress, new Callback<AddressValidationAlert>() {
                @Override
                public void success(AddressValidationAlert addressValidationAlert, Response response) {
                    if (applyAddressCallback != null) {
                        applyAddressCallback.onApplyAddressComplete(addressValidationAlert.getShippingAddressId(),
                                null, addressValidationAlert.getAddressValidationAlert());
                    }
                    // applying a shipping address causes a change to the profile, so refresh cached profile
                    new ProfileDetails().refreshProfile(null);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    if (applyAddressCallback != null) {
                        applyAddressCallback.onApplyAddressComplete(null, ApiError.getErrorMessage(retrofitError), null);
                    }
                    // if timeout, adding address may have triggered a change to the profile,
                    // therefore refresh cached profile so at least if user leaves page and
                    // re-enters they have a chance of succeeding
                    new ProfileDetails().refreshProfile(null);
                }
        });
    }

    /** applies billing address to cart  */
    public static void applyBillingAddress(BillingAddress billingAddress, final ApplyAddressCallback applyAddressCallback) {
        final EasyOpenApi secureApi = Access.getInstance().getEasyOpenApi(true);
        secureApi.addBillingAddressToCart(billingAddress, new Callback<AddressValidationAlert>() {
            @Override
            public void success(AddressValidationAlert addressValidationAlert, Response response) {
                if (applyAddressCallback != null) {
                    applyAddressCallback.onApplyAddressComplete(addressValidationAlert.getBillingAddressId(),
                            null, addressValidationAlert.getAddressValidationAlert());
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (applyAddressCallback != null) {
                    applyAddressCallback.onApplyAddressComplete(null, ApiError.getErrorMessage(retrofitError), null);
                }
            }
        });
    }

    /** applies payment method to cart  */
    public static void applyPaymentMethod(final PaymentMethod paymentMethod, final ApplyPaymentMethodCallback applyPaymentMethodCallback) {
        final EasyOpenApi secureApi = Access.getInstance().getEasyOpenApi(true);
        secureApi.addPaymentMethodToCart(paymentMethod,
                new Callback<PaymentMethodResponse>() {
                    @Override
                    public void success(PaymentMethodResponse paymentMethodResponse, Response response) {
                        if (applyPaymentMethodCallback != null) {
                            applyPaymentMethodCallback.onApplyPaymentMethodComplete(paymentMethodResponse.getCreditCardId(),
                                    paymentMethodResponse.getAuthorized(), null);
                        }
                        // if new payment method created, refresh cached profile
                        if (paymentMethod.getCreditCardId() == null) {
                            new ProfileDetails().refreshProfile(null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (applyPaymentMethodCallback != null) {
                            applyPaymentMethodCallback.onApplyPaymentMethodComplete(null, null,
                                    ApiError.getErrorMessage(retrofitError));
                        }
                    }
                }
        );
    }

    /** applies payment method to cart  */
    public static void encryptAndApplyPaymentMethod(final PaymentMethod paymentMethod, final ApplyPaymentMethodCallback applyPaymentMethodCallback) {
        // encrypt payment method
        String powCardType = paymentMethod.getCardType();
        AddCreditCardPOW creditCard = new AddCreditCardPOW(paymentMethod.getCardNumber(), powCardType);
        List<AddCreditCardPOW> ccList = new ArrayList<AddCreditCardPOW>();
        ccList.add(creditCard);

        Callback<List<POWResponse>> apiCallback = new Callback<List<POWResponse>>() {

            @Override
            public void success(List<POWResponse> powList, Response response) {
                String powStatus = powList.get(0).getStatus();
                if ("0".equals(powStatus) && !TextUtils.isEmpty(powList.get(0).getPacket())) {
                    paymentMethod.setCardNumber(powList.get(0).getPacket());
                    applyPaymentMethod(paymentMethod, applyPaymentMethodCallback);
                } else {
                    if (applyPaymentMethodCallback != null) {
                        String powStatusMsg = "1".equals(powStatus)? "DPS call failed" :
                                ("2".equals(powStatus)? "validation failed" : "unknown");
                        applyPaymentMethodCallback.onApplyPaymentMethodComplete(null, null,
                                "Payment Error: "+powStatusMsg);
                    }
                }
            }
            @Override
            public void failure(RetrofitError retrofitError) {
                if (applyPaymentMethodCallback != null) {
                    applyPaymentMethodCallback.onApplyPaymentMethodComplete(null, null,
                            "Payment Error: " + ApiError.getErrorMessage(retrofitError));
                }
            }
        };

        // encrypt CC number and then proceed in callback
        EasyOpenApi powApi = Access.getInstance().getPOWApi(true);
        powApi.addCreditPOWCall(ccList, apiCallback);
    }

    /** does precheckout and queries for tax and shipping */
    public static void precheckout(final PrecheckoutCallback precheckoutCallback) {

        final EasyOpenApi secureApi = Access.getInstance().getEasyOpenApi(true);
        secureApi.precheckout(new Callback<AddressValidationAlert>() {

            @Override
            public void success(AddressValidationAlert addressValidationAlert, Response response) {

                // check for alerts
                String infoMsg = addressValidationAlert.getAddressValidationAlert();
                if (infoMsg == null) {
                    infoMsg = addressValidationAlert.getInventoryCheckAlert();
                }
                final String finalInfoMsg = infoMsg;

                // get shipping charge, then get tax
                secureApi.getShippingCharge(new Callback<CartContents>() {
                    @Override
                    public void success(CartContents cartContents, Response response) {

                        Cart cart = getCartFromResponse(cartContents);
                        if (cart != null) {
                            final String shippingCharge = cart.getShippingCharge();
                            secureApi.getTax(new Callback<CartContents>() {
                                @Override
                                public void success(CartContents cartContents, Response response) {
                                    Cart cart = getCartFromResponse(cartContents);
                                    doCallback(precheckoutCallback, shippingCharge, cart.getTotalTax(), null, finalInfoMsg);
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    doCallback(precheckoutCallback, shippingCharge, null, "Error retrieving tax: " +
                                            ApiError.getErrorMessage(retrofitError), finalInfoMsg);
                                }
                            });

                        } else {
                            doCallback(precheckoutCallback, null, null, "Error retrieving shipping charge", finalInfoMsg);
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        doCallback(precheckoutCallback, null, null, "Error retrieving shipping charge: " +
                                ApiError.getErrorMessage(retrofitError), finalInfoMsg);
                    }

                    private Cart getCartFromResponse(CartContents cartContents) {
                        if (cartContents != null && cartContents.getCart() != null && cartContents.getCart().size() > 0) {
                            return cartContents.getCart().get(0);
                        }
                        return null;
                    }
                });


            }

            @Override
            public void failure(RetrofitError retrofitError) {
                doCallback(precheckoutCallback, null, null, ApiError.getErrorMessage(retrofitError), null);
            }


            private void doCallback(PrecheckoutCallback precheckoutCallback, String shippingCharge, Float tax, String errMsg, String infoMsg) {
                if (precheckoutCallback != null) {
                    precheckoutCallback.onPrecheckoutComplete(shippingCharge, tax, errMsg, infoMsg);
                }
            }

        });
    }

    /** performs final order submission */
    public static void submitOrder(String cid, final OrderSubmissionCallback orderSubmissionCallback) {

        final EasyOpenApi secureApi = Access.getInstance().getEasyOpenApi(true);

        // upon payment method success, submit the order
        SubmitOrderRequest submitOrderRequest = new SubmitOrderRequest();
        submitOrderRequest.setCardVerificationCode(cid);
        secureApi.submitOrder(submitOrderRequest,
                new Callback<SubmitOrderResponse>() {

                    @Override
                    public void success(SubmitOrderResponse submitOrderResponse, Response response) {
                        if (orderSubmissionCallback != null) {
                            orderSubmissionCallback.onOrderSubmissionComplete(submitOrderResponse.getOrderId(),
                                    submitOrderResponse.getStaplesOrderNumber(), null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (orderSubmissionCallback != null) {
                            orderSubmissionCallback.onOrderSubmissionComplete(null, null, ApiError.getErrorMessage(retrofitError));
                        }
                    }
                }
        );
    }
}