package com.demo.widget.twitter.appwidget.config;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import twitter4j.UserList;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.demo.widget.twitter.R;
import com.demo.widget.twitter.connect.Constants;
import com.demo.widget.twitter.connect.T4JTwitterFunctions;
import com.demo.widget.twitter.helpers.GeneralUtils;
import com.demo.widget.twitter.helpers.MeListItem;

/**
 * Configuration Fragment to show Twitter Me Lists
 */
public class MeListFragment extends ConfigFragment {

	private View mainView;
	private ListView meListView;
	private Spinner updateIntervalSpinner;
	private ProgressBar progressBar;

	public Context context;
	private static final int FETCH_COMPLETLE = 1;
	private MeListFetchHandler meListFetchHandler;

	private ArrayList<MeListItem> meLists;
	private AQuery ListAQuery;

	public MeListFragment() {

	}

	/**
	 * @return MeListFragment new instance initialization of MeListFragment with @param
	 *         updateIntervalSpinnerPosition to set on Update Interval Spinner
	 */
	public static MeListFragment getNewInstance(
			int updateIntervalSpinnerPosition) {
		MeListFragment meListFragment = new MeListFragment();
		Bundle params = new Bundle();
		params.putInt(UPDATE_SPINNER_POSITION, updateIntervalSpinnerPosition);
		meListFragment.setArguments(params);
		return meListFragment;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mainView == null) {
			mainView = inflater.inflate(R.layout.melist_config_fragment,
					container, false);
		}
		meListView = (ListView) mainView.findViewById(R.id.meListView);
		updateIntervalSpinner = (Spinner) mainView
				.findViewById(R.id.updateIntervalSpinner);
		progressBar = (ProgressBar) mainView.findViewById(R.id.progressBar);
		return mainView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = getActivity();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		meListFetchHandler = new MeListFetchHandler(this);
		ListAQuery = new AQuery(context);

		fetchTweetsFomMeList();

	}

	/**
	 * After Status is fetched from Twitter populate ListView with it
	 */
	private void populateStatusList() {
		if (meLists != null && meLists.size() > 0) {
			MeListAdapter meListAdapter = new MeListAdapter(context, 0);
			meListView.setAdapter(meListAdapter);
			progressBar.setVisibility(View.GONE);
			meListView.setVisibility(View.VISIBLE);
			meListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					meListItemClick(meLists.get(position).itemId,meLists.get(position).fullName);
				}
			});
		} else {
			progressBar.setVisibility(View.GONE);
			Toast.makeText(context,
					getResources().getString(R.string.error_fetching_tweets),
					Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * @param listItemId
	 * Notify FragmentActivity i.e. ConfigActivity which list item is clicked
	 * for the purpose of showing and populating widget
	 */
	private void meListItemClick(int listItemId,String listName) {
		((ConfigActivity) context).applyMeListConfiguration(
				updateIntervalSpinner.getSelectedItemPosition(), listItemId,listName);

	}

	/**
	 * method to fetch Tweets from Me List with given Twitter User Id using
	 * T4JTwitterFunctions and converting the Status(Twitter4J) to MeListItem
	 * for purpose of saving to database
	 */
	private void fetchTweetsFomMeList() {

		if (GeneralUtils.isConnectedToInternet(context)) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					ArrayList<UserList> userLists = T4JTwitterFunctions
							.fetchMeLists(context, Constants.CONSUMER_KEY,
									Constants.CONSUMER_SECRET);
					if (userLists != null && userLists.size() > 0) {
						meLists = new ArrayList<MeListItem>();
						for (UserList userList : userLists) {
							MeListItem meList = new MeListItem();
							meList.itemId = userList.getId();
							meList.fullName = userList.getFullName();
							meList.description = userList.getDescription();
							meList.memberCount = Integer.toString(userList
									.getMemberCount());
							meList.imageUrl = userList.getUser()
									.getMiniProfileImageURL();
							meLists.add(meList);
						}
						userLists.clear();
					}
					meListFetchHandler.sendEmptyMessage(FETCH_COMPLETLE);
				}
			}).start();
		} else {
			Toast.makeText(context, getString(R.string.no_network),
					Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * Handler to process after Status is fetched
	 */
	static class MeListFetchHandler extends Handler {
		private final WeakReference<MeListFragment> meListFragmentHolder;

		public MeListFetchHandler(MeListFragment meListFragment) {
			meListFragmentHolder = new WeakReference<MeListFragment>(
					meListFragment);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == FETCH_COMPLETLE) {
				MeListFragment meListFragment = meListFragmentHolder.get();
				meListFragment.populateStatusList();
			}
		}

	}

	/**
	 * Adapter for Me List with MeListItem
	 */
	public class MeListAdapter extends ArrayAdapter<MeListItem> {
		private LayoutInflater layoutInflater;

		public MeListAdapter(Context context, int textViewResourceId) {
			super(context, 0);
			layoutInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public MeListItem getItem(int position) {
			return meLists.get(position);
		}

		@Override
		public int getCount() {
			return meLists.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				holder = new Holder();
				convertView = layoutInflater.inflate(R.layout.list_row, parent,
						false);
				holder.tweetHeading = (TextView) convertView
						.findViewById(R.id.tweetHeading);
				holder.tweetContent = (TextView) convertView
						.findViewById(R.id.tweetContent);
				holder.tweetMembers = (TextView) convertView
						.findViewById(R.id.tweetTimeText);

				holder.tweetHeading.setTextAppearance(context,
						R.style.AdListTitleTextStyle);
				holder.tweetContent.setTextAppearance(context,
						R.style.AdListDescTextStyle);
				holder.tweetMembers.setTextAppearance(context,
						R.style.AdListTitleTextStyle);
				holder.tweetMembers.setTextSize(13);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}

			MeListItem meList = getItem(position);
			holder.tweetHeading.setText(meList.fullName);
			holder.tweetContent.setText(meList.description);
			holder.tweetMembers.setText(meList.memberCount + " "
					+ getString(R.string.members));
			//Helper class to fetch images 
			AQuery aquery = ListAQuery.recycle(convertView);
			aquery.recycle(convertView);
			aquery.id(R.id.imageView).image(meList.imageUrl, true, true, 0, 0,
					null, AQuery.FADE_IN);
			return convertView;
		}

	}

	static class Holder {
		public TextView tweetHeading, tweetContent, tweetMembers;
		public ImageView tweetPic;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mainView != null) {
			ViewGroup parentViewGroup = (ViewGroup) mainView.getParent();
			if (parentViewGroup != null) {
				parentViewGroup.removeAllViews();
			}
		}
	}

}
