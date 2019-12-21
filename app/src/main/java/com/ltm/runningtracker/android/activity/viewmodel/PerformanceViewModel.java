package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.database.Run;
import com.ltm.runningtracker.database.User;
import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.ArrayList;
import java.util.List;

public class PerformanceViewModel extends ViewModel {
  // TODO: Implement the ViewModel


  private LiveData<User> userLiveData;
  private LiveData<Weather> weatherLiveData;
  private List<Run> runLiveData;

  public PerformanceViewModel() {
    userLiveData = getUserRepository().getUser();
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
    runLiveData = getRunRepository().getRuns();
  }

  public LiveData<User> getUser() {
    return userLiveData;
  }

  public LiveData<Weather> getWeather() {
    return weatherLiveData;
  }

  public List<Run> getRuns() {
    return runLiveData;
  }
}