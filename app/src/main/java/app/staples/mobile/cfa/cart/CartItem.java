package app.staples.mobile.cfa.cart;

import android.text.Html;

import app.staples.mobile.cfa.widget.QuantityEditor;
//import QuantityEditor;
import com.staples.mobile.common.access.easyopen.model.browse.Discount;
import com.staples.mobile.common.access.easyopen.model.cart.Image;
import com.staples.mobile.common.access.easyopen.model.cart.Pricing;
import com.staples.mobile.common.access.easyopen.model.cart.Product;

import java.util.List;

public class CartItem {
    private static final String TAG = CartItem.class.getSimpleName();

    private Product product;
    private int proposedQty;
    private int minExpectedBusinessDays;
    private int maxExpectedBusinessDays;

    QuantityEditor qtyWidget;

    // Constructor
    public CartItem(Product product) {
        this.product = product;
        this.proposedQty = product.getQuantity();

        // parse out lead time info ("3 Business Days" or "3 - 5 Business Days")
        String leadTimeDesc = product.getLeadTimeDescription();
        int indexOfDash = leadTimeDesc.indexOf(" - ");
        int indexOfText = leadTimeDesc.indexOf(" Business");
        if (indexOfDash > 0) {
            minExpectedBusinessDays = Integer.parseInt(leadTimeDesc.substring(0, indexOfDash));
            maxExpectedBusinessDays = Integer.parseInt(leadTimeDesc.substring(indexOfDash+3, indexOfText));
        } else {
            minExpectedBusinessDays = Integer.parseInt(leadTimeDesc.substring(0, indexOfText));
            maxExpectedBusinessDays = minExpectedBusinessDays;
        }
    }

    public Product getProduct(){
        return product;
    }

    public String getDescription() {
        return Html.fromHtml(product.getProductName()).toString();
    }

    public String getOrderItemId() {
        return product.getOrderItemId();
    }

    public String getSku() {
        return product.getSku();
    }

    public Pricing getPricing() {
        List<Pricing> pricings = product.getPricing();
        if (pricings != null) {
            for (Pricing pricing : pricings) {
                if (pricing.getFinalPrice() > 0.0f) {
                    return pricing;
                }
            }
        }
        return null;
    }

    public float getFinalPrice() {
        Pricing pricing = getPricing();
        if (pricing != null) {
            return pricing.getFinalPrice();
        }
        return 0.0f;
    }

    public float getTotalOrderItemPrice() {
        Pricing pricing = getPricing();
        if (pricing != null) {
            return pricing.getTotalOrderItemPrice();
        }
        return 0.0f;
    }

    public float getListPrice() {
        Pricing pricing = getPricing();
        if (pricing != null) {
            return pricing.getListPrice();
        }
        return 0.0f;
    }

    public float getRebate() {
        float rebate = 0;
        Pricing pricing = getPricing();
        if (pricing != null) {
            if (pricing.getDiscount() != null) {
                for (Discount discount : pricing.getDiscount()) {
                    if ("rebate".equals(discount.getName()) && discount.getAmount() != 0) {
                        rebate += discount.getAmount();
                    }
                }
            }
        }
        return rebate;
    }

    public String getPriceUnitOfMeasure() {
        Pricing pricing = getPricing();
        if (pricing != null) {
            return pricing.getUnitOfMeasure();
        }
        return null;
    }

    public String getImageUrl() {
        List<Image> images = product.getImage();
        if (images != null) {
            for(Image image : images) {
                if (image.getUrl() != null) {
                    return image.getUrl();
                }
            }
        }
        return null;
    }

    public String getThumbnailImageUrl() {
        List<Image> images = product.getThumbnailImage();
        if (images != null) {
            for(Image image : images) {
                if (image.getUrl() != null) {
                    return image.getUrl();
                }
            }
        }
        return null;
    }

    public String getLeadTimeDescription() {
        // CartFragment logic requires this to be non null
        return product.getLeadTimeDescription() == null? "" : product.getLeadTimeDescription();
    }

    public int getQuantity() {
        return product.getQuantity();
    }

    public void setQuantity(int quantity) {
        product.setQuantity(quantity);
    }

    public int getProposedQty() {
        return proposedQty;
    }

    public void setProposedQty(int proposedQty) {
        this.proposedQty = proposedQty;
    }

    public boolean isProposedQtyDifferent() {
        return getQuantity() != getProposedQty();
    }

    public void resetProposedQty() {
        setProposedQty(getQuantity());
    }

    public QuantityEditor getQtyWidget() {
        return qtyWidget;
    }

    public void setQtyWidget(QuantityEditor qtyWidget) {
        this.qtyWidget = qtyWidget;
    }

    public int getMinExpectedBusinessDays() {
        return minExpectedBusinessDays;
    }

    public int getMaxExpectedBusinessDays() {
        return maxExpectedBusinessDays;
    }

    public boolean isHeavyWeightSKU() {
        boolean isHeavyWeightSKUBool = false;
        String isHeavyWeightSKUStr = product.getIsHeavyWeightSKU();
        if (isHeavyWeightSKUStr != null) {
            isHeavyWeightSKUBool = isHeavyWeightSKUStr.equalsIgnoreCase("Y");
        }
        return isHeavyWeightSKUBool;
    }

    public boolean isAddOnSKU() {
        boolean isAddOnSKUBool = false;
        String isAddOnSKUStr = product.getIsAddOnSKU();
        if (isAddOnSKUStr != null) {
            isAddOnSKUBool = isAddOnSKUStr.equalsIgnoreCase("Y");
        }
        return isAddOnSKUBool;
    }
}
