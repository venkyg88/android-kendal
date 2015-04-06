package com.staples.mobile.cfa.search;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.suggest.api.SuggestApi;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchBarView extends LinearLayout implements View.OnClickListener, TextWatcher, TextView.OnEditorActionListener, AdapterView.OnItemClickListener, Callback<ArrayList<String>> {
    private static final String TAG = "SearchBarView";

    private static final int KEYDELAY = 250; // milliseconds

    private MainActivity activity;
    private ImageView searchOption;
    private SearchText searchText;

    private StartSuggest startSuggest;
    private SearchBarAdapter adapter;

    private String keyword;
    private boolean open;
    private boolean filled;

    public static class SearchText extends AutoCompleteTextView {
        public SearchText(Context context) {
            this(context, null, 0);
        }

        public SearchText(Context context, AttributeSet attrs) {
            this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
        }

        public SearchText(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        public boolean enoughToFilter() {
            return(true);
        }

        public void update(String keyword) {
            performFiltering(keyword, 0);
        }

        @Override
        protected void onMeasure(int widthSpec, int heightSpec) {
            super.onMeasure(widthSpec, heightSpec);
            setMeasuredDimension(MeasureSpec.getSize(widthSpec), getMeasuredHeight());
        }
    }

    public SearchBarView(Context context) {
        this(context, null, 0);
    }

    public SearchBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        activity = (MainActivity) context;

        LayoutInflater inflater = activity.getLayoutInflater();
        inflater.inflate(R.layout.search_bar, this, true);
        searchOption = (ImageView) findViewById(R.id.search_option);
        searchText = (SearchText) findViewById(R.id.search_text);

        searchOption.setImageResource(R.drawable.ic_search_black);
        searchOption.setOnClickListener(this);
        searchText.setVisibility(GONE);
        searchText.setHint(getHint());

        // Add adapter
        adapter = new SearchBarAdapter(activity, this);
        searchText.setAdapter(adapter);
        adapter.loadSearchHistory();

        // Set listeners
        searchText.addTextChangedListener(this);
        searchText.setOnEditorActionListener(this);
        searchText.setOnItemClickListener(this);

        // Tasks
        startSuggest = new StartSuggest();
    }

    private CharSequence getHint() {
        Resources res = getContext().getResources();
        SpannableStringBuilder sb = new SpannableStringBuilder("   ");
        sb.append(res.getString(R.string.search_hint));
        Drawable icon = res.getDrawable(R.drawable.ic_search_black);
        int size = (int) (searchText.getTextSize()*1.25);
        icon.setBounds(0, 0, size, size);
        sb.setSpan(new ImageSpan(icon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return(sb);
    }

    public boolean open() {
        if (open) return(false);
        searchOption.setVisibility(INVISIBLE);
        searchText.setVisibility(VISIBLE);
        searchText.setText(null);
        searchText.requestFocus();

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        open = true;
        filled = false;

        return(true);
    }

    private void setFilled(boolean filled) {
        if (filled==this.filled) return;
        this.filled = filled;
        if (filled) {
            searchOption.setImageResource(R.drawable.ic_close_black);
            searchOption.setVisibility(VISIBLE);
        } else {
            searchOption.setVisibility(INVISIBLE);
        }
    }

    public boolean close() {
        if (!open) return(false);
        searchOption.setImageResource(R.drawable.ic_search_black);
        searchOption.setVisibility(VISIBLE);
        searchText.setVisibility(GONE);

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);

        open = false;
        return(true);
    }

    public String getKeyword() {
        return(keyword);
    }

    // Listeners

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.search_option:
                if (open) {
                    searchText.setText(null);
                    setFilled(false);
                } else {
                    ActionBar.getInstance().openSearch();
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setFilled(s.length() > 0);
        keyword = s.toString().trim();

        removeCallbacks(startSuggest);
        if (open) postDelayed(startSuggest, KEYDELAY);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        switch(actionId) {
            case EditorInfo.IME_ACTION_SEARCH:
                doSearch(null);
                break;
            case EditorInfo.IME_NULL:
                if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER &&
                    event.getAction()==KeyEvent.ACTION_DOWN) {
                    doSearch(null);
                }
                break;
        }
        return(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        keyword = (String) parent.getItemAtPosition(position);
        doSearch(keyword);
    }

    public static String cleanKeyword(String keyword) {
        // trim, truncate & lowercase
        if (keyword==null) return(null);
        keyword = keyword.trim();
        int n = keyword.length();
        if (n==0) return(null);
        if (n>3) {
            keyword = keyword.substring(0, 3);
            n = 3;
        }
        keyword = keyword.toLowerCase();

        // check charset
        for(int i=0;i<n;i++) {
            char c = keyword.charAt(i);
            if (c<'0' || (c>'9' && c<'a') || c>'z') return(null);
        }
        return(keyword);
    }

    private class StartSuggest implements Runnable {
        private String lastKey;

        @Override
        public void run() {
            // If key didn't change just return
            String key = cleanKeyword(keyword);
            if ((lastKey==null && key==null) ||
                    (lastKey!=null && key!=null && lastKey.equals(key))) {
                return;
            }
            lastKey = key;
            if (key==null) return;

            SuggestApi suggestApi = Access.getInstance().getSuggestApi();
            suggestApi.getSuggestions(key, SearchBarView.this);
        }
    }

    @Override
    public void success(ArrayList<String> array, Response response) {
        adapter.setOriginal(array);
        searchText.update(keyword);
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        adapter.setOriginal(null);
        searchText.update(keyword);
    }

    private void doSearch(String keyword) {
        Tracker.getInstance().trackActionForSearch(keyword==null? Tracker.SearchType.BASIC_SEARCH : Tracker.SearchType.AUTOCOMPLETE); // analytics
        if (keyword==null) {
            keyword = searchText.getText().toString().trim();
        }
        if (keyword.isEmpty()) return;

        Toast.makeText(activity, "Searching " + keyword + "...", Toast.LENGTH_SHORT).show();
        adapter.pushRecentKeyword(keyword);
        activity.selectSearch(keyword);
    }

    public void saveSearchHistory() {
        if (adapter!=null)
            adapter.saveSearchHistory();
    }
}
