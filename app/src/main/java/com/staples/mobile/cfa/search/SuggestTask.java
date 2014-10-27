package com.staples.mobile.cfa.search;

import android.util.Log;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;

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
import java.util.List;

public class SuggestTask implements Runnable {
	private static final String TAG = "SuggestTask";

	private final MainActivity suggestMain;
    private final SearchBar searchBar;
	private String keyword;

	// Auto suggestions result from API call
	private ArrayList<String> suggestionList;
	private String error;

	// The number of auto suggestion results
	private static final int SUGGESTION_AMOUNT = 20; 

	public SuggestTask(MainActivity context, SearchBar searchBar) {
		this.suggestMain = context;
        this.searchBar = searchBar;

        suggestionList = new ArrayList<String>();
	}

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

	public void run() {
		List<String> suggestions;
		try {
			suggestions = getSuggestions(keyword);
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
        if (n==0 || n>3) return(null);
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
		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));

		suggestionList.clear();
        for(int count=0;count<SUGGESTION_AMOUNT;) {
            String line = rd.readLine();
            if (line==null) break;
            if (!line.equals("noresult")) {
                suggestionList.add(line);
                count++;
            }
        }
	}

	private List<String> getSuggestions(String keyword) throws IOException{
		InputStream inputStream = null;
		Log.d(TAG, "Running getSuggestions(" + keyword + ")");
		try {
			// clear error message if there are any in the auto suggestion list
			error = null;

			// Check if task has been interrupted
			if (Thread.interrupted()){
				throw new InterruptedException();
			}

            // Build RESTful query and Create a GET Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpSuggestGet = new HttpGet("http://www.staples.com/sbd/content/mainautocomplete/files/"+keyword+".txt");

            // Execute HTTP GET Request
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

		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
			error = suggestMain.getResources().getString(R.string.error) + " " + e.toString();
		} catch (InterruptedException e) {
			Log.e(TAG, "InterruptedException", e);
			error = suggestMain.getResources().getString(R.string.interrupted) + " " + e.toString();
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
			error = suggestMain.getResources().getString(R.string.error) + " " + e.toString();
		} finally {
			// clean up resources
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return suggestionList;
	} 
}
