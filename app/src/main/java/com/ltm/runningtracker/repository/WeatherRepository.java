package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.R;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import java.util.Objects;

/**
 * Repository responsible for storing, exposing and updating a Weather object which acts as cache to
 * the database.
 */
public class WeatherRepository implements WeatherClient.WeatherEventListener {

  private MutableLiveData<Weather> weatherMutableLiveData;

  public WeatherRepository() {
    weatherMutableLiveData = new MutableLiveData<>();
  }

  public LiveData<Weather> getLiveDataWeather() {
    return weatherMutableLiveData;
  }

  public synchronized float getTemperature() {
    return Objects.requireNonNull(weatherMutableLiveData.getValue()).temperature.getTemp();
  }

  @Override
  public synchronized void onWeatherRetrieved(CurrentWeather weather) {
    setWeather(weather.weather);
  }

  @Override
  public void onWeatherError(WeatherLibException wle) {

  }

  @Override
  public void onConnectionError(Throwable t) {

  }

  /**
   * This method will build a weather request specific to the ccurrent latitude and longitude
   */
  public static WeatherRequest buildWeatherRequest() {
    return new WeatherRequest(
        getLocationRepository().getLongitude(),
        getLocationRepository().getLatitude());
  }

  /**
   * This method will build the HTTP client which hits the OpenWeatherAPI endpoint
   */
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
      Log.e("Weather Repository: ", e.getMessage());
    }
    return null;
  }

  private synchronized void setWeather(Weather weather) {
    weatherMutableLiveData.setValue(weather);
  }

}