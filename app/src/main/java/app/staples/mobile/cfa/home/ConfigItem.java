package app.staples.mobile.cfa.home;

import android.util.Log;
import android.widget.ListView;

public class ConfigItem {

    private static final String TAG = ConfigItem.class.getSimpleName();

    public String title;
    public String bannerUrl;
    public String identifier;
    public String size;
    public ListView productListView;

    ConfigItem(String title, String bannerUrl, String identifier, String size) {

        Log.v(TAG, "ConfigItem:ConfigItem():"
                + " size[" + size + "]"
                + " title[" + title + "]"
                + " bannerUrl[" + bannerUrl + "]"
                + " identifier[" + identifier + "]"
                + " this[" + this + "]"
        );

        this.title = title;
        this.bannerUrl = bannerUrl;
        this.identifier = identifier;
        this.size = size;
    }
}
