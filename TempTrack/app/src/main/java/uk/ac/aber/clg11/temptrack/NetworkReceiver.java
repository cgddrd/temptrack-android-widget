package uk.ac.aber.clg11.temptrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Instance of BroadcastReceiver responsible to notifying the widget of changes to network connectivity.
 *
 * Based on code provided in a tutorial available at: http://developer.android.com/training/basics/network-ops/managing.html
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Network connectivity changed. Notifying widget.");

        Intent updateIntent = new Intent(context, TempTrackWidget.class);
        updateIntent.setAction(TempTrackWidget.NETWORK_CHANGE);
        context.sendBroadcast(updateIntent);

    }

}
