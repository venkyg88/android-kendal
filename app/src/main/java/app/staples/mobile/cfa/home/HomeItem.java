package app.staples.mobile.cfa.home;

public class HomeItem {
    private static final String TAG = HomeItem.class.getSimpleName();

    public String title;
    public String bannerUrl;
    public String identifier;
    public String size;

    HomeItem(String title, String bannerUrl, String identifier, String size) {
        this.title = title;
        this.bannerUrl = bannerUrl;
        this.identifier = identifier;
        this.size = size;
    }
}
