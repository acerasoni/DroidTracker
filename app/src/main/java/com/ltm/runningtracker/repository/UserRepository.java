package com.ltm.runningtracker.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.RunningTracker;
import com.ltm.runningtracker.User;

public class UserRepository {

  private LocationRepository locationRepository;
  private WeatherRepository weatherRepository;
  private LiveData<User> user;

  public UserRepository() {
    locationRepository = new LocationRepository(RunningTracker.getAppContext());
    weatherRepository = new WeatherRepository(RunningTracker.getAppContext());
  }

  public LiveData<User> getUser() {
    if(user == null) {
      user = new MutableLiveData<>(new User(locationRepository.getLocation(), weatherRepository.getWeather()));
      return user;
    } else return user;

  }
}
