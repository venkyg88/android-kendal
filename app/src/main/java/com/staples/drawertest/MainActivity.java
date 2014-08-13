package com.staples.drawertest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private class FragmentWrapper {
        private Fragment fragment;
        private Class fragmentClass;
        private String title;

        private FragmentWrapper(Context context, Class fragmentClass, Integer titleId) {
            this.fragmentClass = fragmentClass;
            if (titleId!=null)
                title = context.getResources().getString(titleId);
        }

        public String toString() {
            return(title);
        }
    }

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

        // Fill adapter with fragment titles
        ArrayAdapter<FragmentWrapper> adapter = new ArrayAdapter<FragmentWrapper>(this, R.layout.drawer_item);
        adapter.add(new FragmentWrapper(this, AlfaFragment.class,    R.string.alfa_title));
        adapter.add(new FragmentWrapper(this, BravoFragment.class,   R.string.bravo_title));
        adapter.add(new FragmentWrapper(this, CharlieFragment.class, R.string.charlie_title));

        // Initialize list view
        ListView navigate = (ListView) findViewById(R.id.navigate);
        navigate.setAdapter(adapter);
        navigate.setOnItemClickListener(this);

        // Select initial fragment
        selectSingleFragment(new FragmentWrapper(this, SplashFragment.class, null), false);
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

    private boolean selectSingleFragment(FragmentWrapper fw, boolean push) {
        // Safety check
        if (fw==null) return(false);

        // Create Fragment if necessary
        if (fw.fragment == null)
            fw.fragment = Fragment.instantiate(this, fw.fragmentClass.getName());

        // Swap Fragments
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, fw.fragment);
        if (push)
            transaction.addToBackStack(null);
        transaction.commit();

        return(true);
    }

    public void onItemClick(AdapterView parent, View view, int position, long id) {
        FragmentWrapper fw = (FragmentWrapper) parent.getItemAtPosition(position);
        if (selectSingleFragment(fw, true))
            drawer.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
