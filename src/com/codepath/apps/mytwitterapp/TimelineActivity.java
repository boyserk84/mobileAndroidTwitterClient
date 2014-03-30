package com.codepath.apps.mytwitterapp;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.adapters.TweetsAdapter;
import com.codepath.apps.mytwitterapp.models.Tweet;
import com.codepath.apps.views.EndlessScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * TimelineActivity 
 * 
 * @author nkemavaha
 *
 */
public class TimelineActivity extends Activity {
	private static final int COMPOSE_REQUEST = 101;
	
	////////////////////////
	/// Views
	///////////////////////
	
	private ListView lvTweets; 
	
	private TweetsAdapter tweetsAdapter;
	
	/**
	 * List of TweetObject retrieved for Twitter API requests
	 */
	private ArrayList<Tweet> tweets;
	
	/**
	 * Keep track of last tweet Id -- using for request more tweets since tweetTd:XXXXXX
	 */
	private long lastTweetId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupViews();

		Log.d("DEBUG", "TimeLineActivity --- onCreated");
		// Fetch initial tweets data
		requestTweets( 25, lastTweetId);
	}
	
	/**
	 * Request tweets data from Twitter API
	 * @param count			Number of tweets we'd like to retrieved
	 * @param lastId		Which tweet Id (aka index) we fetch data from 
	 */
	private void requestTweets(int count, long lastId) {
		// Setup handle Rest Client response
		JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				tweets = Tweet.fromJson( jsonTweets );
				// Retrieve data and bind to adapter
				if ( tweetsAdapter == null ) {
					// First time
					tweetsAdapter = new TweetsAdapter( getBaseContext(), tweets);
					lvTweets.setAdapter( tweetsAdapter );
					Log.d("DEBUG", "============ init first time data");
				} else {
					
					// NOTE: Due to "max_id" request, which results in returning ID less than (that is, older than) or equal to the specified ID,
					//			we will always get a duplicate one (overlapped)
					// More info: https://dev.twitter.com/docs/api/1.1/get/statuses/home_timeline
					
					// Remove the duplicate tweet (the first one we retrieved) 
					Tweet firstDuplicateTweet = (tweets.size() > 0)? tweets.get( 0 ) : null;
					boolean hasTheSameTweetId = false;
					if ( firstDuplicateTweet != null ) {
						hasTheSameTweetId = (firstDuplicateTweet.getId() == lastTweetId);
						if ( hasTheSameTweetId == true) {
							Log.d("DEBUG"," remove duplicate " + firstDuplicateTweet.getId() + " last Id:" + lastTweetId + " with size " + tweets.size());
							tweets.remove( 0 );
							Log.d("DEBUG", "After remove size() is " + tweets.size() );
						}
					}
					
					// Either there are more tweets data OR there is no duplicate id.
					boolean hasNewTweets = (tweets.size() > 0 || hasTheSameTweetId == false);
					
					// Only update if new tweets are NOT overlapped.
					if ( hasNewTweets == true ) {
						notifyOnToast( tweets.size() + " more tweets ");
						// Subsequent
						tweetsAdapter.addAll( tweets );
						tweetsAdapter.notifyDataSetChanged();
					}
					Log.d("DEBUG", "Update data");
				}
				
				// Prevent Index Out Of Bound
				if ( tweets.size() > 0 ) {
					lastTweetId = tweets.get( tweets.size() -1 ).getId();
				}
				
				Log.d("DEBUG", "Getting Last Id on success::" + lastTweetId);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorObject) {				
				Log.d("DEBUG", "Request Failed due to " + errorObject.toString());
				notifyOnToast("Error: Rate limit exceeded!");
			}
		};
		
		// Prepare a request
		
		RequestParams request = new RequestParams("count", count);
		
		if ( lastId != -1 ) {
			//since_id
			request.put("max_id", Long.toString(lastId) );
		}
		
		// Call to MyTwitterApp singleton
		MyTwitterApp.getRestClient().getHomeTimeline( handler, request );	
		
	}
	
	private void notifyOnToast(String msg) {
		Toast.makeText( this, msg, Toast.LENGTH_SHORT).show();	
	}
	
	/** Setup views */
	private void setupViews() {
		lvTweets = (ListView) findViewById( R.id.lvTweets );	
		lvTweets.setOnScrollListener( new EndlessScrollListener() {
			
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				// TODO Auto-generated method stub
				Log.d("DEBUG","loading more " + page);
				requestTweets( 5 , lastTweetId);
			}
		});
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timeline, menu);
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		boolean result = super.onOptionsItemSelected(item);
		switch ( item.getItemId() ) {
			case R.id.miCompose:
				Log.d("DEBUG", "Compose message click!");
				Intent i = new Intent(getBaseContext(), ComposeActivity.class );
				
				// TODO: Find the way to get user data
				i.putExtra("userData", "test");
				startActivityForResult( i , COMPOSE_REQUEST);
				result = true;
				break;
				
			default:
				break;
		}
		
		return result;
		
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ( requestCode == COMPOSE_REQUEST ) {
			if ( resultCode == RESULT_OK ) {
				String message = data.getStringExtra("twitterMessage");
				
				if ( message.isEmpty() == false ) {
					
					JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
						
							@Override
							public void onSuccess(JSONObject update) {
								// Update view to reflect latest composed message.
								tweetsAdapter.insert( Tweet.fromJson( update ), 0 );
								tweetsAdapter.notifyDataSetChanged();
							}
							
							@Override
							public void onFailure(Throwable e, JSONObject errorObject) {
								Log.d("DEBUG", "Failed to post " + errorObject.toString() );
								
							}
					};
					
					MyTwitterApp.getRestClient().postStatusUpdate(handler, message);
				}
			}
		}
		
	}

}
