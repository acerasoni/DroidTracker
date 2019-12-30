package com.ltm.runningtracker.util;

import com.ltm.runningtracker.android.service.WeatherUpdateService;
import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.HashMap;
import java.util.Map;

public class WeatherParser {

  public enum WeatherClassifier {
    FREEZING("freezing", 0), COLD("cold", 1), MILD("mild", 2), WARM("warm", 3), HOT("hot", 4);

    private String weatherDefinition;
    private int value;
    private static Map map = new HashMap<>();

    private WeatherClassifier(String brand, int value) {
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

    @Override
    public String toString() {
      return weatherDefinition;
    }
  }

  public static String parseTemperatureToString(String temperature) {
    if (temperature.equals("Unavailable")) {
      return "Unavailable";
    }
    return parseTemperatureToClassifier(Float.parseFloat(temperature)).toString();
  }

  /**
   * Freezing = Less than 5°C Cold = Between 5°C and 10°C Mild = Between 10°C and 20°C Warm =
   * Between 20°C and 35°C Hot = Above 35°C
   */
  public static WeatherClassifier parseTemperatureToClassifier(float temperature) {
    if (temperature < 35f) {
      if (temperature < 20f) {
        if (temperature < 10f) {
          if (temperature < 5f) {
            return WeatherClassifier.FREEZING;
          } else {
            return WeatherClassifier.COLD;
          }
        } else {
          return WeatherClassifier.MILD;
        }
      } else {
        return WeatherClassifier.WARM;
      }
    } else {
      return WeatherClassifier.HOT;
    }
  }

}
