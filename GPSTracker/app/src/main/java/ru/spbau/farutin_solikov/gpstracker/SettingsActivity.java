package ru.spbau.farutin_solikov.gpstracker;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
	}
	
	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private static Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			preference.setSummary(value.toString());
			
			SharedPreferences sharedPreferences = preference.getContext().getSharedPreferences(Controller.PREF_FILE, MODE_PRIVATE);
			SharedPreferences.Editor e = sharedPreferences.edit();
			e.putString(preference.getKey(), value.toString());
			e.apply();
			
			return true;
		}
	};
	
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}
		
	private static void setBooleanPreferenceChangeListener(Preference preference) {
		preference.setOnPreferenceChangeListener(onPreferenceChangeListener);
		
		onPreferenceChangeListener.onPreferenceChange(preference,
				PreferenceManager
						.getDefaultSharedPreferences(preference.getContext())
						.getBoolean(preference.getKey(), true));
	}
	
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this);
	}
	
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.pref_headers, target);
	}
	
	/**
	 * This method stops fragment injection in malicious applications.
	 * Make sure to deny any unknown fragments here.
	 */
	protected boolean isValidFragment(String fragmentName) {
		return PreferenceFragment.class.getName().equals(fragmentName)
				|| UserIDPreferenceFragment.class.getName().equals(fragmentName)
				|| NotificationPreferenceFragment.class.getName().equals(fragmentName);
	}
		
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);
			setHasOptionsMenu(true);
			
			setBooleanPreferenceChangeListener(findPreference(getString(R.string.preference_notifications_new_message)));
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class UserIDPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_user_id);
			setHasOptionsMenu(true);
			
			SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Controller.PREF_FILE, MODE_PRIVATE);
			findPreference(getString(R.string.preference_user_id)).setSummary(sharedPreferences.getString(getString(R.string.preference_user_id), ""));
			
			Preference quit = findPreference(getString(R.string.preference_quit));
			quit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					
					builder.setPositiveButton(R.string.title_button_change, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Controller.PREF_FILE, MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.remove(getString(R.string.preference_user_id));
							editor.apply();
					
							startActivity(new Intent(getActivity(), LoginActivity.class));
						}
					});
					
					builder.setNegativeButton(R.string.title_button_cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							
						}
					});
					
					builder.setTitle(R.string.title_dialog_change);
					builder.create().show();
					
					return true;
				}
			});
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
}
