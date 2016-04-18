package uk.ac.aber.clg11.temptrack;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class TempTrackWidget extends AppWidgetProvider implements OnAsyncTaskCompleted {

    public static String REFRESH_BUTTON = "uk.ac.aber.clg11.temptrack.REFRESH_BUTTON";
    public static String SETTINGS_BUTTON = "uk.ac.aber.clg11.temptrack.SETTINGS_BUTTON";
    public static String PREFERENCES_UPDATE = "uk.ac.aber.clg11.temptrack.PREFERENCES_UPDATE";
    public static String NETWORK_CHANGE = "uk.ac.aber.clg11.temptrack.NETWORK_CHANGE";

    public static String URL;

    private static final String TAG = TempTrackWidget.class.getName();

    private static boolean isWifiConnected = false;
    private static boolean isCellularConnected = false;

    private static boolean isCellularAllowed = true;

    private static boolean isDisplayCelsius = true;

    private static TemperatureFeedData widgetFeedData = null;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // CG - Initialise the default preference values for the widget.
        // See: http://developer.android.com/guide/topics/ui/settings.html#Defaults for more information.
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        syncUserPreferences(context);
        updateNetworkStatusFlags(context);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            performDataSync(context, appWidgetId);

        }

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        context.getApplicationContext().registerReceiver(receiver, filter);

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        if (receiver != null) {
            context.getApplicationContext().unregisterReceiver(receiver);
        }

    }


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, intent.getAction());

        if (REFRESH_BUTTON.equals(intent.getAction())) {

            // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
            // See: http://stackoverflow.com/a/11716757/4768230 for more information.
            Bundle extras = intent.getExtras();

            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            performDataSync(context, widgetId);


        } else if (SETTINGS_BUTTON.equals(intent.getAction())) {

            // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
            // See: http://stackoverflow.com/a/11716757/4768230 for more information.
            Bundle extras = intent.getExtras();

            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            Intent settingsIntent = new Intent(context, TempTrackConfigActivity.class);
            settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);

        } else if (PREFERENCES_UPDATE.equals(intent.getAction())) {

            Log.d(TAG, "Preferences Updated!!");

            syncUserPreferences(context);

            Log.d(TAG, String.valueOf(isCellularAllowed));
            Log.d(TAG, String.valueOf(isCellularConnected));
            Log.d(TAG, String.valueOf(isWifiConnected));

            // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
            // See: http://stackoverflow.com/a/11716757/4768230 for more information.
            Bundle extras = intent.getExtras();

            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            updateWidgetUI(context, widgetId);

        } else if (NETWORK_CHANGE.equals(intent.getAction())) {

            Log.d(TAG, "NETWORK CHANGE WIDGET");

            updateNetworkStatusFlags(context);

        } else {

            super.onReceive(context, intent);

        }

    }

    @Override
    public void onAsyncTaskCompleted(TemperatureFeedData tempData, Context context, int widgetId) {

        Log.d(TAG, tempData.getCurrentTime());

        this.widgetFeedData = tempData;
        this.updateWidgetUI(context, widgetId);

    }

    private void performDataSync(Context context, int appWidgetId) {

        // CG - We always force this to be null when we go to do a data sync, so we can tell the user if we couldn't connect.
        widgetFeedData = null;

        if ((isCellularAllowed && (isCellularConnected || isWifiConnected)) || (!isCellularAllowed && isWifiConnected)) {

            new DownloadXmlTask(this, context, appWidgetId).execute(URL);

        } else {

            Log.d(TAG, "No download, updating UI anyway.");
            updateWidgetUI(context, appWidgetId);

        }

    }

    private void updateNetworkStatusFlags(Context context) {

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

    private void syncUserPreferences(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        isCellularAllowed = !(sharedPref.getBoolean("wifiOnly", false));

        URL = sharedPref.getString("dataFeed", context.getString(R.string.dataFeedDefault));

        // Force the default temperature scale to be in celsius in the unlikely event that we have no preference at all (including a default).
        isDisplayCelsius = sharedPref.getString("dataTempScale", "celsius").equalsIgnoreCase("celsius");

    }

    private SpannableStringBuilder formatTempText(double temperatureValue) {

        // CG - We force the String value for the temp to be set to one decimal place for display consistency.
        // CG - To ensure correct rendering across different devices and users, we force the locale to use the device default.
        String temperatureString = String.format(Locale.getDefault(), "%.1f", temperatureValue);

        return formatTempText(temperatureString);

    }

    private SpannableStringBuilder formatTempText(String tempValueString) {

        String[] words = tempValueString.split("\\.");

        if (words.length >= 2) {

            SpannableStringBuilder builder = new SpannableStringBuilder();

            builder.append(words[0]);

            int start = builder.length();

            builder.append("." + words[1] + "\u00B0");

            builder.setSpan(new RelativeSizeSpan(0.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return builder;

        }

        return null;

    }

    public void updateWidgetUI(Context context, int widgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temp_track_widget);

        if (widgetFeedData != null) {

            double currentTempCelsius = widgetFeedData.getLatestReading().getTemp();
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

        } else {

            setDataStatus(false, context, widgetId);

        }

        Intent intent = new Intent(REFRESH_BUTTON);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.btnRefresh, pendingIntent);

        intent = new Intent(SETTINGS_BUTTON);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        pendingIntent = PendingIntent.getBroadcast(context, widgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.btnSettings, pendingIntent);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        manager.updateAppWidget(widgetId, views);

    }

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

    private double convertCelsiusToFahrenheit(double celsius) {
        return (9.0/5.0) * celsius + 32;
    }

}
