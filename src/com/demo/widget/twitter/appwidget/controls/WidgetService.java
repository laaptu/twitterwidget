package com.demo.widget.twitter.appwidget.controls;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

import com.demo.widget.twitter.database.DatabaseManager;
import com.demo.widget.twitter.helpers.MeListItem;

/**
 * A class to assign Adapter to Widget's ListView with data from Database
 */
public class WidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		Log.i("Go to tweetProvider", "Go to tweetProvider");
		int appWidgetId = intent.getIntExtra(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);

		return (new TweetProvider(this.getApplicationContext(), intent,
				fetchStoredTweetStatii(appWidgetId)));
	}

	private ArrayList<MeListItem> fetchStoredTweetStatii(int appWidgetId) {
		DatabaseManager db = DatabaseManager.INSTANCE;
		db.init(getApplicationContext());
		Log.i("Fetching stored tweet", "@WidgetService");
		return db.fetchStoredTweetStatii(String.valueOf(appWidgetId));
	}

}
