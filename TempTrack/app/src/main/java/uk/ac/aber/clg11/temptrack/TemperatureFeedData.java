package uk.ac.aber.clg11.temptrack;

import java.util.ArrayList;

/**
 * Model class representing a collection of temperature readings.
 *
 * @author Connor Goddard (clg11@aber.ac.uk)
 * @version 1.0
 */
public class TemperatureFeedData {

    private String currentTime;
    private int currentTimeHour = 0;
    private int currentTimeMin = 0;

    private ArrayList<TemperatureReading> temperatureReadings;

    public TemperatureFeedData() {
        this("");
    }

    public TemperatureFeedData(String currentTime) {

        this.currentTime = currentTime;
        this.temperatureReadings = new ArrayList<>();

    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {

        this.currentTime = currentTime;

        // CG - Parse the current time.
        String[] timeVals = currentTime.split(":");

        currentTimeHour = Integer.parseInt(timeVals[0]);
        currentTimeMin = Integer.parseInt(timeVals[1]);

    }

    public ArrayList<TemperatureReading> getTemperatureReadings() {
        return temperatureReadings;
    }

    public void addTemperatureReading(TemperatureReading newTemperatureReading) {

        this.temperatureReadings.add(newTemperatureReading);

    }

    /**
     * Returns the latest temperature reading (i.e. the last reading in the feed collection).
     * @return The latest TemperatureReading instance.
     */
    public TemperatureReading getLatestReading() {

        // CG - Return the temperature value from the last reading parsed from the XML feed.
        return temperatureReadings.get(temperatureReadings.size() - 1);

    }

    /**
     * Returns the maximum temperature value from the collection of readings.
     * @return The maximum temperature value.
     */
    public double getMaxTemperature() {

        double max = Double.MIN_VALUE;

        for (TemperatureReading currentTempReading: temperatureReadings) {

            if (max < currentTempReading.getTemp()) {

                max = currentTempReading.getTemp();

            }
        }

        return max;
    }

    /**
     * Returns the minumum temperature value from the collection of readings.
     * @return The minimum temperature value.
     */
    public double getMinTemperature() {

        double min = Double.MAX_VALUE;

        for (TemperatureReading currentTempReading: temperatureReadings) {

            if (min > currentTempReading.getTemp()) {

                min = currentTempReading.getTemp();

            }
        }

        return min;
    }

    /**
     * Returns a collection of TemperatureReading instances recorded over the specified hour.
     * @param targetHour The hour (24-hr clock) to retrieve temperature readings for.
     * @return A collection of TemperatureReading instances for the specified hour.
     */
    private ArrayList<TemperatureReading> getHourlyTemperatures(int targetHour) {

        ArrayList<TemperatureReading> hourlyTemperatures = new ArrayList<>();

        for (TemperatureReading currentTempReading: temperatureReadings) {

            if (currentTempReading.getHour() == targetHour) {

                hourlyTemperatures.add(currentTempReading);

            }

        }

        return hourlyTemperatures;

    }

    /**
     * Sums over the specified collection of temperature values.
     * @param temperatureReadings The collection of temperature readings to sum together.
     * @return The summed total of temperature values.
     */
    private double sumHourlyTemperatures(ArrayList<TemperatureReading> temperatureReadings) {

        double hourlyTempSum = 0.0;

        for (TemperatureReading currentTempReading: temperatureReadings) {

                hourlyTempSum += currentTempReading.getTemp();

        }

        return hourlyTempSum;

    }

    /**
     * Calculates the average temperature value for the current hour.
     * @return Average temperature value for the current hour.
     */
    public double getHourlyAverageTemperature() {

        // CG - If we have readings < 5 mins into the current hour, we want to use the previous hour's readings.
        int targetHour = (currentTimeMin >= 5) ? currentTimeHour : currentTimeHour--;

        if (targetHour < 0) {
            return 0.0;
        }

        ArrayList<TemperatureReading> hourlyTemps = getHourlyTemperatures(targetHour);

        return sumHourlyTemperatures(hourlyTemps) / hourlyTemps.size();
    }
}
