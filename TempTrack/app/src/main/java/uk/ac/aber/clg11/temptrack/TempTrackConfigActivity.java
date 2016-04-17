package uk.ac.aber.clg11.temptrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class TempTrackConfigActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_temp_track_config);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new TempTrackConfigFragment())
                .commit();

//        // Array of choices
//        String colors[] = {"Red","Blue","White","Yellow","Black", "Green","Purple","Orange","Grey"};
//
//        // Selection of the spinner
//        Spinner spinner = (Spinner) findViewById(R.id.spinFeed);
//
//        // Application of the Array to the Spinner
//        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colors);
//        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
//        spinner.setAdapter(spinnerArrayAdapter);


    }

}
