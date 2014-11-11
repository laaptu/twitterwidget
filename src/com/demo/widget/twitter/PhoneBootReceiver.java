package com.demo.widget.twitter;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.demo.widget.twitter.connect.Constants;
import com.demo.widget.twitter.database.DatabaseManager;
import com.demo.widget.twitter.helpers.GeneralUtils;
import com.demo.widget.twitter.helpers.SessionStore;

public class PhoneBootReceiver extends BroadcastReceiver {

	public static boolean isPhoneReboot = false;
	private DatabaseManager dbManager;

	/* 
	 * This method is required to initiate all update interval of widgets 
	 * and update the widget list after phone reboot
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		isPhoneReboot = true;
		
		Log.i("phone boot done", "phone boot done");
		HashMap<String, Long> updateIntervalsMap = SessionStore
				.getUpdateIntervalWithAppWidgetId(context);
		if (updateIntervalsMap != null) {
			dbManager = DatabaseManager.INSTANCE;
			dbManager.init(context);
			for (Map.Entry<String, Long> mapEntry : updateIntervalsMap
					.entrySet()) {
				int appWidgetId = Integer.valueOf(mapEntry.getKey());
				//Re-initializing all the update interval of widgets
				GeneralUtils.initAlarmManager(context, appWidgetId, SessionStore.getUpdateInterval(context, appWidgetId));
			}
			updateIntervalsMap.clear();
		}
	}

	
}
