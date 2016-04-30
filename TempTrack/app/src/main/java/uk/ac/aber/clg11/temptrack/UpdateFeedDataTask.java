package uk.ac.aber.clg11.temptrack;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParserException;

/**
 * Instance of AsyncTask responsible for performing feed synchronisation and XML parsing within a new background thread.
 * Returns results back to the main UI thread for presentation.
 *
 * Based on code provided in a tutorial available at: http://developer.android.com/training/basics/network-ops/xml.html
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public class UpdateFeedDataTask extends AsyncTask<String, Void, TemperatureFeedData> {

    private static final String TAG = UpdateFeedDataTask.class.getName();

    private OnUpdateFeedDataCompleted delegate;
    private Context context;
    private int widgetId;

    public UpdateFeedDataTask(OnUpdateFeedDataCompleted delegate, Context context, int widgetId) {

        this.delegate = delegate;
        this.context = context;
        this.widgetId = widgetId;

    }

    /**
     * AsyncTask override method used to perform feed download and parsing within a new background thread.
     * @param urls Zero or more urls to process (varargs parameter is a requirement of AsyncTask - only the FIRST URL is ever used).
     * @return A new TemperatureFeedData model containing zero or more parsed TemperatureReading instances.
     */
    @Override
    protected TemperatureFeedData doInBackground(String... urls) {

        try {

            Log.i(TAG, "Performing XML feed download.");
            return loadXmlFromNetwork(urls[0]);

        } catch (IOException e) {

            Log.e(TAG, "Error whilst downloading XML feed from URL - " + e.getLocalizedMessage());

        } catch (XmlPullParserException e) {

            Log.e(TAG, "Error whilst parsing downloaded XML - " + e.getLocalizedMessage());
            e.printStackTrace();

        }

        return null;

    }

    /**
     * AsyncTask method used to trigger the callback method for passing results back to the main UI thread.
     * @param tempData The TemperatureFeedData model containing zero or more TemperatureReading instances parsed from the acquired XML feed.
     */
    @Override
    protected void onPostExecute(TemperatureFeedData tempData) {

        // CG - Call the callback/delegate function hosted on the main UI thread.
        this.delegate.OnUpdateFeedDataCompleted(tempData, context, widgetId);

    }

    /**
     * Attempts to initiate a download request for the latest version of XML feed data, before attempting to parse and return the results.
     * @param urlString The URL specifying the remote location of the XML feed.
     * @return A new TemperatureFeedData model containing zero or more parsed TemperatureReading instances.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private TemperatureFeedData loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {

        InputStream stream = null;
        TemperatureFeedData tempData = null;
        TempFeedXmlParser parser = new TempFeedXmlParser();

        try {
            // CG - Attempt to initiate the download of the XML feed data.
            stream = initiateFileDownloadRequest(urlString);

            // CG - If we are able to download the file successfully, go ahead and parse the XML data.
            tempData = parser.parse(stream);

        } finally {

            // CG - Regardless of the success/failure of the download request, we must always close the stream for efficiency reasons.
            if (stream != null) {
                stream.close();
            }
        }

        return tempData;
    }

    /**
     * Performs the download of data located at the specified URL.
     * @param urlString The URL of the remote data location.
     * @return An InputStream containing the downloaded data, or empty.
     * @throws IOException
     */
    private InputStream initiateFileDownloadRequest(String urlString) throws IOException {

        URL url = new URL(urlString);

        // CG - Create a new HTTP connection to the specified remote resource.
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);

        // CG - We want to use a GET request for file downloads.
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        // CG - Initiate the download request.
        conn.connect();

        return conn.getInputStream();

    }

}
