package uk.ac.aber.clg11.temptrack;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * PreferenceFragment representing a collection of user preference inputs hosted within the PreferencesActivity.
 *
 * Based on code provided in a tutorial available at: http://developer.android.com/guide/topics/ui/settings.html
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public class TempTrackConfigFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private static final String TAG = TempTrackConfigFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // CG - Load in custom preferences from the 'preferences.xml' file.
        addPreferencesFromResource(R.xml.preferences);

        // CG - Create a new 'OnSharedPreferenceChangeListener' to handle preference change events (used to auto-sync preferences within widget).
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                Log.i(TAG, "SharedPreferenceChangeListener Fired.");

                // CG - To send an intent broadcast, we need to access the parent activity (the PreferencesActivity)
                final Activity parentActivity = getActivity();

                // CG - Get a reference to the originating intent from the parent activity.
                Intent intent = parentActivity.getIntent();

                // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
                // See: http://stackoverflow.com/a/11716757/4768230 for more information.
                Bundle extras = intent.getExtras();

                int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

                // CG - Create and fire a new intent that the widget can detect in order to synchronise preference changes.
                Intent updateIntent = new Intent(parentActivity, TempTrackWidget.class);
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
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
