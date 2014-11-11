package com.demo.widget.twitter.helpers;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.UserList;
import android.graphics.Bitmap;

public class DataHolder {
	public static final String APPWIDGET_IDS="ids";
	
	
	
	public static final String ME_LIST="meList";
	public static final String TIMELINE_LIST="timeLineList";
	public static HashMap<Integer,HashMap<String,Bitmap>> imageList =new HashMap<Integer,HashMap<String,Bitmap>>();
	public static final String UPDATE_INTERVAL_DEFAULT="1800000";
}
