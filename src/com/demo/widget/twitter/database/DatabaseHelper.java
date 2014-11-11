package com.demo.widget.twitter.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLiteOpenHelper class to create and update database table two table exists
 * on DB_NAME=tweetList database 1:TWEETS_TABLE_NAME=tweetList To store tweets
 * 2:WIDGET_INFO_TABLE_NAME=widgetInfo to store widget information
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String TWEETS_TABLE_NAME = "tweetList",
			WIDGET_ID = "widgetId", TWEET_HEADING = "tweetHeading",
			TWEET_DESCRIPTION = "tweetDescription",
			LIST_MEMBERS = "memberCount", TWEET_DATE = "tweetDate",
			TWEET_IMAGE = "tweetImageUrl", DB_NAME = "tweetList",
			LAST_UPDATE_DATE = "lastUpdateDate", USER_ID = "userId",
			WIDGET_INFO_TABLE_NAME = "widgetInfo", TYPE = "type",
			LIST_ID = "listId", TYPE_MELIST = "0", TYPE_TIMELINE = "1",
			TWEET_ID = "tweetId", TWEET_IMAGE_FILENAME = "fileName",LIST_NAME="listName";

	public static final int DB_VERSION = 2;

	public static final String CREATE_TWEETS_TABLE = "create table if not exists "
			+ TWEETS_TABLE_NAME
			+ " (_id integer primary key autoincrement, "
			+ WIDGET_ID
			+ " text, "
			+ TWEET_HEADING
			+ " text, "
			+ TWEET_DESCRIPTION
			+ " text, "
			+ LIST_MEMBERS
			+ " text, "
			+ TWEET_DATE
			+ " text, "
			+ TWEET_IMAGE
			+ " text, "
			+ TWEET_IMAGE_FILENAME
			+ " text, "
			+ TWEET_ID
			+ " text, "
			+ USER_ID
			+ " text);";

	public static final String CREATE_WIDGET_INFO_TABLE = "create table if not exists "
			+ WIDGET_INFO_TABLE_NAME
			+ " (_id integer primary key autoincrement, "
			+ WIDGET_ID
			+ " text, "
			+ LAST_UPDATE_DATE
			+ " text, "
			+ TYPE
			+ " text, "
			+ LIST_NAME
			+ " text, "
			+ LIST_ID + " text);";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("create tweet table", CREATE_TWEETS_TABLE);
		db.execSQL(CREATE_WIDGET_INFO_TABLE);
		db.execSQL(CREATE_TWEETS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("drop table if exists " + WIDGET_INFO_TABLE_NAME);
		db.execSQL("drop table if exists " + TWEETS_TABLE_NAME);
		onCreate(db);

	}

}
