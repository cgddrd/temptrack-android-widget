package uk.ac.aber.clg11.temptrack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * Created by connorgoddard on 17/04/2016.
 */
public class TempTrackConfigFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private static final String TAG = TempTrackConfigFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                Log.d(TAG, "SharedPreferenceChangeListener Fired.");

                final Activity parentActivity = getActivity();

                Intent updateIntent = new Intent(parentActivity, TempTrackWidget.class);
                updateIntent.setAction(TempTrackWidget.PREFERENCES_UPDATE);
                parentActivity.sendBroadcast(updateIntent);

            }

        };

    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }


}
