package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.parcelable.User;

public class UserRepository {

  private LocationRepository locationRepository;
  private WeatherRepository weatherRepository;
  private MutableLiveData<User> user;

  public UserRepository() {
    user = new MutableLiveData<>(new User());
    locationRepository = getLocationRepository();
    weatherRepository = getWeatherRepository();
  }

  public LiveData<User> getUser() {
      return user;
  }
}
