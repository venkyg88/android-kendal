package app.staples.mobile.cfa.cart;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.widget.PriceSticker;
import app.staples.mobile.cfa.widget.QuantityEditor;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private static final String TAG = CartAdapter.class.getSimpleName();

    private Context context;
    private LayoutInflater inflater;

    private List<CartItemGroup> cartItemGroups = new ArrayList<CartItemGroup>();

    private Drawable noPhoto;
    String shippingEstimateLabel;

    // widget listeners
    private View.OnClickListener onClickListener;
    private QuantityEditor.OnQtyChangeListener qtyChangeListener;

    /** constructor */
    public CartAdapter(Context context,
                       View.OnClickListener onClickListener,
                       QuantityEditor.OnQtyChangeListener qtyChangeListener) {
        this.context = context;
        this.onClickListener = onClickListener;
        this.qtyChangeListener = qtyChangeListener;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        noPhoto = context.getResources().getDrawable(R.drawable.no_photo);
        shippingEstimateLabel = context.getResources().getString(R.string.expected_delivery);
    }

    public void setItems(List<CartItemGroup> items) {
        cartItemGroups = items;
        notifyDataSetChanged();
    }

/* Views */

    // Create new views (invoked by the layout manager)
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_group, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        CartItemGroup cartItemGroup = getGroupItem(position);
        Cart cart = CartApiManager.getCart();

        // set shipping estimate
        vh.shipEstimateTextView.setText(shippingEstimateLabel + " " + cartItemGroup.getExpectedDelivery());
        vh.shipEstimateItemQtyTextView.setText(context.getResources().getQuantityString(R.plurals.cart_qty,
                cartItemGroup.getExpectedDeliveryItemQty(), cartItemGroup.getExpectedDeliveryItemQty()));

        // adjust the number of child views in cart item list
        int listLayoutChildCount = vh.cartItemListLayout.getChildCount();
        int groupSize = cartItemGroup.getCartItems().size();
        if (listLayoutChildCount > groupSize) {
            vh.cartItemListLayout.removeViews(groupSize, listLayoutChildCount - groupSize);
        } else {
            for (int i = listLayoutChildCount; i < groupSize; i++) {
                View v = inflater.inflate(R.layout.cart_item, vh.cartItemListLayout, false);
                v.setTag(new CartItemViewHolder(v));
                vh.cartItemListLayout.addView(v);
            }
        }

        DecimalFormat currencyFormat = MiscUtils.getCurrencyFormat();
        float amountToReachToCheckoutAddOnItems = cart.getAmountToReachToCheckoutAddOnItems();
        String amountToReachToCheckoutAddOnItemsStr = currencyFormat.format(amountToReachToCheckoutAddOnItems);

        String minimum = context.getResources().getString(R.string.minimum_all_caps);
        String addOnMinimum = amountToReachToCheckoutAddOnItemsStr + " " + minimum;

        // for each cart item
        for (int i = 0; i < groupSize; i++) {

            CartItem cartItem = cartItemGroup.getCartItems().get(i);
            CartItemViewHolder ciVh = (CartItemViewHolder) vh.cartItemListLayout.getChildAt(i).getTag();

            // Set image
            String imageUrl = cartItem.getImageUrl();
            if (imageUrl == null) {
                ciVh.imageView.setImageDrawable(noPhoto);
            } else {
                Picasso.with(context).load(imageUrl).error(noPhoto).into(ciVh.imageView);
            }

            // Set title
            ciVh.titleTextView.setText(cartItem.getDescription());

            // Set price
            float rebate = cartItem.getRebate();
            boolean rebatePresent = (rebate != 0);
            float wasPrice = cartItem.getListPrice();
            // if no list price, use final price as the was price. this handles the case when qty>1
            // and also when there's an employee discount
            if (wasPrice == 0) {
                wasPrice = cartItem.getFinalPrice();
            }
            ciVh.priceSticker.setPricing(cartItem.getTotalOrderItemPrice(), wasPrice,
                    cartItem.getPriceUnitOfMeasure(), rebatePresent? "*":null);
            if (rebatePresent) {
                ciVh.rebateNote.setVisibility(View.VISIBLE);
                ciVh.rebateText.setVisibility(View.VISIBLE);
                ciVh.rebateText.setText(currencyFormat.format(rebate) + " Rebate");
            } else {
                ciVh.rebateNote.setVisibility(View.GONE);
                ciVh.rebateText.setVisibility(View.GONE);
            }

            ciVh.imageView.setTag(cartItem);
            ciVh.titleTextView.setTag(cartItem);
            ciVh.qtyWidget.setTag(cartItem);
            ciVh.deleteButton.setTag(cartItem);
//        ciVh.updateButton.setTag(cartItem);

            // associate qty widget with cart item
            cartItem.setQtyWidget(ciVh.qtyWidget);

            // set widget listeners
            ciVh.qtyWidget.setOnQtyChangeListener(qtyChangeListener);
            ciVh.deleteButton.setOnClickListener(onClickListener);
            ciVh.imageView.setOnClickListener(onClickListener);
            ciVh.titleTextView.setOnClickListener(onClickListener);
//        ciVh.updateButton.setOnClickListener(qtyUpdateButtonListener);

            // set quantity (AFTER listeners set up above)
            ciVh.qtyWidget.setQuantity(cartItem.getProposedQty());

            // set visibility of update button
//        ciVh.updateButton.setVisibility(cartItem.isProposedQtyDifferent()? View.VISIBLE : View.GONE);
            ciVh.qtyWidget.setError(cartItem.isProposedQtyDifferent() ? "Update failed" : null);

            // set visibility of horizontal rule
            ciVh.horizontalRule.setVisibility((i < groupSize-1)? View.VISIBLE : View.GONE);

            boolean isHeavyWeightSKU = cartItem.isHeavyWeightSKU();
            if (isHeavyWeightSKU) {
                ciVh.overweightWarning.setVisibility(View.VISIBLE);
            } else {
                ciVh.overweightWarning.setVisibility(View.GONE);
            }

            boolean isAddOnSKU = cartItem.isAddOnSKU();
            if (isAddOnSKU) {
                ciVh.addOnWarning.setText(addOnMinimum);
                ciVh.addOnWarning.setVisibility(View.VISIBLE);
            } else {
                ciVh.addOnWarning.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return cartItemGroups.size();
    }

    public CartItemGroup getGroupItem(int position) {
        return cartItemGroups.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewGroup shipEstimateLayout;
        TextView shipEstimateTextView;
        TextView shipEstimateItemQtyTextView;
        ViewGroup cartItemListLayout;

        ViewHolder(View itemView) {
            super(itemView);
            shipEstimateLayout = (ViewGroup) itemView.findViewById(R.id.cartitem_shipping_estimate_layout);
            shipEstimateTextView = (TextView) itemView.findViewById(R.id.cartitem_shipping_estimate);
            shipEstimateItemQtyTextView = (TextView) itemView.findViewById(R.id.cartitem_shipping_estimate_itemqty);
            cartItemListLayout = (ViewGroup) itemView.findViewById(R.id.cart_item_list);
        }
    }

    static class CartItemViewHolder {
        ImageView imageView;
        TextView titleTextView;
        PriceSticker priceSticker;
        TextView rebateNote;
        TextView rebateText;
        QuantityEditor qtyWidget;
        View deleteButton;
//        Button updateButton;
        View horizontalRule;
        TextView overweightWarning;
        TextView addOnWarning;

        CartItemViewHolder(View convertView) {
            imageView = (ImageView) convertView.findViewById(R.id.cartitem_image);
            titleTextView = (TextView) convertView.findViewById(R.id.cartitem_title);
            overweightWarning = (TextView) convertView.findViewById(R.id.overweight);
            addOnWarning = (TextView) convertView.findViewById(R.id.addon);
            priceSticker = (PriceSticker) convertView.findViewById(R.id.cartitem_price);
            rebateNote = (TextView)convertView.findViewById(R.id.rebate_note);
            rebateText = (TextView)convertView.findViewById(R.id.rebate_text);
            qtyWidget = (QuantityEditor) convertView.findViewById(R.id.cartitem_qty);
            deleteButton = convertView.findViewById(R.id.cartitem_delete);
//            updateButton = (Button) convertView.findViewById(R.id.cartitem_update);
            horizontalRule = convertView.findViewById(R.id.cart_item_horizontal_rule);
        }
    }
}
