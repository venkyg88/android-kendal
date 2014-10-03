package com.staples.mobile.cfa;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.bundle.BundleFragment;
import com.staples.mobile.cfa.sku.SkuFragment;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";

    private static final Uri STAPLESWEBSITE = Uri.parse("http://m.staples.com/");

    private static final int SURRENDER_TIMEOUT = 5000;

    private DrawerLayout drawerLayout;
    private ListView leftDrawer;
    private ViewGroup topper;
    private View rightDrawer;

    private DrawerItem homeDrawerItem;
    private DrawerItem searchDrawerItem;
    private DrawerItem storeDrawerItem;
    private DrawerItem rewardsDrawerItem;

    public enum Transition {
        NONE  (0, 0, 0, 0, 0),
        SLIDE (0, R.animator.push_enter, R.animator.push_exit, R.animator.pop_enter, R.animator.pop_exit),
        FADE (0, R.animator.fade_in, R.animator.fade_out, 0, 0);

        private int standard;
        private int push_enter;
        private int push_exit;
        private int pop_enter;
        private int pop_exit;

        Transition(int standard, int push_enter, int push_exit, int pop_enter, int pop_exit) {
            this.standard = standard;
            this.push_enter = push_enter;
            this.push_exit = push_exit;
            this.pop_enter = pop_enter;
            this.pop_exit = pop_exit;
        }

        public void setAnimation(FragmentTransaction transaction) {
            if (standard!=0) {
                transaction.setTransition(standard);
            }
            else if (pop_enter!=0 && pop_exit!=0) {
                transaction.setCustomAnimations(push_enter, push_exit, pop_enter, pop_exit);
            }
            else if (push_enter!=0 && push_exit!=0) {
                transaction.setCustomAnimations(push_enter, push_exit);
            }
        }
    };

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        boolean freshStart = (bundle==null);
        prepareMainScreen(freshStart);
    }

    public void showMainScreen() {
        findViewById(R.id.splash).setVisibility(View.GONE);
        findViewById(R.id.main).setVisibility(View.VISIBLE);
    }

    public void prepareMainScreen(boolean freshStart) {
        // Inflate
        setContentView(R.layout.main);

        // Find top-level entities
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawer = (ListView) findViewById(R.id.left_drawer);
        topper = (ViewGroup) findViewById(R.id.topper);
        rightDrawer = findViewById(R.id.right_drawer);

        // Set action bar listeners
        findViewById(R.id.action_left_drawer).setOnClickListener(this);
        findViewById(R.id.action_home).setOnClickListener(this);
        findViewById(R.id.action_search).setOnClickListener(this);
        findViewById(R.id.action_right_drawer).setOnClickListener(this);

        // Initialize left drawer listview
        DrawerAdapter adapter = new DrawerAdapter(this);
        leftDrawer.setAdapter(adapter);
        adapter.fill();
        leftDrawer.setOnItemClickListener(this);

        // Create non-drawer DrawerItems
        homeDrawerItem = adapter.getItem(0); // TODO Hard-coded alias
        searchDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.ic_search, R.string.search_title, ToBeDoneFragment.class);
        storeDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, R.string.store_info_title, ToBeDoneFragment.class);
        rewardsDrawerItem = adapter.getItem(6); // TODO Hard-coded alias

        // Initialize topper
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.topper, topper);
        topper.findViewById(R.id.action_store).setOnClickListener(this);
        topper.findViewById(R.id.action_rewards).setOnClickListener(this);

        // Initialize right drawer listview TODO just hacked for demo
        ArrayAdapter<String> cartAdapter = new ArrayAdapter<String>(this, R.layout.drawer_category);
        ((ListView) rightDrawer.findViewById(R.id.cart_list)).setAdapter(cartAdapter);
        cartAdapter.add("Apple");
        cartAdapter.add("Banana");
        cartAdapter.add("Cantaloupe");

        // Fresh start?
        if (freshStart) {
            selectDrawerItem(homeDrawerItem, Transition.NONE, false);
            Runnable runs = new Runnable() {public void run() {
                showMainScreen();}};
            new Handler().postDelayed(runs, SURRENDER_TIMEOUT);
        } else {
            showMainScreen();
        }
    }

    // Navigation

    public boolean selectFragment(Fragment fragment, Transition transition, boolean push) {
        // Make sure all drawers are closed
        drawerLayout.closeDrawers();

        // Swap Fragments
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (transition!=null) transition.setAnimation(transaction);
        transaction.replace(R.id.content, fragment);
        if (push)
            transaction.addToBackStack(null);
        transaction.commit();
        return(true);
    }

    private boolean selectDrawerItem(DrawerItem item, Transition transition, boolean push) {
        // Safety check
        if (item == null || item.fragmentClass == null) return (false);

        // Create Fragment if necessary
        if (item.fragment == null)
            item.instantiate(this);
        return(selectFragment(item.fragment, transition, push));
    }

    public boolean selectBundle(String title, String path) {
        DrawerItem item = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, 0, BundleFragment.class);
        item.title = title;
        item.path = path;
        selectDrawerItem(item, Transition.SLIDE, true);
        return (true);
    }

    public boolean selectSkuItem(String identifier) {
        DrawerItem item = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, R.string.home_title, SkuFragment.class);
        item.identifier = identifier;
        selectDrawerItem(item, Transition.SLIDE, true);
        return(true);
    }

    // Action bar & topper clicks

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.action_left_drawer:
                if (!drawerLayout.isDrawerOpen(leftDrawer)) {
                    drawerLayout.closeDrawer(rightDrawer);
                    drawerLayout.openDrawer(leftDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.action_home:
                selectDrawerItem(homeDrawerItem, Transition.NONE, true);
                break;

            case R.id.action_search:
                selectDrawerItem(searchDrawerItem, Transition.SLIDE, true);
                break;

            case R.id.action_right_drawer:
                if (!drawerLayout.isDrawerOpen(rightDrawer)) {
                    drawerLayout.closeDrawer(leftDrawer);
                    drawerLayout.openDrawer(rightDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.action_store:
                selectDrawerItem(storeDrawerItem, Transition.SLIDE, true);
                break;

            case R.id.action_rewards:
                selectDrawerItem(rewardsDrawerItem, Transition.SLIDE, true);
                break;
        }
    }

    // Left drawer listview clicks

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerAdapter adapter;

        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        switch(item.type) {
            case FRAGMENT:
                drawerLayout.closeDrawers();
                selectDrawerItem(item, Transition.SLIDE, true);
                break;

            case BROWSE:
                adapter = (DrawerAdapter) parent.getAdapter();
                adapter.setBrowseMode(true);
                break;

            case BACKTOTOP:
                adapter = (DrawerAdapter) parent.getAdapter();
                adapter.setBrowseMode(false);
                break;

            case STACK:
                adapter = (DrawerAdapter) parent.getAdapter();
                adapter.popStack(item);
                break;

            case CATEGORY:
                String identifier = item.getBundleIdentifier();
                if (identifier!=null) {
                    drawerLayout.closeDrawers();
                    selectBundle(item. title, item.path);
                } else {
                    adapter = (DrawerAdapter) parent.getAdapter();
                    adapter.pushStack(item);
                }
                break;
        }
    }
}
