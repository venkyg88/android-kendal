package com.staples.mobile.cfa.search;

/**
 * Author: Yongnan Zhou
 */

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
	private static final String TAG = "SuggestTask:";

	private final MainActivity suggestMain;
	private final String keyword;

	// Auto suggestions result from API call
	public static ArrayList<String> suggestionsList = new ArrayList<String>();
	public static String error = null;

	// The number of auto suggestion results
	private static final int SUGGESTION_AMOUNT = 20; 

	public SuggestTask(MainActivity context, String keyword) {
		this.suggestMain = context;
		this.keyword = keyword;
	}

	public void run() {
		List<String> suggestions = new ArrayList<String>();
		// Check if the keyword is longer than 3. The auto suggestion api only works for the string with at most length 3.
		try {
			if(keyword.length() <= 3 && keyword.length() > 0){
				// Get suggestions for the original text in a list
				suggestions = getSuggestions(keyword);
			}
			else if(suggestionsList.size() > 0){
				// Use previous suggestions (keyword's length=3) to find new suggestions (for keyword's length>3).
				ArrayList<String> newSuggestionsList = new ArrayList<String>();
				for(String previousSuggestion : suggestionsList){
					// only including suggestions that contain keyword
					if(previousSuggestion.contains(keyword)){
						newSuggestionsList.add(previousSuggestion);
					}
				}

				if (newSuggestionsList.size() == 0) {
					// Display "No Suggestions."
					// suggestions.add(suggestMain.getResources().getString(R.string.no_results));
				}
				else{
					// Display "KEYWORD MATCHES."
					suggestions = newSuggestionsList;
				}
				Log.d(TAG, "   -> Auto Suggestion After Matching Filter: " + newSuggestionsList);
			}
			else{
				//If no previous suggestions to use. Start a new suggestion.
				suggestions = getSuggestions(keyword.substring(0,3));
				// Log.d(TAG, "   -> getSuggestions(keyword.substring(0,3))");
			}

			// Send suggestions in the UI
			suggestMain.setSuggestions(suggestions);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
	}

	// Parse InputStream response content to String
	public void inputStreamToSuggestions(InputStream is) throws IOException {
		int counter = 0;
		String line = "";

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));

		suggestionsList.clear();
		// Read response until the end and check if the cookie (WCToken) is still valid
		while ((line = rd.readLine()) != null && counter < SUGGESTION_AMOUNT) {
			// remove "noresult" given from the sever and replace it with "No suggestsions"
			if(line.equals("noresult")){
				// suggestionsList.add(suggestMain.getResources().getString(R.string.no_results));
			}
			else{
				suggestionsList.add(line);
			}
			counter++;
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

			// Check if keyword with legal input. Only allow numeric and alphabets.
			// Use auto suggestion api only when the input is alphanumerical. Otherwise, exception occurs.
			// Use regular expression to check if the input is valid
			int keywordLength = keyword.length();
			if ((keywordLength == 1 && keyword.matches("[a-zA-Z0-9]{1}")) // alpha numerical character can only happens once
					|| (keywordLength == 2 && keyword.matches("[a-zA-Z0-9]{2}")) // ~ twice
					|| (keywordLength == 3 && keyword.matches("[a-zA-Z0-9]{3}"))){ // ~ three times [a-zA-Z0-9] equals to "\\w+"
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
			}
			else{ 
				// If the input doesn't match the format, display No suggestions.
				suggestionsList.clear();
				//suggestionsList.add(suggestMain.getResources().getString(R.string.no_results));
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

		// If there was an error, return the error by itself as result
		if (error != null) {
			suggestionsList.clear();
			suggestionsList.add(error);
		}

		// Print something to UI if we can't get any match suggestions from the server
		if (suggestionsList.size() == 0) {
			suggestionsList.add(suggestMain.getResources().getString(R.string.no_results));
		}

		Log.d(TAG, "   -> Auto Suggestion Response: " + suggestionsList);
		return suggestionsList;
	} 
}
