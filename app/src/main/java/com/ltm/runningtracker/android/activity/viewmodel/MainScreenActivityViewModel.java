package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.database.model.User;
import com.survivingwithandroid.weather.lib.model.Weather;

public class MainScreenActivityViewModel extends ViewModel {

  private LiveData<Location> locationLiveData;
  private LiveData<User> userLiveData;
  private LiveData<Weather> weatherLiveData;
  private LiveData<String> countyLiveData;

  public MainScreenActivityViewModel() {
    locationLiveData = getLocationRepository().getLocationLiveData();
    userLiveData = getUserRepository().getUserLiveData();
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
    countyLiveData = getLocationRepository().getCountyLiveData();
  }

  public LiveData<Location> getLocation() {
    return locationLiveData;
  }

  public LiveData<Weather> getWeather() {
    return weatherLiveData;
  }

  public LiveData<String> getCounty() {
    return countyLiveData;
  }

  public LiveData<User> getUser() { return userLiveData; }

  public boolean doesUserExist() {
    return getUserRepository().doesUserExist();
  }
}
