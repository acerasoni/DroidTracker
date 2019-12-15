package com.ltm.runningtracker;

import android.location.Location;
import com.survivingwithandroid.weather.lib.model.Weather;

public class User {

  private Location location;
  private Weather weather;

  public User(Location location, Weather weather) {
    this.location = location;
    this.weather = weather;
  }

}
