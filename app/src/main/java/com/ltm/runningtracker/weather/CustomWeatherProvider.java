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

public class CustomWeatherProvider implements IWeatherProvider {


  @Override
  public CurrentWeather getCurrentCondition(String data) throws WeatherLibException {
    return null;
  }

  @Override
  public WeatherForecast getForecastWeather(String data) throws WeatherLibException {
    return null;
  }

  @Override
  public List<City> getCityResultList(String data) throws WeatherLibException {
    return null;
  }

  @Override
  public WeatherHourForecast getHourForecastWeather(String data) throws WeatherLibException {
    return null;
  }

  @Override
  public String getQueryCityURL(String cityNamePattern) throws ApiKeyRequiredException {
    return null;
  }

  @Override
  public HistoricalWeather getHistoricalWeather(String data) throws WeatherLibException {
    return null;
  }

  @Override
  public String getQueryCityURLByLocation(Location location) throws ApiKeyRequiredException {
    return null;
  }

  @Override
  public String getQueryCityURLByCoord(double lon, double lat) throws ApiKeyRequiredException {
    return null;
  }

  @Override
  public void setConfig(WeatherConfig config) {

  }

  @Override
  public void setWeatherCodeProvider(IWeatherCodeProvider codeProvider) {

  }

  @Override
  public String getQueryImageURL(String weatherId) throws ApiKeyRequiredException {
    return null;
  }

  @Override
  public String getQueryLayerURL(String cityId, Params params) throws ApiKeyRequiredException {
    return null;
  }

  @Override
  public String getQueryCurrentWeatherURL(WeatherRequest request) throws ApiKeyRequiredException {
    return null;
  }

  @Override
  public String getQueryForecastWeatherURL(WeatherRequest request) throws ApiKeyRequiredException {
    return null;
  }

  @Override
  public String getQueryHourForecastWeatherURL(WeatherRequest request)
      throws ApiKeyRequiredException {
    return null;
  }

  @Override
  public String getQueryHistoricalWeatherURL(WeatherRequest request, Date startDate, Date endDate)
      throws ApiKeyRequiredException {
    return null;
  }
}
