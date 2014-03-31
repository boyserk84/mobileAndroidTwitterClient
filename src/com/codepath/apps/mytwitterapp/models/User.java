package com.codepath.apps.mytwitterapp.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User class
 * Strongly-typed data class for handler JSON Twitter data.
 * @author nkemavaha
 *
 */
public class User implements Serializable{
	private static final long serialVersionUID = 8458607192036128626L;
	
	private String name;
	private long uid;
	private String screenName;
	private String profileBgImageUrl;
	private String profileImageUrl;
	private int numTweets;
	private int followersCount;
	private int friendsCount;
	
	public User () {}
	
    public String getName() {
        return name;
    }

    public long getId() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileBackgroundImageUrl() {
        return profileBgImageUrl;
    }
    
    public String getProfileImage() {
    	return profileImageUrl;
    }

    public int getNumTweets() {
        return numTweets;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public static User fromJson(JSONObject json) {
        User u = new User();
        try {
        	u.name = json.getString("name");
        	u.uid = json.getLong("id");
        	u.screenName = json.getString("screen_name");
        	u.profileBgImageUrl = json.getString("profile_background_image_url");
        	u.numTweets = json.getInt("statuses_count");
        	u.followersCount = json.getInt("followers_count");
        	u.friendsCount = json.getInt("friends_count");
        	u.profileImageUrl = json.getString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return u;
    }


}
