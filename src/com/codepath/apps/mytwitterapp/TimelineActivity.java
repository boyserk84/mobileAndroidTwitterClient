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
import com.codepath.apps.mytwitterapp.models.User;
import com.codepath.apps.views.EndlessScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * TimelineActivity 
 * 
 * Show timeline and tweets of the current session user
 * 
 * @author nkemavaha
 *
 */
public class TimelineActivity extends Activity {
	
	public static final int COMPOSE_REQUEST_CODE = 101;
	public static final int COMPOSE_REQUEST_FAIL = -99;
	
	public static final String USER_DATA_KEY = "userData";
	
	public static final int INITIAL_TWEETS_TO_LOAD = 25;
	public static final int TWEETS_TO_LOAD_WHEN_SCROLL = 10;
	
	////////////////////////
	/// Views
	///////////////////////
	
	private ListView lvTweets; 
	
	private TweetsAdapter tweetsAdapter;
	
	///////////////////////
	/// Data fields
	//////////////////////
	
	/**
	 * List of TweetObject retrieved for Twitter API requests
	 */
	private ArrayList<Tweet> tweets;
	
	/**
	 * Keep track of last tweet Id -- using for request more tweets since tweetTd:XXXXXX
	 */
	private long lastTweetId = -1;
	
	/**
	 * Current session user on this app
	 */
	private User currentSessionUser;

	/**
	 * Flag to indicate whether first chunk of tweets data has been successfully loaded
	 * before we can load more.
	 */
	private boolean isFirstDataLoaded = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupViews();
		
		// Fetch initial tweets data
		requestTweets( INITIAL_TWEETS_TO_LOAD, lastTweetId);
	}
	
	/**
	 * Helper function to request a current session User information from Twitter API.
	 */
	private void setupCurrentSessionUserInfo() {
		if ( currentSessionUser == null ) {
			JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONObject jsonObject ) {
					currentSessionUser = User.fromJson( jsonObject );
					startActivityForResult( getIntentForComposeActivity() , COMPOSE_REQUEST_CODE);	
				}

				@Override
				public void onFailure(Throwable e, JSONObject errorObject) {				
					showLog( "Failure to get current session user info! " + errorObject.toString() );
				}	
			};

			MyTwitterApp.getRestClient().getCurrentUserVerifiedCredentials(handler);
		}
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
					isFirstDataLoaded = true;
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
							showLog( "remove duplicate " + firstDuplicateTweet.getId() + " last Id:" + lastTweetId + " with size " + tweets.size() );
							tweets.remove( 0 );
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
				}
				
				// Prevent Index Out Of Bound
				if ( tweets.size() > 0 ) {
					lastTweetId = tweets.get( tweets.size() -1 ).getId();
				}
				
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorObject) {				
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
	
	/**
	 * Helper function to display a toast message
	 * @param msg		Message to display
	 */
	private void notifyOnToast(String msg) {
		Toast.makeText( this, msg, Toast.LENGTH_SHORT).show();	
	}
	
	/** Setup views */
	private void setupViews() {
		lvTweets = (ListView) findViewById( R.id.lvTweets );	
		
		// Setup endless scrolling
		lvTweets.setOnScrollListener( new EndlessScrollListener() {
			
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				
				if ( isFirstDataLoaded == true ) {
					showLog( "loading more " + page + ": Total Items Count:" + totalItemsCount);
					requestTweets( TWEETS_TO_LOAD_WHEN_SCROLL , lastTweetId);
				}
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
				openComposeActivity();
				result = true;
				break;
				
			default:
				break;
		}
		
		return result;
		
	}
	
	/**
	 * Helper function to handle open a compose activity.
	 * 
	 */
	private void openComposeActivity() {
		// If currentSessionUser has already been loaded and temporarily stored
		if ( currentSessionUser != null ) {
			startActivityForResult( getIntentForComposeActivity() , COMPOSE_REQUEST_CODE);	
		} else {
			// Otherwise, send a request for user information first
			setupCurrentSessionUserInfo();
		}
	}
	
	/**
	 * Helper function to get Intent object specifically for ComposeActivity
	 * @return Intent Object with current session user object.
	 */
	private Intent getIntentForComposeActivity() {
		Intent i = new Intent(getBaseContext(), ComposeActivity.class );
		i.putExtra( USER_DATA_KEY, currentSessionUser);	
		return i;
	}
	
	/** Callback when compose activity is returned. */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ( requestCode == COMPOSE_REQUEST_CODE ) {
			if ( resultCode == RESULT_OK ) {
				String message = data.getStringExtra("twitterMessage");
				
				if ( message.isEmpty() == false ) {
					
					JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
						
							@Override
							public void onSuccess(JSONObject update) {
								// Update view to reflect latest composed message.
								tweetsAdapter.insert( Tweet.fromJson( update ), 0 );
								tweetsAdapter.notifyDataSetChanged();
								notifyOnToast( "Your status has been updated!");
							}
							
							@Override
							public void onFailure(Throwable e, JSONObject errorObject) {
								showLog( "Failed to post " + errorObject.toString() );
								
							}
					};
					
					MyTwitterApp.getRestClient().postStatusUpdate(handler, message);
				} else {
					notifyOnToast("Your message is blank!");	
				}
				// handle failed message
			} else if ( resultCode == COMPOSE_REQUEST_FAIL ){
				notifyOnToast("Your message characters exceeds limit!");
			}
		}
		
	}
	
	//////////////////////////////
	/// Debug Helper functions
	/////////////////////////////

	/** Flag indicate whether we should show a debug log info. */
	private boolean isOnProduction = false;
	
	/**
	 * Helper function for showing debug log so that we can toggle on/off
	 * @param message
	 */
	private void showLog( String message ) {
		if ( isOnProduction == false ) {
			Log.d("DEBUG", message);
		}
	}

}
