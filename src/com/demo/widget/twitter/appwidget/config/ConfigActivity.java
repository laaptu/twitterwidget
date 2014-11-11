package com.demo.widget.twitter.appwidget.config;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.demo.widget.twitter.R;
import com.demo.widget.twitter.appwidget.controls.TwitterService;
import com.demo.widget.twitter.appwidget.controls.WidgetProvider;
import com.demo.widget.twitter.connect.Constants;
import com.demo.widget.twitter.connect.T4JTwitterLoginActivity;
import com.demo.widget.twitter.helpers.GeneralUtils;
import com.demo.widget.twitter.helpers.SessionStore;

/**
 * Configuration activity of the widget
 * launched by default when widget is placed on the homescreen 
 *
 */
public class ConfigActivity extends FragmentActivity {

	private int appWidgetId;
	private boolean isUserLoggedIn = false;
	private static final int RETURN_FROM_TWITTER_LOGIN = 1;

	private TimeLineFragmentConfig timeLineFragmentConfig;
	private MeListFragment meListFragment;

	public static final String ME_LIST_LAUNCH = "meListLaunch",
			TIMELINE_LAUNCH = "timeLineLaunch", DEFAULT_LAUNCH = "default";

	private String action = DEFAULT_LAUNCH;
	private int notifyNature = WidgetProvider.GO_FOR_UPDATE;
	
	
	//Variable required for default update interval selection position
	private int updateSpinnerDefaultSelection = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config_layout);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	    //String.format(format, args)
		assignAppWidgetId();
		updateChangesForLogin();
	}

	/**
	 * method to assign widget id 
	 * and update spinner default value position
	 */
	private void assignAppWidgetId() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null)
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);

		if (intent.hasExtra(WidgetProvider.UPDATE_ACTION))
			notifyNature = intent.getIntExtra(WidgetProvider.UPDATE_ACTION,
					WidgetProvider.GO_FOR_UPDATE);

		updateSpinnerDefaultSelection = SessionStore
				.getUpdateIntervalSpinnerPosition(this, appWidgetId);
		Log.i("updateSpinnerDefaultSelection @ConfigActivity",
				String.valueOf(updateSpinnerDefaultSelection));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.widget_menu, menu);
		if (isUserLoggedIn)
			menu.getItem(0).setTitle(getString(R.string.logout));
		else
			menu.getItem(0).setTitle(getString(R.string.login));
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * T4JTwitterLoginActivity call for purpose of twitter login
	 * with Twitter Consumer key and Consumer Secret as params
	 */
	private void goToTwitterLogin() {
		if (!T4JTwitterLoginActivity.isConnected(getApplicationContext())) {
			Intent intent = new Intent(this, T4JTwitterLoginActivity.class);
			intent.putExtra(T4JTwitterLoginActivity.TWITTER_CONSUMER_KEY,
					Constants.CONSUMER_KEY);
			intent.putExtra(T4JTwitterLoginActivity.TWITTER_CONSUMER_SECRET,
					Constants.CONSUMER_SECRET);
			startActivityForResult(intent, RETURN_FROM_TWITTER_LOGIN);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == RETURN_FROM_TWITTER_LOGIN) {
			boolean isTwitterLoggedIn = resultCode == T4JTwitterLoginActivity.TWITTER_LOGIN_RESULT_CODE_SUCCESS;
			if (isTwitterLoggedIn) {
				updateChangesForLogin();
			} else {
				Toast.makeText(this, "Error login to twitter account",
						Toast.LENGTH_LONG).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * method to show necessary changes after logging out from Twitter
	 */
	private void goToTwitterLogout() {
		T4JTwitterLoginActivity.logOutOfTwitter(getApplicationContext());
		updateChangesForLogin();
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack(null,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		if (timeLineFragmentConfig != null)
			fragmentTransaction.remove(timeLineFragmentConfig);

		if (meListFragment != null)
			fragmentTransaction.remove(meListFragment);
		timeLineFragmentConfig = null;
		meListFragment = null;
		fragmentTransaction.commitAllowingStateLoss();
	}

	/**
	 * View or to say fragment changes as per Twitter Login/Logout
	 */
	private void updateChangesForLogin() {
		isUserLoggedIn = T4JTwitterLoginActivity.isConnected(this);
		if (isUserLoggedIn) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			if (action.equals(DEFAULT_LAUNCH)) {
				timeLineFragmentConfig = TimeLineFragmentConfig
						.getNewInstance(updateSpinnerDefaultSelection);
				fragmentTransaction.add(R.id.configParentFrameLayout,
						timeLineFragmentConfig).commitAllowingStateLoss();
			} else {
				meListFragment = MeListFragment
						.getNewInstance(updateSpinnerDefaultSelection);
				fragmentTransaction.add(R.id.configParentFrameLayout,
						meListFragment).commitAllowingStateLoss();
			}
		}
		invalidateOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals(getString(R.string.login)))
			goToTwitterLogin();
		else if (item.getTitle().equals(getString(R.string.logout)))
			goToTwitterLogout();
		return super.onOptionsItemSelected(item);
	}


	/** 
	 * Called from MeListFragment after Me List Item click
	 * with update interval position @param position
	 * and  List id @param meListId
	 * TwitterService is initialized and app widget with progressbar is shown
	 */
	public void applyMeListConfiguration(int position, int meListId,String listName) {
		Intent meListFetchIntent = createIntentWithAppWidgetId();
		meListFetchIntent.setAction(TwitterService.ACTION_MELIST_FETCH);
		meListFetchIntent.putExtra(TwitterService.ME_LIST_ID, meListId);
		meListFetchIntent.putExtra(WidgetProvider.LIST_NAME, listName);
		this.startService(meListFetchIntent);

		initAlarmManager(position, appWidgetId);

		showAppWidget();

	}

	/**
	 * Called from TimeLineFragmentConfig 
	 * with update interval position @param position 
	 * TwitterService is initialized and app widget with progressbar is shown
	 */
	public void applyTimeLineConfiguration(int position) {
		Intent timeLineFetchIntent = createIntentWithAppWidgetId();
		timeLineFetchIntent.setAction(TwitterService.ACTION_TIMELINE_FETCH);
		timeLineFetchIntent.putExtra(WidgetProvider.LIST_NAME,T4JTwitterLoginActivity.getTwitterUserName(this));
		this.startService(timeLineFetchIntent);

		initAlarmManager(position, appWidgetId);
		showAppWidget();
	}

	private void initAlarmManager(int position, int appWidgetId) {
		long updateIntervalInMillis = GeneralUtils
				.getWidgetUpdateInterval(position);
		SessionStore.storeUpdateInterval(this, updateIntervalInMillis,
				appWidgetId);
	}

	/**
	 * method to change TimeLineFragmentConfig with MeListFragment
	 * called from TimeLineFragmentConfig @param position
	 */
	public void applyConfiguration(int position) {
		if (position == 1) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			meListFragment = MeListFragment
					.getNewInstance(updateSpinnerDefaultSelection);
			fragmentTransaction.replace(R.id.configParentFrameLayout,
					meListFragment);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commitAllowingStateLoss();
		}

	}

	/**
	 * @return Intent
	 * creating TwitterService intent
	 * with appWidgetId and notifyNature:either update or notify
	 */
	private Intent createIntentWithAppWidgetId() {
		Intent intent = new Intent(this, TwitterService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.putExtra(WidgetProvider.UPDATE_ACTION, notifyNature);
		return intent;

	}

	/**
	 * method to show app widget from Configuration Activity
	 */
	private void showAppWidget() {
		Intent intent = new Intent();
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(Activity.RESULT_OK, intent);
		this.finish();
	}

}
