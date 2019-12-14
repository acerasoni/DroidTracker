package com.ltm.runningtracker.android.activity;

import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.location.LocationManager;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.util.TrackerLocationListener;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOProviderType;

public class MainActivity extends AppCompatActivity {

  LocationManager locationManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    locationManager =
        (LocationManager) getSystemService(Context.LOCATION_SERVICE);

  try{
    WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();  WeatherConfig config = new WeatherConfig();
    WeatherClient client = builder.attach(this)
        .provider(new ForecastIOProviderType())
        .httpClient(com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient.class)
        .config(config)
        .build();
  } catch(Exception e) {

  }


  }

  public void onClick(View v) {

    TrackerLocationListener locationListener = new TrackerLocationListener();
    try {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          3, // minimum time interval between updates
          1, // minimum distance between updates, in metres
          locationListener);
    } catch (SecurityException e) {
   //  Log.d("g53mdp", e.toString());
    }
  }
}
