package com.codepath.apps.mytwitterapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mytwitterapp.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Compose Activity Class
 * 
 * Compose a new Tweet message.
 * 
 * @author nkemavaha
 *
 */
public class ComposeActivity extends Activity {

	private static final int TOTAL_STRING_LENGTH = 140;
	
	/////////////////////////////
	///		UI elements
	/////////////////////////////
	
	private EditText etNewMessage;
	
	private TextView tvScreenName;
	
	private TextView tvFullName;
	
	private TextView tvShowTextLimit;
	
	private ImageView ivProfileImage;
	
	////////////////////////////
	/// Data fields
	///////////////////////////
	
	/** Current user who will be posting a tweet. */
	User tweetUserData;
	
	/** Keep track of characters limit */
	private int remainingChar;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
		remainingChar = TOTAL_STRING_LENGTH;
		
		setupViews();
		setupTextChangedListener();
		
		
		// Retrieve data from previous activity
		Intent i = getIntent();
		tweetUserData = (User) i.getSerializableExtra("userData");
		
		// Populate data
		tvScreenName.setText( "@" + tweetUserData.getScreenName()  );
		tvFullName.setText( tweetUserData.getName() );
		
		// Load image
		ImageLoader.getInstance().displayImage(tweetUserData.getProfileImage(), ivProfileImage);
		
		
	}
	
	/** Setup views */
	private void setupViews() {
		tvScreenName = (TextView) findViewById( R.id.tvTwitterTag );
		tvFullName = (TextView) findViewById( R.id.tvTwitterName );
		etNewMessage = (EditText) findViewById( R.id.etTwitterMessage );	
		tvShowTextLimit = (TextView) findViewById( R.id.tvCharsLeft );
		ivProfileImage = (ImageView) findViewById( R.id.ivProfileImage );
	}
	
	/** Setup text changed listener for keeping track of remaining available characters. */
	private void setupTextChangedListener() {
		tvShowTextLimit.setText( Integer.toString( TOTAL_STRING_LENGTH ) );
		
		// Setup listener for text changes so we can keep track of remaining characters space
		etNewMessage.addTextChangedListener( new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				remainingChar = TOTAL_STRING_LENGTH - etNewMessage.getText().length();
				
				// Show remaining text
				if ( remainingChar >= 0 ) {
					// Show regular remaining text
					tvShowTextLimit.setText( Integer.toString( remainingChar ) );
					tvShowTextLimit.setTextColor( Color.BLACK );
				} else {
					// Show a warning of exceeding characters limit 
					tvShowTextLimit.setText( "Exceed chararacters limit by " + Integer.toString( remainingChar * -1) );
					tvShowTextLimit.setTextColor( Color.RED );
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.compose, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		boolean result = super.onOptionsItemSelected(item); 
		switch ( item.getItemId() ) {
			case R.id.miComposeConfirm:
				result = true;
				// Return back data 
				Intent i = new Intent();
				String message = etNewMessage.getText().toString();
				i.putExtra("twitterMessage", message);
				
				// Checking if user's message conforms to characters limit rule.
				if ( remainingChar >= 0 ) {
					setResult( RESULT_OK, i);
				} else {
					// sending failed result code
					setResult( TimelineActivity.COMPOSE_REQUEST_FAIL, i );
				}
				this.finish();
		}
		return result;
	}

}
