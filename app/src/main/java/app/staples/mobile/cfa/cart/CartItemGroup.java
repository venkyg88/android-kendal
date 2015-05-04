package app.staples.mobile.cfa.cart;

import java.util.ArrayList;
import java.util.List;

//import QuantityEditor;

public class CartItemGroup {
    private static final String TAG = CartItemGroup.class.getSimpleName();

    List<CartItem> cartItems;
    String expectedDelivery;
    int expectedDeliveryItemQty;

    // Constructor
    public CartItemGroup() {
        this.cartItems = new ArrayList<CartItem>();
    }

    public void clearItems() {
        this.cartItems.clear();
    }

    public void addItem(CartItem item) {
        this.cartItems.add(item);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public String getExpectedDelivery() {
        return expectedDelivery;
    }

    public void setExpectedDelivery(String expectedDelivery) {
        this.expectedDelivery = expectedDelivery;
    }

    public int getExpectedDeliveryItemQty() {
        return expectedDeliveryItemQty;
    }

    public void setExpectedDeliveryItemQty(int expectedDeliveryItemQty) {
        this.expectedDeliveryItemQty = expectedDeliveryItemQty;
    }
}
