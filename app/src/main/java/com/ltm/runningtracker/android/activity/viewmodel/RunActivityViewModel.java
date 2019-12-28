package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.survivingwithandroid.weather.lib.model.Weather;

public class RunActivityViewModel extends ViewModel {
  private LiveData<Location> locationLiveData;
  private LiveData<Weather> weatherLiveData;

  public RunActivityViewModel() {
    locationLiveData = getLocationRepository().getLocationLiveData();
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
  }

  public LiveData<Location> getLocation() {
    return locationLiveData;
  }

  public LiveData<Weather> getWeather() { return weatherLiveData; }
}
