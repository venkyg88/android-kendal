package com.staples.drawertest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private static final String TOPFRAGMENT = "TopFragment";

    private DrawerLayout drawerLayout;
    private FrameLayout content;
    private ListView leftDrawer;
    private View rightDrawer;
    private SearchView searchText;

    Fragment topFrag;
    Fragment bottomFrag;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        // Find top-level entities
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        content = (FrameLayout) findViewById(R.id.content);
        leftDrawer = (ListView) findViewById(R.id.left_drawer);
        rightDrawer = findViewById(R.id.right_drawer);

        // Initialize action bar
        ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.ic_drawer);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.staples);

//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setDisplayShowHomeEnabled(false);
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(false);
//
//        actionBar.setCustomView(R.layout.action_bar);
//        View view = actionBar.getCustomView();
//        view.findViewById(R.id.action_left_drawer).setOnClickListener(this);
//        view.findViewById(R.id.action_right_drawer).setOnClickListener(this);
//
//        searchText = (SearchView) findViewById(R.id.search_text);
//        searchText.setOnSearchClickListener(this);
//        searchText.setOnQueryTextListener(this);

        // Initialize left drawer listview
        DrawerAdapter adapter = new DrawerAdapter(this);
        adapter.fill();
        leftDrawer.setAdapter(adapter);
        leftDrawer.setOnItemClickListener(this);

        // Initialize right drawer listview TODO just hacked for demo
        ArrayAdapter<String> cartAdapter = new ArrayAdapter<String>(this, R.layout.category_item);
        ((ListView) rightDrawer.findViewById(R.id.cart_list)).setAdapter(cartAdapter);
        cartAdapter.add("Apple");
        cartAdapter.add("Banana");
        cartAdapter.add("Cantaloupe");

        // Select splash fragment if first run
        if (bundle == null) {
//            DrawerItem item = new DrawerItem(DrawerItem.Type.FRAGMENT, this, 0, 0, SplashFragment.class);
//            selectDrawerItem(item, false);
// TODO Hacked test of two fragments in one container
            topFrag = new DrawerItem(DrawerItem.Type.FRAGMENT, this, 0, 0, PersonalFragment.class).instantiate(this);
            bottomFrag = new DrawerItem(DrawerItem.Type.FRAGMENT, this, 0, 0, SplashFragment.class).instantiate(this);

            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.content, topFrag);
            transaction.add(R.id.content, bottomFrag);
            transaction.commit();

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
        transaction.setCustomAnimations(R.animator.push_enter, R.animator.push_exit, R.animator.pop_enter, R.animator.pop_exit);
        transaction.remove(topFrag);
        transaction.remove(bottomFrag);
        transaction.replace(R.id.content, item.fragment);
        if (push)
            transaction.addToBackStack(null);
        transaction.commit();

        return (true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return(super.onCreateOptionsMenu(menu));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!drawerLayout.isDrawerOpen(leftDrawer)) {
                    drawerLayout.closeDrawer(rightDrawer);
                    drawerLayout.openDrawer(leftDrawer);
                } else drawerLayout.closeDrawers();
                return(true);

            case R.id.action_search:
                drawerLayout.closeDrawers();
                // TODO search
                return(true);

            case R.id.action_right_drawer:
                if (!drawerLayout.isDrawerOpen(rightDrawer)) {
                    drawerLayout.closeDrawer(leftDrawer);
                    drawerLayout.openDrawer(rightDrawer);
                } else drawerLayout.closeDrawers();
                return(true);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Drawer clicks
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        if (item.fragmentClass!=null) {
            if (selectDrawerItem(item, true))
                drawerLayout.closeDrawers();
        }
    }
}
