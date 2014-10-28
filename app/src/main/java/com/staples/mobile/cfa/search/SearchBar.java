package com.staples.mobile.cfa.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class SearchBar extends LinearLayout implements View.OnClickListener, TextWatcher, TextView.OnEditorActionListener, AdapterView.OnItemClickListener {
    private static final String TAG = "SearchBar";

    private static final String RECENTKEYWORDSTAG = "recent_keywords";
    private static final int MAXRECENTKEYWORDS = 10;
    private static final int KEYDELAY = 250; // milliseconds

    private MainActivity activity;
    private View header;
    private AutoCompleteTextView editText;
    private ImageView icon;
    private Handler handler;
    private StartSuggest startSuggest;
    private SuggestTask suggestTask;
    private Future<?> suggestPending;
    private FinishSuggest finishSuggest;
    private HighlightAdapter adapter;
    private ArrayList<String> recentKeywords;
    private boolean open;

    public SearchBar(Context context) {
        this(context, null, 0);
    }

    public SearchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.activity = (MainActivity) context;
    }

    public void initSearchBar(){
        // Find elements
        header = findViewById(R.id.header);
        editText = (AutoCompleteTextView) findViewById(R.id.search_text);
        icon = (ImageView) findViewById(R.id.search_icon);

        // Set listeners
        editText.addTextChangedListener(this);
        editText.setOnEditorActionListener(this);
        editText.setOnItemClickListener(this);
        icon.setOnClickListener(this);

        closeSearchBar();

        adapter = new HighlightAdapter(activity);
        editText.setAdapter(adapter);
        editText.setThreshold(1);

        recentKeywords = new ArrayList<String>();
        loadRecentKeywords();

        // Tasks
        handler = new Handler();
        startSuggest = new StartSuggest();
        suggestTask = new SuggestTask(activity, this);
        finishSuggest = new FinishSuggest();
    }

    private static class HighlightAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        private Filter filter;
        private String keyword;

        public HighlightAdapter(Context context) {
            super(context, 0);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            filter = new InclusiveFilter();
add("apples");
add("bananas");
add("cantaloupes");

        }

        private void setHighlightedText(TextView view, String text) {
            if (text==null || keyword==null || keyword.isEmpty()) {
                view.setText(text);
                return;
            }

            SpannableStringBuilder sb = new SpannableStringBuilder(text);
            int n = text.length();
            int k = keyword.length();
            for(int i=0;i<n;) {
                int j = text.indexOf(keyword, i);
                if (j<0) j = n;
                if (j>i) sb.setSpan(new StyleSpan(Typeface.BOLD), i, j, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                i = j+k;
            }
            view.setText(sb);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.search_suggest_item, parent, false);
            }
            setHighlightedText((TextView) view, getItem(position));
            return (view);
        }

        @Override
        public Filter getFilter() {
            return(filter);
        }

        public class InclusiveFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint==null) keyword = "";
                else keyword = constraint.toString();

Log.d(TAG, "Filter " + keyword);
                ArrayList<String> array = new ArrayList<String>();
                int n = getCount();
                for(int i=0;i<n;i++) {
                    String item = getItem(i);
                    if (item!=null && item.indexOf(keyword)>=0)
                        array.add(item);
                }

                FilterResults result = new FilterResults();
                result.values = array;
                result.count = array.size();
                return(result);
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
Log.d(TAG, "Publish "+results.count);
                if (results.count>0)
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
            }
        }
    }

    private void openSearchBar() {
        header.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);

        icon.setImageResource(R.drawable.ic_action_cancel);

        editText.requestFocus();
        editText.setText(null);
        editText.showDropDown();

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        open = true;
    }

    private void closeSearchBar() {
        header.setVisibility(View.VISIBLE);
        editText.setVisibility(View.GONE);

        icon.setImageResource(R.drawable.ic_search);

//        editText.dismissDropDown();

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        open = false;
    }

    // Listeners

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.search_icon:
                if (open) {
                    int n = editText.getText().length();
                    if (n == 0) editText.showDropDown(); //closeSearchBar();
                    else editText.setText(null);
                } else openSearchBar();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        handler.removeCallbacks(startSuggest);
        if (open) handler.postDelayed(startSuggest, KEYDELAY);
    }

    @Override
    public void afterTextChanged(Editable s) {
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
        String keyword = (String) parent.getItemAtPosition(position);
        doSearch(keyword);
    }

    public class StartSuggest implements Runnable {
        String lastKey;

        @Override
        public void run() {
            // Get keyword and key
            String keyword = editText.getText().toString().trim();
            String key = SuggestTask.cleanKeyword(keyword);

            // If key didn't change just return
            if ((lastKey==null && key==null) ||
                (lastKey!=null && key!=null && lastKey.equals(key)))
                return;
            lastKey = key;

            // Cancel previous suggestion
            if (suggestPending!=null) {
                suggestPending.cancel(true);
                suggestPending = null;
            }

            // No suggestion possible
            if (key==null) {
//                adapter.clear();
//                adapter.addAll(recentKeywords);
                adapter.notifyDataSetChanged();
                editText.showDropDown();
                return;
            }

            // Run query
            suggestTask.setKey(key);
            ExecutorService thread = Executors.newSingleThreadExecutor();
            try {
                suggestPending = thread.submit(suggestTask);
            } catch (RejectedExecutionException e) {}
        }
    }

    public class FinishSuggest implements Runnable {
        private List<String> suggestions;

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions;
        }

        @Override
        public void run() {
//            adapter.clear();
//            adapter.addAll(suggestions);
            adapter.notifyDataSetChanged();
            editText.showDropDown();
        }
    }

    private void doSearch(String keyword) {
        if (keyword==null) {
            keyword = editText.getText().toString().trim();
        }
        if (keyword.isEmpty()) return;

        closeSearchBar();
        Toast.makeText(activity, "Searching " + keyword + "...", Toast.LENGTH_SHORT).show();
        pushRecentKeyword(keyword);
        activity.selectSearch(keyword);
    }

    public void callback(List<String> suggestions) {
        finishSuggest.setSuggestions(suggestions);
        handler.post(finishSuggest);
    }

    // Recently used keywords

    private void pushRecentKeyword(String keyword) {
        if (keyword==null || keyword.isEmpty()) return;

        // check if keyword is present
        recentKeywords.remove(keyword);

        // truncate to one smaller than the limit
        for(int i = recentKeywords.size();i>=MAXRECENTKEYWORDS;i--)
            recentKeywords.remove(i-1);

        // add at head
        recentKeywords.add(0, keyword);
    }

    private void loadRecentKeywords() {
        // Get chunk
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        String chunk = prefs.getString(RECENTKEYWORDSTAG, null);
        if (chunk==null) return;

        // Explode
        String[] keywords = chunk.split("\\|");
        for (String keyword : keywords)
            recentKeywords.add(keyword);
    }

    public void saveRecentKeywords() {
        // Build string
        StringBuilder chunk = new StringBuilder();
        boolean flag = false;
        for(String keyword : recentKeywords) {
            if (flag) chunk.append("|");
            chunk.append(keyword);
            flag = true;
        }

        // Save in preferences
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(RECENTKEYWORDSTAG, chunk.toString());
        editor.commit();
    }
}
