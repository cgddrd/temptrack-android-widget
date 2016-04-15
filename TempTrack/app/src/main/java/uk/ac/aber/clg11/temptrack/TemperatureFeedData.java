package uk.ac.aber.clg11.temptrack;

import java.util.ArrayList;

/**
 * Created by connorgoddard on 15/04/2016.
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

    public TemperatureReading getLatestReading() {

        // CG - Return the temperature value from the last reading parsed from the XML feed.
        return temperatureReadings.get(temperatureReadings.size() - 1);

    }

    public double getMaxTemperature() {

        double max = Double.MIN_VALUE;

        for (TemperatureReading currentTempReading: temperatureReadings) {

            if (max < currentTempReading.getTemp()) {

                max = currentTempReading.getTemp();

            }
        }

        return max;
    }

    public double getMinTemperature() {

        double min = Double.MAX_VALUE;

        for (TemperatureReading currentTempReading: temperatureReadings) {

            if (min > currentTempReading.getTemp()) {

                min = currentTempReading.getTemp();

            }
        }

        return min;
    }

    private ArrayList<TemperatureReading> getHourlyTemperatures(int targetHour) {

        ArrayList<TemperatureReading> hourlyTemperatures = new ArrayList<>();

        for (TemperatureReading currentTempReading: temperatureReadings) {

            if (currentTempReading.getHour() == targetHour) {

                hourlyTemperatures.add(currentTempReading);

            }

        }

        return hourlyTemperatures;

    }

    private double sumHourlyTemperatures(ArrayList<TemperatureReading> temperatureReadings) {

        double hourlyTempSum = 0.0;

        for (TemperatureReading currentTempReading: temperatureReadings) {

                hourlyTempSum += currentTempReading.getTemp();

        }

        return hourlyTempSum;

    }

    public double getHourlyAverageTemperature() {

        int targetHour = (currentTimeMin >= 5) ? currentTimeHour : currentTimeHour--;

        if (targetHour < 0) {
            return 0.0;
        }

        ArrayList<TemperatureReading> hourlyTemps = getHourlyTemperatures(targetHour);

        return sumHourlyTemperatures(hourlyTemps) / hourlyTemps.size();
    }
}
