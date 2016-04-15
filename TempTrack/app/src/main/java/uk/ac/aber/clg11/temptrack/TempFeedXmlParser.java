package uk.ac.aber.clg11.temptrack;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by connorgoddard on 15/04/2016.
 */
public class TempFeedXmlParser {

    // We don't use namespaces
    private static final String ns = null;

    public TemperatureFeedData parse(InputStream in) throws XmlPullParserException, IOException {
        try {

            XmlPullParser parser = Xml.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            parser.nextTag();
            return readFeed(parser);

        } finally {
            in.close();
        }
    }

    private TemperatureFeedData readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {

        TemperatureFeedData tempData = new TemperatureFeedData();

        parser.require(XmlPullParser.START_TAG, ns, "temps");

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            // Starts by looking for the entry tag
            if (name.equals("currentTime")) {

                tempData.setCurrentTime(readCurrentTimeElement(parser));

            } else if (name.equals("reading")) {

                tempData.addTemperatureReading(readTempReadingElement(parser));

            } else {
                skip(parser);
            }
        }

        return tempData;
    }

    private TemperatureReading readTempReadingElement(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "reading");

        String hour = parser.getAttributeValue(null, "hour");
        String min = parser.getAttributeValue(null, "min");
        String temp = parser.getAttributeValue(null, "temp");

        // CG - As we don't have a closing tag on the 'reading' element, we need to jump over this one in order to prevent a crash.
        parser.nextTag();

        parser.require(XmlPullParser.END_TAG, ns, "reading");

        return new TemperatureReading(hour, min, temp);

    }

    private String readCurrentTimeElement(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "currentTime");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "currentTime");
        return title;

    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {

        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;

        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
