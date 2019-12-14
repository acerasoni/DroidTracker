package com.ltm.runningtracker.weather;

import android.util.Log;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;

public class CustomWeatherEventListener implements WeatherClient.WeatherEventListener {

  @Override
  public void onWeatherRetrieved(CurrentWeather currentWeather) {
    float currentTemp = currentWeather.weather.temperature.getTemp();
    Log.d("WL", "City [" + currentWeather.weather.location.getCity() + "] Current temp ["
        + currentTemp + "]");
  }

  @Override
  public void onWeatherError(WeatherLibException e) {
    Log.d("WL", "Weather Error - parsing data");
    e.printStackTrace();
  }

  @Override
  public void onConnectionError(Throwable throwable) {
    Log.d("WL", "Connection error");
    throwable.printStackTrace();
  }
}