package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.database.model.Run;
import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.List;

public class PerformanceViewModel extends ViewModel {

  private LiveData<Weather> weatherLiveData;

  public PerformanceViewModel() {
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
  }

  public LiveData<Weather> getWeather() {
    return weatherLiveData;
  }

}