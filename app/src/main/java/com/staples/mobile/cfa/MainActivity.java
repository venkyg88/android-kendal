package com.staples.mobile.cfa;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
import com.staples.mobile.cfa.sku.SkuFragment;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";

    private static final Uri STAPLESWEBSITE = Uri.parse("http://m.staples.com/");

    private DrawerLayout drawerLayout;
    private ListView leftDrawer;
    private ViewGroup topper;
    private View rightDrawer;

    private DrawerItem splashDrawerItem;
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
        view.findViewById(R.id.search).setOnClickListener(this);
        view.findViewById(R.id.right_drawer).setOnClickListener(this);

        // Initialize left drawer listview
        DrawerAdapter adapter = new DrawerAdapter(this);
        leftDrawer.setAdapter(adapter);
        adapter.fill();
        leftDrawer.setOnItemClickListener(this);

        // Create non-drawer DrawerItems
        splashDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, 0, SplashFragment.class);
        searchDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.ic_search, R.string.search_title, ToBeDoneFragment.class);
        storeDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, R.string.store_info_title, ToBeDoneFragment.class);
        rewardsDrawerItem = adapter.getItem(6); // TODO Hard-coded alias

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

        // If first time running
        if (bundle == null) {
            // TODO hide ActionBar
            topper.setVisibility(View.GONE);
            selectDrawerItem(splashDrawerItem, Transition.NONE, false);

            // Robolectric runs postDelayed immediately and can't tolerate queued transactions
            getFragmentManager().executePendingTransactions();

            final DrawerItem item = adapter.getItem(0);
            Runnable runs = new Runnable() {public void run() {selectDrawerItem(item, Transition.FADE, false);}};
            new Handler().postDelayed(runs, 2000);
        }
    }

    // Navigation

    public boolean selectFragment(Fragment fragment, Transition transition, boolean push) {
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

    public boolean selectSkuItem(String identifier) {
        DrawerItem item = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, R.string.home_title, SkuFragment.class);
        item.identifier = identifier;
        selectDrawerItem(item, Transition.SLIDE, true);
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

            case R.id.search:
                selectDrawerItem(searchDrawerItem, Transition.SLIDE, true);
                break;

            case R.id.right_drawer:
                if (!drawerLayout.isDrawerOpen(rightDrawer)) {
                    drawerLayout.closeDrawer(leftDrawer);
                    drawerLayout.openDrawer(rightDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.store:
                selectDrawerItem(storeDrawerItem, Transition.SLIDE, true);
                break;

            case R.id.rewards:
                selectDrawerItem(rewardsDrawerItem, Transition.SLIDE, true);
                break;
        }
    }

    // Left drawer listview clicks
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        if (item.fragmentClass!=null) {
            if (selectDrawerItem(item, Transition.SLIDE, true))
                drawerLayout.closeDrawers();
        }
    }
}
