package app.staples.mobile.cfa.bundle;

import com.staples.mobile.common.access.easyopen.model.browse.Discount;
import com.staples.mobile.common.access.easyopen.model.browse.Image;
import com.staples.mobile.common.access.easyopen.model.browse.Pricing;

import java.util.Comparator;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.IdentifierType;
import app.staples.mobile.cfa.util.MiscUtils;

public class BundleItem {
    public enum SortType {
        BESTMATCH       (R.string.sort_best_match,       R.id.sort_best_match,       null,                             null, null,         "Best Match"),
        PRICEASCENDING  (R.string.sort_price_ascending,  R.id.sort_price_ascending,  new BundleItem.PriceAscending(),  1,    "priceAsc",   "Price: Low to High"),
        PRICEDESCENDING (R.string.sort_price_descending, R.id.sort_price_descending, new BundleItem.PriceDescending(), 2,    "priceDesc",  "Price: High to Low"),
        TITLEASCENDING  (R.string.sort_title_ascending,  R.id.sort_title_ascending,  new BundleItem.TitleAscending(),  3,    "nameAsc",    "Name: A to Z"),
        TITLEDESCENDING (R.string.sort_title_descending, R.id.sort_title_descending, new BundleItem.TitleDescending(), 4,    "nameDesc",   "Name: Z to A"),
        HIGHESTRATED    (R.string.sort_highest_rated,    R.id.sort_highest_rated,    new BundleItem.HighestRated(),    5,    "ratingDesc", "Highest Rated"),
        LOWESTRATED     (0,                              0,                          null,                             6,    "ratingAsc",  "Lowest Rated");

        public final int title;
        public final int button;
        public final Comparator<BundleItem> comparator;
        public final Integer intParam;
        public final String stringParam;
        public final String description;

        SortType(int title, int button, Comparator<BundleItem> comparator, Integer intParam, String stringParam, String description) {
            this.title = title;
            this.button = button;
            this.comparator = comparator;
            this.intParam = intParam;
            this.stringParam = stringParam;
            this.description = description;
        }

        public static SortType findSortTypeById(int id) {
            for(SortType type : values()) {
                if (type.button==id) return(type);
            }
            return(null);
        }
    }
    public static Comparator<BundleItem> indexComparator = new IndexSort();

    public int index;
    public String title;
    public String identifier;
    public IdentifierType type;
    public String imageUrl;
    public float finalPrice;
    public float wasPrice;
    public String unit;
    public float customerRating;
    public int customerCount;
    public float rebatePrice;
    public float addOnBasketPrice;
    public float extraShippingCharge;
    public boolean busy;

    public BundleItem() {}

    public BundleItem(int index, String title, String identifier) {
        this.index = index;
        this.title = title;
        this.identifier = identifier;
        type = IdentifierType.detect(identifier);
    }

    public String setImageUrl(List<Image> images) {
        if (images==null) return(null);
        for(Image image : images) {
            String url = image.getUrl();
            if (url!=null) {
                imageUrl = url;
                return(imageUrl);
            }
        }
        return(null);
    }

    public void processPricing(Pricing pricing) {
        if (pricing==null) return;

        // Basic prices
        finalPrice = pricing.getFinalPrice();
        wasPrice = pricing.getListPrice();
        unit = pricing.getUnitOfMeasure();

        // Rebates
        List<Discount> discounts = pricing.getDiscount();
        if (discounts!=null) {
            for(Discount discount : discounts) {
                if (discount.getName().equals("rebate")) {
                    float amount = discount.getAmount();
                    if (amount > rebatePrice) {
                        rebatePrice = amount;
                    }
                }
            }
        }

        // Add-on
        float basket = pricing.getAddOnBasketSize(); // Product
        float addOn = pricing.getAddOnItem();        // Search
        addOnBasketPrice = Math.max(basket, addOn);

        // Oversize
        float heavy = pricing.getHeavyWeightShipCharge(); // Product
        float oversize = pricing.getOverSizeItem();       // Search
        extraShippingCharge = Math.max(heavy, oversize);
    }

    // Sorting comparators

    private static int compareFloats(float x, float y) {
        float s = x-y;
        if (s<0) return(-1);
        if (s>0) return(1);
        return(0);
    }

    private static int compareStrings(String x, String y) {
        if (x==null) {
            if (y==null) return(0);
            else return(1);
        }
        else {
            if (y==null) return(-1);
            else return(x.compareToIgnoreCase(y));
        }
    }

    public static class IndexSort implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareFloats(x.index, y.index);
            return(s);
        }
    }

    public static class HighestRated implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareFloats(y.customerRating, x.customerRating);
            if (s==0) {
                s = compareFloats(y.customerCount, x.customerCount);
                if (s==0) {
                    s = compareFloats(x.index, y.index);
                }
            }
            return(s);
        }
    }

    public static class PriceAscending implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareFloats(x.finalPrice, y.finalPrice);
            if (s==0) {
                s = compareFloats(x.index, y.index);
            }
            return(s);
        }
    }

    public static class PriceDescending implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareFloats(y.finalPrice, x.finalPrice);
            if (s==0) {
                s = compareFloats(y.index, x.index);
            }
            return(s);
        }
    }
    public static class TitleAscending implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareStrings(x.title, y.title);
            if (s==0) {
                s = compareFloats(x.index, y.index);
            }
            return(s);
        }
    }

    public static class TitleDescending implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareStrings(y.title, x.title);
            if (s==0) {
                s = compareFloats(y.index, x.index);
            }
            return(s);
        }
    }
}
