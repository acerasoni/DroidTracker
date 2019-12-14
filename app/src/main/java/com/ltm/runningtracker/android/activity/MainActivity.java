package com.ltm.runningtracker.android.activity;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.location.LocationManager;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.util.TrackerLocationListener;
import com.ltm.runningtracker.weather.WeatherHandler;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOProviderType;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class MainActivity extends AppCompatActivity {

  LocationManager locationManager;
  WeatherHandler weatherHandler;
  TrackerLocationListener trackerLocationListener;
  public static double lat, lon;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    trackerLocationListener = new TrackerLocationListener();
    locationManager =
        (LocationManager) getSystemService(Context.LOCATION_SERVICE);


  }

  public void onClick(View v) {

    locationManager =
        (LocationManager) getSystemService(Context.LOCATION_SERVICE);


    try {
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        3, // minimum time interval between updates
        1, // minimum distance between updates, in metres
        trackerLocationListener);
  } catch (SecurityException e) {
      Log.d("g53mdp", e.toString());
  }

        try {
      weatherHandler = new WeatherHandler(this);
    } catch (Exception e) {
      Log.d("Exception", e.getMessage());
    }
  }

  public void onClick2(View v) {

    weatherHandler.getCurrentWeather(lat, lon);
  }

}
