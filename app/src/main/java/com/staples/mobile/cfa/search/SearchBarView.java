package com.staples.mobile.cfa.search;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.R;
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
        activity.showActionBar(0, R.drawable.ic_search_white, this);

        // Set listeners
        addTextChangedListener(this);
        setOnEditorActionListener(this);
        setOnItemClickListener(this);

        adapter = new SearchBarAdapter(activity, this);
        setAdapter(adapter);
        adapter.loadSearchHistory();

        // Tasks
        startSuggest = new StartSuggest();
        suggestTask = new SuggestTask(this);
        finishSuggest = new FinishSuggest();

        performFiltering(keyword, 0);
    }

    private void openSearchBar() {
        setVisibility(View.VISIBLE);
        activity.showActionBar(0, R.drawable.ic_cancel_white, this);

        setText(null);
        requestFocus();
        showDropDown();

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        open = true;
    }

    private void closeSearchBar() {
        setVisibility(View.GONE);
        activity.showActionBar(R.string.staples, R.drawable.ic_search_white, this);

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);

        open = false;
    }

    public String getKeyword() {
        return(keyword);
    }

    @Override
    public boolean enoughToFilter() {
        return(true);
    }

    // Listeners

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.option_icon:
                if (open) {
                    if (getText().length()==0) closeSearchBar();
                    else  setText(null);
                } else openSearchBar();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        keyword = s.toString().trim();

        removeCallbacks(startSuggest);
        if (open) postDelayed(startSuggest, KEYDELAY);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            doSearch(null);
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
            // If key didn't change just return
            String key = SuggestTask.cleanKeyword(keyword);
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
        post(finishSuggest);
    }

    public void saveSearchHistory() {
        if (adapter!=null)
            adapter.saveSearchHistory();
    }
}
