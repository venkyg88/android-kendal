package app.staples.mobile.cfa.sku;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import app.staples.R;

public class SkuImageAdapter extends PagerAdapter {
    private static final String TAG = SkuImageAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<SkuImageItem> array;
    private Picasso picasso;
    private int imageWidth;
    private int imageHeight;

    private static class SkuImageItem {
        ImageView view;
        String url;
    }

    public SkuImageAdapter(Context context) {
        super();
        this.context = context;
        picasso = Picasso.with(context);
        Resources res = context.getResources();
        imageWidth = res.getDimensionPixelSize(R.dimen.sku_image_width);
        imageHeight = res.getDimensionPixelSize(R.dimen.sku_image_height);

        array = new ArrayList<SkuImageItem>();
    }

    @Override
    public int getCount() {
        return (array.size());
    }

    public void add(String url) {
        SkuImageItem item = new SkuImageItem();
        item.url = url;
        array.add(item);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SkuImageItem item = array.get(position);

        item.view = new ImageView(context);
        picasso.load(item.url).error(R.drawable.no_photo).resize(imageWidth, imageHeight).centerInside().into(item.view);

        container.addView(item.view);
        return (item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        SkuImageItem item = array.get(position);
        container.removeView(item.view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==((SkuImageItem) object).view);
    }
}
