package com.ltm.runningtracker.util.parser;

import static com.ltm.runningtracker.util.Constants.COLD_STRING;
import static com.ltm.runningtracker.util.Constants.FREEZING_STRING;
import static com.ltm.runningtracker.util.Constants.HOT_STRING;
import static com.ltm.runningtracker.util.Constants.MILD_STRING;
import static com.ltm.runningtracker.util.Constants.WARM_STRING;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * This class contains the enumeration of weather types, and handles conversion from integer to
 * weatherType, and from weatherType to String.
 */
public class WeatherParser {

  public enum WeatherClassifier {
    FREEZING(FREEZING_STRING, 0), COLD(COLD_STRING, 1), MILD(MILD_STRING, 2), WARM(WARM_STRING,
        3), HOT(HOT_STRING, 4);

    private String weatherDefinition;
    private int value;
    private static Map map = new HashMap<>();

    WeatherClassifier(String brand, int value) {
      this.weatherDefinition = brand;
      this.value = value;
    }

    static {
      for (WeatherClassifier weatherClassifier : WeatherClassifier.values()) {
        map.put(weatherClassifier.value, weatherClassifier);
      }
    }

    public static WeatherClassifier valueOf(int weatherClassifier) {
      return (WeatherClassifier) map.get(weatherClassifier);
    }

    public int getValue() {
      return value;
    }

    public static int getNum() {
      return WeatherClassifier.values().length;
    }

    @NotNull
    @Override
    public String toString() {
      return weatherDefinition;
    }
  }

  /**
   * Weathers are arbitrarily defined as the following Freezing = Less than 5°C; Cold = Between 5°C
   * and 10°C; Mild = Between 10°C and 20°C; Warm = Between 20°C and 35°C; Hot = Above 35°C
   *
   * @param temperature as float
   * @return WeatherClassifier which falls within range of temperature
   */
  public static WeatherClassifier parseTemperatureToClassifier(float temperature) {
    if (temperature >= 35f) {
      return WeatherClassifier.HOT;
    }
    if (temperature < 35f && temperature >= 20f) {
      return WeatherClassifier.WARM;
    }
    if (temperature < 20f && temperature >= 10f) {
      return WeatherClassifier.MILD;
    }
    if (temperature < 10f && temperature >= 5f) {
      return WeatherClassifier.COLD;
    }
    if (temperature < 5f) {
      return WeatherClassifier.FREEZING;
    }

    return null;
  }

}
