package com.ltm.runningtracker.android.service;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;
import static com.ltm.runningtracker.repository.WeatherRepository.buildWeatherClient;
import static com.ltm.runningtracker.repository.WeatherRepository.buildWeatherRequest;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ltm.runningtracker.android.service.WeatherService.WeatherServiceBinder;
import com.ltm.runningtracker.exception.InvalidLatitudeOrLongitudeException;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.survivingwithandroid.weather.lib.WeatherClient;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service {

  @Override
  public void onCreate() {
    super.onCreate();

    try {
      Log.d("Trying", "troo");
      // The following requestLocationUpdates call will spin up a background thread which we can listen to
      // by implementing the LocationEngineCallback interface
      LocationEngineRequest locationEngineRequest = new LocationEngineRequest.Builder(
          getPropertyManager().getMinTime()).build();
      // Similar to weather service, we pass the location repository as listener and allow it
      // to update itself when callback occurs
      getLocationRepository().getLocationEngine().requestLocationUpdates(locationEngineRequest, getLocationRepository(), null);
    } catch (SecurityException e) {
      Log.d("Security exception: ", e.toString());
    }
  }

  @Override
  public void onDestroy() {
    Log.d("Weather Service", "onDestroy");
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new LocationService.LocationServiceBinder();
  }

  @Override
  public void onRebind(Intent intent) {
    // TODO Auto-generated method stub
    Log.d("Weather Service", "onUnbind");
    super.onRebind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    // TODO Auto-generated method stub
    Log.d("g53mdp", "service onUnbind");
    return super.onUnbind(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // TODO Auto-generated method stub
    Log.d("g53mdp", "service onStartCommand");
    return Service.START_STICKY;
  }

  // Must stopSelf() when application is killed
  @Override
  public void onTaskRemoved(Intent intent) {
    stopSelf();
    super.onTaskRemoved(intent);
  }

  // No need to use callbacks as the worker thread updating our temperature client is already implemented
  // we just call it periodically. Activities observe temperature object
  public class LocationServiceBinder extends Binder implements IInterface {

    @Override
    public IBinder asBinder() {
      return this;
    }

  }

}
