package app.staples.mobile.cfa.cart;

import android.text.TextUtils;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdate;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.cart.DeleteFromCart;
import com.staples.mobile.common.access.easyopen.model.cart.ItemsAdded;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItem;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.cart.TypedJsonString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.staples.mobile.cfa.util.MiscUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CartApiManager {
    private static final String TAG = CartApiManager.class.getSimpleName();

    public interface CartRefreshCallback {
        public void onCartRefreshComplete(String errMsg);
    }

    // cart object - make static so only one instance, also so not lost on device rotation
    private static Cart cart;
    public static Cart getCart() {
        return cart;
    }
    public static int getCartTotalItems() {
        return cart == null? 0: cart.getTotalItems();
    }
    public static float getSubTotal() {
        return cart == null? 0f: cart.getSubTotal();
    }
    public static float getPreTaxTotal() {
        return cart == null? 0f: cart.getPreTaxTotal();
    }
    public static List<String> manuallyAddedCouponCodes = new ArrayList<String>();

    // special logic around these associate coupon codes
    private final static String ASSOCIATE_REWARDS_DISCOUNT_CODE = "52797";
    private final static String ASSOCIATE_REWARDS_STAPLES_DISCOUNT_CODE = "52798";

    /** gets cart  */
    public static void loadCart(final CartRefreshCallback cartRefreshCallback) {

        // query for items in cart
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.viewCart(1, 1000, "Y",  // 0 offset results in max of 5 items, so using 1
                new Callback<CartContents>() {

                    @Override
                    public void success(CartContents cartContents, Response response) {
                        cart = null;
                        // get data from cartContent request
                        List<Cart> cartCollection = cartContents.getCart();
                        if (cartCollection != null && cartCollection.size() > 0) {
                            cart = cartCollection.get(0);
                            doctorUpAssociateRewardCoupons();
                        }
                        if (cartRefreshCallback != null) {
                            cartRefreshCallback.onCartRefreshComplete(null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (cartRefreshCallback != null) {
                            cartRefreshCallback.onCartRefreshComplete(ApiError.getErrorMessage(retrofitError));
                        }
                    }
                });
    }

    public static Product getCartProduct(String sku) {
        if (cart!=null) {
            List<Product> products = cart.getProduct();
            if (products!=null) {
                for(Product product : products) {
                    if (sku.equals(product.getSku())) {
                        return product;
                    }
                }
            }
        }
        return null;
    }

    public static void resetCart() {
        cart = null;
        manuallyAddedCouponCodes.clear();
    }


    /** adds item to cart */
    public static void addItemToCart(String sku, int qty, final CartRefreshCallback cartRefreshCallback) {

        // add item to cart
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.addToCart(createCartRequestBody(null, sku, qty), new Callback<CartUpdate>() {
            @Override
            public void success(CartUpdate cartUpdate, Response response) {
                // if a successful insert, refill cart
                List<ItemsAdded> items = cartUpdate.getItemsAdded();
                if (items!=null && items.size()>0) {
                    loadCart(cartRefreshCallback);
                } else {
                    // sometimes error message can come in success message
                    if (cartRefreshCallback != null) {
                        String errMsg = cartUpdate.getMessage();
                        if (TextUtils.isEmpty(errMsg)) {
                            errMsg = ApiError.getApiSuccessError(cartUpdate);
                        } else {
                            errMsg = MiscUtils.cleanupHtml(errMsg);
                        }
                        cartRefreshCallback.onCartRefreshComplete(errMsg);
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (cartRefreshCallback != null) {
                    cartRefreshCallback.onCartRefreshComplete(ApiError.getErrorMessage(retrofitError));
                }
            }
        });
    }

    /** update an item in the cart */
    public static void updateItemQty(String orderItemId, String sku, int qty, final CartRefreshCallback cartRefreshCallback) {

        // update item in cart
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.updateCart(createCartRequestBody(orderItemId, sku, qty), new Callback<CartUpdate>() {
            @Override
            public void success(CartUpdate cartUpdate, Response response) {
                // if a successful update, refill cart
                List<ItemsAdded> items = cartUpdate.getItemsAdded();
                if (items!=null && items.size()>0) {
                    loadCart(cartRefreshCallback);
                } else {
                    // sometimes error message can come in success message
                    if (cartRefreshCallback != null) {
                        String errMsg = cartUpdate.getMessage();
                        if (TextUtils.isEmpty(errMsg)) {
                            errMsg = ApiError.getApiSuccessError(cartUpdate);
                        } else {
                            errMsg = MiscUtils.cleanupHtml(errMsg);
                        }
                        if (errMsg != null) {
                            //Remove decimals from inventory quantities being returned in the error message
                            errMsg = errMsg.replaceAll("\\.\\d", "");
                        }
                        cartRefreshCallback.onCartRefreshComplete(errMsg);
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (cartRefreshCallback != null) {
                    cartRefreshCallback.onCartRefreshComplete(ApiError.getErrorMessage(retrofitError));
                }
            }
        });
    }

    /** deletes an item from the cart */
    public static void deleteItem(String orderItemId, final CartRefreshCallback cartRefreshCallback) {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        // delete item from cart
        easyOpenApi.deleteFromCart(orderItemId, new Callback<DeleteFromCart>() {
            @Override
            public void success(DeleteFromCart cartContents, Response response) {
                loadCart(cartRefreshCallback);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (cartRefreshCallback != null) {
                    cartRefreshCallback.onCartRefreshComplete(ApiError.getErrorMessage(retrofitError));
                }
            }
        });
    }

    public static void addCoupon(final String couponCode, final CartRefreshCallback cartRefreshCallback) {
        if (!TextUtils.isEmpty(couponCode)) {
            EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
            Coupon coupon = new Coupon();
            coupon.setPromoName(couponCode);
            easyOpenApi.addCoupon(coupon, new Callback<EmptyResponse>() {
                @Override
                public void success(EmptyResponse emptyResponse, Response response) {
                    loadCart(cartRefreshCallback); // Need updated info about the cart such as shipping and subtotals in addition to new quantities
                    manuallyAddedCouponCodes.add(couponCode);
                }

                @Override
                public void failure(RetrofitError error) {
                    if (cartRefreshCallback != null) {
                        cartRefreshCallback.onCartRefreshComplete(ApiError.getErrorMessage(error));
                    }
                }
            });
        }
    }

    public static void deleteCoupon(final String couponCode, final CartRefreshCallback cartRefreshCallback) {
        if (!TextUtils.isEmpty(couponCode)) {
            EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
            Coupon coupon = new Coupon();
            coupon.setPromoName(couponCode);
            easyOpenApi.deleteCoupon(couponCode, new Callback<EmptyResponse>() {
                @Override
                public void success(EmptyResponse emptyResponse, Response response) {
                    loadCart(cartRefreshCallback);  // need updated info about the cart such as shipping and subtotals in addition to new quantities
                    manuallyAddedCouponCodes.remove(couponCode);
                }

                @Override
                public void failure(RetrofitError error) {
                    if (cartRefreshCallback != null) {
                        cartRefreshCallback.onCartRefreshComplete(ApiError.getErrorMessage(error));
                    }
                }
            });
        }
    }

    public static boolean isAssocCoupon(Coupon coupon) {
        return isAssocCoupon(coupon, ASSOCIATE_REWARDS_DISCOUNT_CODE) ||
                isAssocCoupon(coupon, ASSOCIATE_REWARDS_STAPLES_DISCOUNT_CODE);
    }
    public static boolean isAssocCoupon(Coupon coupon, String couponCode) {
        return (coupon != null && couponCode.equals(coupon.getCode()));
    }

    public static Coupon getAssocRewardCoupon() {
        return getAssocCoupon(ASSOCIATE_REWARDS_DISCOUNT_CODE);
    }
    public static Coupon getAssocRewardStaplesCoupon() {
        return getAssocCoupon(ASSOCIATE_REWARDS_STAPLES_DISCOUNT_CODE);
    }
    private static Coupon getAssocCoupon(String couponCode) {
        if (cart!=null) {
            List<Coupon> coupons = cart.getCoupon();
            if (coupons!=null) {
                for(Coupon coupon : coupons) {
                    if (isAssocCoupon(coupon, couponCode)) {
                        return coupon;
                    }
                }
            }
        }
        return null;
    }

    /** consolidates associate coupons of the specified type into existingCoupon if it exists, or into
     * a new coupon object if it does not.
     * @param existingCoupon if non-null, then associate coupons are consolidated into it and it is returned
     * @param coupons list of coupons from which to remove and consolidate associate coupons
     * @param couponCode coupon type to consolidate
     * @return consolidated coupon (either a new one or a modified existingCoupon if it exists)
     */
    private static Coupon consolidateAssociateCoupon(Coupon existingCoupon, List<Coupon> coupons, String couponCode) {
        Coupon resultingCoupon = existingCoupon;
        Iterator<Coupon> couponIterator = coupons.iterator();
        while (couponIterator.hasNext()) {
            Coupon coupon = couponIterator.next();
            if (isAssocCoupon(coupon, couponCode)) {
                if (coupon.getAdjustedAmount() != 0) {
                    // if first instance of coupon, set a reference to it, otherwise increment amount of first instance
                    if (resultingCoupon == null) {
                        resultingCoupon = coupon;
                    } else {
                        resultingCoupon.setAdjustedAmount(resultingCoupon.getAdjustedAmount() + coupon.getAdjustedAmount());
                    }
                }
                couponIterator.remove(); // remove all associate coupons for now, will add back consolidated one later
            }
        }
        return resultingCoupon;
    }

    /** consolidates associate reward coupons into one at the cart level */
    private static void doctorUpAssociateRewardCoupons() {
        Coupon assocRewardsCoupon = null;
        Coupon assocRewardsStaplesCoupon = null;
        if (cart!=null) {
            List<Coupon> coupons = cart.getCoupon();
            if (coupons!=null) {
                // consolidate cart-level associate coupons
                assocRewardsCoupon = consolidateAssociateCoupon(assocRewardsCoupon, coupons, ASSOCIATE_REWARDS_DISCOUNT_CODE);
                assocRewardsStaplesCoupon = consolidateAssociateCoupon(assocRewardsStaplesCoupon, coupons, ASSOCIATE_REWARDS_STAPLES_DISCOUNT_CODE);
            }
            // consolidate product-level associate coupons
            List<Product> products = cart.getProduct();
            if (products!=null) {
                for(Product product : products) {
                    if (product.getCoupon() != null) {
                        assocRewardsCoupon = consolidateAssociateCoupon(assocRewardsCoupon, product.getCoupon(), ASSOCIATE_REWARDS_DISCOUNT_CODE);
                        assocRewardsStaplesCoupon = consolidateAssociateCoupon(assocRewardsStaplesCoupon, product.getCoupon(), ASSOCIATE_REWARDS_STAPLES_DISCOUNT_CODE);
                    }
                }
            }
            if (assocRewardsCoupon != null || assocRewardsStaplesCoupon != null) {
                if (cart.getCoupon() == null) {
                    cart.setCoupon(new ArrayList<Coupon>());
                }
                if (assocRewardsCoupon != null) {
                    cart.getCoupon().add(0, assocRewardsCoupon);
                }
                if (assocRewardsStaplesCoupon != null) {
                    cart.getCoupon().add(0, assocRewardsStaplesCoupon);
                }
            }
        }
    }

    /** amongst the sku-level coupons, get the ones that were manually added */
    public static List<Coupon> getManualSkuLevelCoupons() {
        List<Coupon> coupons = new ArrayList<Coupon>();
        if (cart!=null) {
            List<Product> products = cart.getProduct();
            if (products!=null) {
                for(Product product : products) {
                    List<Coupon> productCoupons = product.getCoupon();
                    if (productCoupons!=null) {
                        for(Coupon coupon : productCoupons) {
                            if (manuallyAddedCouponCodes.contains(coupon.getCode())) {
                                coupons.add(coupon);
                            }
                        }
                    }
                }
            }
        }
        return coupons;
    }

    /** returns sum of adjusted amounts for rewards and coupons applied to cart */
    public static float getCouponsRewardsAdjustedAmount() {
        float totalAdjustedAmount = 0;
        // cart-level coupons & rewards
        if (cart!=null) {
            List<Coupon> coupons = cart.getCoupon();
            if (coupons!=null) {
                for(Coupon coupon : coupons) {
                    if (!isAssocCoupon(coupon)) {
                        totalAdjustedAmount += coupon.getAdjustedAmount();
                    }
                }
            }
        }
        // sku-level
        List<Coupon> coupons = getManualSkuLevelCoupons();
        if (coupons!=null) {
            for(Coupon coupon : coupons) {
                totalAdjustedAmount += coupon.getAdjustedAmount();
            }
        }
        return totalAdjustedAmount;
    }

    public static String getExpectedDeliveryRange() {
        int overallMinDays = -1;
        int overallMaxDays = -1;
        if (cart != null) {
            List<Product> products = cart.getProduct();
            if (products!=null) {
                for(Product product : products) {
                    int minDays = 0;
                    int maxDays = 0;
                    String leadTimeDesc = product.getLeadTimeDescription();
                    int indexOfDash = leadTimeDesc.indexOf(" - ");
                    int indexOfText = leadTimeDesc.indexOf(" Business");
                    if (indexOfDash > 0) {
                        minDays = Integer.parseInt(leadTimeDesc.substring(0, indexOfDash));
                        maxDays = Integer.parseInt(leadTimeDesc.substring(indexOfDash + 3, indexOfText));
                    } else {
                        minDays = Integer.parseInt(leadTimeDesc.substring(0, indexOfText));
                        maxDays = minDays;
                    }
                    if (overallMinDays == -1 || minDays < overallMinDays) {
                        overallMinDays = minDays;
                    }
                    if (maxDays > overallMaxDays) {
                        overallMaxDays = maxDays;
                    }
                }
            }
        }

        if (overallMinDays > -1) {
            StringBuilder deliveryRange = new StringBuilder();
            if (overallMaxDays > overallMinDays) {
                deliveryRange.append(overallMinDays).append(" - ").append(overallMaxDays);
            } else {
                deliveryRange.append(overallMinDays);
            }
            return deliveryRange.toString();
        }
        return null;
    }

    //set orderItemId to null when adding new items
    private static TypedJsonString createCartRequestBody(String orderItemId, String sku, int qty) {
        OrderItem orderItem = new OrderItem(orderItemId, sku, qty);
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        orderItems.add(orderItem);
        //TODO add more cart items as required
        //generates json string for corresponding updates
        String json = generateAddUpdateBody(orderItems);
        return new TypedJsonString(json);
    }

    private static String generateAddUpdateBody(List<OrderItem> orderItemList){
        StringBuffer sb= new StringBuffer("{ \"orderItem\":[\n");
        int index=0;
        for(OrderItem orderItem : orderItemList){
            if (index>0) sb.append(",\n");
            sb.append("{");
            String id = orderItem.getOrderItemId();
            if (id!=null && !id.isEmpty()) {
                sb.append("\"orderItemId_" + index + "\":\"" + id + "\", ");
            }
            sb.append("\"partNumber_" + index + "\":\"" + orderItem.getPartNumber() + "\", ");
            sb.append("\"quantity_"+index+"\":\""+orderItem.getQuantity()+"\" }");
            index++;
        }
        sb.append("] }");
        return sb.toString();
    }
}
