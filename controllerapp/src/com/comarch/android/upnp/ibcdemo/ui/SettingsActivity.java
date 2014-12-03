/**
 *
 * Copyright 2013-2014 UPnP Forum All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE OR WARRANTIES OF 
 * NON-INFRINGEMENT, ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are 
 * those of the authors and should not be interpreted as representing official 
 * policies, either expressed or implied, by the UPnP Forum.
 *
 **/

package com.comarch.android.upnp.ibcdemo.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.busevent.ActionCollectionEvent;
import com.comarch.android.upnp.ibcdemo.busevent.ControlPointNameChanged;
import com.comarch.android.upnp.ibcdemo.persistence.AccompanyingFilePersistence;
import com.comarch.android.upnp.ibcdemo.persistence.DatabaseHelper;
import com.comarch.android.upnp.ibcdemo.persistence.PreferencesHelper;
import com.comarch.android.upnp.ibcdemo.util.deliverer.ActivityBusDeliverer;
import com.google.common.base.Strings;

public class SettingsActivity extends PreferenceActivity {

    private final ActivityBusDeliverer adapter = new ActivityBusDeliverer(this);
    private AlertDialog mWaitDialog;
    private AccompanyingFilePersistence mAccompanyingFile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
        setContentView(R.layout.preferences_layout);
        setupActionBar();
        
        mAccompanyingFile = new AccompanyingFilePersistence(this);
        
        View header = findViewById(R.id.headerBarSettings);
        header.setBackgroundResource(R.color.statusBarBackgroundMain);
        header.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(getActionBar()!=null){
                getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            //Fixed: http://stackoverflow.com/questions/12276027/how-can-i-return-to-a-parent-activity-correctly
            //NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        adapter.getBus().register(this);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        adapter.getBus().unregister(this);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the device configuration dictates that a simplified, single-pane UI
     * should be shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {

        addPreferencesFromResource(R.xml.preferences);

        bindCPNamePreferenceChange(findPreference(PreferencesHelper.CP_NAME));
        bindLoginPreferenceChange(findPreference(PreferencesHelper.XMPP_LOGIN));
        bindPreferenceSummaryToValue(findPreference(PreferencesHelper.XMPP_SERVER));
        bindPreferenceSummaryToValue(findPreference(PreferencesHelper.XMPP_PORT));
        bindPreferenceSummaryToValue(findPreference(PreferencesHelper.XMPP_PUBSUB));

        Preference myPref = (Preference)findPreference("clear_cache");
        myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                     public boolean onPreferenceClick(Preference preference) {
                        adapter.getBus().post(ActionCollectionEvent.CLEAR);
                        return true; 
                     }
                 });   
        Preference resetDemo = (Preference)findPreference("reset_demo");
        resetDemo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
				mWaitDialog = alertDialogBuilder.setTitle("Please wait...").setMessage("Reseting devices to default state").setCancelable(false).create();
				mWaitDialog.show();
				adapter.getBus().post(ActionCollectionEvent.RESET_DEMO);
				return false;
			}
        });
    }

	/**
     * A preference value change listener that updates the preference's summary to reflect its new value.
     */
    
    private Preference.OnPreferenceChangeListener setSummaryListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private Preference.OnPreferenceChangeListener loginPreferenceListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String value = (String) newValue;
            if(value.matches("^.+@[^@]+$")){
                return setSummaryListener.onPreferenceChange(preference, newValue);
            }else if(Strings.isNullOrEmpty(value)){
                return false;
            }else{
                Toast.makeText(SettingsActivity.this,"Login has to be in format login@domain",Toast.LENGTH_LONG).show();
                return false;
            }
        }
    };
    
    private Preference.OnPreferenceChangeListener cpNamePreferenceListener = new Preference.OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			adapter.getBus().post(new ControlPointNameChanged((String) newValue));
			return setSummaryListener.onPreferenceChange(preference, newValue);
		}
	};
    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary (line of text below
     * the preference title) is updated to reflect the value. The summary is also immediately updated upon calling this method. The exact
     * display format is dependent on the type of preference.
     * 
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(setSummaryListener);

        // Trigger the listener immediately with the preference's
        // current value.
        setSummaryListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }
    
    private void bindLoginPreferenceChange(Preference preference){
        preference.setOnPreferenceChangeListener(loginPreferenceListener);
        loginPreferenceListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }
    
    private void bindCPNamePreferenceChange(Preference preference) {
    	preference.setOnPreferenceChangeListener(cpNamePreferenceListener);
    	cpNamePreferenceListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
		
	}
    
    
    public void onEvent(ActionCollectionEvent event) {
        switch (event) {
        case CLEAR:
        	DatabaseHelper.clearAll(this);
        	mAccompanyingFile.resetAccompanyingContent();
        	
            if(mWaitDialog!=null){
            	mWaitDialog.cancel();
            	mWaitDialog = null;
            }
        case RESET_DEMO:
        case NONE:
            break;
        }
    }
    
}
