package com.ltm.runningtracker.weather;

import com.survivingwithandroid.weather.lib.provider.IProviderType;

public class CustomWeatherProviderType implements IProviderType {

  @Override
  public String getProviderClass() {
    return "com.ltm.runningtracker.temperature.CustomWeatherProvider";
  }

  @Override
  public String getCodeProviderClass() {
    return "com.survivingwithandroid.temperature.lib.provider.forecastio.ForecastIOCodeProvider";
  }
}

