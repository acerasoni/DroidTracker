package com.ltm.runningtracker;

import android.app.Application;
import android.content.Context;
import com.ltm.runningtracker.manager.PropertyManager;
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
  private static LocationRepository locationRepository;
  private static UserRepository userRepository;
  private static WeatherRepository weatherRepository;

  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();
    propertyManager = new PropertyManager(getResources().getString(R.string.app_properties));
    // Mapbox Access token
    Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
  }

  public static Context getAppContext() {
    return RunningTrackerApplication.context;
  }

  public static PropertyManager getPropertyManager() {
    return RunningTrackerApplication.propertyManager;
  }

  public static LocationRepository getLocationRepository() {
    if(locationRepository == null) {
      locationRepository = new LocationRepository();
      return locationRepository;
   } else return locationRepository;
  }

  public static WeatherRepository getWeatherRepository() {
    if(weatherRepository == null) {
      weatherRepository = new WeatherRepository();
      return weatherRepository;
    } else return weatherRepository;
  }

  public static UserRepository getUserRepository() {
    if(userRepository == null) {
      userRepository = new UserRepository();
      return userRepository;
    } else return userRepository;
  }

}
