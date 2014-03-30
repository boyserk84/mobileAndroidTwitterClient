package com.codepath.apps.mytwitterapp.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codepath.apps.mytwitterapp.MyTwitterClient;

import android.content.Context;
import android.text.format.DateUtils;

/**
 * Tweet Class
 * Strongly-typed data class of Tweet JSON object.
 * @author nkemavaha
 *
 */
public class Tweet {
	private String body;
	private long uid;
	private boolean favorited;
	private boolean retweeted;
    private User user;
    private String timeStamp;

    public User getUser() {
        return user;
    }

    public String getBody() {
        return body;
    }

    public long getId() {
        return uid;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }
    
    public String getTimeStamp() {
    	return timeStamp;
    }
    
    /**
     * Helper function to convert raw timestamp to relative timestamp
     * (i.e. 2 minutes ago, 3 weeks ago)
     * Reference & credits: http://stackoverflow.com/questions/7082518/android-getrelativetime-example
     * http://stackoverflow.com/questions/2009207/java-unparseable-date-exception
     * @param context		Android Context
     * @return Relative timestamp in string. Otherwise, null is returned.
     */
    public String getRelativeTimeStamp(Context context ) {
    	Date date = null;
    	String str = null;
		try {
			// Format from Twitter EEE MMM d HH:mm:ss Z yyyy
			date = new SimpleDateFormat( MyTwitterClient.TWITTER_DATETIME_FORMAT, Locale.US ).parse( timeStamp );
	    	str = (String) DateUtils.getRelativeDateTimeString(
	     			context, 
	     			date.getTime() , 
	     			DateUtils.SECOND_IN_MILLIS, 
	     			DateUtils.WEEK_IN_MILLIS , 
	     			0);
		} catch (ParseException e) {
			e.printStackTrace();
		}

    	return str;
    	
    }

    /**
     * Convert JSONObject to Tweet data object
     * @param jsonObject
     * @return
     */
    public static Tweet fromJson(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
        	tweet.body = jsonObject.getString("text");
        	tweet.uid = jsonObject.getLong("id");
        	tweet.favorited = jsonObject.getBoolean("favorited");
        	tweet.retweeted = jsonObject.getBoolean("retweeted");
            tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
            tweet.timeStamp = jsonObject.getString("created_at");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return tweet;
    }

    /**
     * Convert JSON array to ArrayList of Tweet object
     * @param jsonArray
     * @return
     */
    public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());

        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Tweet tweet = Tweet.fromJson(tweetJson);
            if (tweet != null) {
                tweets.add(tweet);
            }
        }

        return tweets;
    }
}