package com.demo.widget.twitter.appwidget.controls;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import twitter4j.Paging;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.demo.widget.twitter.R;
import com.demo.widget.twitter.connect.Constants;
import com.demo.widget.twitter.connect.T4JTwitterFunctions;
import com.demo.widget.twitter.database.DatabaseManager;
import com.demo.widget.twitter.database.FileManager;

/**
 * 
 * Service class responsible for Status Fetch,load its images ,save it to file
 * for notify app widget for updates
 * 
 */
public class TwitterService extends Service {

	private int appWidgetId;
	private DatabaseManager db;

	public static final String ACTION_MELIST_FETCH = "meListFetch",
			ACTION_TIMELINE_FETCH = "timeLineFetch";

	public static final String ME_LIST_ID = "meListId";
	private int invalidMeListId = -1;
	private StatiiFetchHandler statiiFetchHandler;
	private int statiiFetchStatus = 0;
	private int notifyNature = WidgetProvider.GO_FOR_UPDATE;
	// For convenience the 10 status is only fetched
	private Paging paging = new Paging(1, 10);
	private int count = 0;
	private FileManager fileManager = FileManager.INSTANCE;
	private String listName = "";

	/**
	 * As per the intent passed to the service either fetch Me List Status or
	 * fetch Time Line Status
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		notifyNature = intent.getIntExtra(WidgetProvider.UPDATE_ACTION,
				WidgetProvider.GO_FOR_UPDATE);
		db = DatabaseManager.INSTANCE;
		db.init(getApplicationContext());

		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
			if (intent.hasExtra(WidgetProvider.LIST_NAME))
				listName = intent.getStringExtra(WidgetProvider.LIST_NAME);
			else
				listName = getString(R.string.tweets);
			Intent visibilityIntent = new Intent();
			visibilityIntent.setAction(WidgetProvider.WIDGET_VISIBILITY);
			visibilityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			visibilityIntent.putExtra(WidgetProvider.LIST_NAME, listName);
			visibilityIntent.putExtra(WidgetProvider.WIDGET_VISIBILITY, false);
			sendBroadcast(visibilityIntent);


			statiiFetchHandler = new StatiiFetchHandler(this);

			if (intent.getAction().equals(ACTION_MELIST_FETCH)) {
				Log.i("Fetch me List", "@TwitterService");
				fetchMeListStatii(intent.getIntExtra(ME_LIST_ID,
						invalidMeListId));
			} else if (intent.getAction().equals(ACTION_TIMELINE_FETCH)) {
				// fetch timeLine
				Log.i("Fetch time line statii", "@TwitterService");
				fetchTimeLineStatii();
			}

			Log.i("Fetching tweets", "fetching tweets");
		} else {
			Log.i("stopping self", "stopping self");
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private static final int STATII_FETCHED = 0x1;

	/**
	 * Handler to take care of action once Status is fetched
	 * 
	 */
	static class StatiiFetchHandler extends Handler {
		private final WeakReference<TwitterService> twitterServiceHolder;

		public StatiiFetchHandler(TwitterService twitterService) {
			twitterServiceHolder = new WeakReference<TwitterService>(
					twitterService);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case STATII_FETCHED:
				Log.i("Go populate Width ", "Go Populate Widget");
				twitterServiceHolder.get().statiiFetchHandler
						.removeMessages(msg.what);
				if (twitterServiceHolder.get().statiiFetchStatus == 1)
					twitterServiceHolder.get().loadImages();
				else
					twitterServiceHolder.get().populateWidgetWithStatii();
				break;

			default:
				break;
			}
		}
	}

	/**
	 * Using AQuery(a helper class) to load status images and store them on
	 * sdcard
	 */
	private void loadImages() {
		if (imageUrlList != null && imageUrlList.size() > 0) {
			for (Map.Entry<String, String> mapEntry : imageUrlList.entrySet()) {
				AQuery aquery = new AQuery(getBaseContext());
				final String idValue = mapEntry.getValue();
				aquery.ajax(mapEntry.getKey(), Bitmap.class,
						new AjaxCallback<Bitmap>() {
							@Override
							public void callback(String url, Bitmap object,
									AjaxStatus status) {
								super.callback(url, object, status);
								Log.i("store images", url);
								storeImages(url, object, idValue);
							}
						});
			}
		}
	}

	/**
	 * @param url
	 * @param bitmap
	 * @param idValue
	 *            Store bitmaps as images on Sdcard Update the value(imagePath)
	 *            on Database with helper class FileManager
	 */
	private void storeImages(String url, Bitmap bitmap, String idValue) {

		if (count == 0) {
			Log.i("create directory", "@twitterService");
			fileManager.deleteAndCreateDirectory(appWidgetId);

		}

		fileManager.storeBitmap(appWidgetId, bitmap, idValue, db);
		count++;

		if (count == imageUrlList.size()) {
			count = 0;
			imageUrlList.clear();
			populateWidgetWithStatii();
		}

	}

	private HashMap<String, String> imageUrlList;

	/**
	 * Method to fetchTimeLineStatii with helper calls T4JTwitterFunctions
	 */
	private void fetchTimeLineStatii() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				imageUrlList = new HashMap<String, String>();
				statiiFetchStatus = T4JTwitterFunctions.getUserTimeTimeStatii(
						getApplicationContext(), Constants.CONSUMER_KEY,
						Constants.CONSUMER_SECRET, paging, appWidgetId,
						imageUrlList,listName);
				statiiFetchHandler.sendEmptyMessage(STATII_FETCHED);
			}
		}).start();
	}

	/**
	 * Method to fetch Me List Statii with helper calls T4JTwitterFunctions
	 */
	private void fetchMeListStatii(final int meListItemId) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				imageUrlList = new HashMap<String, String>();
				statiiFetchStatus = T4JTwitterFunctions.fetchTweetsFromMeList(
						getApplicationContext(), Constants.CONSUMER_KEY,
						Constants.CONSUMER_SECRET, paging, meListItemId,
						appWidgetId, imageUrlList,listName);
				Log.i("statuslist fetch", String.valueOf(meListItemId));
				statiiFetchHandler.sendEmptyMessage(STATII_FETCHED);
			}
		}).start();
	}

	/**
	 * As per the result of Status fetch either populate the widget or show
	 * error messages
	 */
	private void populateWidgetWithStatii() {
		Intent visibilityIntent = new Intent();
		visibilityIntent.setAction(WidgetProvider.WIDGET_VISIBILITY);
		visibilityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				appWidgetId);
		visibilityIntent.putExtra(WidgetProvider.WIDGET_VISIBILITY, true);
		visibilityIntent.putExtra(WidgetProvider.LIST_NAME, listName);
		sendBroadcast(visibilityIntent);

		if (statiiFetchStatus == 1) {
			Log.i("Twitter Service me list tweet size",
					String.valueOf(statiiFetchStatus));

			Intent widgetUpdate = new Intent();
			widgetUpdate.setAction(WidgetProvider.WIDGET_ME_LIST_STATII_UPDATE);
			widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			widgetUpdate.putExtra(WidgetProvider.UPDATE_ACTION, notifyNature);
			sendBroadcast(widgetUpdate);

		} else {
			Toast.makeText(this, getString(R.string.error_fetching_tweets),
					Toast.LENGTH_SHORT).show();
		}
		stopSelf();

	}

}
