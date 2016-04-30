package uk.ac.aber.clg11.temptrack;

import android.content.Context;


/**
 * Interface for specifying the callback method for the background AsyncTask to perform feed synchronisation.
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public interface OnUpdateFeedDataCompleted {

    /**
     * Callback method for handling data results from background feed synchronisation task.
     * @param tempData The collection of results parsed from the downloaded XML file.
     * @param context The current context.
     * @param widgetId The ID of the current widget.
     */
    void OnUpdateFeedDataCompleted(TemperatureFeedData tempData, Context context, int widgetId);

}
