package com.ltm.runningtracker;

import android.app.Application;
import android.content.Context;
import com.ltm.runningtracker.android.contentprovider.DroidContentProvider;
import com.ltm.runningtracker.repository.RunRepository;
import com.ltm.runningtracker.util.PropertyManager;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.repository.UserRepository;
import com.ltm.runningtracker.repository.WeatherRepository;
import com.mapbox.mapboxsdk.Mapbox;

/**
 * https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
 */
public class RunningTrackerApplication extends Application {

  private static Context context;
  private static PropertyManager propertyManager;

  // Repositories
  private static LocationRepository locationRepository;
  private static UserRepository userRepository;
  private static WeatherRepository weatherRepository;
  private static RunRepository runRepository;

  // Content provider
  private static DroidContentProvider droidContentProvider;

  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();

    // Initialise repositories
    propertyManager = new PropertyManager(getResources().getString(R.string.app_properties));
    locationRepository = getLocationRepository();
    userRepository = getUserRepository();
    weatherRepository = getWeatherRepository();
    runRepository = getRunRepository();

    // Mapbox Access token
    Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
  }

  public static Context getAppContext() {
    return RunningTrackerApplication.context;
  }

  public static PropertyManager getPropertyManager() {
    return RunningTrackerApplication.propertyManager;
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

  public static synchronized RunRepository getRunRepository() {
    if (runRepository == null) {
      runRepository = new RunRepository();
    }
    return runRepository;
  }

  public static synchronized DroidContentProvider getDroidContentProvider() {
    if (droidContentProvider == null) {
      droidContentProvider = new DroidContentProvider();
    }
    return droidContentProvider;
  }

}
