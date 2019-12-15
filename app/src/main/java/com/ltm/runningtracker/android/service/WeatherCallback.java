package com.ltm.runningtracker.android.service;

import com.survivingwithandroid.weather.lib.model.Weather;

public interface WeatherCallback {

  void weatherUpdateEvent(Weather weather);

}
