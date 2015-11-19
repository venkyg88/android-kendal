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

    public DailyDealsItem(String name, int soldCount,String endDate,Details details,String identifier) {
        this.name = name;
        this.soldCount = soldCount;
        this.endDate = endDate;
        this.details = details;
        this.identifier = identifier;
    }
}
