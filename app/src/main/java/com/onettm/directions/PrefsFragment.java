package com.onettm.directions;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.onettm.directions.data.LocationsManager;

import java.util.Collections;

public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        DirectionsApplication.getInstance().loadPref(sharedPreferences);
        final LocationsManager locationsManager = DirectionsApplication.getInstance().getLocationsManager();

        locationsManager.setLocations(Collections.<LocationItem>emptyList());
        locationsManager.setDecisionLocation(null);

        locationsManager.invalidate();
    }
}
