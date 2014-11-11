package com.demo.widget.twitter.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.demo.widget.twitter.appwidget.controls.WidgetProvider;

public class GeneralUtils {

	public static final String TIME_FORMAT = "MMddyyyyHHmm";

	/**
	 * @return String method to getCurrentTime of the phone
	 */
	public static String getCurrentTime() {
		Calendar currentCalendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT,
				Locale.US);
		String currentTime = simpleDateFormat.format(currentCalendar.getTime());
		simpleDateFormat = null;
		currentCalendar = null;
		return currentTime;
	}

	/**
	 * @param context
	 * @return boolean method to check whether internet is present or not
	 */
	public static boolean isConnectedToInternet(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param context
	 * @param appWidgetId
	 *            Method to cancel already initiated AlarmManager on appWidget
	 *            update/delete
	 */
	public static void cancelAlarmManager(Context context, int appWidgetId) {
		if (WidgetProvider.getUri(appWidgetId) != null) {
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent refreshIntent = new Intent(context, WidgetProvider.class);
			refreshIntent.setAction(WidgetProvider.WIDGET_UPDATE);
			refreshIntent.setData(WidgetProvider.getUri(appWidgetId));
			refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(
					context, appWidgetId, refreshIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.cancel(pendingIntentAlarm);
			WidgetProvider.remoteUri(appWidgetId);
		}
	}

	/**
	 * @param context
	 * @param appWidgetId
	 * @param updateIntervalInMillis
	 *            Initializing AlarmManager meaning update interval making work
	 */
	public static void initAlarmManager(Context context, int appWidgetId,
			long updateIntervalInMillis) {

		GeneralUtils.cancelAlarmManager(context, appWidgetId);
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.appendPath("" + appWidgetId);
		Uri uri = uriBuilder.build();

		WidgetProvider.addUri(appWidgetId, uri);

		Intent refreshIntent = new Intent(context, WidgetProvider.class);
		refreshIntent.setAction(WidgetProvider.WIDGET_UPDATE);
		refreshIntent.setData(WidgetProvider.getUri(appWidgetId));
		refreshIntent
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(context,
				appWidgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + updateIntervalInMillis,
				updateIntervalInMillis, pendingIntentAlarm);

	}

	/**
	 * @param position
	 * @return long Get widgetUpdateInterval as per spinner selected position
	 *         this logic is as per data @ stringArray update_interval
	 */
	public static long getWidgetUpdateInterval(int position) {
		// getResources().getStringArray(R.array.update_interval);
		switch (position) {
		case 0:
			// for 30 minutes
			return 1800000;
		case 1:
			// for 1 hour
			// 1*60*60*1000
			return 3600000;
		case 2:
			// for 4 hours
			return 14400000;
		case 3:
			// for 24 hours
			return 86400000;
		default:
			// for 30 minutes
			return 1800000;
		}
	}

	/**
	 * @param updateIntervalValue
	 * @return int This method is for setting the default update interval
	 *         spinner position as saved on SharedPrefrence and as per data @
	 *         stringArray update_interval
	 */
	public static int getWidgetUpdateIntervalSpinnerPosition(
			long updateIntervalValue) {
		String updateInterval = Long.toString(updateIntervalValue);
		updateInterval = updateInterval.trim();
		Log.i("updateInterval @ GeneralUtils", updateInterval);
		if (updateInterval.equals("1800000")) {
			Log.i("updateInterval @ GeneralUtils", "30 mins");
			return 0;
		} else if (updateInterval.equals("3600000")) {
			Log.i("updateInterval @ GeneralUtils", "every hours");
			return 1;
		} else if (updateInterval.equals("14400000")) {
			Log.i("updateInterval @ GeneralUtils", "every  4 hours");
			return 2;
		} else if (updateInterval.equals("86400000")) {
			Log.i("updateInterval @ GeneralUtils", "every 24 hours");
			return 3;
		}
		return 0;
	}

	/**
	 * @param dateTime
	 * @return String method to display TimeDifference currentTime-tweet status
	 *         time
	 * 
	 */
	public static String getTimeDifference(Date dateTime) {
		StringBuffer sb = new StringBuffer();
		Date current = Calendar.getInstance().getTime();
		long diffInSeconds = (current.getTime() - dateTime.getTime()) / 1000;

		long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
		long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60
				: diffInSeconds;
		long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24
				: diffInSeconds;
		long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30
				: diffInSeconds;
		long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12
				: diffInSeconds;
		long years = (diffInSeconds = (diffInSeconds / 12));

		if (years > 0) {
			if (years == 1) {
				sb.append("a yr");
			} else {
				sb.append(years + " yrs");
			}
			/*
			 * if (years <= 6 && months > 0) { if (months == 1) {
			 * sb.append(" and a mth"); } else { sb.append(" and " + months +
			 * " mths"); } }
			 */
		} else if (months > 0) {
			if (months == 1) {
				sb.append("a mth");
			} else {
				sb.append(months + " mths");
			}
			/*
			 * if (months <= 6 && days > 0) { if (days == 1) {
			 * sb.append(" and a day"); } else { sb.append(" and " + days +
			 * " days"); } }
			 */
		} else if (days > 0) {
			if (days == 1) {
				sb.append("a day");
			} else {
				sb.append(days + " days");
			}
			/*
			 * if (days <= 3 && hrs > 0) { if (hrs == 1) {
			 * sb.append(" and an hour"); } else { sb.append(" and " + hrs +
			 * " hours"); } }
			 */
		} else if (hrs > 0) {
			if (hrs == 1) {
				sb.append("an hr");
			} else {
				sb.append(hrs + " hrs");
			}
			/*
			 * if (min > 1) { sb.append(" and " + min + " minutes"); }
			 */
		} else if (min > 0) {
			if (min == 1) {
				sb.append("a min");
			} else {
				sb.append(min + " mins");
			}
			/*
			 * if (sec > 1) { sb.append(" and " + sec + " seconds"); }
			 */
		} else {
			if (sec <= 1) {
				sb.append("a sec");
			} else {
				sb.append(sec + " secs");
			}
		}

		sb.append(" ago");

		return sb.toString();
	}

}
