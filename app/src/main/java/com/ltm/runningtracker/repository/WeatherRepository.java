package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTracker.getPropertyManager;

import com.ltm.runningtracker.RunningTracker;
import com.ltm.runningtracker.listener.CustomWeatherListener;
import com.ltm.runningtracker.manager.WeatherManager;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class WeatherRepository {

  private Weather weather;
  public WeatherManager weatherManager;

  public WeatherRepository() {
    weatherManager = new WeatherManager(getPropertyManager().getMinTime(), new CustomWeatherListener(this));
    weatherManager.requestWeatherUpdates();
  }

  public Weather getWeather() {
    return weather;
  }

  public void setWeather(Weather weather) {
    this.weather = weather;
  }

}
