package com.staples.drawertest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        // Initialize drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.drawable.ic_drawer,
                                           R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Initialize list view
        ListView navigate = (ListView) findViewById(R.id.navigate);
        DrawerAdapter adapter = new DrawerAdapter(this);
        adapter.fill();
        navigate.setAdapter(adapter);
        navigate.setOnItemClickListener(this);

        // Select initial fragment
        selectSingleFragment(new DrawerItem(DrawerItem.Type.FRAGMENT, this, 0, 0, SplashFragment.class), false);
    }
    @Override
    protected void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private boolean selectSingleFragment(DrawerItem item, boolean push) {
        // Safety check
        if (item==null || item.fragmentClass==null) return(false);

        // Create Fragment if necessary
        if (item.fragment == null)
            item.fragment = Fragment.instantiate(this, item.fragmentClass.getName());

        // Swap Fragments
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, item.fragment);
        if (push)
            transaction.addToBackStack(null);
        transaction.commit();

        return(true);
    }

    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        if (item.fragmentClass!=null) {
            if (selectSingleFragment(item, true))
                drawer.closeDrawers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
