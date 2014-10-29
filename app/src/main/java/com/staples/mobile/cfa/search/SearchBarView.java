package com.staples.mobile.cfa.search;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class SearchBarView extends AutoCompleteTextView implements View.OnClickListener, TextWatcher, TextView.OnEditorActionListener, AdapterView.OnItemClickListener {
    private static final String TAG = "SearchBarView";

    private static final int KEYDELAY = 250; // milliseconds

    private MainActivity activity;
    private View header;
    private ImageView icon;
    private Handler handler;
    private StartSuggest startSuggest;
    private SuggestTask suggestTask;
    private Future<?> suggestPending;
    private FinishSuggest finishSuggest;
    private SearchBarAdapter adapter;
    private String keyword;
    private boolean open;

    public SearchBarView(Context context) {
        this(context, null, 0);
    }

    public SearchBarView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
    }

    public SearchBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.activity = (MainActivity) context;
    }

    public void initSearchBar(){
        // Find elements by going up a level then down
        View parent = (View) getParent();
        header = parent.findViewById(R.id.header);
        icon = (ImageView) parent.findViewById(R.id.search_icon);

        // Set listeners
        addTextChangedListener(this);
        setOnEditorActionListener(this);
        setOnItemClickListener(this);
        icon.setOnClickListener(this);

        adapter = new SearchBarAdapter(activity);
        setAdapter(adapter);
        adapter.loadSearchHistory();

        // Tasks
        handler = new Handler();
        startSuggest = new StartSuggest();
        suggestTask = new SuggestTask(this);
        finishSuggest = new FinishSuggest();

        performFiltering(keyword, 0);
    }

    private void openSearchBar() {
        header.setVisibility(View.GONE);
        setVisibility(View.VISIBLE);

        icon.setImageResource(R.drawable.ic_action_cancel);

        setText(null);
        requestFocus();
        showDropDown();

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        open = true;
    }

    private void closeSearchBar() {
        header.setVisibility(View.VISIBLE);
        setVisibility(View.GONE);

        icon.setImageResource(R.drawable.ic_search);

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);

        open = false;
    }

    @Override
    public boolean enoughToFilter() {
        return(true);
    }

    // Listeners

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.search_icon:
                if (open) {
                    int n = getText().length();
                    if (n == 0) closeSearchBar();
                    else setText(null);
                } else openSearchBar();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (handler==null) return;
        handler.removeCallbacks(startSuggest);
        if (open) handler.postDelayed(startSuggest, KEYDELAY);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            doSearch(null);
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        keyword = (String) parent.getItemAtPosition(position);
        doSearch(keyword);
    }

    private class StartSuggest implements Runnable {
        private String lastKey;

        @Override
        public void run() {
            // Get keyword and key
            keyword = getText().toString().trim();
            String key = SuggestTask.cleanKeyword(keyword);

            // If key didn't change just return
            if ((lastKey==null && key==null) ||
                    (lastKey!=null && key!=null && lastKey.equals(key))) {
                performFiltering(keyword, 0);
                return;
            }
            lastKey = key;

            // Cancel previous suggestion
            if (suggestPending!=null) {
                suggestPending.cancel(true);
                suggestPending = null;
            }

            // No suggestion possible
            if (key==null) {
                performFiltering(keyword, 0);
                return;
            }

            // Run query
            suggestTask.setKey(key);
            ExecutorService thread = Executors.newSingleThreadExecutor();
            try {
                suggestPending = thread.submit(suggestTask);
            } catch(RejectedExecutionException e) {}
        }
    }

    private class FinishSuggest implements Runnable {
        private ArrayList<String> suggestions;

        public void setSuggestions(ArrayList<String> suggestions) {
            this.suggestions = suggestions;
        }

        @Override
        public void run() {
            adapter.setOriginal(suggestions);
            adapter.notifyDataSetChanged();
            performFiltering(keyword, 0);
        }
    }

    private void doSearch(String keyword) {
        if (keyword==null) {
            keyword = getText().toString().trim();
        }
        if (keyword.isEmpty()) return;

        closeSearchBar();
        Toast.makeText(activity, "Searching " + keyword + "...", Toast.LENGTH_SHORT).show();
        adapter.pushRecentKeyword(keyword);
        activity.selectSearch(keyword);
    }

    public void callback(ArrayList<String> suggestions) {
        finishSuggest.setSuggestions(suggestions);
        handler.post(finishSuggest);
    }

    public void saveSearchHistory() {
        if (adapter!=null)
            adapter.saveSearchHistory();
    }
}
