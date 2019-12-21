package com.ltm.runningtracker.util;

import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.HashMap;
import java.util.Map;

public class WeatherParser {

  public enum WeatherClassifier {
    FREEZING("Freezing", 0), COLD("Cold", 1), WARM("Warm", 2), HOT("Hot", 3);

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

    @Override
    public String toString(){
      return weatherDefinition;
    }
  }

  public static WeatherClassifier parseWeatherToClassifier(Weather weather) {
    float temperature = weather.temperature.getTemp();

    if (temperature < 35f) {
      if (temperature < 20f) {
        if (temperature < 5f) {
          return WeatherClassifier.FREEZING;
        } else {
          return WeatherClassifier.COLD;
        }
      } else {
        return WeatherClassifier.WARM;
      }
    } else {
      return WeatherClassifier.HOT;
    }
  }
}
