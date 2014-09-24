package com.staples.mobile;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.staples.mobile.sku.SkuFragment;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";

    private static final Uri STAPLESWEBSITE = Uri.parse("http://m.staples.com/");

    private DrawerLayout drawerLayout;
    private ListView leftDrawer;
    private ViewGroup topper;
    private View rightDrawer;

    private DrawerItem storeDrawerItem;
    private DrawerItem rewardsDrawerItem;
    private DrawerItem skuDrawerItem;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        // Find top-level entities
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawer = (ListView) findViewById(R.id.left_drawer);
        topper = (ViewGroup) findViewById(R.id.topper);
        rightDrawer = findViewById(R.id.right_drawer);

        // Initialize action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar);

        // Set action bar listeners
        View view = actionBar.getCustomView();
        view.findViewById(R.id.left_drawer).setOnClickListener(this);
        view.findViewById(R.id.website).setOnClickListener(this);
        view.findViewById(R.id.right_drawer).setOnClickListener(this);

        // Initialize left drawer listview
        DrawerAdapter adapter = new DrawerAdapter(this);
        leftDrawer.setAdapter(adapter);
        adapter.fill();
        leftDrawer.setOnItemClickListener(this);

        // Initialize non-drawer DrawerItems
        storeDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, R.string.store_info_title, ToBeDoneFragment.class);
        rewardsDrawerItem = adapter.getItem(6); // TODO Hard-coded alias
        skuDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, R.string.home_title, SkuFragment.class);

        // Initialize topper
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.topper, topper);
        topper.findViewById(R.id.store).setOnClickListener(this);
        topper.findViewById(R.id.rewards).setOnClickListener(this);

        // Initialize right drawer listview TODO just hacked for demo
        ArrayAdapter<String> cartAdapter = new ArrayAdapter<String>(this, R.layout.category_item);
        ((ListView) rightDrawer.findViewById(R.id.cart_list)).setAdapter(cartAdapter);
        cartAdapter.add("Apple");
        cartAdapter.add("Banana");
        cartAdapter.add("Cantaloupe");

        // Select first drawer item if first run
        if (bundle == null) {
            selectDrawerItem(adapter.getItem(0), false);
        }
    }

    private boolean selectDrawerItem(DrawerItem item, boolean push) {
        // Safety check
        if (item == null || item.fragmentClass == null) return (false);

        // Create Fragment if necessary
        if (item.fragment == null)
            item.instantiate(this);

        // Swap Fragments
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (push)
            transaction.setCustomAnimations(R.animator.push_enter, R.animator.push_exit, R.animator.pop_enter, R.animator.pop_exit);
        transaction.replace(R.id.content, item.fragment);
        if (push)
            transaction.addToBackStack(null);
        transaction.commit();

        return(true);
    }

    public boolean selectSkuItem(String identifier) {
        skuDrawerItem.identifier = identifier;
        selectDrawerItem(skuDrawerItem, true);
        return(true);
    }

    public boolean openWebsite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, STAPLESWEBSITE);
        try {
            startActivity(intent);
            return (true);
        } catch(Exception e)  {
            return(false);
        }
    }

    // Action bar & topper clicks

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.left_drawer:
                if (!drawerLayout.isDrawerOpen(leftDrawer)) {
                    drawerLayout.closeDrawer(rightDrawer);
                    drawerLayout.openDrawer(leftDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.website:
                openWebsite();
                break;

            case R.id.right_drawer:
                if (!drawerLayout.isDrawerOpen(rightDrawer)) {
                    drawerLayout.closeDrawer(leftDrawer);
                    drawerLayout.openDrawer(rightDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.store:
                selectDrawerItem(storeDrawerItem, true);
                break;

            case R.id.rewards:
                selectDrawerItem(rewardsDrawerItem, true);
                break;
        }
    }

    // Left drawer listview clicks
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        if (item.fragmentClass!=null) {
            if (selectDrawerItem(item, true))
                drawerLayout.closeDrawers();
        }
    }
}
