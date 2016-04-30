package uk.ac.aber.clg11.temptrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Represents the activity for displaying and managing widget preferences.
 *
 * Based on code provided in a tutorial available at: http://developer.android.com/guide/topics/ui/settings.html
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public class TempTrackConfigActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new TempTrackConfigFragment())
                            .commit();

    }

}
