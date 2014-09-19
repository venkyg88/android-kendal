package com.staples.mobile.lms;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by pyhre001 on 9/16/14.
 */
public class LmsAdapter extends PagerAdapter {
    private static final String TAG = "LmsAdapter";

    private ArrayList<String> array;

    public LmsAdapter() {
        super();
        array = new ArrayList();
    }

    @Override
    public int getCount() {
        return(array.size());
    }

    public void add(String title) {
        array.add(title);
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return(null);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    public boolean	 isViewFromObject(View view, Object object) {
        return(true);
    }

    public String getPageTitle(int position) {
        return(array.get(position));
    }
}
