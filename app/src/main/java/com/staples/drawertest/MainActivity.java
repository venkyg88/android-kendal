package com.staples.drawertest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawer;
    private ListView navigate;
    private FrameLayout content;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        // Find top-level entities
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        navigate = (ListView) findViewById(R.id.navigate);
        content = (FrameLayout) findViewById(R.id.content);

        // Initialize action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.action_bar, null);
        view.findViewById(R.id.action_drawer).setOnClickListener(this);
        actionBar.setCustomView(view);

        // Initialize list view
        DrawerAdapter adapter = new DrawerAdapter(this);
        adapter.fill();
        navigate.setAdapter(adapter);
        navigate.setOnItemClickListener(this);

        // Select splash fragment
        DrawerItem item = new DrawerItem(DrawerItem.Type.FRAGMENT, this, 0, 0, SplashFragment.class);
        selectSingleFragment(item, false);
    }

    private boolean selectSingleFragment(DrawerItem item, boolean push) {
        // Safety check
        if (item==null || item.fragmentClass==null) return(false);

        // Create Fragment if necessary
        if (item.fragment == null)
            item.fragment = Fragment.instantiate(this, item.fragmentClass.getName());

        // Check if Fragment present already
        else {
            int count = content.getChildCount();
            for(int i=0;i<count;i++)
                if (content.getChildAt(i)==item.fragment.getView())
                    return (true);
        }

        // Swap Fragments
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, item.fragment);
        if (push)
            transaction.addToBackStack(null);
        transaction.commit();

        return(true);
    }

    // ActionBar clicks
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.action_drawer:
                if (!drawer.isDrawerOpen(navigate)) drawer.openDrawer(navigate);
                else drawer.closeDrawer(navigate);
                break;
        }
    }

    // Drawer clicks
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        if (item.fragmentClass!=null) {
            if (selectSingleFragment(item, true))
                drawer.closeDrawer(navigate);
        }
    }
}
