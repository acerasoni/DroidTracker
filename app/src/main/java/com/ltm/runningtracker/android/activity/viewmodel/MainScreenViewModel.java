package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.parcelable.User;
import com.ltm.runningtracker.repository.UserRepository;
import com.survivingwithandroid.weather.lib.model.Weather;

public class MainScreenViewModel extends ViewModel {

  private LiveData<Location> locationLiveData;
  private LiveData<User> userLiveData;
  private LiveData<Weather> weatherLiveData;

  public MainScreenViewModel() {
    locationLiveData = getLocationRepository().getLocationLiveData();
    userLiveData = getUserRepository().getUser();
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
  }

  public LiveData<User> getUser() {
    return getUserRepository().getUser();
  }

  public LiveData<Location> getLocation() {
    return locationLiveData;
  }

  public LiveData<Weather> getWeather() { return weatherLiveData; }
}
