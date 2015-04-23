package com.staples.mobile.cfa.search;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;

import java.util.ArrayList;

public class SearchBarAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = SearchBarAdapter.class.getSimpleName();

    private static final String PREFS_HISTORY = "searchHistory";
    private static final int MAXHISTORY = 10;
    private static final boolean INVERTHIGHLIGHT = true;

    private Activity activity;
    private SearchBarView searchBar;
    private ArrayList<String> original;
    private ArrayList<String> history;
    private ArrayList<String> active;
    private LayoutInflater inflater;
    private Filter filter;

    public SearchBarAdapter(Activity activity, SearchBarView searchBar) {
        super();
        this.activity = activity;
        this.searchBar = searchBar;

        original = new ArrayList<String>();
        history = new ArrayList<String>();
        inflater = activity.getLayoutInflater();
        filter = new InclusiveFilter();
    }

    @Override
    public int getCount() {
        if (active==null) return(0);
        return(active.size());
    }

    @Override
    public String getItem(int position) {
        return(active.get(position));
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    public void setOriginal(ArrayList<String> original) {
        this.original = original;
    }

    /**
     * setHighlightedText is controlled by INVERTHIGHLIGHT
     * false -> highlight matched text
     * true -> highlight non-matched text
     */
    private void setHighlightedText(TextView view, String text) {
        String keyword = searchBar.getKeyword();
        if (text==null || keyword==null || keyword.isEmpty()) {
            view.setText(text);
            return;
        }

        // Divide string into matching & non-matching spans and style
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        int textLen = text.length();
        int keyLen = keyword.length();
        int i, j, k;
        for(i=0;i<textLen;i=k) {
            j = text.indexOf(keyword, i);
            if (j<0) j = textLen;
            if (INVERTHIGHLIGHT && i<j)
                sb.setSpan(new StyleSpan(Typeface.BOLD), i, j, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            if (j>=textLen) break;
            for(k=j+keyLen;k<textLen;k+=keyLen)
                if (!text.startsWith(keyword, k)) break;
            if (!INVERTHIGHLIGHT && j<k)
                sb.setSpan(new StyleSpan(Typeface.BOLD), j, k, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        view.setText(sb);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.search_suggest_item, parent, false);
        }
        TextView text = (TextView) view.findViewById(R.id.text);
        setHighlightedText(text, getItem(position));
        return (view);
    }

    @Override
    public Filter getFilter() {
        return(filter);
    }

    public class InclusiveFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            // No constraint means show history
            if (constraint==null || constraint.length()==0) {
                results.values = history;
                results.count = history.size();
                return(results);
            }

            String span = constraint.toString();
            ArrayList<String> array = new ArrayList<String>();
            if (original!=null) {
                int n = original.size();
                for(int i=0;i<n;i++) {
                    String item = original.get(i);
                    if (item!=null && item.contains(span))
                        array.add(item);
                }
            }

            results.values = array;
            results.count = array.size();
            return(results);
        }

        @Override @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            active = (ArrayList<String>) results.values;
            if (results.count>0)
                notifyDataSetChanged();
            else
                notifyDataSetInvalidated();
        }
    }

    // Search history

    public void pushRecentKeyword(String keyword) {
        if (keyword==null || keyword.isEmpty()) return;

        // check if keyword is present
        history.remove(keyword);

        // truncate to one smaller than the limit
        for(int i = history.size();i>=MAXHISTORY;i--)
            history.remove(i-1);

        // add at head
        history.add(0, keyword);
    }

    public void loadSearchHistory() {
        // Get chunk
        SharedPreferences prefs = activity.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
        String chunk = prefs.getString(PREFS_HISTORY, null);
        if (chunk==null || chunk.isEmpty()) return;

        // Explode
        String[] keywords = chunk.split("\\|");
        for(String keyword : keywords)
            history.add(keyword);
        notifyDataSetChanged();
    }

    public void saveSearchHistory() {
        // Build string
        StringBuilder chunk = new StringBuilder();
        boolean flag = false;
        for(String keyword : history) {
            if (flag) chunk.append("|");
            chunk.append(keyword);
            flag = true;
        }

        // Save in preferences
        SharedPreferences prefs = activity.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_HISTORY, chunk.toString());
        editor.apply();
    }
}
