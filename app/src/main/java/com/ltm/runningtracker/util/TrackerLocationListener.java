package com.ltm.runningtracker.util;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import com.ltm.runningtracker.android.activity.MainActivity;

public class TrackerLocationListener implements LocationListener {

  @Override
  public void onLocationChanged(Location location) {
    MainActivity.lat = location.getLatitude();
    MainActivity.lon = location.getLongitude();
    Log.d("g53mdp", location.getLatitude() + " " + location.getLongitude());
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // information about the signal, i.e. number of satellites
    Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
  }

  @Override
  public void onProviderEnabled(String provider) {
    // the user enabled (for example) the GPS
    Log.d("g53mdp", "onProviderEnabled: " + provider);
  }

  @Override
  public void onProviderDisabled(String provider) {
    // the user disabled (for example) the GPS
    Log.d("g53mdp", "onProviderDisabled: " + provider);
  }
}