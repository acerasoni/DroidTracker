package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.survivingwithandroid.weather.lib.model.Weather;

public class RunActivityViewModel extends ViewModel {

  private LiveData<Location> locationLiveData;
  private LiveData<Weather> weatherLiveData;
  private LiveData<Long> durationLiveData;
  private LiveData<Long> distanceLiveData;

  public RunActivityViewModel() {
    locationLiveData = getLocationRepository().getLocationLiveData();
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
    durationLiveData = getRunRepository().getDurationLiveData();
    distanceLiveData = getRunRepository().getDistanceLiveData();
  }

  public LiveData<Location> getLocation() {
    return locationLiveData;
  }

  public LiveData<Weather> getWeather() { return weatherLiveData; }

  public LiveData<Long> getDistance() { return distanceLiveData; }

  public LiveData<Long> getDuration() { return durationLiveData; }

}
