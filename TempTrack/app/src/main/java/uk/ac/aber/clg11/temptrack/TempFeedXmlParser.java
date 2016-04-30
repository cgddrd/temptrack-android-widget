package uk.ac.aber.clg11.temptrack;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class responsible for parsing Temperature Reading XML feed data.
 *
 * Based on code provided in a tutorial available at: http://developer.android.com/training/basics/network-ops/xml.html
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public class TempFeedXmlParser {

    // CG - XSLT namespaces are not used in this case, so set them all to 'null' when parsing.
    private static final String ns = null;

    /**
     * Creates a new XmlPullParser that is used to parse the XML data contained within the specified InputStream.
     * @param stream The InputStream containing the data representing the XML feed.
     * @return A new TemperatureFeedData model containing zero or more parsed TemperatureReading instances.
     * @throws XmlPullParserException
     * @throws IOException
     */
    public TemperatureFeedData parse(InputStream stream) throws XmlPullParserException, IOException {
        try {

            XmlPullParser parser = Xml.newPullParser();

            // CG - In this case, we are not using XSLT namespaces.
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);

            // CG - We need to skip the opening document tag for the XML file prior to beginning parsing.
            parser.nextTag();
            return readFeed(parser);

        } finally {
            stream.close();
        }
    }

    /**
     * Reads the XML feed data, and converts the provided readings into a collection of 'TemperatureReading' models.
     * @param parser The XmlPullParser instance that will undertake the parsing process.
     * @return A new TemperatureFeedData model containing zero or more parsed TemperatureReading instances.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private TemperatureFeedData readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {

        TemperatureFeedData tempData = new TemperatureFeedData();

        // CG - Locate the parent tag for temperature reading data.
        parser.require(XmlPullParser.START_TAG, ns, "temps");

        // CG - Loop through all of the child elements representing each temperature reading.
        while (parser.next() != XmlPullParser.END_TAG) {

            // CG - Handle errors related to mismatching/corrupt XML tags.
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // CG - Get the name of the opening XML tag.
            String tagName = parser.getName();

            // CG - Handle parsing of different XML tags.
            if (tagName.equals("currentTime")) {

                tempData.setCurrentTime(readCurrentTimeElement(parser));

            } else if (tagName.equals("reading")) {

                tempData.addTemperatureReading(readTempReadingElement(parser));

            } else {
                skip(parser);
            }
        }

        return tempData;
    }

    /**
     * Handles parsing of 'reading' XML tags.
     * @param parser The XmlPullParser instance that will undertake the parsing process.
     * @return A new TemperatureReading model populated with reading data.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private TemperatureReading readTempReadingElement(XmlPullParser parser) throws XmlPullParserException, IOException {

        // CG - Access the opening tag.
        parser.require(XmlPullParser.START_TAG, ns, "reading");

        // CG - Parse the XML tag attributes to extract reading information.
        String hour = parser.getAttributeValue(null, "hour");
        String min = parser.getAttributeValue(null, "min");
        String temp = parser.getAttributeValue(null, "temp");

        // CG - As we don't have a closing tag on the 'reading' element, we need to jump over this one in order to prevent a crash.
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, ns, "reading");

        return new TemperatureReading(hour, min, temp);

    }

    /**
     * Handles parsing of 'currentTime' XML tags.
     * @param parser The XmlPullParser instance that will undertake the parsing process.
     * @return A new String representing the current time for the reading.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private String readCurrentTimeElement(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "currentTime");

        String title = readText(parser);

        parser.require(XmlPullParser.END_TAG, ns, "currentTime");

        return title;

    }

    /**
     * Extracts textual content contained within a single pair of XML tags.
     * @param parser The XmlPullParser instance that will undertake the parsing process.
     * @return A new String representing the text value for the current XML element.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {

        String result = "";

        // CG - Check if we can extract text content from the current XML element.
        if (parser.next() == XmlPullParser.TEXT) {

            // CG - If so, extract the text content.
            result = parser.getText();
            parser.nextTag();

        }

        return result;

    }

    /**
     * Skips XML tags (can be single-layer or hierarchical) that the parser is not interested in.
     *
     * Modified from original source: http://developer.android.com/training/basics/network-ops/xml.html#skip
     *
     * @param parser The XmlPullParser instance that will undertake the parsing process.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {

        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;

        // CG - Handle cases where the tag we wish to skip has children (we want to skip all of those as well).
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
