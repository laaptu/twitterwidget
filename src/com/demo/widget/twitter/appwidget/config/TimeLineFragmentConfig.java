package com.demo.widget.twitter.appwidget.config;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

import com.demo.widget.twitter.R;

/**
 * Configuration Fragment to show TimeLine on appwidget contains spinner and
 * button
 * 
 */
public class TimeLineFragmentConfig extends ConfigFragment {

	private View mainView;
	private Spinner tweetListSpinner;
	private Button applyButton;
	private Context context;

	public TimeLineFragmentConfig() {

	}

	/**
	 * @param updateIntervalSpinnerPosition
	 * @return Initialization of TimeLineFragment with update interval default
	 *         position
	 */
	public static TimeLineFragmentConfig getNewInstance(
			int updateIntervalSpinnerPosition) {
		TimeLineFragmentConfig timeLineFragmentConfig = new TimeLineFragmentConfig();
		Bundle params = new Bundle();
		params.putInt(UPDATE_SPINNER_POSITION, updateIntervalSpinnerPosition);
		Log.i("Time Line Fragment spinner position",
				String.valueOf(updateIntervalSpinnerPosition));
		timeLineFragmentConfig.setArguments(params);
		return timeLineFragmentConfig;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mainView == null) {
			mainView = inflater.inflate(R.layout.timeline_config_fragment,
					container, false);
		}
		updateIntervalSpinner = (Spinner) mainView
				.findViewById(R.id.updateIntervalSpinner);
		tweetListSpinner = (Spinner) mainView
				.findViewById(R.id.tweetListSpinner);
		applyButton = (Button) mainView.findViewById(R.id.applyBtn);
		tweetListSpinner.setSelection(0);
		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		applyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onApplyButtonClick();
			}
		});
		tweetListSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						onSpinnerItemSelected(position);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
	}

	/**
	 * Method to call ConfigActivity @ button click for widget show and
	 * population
	 */
	private void onApplyButtonClick() {
		((ConfigActivity) context)
				.applyTimeLineConfiguration(updateIntervalSpinner
						.getSelectedItemPosition());
	}

	/**
	 * @param index
	 *            Method to switch TimeLineFragmentConfig with MeListFragmnet
	 */
	private void onSpinnerItemSelected(int index) {
		if (index == 1) {
			tweetListSpinner.setSelection(0);
			((ConfigActivity) context).applyConfiguration(1);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = getActivity();
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
