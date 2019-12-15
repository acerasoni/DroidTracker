package com.ltm.runningtracker.manager;

import static com.ltm.runningtracker.RunningTracker.getAppContext;
import static com.ltm.runningtracker.RunningTracker.getLocationRepository;
import static com.ltm.runningtracker.RunningTracker.getWeatherRepository;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.MainActivity;
import com.ltm.runningtracker.android.service.WeatherCallback;
import com.ltm.runningtracker.android.service.WeatherUpdateService;
import com.ltm.runningtracker.repository.WeatherRepository;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class WeatherManager {

  Activity context;
  private WeatherUpdateService.WeatherServiceBinder myService = null;

  public void requestWeatherUpdates(Activity context) {
    this.context = context;
    context.startService(new Intent(context, WeatherUpdateService.class));
    context.bindService(new Intent(context, WeatherUpdateService.class), serviceConnection, Context.BIND_AUTO_CREATE);
  }

  public void removeUpdates() {

  }

  private ServiceConnection serviceConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      // TODO Auto-generated method stub
      Log.d("g53mdp", "MainActivity onServiceConnected");
      myService = (WeatherUpdateService.WeatherServiceBinder) service;
      myService.registerCallback(callback);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      // TODO Auto-generated method stub
      Log.d("g53mdp", "MainActivity onServiceDisconnected");
      myService.unregisterCallback(callback);
      myService = null;
    }
  };

  WeatherCallback callback = new WeatherCallback() {
    @Override
    public void weatherUpdateEvent(final Weather weather) {
      getWeatherRepository().setWeather(weather);
    }
  };

  // Weather objects builder methods
  public static WeatherRequest buildWeatherRequest() {
    return new WeatherRequest(
        getLocationRepository().getLocation().getLongitude(),
        getLocationRepository().getLocation().getLatitude());
  }

  public static WeatherClient buildWeatherClient() {
    WeatherConfig weatherConfig = new WeatherConfig();
    weatherConfig.ApiKey = getAppContext().getString(R.string.openweather_api_key);
    WeatherClient weatherClient;
    try {
      weatherClient = (new WeatherClient.ClientBuilder()).attach(getAppContext())
          .provider(new OpenweathermapProviderType())
          .httpClient(WeatherDefaultClient.class)
          .config(weatherConfig)
          .build();
      return weatherClient;
    } catch (WeatherProviderInstantiationException e) {
      Log.d("Exception:", e.getMessage());
    }
    return null;
  }

}
