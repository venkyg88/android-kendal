package app.staples.mobile.cfa.dailydeals;

import com.staples.mobile.common.access.easyopen.model.dailydeals.Details;

import app.staples.mobile.cfa.util.MiscUtils;

/**
 * Created by Hardik Patel on 17/11/15.
 */
public class DailyDealsItem {

    public String name;
    public int soldCount;
    public String endDate;
    public Details details;
    public boolean busy;
    public String identifier;

//    public String productImage;

    //    public String dealPromoMessage = product.getDetails().getDealPromoMessage();
//    public int rating;
//    public int numberOfReview;
//    public String unitOfMeasure;
    //TODO pricing

    public DailyDealsItem(String name, int soldCount,String endDate,Details details,String identifier)
    {
        this.name = name;
        this.soldCount = soldCount;
        this.endDate = endDate;
        this.details = details;
        this.identifier = identifier;
//        this.productImage = productImage;
//        this.rating = rating;
//        this.numberOfReview = numberOfReview;
//        this.unitOfMeasure = unitOfMeasure;
    }
}
