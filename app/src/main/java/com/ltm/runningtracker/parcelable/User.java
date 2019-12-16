package com.ltm.runningtracker.parcelable;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.survivingwithandroid.weather.lib.model.Weather;

public class User {

  public User() {
    lol = (float) Math.random();
  }
  public float getLol() {
    return lol;
  }

  public void setLol(float lol) {
    this.lol = lol;
  }

  private float lol = 2f;
}
