package uk.ac.aber.clg11.temptrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by connorgoddard on 17/04/2016.
 */
public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = TempTrackWidget.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "NETWORK CHANGED!");

        Intent updateIntent = new Intent(context, TempTrackWidget.class);
        updateIntent.setAction(TempTrackWidget.NETWORK_CHANGE);
        context.sendBroadcast(updateIntent);

    }

}
