package com.demo.widget.twitter.appwidget.config;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Spinner;

/**
 * Parent class of Configuration Fragments : MeListFragment & TimeLineFragmentConfig
 * its sole purpose is to set UpdateInterval default position on spinner
 * 
 */
public class ConfigFragment extends Fragment {
	
	public int updateIntervalDefaultPosition=0;
	public static final String UPDATE_SPINNER_POSITION="updateIntervalPos";
	public Spinner updateIntervalSpinner;
	
	public ConfigFragment(){
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle params = getArguments();
		if(params !=null && params.containsKey(UPDATE_SPINNER_POSITION)){
			updateIntervalDefaultPosition = params.getInt(UPDATE_SPINNER_POSITION);
		}else if(savedInstanceState !=null && savedInstanceState.containsKey(UPDATE_SPINNER_POSITION)){
			updateIntervalDefaultPosition=savedInstanceState.getInt(UPDATE_SPINNER_POSITION);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(UPDATE_SPINNER_POSITION, updateIntervalDefaultPosition);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i("updateInterval position",String.valueOf(updateIntervalDefaultPosition));
		if(updateIntervalSpinner !=null){
			Log.i("updateInterval position 0",String.valueOf(updateIntervalDefaultPosition));
			updateIntervalSpinner.setSelection(updateIntervalDefaultPosition);
		}
	}
}
