package com.ltm.runningtracker.weather;

import android.content.Context;
import com.ltm.runningtracker.R;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class WeatherHandler {

  private WeatherConfig weatherConfig;
  private WeatherClient weatherClient;
  private CustomWeatherEventListener customWeatherEventListener;

  public WeatherHandler(Context context) throws WeatherProviderInstantiationException {
    weatherConfig = new WeatherConfig();
    weatherConfig.ApiKey = context.getString(R.string.openweather_api_key);

    weatherClient = (new WeatherClient.ClientBuilder()).attach(context)
        .provider(new ForecastIOProviderType())
        .httpClient(WeatherDefaultClient.class)
        .config(weatherConfig)
        .build();

    customWeatherEventListener = new CustomWeatherEventListener();
  }

  public void getCurrentWeather(double lat, double lon) {
    weatherClient.getCurrentCondition(new WeatherRequest(lat, lon), customWeatherEventListener);
  }

}
