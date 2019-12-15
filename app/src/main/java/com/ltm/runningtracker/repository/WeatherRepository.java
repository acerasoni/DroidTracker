package com.ltm.runningtracker.repository;

import android.app.Activity;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.manager.WeatherManager;
import com.survivingwithandroid.weather.lib.model.Weather;

public class WeatherRepository {

  public MutableLiveData<Weather> weatherMutableLiveData;
  public WeatherManager weatherManager;

  public WeatherRepository() {
    weatherMutableLiveData = new MutableLiveData<>();
    weatherManager = new WeatherManager();
  }

  public Weather getWeather() {
    return weatherMutableLiveData.getValue();
  }

  public LiveData<Weather> getLiveDataWeather() { return weatherMutableLiveData;}

  public void setWeather(Weather weather) {
    weatherMutableLiveData.setValue(weather);
  }

  public void requestWeatherUpdates(Context context) {
    weatherManager.requestWeatherUpdates(context);
  }

  public void removeUpdates(Context context) {
    weatherManager.removeUpdates(context);
  }

}
