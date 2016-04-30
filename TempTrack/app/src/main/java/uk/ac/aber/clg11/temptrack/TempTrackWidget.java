package uk.ac.aber.clg11.temptrack;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents the "controller" for the homescreen widget, providing much of the functionality associated with obtaining temperature feed data and updating the user interface.
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public class TempTrackWidget extends AppWidgetProvider implements OnUpdateFeedDataCompleted {

    public static String REFRESH_BUTTON = "uk.ac.aber.clg11.temptrack.REFRESH_BUTTON";
    public static String SETTINGS_BUTTON = "uk.ac.aber.clg11.temptrack.SETTINGS_BUTTON";
    public static String PREFERENCES_UPDATE = "uk.ac.aber.clg11.temptrack.PREFERENCES_UPDATE";
    public static String NETWORK_CHANGE = "uk.ac.aber.clg11.temptrack.NETWORK_CHANGE";

    public static String URL;

    private static final String TAG = TempTrackWidget.class.getName();

    // CG - Flags for determining the various connection states and permissions for network access.
    private static boolean isWifiConnected = false;
    private static boolean isCellularConnected = false;
    private static boolean isCellularAllowed = true;

    // CG - Flag determining the current metric for the temperature display.
    private static boolean isDisplayCelsius = true;

    // CG - Reference to the latest collection of acquired temperature readings.
    private static TemperatureFeedData widgetFeedData = null;

    // CG - Internal BroadcastReceiver for tracking network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // CG - Initialise the default preference values for the widget.
        // See: http://developer.android.com/guide/topics/ui/settings.html#Defaults for more information.
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        // CG - Always synchronise permissions when starting up the widget, and check network connectivity.
        syncUserPreferences(context);
        updateNetworkStatusFlags(context);

        // There may be multiple widgets active, so update all of them.
        for (int appWidgetId : appWidgetIds) {

            // CG - We want to perform data synchronisation for all widgets in turn.
            performDataSync(context, appWidgetId);

        }

    }

    @Override
    public void onEnabled(Context context) {

        // CG - Portions of this code have been modified from an original source: http://developer.android.com/training/basics/network-ops/managing.html

        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        receiver = new NetworkReceiver();

        // CG - We need to make sure to register the receiver within the current context.
        context.getApplicationContext().registerReceiver(receiver, filter);

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        // CG - Make sure to unregister the listener ONLY WHEN the LAST widget is destroyed.
        // Modified from original source: http://developer.android.com/guide/topics/ui/settings.html#Defaults for more information.
        if (receiver != null) {
            context.getApplicationContext().unregisterReceiver(receiver);
        }

    }


    @Override
    public void onReceive(Context context, Intent intent) {

        // CG - Check if the 'refresh/update' button has been pressed.
        if (REFRESH_BUTTON.equals(intent.getAction())) {

            // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
            // See: http://stackoverflow.com/a/11716757/4768230 for more information.
            Bundle extras = intent.getExtras();

            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            // CG - Trigger a background thread to perform feed synchronisation.
            performDataSync(context, widgetId);

        // CG - Check if the 'settings' button has been pressed.
        } else if (SETTINGS_BUTTON.equals(intent.getAction())) {

            // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
            // See: http://stackoverflow.com/a/11716757/4768230 for more information.
            Bundle extras = intent.getExtras();

            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            // CG - Create a new Intent to display the custom preferences activity.
            Intent settingsIntent = new Intent(context, TempTrackConfigActivity.class);

            // CG - Make sure to pass in the current widget ID, so we can isolate changes.
            settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);

        // CG - Check if a preference has been updated (received from the 'OnSharedPreferenceChangedListener')
        } else if (PREFERENCES_UPDATE.equals(intent.getAction())) {

            Log.i(TAG, "Preferences Updated");

            // Update the user preferences with the current widget.
            syncUserPreferences(context);

            // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
            // See: http://stackoverflow.com/a/11716757/4768230 for more information.
            Bundle extras = intent.getExtras();

            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            // Update the widget UI (e.g. to change the temperature display from celsius to fahrenheit).
            updateWidgetUI(context, widgetId);

        // CG - Check if a change to network connectivity has occurred (received from 'NetworkReceiver')
        } else if (NETWORK_CHANGE.equals(intent.getAction())) {

            Log.i(TAG, "Network Connectivity Changed");
            updateNetworkStatusFlags(context);

        } else {

            super.onReceive(context, intent);

        }

    }


    /**
     * Callback function to receive results from background data synchronisation.
     * @param tempData The temperature feed results.
     * @param context The current context.
     * @param widgetId The ID of the current widget.
     */
    @Override
    public void OnUpdateFeedDataCompleted(TemperatureFeedData tempData, Context context, int widgetId) {

        Log.i(TAG, tempData.getCurrentTime());

        // Update the internal reference to feed results, before updating the widget UI to display the latest values.
        widgetFeedData = tempData;
        this.updateWidgetUI(context, widgetId);

    }

    /**
     * Determines if network access is available, before triggering a new background download request if possible.
     * @param context The current context.
     * @param widgetId The ID of the current widget.
     */
    private void performDataSync(Context context, int widgetId) {

        // CG - We always force this to be null when we go to do a data sync, so we can tell the user if we couldn't connect.
        widgetFeedData = null;

        /* CG - Here we allow a new background synchronisation task to occur in two cases:
         *
         * 1. Cellular access has been granted AND cellular connectivity OR WiFi connectivity is available.
         * 2. Cellular access has not been granted AND WiFi connectivity (only) is available.
         */
        if ((isCellularAllowed && (isCellularConnected || isWifiConnected)) || (!isCellularAllowed && isWifiConnected)) {

            // Fire off the new AsyncTask to download temperature feed data via a background thread.
            new UpdateFeedDataTask(this, context, widgetId).execute(URL);

        // Otherwise if we can't download new data, we need to display an error message to the user.
        } else {

            updateWidgetUI(context, widgetId);

        }

    }

    /**
     * Obtains network connectivity information from ConnectivityManager before updating internal status flags.
     * @param context The current context.
     */
    private void updateNetworkStatusFlags(Context context) {

        // CG - Portions of this code have been modified from an original source: http://developer.android.com/training/basics/network-ops/managing.html

        // CG - Here we use the ConnectivityManager to obtain network status information.
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected()) {
            isWifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            isCellularConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            isWifiConnected = false;
            isCellularConnected = false;
        }

    }

    /**
     * Synchronises persistent application preferences with internal flags representing each preference.
     * @param context The current context.
     */
    private void syncUserPreferences(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        isCellularAllowed = !(sharedPref.getBoolean("wifiOnly", false));

        // CG - In the case that no feed has been selected (highly unlikely), use a default fallback (set to 'TempData1.xml).
        URL = sharedPref.getString("dataFeed", context.getString(R.string.dataFeedDefault));

        // CG - Force the default temperature scale to be in celsius in the unlikely event that we have no preference at all (including a default).
        isDisplayCelsius = sharedPref.getString("dataTempScale", "celsius").equalsIgnoreCase("celsius");

    }

    /**
     * Formats a temperature value (double) to a string representation (1 decimal place).
     * @param temperatureValue The raw temperature value.
     */
    private SpannableStringBuilder formatTempText(double temperatureValue) {

        // CG - We force the String value for the temp to be set to one decimal place for display consistency.
        // CG - To ensure correct rendering across different devices and users, we force the locale to use the device default.
        String temperatureString = String.format(Locale.getDefault(), "%.1f", temperatureValue);

        return formatTempText(temperatureString);

    }

    /**
     * Formats a temperature value (double) to a string representation (1 decimal place).
     * @param tempValueString The temperature value to format.
     */
    private SpannableStringBuilder formatTempText(String tempValueString) {

        String[] words = tempValueString.split("\\.");

        if (words.length >= 2) {

            SpannableStringBuilder builder = new SpannableStringBuilder();

            builder.append(words[0]);

            int start = builder.length();

            builder.append("." + words[1] + "\u00B0");

            // CG - We can use 'RelativeSizeSpan' to make the text for part of our string proportionally smaller or larger.
            // See: http://stackoverflow.com/a/16335416 for more information.
            builder.setSpan(new RelativeSizeSpan(0.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return builder;

        }

        return null;

    }

    /**
     * Updates all information fields located within the widget user interface.
     * @param context The current context.
     * @param widgetId The ID of the current widget.
     */
    public void updateWidgetUI(Context context, int widgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temp_track_widget);

        // CG - Check if we currently have temperature feed data to display.
        if (widgetFeedData != null) {

            // CG - If so, process/format the data and update the UI accordingly.

            double currentTempCelsius = widgetFeedData.getLatestReading().getTemp();

            // CG - As temperature data is in celsius by default, we only need to convert if displaying in fahrenheit.
            double displayTemp = isDisplayCelsius ? currentTempCelsius : convertCelsiusToFahrenheit(currentTempCelsius);

            setDataStatus(true, context, widgetId);

            String minTemp = String.format(Locale.getDefault(), "%.1f", widgetFeedData.getMinTemperature());
            String maxTemp = String.format(Locale.getDefault(), "%.1f", widgetFeedData.getMaxTemperature());
            String avgTemp = String.format(Locale.getDefault(), "%.1f", widgetFeedData.getHourlyAverageTemperature());

            views.setTextViewText(R.id.textTemp, formatTempText(displayTemp));

            views.setTextViewText(R.id.textMin, minTemp + "\u00B0");
            views.setTextViewText(R.id.textMax, maxTemp + "\u00B0");
            views.setTextViewText(R.id.textViewAverage, avgTemp + "\u00B0");

            SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a");
            SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm");

            try {
                Date date = parseFormat.parse(widgetFeedData.getCurrentTime());
                views.setTextViewText(R.id.textTime, displayFormat.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        // CG - Otherwise, display an error message to the user.
        } else {

            setDataStatus(false, context, widgetId);

        }

        // CG - Set up the intents for the 'refresh/update' and 'settings' buttons.

        Intent intent = new Intent(REFRESH_BUTTON);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.btnRefresh, pendingIntent);

        intent = new Intent(SETTINGS_BUTTON);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        pendingIntent = PendingIntent.getBroadcast(context, widgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.btnSettings, pendingIntent);

        // Perform the UI update for the widget.
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(widgetId, views);

    }

    /**
     * Determines whether or not to show or hide temperature information, with an error message shown/hidden in the opposite case.
     * @param isActive Specifies whether to show (true) or to hide (false) temperature information.
     * @param context The current context.
     * @param widgetId The ID of the current widget.
     */
    private void setDataStatus(boolean isActive, Context context, int widgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temp_track_widget);

        if (isActive) {

            views.setViewVisibility(R.id.textTemp, View.VISIBLE);
            views.setViewVisibility(R.id.timeContainer, View.VISIBLE);
            views.setViewVisibility(R.id.statsContainer, View.VISIBLE);

            views.setViewVisibility(R.id.textViewNoData, View.GONE);

        } else {

            views.setViewVisibility(R.id.textTemp, View.GONE);
            views.setViewVisibility(R.id.timeContainer, View.GONE);
            views.setViewVisibility(R.id.statsContainer, View.GONE);

            views.setViewVisibility(R.id.textViewNoData, View.VISIBLE);

        }

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(widgetId, views);
    }

    /**
     * Converts temperature readings from celsius and fahrenheit measures (celsius is default)
     * @param celsius The temperature value in celsius to be converted.
     */
    private double convertCelsiusToFahrenheit(double celsius) {
        return (9.0/5.0) * celsius + 32;
    }

}
