package com.ltm.runningtracker.android.service;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;
import static com.ltm.runningtracker.repository.WeatherRepository.buildWeatherClient;
import static com.ltm.runningtracker.repository.WeatherRepository.buildWeatherRequest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;
import androidx.annotation.Nullable;
import com.ltm.runningtracker.repository.LocationRepository;
import com.survivingwithandroid.weather.lib.WeatherClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherService extends Service {

  // Worker thread
  private ScheduledExecutorService scheduledExecutorService;
  private WeatherClient weatherClient;

  // Because weather is dependent on location and will read from the repo, it must be synchronized
  private Object lock;

  @Override
  public void onCreate() {
    super.onCreate();

    weatherClient = buildWeatherClient();
    lock = LocationRepository.getLock();

    // Weather update worker thread
    // Reasons for this:
    // 1. Need to wait for location service to fetch at least one valid location
    // 2. Updating temperature and location must not happen concurrently as this could
    // cause inconsistencies.
    Runnable requestWeatherTask = () -> {
      try {
        synchronized (lock) {
          if (getLocationRepository().getLocation() == null) {
            // This means location has not been fetched yet. Lock will wait until notified
            // by location service - meaning at least one location was successfully retrieved.
            lock.wait();
          }
          // Weather repository listeners for changes in weather (passed as a listener), hence
          // no need to directly post value to repo
          weatherClient
              .getCurrentCondition(buildWeatherRequest(), getWeatherRepository());
        }
      } catch (InterruptedException e) {
        Log.e("WeatherService: ", "StartUp thread interrupted.");
      }

    };

    // Begin execution of worker thread
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService
        .scheduleAtFixedRate(requestWeatherTask, 0, getPropertyManager().getMinTime(),
            TimeUnit.SECONDS);
  }

  // No need to use callbacks as the worker thread updating our temperature client is already implemented
  // we just call it periodically. Activities observe temperature object
  public class WeatherServiceBinder extends Binder implements IInterface {

    @Override
    public IBinder asBinder() {
      return this;
    }

  }

  @Override
  public void onDestroy() {
    scheduledExecutorService.shutdownNow();
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new WeatherServiceBinder();
  }

  @Override
  public void onRebind(Intent intent) {
    super.onRebind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    return super.onUnbind(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return Service.START_STICKY;
  }

  // Must stopSelf() when application is killed
  @Override
  public void onTaskRemoved(Intent intent) {
    stopSelf();
    super.onTaskRemoved(intent);
  }

}
