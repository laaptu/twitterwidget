package com.demo.widget.twitter.appwidget.controls;

import java.util.HashMap;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.demo.widget.twitter.PhoneBootReceiver;
import com.demo.widget.twitter.R;
import com.demo.widget.twitter.appwidget.config.ConfigActivity;
import com.demo.widget.twitter.database.DatabaseHelper;
import com.demo.widget.twitter.database.DatabaseManager;
import com.demo.widget.twitter.database.FileManager;
import com.demo.widget.twitter.helpers.GeneralUtils;
import com.demo.widget.twitter.helpers.SessionStore;
import com.demo.widget.twitter.helpers.WidgetInfo;

public class WidgetProvider extends AppWidgetProvider {

	public static String WIDGET_UPDATE = "com.demo.widget.twitter.TWEET_WIDGET_UPDATE";
	public static String WIDGET_ME_LIST_STATII_UPDATE = "com.demo.widget.twitter.ME_LIST_STATII";
	public static String WIDGET_LIST_REFRESH = "com.demo.widget.twitter.WIDGET_REFRESH";
	public static String WIDGET_LIST_SETTING = "com.demo.widget.twitter.WIDGET_LIST_SETTING";
	public static String WIDGET_VISIBILITY = "com.demo.widget.twitter.WIDGET_VISIBILITY";

	private static HandlerThread sWorkerThread;
	private static Handler sWorkerQueue;

	public static final String UPDATE_ACTION = "updateAction",
			VISIBILITY_ACTION = "visibility";
	public static final int GO_FOR_UPDATE = 0x1, GO_FOR_NOTIFY = 0x2;
	private int notifyNature = GO_FOR_UPDATE;
	public static String LIST_NAME = "listName";

	private static HashMap<Integer, Uri> uriHolder = new HashMap<Integer, Uri>();

	public WidgetProvider() {
		sWorkerThread = new HandlerThread("WidgetProvider-worker");
		sWorkerThread.start();
		sWorkerQueue = new Handler(sWorkerThread.getLooper());
	}

	/*
	 * Once the phone boots,need to populate the widgets,so for that using
	 * PhoneBootReceiver isPhoneReboot value make Android update our widgets
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		if (PhoneBootReceiver.isPhoneReboot) {
			PhoneBootReceiver.isPhoneReboot = false;
			final int N = appWidgetIds.length;
			for (int i = 0; i < N; ++i) {
				RemoteViews remoteViews = updateWidgetListView(context,
						appWidgetIds[i]);
				appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
			}
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	/*
	 * MEthod which receives broadcast for Status Update Refresh Widget Delete
	 * Widget ListView and Progressbar toggle visibility
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		initDatabase(context);

		final String listName = intent.hasExtra(LIST_NAME) ? intent
				.getStringExtra(LIST_NAME) : context.getString(R.string.tweets);

		Log.i("Update Recieved", "Update Received");
		final int appWidgetId = intent.getIntExtra(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		final Context ctx = context;

		if (WIDGET_UPDATE.equals(action)) {
			Log.i("Widget Update from", "Alarm manager");
			Log.i("appWidgetId", String.valueOf(appWidgetId));
			refreshStatiiList(context, appWidgetId);

		} else if (WIDGET_ME_LIST_STATII_UPDATE.equals(action)) {
			if (intent.hasExtra(WidgetProvider.UPDATE_ACTION))
				notifyNature = intent.getIntExtra(UPDATE_ACTION, GO_FOR_UPDATE);
			Log.i("Go for me list update", "go for  me list update");

			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				AppWidgetManager appWidgetManager = AppWidgetManager
						.getInstance(context);
				RemoteViews remoteViews = updateWidgetListView(context,
						appWidgetId);

				if (!listName.equals(dbManager.getTweetListName(appWidgetId))) {
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
				}
				if (notifyNature == GO_FOR_NOTIFY) {
					final ComponentName cn = new ComponentName(context,
							WidgetProvider.class);
					appWidgetManager.notifyAppWidgetViewDataChanged(
							appWidgetManager.getAppWidgetIds(cn),
							R.id.tweetListView);
					Log.i("update nature==", "notify");
				} else {
					Log.i("update nature==", "update");
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
				}

				Log.i("Invalid App Widget Id",
						String.valueOf(AppWidgetManager.INVALID_APPWIDGET_ID));
				Log.i("App Widget Id", String.valueOf(appWidgetId));

				// }

			}
		} else if (WIDGET_LIST_REFRESH.equals(action)) {
			Log.i("Refresh Action", "Go for refresh");

			Log.i("appWidgetId", String.valueOf(appWidgetId));
			refreshStatiiList(context, appWidgetId);

		} else if (WIDGET_LIST_SETTING.equals(action)) {

			Log.i("settings go", "go for settings");
			Log.i("app widget id", String.valueOf(appWidgetId));
			sWorkerQueue.removeMessages(0);
			sWorkerQueue.post(new Runnable() {

				@Override
				public void run() {
					Intent settingIntent = new Intent(ctx, ConfigActivity.class);
					settingIntent.putExtra(UPDATE_ACTION, GO_FOR_NOTIFY);
					settingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Bundle params = new Bundle();
					params.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
							appWidgetId);
					settingIntent.putExtras(params);
					ctx.startActivity(settingIntent);
				}
			});

		} else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {

			Log.i("delete contents of widgetid", String.valueOf(appWidgetId));

			sWorkerQueue.removeMessages(0);
			sWorkerQueue.post(new Runnable() {

				@Override
				public void run() {
					// delete table from db associated with that widget
					Log.i("app widget value deleted", "from database");
					dbManager.deleteStoredValueOfWidget(Integer
							.toString(appWidgetId));
					SessionStore.deleteUpdateInterval(ctx, appWidgetId);
					GeneralUtils.cancelAlarmManager(ctx, appWidgetId);
					FileManager.INSTANCE.deleteDirectory(appWidgetId);
				}
			});
		} else if (WIDGET_VISIBILITY.equals(action)) {

			final boolean showListViewWidget = intent.getBooleanExtra(
					WIDGET_VISIBILITY, false);

			sWorkerQueue.removeMessages(0);
			sWorkerQueue.post(new Runnable() {

				@Override
				public void run() {
					AppWidgetManager appWidgetManager = AppWidgetManager
							.getInstance(ctx);
					RemoteViews remoteView = new RemoteViews(ctx
							.getPackageName(), R.layout.widget_layout);
					if (showListViewWidget) {
						remoteView.setViewVisibility(R.id.listViewLayout,
								View.VISIBLE);
						remoteView.setViewVisibility(R.id.progressBar,
								View.GONE);
					} else {
						remoteView.setViewVisibility(R.id.listViewLayout,
								View.GONE);
						remoteView.setViewVisibility(R.id.progressBar,
								View.VISIBLE);
					}

					// remoteView.setTextViewText(R.id.headingTextView,
					// listName);
					appWidgetManager.updateAppWidget(appWidgetId, remoteView);
				}
			});

		}

		super.onReceive(context, intent);
	}

	/**
	 * @param ctx
	 * @param appWidgetId
	 *            Method to refresh Widget Status or feeds
	 */
	private void refreshStatiiList(final Context ctx, final int appWidgetId) {
		final WidgetInfo widgetInfo = dbManager.getWidgetInfo(Integer
				.toString(appWidgetId));
		sWorkerQueue.removeMessages(0);
		sWorkerQueue.post(new Runnable() {

			@Override
			public void run() {
				Intent listRefreshIntent = new Intent(ctx, TwitterService.class);
				listRefreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);
				listRefreshIntent.putExtra(WidgetProvider.UPDATE_ACTION,
						WidgetProvider.GO_FOR_NOTIFY);
				Log.i("WidgetInfoType", widgetInfo.widgetType);
				if (widgetInfo.widgetType.equals(DatabaseHelper.TYPE_TIMELINE)) {
					listRefreshIntent
							.setAction(TwitterService.ACTION_TIMELINE_FETCH);
				} else {
					listRefreshIntent
							.setAction(TwitterService.ACTION_MELIST_FETCH);
					listRefreshIntent.putExtra(TwitterService.ME_LIST_ID,
							widgetInfo.listItemId);
				}
				ctx.startService(listRefreshIntent);
			}
		});
	}

	private DatabaseManager dbManager;

	/**
	 * @param context
	 *            Initializing DatabaseManager
	 */
	private void initDatabase(Context context) {
		if (dbManager == null) {
			dbManager = DatabaseManager.INSTANCE;
			dbManager.init(context);
		}
	}

	/**
	 * @param context
	 * @param appWidgetId
	 * @return Method to populate app widget listView and setting button click
	 *         action as well
	 */
	private RemoteViews updateWidgetListView(Context context, int appWidgetId) {

		// for (int i = 0; i < N; i++) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		Intent svcIntent = new Intent(context, WidgetService.class);
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		remoteViews
				.setRemoteAdapter(appWidgetId, R.id.tweetListView, svcIntent);
		remoteViews.setEmptyView(R.id.tweetListView, R.id.empty_view);
		Log.i("Update App Widget", "Update app widgets");

		remoteViews.setTextViewText(R.id.headingTextView,
				dbManager.getTweetListName(appWidgetId));

		final Intent refreshIntent = new Intent(context, WidgetProvider.class);
		refreshIntent
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		refreshIntent.setAction(WidgetProvider.WIDGET_LIST_REFRESH);
		final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
				context, appWidgetId, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		// refreshPendingIntent.
		remoteViews.setOnClickPendingIntent(R.id.refreshImageView,
				refreshPendingIntent);

		final Intent settingIntent = new Intent(context, WidgetProvider.class);

		settingIntent
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		settingIntent.setAction(WIDGET_LIST_SETTING);

		final PendingIntent settingPendingIntent = PendingIntent.getBroadcast(
				context, appWidgetId, settingIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.settingImageView,
				settingPendingIntent);

		initAlarmManager(context, appWidgetId,
				SessionStore.getUpdateInterval(context, appWidgetId));

		return remoteViews;
	}

	/**
	 * @param context
	 * @param appWidgetId
	 * @param updateIntervalInMillis
	 *            Using helper class GeneralUtils to initialize AlarmManager for
	 *            periodic update of widgets as per the value set on Update
	 *            Interval Spinner
	 */
	private void initAlarmManager(Context context, int appWidgetId,
			long updateIntervalInMillis) {
		GeneralUtils.initAlarmManager(context, appWidgetId,
				updateIntervalInMillis);
	}

	// ---Respective Methods to add Uri in relation with appwidget Id to
	// uniquely identify AlarmManager intent to start/stop it

	public static void addUri(int appWidgetId, Uri uri) {
		uriHolder.put(Integer.valueOf(appWidgetId), uri);
	}

	public static Uri getUri(int appWidgetId) {
		return uriHolder.get(appWidgetId);
	}

	public static void remoteUri(int appWidgetId) {
		uriHolder.remove(appWidgetId);
	}

	// -----------------------------------------------------------------------------------------------

}
