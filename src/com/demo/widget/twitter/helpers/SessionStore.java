package com.demo.widget.twitter.helpers;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Helper class to store and retrieve values to and from SharedPreferences
 * UpdateInterval is saved as < item name="appwidgetId">updateInterval</item>
 * 
 */
public class SessionStore {

	public static final String TWITTER_SHARED_PREF = "twitter_shared_pref";
	public static final String TWEET_UPDATE_PREF = "tweetUpdatePref";
	public static final String LOGIN_STATUS = "loginStatus";

	public static final String UPDATE_INTERVAL = "updateInterval";

	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";

	private static final long DEFAULT_UPDATE_INTERVAL = 1800000;

	/**
	 * @param context
	 * @param updateInterval
	 * @param appWidgetId
	 * @return Method to store update interval in respect to its appWidgetId
	 */
	public static boolean storeUpdateInterval(Context context,
			long updateInterval, int appWidgetId) {
		SharedPreferences sharedPrefrences = context.getSharedPreferences(
				TWEET_UPDATE_PREF, Context.MODE_PRIVATE);
		String appWidgetIdValue = Integer.toString(appWidgetId);
		long storedUpdateInterval = sharedPrefrences.getLong(appWidgetIdValue,
				0);
		if (updateInterval != storedUpdateInterval) {
			Editor editor = sharedPrefrences.edit();
			editor.putLong(appWidgetIdValue, updateInterval);
			editor.commit();
			return true;
		} else {
			return false;
		}

	}

	/**
	 * @param context
	 * @param appWidgetId
	 *            Method to delete store update interval value as per its
	 *            respective appWidgetId
	 */
	public static void deleteUpdateInterval(Context context, int appWidgetId) {
		String appWidgetValue = Integer.toString(appWidgetId);
		Editor editor = context.getSharedPreferences(TWEET_UPDATE_PREF,
				Context.MODE_PRIVATE).edit();
		editor.remove(appWidgetValue);
		editor.commit();
	}

	/**
	 * @param context
	 * @param appWidgetId
	 * @return Method to get update interval as per its respective appWidgetId
	 */
	public static long getUpdateInterval(Context context, int appWidgetId) {
		SharedPreferences sharedPrefrences = context.getSharedPreferences(
				TWEET_UPDATE_PREF, Context.MODE_PRIVATE);
		String appWidgetIdValue = Integer.toString(appWidgetId);
		return sharedPrefrences.getLong(appWidgetIdValue,
				DEFAULT_UPDATE_INTERVAL);
	}

	/**
	 * @param context
	 * @return Method to get Update interval and appwidget ids required to
	 *         initialize AlarmManager on phone start/reboot
	 */
	public static HashMap<String, Long> getUpdateIntervalWithAppWidgetId(
			Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				TWEET_UPDATE_PREF, Context.MODE_PRIVATE);
		HashMap<String, Long> updateIntervalMap = (HashMap<String, Long>) sharedPreferences
				.getAll();
		if (updateIntervalMap != null && updateIntervalMap.size() > 0)
			return updateIntervalMap;
		return null;
	}

	/**
	 * @param context
	 * @param appWidgetId
	 * @return int Method to retrieve stored Update interval spinner position as
	 *         per appwidetId
	 */
	public static int getUpdateIntervalSpinnerPosition(Context context,
			int appWidgetId) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				TWEET_UPDATE_PREF, Context.MODE_PRIVATE);
		return GeneralUtils
				.getWidgetUpdateIntervalSpinnerPosition(sharedPreferences
						.getLong(String.valueOf(appWidgetId),
								DEFAULT_UPDATE_INTERVAL));
	}

}
