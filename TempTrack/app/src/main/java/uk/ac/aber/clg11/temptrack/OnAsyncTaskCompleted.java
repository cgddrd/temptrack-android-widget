package uk.ac.aber.clg11.temptrack;

import android.content.Context;

/**
 * Created by connorgoddard on 15/04/2016.
 */
public interface OnAsyncTaskCompleted {

    void onAsyncTaskCompleted(TemperatureFeedData tempData, Context context, int widgetId);

}
