package com.ltm.runningtracker;

import android.app.Application;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import com.ltm.runningtracker.repository.RunRepository;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.repository.UserRepository;
import com.ltm.runningtracker.repository.WeatherRepository;
import com.ltm.runningtracker.util.UpdatePreferences;
import com.mapbox.mapboxsdk.Mapbox;

public class RunningTrackerApplication extends Application {

  private static Context context;
  private static UpdatePreferences updatePreferences;

  // Repositories
  private static LocationRepository locationRepository;
  private static UserRepository userRepository;
  private static WeatherRepository weatherRepository;
  private static RunRepository runRepository;

  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();

    // Initialise mapbox instance with access token
    Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
  }

  public static Context getAppContext() {
    return RunningTrackerApplication.context;
  }

  public static UpdatePreferences getUpdatePreferences() {
    if (updatePreferences == null) {
      updatePreferences = new UpdatePreferences(context);
    }
    return updatePreferences;
  }

  public static synchronized LocationRepository getLocationRepository() {
    if (locationRepository == null) {
      locationRepository = new LocationRepository();
    }
    return locationRepository;
  }

  public static synchronized WeatherRepository getWeatherRepository() {
    if (weatherRepository == null) {
      weatherRepository = new WeatherRepository();

    }
    return weatherRepository;
  }

  public static synchronized UserRepository getUserRepository() {
    if (userRepository == null) {
      userRepository = new UserRepository();
    }
    return userRepository;
  }

  @RequiresApi(api = VERSION_CODES.O)
  public static synchronized RunRepository getRunRepository() {
    if (runRepository == null) {
      runRepository = new RunRepository();
    }
    return runRepository;
  }

}