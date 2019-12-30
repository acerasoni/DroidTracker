package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;

import android.content.Intent;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.service.WeatherService;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class WeatherRepository implements WeatherClient.WeatherEventListener {

  public MutableLiveData<Weather> weatherMutableLiveData;

  public WeatherRepository() {
    weatherMutableLiveData = new MutableLiveData<>();
  }

  public LiveData<Weather> getLiveDataWeather() { return weatherMutableLiveData;}

  private void setWeather(Weather weather) {
    weatherMutableLiveData.setValue(weather);
  }

  public String getTemperature() {
    return Float.toString(weatherMutableLiveData.getValue().temperature.getTemp());
  }

  @Override
  public void onWeatherRetrieved(CurrentWeather weather) {
    setWeather(weather.weather);
  }

  @Override
  public void onWeatherError(WeatherLibException wle) {

  }

  @Override
  public void onConnectionError(Throwable t) {

  }

  public void onDestroy() {
    getAppContext().stopService(new Intent(getAppContext(), WeatherService.class));
  }

  // Weather request builder method
  public static WeatherRequest buildWeatherRequest() {
    return new WeatherRequest(
        getLocationRepository().getLongitude(),
        getLocationRepository().getLatitude());
  }

  // Weather client builder method
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
