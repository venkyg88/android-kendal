package com.staples.mobile.cfa;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.bundle.BundleFragment;
import com.staples.mobile.cfa.cart.CartAdapter;
import com.staples.mobile.cfa.search.SearchFragment;
import com.staples.mobile.cfa.search.SuggestTask;
import com.staples.mobile.cfa.sku.SkuFragment;
import com.staples.mobile.cfa.widget.BadgeImageView;
import com.staples.mobile.cfa.widget.DataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener, LoginHelper.OnLoginCompleteListener {
    private static final String TAG = "MainActivity";

    private static final int SURRENDER_TIMEOUT = 5000;

    private DrawerLayout drawerLayout;
    private View leftDrawer;
    private ViewGroup topper;
    private View rightDrawer;
    private BadgeImageView rightDrawerAction;
    private TextView cartTitle;
    private CartAdapter cartAdapter;

    private DrawerItem homeDrawerItem;
    private DrawerItem searchDrawerItem;
    private DrawerItem storeDrawerItem;
    private DrawerItem rewardsDrawerItem;

    // search related variables:
    public final static String BEST_MATCHES = "0";
    private AutoCompleteTextView editText;
    private Button deleteButton;
    private Handler guiThread;
    private ExecutorService suggestThread;
    private Runnable getSuggestionTask;
    private Future<?> suggPending;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> saveKeywordsList;
    private List<String> items;

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
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        boolean freshStart = (bundle == null);
        prepareMainScreen(freshStart);

        LoginHelper loginHelper = new LoginHelper(this);
        loginHelper.setOnLoginCompleteListener(this);
        loginHelper.getRegisteredUserTokens();
        //loginHelper.getGuestTokens();

        initSearchBar();
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
        leftDrawer = findViewById(R.id.left_drawer);
        topper = (ViewGroup) findViewById(R.id.topper);
        rightDrawer = findViewById(R.id.right_drawer);
        rightDrawerAction = (BadgeImageView)findViewById(R.id.action_right_drawer);
        cartTitle = (TextView)findViewById(R.id.checkout);

        // Set action bar listeners
        findViewById(R.id.action_left_drawer).setOnClickListener(this);
        findViewById(R.id.action_home).setOnClickListener(this);
        findViewById(R.id.action_search).setOnClickListener(this);
        findViewById(R.id.action_right_drawer).setOnClickListener(this);

        // Initialize left drawer listview
        DataWrapper wrapper = (DataWrapper) findViewById(R.id.left_drawer);
        ListView menu = (ListView) wrapper.findViewById(R.id.menu);
        DrawerAdapter adapter = new DrawerAdapter(this, wrapper);
        menu.setAdapter(adapter);
        adapter.fill();
        menu.setOnItemClickListener(this);

        // Create non-drawer DrawerItems
        homeDrawerItem = adapter.getItem(0); // TODO Hard-coded alias
        searchDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.ic_search, R.string.search_title, SearchFragment.class);
        storeDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, R.string.store_info_title, ToBeDoneFragment.class);
        rewardsDrawerItem = adapter.getItem(6); // TODO Hard-coded alias

        // Initialize topper
        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(R.layout.topper, topper);
        topper.findViewById(R.id.action_store).setOnClickListener(this);
        topper.findViewById(R.id.action_rewards).setOnClickListener(this);

        // Initialize right drawer cart listview
        ProgressBar cartProgressBar = (ProgressBar)rightDrawer.findViewById(R.id.cart_progress_bar);
        cartAdapter = new CartAdapter(this, R.layout.cart_item, cartProgressBar);
        cartAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                setCartItemCount(cartAdapter.getTotalCount());
            }
        });
//        cartAdapter.fill();  // can't fill cart until login process completes (asynchronous)
        this.setCartItemCount(0); // initialize count to zero until we're able to fill the cart
        ((ListView) rightDrawer.findViewById(R.id.cart_list)).setAdapter(cartAdapter);

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

    @Override
    public void onLoginComplete(boolean success, String errMsg) {
        if (success) {
            // load cart drawer (requires successful login)
            cartAdapter.fill();
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


    /** sets item count indicator on cart icon and cart drawer title */
    public void setCartItemCount(int count) {
        // set text of cart icon
        rightDrawerAction.setText(count == 0? "":String.valueOf(count));
        // Set text of cart drawer title
        cartTitle.setText(String.format(getResources().getString(R.string.your_cart), count,
                count==1? "":"s"));
    }

    /** adds an item to the cart */
    public void addItemToCart(String partNumber) {
        cartAdapter.addToCart(partNumber);
        drawerLayout.openDrawer(rightDrawer);
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
            case ACCOUNT:
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
                    selectBundle(item.title, item.path);
                } else {
                    adapter = (DrawerAdapter) parent.getAdapter();
                    adapter.pushStack(item);
                }
                break;
        }
    }

    // search methods:
    private void initSearchBar(){
        initSuggestThreading();
        findSearchViews();
        setSearchListeners();
        setAdapters();
        setSavedKeywords();
    }

    private void findSearchViews() {
        editText = (AutoCompleteTextView) findViewById(R.id.original_text);
        deleteButton = (Button) findViewById(R.id.suggestions_delete_button);
        deleteButton.setVisibility(View.GONE);
    }

    private void setSavedKeywords(){
        Context context = MainActivity.this;
        SharedPreferences sp = context.getSharedPreferences("RECENT_SEARCH_KEYWORDS", MODE_PRIVATE);

        String savedKeywordsString = sp.getString("KEYWORD_LIST", "");

        if(!savedKeywordsString.equals("")){
            String[] keywords = savedKeywordsString.split("/_/");
            saveKeywordsList = new ArrayList<String>();
            for(int i = keywords.length - 1; i >= 0; i--){
                // Log.d(TAG, "Each Saved Keyword -> " + keywords[i]);
                saveKeywordsList.add(keywords[i]);
            }
            setSuggestions(saveKeywordsList);
        }
    }

    private void setSearchListeners(){
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                queueUpdate(100); /* wait 100 milliseconds */
            }
            public void afterTextChanged(Editable s) {
                showSuggestDropDown(100);
            }
        });

        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String keyword = (String) parent.getItemAtPosition(position);
                if(keyword.equals(getResources().getString(R.string.no_results)) || SuggestTask.error != null){
                    Toast.makeText(getApplicationContext(), "Please try another keyword.", Toast.LENGTH_SHORT).show();
                }
                else{
                    doSearch(editText.getText().toString());
                }
            }
        });

        editText.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                editText.showDropDown();
                return false;
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch(editText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        deleteButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionevent)
            {
                int action = motionevent.getAction();
                if (action == MotionEvent.ACTION_UP)
                {
                    Log.d(TAG + "deleteButton", "deleteButton clicked");
                    editText.setText("");
                }
                return false;
            }
        });
    }

    private void doSearch(String keyword) {
        Toast.makeText(getApplicationContext(), "Searching " + editText.getText() + "...", Toast.LENGTH_SHORT).show();

        saveKeyword(keyword);

        if(keyword.length() > 0){
            selectDrawerItem(searchDrawerItem, Transition.FADE, true);
            editText.dismissDropDown();
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            SearchFragment.keyword = String.valueOf(editText.getText());
        }
        else{
            Toast.makeText(getApplicationContext(), "Please enter keyword.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveKeyword(String keyword){
        // Save the keyword before doing the search
        Context context = MainActivity.this;
        SharedPreferences sp = context.getSharedPreferences("RECENT_SEARCH_KEYWORDS", MODE_PRIVATE);

        String savedKeywordsString = sp.getString("KEYWORD_LIST", "");
        if(savedKeywordsString.equals("")){
            savedKeywordsString = keyword;
        }
        else{
            savedKeywordsString = savedKeywordsString + "/_/" + keyword;
        }

        if(keyword.equals("clear")){
            savedKeywordsString = "";
        }

        // save updated KEYWORD_LIST
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("KEYWORD_LIST", savedKeywordsString);
        editor.commit();
    }

    private void setAdapters() {
        items = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, R.layout.search_auto_suggestion_row, items);
        editText.setAdapter(adapter);
    }

    private void initSuggestThreading() {
        guiThread = new Handler();
        suggestThread = Executors.newSingleThreadExecutor();

        getSuggestionTask = new Runnable() {
            public void run() {
                String original = editText.getText().toString().trim();

                // Cancel previous suggestion if there was one
                if (suggPending != null){
                    suggPending.cancel(true);
                }

                // Check to make sure there is text to work on
                if (original.length() != 0) {
                    // Display delete button
                    deleteButton.setVisibility(View.VISIBLE);

                    try {
                        SuggestTask suggestTask = new SuggestTask(MainActivity.this, original);
                        suggPending = suggestThread.submit(suggestTask);
                    } catch (RejectedExecutionException e) {
                    }
                }
                else{
                    deleteButton.setVisibility(View.GONE);
                    adapter.clear();
                    setSavedKeywords();
                }
            }
        };
    }

    private void queueUpdate(long delayMillis) {
        guiThread.removeCallbacks(getSuggestionTask);
        guiThread.postDelayed(getSuggestionTask, delayMillis);
    }

    private void showSuggestDropDown(long delayMillis) {
        Runnable showSuggestDropDownTask = new Runnable() {
            public void run() {
                editText.showDropDown();
            }
        };
        guiThread.postDelayed(showSuggestDropDownTask, delayMillis);
    }

    public void setSuggestions(List<String> suggestions) {
        guiSetSuggestions(editText, suggestions);
    }

    private void guiSetSuggestions(final AutoCompleteTextView editText, final List<String> suggestions) {
        guiThread.post(new Runnable() {
            public void run() {
                adapter.clear();

                for (String listItem : suggestions) {
                    adapter.add(listItem);
                }

                adapter.getFilter().filter(editText.getText(), null);
            }
        });
    }
}
