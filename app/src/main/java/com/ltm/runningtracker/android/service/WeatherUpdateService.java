package com.ltm.runningtracker.android.service;

import static com.ltm.runningtracker.RunningTracker.getAppContext;
import static com.ltm.runningtracker.RunningTracker.getLocationRepository;
import static com.ltm.runningtracker.RunningTracker.getPropertyManager;
import static com.ltm.runningtracker.RunningTracker.getWeatherRepository;
import static com.ltm.runningtracker.manager.WeatherManager.buildWeatherClient;
import static com.ltm.runningtracker.manager.WeatherManager.buildWeatherRequest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;
import androidx.annotation.Nullable;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.listener.CustomWeatherListener;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherUpdateService extends Service {

  // Worker thread
  ScheduledExecutorService scheduledExecutorService;

  RemoteCallbackList<WeatherServiceBinder> remoteCallbackList = new RemoteCallbackList<>();
  CustomWeatherListener customWeatherListener;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d("hi","ho");
    customWeatherListener = new CustomWeatherListener(remoteCallbackList);
    Runnable requestWeatherTask = () -> {
      if (getLocationRepository().getLocation() != null) {
        WeatherRequest weatherRequest = buildWeatherRequest();
        buildWeatherClient()
            .getCurrentCondition(weatherRequest, customWeatherListener);
      }
    };

    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService
        .scheduleAtFixedRate(requestWeatherTask, 0, getPropertyManager().getMinTime(),
            TimeUnit.SECONDS);
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
    // TODO Auto-generated method stub
    Log.d("g53mdp", "service onRebind");
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

  public class WeatherServiceBinder extends Binder implements IInterface
  {
    @Override
    public IBinder asBinder() {
      return this;
    }

    public void registerCallback(WeatherCallback callback) {
      this.callback = callback;
      remoteCallbackList.register(WeatherServiceBinder.this);
    }

    public void unregisterCallback(WeatherCallback callback) {
      remoteCallbackList.unregister(WeatherServiceBinder.this);
    }

    public WeatherCallback callback;
  }

}
