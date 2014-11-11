package com.demo.widget.twitter.connect;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.demo.widget.twitter.database.DatabaseManager;
import com.demo.widget.twitter.helpers.GeneralUtils;
import com.demo.widget.twitter.helpers.MeListItem;

public class T4JTwitterFunctions {

	public T4JTwitterFunctions() {
		// TODO Auto-generated constructor stub
	}

	public static void postToTwitter(Context c, final Activity callingActivity,
			final String consumerKey, final String consumerSecret,
			final String message, final TwitterPostResponse postResponse) {
		if (!T4JTwitterLoginActivity.isConnected(c)) {
			postResponse.OnResult(false);
			return;
		}

		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey);
		configurationBuilder.setOAuthConsumerSecret(consumerSecret);
		configurationBuilder.setOAuthAccessToken(T4JTwitterLoginActivity
				.getAccessToken((c)));
		configurationBuilder.setOAuthAccessTokenSecret(T4JTwitterLoginActivity
				.getAccessTokenSecret(c));
		Configuration configuration = configurationBuilder.build();
		final Twitter twitter = new TwitterFactory(configuration).getInstance();
		// twitter.get

		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean success = true;
				try {
					twitter.updateStatus(message);
				} catch (TwitterException e) {
					e.printStackTrace();
					success = false;
				}

				final boolean finalSuccess = success;

				callingActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						postResponse.OnResult(finalSuccess);
					}
				});

			}
		}).start();
	}

	public static abstract class TwitterPostResponse {
		public abstract void OnResult(Boolean success);
	}

	public static String SEPARATOR = ",";

	/**
	 * @param context
	 * @param consumerKey
	 * @param consumerSecret
	 * @param paging
	 * @param listId
	 * @param appWidgetId
	 * @param imageUrlList
	 * @return Method to fetch Tweets from selected Me List
	 */
	public static int fetchTweetsFromMeList(Context context,
			String consumerKey, String consumerSecret, Paging paging,
			int listId, int appWidgetId, HashMap<String, String> imageUrlList,String listName) {

		int returnValue = 0;
		ArrayList<MeListItem> timeLineStatii = new ArrayList<MeListItem>();
		if (!T4JTwitterLoginActivity.isConnected(context)) {
			Log.i("No connectivity ", "No connectivity");
			return 0;
		}

		Twitter twitter = buildTwitter(consumerKey, consumerSecret, context);

		try {
			ResponseList<Status> statiiList = twitter.getUserListStatuses(
					listId, paging);
			if (statiiList != null && statiiList.size() > 0) {
				for (Status status : statiiList) {
					MeListItem meListItem = new MeListItem();
					meListItem.imageUrl = status.getUser()
							.getMiniProfileImageURL();

					meListItem.description = status.getText();
					meListItem.fullName = status.getUser().getName();

					meListItem.tweetDate = GeneralUtils
							.getTimeDifference(status.getCreatedAt());
					meListItem.userId = String
							.valueOf(status.getUser().getId());
					meListItem.id = String.valueOf(status.getId());
					addImageUrl(imageUrlList, meListItem.imageUrl,
							meListItem.id);
					timeLineStatii.add(meListItem);

				}
				statiiList.clear();

				// return 1;
			}

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DatabaseManager dbManager = DatabaseManager.INSTANCE;
		dbManager.init(context);
		returnValue = timeLineStatii.size() > 0 ? 1 : 0;
		dbManager.storeTweetStatii(timeLineStatii, appWidgetId, listId,listName);
		return returnValue;

	}

	/**
	 * @param context
	 * @param consumerKey
	 * @param consumerSecret
	 * @return method to fetch Me Lists from Twitter
	 */
	public static ArrayList<UserList> fetchMeLists(Context context,
			String consumerKey, String consumerSecret) {
		if (!T4JTwitterLoginActivity.isConnected(context)) {
			Log.i("No connectivity ", "No connectivity");
			return null;
		}

		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey);
		configurationBuilder.setOAuthConsumerSecret(consumerSecret);
		configurationBuilder.setOAuthAccessToken(T4JTwitterLoginActivity
				.getAccessToken(context));
		configurationBuilder.setOAuthAccessTokenSecret(T4JTwitterLoginActivity
				.getAccessTokenSecret(context));
		Configuration configuration = configurationBuilder.build();
		Log.i("building configuration", "building configuration");

		Twitter twitter = new TwitterFactory(configuration).getInstance();

		try {
			return (ArrayList<UserList>) twitter.getUserLists(twitter.getId());
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * @param context
	 * @param consumerKey
	 * @param consumerSecret
	 * @param paging
	 * @param appWidgetId
	 * @param imageUrlList
	 * @return Method to fetch User Time Line Status
	 */
	public static int getUserTimeTimeStatii(Context context,
			String consumerKey, String consumerSecret, Paging paging,
			int appWidgetId, HashMap<String, String> imageUrlList,String listName) {

		int returnValue = 0;
		if (!T4JTwitterLoginActivity.isConnected(context)) {
			Log.i("Not logged in", "to twitter");
			return 0;
		}

		ArrayList<MeListItem> timeLineStatii = new ArrayList<MeListItem>();
		Twitter twitter = buildTwitter(consumerKey, consumerSecret, context);
		try {
			ResponseList<Status> statiiList = twitter.getHomeTimeline(paging);
			if (statiiList != null && statiiList.size() > 0) {
				for (Status status : statiiList) {
					MeListItem meListItem = new MeListItem();
					meListItem.imageUrl = status.getUser()
							.getMiniProfileImageURL();

					meListItem.description = status.getText();
					
					
					meListItem.fullName = status.getUser().getName();
					// status.get
					Log.i(" User time line Full Name fetch", "fullname ="
							+ meListItem.fullName);
					meListItem.tweetDate = GeneralUtils
							.getTimeDifference(status.getCreatedAt());
					meListItem.userId = String
							.valueOf(status.getUser().getId());
					meListItem.id = String.valueOf(status.getId());
					addImageUrl(imageUrlList, meListItem.imageUrl,
							meListItem.id);
					timeLineStatii.add(meListItem);

				}
				statiiList.clear();

				// return 1;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		DatabaseManager dbManager = DatabaseManager.INSTANCE;
		dbManager.init(context);
		returnValue = timeLineStatii.size() > 0 ? 1 : 0;
		dbManager.storeTweetStatii(timeLineStatii, appWidgetId, 0,listName);
		return returnValue;

	}

	public static void addImageUrl(HashMap<String, String> imageUrlList,
			String imageUrl, String statusId) {
		if (imageUrlList.containsKey(imageUrl)) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(imageUrlList.get(imageUrl));
			stringBuilder.append(SEPARATOR);
			stringBuilder.append(statusId);
			imageUrlList.put(imageUrl, stringBuilder.toString());
			stringBuilder.delete(0, stringBuilder.length());
			stringBuilder = null;
		} else {
			imageUrlList.put(imageUrl, statusId);
		}
	}

	public static Twitter buildTwitter(String consumerKey,
			String consumerSecret, Context context) {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey);
		configurationBuilder.setOAuthConsumerSecret(consumerSecret);
		configurationBuilder.setOAuthAccessToken(T4JTwitterLoginActivity
				.getAccessToken(context));
		configurationBuilder.setOAuthAccessTokenSecret(T4JTwitterLoginActivity
				.getAccessTokenSecret(context));
		Configuration configuration = configurationBuilder.build();
		return new TwitterFactory(configuration).getInstance();
	}

}
