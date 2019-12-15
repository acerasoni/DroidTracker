package com.ltm.runningtracker.listener;

import android.util.Log;
import com.ltm.runningtracker.repository.WeatherRepository;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.Weather;

public class CustomWeatherEventListener implements WeatherClient.WeatherEventListener {

  private WeatherRepository weatherRepository;

  public CustomWeatherEventListener(WeatherRepository weatherRepository) {
    this.weatherRepository = weatherRepository;
  }

  @Override
  public void onWeatherRetrieved(CurrentWeather currentWeather) {
    weatherRepository.setWeather(currentWeather.weather);
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