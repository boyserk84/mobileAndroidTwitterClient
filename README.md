# My Twitter Client App

## Overview

This app use RestClientTemplate as a skeleton. [RestClientTemplate](https://github.com/thecodepath/android-rest-client-template) - Rest Client Template to make handling OAuth simple.


The following things are supported out of the box:
 * Authenticating with any OAuth 1.0a or OAuth 2 API
 * Sending requests for and parsing JSON API data using a defined client
 * Persisting data to a local SQLite store through an ORM layer
 * Displaying and caching remote image data into views

The following libraries are used to make this possible:
 * [scribe-java](https://github.com/fernandezpablo85/scribe-java) - Simple OAuth library for handling the authentication flow.
 * [Android Async HTTP](https://github.com/loopj/android-async-http) - Simple asynchronous HTTP requests with JSON parsing
 * [codepath-oauth](https://github.com/thecodepath/android-oauth-handler) - Custom-built library for managing OAuth authentication and signing of requests
 * [UniversalImageLoader](https://github.com/nostra13/Android-Universal-Image-Loader) - Used for async image loading and caching them in memory and on disk.
 * [ActiveAndroid](https://github.com/pardom/ActiveAndroid) - Simple ORM for persisting a local SQLite database on the Android device
 
 
# User Stories:

 * User can sign in using OAuth login flow
 * User can view last 25 tweets from their home timeline
 * User should be able to see the user, body and timestamp for tweet
 * User should be displayed the relative timestamp for a tweet "8m", "7h"
	* X Optional: Links in tweets are clickable and viewable
 * User can load more tweets once they reach the bottom of the list using "infinite scroll" pagination
 * User can compose a new tweet
	* User can click a “Compose” icon in the Action Bar on the top right
	* User will have a Compose view opened
	* User can enter a message and hit a button to post to twitter
	* User should be taken back to home timeline with new tweet visible
        X Optional: User can see a counter with total number of characters left for tweet
