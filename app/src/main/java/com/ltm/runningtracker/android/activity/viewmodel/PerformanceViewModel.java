package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.survivingwithandroid.weather.lib.model.Weather;

public class PerformanceViewModel extends ViewModel {

  private LiveData<Weather> weatherLiveData;

  public PerformanceViewModel() {
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
  }

  public LiveData<Weather> getWeather() {
    return weatherLiveData;
  }

}