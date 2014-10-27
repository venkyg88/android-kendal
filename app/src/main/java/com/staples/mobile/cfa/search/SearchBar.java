package com.staples.mobile.cfa.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
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
    private View cancelButton;
    private Handler handler;
    private StartSuggest startSuggest;
    private SuggestTask suggestTask;
    private Future<?> suggestPending;
    private FinishSuggest finishSuggest;
    private NoFilterAdapter adapter;
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
        cancelButton=findViewById(R.id.search_cancel);
        View activate = findViewById(R.id.search_activate);

        // Set listeners
        editText.addTextChangedListener(this);
        editText.setOnEditorActionListener(this);
        editText.setOnItemClickListener(this);
        cancelButton.setOnClickListener(this);
        activate.setOnClickListener(this);

        closeSearchBar();

        adapter = new NoFilterAdapter(activity);
        editText.setAdapter(adapter);
        editText.setThreshold(0);

        recentKeywords = new ArrayList<String>();
        loadRecentKeywords();

        handler = new Handler();
        startSuggest = new StartSuggest();
        suggestTask = new SuggestTask(activity, this);
        finishSuggest = new FinishSuggest();
    }

    private static class NoFilterAdapter extends BaseAdapter implements ListAdapter, Filterable {
        private ArrayList<String> array;
        private LayoutInflater inflater;
        private Filter filter;

        private NoFilterAdapter(Context context) {
            array = new ArrayList<String>();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            filter = new NoFilter();
        }

        @Override
        public int getCount() {
            return(array.size());
        }

        @Override
        public String getItem(int position) {
            return(array.get(position));
        }

        @Override
        public long getItemId(int position) {
            return(position);
        }
        public void clear() {
            array.clear();
        }

        public void add(String string) {
            if (string==null) return;
            array.add(string);
        }

        public void addAll(List<String> strings) {
            if (strings==null) return;
            for(String string : strings)
                array.add(string);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.search_auto_suggestion_row, parent, false);
            }
            ((TextView) view).setText(array.get(position));
            return (view);
        }

        public Filter getFilter() {
            return (filter);
        }

        public class NoFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence arg0) {
                FilterResults result = new FilterResults();
                result.values = array;
                result.count = array.size();
                return result;
            }

            @Override
            protected void publishResults(CharSequence arg0, FilterResults arg1) {
                notifyDataSetChanged();
            }
        }
    }

    private void openSearchBar() {
        header.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

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
        cancelButton.setVisibility(View.GONE);

        editText.dismissDropDown();

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        open = false;
    }

    // Listeners

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.search_activate:
                if (!open) openSearchBar();
                else doSearch(null);
                break;
            case R.id.search_cancel:
                int n = editText.getText().length();
                if (n==0) closeSearchBar();
                else editText.setText(null);
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
        @Override
        public void run() {
            // Cancel previous suggestion
            if (suggestPending!=null) {
                suggestPending.cancel(true);
                suggestPending = null;
            }

            // If empty show past keywords
            String keyword = editText.getText().toString().trim();
            if (keyword.isEmpty() || keyword.equals(" ")) {
                adapter.clear();
                adapter.addAll(recentKeywords);
                adapter.notifyDataSetChanged();
                editText.showDropDown();
                return;
            }

            String key = SuggestTask.cleanKeyword(keyword);
            if (key==null) {
                adapter.clear();
                adapter.notifyDataSetChanged();
                return;
            }

            suggestTask.setKeyword(key);
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
            adapter.clear();
            adapter.addAll(suggestions);
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
        for(int i=0;i< recentKeywords.size();i++) {
            if (recentKeywords.get(i).equals(keyword)) {
                recentKeywords.remove(i);
                recentKeywords.add(0, keyword);
                return;
            }
        }

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
