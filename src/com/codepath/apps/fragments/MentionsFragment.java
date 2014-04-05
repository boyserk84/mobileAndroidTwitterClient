package com.codepath.apps.fragments;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;

import com.codepath.apps.mytwitterapp.MyTwitterApp;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * MentionsFragment
 * 
 * Showing tweets message of this user being mentioned
 * @author nkemavaha
 *
 */
public class MentionsFragment extends TweetsListFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void requestTwitterData(int count, long lastId) {
		// Setup handle Rest Client response
		JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				processTweetsData( jsonTweets );
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorObject) {
				processFailureResponse( errorObject );
			}
		};
		
		// Prepare a request
		RequestParams request = new RequestParams("count", count);

		boolean isSubsequentLoad = (lastId != -1);
		
		if ( isSubsequentLoad ) {
			//since_id
			request.put("max_id", Long.toString(lastId) );
			// save the id
			lastTweetId = lastId;
		}
		
		MyTwitterApp.getRestClient().getMentionsTimeline( handler, request );		
	}
}
