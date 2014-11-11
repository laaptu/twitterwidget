package com.demo.widget.twitter;

import com.bugsense.trace.BugSenseHandler;
import com.demo.widget.twitter.connect.Constants;

import android.app.Application;

public class WidgetApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		BugSenseHandler.initAndStartSession(getApplicationContext(), Constants.BUGSENSE_API_KEY);
	}
	
	

}
