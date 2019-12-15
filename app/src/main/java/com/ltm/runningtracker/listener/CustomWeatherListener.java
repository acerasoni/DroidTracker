package com.ltm.runningtracker.listener;

import android.os.RemoteCallbackList;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.ltm.runningtracker.android.service.WeatherUpdateService.WeatherServiceBinder;
import com.ltm.runningtracker.repository.WeatherRepository;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.Weather;

/**
 * This class is responsible for storing the current Weather object and providing access to the
 * service
 */
public class CustomWeatherListener implements WeatherClient.WeatherEventListener {

  RemoteCallbackList<WeatherServiceBinder> remoteCallbackList;

  public CustomWeatherListener(RemoteCallbackList<WeatherServiceBinder> remoteCallbackList) {
    this.remoteCallbackList = remoteCallbackList;
  }

  @Override
  public void onWeatherRetrieved(CurrentWeather currentWeather) {
    doCallbacks(currentWeather.weather);
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

  public void doCallbacks(Weather weather) {
    final int n = remoteCallbackList.beginBroadcast();
    for (int i=0; i<n; i++) {
      remoteCallbackList.getBroadcastItem(i).callback.weatherUpdateEvent(weather);
    }
    remoteCallbackList.finishBroadcast();
  }

}