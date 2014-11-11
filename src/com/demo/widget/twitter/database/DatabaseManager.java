package com.demo.widget.twitter.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import com.demo.widget.twitter.helpers.GeneralUtils;
import com.demo.widget.twitter.helpers.MeListItem;
import com.demo.widget.twitter.helpers.WidgetInfo;

/**
 * Singleton class :generally a helper class to update,insert,delete items to
 * database tables
 * 
 */
public enum DatabaseManager {
	INSTANCE;

	private boolean isDbClosed = true;
	private SQLiteDatabase db;

	/**
	 * @param context
	 *            Initialize Database and open if closed
	 */
	public void init(Context context) {
		if (isDbClosed) {
			DatabaseHelper dbHelper = new DatabaseHelper(context);
			db = dbHelper.getWritableDatabase();
			isDbClosed = false;
		}
	}

	/**
	 * Close already opened database
	 */
	public void closeDatabase() {
		if (!isDbClosed && db != null) {
			isDbClosed = true;
			db.close();
		}
	}

	public boolean isDatabaseClosed() {
		return isDbClosed;
	}

	/**
	 * @param meLists
	 * @param appWidgetId
	 * @param listItemId
	 *            store newly fetched Status to database and delete already
	 *            stored status values all status is deependent of widget id
	 */
	public void storeTweetStatii(ArrayList<MeListItem> meLists,
			int appWidgetId, int listItemId,String listName) {
		ContentValues cv;
		deleteStoredValueOfWidget(Integer.toString(appWidgetId));
		for (MeListItem meListItem : meLists) {
			cv = new ContentValues();
			cv.put(DatabaseHelper.TWEET_HEADING, meListItem.fullName);
			cv.put(DatabaseHelper.TWEET_DESCRIPTION, meListItem.description);
			cv.put(DatabaseHelper.WIDGET_ID, Integer.toString(appWidgetId));
			cv.put(DatabaseHelper.USER_ID, meListItem.userId);
			cv.put(DatabaseHelper.TWEET_IMAGE, meListItem.imageUrl);
			cv.put(DatabaseHelper.TWEET_DATE, meListItem.tweetDate);
			cv.put(DatabaseHelper.TWEET_ID, meListItem.id);
			db.insert(DatabaseHelper.TWEETS_TABLE_NAME, null, cv);
		}
		meLists.clear();

		cv = new ContentValues();
		cv.put(DatabaseHelper.WIDGET_ID, Integer.toString(appWidgetId));
		cv.put(DatabaseHelper.LAST_UPDATE_DATE, GeneralUtils.getCurrentTime());
		cv.put(DatabaseHelper.TYPE,
				listItemId == 0 ? DatabaseHelper.TYPE_TIMELINE
						: DatabaseHelper.TYPE_MELIST);
		if (listItemId != 0)
			cv.put(DatabaseHelper.LIST_ID, Integer.toString(listItemId));
		cv.put(DatabaseHelper.LIST_NAME, listName);
		db.insert(DatabaseHelper.WIDGET_INFO_TABLE_NAME, null, cv);
		cv = null;
	}

	/**
	 * @param appWidgetId
	 *            method to delete previously stored values of widgets like
	 *            widget info,tweet status from database
	 */
	public void deleteStoredValueOfWidget(String appWidgetId) {
		db.delete(DatabaseHelper.TWEETS_TABLE_NAME, DatabaseHelper.WIDGET_ID
				+ "=?", new String[] { appWidgetId });
		db.delete(DatabaseHelper.WIDGET_INFO_TABLE_NAME,
				DatabaseHelper.WIDGET_ID + "=?", new String[] { appWidgetId });
	}

	/**
	 * @param appWidgetId
	 * @return Fetch stored Tweets in accordance with appWidgetId
	 */
	public ArrayList<MeListItem> fetchStoredTweetStatii(String appWidgetId) {
		ArrayList<MeListItem> meList = new ArrayList<MeListItem>();
		Cursor cursor = db.query(DatabaseHelper.TWEETS_TABLE_NAME, null,
				DatabaseHelper.WIDGET_ID + "=?", new String[] { appWidgetId },
				null, null, null);
	    //Gravity.C
		while (cursor.moveToNext()) {
			MeListItem meListItem = new MeListItem();
			meListItem.fullName = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.TWEET_HEADING));
			meListItem.description = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.TWEET_DESCRIPTION));
			meListItem.tweetDate = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.TWEET_DATE));
			meListItem.imageUrl = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.TWEET_IMAGE));
			meListItem.imagePath = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.TWEET_IMAGE_FILENAME));
			meListItem.id = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.TWEET_ID));
			meList.add(meListItem);
		}
		cursor.close();

		return meList.size() == 0 ? null : meList;
	}

	/**
	 * @param appWidgetId
	 * @param idValue
	 * @param fileName
	 *            Once images are fetched,they are stored on Sdcard by
	 *            Filemanager and store the file path on database,so that those
	 *            images can later be loaded into widget listview image
	 */
	public void saveFileName(int appWidgetId, String idValue, String fileName) {
		ContentValues cv = new ContentValues();
		Log.i("@saveFileName appWidgetId:idValue",
				Integer.toString(appWidgetId) + "::" + idValue);
		cv.put(DatabaseHelper.TWEET_IMAGE_FILENAME, fileName);
		int effectedRows = db.update(DatabaseHelper.TWEETS_TABLE_NAME, cv,
				DatabaseHelper.WIDGET_ID + "=? and " + DatabaseHelper.TWEET_ID
						+ "=?", new String[] { Integer.toString(appWidgetId),
						idValue });
		Log.i("effected Rows", String.valueOf(effectedRows));
	}

	/**
	 * @param appWidgetId
	 * @return WidgetInfo retrieve stored value Widget by their appwidgetId and
	 *         return their information on helper class WidgetInfo
	 */
	public WidgetInfo getWidgetInfo(String appWidgetId) {
		Log.i("get widget info", appWidgetId);
		Cursor cursor = db.query(DatabaseHelper.WIDGET_INFO_TABLE_NAME,
				new String[] { "_id", DatabaseHelper.TYPE,
						DatabaseHelper.LIST_ID }, DatabaseHelper.WIDGET_ID
						+ "=?", new String[] { appWidgetId }, null, null, null);

		WidgetInfo widgetInfo = new WidgetInfo();
		while (cursor.moveToNext()) {
			widgetInfo.widgetType = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.TYPE));
			String listItemId = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.LIST_ID));
			if (listItemId != null)
				widgetInfo.listItemId = Integer.valueOf(listItemId);
			Log.i("widget type", "types ==" + widgetInfo.widgetType);
		}
		Log.i("WidgetInfo type", "type =" + widgetInfo.widgetType);
		Log.i("list item id", String.valueOf(widgetInfo.listItemId));
		cursor.close();
		return widgetInfo;

	}
	
	public String getTweetListName(int appWidgetId){
		Cursor cursor = db.query(DatabaseHelper.WIDGET_INFO_TABLE_NAME,
				new String[] { "_id", DatabaseHelper.LIST_NAME,
						DatabaseHelper.LIST_ID }, DatabaseHelper.WIDGET_ID
						+ "=?", new String[] { Integer.toString(appWidgetId) }, null, null, null);
		String listName=null;
		while(cursor.moveToNext()){
			listName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LIST_NAME));
		}
		cursor.close();
		return listName==null || TextUtils.isEmpty(listName.trim())?
				 "Tweets":listName;
	}

}
