package uk.ac.aber.clg11.temptrack;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
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

    public static String WIDGET_BUTTON = "uk.ac.aber.clg11.temptrack.REFRESH_BUTTON";
    public static final String URL = "http://users.aber.ac.uk/aos/CSM22/temp1data.php";
    private static final String TAG = DownloadXmlTask.class.getName();

    private TemperatureFeedData widgetFeedData = null;

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

        String minTemp = String.format(Locale.getDefault(), "%.1f", widgetFeedData.getMinTemperature());
        String maxTemp = String.format(Locale.getDefault(), "%.1f", widgetFeedData.getMaxTemperature());
        String avgTemp = String.format(Locale.getDefault(), "%.1f", widgetFeedData.getHourlyAverageTemperature());

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temp_track_widget);

        views.setTextViewText(R.id.textTemp, formatTempText(widgetFeedData.getLatestReading().getTempStringValue()));
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

        Intent intent = new Intent(WIDGET_BUTTON);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.btnRefresh, pendingIntent);

        //ComponentName thisWidget = new ComponentName(context, TempTrackWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        manager.updateAppWidget(widgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            new DownloadXmlTask(this, context, appWidgetId).execute(URL);

        }

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);

        if (WIDGET_BUTTON.equals(intent.getAction())) {

            // CG - We use the 'widgetId' to distinguish click events between multiple instances of the same widget.
            // See: http://stackoverflow.com/a/11716757/4768230 for more information.
            Bundle extras = intent.getExtras();

            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            //Toast.makeText(context, "activated", Toast.LENGTH_LONG).show();

            new DownloadXmlTask(this, context, widgetId).execute(URL);


        }

    }

    @Override
    public void onAsyncTaskCompleted(TemperatureFeedData tempData, Context context, int widgetId) {

        Log.d(TAG, tempData.getCurrentTime());

        this.widgetFeedData = tempData;
        this.updateWidgetUI(context, widgetId);

    }
}

