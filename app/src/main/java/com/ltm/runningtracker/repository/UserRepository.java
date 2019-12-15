package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTracker.getLocationRepository;
import static com.ltm.runningtracker.RunningTracker.getWeatherRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.RunningTracker;
import com.ltm.runningtracker.User;

public class UserRepository {

  private LocationRepository locationRepository;
  private WeatherRepository weatherRepository;
  private LiveData<User> user;

  public UserRepository() {
    locationRepository = getLocationRepository();
    weatherRepository = getWeatherRepository();
  }

  public LiveData<User> getUser() {
    if(user == null) {
      user = new MutableLiveData<>(new User(locationRepository.getLocation(), weatherRepository.getWeather()));
      return user;
    } else return user;

  }
}
