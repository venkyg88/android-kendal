package com.staples.drawertest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
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

    private enum FragmentWrapper {
        ALFA    (0, AlfaFragment.class,    R.string.alfa_title),
        BRAVO   (1, BravoFragment.class,   R.string.bravo_title),
        CHARLIE (2, CharlieFragment.class, R.string.charlie_title);

        private int index;
        private Class fragmentClass;
        private int titleId;

        private FragmentWrapper(int index, Class fragmentClass, int titleId) {
            this.index = index;
            this.fragmentClass = fragmentClass;
            this.titleId = titleId;
        }

        private String getTitle(Context context) {
            String title = context.getResources().getString(titleId);
            return(title);
        }
    }

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    private static final int NFRAGMENTS = FragmentWrapper.values().length;
    private Fragment fragments[] = new Fragment[NFRAGMENTS];

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        drawer = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.drawable.ic_drawer,
                                                                 R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        /* fill adapter with fragment titles */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_item);
        for(FragmentWrapper fw : FragmentWrapper.values())
            adapter.add(fw.getTitle(this));

        /* initialize list view */
        ListView navigate = (ListView) findViewById(R.id.navigate);
        navigate.setAdapter(adapter);
        navigate.setOnItemClickListener(this);

        /* select initial fragment */
        selectSingleFragment(FragmentWrapper.BRAVO.index);
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

    public boolean selectSingleFragment(int index) {
        if (index<0 || index>=NFRAGMENTS) return(false);

        if (fragments[index]==null)
            fragments[index] = Fragment.instantiate(this, FragmentWrapper.values()[index].fragmentClass.getName());

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, fragments[index]);
        transaction.commit();

        return(true);
    }

    public void onItemClick(AdapterView parent, View view, int position, long id) {
        if (selectSingleFragment(position))
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
