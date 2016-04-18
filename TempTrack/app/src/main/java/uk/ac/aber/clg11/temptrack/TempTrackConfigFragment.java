package uk.ac.aber.clg11.temptrack;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
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

                Intent intent = parentActivity.getIntent();

                // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
                // See: http://stackoverflow.com/a/11716757/4768230 for more information.
                Bundle extras = intent.getExtras();

                int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

                Log.d("TWAT", String.valueOf(widgetId));

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
