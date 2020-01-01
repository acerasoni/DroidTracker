package com.ltm.runningtracker.util;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class WeatherParser {

  public enum WeatherClassifier {
    FREEZING("freezing", 0), COLD("cold", 1), MILD("mild", 2), WARM("warm", 3), HOT("hot", 4);

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

    public static int getNum() { return WeatherClassifier.values().length; }

    @NotNull
    @Override
    public String toString() {
      return weatherDefinition;
    }
  }

  /**
   * Freezing = Less than 5°C Cold = Between 5°C and 10°C Mild = Between 10°C and 20°C Warm =
   * Between 20°C and 35°C Hot = Above 35°C
   */
  public static WeatherClassifier parseTemperatureToClassifier(float temperature) {
    if(temperature >= 35f) return WeatherClassifier.HOT;
    if(temperature < 35f && temperature >= 20f) return WeatherClassifier.WARM;
    if(temperature < 20f && temperature >= 10f) return WeatherClassifier.MILD;
    if(temperature < 10f && temperature >= 5f) return WeatherClassifier.COLD;
    if(temperature < 5f) return WeatherClassifier.FREEZING;

    return null;
  }

}
