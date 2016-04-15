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
 * Created by connorgoddard on 15/04/2016.
 */
public class DownloadXmlTask extends AsyncTask<String, Void, TemperatureFeedData> {

    private static final String TAG = DownloadXmlTask.class.getName();

    private OnAsyncTaskCompleted delegate;
    private Context context;
    private int widgetId;

    public DownloadXmlTask(OnAsyncTaskCompleted delegate, Context context, int widgetId) {

        this.delegate = delegate;
        this.context = context;
        this.widgetId = widgetId;

    }

    @Override
    protected TemperatureFeedData doInBackground(String... urls) {

        try {

            Log.d(TAG, "Performing download...");
            return loadXmlFromNetwork(urls[0]);

        } catch (IOException e) {

            Log.e(TAG, "Error whilst downloading XML feed from URL - " + e.getLocalizedMessage());

        } catch (XmlPullParserException e) {

            Log.e(TAG, "Error whilst parsing downloaded XML - " + e.getLocalizedMessage());
            e.printStackTrace();

        }

        return null;

    }

    @Override
    protected void onPostExecute(TemperatureFeedData tempData) {

        this.delegate.onAsyncTaskCompleted(tempData, context, widgetId);

    }

    private TemperatureFeedData loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {

        InputStream stream = null;
        TemperatureFeedData tempData = null;
        TempFeedXmlParser parser = new TempFeedXmlParser();

        try {

            stream = downloadUrl(urlString);
            tempData = parser.parse(stream);

        } finally {

            if (stream != null) {
                stream.close();
            }
        }

        return tempData;
    }


    private InputStream downloadUrl(String urlString) throws IOException {

        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);

        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        // Starts the query
        conn.connect();

        return conn.getInputStream();

    }

}
