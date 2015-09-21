package app.staples.mobile.cfa.weeklyad_refresh;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import app.staples.R;

/**
 * Created by Avinash Dodda.
 */
public class WeeklyAdImageAdapter extends PagerAdapter {
    Context context;
    private ArrayList<WeeklyAdImageItem> array;
    private Picasso picasso;

    private static class WeeklyAdImageItem {
        ImageView view;
        String url;
    }

    WeeklyAdImageAdapter(Context context){
        this.context=context;
        picasso = Picasso.with(context);
        Resources res = context.getResources();
        array = new ArrayList<WeeklyAdImageItem>();
    }

    @Override
    public int getCount() {
        return array.size();
    }

    public void add(String url) {
        WeeklyAdImageItem item = new WeeklyAdImageItem();
        item.url = url;
        array.add(item);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        WeeklyAdImageItem item = array.get(position);
        item.view = new ImageView(context);
        picasso.load(item.url)
                .error(R.drawable.no_photo)
                .fit()
                .into(item.view);

        container.addView(item.view);
        return (item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        WeeklyAdImageItem item = array.get(position);
        container.removeView(item.view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==((WeeklyAdImageItem) object).view);
    }
}
