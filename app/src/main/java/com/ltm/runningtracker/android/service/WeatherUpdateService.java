package com.ltm.runningtracker.android.service;

import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;
import static com.ltm.runningtracker.repository.WeatherRepository.buildWeatherClient;
import static com.ltm.runningtracker.repository.WeatherRepository.buildWeatherRequest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;
import androidx.annotation.Nullable;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherUpdateService extends Service {

  // Worker thread
  private ScheduledExecutorService scheduledExecutorService;
  private WeatherClient weatherClient;

  @Override
  public void onCreate() {
    super.onCreate();
    weatherClient = buildWeatherClient();

    Runnable requestWeatherTask = () -> {
      weatherClient
          .getCurrentCondition(buildWeatherRequest(), getWeatherRepository());
    };

    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService
        .scheduleAtFixedRate(requestWeatherTask, 0, getPropertyManager().getMinTime(),
            TimeUnit.SECONDS);
  }

  @Override
  public void onDestroy() {
    Log.d("Weather Service", "onDestroy");
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

  public class WeatherServiceBinder extends Binder implements IInterface {

    @Override
    public IBinder asBinder() {
      return this;
    }

  }

}
