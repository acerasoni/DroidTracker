package com.ltm.runningtracker.weather;

import com.survivingwithandroid.weather.lib.provider.IProviderType;

public class CustomWeatherProviderType implements IProviderType {

  @Override
  public String getProviderClass() {
    return "com.ltm.runningtracker.weather.CustomWeatherProvider";
  }

  @Override
  public String getCodeProviderClass() {
    return "com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOCodeProvider";
  }
}

