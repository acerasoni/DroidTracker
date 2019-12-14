package com.ltm.runningtracker.android.activity;

import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.location.LocationManager;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.User;
import com.ltm.runningtracker.location.LocationHandler;
import com.ltm.runningtracker.location.TrackerLocationListener;
import com.ltm.runningtracker.weather.WeatherHandler;

public class MainActivity extends AppCompatActivity {

  private WeatherHandler weatherHandler;
  private LocationHandler locationHandler;
  private User user;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    user = new User();
    locationHandler = new LocationHandler(user, this);
    weatherHandler = new WeatherHandler(this);
  }

  public void onClick(View v) {
  }

  public void onClick2(View v) {
    weatherHandler.getCurrentWeather(user.getLatitude(), user.getLongitude());
  }

}
