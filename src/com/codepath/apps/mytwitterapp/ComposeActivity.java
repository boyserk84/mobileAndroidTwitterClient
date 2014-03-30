package com.codepath.apps.mytwitterapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.mytwitterapp.models.User;

/**
 * Compose Activity Class
 * 
 * Compose a new Tweet message.
 * 
 * @author nkemavaha
 *
 */
public class ComposeActivity extends Activity {

	EditText etNewMessage;
	
	TextView tvScreenName;
	
	TextView tvFullName;
	
	User tweetUserData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
		setupViews();
		
		// Retrieve data from previous activity
		//Intent i = getIntent();
		//tweetUserData = (User) i.getSerializableExtra("userData");
		
		//tvScreenName.setText( tweetUserData.getScreenName() );
		//tvFullName.setText( tweetUserData.getName() );
		
		
	}
	
	/** Setup views */
	private void setupViews() {
		tvScreenName = (TextView) findViewById( R.id.tvTwitterTag );
		tvFullName = (TextView) findViewById( R.id.tvTwitterName );
		etNewMessage = (EditText) findViewById( R.id.etTwitterMessage );	
		
		etNewMessage.addTextChangedListener( new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {		
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
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
				setResult( RESULT_OK, i);
				this.finish();
		}
		return result;
	}

}
