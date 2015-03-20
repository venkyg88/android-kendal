package com.staples.mobile.cfa.bundle;

import com.staples.mobile.cfa.IdentifierType;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.browse.*;

import java.util.Comparator;
import java.util.List;

public class BundleItem {
    public enum SortType {
        ORIGINAL        (R.string.sort_best_match,       R.id.sort_best_match,       new BundleItem.Original()),
        PRICEASCENDING  (R.string.sort_price_ascending,  R.id.sort_price_ascending,  new BundleItem.PriceAscending()),
        PRICEDESCENDING (R.string.sort_price_descending, R.id.sort_price_descending, new BundleItem.PriceDescending()),
        TITLEASCENDING  (R.string.sort_title_ascending,  R.id.sort_title_ascending,  new BundleItem.TitleAscending()),
        TITLEDESCENDING (R.string.sort_title_descending, R.id.sort_title_descending, new BundleItem.TitleDescending());

        public final int title;
        public final int button;
        public final Comparator<BundleItem> comparator;

        SortType(int title, int button, Comparator<BundleItem> comparator) {
            this.title = title;
            this.button = button;
            this.comparator = comparator;
        }

        public static SortType findSortTypeById(int id) {
            for(SortType type : values()) {
                if (type.button==id) return(type);
            }
            return(null);
        }
    }

    public int index;
    public String title;
    public String identifier;
    public IdentifierType type;
    public String imageUrl;
    public float price;
    public String unit;
    public float customerRating;
    public int customerCount;

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

    public Float setPrice(List<Pricing> pricings) {
        if (pricings==null) return(null);
        for(Pricing pricing : pricings) {
            float finalPrice = pricing.getFinalPrice();
            if (finalPrice>0.0f) {
                price = finalPrice;
                unit = pricing.getUnitOfMeasure();
                return(price);
            }
        }
        return(null);
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

    public static class Original implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareFloats(x.index, y.index);
            return(s);
        }
    }

    public static class PriceAscending implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareFloats(x.price, y.price);
            if (s==0) {
                s = compareFloats(x.index, y.index);
            }
            return(s);
        }
    }

    public static class PriceDescending implements Comparator<BundleItem> {
        @Override
        public int compare(BundleItem x, BundleItem y) {
            int s = compareFloats(y.price, x.price);
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
