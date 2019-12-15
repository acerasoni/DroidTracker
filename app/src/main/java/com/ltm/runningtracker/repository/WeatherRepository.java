package com.ltm.runningtracker.repository;

import android.content.Context;
import android.util.Log;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.listener.CustomWeatherEventListener;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class WeatherRepository {

  private Weather weather;

  private WeatherConfig weatherConfig;
  private WeatherClient weatherClient;
  private CustomWeatherEventListener customWeatherEventListener;


  public WeatherRepository(Context context) {
    weatherConfig = new WeatherConfig();
    weatherConfig.ApiKey = context.getString(R.string.openweather_api_key);

    try{
      weatherClient = (new WeatherClient.ClientBuilder()).attach(context)
          .provider(new OpenweathermapProviderType())
          .httpClient(WeatherDefaultClient.class)
          .config(weatherConfig)
          .build();
    } catch (WeatherProviderInstantiationException e) {
      Log.d("Exception:", e.getMessage());
    }

    customWeatherEventListener = new CustomWeatherEventListener(this);
  }

  public void getCurrentWeather(double lat, double lon) {
    weatherClient.getCurrentCondition(new WeatherRequest(lon, lat), customWeatherEventListener);
  }

  public Weather getWeather() {
    return weather;
  }

  public void setWeather(Weather weather) {
    this.weather = weather;
  }
}
