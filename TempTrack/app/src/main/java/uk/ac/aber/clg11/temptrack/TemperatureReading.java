package uk.ac.aber.clg11.temptrack;

import android.content.res.Resources;
import android.util.Log;

import java.util.Locale;

/**
 * Model class representing an individual temperature reading.
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public class TemperatureReading {

    private int hour;
    private int min;
    private double temp;

    private static final String TAG = TemperatureReading.class.getName();

    public TemperatureReading(int hour, int min, double temp) {

        this.setHour(hour);
        this.setMin(min);
        this.setTemp(temp);

    }

    public TemperatureReading(String hour, String min, String temp) {

        try {

            this.hour = Integer.parseInt(hour);
            this.min = Integer.parseInt(min);
            this.temp = Double.parseDouble(temp);

        } catch (NumberFormatException nFE) {

            Log.e(TAG, "Error whilst creating new TemperatureReading instance - " + nFE.getLocalizedMessage() + "\n\nAll values set to '-1'.");

            this.hour = -1;
            this.min = -1;
            this.temp = -1.0;

        }

    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public double getTemp() {
        return temp;
    }

    /**
     * Converts a temperature value (double) to String representation.
     * @return The String representation of a temperature value.
     */
    public String getTempStringValue() {

        // CG - We force the String value for the temp to be set to one decimal place for display consistency.
        // CG - To ensure correct rendering across different devices and users, we force the locale to use the device default.
        return String.format(Locale.getDefault(), "%.1f", temp);

    }

    public void setTemp(double temp) {
        this.temp = temp;
    }
}
