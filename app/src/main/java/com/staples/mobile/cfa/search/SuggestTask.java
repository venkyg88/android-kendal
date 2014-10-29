package com.staples.mobile.cfa.search;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SuggestTask implements Runnable {
	private static final String TAG = "SuggestTask";

    private static final String SUGGESTSERVER = "http://www.staples.com/sbd/content/mainautocomplete/files/";
    private static final int MAXFETCH = 50;

    private SearchBarView searchBar;
	private String key;

	// Auto suggestions result from API call
	private ArrayList<String> suggestionList;
	private String error;

	// The number of auto suggestion results

	public SuggestTask(SearchBarView searchBar) {
        this.searchBar = searchBar;

        suggestionList = new ArrayList<String>();
	}

    public void setKey(String key) {
        this.key = key;
    }

	public void run() {
		ArrayList<String> suggestions;
		try {
			suggestions = getSuggestions(key);
            searchBar.callback(suggestions);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
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

	// Parse InputStream response content to String
	public void inputStreamToSuggestions(InputStream is) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        int count;

		suggestionList.clear();
        for(count=0;count<MAXFETCH;) {
            String line = rd.readLine();
            if (line==null) break;
            if (!line.equals("noresult")) {
                suggestionList.add(line);
                count++;
            }
        }
        Log.d(TAG, count+" suggestions");
	}

	private ArrayList<String> getSuggestions(String key) throws IOException{
		InputStream inputStream = null;
		Log.d(TAG, "Running getSuggestions(" + key + ")");
		try {
			// clear error message if there are any in the auto suggestion list
			error = null;

			// Check if task has been interrupted
			if (Thread.interrupted()){
				throw new InterruptedException();
			}

            // Query server
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpSuggestGet = new HttpGet(SUGGESTSERVER+key+".txt");
            HttpResponse httpResponse = httpclient.execute(httpSuggestGet);

            // Check if task has been interrupted. If it is, that means there is no result returned.
            if (Thread.interrupted()){
                throw new InterruptedException();
            }

            // Check if we get the correct response from http status code
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                // Get HTTP Response's Content, Read results from the query
                inputStream = httpResponse.getEntity().getContent();
                inputStreamToSuggestions(inputStream);
            }
            else
            {
                Log.d(TAG, "Incorrect HTTP status code: " + httpResponse.getStatusLine().getStatusCode());
            }
			// Check if the task has been interrupted
			if (Thread.interrupted()){
				throw new InterruptedException();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
			error = e.toString();
		} finally {
			// clean up resources
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return suggestionList;
	} 
}
