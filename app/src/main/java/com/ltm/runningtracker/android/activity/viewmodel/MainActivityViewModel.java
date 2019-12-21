package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.database.User;
import com.survivingwithandroid.weather.lib.model.Weather;

public class MainActivityViewModel extends ViewModel {

  private LiveData<Location> locationLiveData;
  private LiveData<User> userLiveData;
  private LiveData<Weather> weatherLiveData;

  public MainActivityViewModel() {
    locationLiveData = getLocationRepository().getLocationLiveData();
    userLiveData = getUserRepository().getUser();
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
  }

  public LiveData<User> getUser() {
    return userLiveData;
  }

  public LiveData<Location> getLocation() {
    return locationLiveData;
  }

  public LiveData<Weather> getWeather() { return weatherLiveData; }
}
