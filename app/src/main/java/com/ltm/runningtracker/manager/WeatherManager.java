package com.ltm.runningtracker.manager;

import static com.ltm.runningtracker.RunningTracker.getAppContext;
import static com.ltm.runningtracker.RunningTracker.getLocationRepository;
import static com.ltm.runningtracker.RunningTracker.getPropertyManager;

import android.util.Log;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.RunningTracker;
import com.ltm.runningtracker.User;
import com.ltm.runningtracker.listener.CustomWeatherListener;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.repository.UserRepository;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherManager {

  private WeatherConfig weatherConfig;
  private WeatherClient weatherClient;
  private CustomWeatherListener customWeatherListener;
  private LocationRepository locationRepository;
  private long minUpdateTime;

  public WeatherManager(long minUpdateTime, CustomWeatherListener customWeatherListener) {
    this.minUpdateTime = minUpdateTime;
    this.customWeatherListener = customWeatherListener;
    weatherConfig = new WeatherConfig();
    weatherConfig.ApiKey = getAppContext().getString(R.string.openweather_api_key);
    locationRepository = getLocationRepository();

    try {
      weatherClient = (new WeatherClient.ClientBuilder()).attach(getAppContext())
          .provider(new OpenweathermapProviderType())
          .httpClient(WeatherDefaultClient.class)
          .config(weatherConfig)
          .build();
    } catch (WeatherProviderInstantiationException e) {
      Log.d("Exception:", e.getMessage());
    }
  }

  public void requestWeatherUpdates() {
    Runnable requestWeatherTask = () -> {
      if(locationRepository.getLocation() != null) {
        WeatherRequest weatherRequest = new WeatherRequest(locationRepository.getLocation().getLongitude(), locationRepository.getLocation().getLatitude());
        weatherClient.getCurrentCondition(weatherRequest, customWeatherListener);
      }
    };

    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService.scheduleAtFixedRate(requestWeatherTask, 0, getPropertyManager().getMinTime(),
        TimeUnit.SECONDS);
  }

}
