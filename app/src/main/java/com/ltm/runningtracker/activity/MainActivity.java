package com.ltm.runningtracker.activity;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.util.TrackerLocationListener;

public class MainActivity extends AppCompatActivity {

  LocationManager locationManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    locationManager =
        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//    ActivityCompat.requestPermissions(this,
//        new String[]{permission.ACCESS_FINE_LOCATION},
//        0);

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
