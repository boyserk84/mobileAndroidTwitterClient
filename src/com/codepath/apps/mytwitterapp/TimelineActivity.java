package com.codepath.apps.mytwitterapp;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.adapters.TweetsAdapter;
import com.codepath.apps.fragments.HomeTimelineFragment;
import com.codepath.apps.fragments.MentionsFragment;
import com.codepath.apps.interfaces.ResultDataAPIListener;
import com.codepath.apps.mytwitterapp.models.Tweet;
import com.codepath.apps.mytwitterapp.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * TimelineActivity 
 * 
 * Show timeline and tweets of the current session user
 * 
 * TODO:
 * 	- Issue with switching between tabs
 *  - Issue with Mention Fragment load more than needed
 * 
 * 
 * @author nkemavaha
 *
 */
public class TimelineActivity extends FragmentActivity implements TabListener, ResultDataAPIListener {
	
	public static final int COMPOSE_REQUEST_CODE = 101;
	public static final int COMPOSE_REQUEST_FAIL = -99;
	
	public static final String USER_DATA_KEY = "userData";
	
	public static final int INITIAL_TWEETS_TO_LOAD = 25;
	
	
	//////////////////////
	// Fragments
	//////////////////////
	
	/** HomeTimeline fragment */
	private HomeTimelineFragment homeTimelineFragment;
	
	/** Mentions fragment */
	private MentionsFragment mentionsFragment;
	
	///////////////////////
	/// Data fields
	//////////////////////	
	/**
	 * Current session user on this app
	 */
	private User currentSessionUser;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupCurrentSessionUserInfo();
		
		if ( savedInstanceState == null ) {
			homeTimelineFragment= new HomeTimelineFragment();
			mentionsFragment = new MentionsFragment();
		}
		
		setupNavigationTabs();
		
		
	}
	
	/**
	 * Setup navigation tabs and set fragment to each tab.
	 */
	private void setupNavigationTabs() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );
		actionBar.setDisplayShowTitleEnabled( true );
		Tab tabHome = actionBar.newTab().setText("HOME").setTag( "HomeTimelineFragment" ).setIcon(R.drawable.ic_home)
				.setTabListener( this );
		Tab tabMention = actionBar.newTab().setText("Mentions").setTag( "MentionsFragment" ).setIcon( R.drawable.ic_mention )
				.setTabListener( this );
		
		actionBar.addTab(tabHome);
		actionBar.addTab(tabMention);
		actionBar.selectTab( tabHome );
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
	 * Helper function to display a toast message
	 * @param msg		Message to display
	 */
	private void notifyOnToast(String msg) {
		Toast.makeText( this, msg, Toast.LENGTH_SHORT).show();	
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
		startActivityForResult( getIntentForComposeActivity() , COMPOSE_REQUEST_CODE);	
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
		// Check if there is any internet connection, before sending request
		if ( isNetworkAvailable() == false ) {
			notifyOnToast("Error: No Internet Connection");
			return;
		}
		
		if ( requestCode == COMPOSE_REQUEST_CODE ) {
			if ( resultCode == RESULT_OK ) {
				String message = data.getStringExtra("twitterMessage");
				
				if ( message.isEmpty() == false ) {
					
					JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
						
							@Override
							public void onSuccess(JSONObject update) {
								homeTimelineFragment.getAdapter().insert( Tweet.fromJson( update ), 0);
								homeTimelineFragment.getAdapter().notifyDataSetChanged();
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
	
    /**
     * Helper function to help detect if there is internet available
     * Reference & credits: http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
     * 
     * @return
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    //////////////////////////////////////
    /// OnTab Selected Listeners
    /////////////////////////////////////

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		FragmentManager manager = getSupportFragmentManager();
		
		// use appropriate transaction for backward compatibility
		android.support.v4.app.FragmentTransaction fts = manager.beginTransaction();
		
		if ( tab.getTag() == "HomeTimelineFragment") {
			// Set Framelayout container and replace it with HomeTimelineFragment
			fts.replace( R.id.frameContainer , homeTimelineFragment);
		} else {
			// Set Framelayout container and replace it with MentionsFragment
			fts.replace( R.id.frameContainer , mentionsFragment );
		}
		
		// commit and update changes to fragment
		fts.commit();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	////////////////////////////////////////////////
	/// Listeners - listening event from fragments
	////////////////////////////////////////////////
	
	// When we receive response event from fragment and we need to show this result to user
	@Override
	public void onResponseReceived(String message) {
		notifyOnToast( message );
	}

}
