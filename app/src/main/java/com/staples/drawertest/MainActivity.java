package com.staples.drawertest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private FrameLayout content;
    private ListView leftDrawer;
    private View rightDrawer;
    private SearchView searchText;

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
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setCustomView(R.layout.action_bar);
        View view = actionBar.getCustomView();
        view.findViewById(R.id.action_left_drawer).setOnClickListener(this);
        view.findViewById(R.id.action_right_drawer).setOnClickListener(this);

        searchText = (SearchView) findViewById(R.id.search_text);
        searchText.setOnSearchClickListener(this);
        searchText.setOnQueryTextListener(this);

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
            DrawerItem item = new DrawerItem(DrawerItem.Type.FRAGMENT, this, 0, 0, SplashFragment.class);
            selectDrawerItem(item, false);
        }
    }

    private boolean selectDrawerItem(DrawerItem item, boolean push) {
        // Safety check
        if (item == null || item.fragmentClass == null) return (false);

        // Create Fragment if necessary
        if (item.fragment == null)
            item.instantiate(this);

            // Check if Fragment present already
        else {
            int count = content.getChildCount();
            for (int i = 0; i < count; i++)
                if (content.getChildAt(i) == item.fragment.getView())
                    return (true);
        }

        // Swap Fragments
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, item.fragment);
        if (push)
            transaction.addToBackStack(null);
        transaction.commit();

        return (true);
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
    }

    // ActionBar clicks
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_left_drawer:
                closeKeyboard();
                if (!drawerLayout.isDrawerOpen(leftDrawer)) {
                    drawerLayout.closeDrawer(rightDrawer);
                    drawerLayout.openDrawer(leftDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.search_text:
                drawerLayout.closeDrawers();
                break;

            case R.id.action_right_drawer:
                closeKeyboard();
                if (!drawerLayout.isDrawerOpen(rightDrawer)) {
                    drawerLayout.closeDrawer(leftDrawer);
                    drawerLayout.openDrawer(rightDrawer);
                } else drawerLayout.closeDrawers();
                break;
        }
    }

    // SearchView updates
    public boolean onQueryTextChange(String query) {
        Log.d(TAG, "Search onQueryTextChange " + query);
        return(true);
    }

    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG,"Search onQueryTextSubmit "+query);
        closeKeyboard();
        return(true);
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
