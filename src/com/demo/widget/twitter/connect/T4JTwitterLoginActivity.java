package com.demo.widget.twitter.connect;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.demo.widget.twitter.R;

public class T4JTwitterLoginActivity extends Activity {

	public static final int TWITTER_LOGIN_RESULT_CODE_SUCCESS = 1;
	public static final int TWITTER_LOGIN_RESULT_CODE_FAILURE = 2;

	public static final String TWITTER_CONSUMER_KEY = "twitter_consumer_key";
	public static final String TWITTER_CONSUMER_SECRET = "twitter_consumer_secret";

	private WebView twitterLoginWebView;
	private ProgressBar mProgressDialog;
	public String twitterConsumerKey;
	public String twitterConsumerSecret;

	private static Twitter twitter;
	private static RequestToken requestToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_login);
		
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		twitterConsumerKey = getIntent().getStringExtra(TWITTER_CONSUMER_KEY);
		twitterConsumerSecret = getIntent().getStringExtra(
				TWITTER_CONSUMER_SECRET);
		if (twitterConsumerKey == null || twitterConsumerSecret == null) {
			Log.e(Constants.TAG,
					"ERROR: Consumer Key and Consumer Secret required!");
			T4JTwitterLoginActivity.this
					.setResult(TWITTER_LOGIN_RESULT_CODE_FAILURE);
			T4JTwitterLoginActivity.this.finish();
		}

		

		mProgressDialog = (ProgressBar) findViewById(R.id.authorize_webview_progress);
		mProgressDialog.setProgress(0);
		mProgressDialog.setVisibility(View.VISIBLE);
		;

		twitterLoginWebView = (WebView) findViewById(R.id.twitter_login_web_view);
		twitterLoginWebView
				.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		twitterLoginWebView.getSettings().setJavaScriptEnabled(true);
		twitterLoginWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				if(mProgressDialog.getVisibility()==View.GONE)
					mProgressDialog.setVisibility(View.VISIBLE);
				mProgressDialog.setProgress(progress);
				if (progress == 100)
					mProgressDialog.setVisibility(View.GONE);
			}
		});

		twitterLoginWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(Constants.TWITTER_CALLBACK_URL)) {
					Uri uri = Uri.parse(url);
					T4JTwitterLoginActivity.this.saveAccessTokenAndFinish(uri);
					return true;
				}
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);

				
			}
		});

		Log.d(Constants.TAG, "ASK OAUTH");
		askOAuth();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void saveAccessTokenAndFinish(final Uri uri) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String verifier = uri
						.getQueryParameter(Constants.IEXTRA_OAUTH_VERIFIER);
				try {
					SharedPreferences sharedPrefs = getSharedPreferences(
							Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);
					Editor e = sharedPrefs.edit();
					e.putString(Constants.PREF_KEY_TOKEN,
							accessToken.getToken());
					e.putString(Constants.PREF_KEY_SECRET,
							accessToken.getTokenSecret());
					e.putString(Constants.USER_NAME,twitter.showUser(twitter.getId()).getName());
					e.commit();
					Log.d(Constants.TAG, "TWITTER LOGIN SUCCESS!!!");
					T4JTwitterLoginActivity.this
							.setResult(TWITTER_LOGIN_RESULT_CODE_SUCCESS);
				} catch (Exception e) {
					e.printStackTrace();
					if (e.getMessage() != null)
						Log.e(Constants.TAG, e.getMessage());
					else
						Log.e(Constants.TAG, "ERROR: Twitter callback failed");
					T4JTwitterLoginActivity.this
							.setResult(TWITTER_LOGIN_RESULT_CODE_FAILURE);
				}
				T4JTwitterLoginActivity.this.finish();
			}
		}).start();
	}

	// ====== TWITTER HELPER METHODS ======

	public static boolean isConnected(Context ctx) {
		SharedPreferences sharedPrefs = ctx.getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPrefs.getString(Constants.PREF_KEY_TOKEN, null) != null;
	}

	public static void logOutOfTwitter(Context ctx) {
		SharedPreferences sharedPrefs = ctx.getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor e = sharedPrefs.edit();
		e.putString(Constants.PREF_KEY_TOKEN, null);
		e.putString(Constants.PREF_KEY_SECRET, null);
		e.commit();
	}

	public static String getAccessToken(Context ctx) {
		SharedPreferences sharedPrefs = ctx.getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPrefs.getString(Constants.PREF_KEY_TOKEN, null);
	}

	public static String getAccessTokenSecret(Context ctx) {
		SharedPreferences sharedPrefs = ctx.getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPrefs.getString(Constants.PREF_KEY_SECRET, null);
	}
	
	public static String getTwitterUserName(Context context){
		SharedPreferences sharedPrefs = context.getSharedPreferences(Constants.PREFERENCE_NAME,Context.MODE_PRIVATE);
		return sharedPrefs.getString(Constants.USER_NAME, context.getResources().getString(R.string.tweets));
	}

	private void askOAuth() {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(twitterConsumerKey);
		configurationBuilder.setOAuthConsumerSecret(twitterConsumerSecret);
		Configuration configuration = configurationBuilder.build();
		twitter = new TwitterFactory(configuration).getInstance();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					requestToken = twitter
							.getOAuthRequestToken(Constants.TWITTER_CALLBACK_URL);
				} catch (Exception e) {
					final String errorString = e.toString();
					T4JTwitterLoginActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// mProgressDialog.cancel();
							mProgressDialog.setProgress(0);
							Toast.makeText(T4JTwitterLoginActivity.this,
									errorString.toString(), Toast.LENGTH_SHORT)
									.show();
							finish();
						}
					});
					e.printStackTrace();
					return;
				}

				T4JTwitterLoginActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.d(Constants.TAG, "LOADING AUTH URL");
						twitterLoginWebView.loadUrl(requestToken
								.getAuthenticationURL());
					}
				});
			}
		}).start();
	}
}
