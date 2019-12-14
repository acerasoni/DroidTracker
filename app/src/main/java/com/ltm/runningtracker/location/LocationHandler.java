package com.ltm.runningtracker.location;

import android.content.Context;
import android.location.LocationManager;
import android.util.Log;
import com.ltm.runningtracker.User;

public class LocationHandler {

  private TrackerLocationListener trackerLocationListener;
  private LocationManager locationManager;

  public LocationHandler(User user, Context context) {
    trackerLocationListener = new TrackerLocationListener(user);
    locationManager =
        (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    locationManager =
        (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    try {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          3, // minimum time interval between updates
          1, // minimum distance between updates, in metres
          trackerLocationListener);
    } catch (SecurityException e) {
      Log.d("g53mdp", e.toString());
    }
  }
}
