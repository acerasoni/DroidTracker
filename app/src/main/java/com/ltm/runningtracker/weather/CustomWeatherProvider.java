package com.ltm.runningtracker.weather;

import android.location.Location;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.ApiKeyRequiredException;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.HistoricalWeather;
import com.survivingwithandroid.weather.lib.model.WeatherForecast;
import com.survivingwithandroid.weather.lib.model.WeatherHourForecast;
import com.survivingwithandroid.weather.lib.provider.IWeatherCodeProvider;
import com.survivingwithandroid.weather.lib.provider.IWeatherProvider;
import com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOWeatherProvider;
import com.survivingwithandroid.weather.lib.request.Params;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.survivingwithandroid.weather.lib.util.WeatherUtility;
import java.util.Date;
import java.util.List;

public class CustomWeatherProvider extends ForecastIOWeatherProvider {

  @Override
  public String getQueryCurrentWeatherURL(WeatherRequest request) throws ApiKeyRequiredException {
    return "http://api.openweathermap.org/data/2.5/weather?lat=" + request.getLat() + "&lon=" + request.getLon() + "&APPID=678838031a4065f09e10bff3eda7bc65";
  }
}
